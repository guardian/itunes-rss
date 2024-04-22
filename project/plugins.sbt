addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.2")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")

//otherwise the build fails because of scala-compiler wanting a newer version of scala-xml
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
