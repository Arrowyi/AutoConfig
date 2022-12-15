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
import indi.arrowyi.autoconfig.configmanager.AutoConfig;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.List;
import java.util.Set;

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
        , "indi.arrowyi.autoconfig.AutoRegisterBooleanWithDefault"
        , "indi.arrowyi.autoconfig.AutoRegisterAccessor"
        , "indi.arrowyi.autoconfig.AutoRegisterDefaultLoader"})
public class AutoConfigProcessor extends AbstractProcessor {

    enum Type {
        BOOLEAN,
        INT,
        FLOAT,
        DOUBLE,
        STRING,
        LONG
    }

    static class ConfigItemInfo {
        String key;
        Type type;
        Object defaultValue;
        String accessor;
        String defaultLoader;
    }

    static class ConfigClass {
        String name;
        TypeElement classType;

        public ConfigClass(String name, TypeElement className) {
            this.name = name;
            this.classType = className;
        }
    }

    private final FieldProcessor fieldProcessor = new FieldProcessor(this);
    private final ClassProcessor classProcessor = new ClassProcessor(this);

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        printMessageW("begin to process indi.arrowyi.configcomplier.AutoConfigProcessor");

        List<ConfigItemInfo> FieldItems = fieldProcessor.processFieldAnnotation(roundEnvironment, processingEnv);
        List<ConfigClass> accessorItems = classProcessor.processAccessorAnnotation(roundEnvironment, processingEnv);
        List<ConfigClass> loaderItems = classProcessor.processLoaderAnnotation(roundEnvironment, processingEnv);

        if (FieldItems.size() < 1 && accessorItems.size() < 1 && loaderItems.size() < 1) {
            printMessageW("no items found for this round ");
        } else {
            generateFile(FieldItems, accessorItems, loaderItems);
        }

        printMessageW("end of process indi.arrowyi.configcomplier.CommonSettingsProcessor");
        return true;
    }

    private boolean generateFile(List<ConfigItemInfo> items, List<ConfigClass> accessors, List<ConfigClass> loaders) {
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

        for (ConfigClass configClass : accessors) {
            ClassName accessorClass = ClassName.get(configClass.classType);
            registerKeyMethod.addStatement("config.registerAccessor($S, new $T())",
                    configClass.name, accessorClass);
        }

        for (ConfigClass configClass : loaders) {
            ClassName loaderClass = ClassName.get(configClass.classType);
            registerKeyMethod.addStatement("config.registerDefaultValueLoader($S, new $T())",
                    configClass.name, loaderClass);
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

    void printMessageW(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg);
    }

    void printMessageE(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
    }

}
