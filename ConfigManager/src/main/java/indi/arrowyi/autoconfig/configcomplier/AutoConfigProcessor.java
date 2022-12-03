package indi.arrowyi.autoconfig.configcomplier;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import indi.arrowyi.autoconfig.AutoRegister;
import indi.arrowyi.autoconfig.AutoRegisterToDefault;
import indi.arrowyi.autoconfig.configmanager.AutoConfig;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes({"indi.arrowyi.autoconfig.AutoConfig"
        , "indi.arrowyi.autoconfig.AutoRegisterToDefault"})
public class AutoConfigProcessor extends AbstractProcessor {
    private static class ConfigItemInfo {
        String key;
        AutoRegister.Type type;
        String defaultValue;
        String accessor;
        String defaultLoader;
    }

    private List<ConfigItemInfo> items = new ArrayList<>();

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        printMessageW("begin to process indi.arrowyi.configcomplier.AutoConfigProcessor");

        if (processCommonSettings(roundEnvironment)) {
            if (items.size() < 1) {
                printMessageW("no items found for this round ");
            } else {
                generateFile(items);
                items.clear();
            }
        }

        printMessageW("end of process indi.arrowyi.configcomplier.CommonSettingsProcessor");
        return true;
    }

    private boolean processCommonSettings(RoundEnvironment roundEnvironment) {
        boolean res = false;
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(AutoRegister.class);
        printMessageW("elements size is " + elements.size());

        Set<? extends Element> elements1 = roundEnvironment.getElementsAnnotatedWith(AutoRegisterToDefault.class);
        printMessageW("elements1 size is " + elements1.size());

        printMessageW("items size is " + items.size());


        if (elements != null || elements.size() > 0) {
            res |= handleAutoRegister(elements, (VariableElement variableElement) -> {
                ConfigItemInfo configItemInfo = new ConfigItemInfo();

                String key = variableElement.getConstantValue().toString();
                AutoRegister annotation = variableElement.getAnnotation(AutoRegister.class);

                if(annotation == null){
                    printMessageW("get AutoRegister is null");
                    return null;
                }
                AutoRegister.Type type = annotation.type();
                String accessor = annotation.accessor();
                String defaultValue = annotation.defaultValue();
                String defaultLoader = annotation.defaultLoader();

                if (key == null || key.isEmpty()) {
                    printMessageE("handleAutoConfigRegister : the element's value is null --> " + variableElement.getSimpleName());
                    return null;
                }

                configItemInfo.key = key;
                configItemInfo.type = type;

                if (accessor != null ) {
                    configItemInfo.accessor = accessor;
                }

                if (defaultLoader != null) {
                    configItemInfo.defaultLoader = defaultLoader;
                }

                if (defaultValue != null) {
                    configItemInfo.defaultValue = defaultValue;
                }

                return configItemInfo;
            });
        }

        if (elements1 != null && elements1.size() > 0) {
            res |= handleAutoRegister(elements1, (VariableElement variableElement) -> {
                ConfigItemInfo configItemInfo = new ConfigItemInfo();

                String key = variableElement.getConstantValue().toString();
                AutoRegisterToDefault annotation = variableElement.getAnnotation(AutoRegisterToDefault.class);
                if(annotation == null){
                    printMessageW("get AutoRegisterToDefault is null and annotation is " + variableElement);
                    return null;
                }
                AutoRegister.Type type = annotation.type();
                String accessor = annotation.accessor();
                String defaultValue = annotation.defaultValue();
                String defaultLoader = annotation.defaultLoader();

                if (key == null || key.isEmpty()) {
                    printMessageE("handleAutoConfigRegister : the element's value is null --> " + variableElement.getSimpleName());
                    return null;
                }

                configItemInfo.key = key;
                configItemInfo.type = type;

                if (accessor != null ) {
                    configItemInfo.accessor = accessor;
                }

                if (defaultLoader != null) {
                    configItemInfo.defaultLoader = defaultLoader;
                }

                if (defaultValue != null) {
                    configItemInfo.defaultValue = defaultValue;
                }

                return configItemInfo;
            });
        }

        return res;

    }

    private boolean handleAutoRegister(Set<? extends Element> elements, Function<VariableElement, ConfigItemInfo> function) {
        for (Element element : elements) {
            if (element.getKind() != ElementKind.FIELD) {
                printMessageE("handleAutoConfigRegister : the element kind is not ok, it is " + element.getSimpleName());
                continue;
            }

            VariableElement variableElement = (VariableElement) element;
            TypeMirror variable = variableElement.asType();
            TypeMirror string = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
            Types typeUtils = processingEnv.getTypeUtils();

            Set<Modifier> modifiers = element.getModifiers();
            if (!(modifiers.contains(Modifier.STATIC) && modifiers.contains(Modifier.FINAL))) {
                printMessageE("handleAutoConfigRegister : the element modifier is not ok : " + variableElement.getSimpleName());
                continue;
            }


            if (!typeUtils.isSameType(variable, string)) {
                printMessageE("handleAutoConfigRegister : the element's type is not ok , it is " + variableElement.getSimpleName());
                continue;
            }


            ConfigItemInfo configItemInfo = function.apply(variableElement);
            if (configItemInfo != null) {
                items.add(configItemInfo);
            }
        }

        return true;
    }

    private boolean generateFile(List<ConfigItemInfo> items) {
        ClassName ConfigRegister = ClassName.get("indi.arrowyi.autoconfig.configmanager"
                , "ConfigRegister");

        TypeSpec.Builder commonSettingsDefBuilder = TypeSpec.classBuilder(items.get(0).key + "CommonSettingsDef")
                .addSuperinterface(ConfigRegister).addAnnotation(AnnotationSpec.builder(AutoService.class)
                        .addMember("value", "ConfigRegister.class").build()).addModifiers(Modifier.PUBLIC);

        ClassName autoConfig = ClassName.get("indi.arrowyi.autoconfig.configmanager", "AutoConfig");
        ClassName aa = ClassName.get(AutoConfig.Type.class);

        MethodSpec.Builder registerKeyMethod = MethodSpec.methodBuilder("register")
                .addAnnotation(Override.class).addModifiers(Modifier.PUBLIC).returns(TypeName.VOID)
                .addParameter(ParameterSpec.builder(autoConfig, "config").build());

        for (ConfigItemInfo configItemInfo : items) {
            AutoConfig.Type type = TypeMap(configItemInfo.type);
            registerKeyMethod.addStatement("config.register($S, $T.$L, $S, $S, $S)"
                    , configItemInfo.key, ClassName.get("indi.arrowyi.autoconfig.configmanager.AutoConfig"
                            , "Type"), type.name(), configItemInfo.accessor, configItemInfo.defaultLoader
                    , configItemInfo.defaultValue);
        }

        commonSettingsDefBuilder.addMethod(registerKeyMethod.build());

        JavaFile javaFile = JavaFile.builder("indi.arrowyi.autoconfig.configmanager"
                , commonSettingsDefBuilder.build()).build();

        Filer filer = processingEnv.getFiler();

        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private AutoConfig.Type TypeMap(AutoRegister.Type type) {
        switch (type) {
            case BOOLEAN:
                return AutoConfig.Type.BOOLEAN;
            case INT:
                return AutoConfig.Type.INT;
            case STRING:
                return AutoConfig.Type.STRING;
            case FLOAT:
                return AutoConfig.Type.FLOAT;
            case DOUBLE:
                return AutoConfig.Type.DOUBLE;
            case LONG:
                return AutoConfig.Type.LONG;
        }

        return null;
    }

    private void printMessageW(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg);
    }

    private void printMessageE(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);

    }

}
