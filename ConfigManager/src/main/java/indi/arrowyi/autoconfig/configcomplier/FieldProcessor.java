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

import indi.arrowyi.autoconfig.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static indi.arrowyi.autoconfig.configmanager.AutoConfig.DEFAULT_ACCESSOR;
import static indi.arrowyi.autoconfig.configmanager.AutoConfig.DEFAULT_LOADER;

class FieldProcessor {
    private final AutoConfigProcessor utils;

    FieldProcessor(AutoConfigProcessor utils) {
        this.utils = utils;
    }

    List<AutoConfigProcessor.ConfigItemInfo> processFieldAnnotation(RoundEnvironment roundEnvironment
            , ProcessingEnvironment processingEnv) {
        List<AutoConfigProcessor.ConfigItemInfo> items = new ArrayList<>();

        Set<? extends Element> elementsInt = roundEnvironment.getElementsAnnotatedWith(AutoRegisterInt.class);
        utils.printMessageW("elementsInt size is " + elementsInt.size());

        Set<? extends Element> elementsLong = roundEnvironment.getElementsAnnotatedWith(AutoRegisterLong.class);
        utils.printMessageW("elementsLong size is " + elementsLong.size());

        Set<? extends Element> elementsBoolean = roundEnvironment.getElementsAnnotatedWith(AutoRegisterBoolean.class);
        utils.printMessageW("elementsBoolean size is " + elementsBoolean.size());

        Set<? extends Element> elementsFloat = roundEnvironment.getElementsAnnotatedWith(AutoRegisterFloat.class);
        utils.printMessageW("elementsFloat size is " + elementsFloat.size());

        Set<? extends Element> elementsDouble = roundEnvironment.getElementsAnnotatedWith(AutoRegisterDouble.class);
        utils.printMessageW("elementsDouble size is " + elementsDouble.size());

        Set<? extends Element> elementsString = roundEnvironment.getElementsAnnotatedWith(AutoRegisterString.class);
        utils.printMessageW("elementsString size is " + elementsString.size());


        Set<? extends Element> elementsIntDefault = roundEnvironment.getElementsAnnotatedWith(AutoRegisterIntWithDefault.class);
        utils.printMessageW("elementsIntDefault size is " + elementsIntDefault.size());

        Set<? extends Element> elementsLongDefault = roundEnvironment.getElementsAnnotatedWith(AutoRegisterLongWithDefault.class);
        utils.printMessageW("elementsLongDefault size is " + elementsLongDefault.size());

        Set<? extends Element> elementsFloatDefault = roundEnvironment.getElementsAnnotatedWith(AutoRegisterFloatWithDefault.class);
        utils.printMessageW("elementsFloatDefault size is " + elementsFloatDefault.size());

        Set<? extends Element> elementsDoubleDefault = roundEnvironment.getElementsAnnotatedWith(AutoRegisterDoubleWithDefault.class);
        utils.printMessageW("elementsDoubleDefault size is " + elementsDoubleDefault.size());

        Set<? extends Element> elementsBooleanDefault = roundEnvironment.getElementsAnnotatedWith(AutoRegisterBooleanWithDefault.class);
        utils.printMessageW("elementsBooleanDefault size is " + elementsBooleanDefault.size());

        Set<? extends Element> elementsStringDefault = roundEnvironment.getElementsAnnotatedWith(AutoRegisterStringWithDefault.class);
        utils.printMessageW("elementsStringDefault size is " + elementsStringDefault.size());

        Set<? extends Element> elementsAccessor = roundEnvironment.getElementsAnnotatedWith(AutoRegisterAccessor.class);
        utils.printMessageW("elementsStringDefault size is " + elementsStringDefault.size());

        if (elementsInt.size() > 0) {
            handleAutoRegister(elementsInt, items, processingEnv, (VariableElement variableElement) -> {
                AutoRegisterInt annotation = variableElement.getAnnotation(AutoRegisterInt.class);
                if (annotation == null) {
                    utils.printMessageW("get AutoRegisterInt is null");
                    return null;
                }
                return parseAnnotation(AutoConfigProcessor.Type.INT, annotation.defaultValue(), annotation.accessor(), annotation.defaultLoader());
            });
        }

        if (elementsLong.size() > 0) {
            handleAutoRegister(elementsLong, items, processingEnv, (VariableElement variableElement) -> {
                AutoRegisterLong annotation = variableElement.getAnnotation(AutoRegisterLong.class);
                if (annotation == null) {
                    utils.printMessageW("get AutoRegisterLong is null");
                    return null;
                }
                return parseAnnotation(AutoConfigProcessor.Type.LONG, annotation.defaultValue(), annotation.accessor(), annotation.defaultLoader());
            });
        }

        if (elementsBoolean.size() > 0) {
            handleAutoRegister(elementsBoolean, items, processingEnv, (VariableElement variableElement) -> {
                AutoRegisterBoolean annotation = variableElement.getAnnotation(AutoRegisterBoolean.class);
                if (annotation == null) {
                    utils.printMessageW("get AutoRegisterBoolean is null");
                    return null;
                }
                return parseAnnotation(AutoConfigProcessor.Type.BOOLEAN, annotation.defaultValue(), annotation.accessor(), annotation.defaultLoader());
            });
        }

        if (elementsFloat.size() > 0) {
            handleAutoRegister(elementsFloat, items, processingEnv, (VariableElement variableElement) -> {
                AutoRegisterFloat annotation = variableElement.getAnnotation(AutoRegisterFloat.class);
                if (annotation == null) {
                    utils.printMessageW("get AutoRegisterFloat is null");
                    return null;
                }
                return parseAnnotation(AutoConfigProcessor.Type.FLOAT, annotation.defaultValue(), annotation.accessor(), annotation.defaultLoader());
            });
        }

        if (elementsDouble.size() > 0) {
            handleAutoRegister(elementsDouble, items, processingEnv, (VariableElement variableElement) -> {
                AutoRegisterDouble annotation = variableElement.getAnnotation(AutoRegisterDouble.class);
                if (annotation == null) {
                    utils.printMessageW("get AutoRegisterDouble is null");
                    return null;
                }
                return parseAnnotation(AutoConfigProcessor.Type.DOUBLE, annotation.defaultValue(), annotation.accessor(), annotation.defaultLoader());
            });
        }

        if (elementsString.size() > 0) {
            handleAutoRegister(elementsString, items, processingEnv, (VariableElement variableElement) -> {
                AutoRegisterString annotation = variableElement.getAnnotation(AutoRegisterString.class);
                if (annotation == null) {
                    utils.printMessageW("get AutoRegisterString is null");
                    return null;
                }
                return parseAnnotation(AutoConfigProcessor.Type.STRING, annotation.defaultValue(), annotation.accessor(), annotation.defaultLoader());
            });
        }


        if (elementsIntDefault.size() > 0) {
            handleAutoRegister(elementsIntDefault, items, processingEnv, (VariableElement variableElement) -> {
                AutoRegisterIntWithDefault annotation = variableElement.getAnnotation(AutoRegisterIntWithDefault.class);
                if (annotation == null) {
                    utils.printMessageW("get AutoRegisterString is null");
                    return null;
                }
                return parseAnnotationWithDefault(AutoConfigProcessor.Type.INT, 0);
            });
        }

        if (elementsLongDefault.size() > 0) {
            handleAutoRegister(elementsLongDefault, items, processingEnv, (VariableElement variableElement) -> {
                AutoRegisterLongWithDefault annotation = variableElement.getAnnotation(AutoRegisterLongWithDefault.class);
                if (annotation == null) {
                    utils.printMessageW("get elementsLongDefault is null");
                    return null;
                }
                return parseAnnotationWithDefault(AutoConfigProcessor.Type.LONG, 0L);
            });
        }

        if (elementsFloatDefault.size() > 0) {
            handleAutoRegister(elementsFloatDefault, items, processingEnv, (VariableElement variableElement) -> {
                AutoRegisterFloatWithDefault annotation = variableElement.getAnnotation(AutoRegisterFloatWithDefault.class);
                if (annotation == null) {
                    utils.printMessageW("get AutoRegisterFloatWithDefault is null");
                    return null;
                }
                return parseAnnotationWithDefault(AutoConfigProcessor.Type.FLOAT, 0.0f);
            });
        }

        if (elementsDoubleDefault.size() > 0) {
            handleAutoRegister(elementsDoubleDefault, items, processingEnv, (VariableElement variableElement) -> {
                AutoRegisterDoubleWithDefault annotation = variableElement.getAnnotation(AutoRegisterDoubleWithDefault.class);
                if (annotation == null) {
                    utils.printMessageW("get AutoRegisterDoubleWithDefault is null");
                    return null;
                }
                return parseAnnotationWithDefault(AutoConfigProcessor.Type.DOUBLE, 0.0);
            });
        }

        if (elementsBooleanDefault.size() > 0) {
            handleAutoRegister(elementsBooleanDefault, items, processingEnv, (VariableElement variableElement) -> {
                AutoRegisterBooleanWithDefault annotation = variableElement.getAnnotation(AutoRegisterBooleanWithDefault.class);
                if (annotation == null) {
                    utils.printMessageW("get AutoRegisterBooleanWithDefault is null");
                    return null;
                }
                return parseAnnotationWithDefault(AutoConfigProcessor.Type.BOOLEAN, false);
            });
        }

        if (elementsStringDefault.size() > 0) {
            handleAutoRegister(elementsStringDefault, items, processingEnv, (VariableElement variableElement) -> {
                AutoRegisterStringWithDefault annotation = variableElement.getAnnotation(AutoRegisterStringWithDefault.class);
                if (annotation == null) {
                    utils.printMessageW("get AutoRegisterStringWithDefault is null");
                    return null;
                }
                return parseAnnotationWithDefault(AutoConfigProcessor.Type.STRING, "");
            });
        }

        utils.printMessageW("items size is " + items.size());

        return items;
    }


