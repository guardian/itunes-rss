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
      "org.jsoup" % "jsoup" % "1.18.1",
      "com.gu" %% "content-api-client" % "36.0.0",
      "com.squareup.okhttp3" % "okhttp" % "4.12.0", // SNYK-JAVA-ORGJETBRAINSKOTLIN-2393744, SNYK-JAVA-COMSQUAREUPOKIO-5820002
      "software.amazon.awssdk" % "secretsmanager" % "2.25.32", // SNYK-JAVA-IONETTY-1042268
      "org.scalactic" %% "scalactic" % "3.2.19",
      "org.scalatest" %% "scalatest" % "3.2.19" % "test",
      "net.logstash.logback" % "logstash-logback-encoder" % "7.4",
      "com.gu" %% "content-api-models-json" % "30.0.0" % "test", // keeping in line with imports from content-api-client
      "com.gu" %% "simple-configuration-core" % "2.0.0",
      "com.gu.play-secret-rotation" %% "play-v30" % "8.2.1",
      "com.gu.play-secret-rotation" %% "aws-parameterstore-sdk-v2" % "8.2.1",
      //AWS SDK v2 clients
      "software.amazon.awssdk" % "url-connection-client" % "2.26.22", //only used at startup. For operations we use akka http client
      "joda-time" % "joda-time" % "2.12.7"
    ),
    maintainer := "Guardian Content Platforms <content-platforms.dev@theguardian.com>",
    version := "1.0",
    Debian / serverLoading := Some (Systemd),
    daemonUser := "content-api",
    daemonGroup := "content-api",
    linuxPackageMappings += packageTemplateMapping(s"/var/run/${name.value}")() withUser (daemonUser.value) withGroup (daemonGroup.value),
    Test / testOptions += Tests.Argument(
      TestFrameworks.ScalaTest,
      "-u", sys.env.getOrElse("SBT_JUNIT_OUTPUT", "junit"),
      "-o"
    )
)

Universal / packageName := normalizedName.value

dependencyOverrides ++=Seq(
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % "2.17.1",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % "2.17.2",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.17.2",
  "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % "2.17.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.17.2",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.17.2",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.17.2",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.17.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.17.2"
)

excludeDependencies ++= Seq(
  ExclusionRule("software.amazon.awssdk", "apache-client")
)
