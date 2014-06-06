package com.ketalo.play.plugins.emberjs

import sbt._

trait EmberJsKeys {
  val emberAdminJsAssetsDir = SettingKey[File]("play-emberjs-admin-assets-dir")
  val emberAdminJsFileEnding = SettingKey[String]("play-emberjs-admin-file-ending")
  val emberAdminJsAssetsGlob = SettingKey[PathFinder]("play-emberjs-admin-assets-glob")
  val emberAdminJsTemplateFile = SettingKey[String]("play-emberjs-admin-template-file")
  val emberAdminJsFileRegexFrom = SettingKey[String]("play-emberjs-admin-file-regex-from")
  val emberAdminJsFileRegexTo = SettingKey[String]("play-emberjs-admin-file-regex-to")
  val emberUserJsAssetsDir = SettingKey[File]("play-emberjs-user-assets-dir")
  val emberUserJsAssetsOverrideDir = SettingKey[File]("play-emberjs-user-override-assets-dir")
  val emberUserJsFileEnding = SettingKey[String]("play-emberjs-user-file-ending")
  val emberUserJsAssetsGlob = SettingKey[PathFinder]("play-emberjs-user-assets-glob")
  val emberUserJsTemplateFile = SettingKey[String]("play-emberjs-user-template-file")
  val emberUserJsFileRegexFrom = SettingKey[String]("play-emberjs-user-file-regex-from")
  val emberUserJsFileRegexTo = SettingKey[String]("play-emberjs-user-file-regex-to")


  val emberUserJsOverrideAssetsDir = SettingKey[File]("play-emberjs-user-override-assets-dir")
  val emberUserJsOverrideFileEnding = SettingKey[String]("play-emberjs-user-override-file-ending")
  val emberUserJsOverrideAssetsGlob = SettingKey[PathFinder]("play-emberjs-user-override-assets-glob")
  val emberUserJsOverrideTemplateFile = SettingKey[String]("play-emberjs-user-override-template-file")
  val emberUserJsOverrideFileRegexFrom = SettingKey[String]("play-emberjs-user-override-file-regex-from")
  val emberUserJsOverrideFileRegexTo = SettingKey[String]("play-emberjs-user-override-file-regex-to")


  val emberJsPrefix = SettingKey[String]("play-emberjs-prefix")
  val emberJsVersion = SettingKey[String]("play-emberjs-version")

  val emberAdminControllerJsAssetsDir = SettingKey[File]("play-emberjs-admincontroller-assets-dir")
  val emberAdminControllerJsFileEnding = SettingKey[String]("play-emberjs-admincontroller-file-ending")
  val emberAdminControllerJsAssetsGlob = SettingKey[PathFinder]("play-emberjs-admincontroller-assets-glob")
  val emberAdminControllerJsTemplateFile = SettingKey[String]("play-emberjs-admincontroller-template-file")
  val emberAdminControllerJsFileRegexFrom = SettingKey[String]("play-emberjs-admincontroller-file-regex-from")
  val emberAdminControllerJsFileRegexTo = SettingKey[String]("play-emberjs-admincontroller-file-regex-to")
  val emberAdminControllerJsVersion = SettingKey[String]("play-emberjs-admincontroller-version")
  val emberAdminViewJsAssetsDir = SettingKey[File]("play-emberjs-adminview-assets-dir")
  val emberAdminViewJsFileEnding = SettingKey[String]("play-emberjs-adminview-file-ending")
  val emberAdminViewJsAssetsGlob = SettingKey[PathFinder]("play-emberjs-adminview-assets-glob")
  val emberAdminViewJsTemplateFile = SettingKey[String]("play-emberjs-adminview-template-file")
  val emberAdminViewJsFileRegexFrom = SettingKey[String]("play-emberjs-adminview-file-regex-from")
  val emberAdminViewJsFileRegexTo = SettingKey[String]("play-emberjs-adminview-file-regex-to")

