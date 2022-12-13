/*
 * Copyright (c) 2022.  Arrowyi. All rights reserved
 * email : arrowyi@gmail.com
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package indi.arrowyi.autoconfig.configcomplier;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import indi.arrowyi.autoconfig.*;
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

import static indi.arrowyi.autoconfig.configmanager.AutoConfig.DEFAULT_ACCESSOR;
import static indi.arrowyi.autoconfig.configmanager.AutoConfig.DEFAULT_LOADER;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"indi.arrowyi.autoconfig.AutoRegisterInt"
        , "indi.arrowyi.autoconfig.AutoRegisterLong"
        , "indi.arrowyi.autoconfig.AutoRegisterDouble"
        , "indi.arrowyi.autoconfig.AutoRegisterFloat"
        , "indi.arrowyi.autoconfig.AutoRegisterBoolean"
        , "indi.arrowyi.autoconfig.AutoRegisterString"
        , "indi.arrowyi.autoconfig.AutoRegisterIntWithDefault"
        , "indi.arrowyi.autoconfig.AutoRegisterLongWithDefault"
        , "indi.arrowyi.autoconfig.AutoRegisterFloatWithDefault"
        , "indi.arrowyi.autoconfig.AutoRegisterDoubleWithDefault"
        , "indi.arrowyi.autoconfig.AutoRegisterStringWithDefault"
        , "indi.arrowyi.autoconfig.AutoRegisterBooleanWithDefault"})
public class AutoConfigProcessor extends AbstractProcessor {

    enum Type {
        BOOLEAN,
        INT,
        FLOAT,
        DOUBLE,
        STRING,
        LONG
    }

    private static class ConfigItemInfo {
        String key;
        Type type;
        Object defaultValue;
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
        Set<? extends Element> elementsInt = roundEnvironment.getElementsAnnotatedWith(AutoRegisterInt.class);
        printMessageW("elementsInt size is " + elementsInt.size());

        Set<? extends Element> elementsLong = roundEnvironment.getElementsAnnotatedWith(AutoRegisterLong.class);
        printMessageW("elementsLong size is " + elementsLong.size());

        Set<? extends Element> elementsBoolean = roundEnvironment.getElementsAnnotatedWith(AutoRegisterBoolean.class);
        printMessageW("elementsBoolean size is " + elementsBoolean.size());

        Set<? extends Element> elementsFloat = roundEnvironment.getElementsAnnotatedWith(AutoRegisterFloat.class);
        printMessageW("elementsFloat size is " + elementsFloat.size());

        Set<? extends Element> elementsDouble = roundEnvironment.getElementsAnnotatedWith(AutoRegisterDouble.class);
        printMessageW("elementsDouble size is " + elementsDouble.size());

        Set<? extends Element> elementsString = roundEnvironment.getElementsAnnotatedWith(AutoRegisterString.class);
        printMessageW("elementsString size is " + elementsString.size());


        Set<? extends Element> elementsIntDefault = roundEnvironment.getElementsAnnotatedWith(AutoRegisterIntWithDefault.class);
        printMessageW("elementsIntDefault size is " + elementsIntDefault.size());

        Set<? extends Element> elementsLongDefault = roundEnvironment.getElementsAnnotatedWith(AutoRegisterLongWithDefault.class);
        printMessageW("elementsLongDefault size is " + elementsLongDefault.size());

        Set<? extends Element> elementsFloatDefault = roundEnvironment.getElementsAnnotatedWith(AutoRegisterFloatWithDefault.class);
        printMessageW("elementsFloatDefault size is " + elementsFloatDefault.size());

        Set<? extends Element> elementsDoubleDefault = roundEnvironment.getElementsAnnotatedWith(AutoRegisterDoubleWithDefault.class);
        printMessageW("elementsDoubleDefault size is " + elementsDoubleDefault.size());

        Set<? extends Element> elementsBooleanDefault = roundEnvironment.getElementsAnnotatedWith(AutoRegisterBooleanWithDefault.class);
        printMessageW("elementsBooleanDefault size is " + elementsBooleanDefault.size());

        Set<? extends Element> elementsStringDefault = roundEnvironment.getElementsAnnotatedWith(AutoRegisterStringWithDefault.class);
        printMessageW("elementsStringDefault size is " + elementsStringDefault.size());

        printMessageW("items size is " + items.size());


        if (elementsInt != null && elementsInt.size() > 0) {
            res |= handleAutoRegister(elementsInt, (VariableElement variableElement) -> {
                AutoRegisterInt annotation = variableElement.getAnnotation(AutoRegisterInt.class);
                if (annotation == null) {
                    printMessageW("get AutoRegisterInt is null");
                    return null;
                }
                return parseAnnotation(Type.INT, annotation.defaultValue(), annotation.accessor(), annotation.defaultLoader());
            });
        }

        if (elementsLong != null && elementsLong.size() > 0) {
            res |= handleAutoRegister(elementsLong, (VariableElement variableElement) -> {
                AutoRegisterLong annotation = variableElement.getAnnotation(AutoRegisterLong.class);
                if (annotation == null) {
                    printMessageW("get AutoRegisterLong is null");
                    return null;
                }
                return parseAnnotation(Type.LONG, annotation.defaultValue(), annotation.accessor(), annotation.defaultLoader());
            });
        }

        if (elementsBoolean != null && elementsBoolean.size() > 0) {
            res |= handleAutoRegister(elementsBoolean, (VariableElement variableElement) -> {
                AutoRegisterBoolean annotation = variableElement.getAnnotation(AutoRegisterBoolean.class);
                if (annotation == null) {
                    printMessageW("get AutoRegisterBoolean is null");
                    return null;
                }
                return parseAnnotation(Type.BOOLEAN, annotation.defaultValue(), annotation.accessor(), annotation.defaultLoader());
            });
        }

        if (elementsFloat != null && elementsFloat.size() > 0) {
            res |= handleAutoRegister(elementsFloat, (VariableElement variableElement) -> {
                AutoRegisterFloat annotation = variableElement.getAnnotation(AutoRegisterFloat.class);
                if (annotation == null) {
                    printMessageW("get AutoRegisterFloat is null");
                    return null;
                }
                return parseAnnotation(Type.FLOAT, annotation.defaultValue(), annotation.accessor(), annotation.defaultLoader());
            });
        }

        if (elementsDouble != null && elementsDouble.size() > 0) {
            res |= handleAutoRegister(elementsDouble, (VariableElement variableElement) -> {
                AutoRegisterDouble annotation = variableElement.getAnnotation(AutoRegisterDouble.class);
                if (annotation == null) {
                    printMessageW("get AutoRegisterDouble is null");
                    return null;
                }
                return parseAnnotation(Type.DOUBLE, annotation.defaultValue(), annotation.accessor(), annotation.defaultLoader());
            });
        }

        if (elementsString != null && elementsString.size() > 0) {
            res |= handleAutoRegister(elementsString, (VariableElement variableElement) -> {
                AutoRegisterString annotation = variableElement.getAnnotation(AutoRegisterString.class);
                if (annotation == null) {
                    printMessageW("get AutoRegisterString is null");
                    return null;
                }
                return parseAnnotation(Type.STRING, annotation.defaultValue(), annotation.accessor(), annotation.defaultLoader());
            });
        }


        if (elementsIntDefault != null && elementsIntDefault.size() > 0) {
            res |= handleAutoRegister(elementsIntDefault, (VariableElement variableElement) -> {
                AutoRegisterIntWithDefault annotation = variableElement.getAnnotation(AutoRegisterIntWithDefault.class);
                if (annotation == null) {
                    printMessageW("get AutoRegisterString is null");
                    return null;
                }
                return parseAnnotationWithDefault(Type.INT, 0);
            });
        }

        if (elementsLongDefault != null && elementsLongDefault.size() > 0) {
            res |= handleAutoRegister(elementsLongDefault, (VariableElement variableElement) -> {
                AutoRegisterLongWithDefault annotation = variableElement.getAnnotation(AutoRegisterLongWithDefault.class);
                if (annotation == null) {
                    printMessageW("get elementsLongDefault is null");
                    return null;
                }
                return parseAnnotationWithDefault(Type.LONG, 0L);
            });
        }

        if (elementsFloatDefault != null && elementsFloatDefault.size() > 0) {
            res |= handleAutoRegister(elementsFloatDefault, (VariableElement variableElement) -> {
                AutoRegisterFloatWithDefault annotation = variableElement.getAnnotation(AutoRegisterFloatWithDefault.class);
                if (annotation == null) {
                    printMessageW("get AutoRegisterFloatWithDefault is null");
                    return null;
                }
                return parseAnnotationWithDefault(Type.FLOAT, 0.0f);
            });
        }

        if (elementsDoubleDefault != null && elementsDoubleDefault.size() > 0) {
            res |= handleAutoRegister(elementsDoubleDefault, (VariableElement variableElement) -> {
                AutoRegisterDoubleWithDefault annotation = variableElement.getAnnotation(AutoRegisterDoubleWithDefault.class);
                if (annotation == null) {
                    printMessageW("get AutoRegisterDoubleWithDefault is null");
                    return null;
                }
                return parseAnnotationWithDefault(Type.DOUBLE, 0.0);
            });
        }

        if (elementsBooleanDefault != null && elementsBooleanDefault.size() > 0) {
            res |= handleAutoRegister(elementsBooleanDefault, (VariableElement variableElement) -> {
                AutoRegisterBooleanWithDefault annotation = variableElement.getAnnotation(AutoRegisterBooleanWithDefault.class);
                if (annotation == null) {
                    printMessageW("get AutoRegisterBooleanWithDefault is null");
                    return null;
                }
                return parseAnnotationWithDefault(Type.BOOLEAN, false);
            });
        }

        if (elementsStringDefault != null && elementsStringDefault.size() > 0) {
            res |= handleAutoRegister(elementsStringDefault, (VariableElement variableElement) -> {
                AutoRegisterStringWithDefault annotation = variableElement.getAnnotation(AutoRegisterStringWithDefault.class);
                if (annotation == null) {
                    printMessageW("get AutoRegisterStringWithDefault is null");
                    return null;
                }
                return parseAnnotationWithDefault(Type.STRING, "");
            });
        }


        return res;
    }

    private ConfigItemInfo parseAnnotation(Type type, Object defaultValue, String accessor, String loader) {
        ConfigItemInfo configItemInfo = new ConfigItemInfo();

        configItemInfo.type = type;
        handleRegisterParam(configItemInfo, accessor, loader);

        if (defaultValue != null) {
            configItemInfo.defaultValue = defaultValue;
        }

        return configItemInfo;
    }

    private ConfigItemInfo parseAnnotationWithDefault(Type type, Object defaultValue) {
        ConfigItemInfo configItemInfo = new ConfigItemInfo();

        configItemInfo.type = type;
        configItemInfo.defaultLoader = DEFAULT_LOADER;
        configItemInfo.accessor = DEFAULT_ACCESSOR;
        configItemInfo.defaultValue = defaultValue;

        return configItemInfo;
    }

    private void handleRegisterParam(ConfigItemInfo configItemInfo, String accessor, String loader) {
        configItemInfo.accessor = (accessor == null ? "" : accessor);
        configItemInfo.defaultLoader = (loader == null ? "" : loader);
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

            String key = variableElement.getConstantValue().toString();

            if (key == null || key.isEmpty()) {
                printMessageE("handleAutoConfigRegister : the element's value is null --> " + variableElement.getSimpleName());
                continue;
            }

            configItemInfo.key = key;

            items.add(configItemInfo);

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
            switch (configItemInfo.type) {
                case STRING:
                    registerKeyMethod.addStatement("config.registerString($S, $S, $S, $S)"
                            , configItemInfo.key, configItemInfo.accessor, configItemInfo.defaultLoader
                            , configItemInfo.defaultValue);
                    break;
                case INT:
                    registerKeyMethod.addStatement("config.registerInt($S, $S, $S, $L)"
                            , configItemInfo.key, configItemInfo.accessor, configItemInfo.defaultLoader
                            , configItemInfo.defaultValue);
                    break;
                case LONG:
                    registerKeyMethod.addStatement("config.registerLong($S, $S, $S, $Ll)"
                            , configItemInfo.key, configItemInfo.accessor, configItemInfo.defaultLoader
                            , configItemInfo.defaultValue);
                    break;
                case BOOLEAN:
                    registerKeyMethod.addStatement("config.registerBoolean($S, $S, $S, $L)"
                            , configItemInfo.key, configItemInfo.accessor, configItemInfo.defaultLoader
                            , configItemInfo.defaultValue);
                    break;
                case FLOAT:
                    registerKeyMethod.addStatement("config.registerFloat($S, $S, $S, $Lf)"
                            , configItemInfo.key, configItemInfo.accessor, configItemInfo.defaultLoader
                            , configItemInfo.defaultValue);
                    break;
                case DOUBLE:
                    registerKeyMethod.addStatement("config.registerDouble($S, $S, $S, $L)"
                            , configItemInfo.key, configItemInfo.accessor, configItemInfo.defaultLoader
                            , configItemInfo.defaultValue);
                    break;
                default:
                    break;
            }

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

    private void printMessageW(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg);
    }

    private void printMessageE(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);

    }

}
