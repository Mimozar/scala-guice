/*
 *  Copyright 2010-2014 Benjamin Lings
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.codingwell

import com.google.inject.internal.Annotations
import com.google.inject.util.Types.newParameterizedType
import com.google.inject.{Key, TypeLiteral}
import java.lang.annotation.Annotation
import scala.reflect.{ClassTag, classTag}


package object scalaguice {

  /**
   * Create a [[https://google.github.io/guice/api-docs/5.0.1/javadoc/com/google/inject/TypeLiteral.html com.google.inject.TypeLiteral]] from a [[scala.reflect.runtime.universe.TypeTag]].
   * Subtypes of [[scala.AnyVal]] will be converted to their corresponding
   * Java wrapper classes.
   *
   * Note that any methods that call this and accept [[T]] as a parameter must also be defined as [[inline]]
   * to preserve [[T]]'s complete structure (e.g., if [[T]] could represent a parameterized type)
   */
  inline def typeLiteral[T]: TypeLiteral[T] = {
    val javaType = TypeConversions.scalaTypeToJavaType[T]
    TypeLiteral.get(javaType).asInstanceOf[TypeLiteral[T]]
  }

  def cls[T: ClassTag]: Class[T] = classTag[T].runtimeClass.asInstanceOf[Class[T]]

  /**
   * Returns the name the set should use.  This is based on the annotation.
   * If the annotation has an instance and is not a marker annotation,
   * we ask the annotation for its toString.  If it was a marker annotation
   * or just an annotation type, we use the annotation's name. Otherwise,
   * the name is the empty string.
   */
  private[scalaguice] def nameOf[T](key: Key[T]): String = {
    val annotation: Annotation = key.getAnnotation
    val annotationType: Class[_ <: Annotation] = key.getAnnotationType
    if (annotation != null && !Annotations.isMarker(annotationType)) {
      key.getAnnotation.toString
    } else if (key.getAnnotationType != null) {
      "@" + key.getAnnotationType.getName
    } else {
      ""
    }
  }

  private[scalaguice] class WrapHelper[WType[_]] {
    def around[T](typ: TypeLiteral[T])(implicit classTag: ClassTag[WType[T]]): TypeLiteral[WType[T]] = {
      val wType = newParameterizedType(classTag.runtimeClass, typ.getType)
      TypeLiteral.get(wType).asInstanceOf[TypeLiteral[WType[T]]]
    }
  }

  private[scalaguice] def wrap[WType[_]] = new WrapHelper[WType]

  private[scalaguice] class WrapHelper2[WType[_, _]] {
    def around[K, V](kTyp: TypeLiteral[K], vTyp: TypeLiteral[V])(implicit classTag: ClassTag[WType[K, V]]): TypeLiteral[WType[K, V]] = {
      val wType = newParameterizedType(
        classTag.runtimeClass,
        kTyp.getType,
        vTyp.getType
      )

      TypeLiteral.get(wType).asInstanceOf[TypeLiteral[WType[K, V]]]
    }
  }

  private[scalaguice] def wrap2[WType[_, _]] = new WrapHelper2[WType]
}
