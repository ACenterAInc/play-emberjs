package com.ketalo.play.plugins.emberjs

import sbt._
import sbt.Keys._
import org.apache.commons.io.FilenameUtils

object EmberJsPlugin extends Plugin with EmberJsTasks {

  val emberCommonViewJsSettins = Seq(
    emberJsVersion := "1.4.0",
    emberCommonViewJsAssetsDir <<= (sourceDirectory in Compile)(src => (src / "assets" / "templates" / "base" / "common" / "views" )),
    emberCommonViewJsFileEnding := ".handlebars",
    emberCommonViewJsTemplateFile <<= (emberJsPrefix)(prefix => prefix + "/views/common.pre.js"),
    emberCommonViewJsAssetsGlob <<= (emberCommonViewJsAssetsDir)(assetsDir =>  (assetsDir ** "*.handlebars")),
    emberCommonViewJsFileRegexFrom <<= (emberCommonViewJsFileEnding)(fileEnding => fileEnding),
    emberCommonViewJsFileRegexTo <<= (emberCommonViewJsFileEnding)(fileEnding => FilenameUtils.removeExtension(fileEnding) + ".js"),

    resourceGenerators in Compile <+= EmberCommonViewJsCompiler
  )


  val emberUserJsSettins = Seq(
    emberJsVersion := "1.4.0",
    emberUserJsAssetsDir <<= (sourceDirectory in Compile)(src => (src / "assets" / "templates" )),
    emberUserJsFileEnding := ".handlebars",
    emberUserJsTemplateFile <<= (emberJsPrefix)(prefix => prefix + "/views/user.pre.js"),
    emberUserJsAssetsGlob <<= (emberUserJsAssetsDir)(assetsDir =>  (assetsDir ** "*.handlebars")),
    emberUserJsFileRegexFrom <<= (emberUserJsFileEnding)(fileEnding => fileEnding),
    emberUserJsFileRegexTo <<= (emberUserJsFileEnding)(fileEnding => FilenameUtils.removeExtension(fileEnding) + ".js"),

    resourceGenerators in Compile <+= EmberUserJsCompiler
  )

  val emberUserJsOverrideSettins = Seq(
    emberJsVersion := "1.4.0",
    emberUserJsOverrideAssetsDir <<= (sourceDirectory in Compile)(src => (src / "assets" / "templates" /  "custom" / "user" / "views" )),
    emberUserJsOverrideFileEnding := ".handlebars",
    emberUserJsOverrideTemplateFile <<= (emberJsPrefix)(prefix => prefix + "/views/user_override.pre.js"),
    emberUserJsOverrideAssetsGlob <<= (emberUserJsOverrideAssetsDir)(overrideassetsDir =>  (overrideassetsDir ** "*.handlebars")),
    emberUserJsOverrideFileRegexFrom <<= (emberUserJsOverrideFileEnding)(fileEnding => fileEnding),
    emberUserJsOverrideFileRegexTo <<= (emberUserJsOverrideFileEnding)(fileEnding => FilenameUtils.removeExtension(fileEnding) + ".js"),

    resourceGenerators in Compile <+= EmberUserJsOverrideCompiler
  )

  val emberAdminViewJsSettins = Seq(
    emberJsVersion := "1.4.0",
    emberAdminViewJsAssetsDir <<= (sourceDirectory in Compile)(src => (src / "assets" / "templates" /  "base" / "admin" / "views" )),
    emberAdminViewJsFileEnding := ".handlebars",
    emberAdminViewJsTemplateFile <<= (emberJsPrefix)(prefix => prefix + "/views/admin.pre.js"),
    emberAdminViewJsAssetsGlob <<= (emberAdminViewJsAssetsDir)(assetsDir =>  (assetsDir ** "*.handlebars")),
    emberAdminViewJsFileRegexFrom <<= (emberAdminViewJsFileEnding)(fileEnding => fileEnding),
    emberAdminViewJsFileRegexTo <<= (emberAdminViewJsFileEnding)(fileEnding => FilenameUtils.removeExtension(fileEnding) + ".js"),

    resourceGenerators in Compile <+= EmberAdminViewJsCompiler
  )

  val emberAdminControllerJsSettings = Seq(
    emberJsVersion := "1.4.0",
    emberAdminControllerJsAssetsDir <<= (sourceDirectory in Compile)(src => (src / "assets" / "templates" /  "base" / "admin" / "controllers" )),
    emberAdminControllerJsFileEnding := ".js",
    emberAdminControllerJsTemplateFile <<= (emberJsPrefix)(prefix => prefix + "/controllers/admin.pre.js"),
    emberAdminControllerJsAssetsGlob <<= (emberAdminControllerJsAssetsDir)(assetsDir =>  (assetsDir ** "*.js")),
    emberAdminControllerJsFileRegexFrom <<= (emberAdminControllerJsFileEnding)(fileEnding => fileEnding),
    emberAdminControllerJsFileRegexTo <<= (emberUserJsFileEnding)(fileEnding => FilenameUtils.removeExtension(fileEnding) + ".js"),

    resourceGenerators in Compile <+= EmberAdminControllerJsCompiler
  )

