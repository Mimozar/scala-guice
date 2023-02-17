Scala extensions for Google Guice 5.1
=====================================

**Develop:** [![Build Status](https://travis-ci.org/codingwell/scala-guice.png?branch=develop)](https://travis-ci.org/codingwell/scala-guice)

Getting Started
---------------

### Add dependency
We currently support Scala `2.11`, `2.12`, `2.13`, and `3`

##### maven:
```xml
<dependency>
    <groupId>net.codingwell</groupId>
    <artifactId>scala-guice_2.13</artifactId>
    <version>5.1.1</version>
</dependency>
```

##### sbt:
```scala
"net.codingwell" %% "scala-guice" % "5.1.1"
```

##### gradle:
```groovy
'net.codingwell:scala-guice_2.13:5.1.1'
```

### Mixin
Mixin ScalaModule with your AbstractModule for rich scala magic (or ScalaPrivateModule with your PrivateModule):
```scala
import com.google.inject.{AbstractModule, PrivateModule}
import net.codingwell.scalaguice.{ScalaModule, ScalaPrivateModule}

class MyModule extends AbstractModule with ScalaModule {
  def configure(): Unit = {
    bind[Service].to[ServiceImpl].in[Singleton]
    bind[CreditCardPaymentService]
    bind[Bar[Foo]].to[FooBarImpl]
    bind[PaymentService].annotatedWith(Names.named("paypal")).to[CreditCardPaymentService]
  }
}

class MyPrivateModule extends PrivateModule with ScalaPrivateModule {
  def configure(): Unit = {
    bind[Foo].to[RealFoo]
    expose[Foo]

    install(new TransactionalBarModule())
    expose[Bar].annotatedWith[Transactional]

    bind[SomeImplementationDetail]
    install(new MoreImplementationDetailsModule())
  }
}
```

### Inject
Wrap the injector in a ScalaInjector for even more rich scala magic:
```scala
import com.google.inject.Guice

object MyServer {
  def main(args: Array[String]) {
    val injector = Guice.createInjector(new MyModule(), new MyPrivateModule)

    import net.codingwell.scalaguice.InjectorExtensions._
    val service = injector.instance[Service]
    val foo = injector.instance[Foo]

    // Retrieve a Bar annotated with Transactional
    val bar = injector.instance[Bar, Transactional]

    // Retrieve a PaymentService annotated with a specific Annotation instance.
    val paymentService = injector.instance[PaymentService](Names.named("paypal"))
    ...
  }
}
```

Additional Features
-------------------

### Module Traits

```scala
class MyModule extends AbstractModule with ScalaModule
```
```scala
class MyPrivateModule extends PrivateModule with ScalaPrivateModule
```

This gives to access to scala style bindings:

```scala
bind[A].to[B]
bind[A].to(classOf[B])
bind[A].to(typeLiteral[B])
bind[A].toInstance("A")
bind[A].annotatedWith[Ann].to[B]
bind[A].annotatedWith( classOf[Ann] ).to[B]
bind[A].annotatedWith( Names.named("name") ).to[B]
bind[A].annotatedWithName("name").to[B]
bind[A].toProvider[BProvider]
bind[A].toProvider[TypeProvider[B]]
bind[A[String]].to[B[String]]
bind[A].to[B].in[Singleton]

bindInterceptor[AOPI](methodMatcher = annotatedWith[AOP])
```

### Injector Extensions

```scala
import net.codingwell.scalaguice.InjectorExtensions._

injector.instance[A]
injector.instance[A, Ann]
injector.instance[A]( Names.named("name") )

injector.provider[A]
injector.provider[A, Ann]
injector.provider[A]( Names.named("name") )

//Returns Option[A]
injector.existingBinding[A]
injector.existingBinding[A, Ann]
injector.existingBinding[A]( Names.named("name") )
```

### Multibinding

The ScalaMultibinder adds scala style multibindings:

```scala
class MyModule extends AbstractModule with ScalaModule {
  def configure(): Unit = {
    val stringMulti = ScalaMultibinder.newSetBinder[String](binder)
    stringMulti.addBinding.toInstance("A")

    val annotatedMulti = ScalaMultibinder.newSetBinder[A, Annotation](binder)
    annotatedMulti.addBinding.to[A]

    val namedMulti = ScalaMultibinder.newSetBinder[ServiceConfiguration](binder, Names.named("backend"))
    namedMulti.addBinding.toInstance(config.getAdminServiceConfiguration)
  }
}
```

And then they may be retrieved as immutable.Set[T]. (examples in order)

```scala
class StringThing @Inject() (strings: immutable.Set[String]) { ... }

class AThing @Inject() (@Annotation configs: immutable.Set[A]) { ... }

class Service @Inject() (@Names.named("backend") configs: immutable.Set[ServiceConfiguration]) { ... }
```

#### Generic Multibinding

```scala
trait Provider[T] {
  def provide: T
}

class StringProvider extends Provider[String] {
  override def provide = "Hello world!"
}

class IntProvider extends Provider[Int] {
  override def provide = 42
}
```

```scala
val multibinder = ScalaMultibinder.newSetBinder[Provider[_]](binder)

multibinder.addBinding.toInstance(new StringProvider)
multibinder.addBinding.toInstance(new IntProvider)
```

`Provider[_]` is actually translated to `Provider[java.lang.Object]`. So you need to use `immutable.Set[Provider[Any]]` and not `immutable.Set[Provider[_]]` :

```scala
class SomeClass @Inject() (providers: immutable.Set[Provider[Any]]) { ... }
```


### OptionBinding

Newly available in Guice 4.0-beta5, we've got some support for OptionalBinder.

```scala
class MyModule extends AbstractModule with ScalaModule {
  def configure(): Unit = {
    val optBinder = ScalaOptionBinder.newOptionBinder[String](binder)
    optBinder.setDefault.toInstance("A")
    // To override the default binding (likely in another module):
    optBinder.setBinding.toInstance("B")

    val annotatedOptBinder = ScalaOptionBinder.newOptionBinder[A, Annotation](binder)
    annotatedOptBinder.setDefault.to[A]

    val namedOptBinder = ScalaOptionBinder.newOptionBinder[ServiceConfiguration](binder, Names.named("backend"))
    namedOptBinder.setBinding.toInstance(config.getAdminServiceConfiguration)
  }
}
```

And then they may be retrieved as `Option[T]`, `Option[Provider[T]]`, and `Option[javax.inject.Provider[T]]`. (examples in order)

```scala
class StringThing @Inject() (name: Option[String]) { ... }

class AThing @Inject() (@Annotation aProvider: Option[Provider[T]]) { ... }

class Service @Inject() (@Names.named("backend") configProvider: Option[javax.inject.Provider[ServiceConfiguration]]) { ... }
```

### MapBinding

The ScalaMapBinder adds scala style mapbindings:

```scala
class MyModule extends AbstractModule with ScalaModule {
  def configure(): Unit = {
    val mBinder = ScalaMapBinder.newMapBinder[String, Int](binder)
    mBinder.addBinding("1").toInstance(1)
  }
}
```

And then may be retrieved as any of the following:
- `immutable.Map[K, V]`
- `immutable.Map[K, Provider[V]]`
- `immutable.Map[K, javax.inject.Provider[V]]`

If you call `mapBinder.permitDuplicates()` on the binder then you may also inject:
- `immutable.Map[K, immutable.Set[V]]`
- `immutable.Map[K, immutable.Set[Provider[V]]]`

### Interceptor Binding

bindInterceptor adds scala style interceptor binding

```java
bindInterceptor(Matchers.any(), Matchers.annotatedWith(classOf[Logging]), new LoggingInterceptor())
```

```scala
bindInterceptor[LoggingInterceptor](methodMatcher = annotatedWith[Logging])
```

## Gotchas

### Reserved Words

In Scala, the words `override` and `with` are reserved and must be escaped to be used.
```scala
Modules.`override`(new BaseModule).`with`(new TestModule)
```

### Mixins

To our knowledge there is no way to represent a Mixin `A with B` in Google Guice.
Guice sees a mixin as its base type and therefore `A with B` is equivalent to `A`.
This means only one can be bound in the injector.
It is also possible to try to inject `A` where `A with B` is expected, most likely resulting in a `ClassCastException`.

### And the stuff we forgot...

If you find a feature we support but don't mention here, submit an issue and we will add it.

If you find a feature we don't support but want, implement it and send us a pull request. Alternatively, you can file an issue and we may or may not get to it.