  val emberModelJsAssetsDir = SettingKey[File]("play-emberjs-model-assets-dir")
  val emberModelJsFileEnding = SettingKey[String]("play-emberjs-model-file-ending")
  val emberModelJsAssetsGlob = SettingKey[PathFinder]("play-emberjs-model-assets-glob")
  val emberModelJsTemplateFile = SettingKey[String]("play-emberjs-model-template-file")
  val emberModelJsFileRegexFrom = SettingKey[String]("play-emberjs-model-file-regex-from")
  val emberModelJsFileRegexTo = SettingKey[String]("play-emberjs-model-file-regex-to")
  val emberModelJsVersion = SettingKey[String]("play-emberjs-model-version")

  val emberUserControllerJsAssetsDir = SettingKey[File]("play-emberjs-usercontroller-assets-dir")
  val emberUserControllerJsFileEnding = SettingKey[String]("play-emberjs-usercontroller-file-ending")
  val emberUserControllerJsAssetsGlob = SettingKey[PathFinder]("play-emberjs-usercontroller-assets-glob")
  val emberUserControllerJsTemplateFile = SettingKey[String]("play-emberjs-usercontroller-template-file")
  val emberUserControllerJsFileRegexFrom = SettingKey[String]("play-emberjs-usercontroller-file-regex-from")
  val emberUserControllerJsFileRegexTo = SettingKey[String]("play-emberjs-usercontroller-file-regex-to")
  val emberUserControllerJsVersion = SettingKey[String]("play-emberjs-usercontroller-version")


  val emberUserControllerJsOverrideAssetsDir = SettingKey[File]("play-emberjs-usercontroller-override-assets-dir")
  val emberUserControllerJsOverrideFileEnding = SettingKey[String]("play-emberjs-usercontroller-override-file-ending")
  val emberUserControllerJsOverrideAssetsGlob = SettingKey[PathFinder]("play-emberjs-usercontroller-override-assets-glob")
  val emberUserControllerJsOverrideTemplateFile = SettingKey[String]("play-emberjs-usercontroller-override-template-file")
  val emberUserControllerJsOverrideFileRegexFrom = SettingKey[String]("play-emberjs-usercontroller-override-file-regex-from")
  val emberUserControllerJsOverrideFileRegexTo = SettingKey[String]("play-emberjs-usercontroller-override-file-regex-to")
  val emberUserControllerJsOverrideVersion = SettingKey[String]("play-emberjs-usercontroller-override-version")




  val emberCommonViewJsAssetsDir = SettingKey[File]("play-emberjs-commonview-assets-dir")
  val emberCommonViewJsFileEnding = SettingKey[String]("play-emberjs-commonview-file-ending")
  val emberCommonViewJsAssetsGlob = SettingKey[PathFinder]("play-emberjs-commonview-assets-glob")
  val emberCommonViewJsTemplateFile = SettingKey[String]("play-emberjs-commonview-template-file")
  val emberCommonViewJsFileRegexFrom = SettingKey[String]("play-emberjs-commonview-file-regex-from")
  val emberCommonViewJsFileRegexTo = SettingKey[String]("play-emberjs-commonview-file-regex-to")

  val emberCommonControllerJsAssetsDir = SettingKey[File]("play-emberjs-commoncontroller-assets-dir")
  val emberCommonControllerJsFileEnding = SettingKey[String]("play-emberjs-commoncontroller-file-ending")
  val emberCommonControllerJsAssetsGlob = SettingKey[PathFinder]("play-emberjs-commoncontroller-assets-glob")
  val emberCommonControllerJsTemplateFile = SettingKey[String]("play-emberjs-commoncontroller-template-file")
  val emberCommonControllerJsFileRegexFrom = SettingKey[String]("play-emberjs-commoncontroller-file-regex-from")
  val emberCommonControllerJsFileRegexTo = SettingKey[String]("play-emberjs-commoncontroller-file-regex-to")



}
