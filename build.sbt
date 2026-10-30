import com.typesafe.sbt.packager.archetypes.systemloader.ServerLoader.Systemd

organization  := "com.gu"
description   := "podcasts RSS feed"

scalaVersion  := "2.13.18"
scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
routesGenerator := InjectedRoutesGenerator

val awsSdkVersion = "2.55.11"
val jacksonVersion = "2.21.8"

val root = Project("podcasts-rss", file("."))
  .enablePlugins(PlayScala, JavaServerAppPackaging, SystemdPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.jsoup" % "jsoup" % "1.18.1",
      "com.gu" %% "content-api-client" % "36.0.0",
      "com.squareup.okhttp3" % "okhttp" % "4.12.0", // SNYK-JAVA-ORGJETBRAINSKOTLIN-2393744, SNYK-JAVA-COMSQUAREUPOKIO-5820002
      "software.amazon.awssdk" % "secretsmanager" % awsSdkVersion, // SNYK-JAVA-IONETTY-1042268
      "software.amazon.awssdk" % "ssm" % awsSdkVersion, // SNYK-JAVA-IONETTY-1042268
      "org.scalactic" %% "scalactic" % "3.2.19",
      "org.scalatest" %% "scalatest" % "3.2.19" % "test",
      "net.logstash.logback" % "logstash-logback-encoder" % "7.4",
      "com.gu" %% "content-api-models-json" % "30.0.0" % "test", // keeping in line with imports from content-api-client
      "com.gu" %% "simple-configuration-core" % "15.0.0",
      "com.gu.play-secret-rotation" %% "play-v30" % "21.1.0",
      "com.gu.play-secret-rotation" %% "aws-parameterstore-sdk-v2" % "21.1.0",
      //AWS SDK v2 clients
      "software.amazon.awssdk" % "url-connection-client" % awsSdkVersion, //only used at startup. For operations we use akka http client
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

dependencyOverrides ++= Seq(
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % jacksonVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % jacksonVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % jacksonVersion,
  "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % jacksonVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.21", // Minor-only version since Jackson 2.20, per the BOM.
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion
)

excludeDependencies ++= Seq(
  ExclusionRule("software.amazon.awssdk", "apache-client")
)
