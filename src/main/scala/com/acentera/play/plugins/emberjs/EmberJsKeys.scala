package com.acentera.play.plugins.emberjs

import sbt._

trait EmberJsKeys {

  val emberJsAssetsDir = SettingKey[File]("play-emberjs-assets-dir")
  val emberJsPrefix = SettingKey[String]("play-emberjs-prefix")
  val emberJsVersion = SettingKey[String]("play-emberjs-version")
  val emberObjects = SettingKey[Seq[Seq[String]]]("play-emberjs-objects")

}
