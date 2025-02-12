/*
 *  Copyright 2010-2014 Benjamin Lings
 *  Author: Thomas Suckow
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

import com.google.common.collect.ImmutableSet
import com.google.inject._
import com.google.inject.spi._
import java.util.{Set => JSet}
import scala.collection.{immutable => im}
import scala.jdk.CollectionConverters._

/**
 * Provider for a Scala Immutable Set from a Java Set.
 *
 * Example:
 * {{{
 * .toProvider( new SetProvider[T]( Key.get( typeLiteral[JSet[T]] ) ) )
 * }}}
 */
class SetProvider[T] (val source:Key[JSet[T]]) extends ProviderWithDependencies[im.Set[T]] {

  @Inject() var injector:Injector = _

  def get():im.Set[T] = {
    injector.getInstance( source ).asScala.toSet[T]
  }

  def getDependencies: JSet[Dependency[_]] = {
    ImmutableSet.of( Dependency.get( source ) )
  }
}
