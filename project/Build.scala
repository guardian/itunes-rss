import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._
import play.sbt._
import play.sbt.routes.RoutesKeys._
import com.typesafe.sbt.SbtScalariform.scalariformSettings
import com.typesafe.sbt.packager.Keys._
import com.gu.riffraff.artifact._
import RiffRaffArtifact.autoImport._

object iTunesRssBuild extends Build {

  val basicSettings = Seq(
    organization  := "com.gu",
    description   := "iTunes RSS feed",
    scalaVersion  := "2.11.7",
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
  )

  val root = Project("itunes-rss", file("."))
    .enablePlugins(PlayScala)
    .enablePlugins(RiffRaffArtifact)
    .settings(

      libraryDependencies ++= Seq(
        "com.gu" %% "content-api-client" % "7.7",
        "org.scalactic" %% "scalactic" % "2.2.4",
        "org.scalatest" %% "scalatest" % "2.2.5" % "test"
      ),
      riffRaffPackageName := "content-api-itunes  -rss",
      riffRaffPackageType := (packageZipTarball in config("universal")).value
    )
    .settings(basicSettings)
    .settings(scalariformSettings)

}
