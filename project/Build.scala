import com.typesafe.sbt.packager.universal.UniversalPlugin
import sbt._
import Keys._
import play.sbt._
import com.typesafe.sbt.SbtScalariform.scalariformSettings
import com.gu.riffraff.artifact._
import RiffRaffArtifact.autoImport._
import UniversalPlugin.autoImport._

object PodcastsRssBuild extends Build {

  val basicSettings = Seq(
    organization  := "com.gu",
    description   := "podcasts RSS feed",
    scalaVersion  := "2.11.8",
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
  )

  val circeVersion = "0.8.0"
  val capiVersion = "11.36"

  val root = Project("podcasts-rss", file("."))
    .enablePlugins(PlayScala)
    .enablePlugins(RiffRaffArtifact)
    .enablePlugins(UniversalPlugin)
    .settings(

      libraryDependencies ++= Seq(
        "org.jsoup" % "jsoup" % "1.8.1",
        "com.gu" %% "content-api-client" % capiVersion,
        "org.scalactic" %% "scalactic" % "2.2.4",
        "io.circe" %% "circe-core" % circeVersion,
        "io.circe" %% "circe-parser" % circeVersion,
        "org.scalatest" %% "scalatest" % "2.2.5" % "test",
        "com.gu" %% "content-api-models-json" % capiVersion % "test"
      ),
      riffRaffPackageName := "podcasts-rss",
      riffRaffManifestProjectName := s"Off-platform::${name.value}",
      riffRaffPackageType := (packageZipTarball in Universal).value,
      riffRaffBuildIdentifier := sys.env.getOrElse("BUILD_NUMBER", "DEV"),
      riffRaffUploadArtifactBucket := Some("riffraff-artifact"),
      riffRaffUploadManifestBucket := Some("riffraff-builds")
    )
    .settings(basicSettings)
    .settings(scalariformSettings)

}
