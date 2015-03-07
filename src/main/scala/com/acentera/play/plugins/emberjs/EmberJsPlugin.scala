package com.acentera.play.plugins.emberjs

import sbt._
import sbt.Keys._


object EmberJsPlugin extends Plugin with EmberJsTasks {

  val EmberJsViewScriptWatcher = Seq(
    emberJsAssetsDir <<= (sourceDirectory in Compile)(src => src / "assets" / "templates"),
    resourceGenerators in Compile <+= EmberJsViewCompiler
  )


  val EmberJsControllerScriptWatcher = Seq(
    emberJsAssetsDir <<= (sourceDirectory in Compile)(src => src / "assets" / "templates"),
    resourceGenerators in Compile <+= EmberJsControllerCompiler
  )

  val EmberJsModelScriptWatcher = Seq(
    emberJsAssetsDir <<= (sourceDirectory in Compile)(src => src / "assets" / "templates"),
    resourceGenerators in Compile <+= EmberJsModelControllerCompiler
  )


  override def projectSettings: Seq[Setting[_]] = super.projectSettings ++ EmberJsViewScriptWatcher ++ EmberJsControllerScriptWatcher ++ EmberJsModelScriptWatcher

}
