addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.10")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.3")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.14.0")
//otherwise the build fails because of scala-compiler wanting a newer version of scala-xml
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

addDependencyTreePlugin