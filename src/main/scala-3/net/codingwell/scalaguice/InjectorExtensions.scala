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
package net.codingwell.scalaguice

import com.google.inject.{Binding, Injector, Key, Provider}
import java.lang.annotation.Annotation
import net.codingwell.scalaguice.KeyExtensions._
import scala.reflect.ClassTag

object InjectorExtensions {

  implicit class ScalaInjector(val self: Injector) /*extends AnyVal*/ {
    inline def instance[T]: T = self.getInstance(typeLiteral[T].toKey)
    inline def instance[T](ann: Annotation): T = self.getInstance(typeLiteral[T].annotatedWith(ann))
    inline def instance[T, Ann <: Annotation : ClassTag]: T = self.getInstance(typeLiteral[T].annotatedWith[Ann])

    inline def existingBinding[T]: Option[Binding[T]] = existingBinding(typeLiteral[T].toKey)
    inline def existingBinding[T](ann: Annotation): Option[Binding[T]] = existingBinding(typeLiteral[T].annotatedWith(ann))
    inline def existingBinding[T, Ann <: Annotation : ClassTag]: Option[Binding[T]] = existingBinding(typeLiteral[T].annotatedWith[Ann])
    def existingBinding[T](key: Key[T]): Option[Binding[T]] = Option(self.getExistingBinding(key))

    inline def provider[T]: Provider[T] = self.getProvider(typeLiteral[T].toKey)
    inline def provider[T](ann: Annotation): Provider[T] = self.getProvider(typeLiteral[T].annotatedWith(ann))
    inline def provider[T, Ann <: Annotation : ClassTag]: Provider[T] = self.getProvider(typeLiteral[T].annotatedWith[Ann])
  }
}
