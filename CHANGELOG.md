5.1.0
-----
2022-05-26

* Update Guice 5.1.0
* Update to Scala 2.13.8

5.0.2
-----
2021-09-18

* Update to Scala 2.13.6 (Thanks cacoco)
* Update to Scala 2.12.15
* Update sbt to improve support for modern scala

5.0.1
-----
2021-05-22

* Fix not being able to bind a generic containing the Nothing type

5.0.0
-----
2021-03-08

* Update Guice to 5.0.1

4.2.11
------
2020-07-12

* Resolve inability to find owner causing nested class to not be bindable

4.2.10
------
2020-06-29

* Fix AnyVal types being erased to the type they wrap
* Add provider helpers to the injector extensions

4.2.9
-----
2020-06-24

* Fix Mixins not resolving to the base types (as they did in 4.2.0)
* Add `annotatedWith` that takes a class object
* Fix `(=> Unit)` throwing an exception about being unable to find `<byname>`

4.2.8
-----
2020-06-22

* Fix primative arrays
* Fix Unit

4.2.7
-----
2020-05-27

* Guice 4.2.3

4.2.6
-----
2020-07-19

* Fix for Singleton types

4.2.5
-----
2019-06-22

* Scala 2.13 support

4.2.4
-----
2019-06-02

* Remove Manifest

4.2.3
-----
2019-03-02

* Fix issue with binding to types in different classloaders

4.2.2
-----

* Prep for Scala 2.13
* Guice 4.2.2

4.2.1
-----
2018-05-24

* Better support for wildcard types

4.2.0
-----
2018-03-11

* Guice 4.2

4.1.1
-----
2017-11-14

* Rebuild with updated Scala versions (2.10.7, 2.11.12, 2.12.4)

4.1.0
-----
2016-08-07

* Fix error message being the wrong file/line number
* Update Guice to 4.1
* Add support for Scala 2.12-M5

2016-11-24
* Add support for Scala 2.12