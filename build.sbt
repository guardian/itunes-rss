import scalariform.formatter.preferences._

organization  := "com.gu"
description   := "podcasts RSS feed"
scalaVersion  := "2.12.7"
scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

val root = Project("podcasts-rss", file("."))
  .enablePlugins(PlayScala, RiffRaffArtifact, UniversalPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.jsoup" % "jsoup" % "1.8.1",
      "com.gu" %% "content-api-client" % "15.7",
      "com.squareup.okhttp3" % "okhttp" % "3.11.0",
      "org.scalactic" %% "scalactic" % "3.0.5",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test",
      "com.gu" %% "content-api-models-json" % "15.5" % "test"
    ),
    riffRaffPackageName := "podcasts-rss",
    riffRaffManifestProjectName := s"Off-platform::${name.value}",
    riffRaffPackageType := (packageZipTarball in Universal).value,
    riffRaffUploadArtifactBucket := Some("riffraff-artifact"),
    riffRaffUploadManifestBucket := Some("riffraff-builds")
  )
