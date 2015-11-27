import com.typesafe.sbt.packager.universal.UniversalPlugin
import sbt._
import Keys._
import play.Play.autoImport._
import play.sbt._
import com.typesafe.sbt.SbtScalariform.scalariformSettings
import com.gu.riffraff.artifact._
import RiffRaffArtifact.autoImport._
import UniversalPlugin.autoImport._

object iTunesRssBuild extends Build {

  val basicSettings = Seq(
    organization  := "com.gu",
    description   := "iTunes RSS feed",
    scalaVersion  := "2.11.7",
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
  )

  val root = Project("content-api-itunes-rss", file("."))
    .enablePlugins(PlayScala)
    .enablePlugins(RiffRaffArtifact)
    .enablePlugins(UniversalPlugin)
    .settings(

      libraryDependencies ++= Seq(
        "com.gu" %% "content-api-client" % "7.9",
        "org.scalactic" %% "scalactic" % "2.2.4",
        "org.scalatest" %% "scalatest" % "2.2.5" % "test"
      ),
      riffRaffPackageName := "content-api-itunes  -rss",
      riffRaffPackageType := (packageZipTarball in Universal).value
    )
    .settings(basicSettings)
    .settings(scalariformSettings)

}
