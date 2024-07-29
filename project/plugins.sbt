addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.5")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.3")

//otherwise the build fails because of scala-compiler wanting a newer version of scala-xml
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
