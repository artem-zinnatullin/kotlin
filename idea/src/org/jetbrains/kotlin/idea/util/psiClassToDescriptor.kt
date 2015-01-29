/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.util.psiClassToDescriptor

import org.jetbrains.kotlin.idea.caches.resolve.ResolutionFacade
import com.intellij.psi.PsiClass
import org.jetbrains.kotlin.psi.JetClassOrObject
import org.jetbrains.kotlin.asJava.KotlinLightClass
import org.jetbrains.kotlin.idea.caches.resolve.KotlinLightClassForDecompiledDeclaration
import org.jetbrains.kotlin.idea.caches.resolve.JavaResolveExtension
import org.jetbrains.kotlin.load.java.structure.impl.JavaClassImpl
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor

public fun ResolutionFacade.psiClassToDescriptor(
        psiClass: PsiClass,
        declarationTranslator: (JetClassOrObject) -> JetClassOrObject? = { it }
): ClassifierDescriptor? {
    return if (psiClass is KotlinLightClass && psiClass !is KotlinLightClassForDecompiledDeclaration) {
        val origin = psiClass.origin ?: return null
        val declaration = declarationTranslator(origin) ?: return null
        resolveToDescriptor(declaration)
    } else {
        get(JavaResolveExtension)(psiClass).first.resolveClass(JavaClassImpl(psiClass))
    }  as? ClassifierDescriptor
}