    private AutoConfigProcessor.ConfigItemInfo parseAnnotation(AutoConfigProcessor.Type type, Object defaultValue, String accessor, String loader) {
        AutoConfigProcessor.ConfigItemInfo configItemInfo = new AutoConfigProcessor.ConfigItemInfo();

        configItemInfo.type = type;
        handleRegisterParam(configItemInfo, accessor, loader);

        if (defaultValue != null) {
            configItemInfo.defaultValue = defaultValue;
        }

        return configItemInfo;
    }

    private AutoConfigProcessor.ConfigItemInfo parseAnnotationWithDefault(AutoConfigProcessor.Type type, Object defaultValue) {
        AutoConfigProcessor.ConfigItemInfo configItemInfo = new AutoConfigProcessor.ConfigItemInfo();

        configItemInfo.type = type;
        configItemInfo.defaultLoader = DEFAULT_LOADER;
        configItemInfo.accessor = DEFAULT_ACCESSOR;
        configItemInfo.defaultValue = defaultValue;

        return configItemInfo;
    }

    private void handleRegisterParam(AutoConfigProcessor.ConfigItemInfo configItemInfo, String accessor, String loader) {
        configItemInfo.accessor = (accessor == null ? "" : accessor);
        configItemInfo.defaultLoader = (loader == null ? "" : loader);
    }

