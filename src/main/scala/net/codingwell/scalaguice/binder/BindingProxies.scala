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
package binder

import com.google.inject._
import com.google.inject.binder._
import com.google.inject.name.Names
import java.lang.annotation.{Annotation => JAnnotation}
import java.lang.reflect.{Constructor => JConstructor}
import net.codingwell.scalaguice.ScalaModule.{ScalaLinkedBindingBuilder, ScalaScopedBindingBuilder}
import scala.language.postfixOps

/**
 * Proxy for com.google.inject.binder.ScopedBindingBuilder
 */
trait ScopedBindingBuilderProxy extends ScopedBindingBuilder {

  def self: ScopedBindingBuilder

  def asEagerSingleton(): Unit = self.asEagerSingleton()
  def in(scope: Scope): Unit = self.in(scope)
  def in(scopeAnnotation: Class[_ <: JAnnotation]): Unit = self.in(scopeAnnotation)
  override def hashCode: Int = self.hashCode
  override def equals(that: Any): Boolean = that match {
    case null  => false
    case _     =>
      val x = that.asInstanceOf[AnyRef]
      (x eq this.asInstanceOf[AnyRef]) || (x eq self.asInstanceOf[AnyRef]) || (x equals self)
  }
  override def toString: String = "" + self
}

/**
 * Proxy for com.google.inject.binder.LinkedBindingBuilder
 */
trait LinkedBindingBuilderProxy[T] extends LinkedBindingBuilder[T] with ScopedBindingBuilderProxy {
  override def self: LinkedBindingBuilder[T]

  override def toInstance(instance: T): Unit = self.toInstance(instance)

  override def to(implementation: Class[_ <: T]) = newBuilder(self.to(implementation))
  override def to(implementation: TypeLiteral[_ <: T]) = newBuilder(self.to(implementation))
  override def to(targetKey: Key[_ <: T]) = newBuilder(self.to(targetKey))
  override def toConstructor[S <: T](constructor:JConstructor[S]) = newBuilder(self.toConstructor(constructor))
  override def toConstructor[S <: T](constructor:JConstructor[S], literal:TypeLiteral[_ <: S]) = newBuilder(self.toConstructor(constructor,literal))
  override def toProvider(provider: Provider[_ <: T]) = newBuilder(self.toProvider(provider))
  override def toProvider(provider: jakarta.inject.Provider[_ <: T]) = newBuilder(self.toProvider(provider))
  def toProvider(provider: Class[_ <: jakarta.inject.Provider[_ <: T]]) = newBuilder(self.toProvider(provider))
  def toProvider(provider: TypeLiteral[_ <: jakarta.inject.Provider[_ <: T]]) = newBuilder(self.toProvider(provider))
  def toProvider(providerKey: Key[_ <: jakarta.inject.Provider[_ <: T]]) = newBuilder(self.toProvider(providerKey))

  private[this] def newBuilder(underlying: ScopedBindingBuilder) = new ScalaScopedBindingBuilder {
    val self = underlying
  }
}

/**
 * Proxy for com.google.inject.binder.AnnotatedBindingBuilder
 */
trait AnnotatedBindingBuilderProxy[T] extends AnnotatedBindingBuilder[T] with LinkedBindingBuilderProxy[T] {
  override def self: AnnotatedBindingBuilder[T]

  def annotatedWith(annotation: JAnnotation): ScalaLinkedBindingBuilder[T] = newBuilder(self.annotatedWith(annotation))
  def annotatedWith(annotationType: Class[_ <: JAnnotation]): ScalaLinkedBindingBuilder[T] = newBuilder(self.annotatedWith(annotationType))
  def annotatedWithName(name: String): ScalaLinkedBindingBuilder[T] = annotatedWith(Names.named(name))

  private[this] def newBuilder(underlying: LinkedBindingBuilder[T]) = new ScalaLinkedBindingBuilder[T] {
    val self = underlying
  }
}

/**
 * Proxy for com.google.inject.binder.AnnotatedElementBuilder
 */
trait AnnotatedElementBuilderProxy[T] extends AnnotatedElementBuilder {
  def self: AnnotatedElementBuilder

  def annotatedWith(annotation: JAnnotation): Unit = self.annotatedWith(annotation)
  def annotatedWith(annotationType: Class[_ <: JAnnotation]): Unit = self.annotatedWith(annotationType)
  def annotatedWithName(name: String): Unit = annotatedWith(Names.named(name))
  override def hashCode: Int = self.hashCode
  override def equals(that: Any): Boolean = that match {
    case null  => false
    case _     =>
      val x = that.asInstanceOf[AnyRef]
      (x eq this.asInstanceOf[AnyRef]) || (x eq self.asInstanceOf[AnyRef]) || (x equals self)
  }
  override def toString: String = "" + self
}
