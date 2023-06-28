import com.typesafe.sbt.packager.archetypes.systemloader.ServerLoader.Systemd

organization  := "com.gu"
description   := "podcasts RSS feed"

scalaVersion  := "2.13.10"
scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
routesGenerator := InjectedRoutesGenerator

val root = Project("podcasts-rss", file("."))
  .enablePlugins(PlayScala, JavaServerAppPackaging, SystemdPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.jsoup" % "jsoup" % "1.15.4",
      "com.gu" %% "content-api-client" % "19.2.1",
      "com.squareup.okhttp3" % "okhttp" % "4.10.0",
      "software.amazon.awssdk" % "secretsmanager" % "2.20.69",
      "org.scalactic" %% "scalactic" % "3.2.15",
      "org.scalatest" %% "scalatest" % "3.2.15" % "test",
      "net.logstash.logback" % "logstash-logback-encoder" % "7.3",
      "com.gu" %% "content-api-models-json" % "17.5.1" % "test",
      "com.gu" %% "simple-configuration-core" % "1.5.7",
      "com.gu.play-secret-rotation" %% "play-v28" % "0.37",
      "com.gu.play-secret-rotation" %% "aws-parameterstore-sdk-v2" % "0.37",
      //AWS SDK v2 clients
      "software.amazon.awssdk" % "url-connection-client" % "2.20.68", //only used at startup. For operations we use akka http client
    ),
    maintainer := "Guardian Content Platforms <content-platforms.dev@theguardian.com>",
    version := "1.0",
    Debian / serverLoading := Some (Systemd),
    daemonUser := "content-api",
    daemonGroup := "content-api",
    linuxPackageMappings += packageTemplateMapping(s"/var/run/${name.value}")() withUser (daemonUser.value) withGroup (daemonGroup.value),
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-u", sys.env.getOrElse("SBT_JUNIT_OUTPUT", "junit"))
)

Universal / packageName := normalizedName.value

dependencyOverrides ++=Seq(
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.12.7.1",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.9.8",

  //"org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0",

    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.7",
  "org.apache.tomcat.embed" % "tomcat-embed-core" % "8.5.86",
  "org.apache.tomcat" % "tomcat-annotations-api" % "8.5.86",
  "org.apache.thrift" % "libthrift" % "0.14.0",

  "io.netty" % "netty-handler" % "4.1.94.Final",

  "com.google.guava" % "guava" % "32.0.0-jre"
)

excludeDependencies ++= Seq(
  ExclusionRule("software.amazon.awssdk", "apache-client")
)