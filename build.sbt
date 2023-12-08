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
      "com.gu" %% "content-api-client" % "19.4.0",
      "com.squareup.okhttp3" % "okhttp" % "4.12.0", // SNYK-JAVA-ORGJETBRAINSKOTLIN-2393744, SNYK-JAVA-COMSQUAREUPOKIO-5820002
      "software.amazon.awssdk" % "secretsmanager" % "2.20.162", // SNYK-JAVA-IONETTY-1042268
      "org.scalactic" %% "scalactic" % "3.2.15",
      "org.scalatest" %% "scalatest" % "3.2.15" % "test",
      "net.logstash.logback" % "logstash-logback-encoder" % "7.3",
      "com.gu" %% "content-api-models-json" % "17.7.0" % "test",
      "com.gu" %% "simple-configuration-core" % "1.5.8",
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
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.7",
  "io.netty" % "netty-handler" % "4.1.94.Final",
  "io.netty" % "netty-codec-http2" % "4.1.100.Final", // SNYK-JAVA-IONETTY-5953332
  "ch.qos.logback" % "logback-classic" % "1.4.14",
)

excludeDependencies ++= Seq(
  ExclusionRule("software.amazon.awssdk", "apache-client")
)