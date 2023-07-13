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
import indi.arrowyi.autoconfig.AutoRegister;
import indi.arrowyi.autoconfig.configmanager.AutoConfig;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

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
        , "indi.arrowyi.autoconfig.AutoRegisterDefaultLoader"
        , "indi.arrowyi.autoconfig.AutoRegister"})
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
        pickupDefaultAccessorOrLoaderConfigItems(FieldItems);
        List<ConfigClass> accessorItems = classProcessor.processAccessorAnnotation(roundEnvironment, processingEnv);
        List<ConfigClass> loaderItems = classProcessor.processLoaderAnnotation(roundEnvironment, processingEnv);
        Set<String> containsSet = classProcessor.processRegisterAnnotation(roundEnvironment, processingEnv);

        if (FieldItems.size() < 1 && accessorItems.size() < 1 && loaderItems.size() < 1) {
            printMessageW("no items found for this round ");
        } else {
            printMessageW("begin to generate files, module name is " + fieldProcessor.moduleName);

            String accessorName = generateDefaultAccessor(defaultAccessors, fieldProcessor.moduleName);
            String loaderName = generateDefaultLoader(defaultLoaders, fieldProcessor.moduleName);
            generateRegisterFile(FieldItems, accessorItems, loaderItems, fieldProcessor.moduleName, loaderName, accessorName, containsSet);
        }

        printMessageW("end of process indi.arrowyi.configcomplier.CommonSettingsProcessor");
        return true;
    }

    private List<ConfigItemInfo> defaultAccessors = new ArrayList<>();
    private List<ConfigItemInfo> defaultLoaders = new ArrayList<>();

    private boolean pickupDefaultAccessorOrLoaderConfigItems(List<ConfigItemInfo> itemInfos) {
        for (ConfigItemInfo info : itemInfos) {
            if (info.accessor == null || info.accessor.isEmpty()) {
                defaultAccessors.add(info);
            }

            if (info.defaultLoader == null || info.defaultLoader.isEmpty()) {
                defaultLoaders.add(info);
            }
        }

        return true;
    }

    private String generateDefaultAccessor(List<ConfigItemInfo> items, String moduleName) {
        ClassName iConfigAccessor = ClassName.get("indi.arrowyi.autoconfig.configmanager"
                , "ConfigAccessor");

        String className = (moduleName != null ? moduleName : items.get(0).key) + "520DefaultAccessor";

        ClassName defaultAccessor = ClassName.get("indi.arrowyi.autoconfig.configmanager"
                , className);

        ClassName object = ClassName.get("java.lang", "Object");
        ClassName string = ClassName.get("java.lang", "String");
        ClassName type = ClassName.get(AutoConfig.Type.class);

        ClassName hashMap = ClassName.get(HashMap.class);
        ClassName map = ClassName.get(Map.class);

        TypeSpec.Builder defaultAccessorBuilder = TypeSpec.classBuilder(defaultAccessor)
                .addSuperinterface(iConfigAccessor).addModifiers(Modifier.PUBLIC);

        MethodSpec.Builder setMethod = MethodSpec.methodBuilder("set")
                .addAnnotation(Override.class).addModifiers(Modifier.PUBLIC).returns(TypeName.BOOLEAN)
                .addParameter(ParameterSpec.builder(string, "key").build())
                .addParameter(ParameterSpec.builder(type, "type").build())
                .addParameter(ParameterSpec.builder(object, "value").build())
                .beginControlFlow("if (type.isTypeOf(value))")
                .addStatement("values.putIfAbsent(key, value)").addStatement("return true")
                .nextControlFlow("else").addStatement("return false").endControlFlow();


        MethodSpec.Builder getMethod = MethodSpec.methodBuilder("get")
                .addAnnotation(Override.class).addModifiers(Modifier.PUBLIC).returns(object)
                .addParameter(ParameterSpec.builder(string, "key").build())
                .addParameter(ParameterSpec.builder(type, "type").build())
                .addParameter(ParameterSpec.builder(object, "defaultValue").build())
                .addStatement("return values.get(key) == null ? defaultValue : values.get(key)");


        FieldSpec.Builder mapField = FieldSpec.builder(ParameterizedTypeName.get(map, string, object), "values"
                        , Modifier.PRIVATE)
                .initializer("new $T()", hashMap);


        defaultAccessorBuilder.addMethod(setMethod.build()).addField(mapField.build()).addMethod(getMethod.build());

        JavaFile javaFile = JavaFile.builder("indi.arrowyi.autoconfig.configmanager"
                , defaultAccessorBuilder.build()).build();

        Filer filer = processingEnv.getFiler();

        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (ConfigItemInfo info : items) {
            info.accessor = className;
        }

        return className;

    }

    private String generateDefaultLoader(List<ConfigItemInfo> items, String moduleName) {

        ClassName iDefaultValueLoader = ClassName.get("indi.arrowyi.autoconfig.configmanager"
                , "DefaultValueLoader");

        String className = (moduleName != null ? moduleName : items.get(0).key) + "1314DefaultDefaultLoader";

        ClassName defaultLoader = ClassName.get("indi.arrowyi.autoconfig.configmanager"
                , className);

        ClassName object = ClassName.get("java.lang", "Object");
        ClassName string = ClassName.get("java.lang", "String");
        ClassName type = ClassName.get(AutoConfig.Type.class);


        TypeSpec.Builder defaultLoaderBuilder = TypeSpec.classBuilder(defaultLoader)
                .addSuperinterface(iDefaultValueLoader).addModifiers(Modifier.PUBLIC);

        MethodSpec.Builder getDefaultValueMethod = MethodSpec.methodBuilder("getDefaultValue")
                .addAnnotation(Override.class).addModifiers(Modifier.PUBLIC).returns(object)
                .addParameter(ParameterSpec.builder(string, "key").build())
                .addParameter(ParameterSpec.builder(type, "type").build());

        getDefaultValueMethod.beginControlFlow("switch(key)");

        for (ConfigItemInfo info : items) {
            switch (info.type) {
                case STRING:
                    getDefaultValueMethod.addStatement("case $S : return $S", info.key, info.defaultValue);
                    break;
                case INT:
                case BOOLEAN:
                case DOUBLE:
                    getDefaultValueMethod.addStatement("case $S : return $L", info.key, info.defaultValue);
                    break;
                case LONG:
                    getDefaultValueMethod.addStatement("case $S : return $Ll", info.key, info.defaultValue);
                    break;
                case FLOAT:
                    getDefaultValueMethod.addStatement("case $S : return $Lf", info.key, info.defaultValue);
                    break;
            }

            info.defaultLoader = className;
        }

        getDefaultValueMethod.endControlFlow();
        getDefaultValueMethod.addStatement("return null");

        defaultLoaderBuilder.addMethod(getDefaultValueMethod.build());

        JavaFile javaFile = JavaFile.builder("indi.arrowyi.autoconfig.configmanager"
                , defaultLoaderBuilder.build()).build();

        Filer filer = processingEnv.getFiler();

        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return className;

    }

    private boolean generateRegisterFile(List<ConfigItemInfo> items, List<ConfigClass> accessors, List<ConfigClass> loaders
            , String moduleName, String defaultLoaderName, String defaultAccessorName, Set<String> contains) {
        ClassName ConfigRegister = ClassName.get("indi.arrowyi.autoconfig.configmanager"
                , "ConfigRegister");

        String className = (moduleName != null ? moduleName : items.get(0).key) + "Register";

        TypeSpec.Builder registerClassBuilder = TypeSpec.classBuilder(className)
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
                    registerKeyMethod.addStatement("config.registerString($S, $S, $S)"
                            , configItemInfo.key, configItemInfo.accessor, configItemInfo.defaultLoader);
                    break;
                case INT:
                    registerKeyMethod.addStatement("config.registerInt($S, $S, $S)"
                            , configItemInfo.key, configItemInfo.accessor, configItemInfo.defaultLoader);
                    break;
                case LONG:
                    registerKeyMethod.addStatement("config.registerLong($S, $S, $S)"
                            , configItemInfo.key, configItemInfo.accessor, configItemInfo.defaultLoader);
                    break;
                case BOOLEAN:
                    registerKeyMethod.addStatement("config.registerBoolean($S, $S, $S)"
                            , configItemInfo.key, configItemInfo.accessor, configItemInfo.defaultLoader);
                    break;
                case FLOAT:
                    registerKeyMethod.addStatement("config.registerFloat($S, $S, $S)"
                            , configItemInfo.key, configItemInfo.accessor, configItemInfo.defaultLoader);
                    break;
                case DOUBLE:
                    registerKeyMethod.addStatement("config.registerDouble($S, $S, $S)"
                            , configItemInfo.key, configItemInfo.accessor, configItemInfo.defaultLoader);
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

        if (defaultAccessorName != null && !defaultAccessorName.isEmpty()) {
            ClassName defaultAccessor = ClassName.get("indi.arrowyi.autoconfig.configmanager"
                    , defaultAccessorName);
            registerKeyMethod.addStatement("config.registerAccessor($S, new $T())",
                    defaultAccessorName, defaultAccessor);
        }


        for (ConfigClass configClass : loaders) {
            ClassName loaderClass = ClassName.get(configClass.classType);
            registerKeyMethod.addStatement("config.registerDefaultValueLoader($S, new $T())",
                    configClass.name, loaderClass);
        }

        if (defaultLoaderName != null && !defaultLoaderName.isEmpty()) {
            ClassName defaultLoader = ClassName.get("indi.arrowyi.autoconfig.configmanager"
                    , defaultLoaderName);
            registerKeyMethod.addStatement("config.registerDefaultValueLoader($S, new $T())",
                    defaultLoaderName, defaultLoader);
        }


        registerClassBuilder.addMethod(registerKeyMethod.build());

        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        codeBuilder.add("{ \"$L\" ", className);
        for (String contain : contains) {
            codeBuilder.add(" , ");
            codeBuilder.add("\" $L \" ", contain);
        }
        codeBuilder.add(" }");

        AnnotationSpec.Builder annotation = AnnotationSpec.builder(AutoRegister.class)
                .addMember("contains", codeBuilder.build());

        registerClassBuilder.addAnnotation(annotation.build());

        JavaFile javaFile = JavaFile.builder("indi.arrowyi.autoconfig.configmanager"
                , registerClassBuilder.build()).build();

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
