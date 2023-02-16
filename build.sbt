name := "Scala Guice"

description := "Scala syntax for Guice"

organization := "net.codingwell"

version := "5.2.0"
versionScheme := Some("pvp")

licenses := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/codingwell/scala-guice"))

libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "5.1.0",
  "org.scalatest" %% "scalatest" % "3.2.9" % "test",
  "com.google.code.findbugs" % "jsr305" % "3.0.2" % "compile"
)

libraryDependencies ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) => Seq(
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.2",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    )
    case _ => Seq.empty
  }
}

autoAPIMappings := true

//scalaVersion := "2.13.8"

val scala3 = "3.2.2"
scalaVersion := scala3 // for IDE
//crossScalaVersions := Seq(scala3)
crossScalaVersions := Seq("2.11.12", "2.12.15", "2.13.8", scala3)

scalacOptions := Seq("-unchecked", "-deprecation", "-feature")

Compile / unmanagedSourceDirectories ++= {
  val sourceDir = (Compile / sourceDirectory).value
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n >= 13 => Seq(sourceDir / "scala-2.13+")
    case Some((3, _)) => Seq(sourceDir / "scala-2.13+")
    case _ => Seq(sourceDir / "scala-2.12-")
  }
}

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra :=
<scm>
   <connection>scm:git:https://github.com/codingwell/scala-guice.git</connection>
   <developerConnection>scm:git:ssh://git@github.com:codingwell/scala-guice.git</developerConnection>
   <url>https://github.com/codingwell/scala-guice</url>
</scm>
<developers>
  <developer>
    <id>tsuckow</id>
    <name>Thomas Suckow</name>
    <email>tsuckow@gmail.com</email>
    <url>http://codingwell.net</url>
    <organization>Coding Well</organization>
    <organizationUrl>http://codingwell.net</organizationUrl>
    <roles>
      <role>developer</role>
    </roles>
  </developer>
</developers>
<contributors>
  <contributor>
    <name>Ben Lings</name>
    <roles>
      <role>creator</role>
    </roles>
  </contributor>
</contributors>
