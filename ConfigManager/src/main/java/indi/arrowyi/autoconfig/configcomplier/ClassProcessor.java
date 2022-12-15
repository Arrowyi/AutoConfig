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

import indi.arrowyi.autoconfig.AutoRegisterAccessor;
import indi.arrowyi.autoconfig.AutoRegisterDefaultLoader;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ClassProcessor {

    private final AutoConfigProcessor utils;

    public ClassProcessor(AutoConfigProcessor utils) {
        this.utils = utils;
    }

    List<AutoConfigProcessor.ConfigClass> processAccessorAnnotation(RoundEnvironment roundEnvironment
            , ProcessingEnvironment processingEnv) {
        Set<? extends Element> elementsAccessor = roundEnvironment.getElementsAnnotatedWith(AutoRegisterAccessor.class);
        utils.printMessageW("elementsAccessor size is " + elementsAccessor.size());

        TypeMirror configAccessor = processingEnv.getElementUtils()
                .getTypeElement("indi.arrowyi.autoconfig.configmanager.ConfigAccessor").asType();

        return parseRegisterAccessorAndLoader(elementsAccessor, processingEnv, configAccessor, typeElement -> {
            AutoRegisterAccessor autoRegisterAccessor = typeElement.getAnnotation(AutoRegisterAccessor.class);
            return autoRegisterAccessor.value();
        });
    }

    List<AutoConfigProcessor.ConfigClass> processLoaderAnnotation(RoundEnvironment roundEnvironment
            , ProcessingEnvironment processingEnv) {

        Set<? extends Element> elementsLoader = roundEnvironment.getElementsAnnotatedWith(AutoRegisterDefaultLoader.class);
        utils.printMessageW("elementsLoader size is " + elementsLoader.size());

        TypeMirror configLoader = processingEnv.getElementUtils()
                .getTypeElement("indi.arrowyi.autoconfig.configmanager.DefaultValueLoader").asType();

        return parseRegisterAccessorAndLoader(elementsLoader, processingEnv, configLoader, typeElement -> {
            AutoRegisterDefaultLoader autoRegisterAccessor = typeElement.getAnnotation(AutoRegisterDefaultLoader.class);
            return autoRegisterAccessor.value();
        });
    }


    private List<AutoConfigProcessor.ConfigClass> parseRegisterAccessorAndLoader(Set<? extends Element> elements
            , ProcessingEnvironment processingEnv, TypeMirror interfaceType, Function<TypeElement, String> function) {
        List<AutoConfigProcessor.ConfigClass> accessors = new ArrayList<>();
        for (Element element : elements) {
            if (!checkElement(element, processingEnv, interfaceType)) {
                continue;
            }

            TypeElement typeElement = (TypeElement) element;
            String name = function.apply(typeElement);

            accessors.add(new AutoConfigProcessor.ConfigClass(name, typeElement));
        }

        return accessors;
    }

    private boolean checkElement(Element element, ProcessingEnvironment processingEnv, TypeMirror interfaceTypeMirror) {
        //check is class
        if (element.getKind() != ElementKind.CLASS) {
            utils.printMessageE("handleRegisterAccessorAndLoader : the element kind is not ok, it is " + element.getSimpleName());
            return false;
        }

        TypeElement typeElement = (TypeElement) element;
        TypeMirror configInterface = interfaceTypeMirror;
        Types typeUtils = processingEnv.getTypeUtils();

        //check is not abstract and is public
        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(Modifier.ABSTRACT) || !modifiers.contains(Modifier.PUBLIC)) {
            utils.printMessageE("handleRegisterAccessorAndLoader : the element modifier is not ok with abstract or without public "
                    + typeElement.getSimpleName());
            return false;
        }

        //check is implement the interface
        List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
        if (!interfaces.contains(configInterface)) {
            TypeMirror superClass = typeElement.getSuperclass();
            while (superClass.getKind() != TypeKind.NONE
                    && (!typeUtils.isSameType(superClass, configInterface))) {
                TypeElement superType = (TypeElement) typeUtils.asElement(superClass);
                List<? extends TypeMirror> superInterfaces = superType.getInterfaces();
                if (superInterfaces.contains(configInterface)) {
                    break;
                }

                superClass = superType.getSuperclass();
            }

            if (superClass.getKind() == TypeKind.NONE) {
                utils.printMessageE("handleRegisterAccessorAndLoader : " +
                        "the class not implement the ConfigAccessor interface "
                        + typeElement.getSimpleName());
                return false;
            }
        }

        //check has the public constructor with no param
        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructorElement = (ExecutableElement) enclosed;
                if (constructorElement.getParameters().size() == 0 &&
                        constructorElement.getModifiers().contains(Modifier.PUBLIC)) {
                    return true;
                }
            }
        }

        return false;
    }
}
