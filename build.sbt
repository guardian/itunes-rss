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
      "org.scalactic" %% "scalactic" % "3.0.5",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test",
      "com.gu" %% "content-api-models-json" % "15.5" % "test"
    ),
    maintainer := "Guardian Content Platforms <content-platforms.dev@theguardian.com>",

      Debian / serverLoading := Some (Systemd),
      daemonUser := "content-api",
      daemonGroup := "content-api",
      linuxPackageMappings += packageTemplateMapping(s"/var/run/${name.value}")() withUser (daemonUser.value) withGroup (daemonUser.value)

//    riffRaffPackageName := "podcasts-rss",
//    riffRaffManifestProjectName := s"Off-platform::${name.value}",
//    riffRaffPackageType := (packageZipTarball in Universal).value,
//    riffRaffUploadArtifactBucket := Some("riffraff-artifact"),
//    riffRaffUploadManifestBucket := Some("riffraff-builds")
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