    private void handleAutoRegister(Set<? extends Element> elements, List<AutoConfigProcessor.ConfigItemInfo> items
            , ProcessingEnvironment processingEnv, Function<VariableElement, AutoConfigProcessor.ConfigItemInfo> function) {
        for (Element element : elements) {
            if (element.getKind() != ElementKind.FIELD) {
                utils.printMessageE("handleAutoConfigRegister : the element kind is not ok, it is " + element.getSimpleName());
                continue;
            }

            VariableElement variableElement = (VariableElement) element;
            TypeMirror variable = variableElement.asType();
            TypeMirror string = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
            Types typeUtils = processingEnv.getTypeUtils();

            Set<Modifier> modifiers = element.getModifiers();
            if (!(modifiers.contains(Modifier.STATIC) && modifiers.contains(Modifier.FINAL))) {
                utils.printMessageE("handleAutoConfigRegister : the element modifier is not ok : " + variableElement.getSimpleName());
                continue;
            }


            if (!typeUtils.isSameType(variable, string)) {
                utils.printMessageE("handleAutoConfigRegister : the element's type is not ok , it is " + variableElement.getSimpleName());
                continue;
            }

            AutoConfigProcessor.ConfigItemInfo configItemInfo = function.apply(variableElement);

            String key = variableElement.getConstantValue().toString();

            if (key == null || key.isEmpty()) {
                utils.printMessageE("handleAutoConfigRegister : the element's value is null --> " + variableElement.getSimpleName());
                continue;
            }

            configItemInfo.key = key;

            items.add(configItemInfo);

        }

    }

}
