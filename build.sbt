import com.typesafe.sbt.packager.archetypes.systemloader.ServerLoader.Systemd

organization  := "com.gu"
description   := "podcasts RSS feed"

scalaVersion  := "2.12.7"
scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
routesGenerator := InjectedRoutesGenerator

val root = Project("podcasts-rss", file("."))
  .enablePlugins(PlayScala, JavaServerAppPackaging, SystemdPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.jsoup" % "jsoup" % "1.15.4",
      "com.gu" %% "content-api-client" % "15.7",
      "com.squareup.okhttp3" % "okhttp" % "4.9.2",
      "software.amazon.awssdk" % "secretsmanager" % "2.20.57",
      "org.scalactic" %% "scalactic" % "3.0.5",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test",
      "net.logstash.logback" % "logstash-logback-encoder" % "7.3",
      "com.gu" %% "content-api-models-json" % "15.5" % "test",
      "com.gu" %% "simple-configuration-core" % "1.5.7",
      //AWS SDK v2 clients
      "software.amazon.awssdk" % "url-connection-client" % "2.20.26", //only used at startup. For operations we use akka http client

    ),
    maintainer := "Guardian Content Platforms <content-platforms.dev@theguardian.com>",
    version := "1.0",
    Debian / serverLoading := Some (Systemd),
    daemonUser := "content-api",
    daemonGroup := "content-api",
    linuxPackageMappings += packageTemplateMapping(s"/var/run/${name.value}")() withUser (daemonUser.value) withGroup (daemonUser.value),
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-u", sys.env.getOrElse("SBT_JUNIT_OUTPUT", "junit"))
)

Universal / packageName := normalizedName.value

dependencyOverrides ++=Seq(
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.12.6.1",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.9.8",

  "com.typesafe.play" % "play-akka-http-server_2.12" % "2.7.0",

  "com.typesafe.akka" % "akka-actor_2.12" % "2.5.31",
  "com.typesafe.akka" % "akka-slf4j_2.12" % "2.5.31",
  "com.typesafe.akka" % "akka-stream_2.12" % "2.5.31",
  "com.typesafe.akka" % "akka-http-core_2.12" % "10.1.15",

  "org.apache.tomcat.embed" % "tomcat-embed-core" % "8.5.85",
  "org.apache.tomcat" % "tomcat-annotations-api" % "8.5.85",
  "org.apache.thrift" % "libthrift" % "0.14.0",
)