  val emberCommonControllerJsSettings = Seq(
    emberJsVersion := "1.4.0",
    emberCommonControllerJsAssetsDir <<= (sourceDirectory in Compile)(src => (src / "assets" / "templates" /  "base" / "common" / "controllers" )),
    emberCommonControllerJsFileEnding := ".js",
    emberCommonControllerJsTemplateFile <<= (emberJsPrefix)(prefix => prefix + "/controllers/common.pre.js"),
    emberCommonControllerJsAssetsGlob <<= (emberCommonControllerJsAssetsDir)(assetsDir =>  (assetsDir ** "*.js")),
    emberCommonControllerJsFileRegexFrom <<= (emberCommonControllerJsFileEnding)(fileEnding => fileEnding),
    emberCommonControllerJsFileRegexTo <<= (emberUserJsFileEnding)(fileEnding => FilenameUtils.removeExtension(fileEnding) + ".js"),

    resourceGenerators in Compile <+= EmberCommonControllerJsCompiler

  )

  val emberUserControllerJsSettings = Seq(
    emberJsVersion := "1.4.0",
    //emberUserControllerJsAssetsDir <<= (sourceDirectory in Compile)(src => (src / "assets" / "templates" /  "base" / "user" / "controllers" )),
    emberUserControllerJsAssetsDir <<= (sourceDirectory in Compile)(src => (src / "assets" / "templates" )),
    emberUserControllerJsFileEnding := ".js",
    emberUserControllerJsTemplateFile <<= (emberJsPrefix)(prefix => prefix + "/controllers/user.pre.js"),
    emberUserControllerJsAssetsGlob <<= (emberUserControllerJsAssetsDir)(assetsDir =>  (assetsDir ** "*.js")),
    emberUserControllerJsFileRegexFrom <<= (emberUserControllerJsFileEnding)(fileEnding => fileEnding),
    emberUserControllerJsFileRegexTo <<= (emberUserJsFileEnding)(fileEnding => FilenameUtils.removeExtension(fileEnding) + ".js"),

    resourceGenerators in Compile <+= EmberUserControllerJsCompiler

  )

  val emberUserControllerOverrideJsSettings = Seq(
    emberJsVersion := "1.4.0",
    emberUserControllerJsOverrideAssetsDir <<= (sourceDirectory in Compile)(src => (src / "assets" / "templates" /  "custom" / "user" / "controllers" )),
    emberUserControllerJsOverrideFileEnding := ".js",
    emberUserControllerJsOverrideTemplateFile <<= (emberJsPrefix)(prefix => prefix + "/controllers/user_override.pre.js"),
    emberUserControllerJsOverrideAssetsGlob <<= (emberUserControllerJsOverrideAssetsDir)(assetsDir =>  (assetsDir ** "*.js")),
    emberUserControllerJsOverrideFileRegexFrom <<= (emberUserControllerJsOverrideFileEnding)(fileEnding => fileEnding),
    emberUserControllerJsOverrideFileRegexTo <<= (emberUserJsOverrideFileEnding)(fileEnding => FilenameUtils.removeExtension(fileEnding) + ".js"),

    resourceGenerators in Compile <+= EmberUserControllerJsOverrideCompiler

  )


  val emberModelJsSettings = Seq(
    emberJsVersion := "1.4.0",
    emberModelJsAssetsDir <<= (sourceDirectory in Compile)(src => (src / "assets" / "templates" /  "base" / "common" / "models")),
    emberModelJsFileEnding := ".js",
    emberModelJsTemplateFile <<= (emberJsPrefix)(prefix => prefix + "/models.pre.js"),
    emberModelJsAssetsGlob <<= (emberModelJsAssetsDir)(assetsDir =>  (assetsDir ** "*.js")),
    emberModelJsFileRegexFrom <<= (emberModelJsFileEnding)(fileEnding => fileEnding),
    emberModelJsFileRegexTo <<= (emberUserJsFileEnding)(fileEnding => FilenameUtils.removeExtension(fileEnding) + ".js"),

    resourceGenerators in Compile <+= EmberModelJsCompiler

  )

  //override def projectSettings: Seq[Setting[_]] = super.projectSettings ++ emberUserJsSettins ++ emberUserControllerJsSettings ++ emberModelJsSettings ++ emberCommonViewJsSettins ++ emberCommonControllerJsSettings ++ emberAdminViewJsSettins ++ emberAdminControllerJsSettings ++ emberUserJsOverrideSettins
  //override def projectSettings: Seq[Setting[_]] = super.projectSettings ++ emberUserJsSettins ++ emberModelJsSettings ++ emberUserControllerJsSettings
  override def projectSettings: Seq[Setting[_]] = super.projectSettings ++ emberUserJsSettins ++ emberUserControllerJsSettings
  //
  //++ emberCommonViewJsSettins



}
