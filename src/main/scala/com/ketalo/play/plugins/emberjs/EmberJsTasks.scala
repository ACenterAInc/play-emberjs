package com.ketalo.play.plugins.emberjs

import java.io._
import org.apache.commons.io.FilenameUtils

import sbt._
import play.PlayExceptions.AssetCompilationException

trait EmberJsTasks extends EmberJsKeys {
  val modificationTimeCache = scala.collection.mutable.Map.empty[String, Long] // Keeps track of the modification time
  var modelId : Long = 0;
  val versions = Map(
    "1.4.0"       -> (("ember-1.4.0.for-rhino", "handlebars-v1.3.0", "headless-ember-1.1.2", "acentera"))
  )

  private def loadResource(name: String): Option[Reader] = {
    Option(this.getClass.getClassLoader.getResource(name)).map(_.openConnection().getInputStream).map(s => new InputStreamReader(s))
  }

  def compileJsOnly(version:String, name: String, source: String): Either[(String, Int, Int), String] = {
    println(s"Compile JS Controllers $name with ember")

    import org.mozilla.javascript._
    import org.mozilla.javascript.tools.shell._

 //   val (ember, handlebars, headless) = versions.get(version).getOrElse(("", "", ""))
    val ctx = Context.enter
    ctx.setLanguageVersion(org.mozilla.javascript.Context.VERSION_1_7)
    ctx.setOptimizationLevel(-1) // Needed to get around a 64K limit

    val global = new Global
    global.init(ctx)
    val scope = ctx.initStandardObjects(global)

    def loadScript(script: String) {
      // load handlebars
      val scriptFile = loadResource(script + ".js").getOrElse(throw new Exception("script: could not find " + script))

      try {
        ctx.evaluateReader(scope, scriptFile, script, 1, null)
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
  //  loadScript(handlebars)
  //  loadScript(headless)
  //  loadScript(ember)

    ScriptableObject.putProperty(scope, "rawSource", source.replace("\r", ""))

    try {
      Right(ctx.evaluateString(scope, "rawSource", "EmberUserControllerJsCompiler", 0, null).toString)
    } catch {
      case e: JavaScriptException => {
        Left(e.details(), e.lineNumber(), 0)
      }
      case e: org.mozilla.javascript.EcmaError => {
        Left(e.details(), e.lineNumber(), 0)
      }
    }
  }

  def compileModel(version:String, name: String, source: String): Either[(String, Int, Int), String] = {
    //println(s"Compile handlebars template: $name with ember version $version")

    import org.mozilla.javascript._
    import org.mozilla.javascript.tools.shell._

    val (ember, handlebars, headless, acentera) = versions.get(version).getOrElse(("", "", "",""))
    val ctx = Context.enter
    ctx.setLanguageVersion(org.mozilla.javascript.Context.VERSION_1_7)
    ctx.setOptimizationLevel(-1) // Needed to get around a 64K limit

    val global = new Global
    global.init(ctx)
    val scope = ctx.initStandardObjects(global)

    def loadScript(script: String) {
      // load handlebars
      val scriptFile = loadResource(script + ".js").getOrElse(throw new Exception("script: could not find " + script))

      try {
        ctx.evaluateReader(scope, scriptFile, script, 1, null)
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
    loadScript(handlebars)
    loadScript(headless)
    loadScript(ember)
    loadScript(acentera)

    ScriptableObject.putProperty(scope, "rawSource", source.replace("\r", ""))

    try {
      Right(ctx.evaluateString(scope, "(Ember.Handlebars.precompile(rawSource).toString())", "EmberModelCompiler", 0, null).toString)
    } catch {
      case e: JavaScriptException => {
        Left(e.details(), e.lineNumber(), 0)
      }
      case e: org.mozilla.javascript.EcmaError => {
        Left(e.details(), e.lineNumber(), 0)
      }
    }
  }

  def compile(version:String, name: String, source: String): Either[(String, Int, Int), String] = {
    println(s"Compile handlebars template: $name with ember")

    import org.mozilla.javascript._
    import org.mozilla.javascript.tools.shell._

    val (ember, handlebars, headless, acentera) = versions.get(version).getOrElse(("", "", "",""))
    val ctx = Context.enter
    ctx.setLanguageVersion(org.mozilla.javascript.Context.VERSION_1_7)
    ctx.setOptimizationLevel(-1) // Needed to get around a 64K limit

    val global = new Global
    global.init(ctx)
    val scope = ctx.initStandardObjects(global)

    def loadScript(script: String) {
      // load handlebars
      val scriptFile = loadResource(script + ".js").getOrElse(throw new Exception("script: could not find " + script))

      try {
        ctx.evaluateReader(scope, scriptFile, script, 1, null)
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
    loadScript(handlebars)
    loadScript(headless)
    loadScript(ember)

    ScriptableObject.putProperty(scope, "rawSource", source.replace("\r", ""))

    try {
      Right(ctx.evaluateString(scope, "(Ember.Handlebars.precompile(rawSource).toString())", "EmberJsCompiler", 0, null).toString)
    } catch {
      case e: JavaScriptException => {
        Left(e.details(), e.lineNumber(), 0)
      }
      case e: org.mozilla.javascript.EcmaError => {
        Left(e.details(), e.lineNumber(), 0)
      }
    }
  }

  protected def templateName(sourceFile: String, assetsDir: String): String = {
    val sourceFileWithForwardSlashes = FilenameUtils.separatorsToUnix(sourceFile)
    val assetsDirWithForwardSlashes  = FilenameUtils.separatorsToUnix(assetsDir)
    FilenameUtils.removeExtension(
      sourceFileWithForwardSlashes.replace(assetsDirWithForwardSlashes + "/", "")
    )
  }

  import Keys._

  lazy val EmberUserControllerJsCompiler = (sourceDirectory in Compile, resourceManaged in Compile, cacheDirectory, emberJsPrefix, emberUserControllerJsTemplateFile, emberUserControllerJsFileRegexFrom, emberUserControllerJsFileRegexTo, emberUserControllerJsAssetsDir, emberUserControllerJsAssetsGlob).map {
      (src, resources, cache, prefix, templateFile, fileReplaceRegexp, fileReplaceWith, assetsDir, files) =>
      val version = "1.4.0"
      val cacheFile = cache / "emberjsusercontroller"
      val templatesDir = resources / "public" / "templates"
      val global = templatesDir /  templateFile
      val globalMinified = templatesDir /  (FilenameUtils.removeExtension(templateFile) + ".min.js")

      def naming(name: String) = name.replaceAll(fileReplaceRegexp, fileReplaceWith)

      val latestTimestamp = files.get.sortBy(f => FileInfo.lastModified(f).lastModified).reverse.map(f => FileInfo.lastModified(f)).headOption.getOrElse(FileInfo.lastModified(global))
      val currentInfos = files.get.map(f => f -> FileInfo.lastModified(f))
      val allFiles = (currentInfos ++ Seq(global -> latestTimestamp, globalMinified -> latestTimestamp)).toMap

      val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)
      val previousGeneratedFiles = previousRelation._2s

      if (previousInfo != allFiles) {
        val output = new StringBuilder
        output ++= """

                 """

        val generated:Seq[(File, File)] = (files x relativeTo(assetsDir)).flatMap {
          case (sourceFile, name) => {
            val template = templateName(sourceFile.getPath, assetsDir.getPath)
            val jsSource = compileJsOnly(version, template, IO.read(sourceFile)).left.map {
              case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                msg,
                Some(line),
                Some(column))
            }.right.get
            /*
            val jsSource = if (modificationTimeCache.get(sourceFile.getAbsolutePath).map(time => time != sourceFile.lastModified()).getOrElse(true)) {
              compileJsOnly(version, template, IO.read(sourceFile)).left.map {
                case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                  msg,
                  Some(line),
                  Some(column))
              }.right.get
            } else {
              IO.read(new File(resources, "public/templates/" + naming(name)))
            }*/
            modificationTimeCache += (sourceFile.getAbsolutePath -> sourceFile.lastModified)

            output ++= "\n//Loading user controller %s ...  \n\n acenteracontrollers['%s'] = function() { \n\n %s }\n\n".format(template, template.replaceAll("/","_"), jsSource)


            output ++= "\n//End of loading user controller %s \n".format(template)
            val out = new File(resources, "public/templates/"+ prefix + "usercontroller_" + naming(name))
            //println(s" out is : %s".format(out))
            IO.write(out, jsSource)
            Seq(sourceFile -> out)
          }
        }

        output ++= "// end of load\n"
        IO.write(global, output.toString)

        // Minify
        val minified = play.core.jscompile.JavascriptCompiler.minify(output.toString, None)
        IO.write(globalMinified, minified)

        val allTemplates = generated ++ Seq(global -> global, globalMinified -> globalMinified)

        Sync.writeInfo(cacheFile,
          Relation.empty[java.io.File, java.io.File] ++ allTemplates,
          allFiles)(FileInfo.lastModified.format)

        allTemplates.map(_._2).distinct.toSeq
      } else {
        previousGeneratedFiles.toSeq
      }
  }




  lazy val EmberUserControllerJsOverrideCompiler = (sourceDirectory in Compile, resourceManaged in Compile, cacheDirectory, emberJsPrefix, emberUserControllerJsOverrideTemplateFile, emberUserControllerJsOverrideFileRegexFrom, emberUserControllerJsOverrideFileRegexTo, emberUserControllerJsOverrideAssetsDir, emberUserControllerJsOverrideAssetsGlob).map {
    (src, resources, cache, prefix, templateFile, fileReplaceRegexp, fileReplaceWith, assetsDir, files) =>

              val version = "1.4.0"
      val cacheFile = cache / "emberjsusercontroller_override"
      val templatesDir = resources / "public" / "templates"
      val global = templatesDir /  templateFile
      val globalMinified = templatesDir /  (FilenameUtils.removeExtension(templateFile) + ".min.js")

      def naming(name: String) = name.replaceAll(fileReplaceRegexp, fileReplaceWith)

      val latestTimestamp = files.get.sortBy(f => FileInfo.lastModified(f).lastModified).reverse.map(f => FileInfo.lastModified(f)).headOption.getOrElse(FileInfo.lastModified(global))
      val currentInfos = files.get.map(f => f -> FileInfo.lastModified(f))
      val allFiles = (currentInfos ++ Seq(global -> latestTimestamp, globalMinified -> latestTimestamp)).toMap

      val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)
      val previousGeneratedFiles = previousRelation._2s

      if (previousInfo != allFiles) {
        val output = new StringBuilder
        output ++= """

                   """

        val generated:Seq[(File, File)] = (files x relativeTo(assetsDir)).flatMap {
          case (sourceFile, name) => {
            val template = templateName(sourceFile.getPath, assetsDir.getPath)
            val jsSource = compileJsOnly(version, template, IO.read(sourceFile)).left.map {
              case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                msg,
                Some(line),
                Some(column))
            }.right.get
            /*
            val jsSource = if (modificationTimeCache.get(sourceFile.getAbsolutePath).map(time => time != sourceFile.lastModified()).getOrElse(true)) {
              compileJsOnly(version, template, IO.read(sourceFile)).left.map {
                case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                  msg,
                  Some(line),
                  Some(column))
              }.right.get
            } else {
              IO.read(new File(resources, "public/templates/" + naming(name)))
            }*/
            modificationTimeCache += (sourceFile.getAbsolutePath -> sourceFile.lastModified)

            //output ++= "\n//Loading user controller %s ...\n\n%s".format(template, jsSource)
            output ++= "\n//Loading user controller %s ...  \n\n acenteracontrollers['%s'] = function() { \n\n %s }\n\n".format(template, template.replaceAll("/","_"), jsSource)

            output ++= "\n//End of loading user controller %s \n".format(template)
            val out = new File(resources, "public/templates/" + prefix + "usercontroller_override_" + naming(name))
            //println(s" out is : %s".format(out))
            IO.write(out, jsSource)
            Seq(sourceFile -> out)
          }
        }

        output ++= "// end of load\n"
        IO.write(global, output.toString)

        // Minify
        val minified = play.core.jscompile.JavascriptCompiler.minify(output.toString, None)
        IO.write(globalMinified, minified)

        val allTemplates = generated ++ Seq(global -> global, globalMinified -> globalMinified)

        Sync.writeInfo(cacheFile,
          Relation.empty[java.io.File, java.io.File] ++ allTemplates,
          allFiles)(FileInfo.lastModified.format)

        allTemplates.map(_._2).distinct.toSeq
      } else {
        previousGeneratedFiles.toSeq
      }
  }

  lazy val EmberAdminControllerJsCompiler = (sourceDirectory in Compile, resourceManaged in Compile, cacheDirectory, emberJsPrefix, emberAdminControllerJsTemplateFile, emberAdminControllerJsFileRegexFrom, emberAdminControllerJsFileRegexTo, emberAdminControllerJsAssetsDir, emberAdminControllerJsAssetsGlob).map {
      (src, resources, cache, prefix, templateFile, fileReplaceRegexp, fileReplaceWith, assetsDir, files) =>
              val version = "1.4.0"
      val cacheFile = cache / "emberjsadmincontroller"
      val templatesDir = resources / "public" / "templates"
      val global = templatesDir /  templateFile
      val globalMinified = templatesDir /  (FilenameUtils.removeExtension(templateFile) + ".min.js")

      def naming(name: String) = name.replaceAll(fileReplaceRegexp, fileReplaceWith)

      val latestTimestamp = files.get.sortBy(f => FileInfo.lastModified(f).lastModified).reverse.map(f => FileInfo.lastModified(f)).headOption.getOrElse(FileInfo.lastModified(global))
      val currentInfos = files.get.map(f => f -> FileInfo.lastModified(f))
      val allFiles = (currentInfos ++ Seq(global -> latestTimestamp, globalMinified -> latestTimestamp)).toMap

      val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)
      val previousGeneratedFiles = previousRelation._2s

      if (previousInfo != allFiles) {
        val output = new StringBuilder
        output ++= """

                 """

        val generated:Seq[(File, File)] = (files x relativeTo(assetsDir)).flatMap {
          case (sourceFile, name) => {
            val template = templateName(sourceFile.getPath, assetsDir.getPath)
            val jsSource = compileJsOnly(version, template, IO.read(sourceFile)).left.map {
              case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                msg,
                Some(line),
                Some(column))
            }.right.get
            /*val jsSource = if (modificationTimeCache.get(sourceFile.getAbsolutePath).map(time => time != sourceFile.lastModified()).getOrElse(true)) {
              compileJsOnly(version, template, IO.read(sourceFile)).left.map {
                case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                  msg,
                  Some(line),
                  Some(column))
              }.right.get
            } else {
              IO.read(new File(resources, "public/templates/" + naming(name)))
            }*/
            modificationTimeCache += (sourceFile.getAbsolutePath -> sourceFile.lastModified)

            //output ++= "\n//Loading controller %s ...\n\n%s".format(template, jsSource)
            output ++= "\n//Loading user controller %s ...  \n\n acenteracontrollers['%s'] = function() { \n\n %s }\n\n".format(template, template.replaceAll("/","_"), jsSource)

            output ++= "\n//End of loading controller %s \n".format(template)
            val out = new File(resources, "public/templates/" + prefix + "admincontroller_" + naming(name))
            //println(s" out is : %s".format(out))
            IO.write(out, jsSource)
            Seq(sourceFile -> out)
          }
        }

        output ++= "// end of load\n"
        IO.write(global, output.toString)

        // Minify
        val minified = play.core.jscompile.JavascriptCompiler.minify(output.toString, None)
        IO.write(globalMinified, minified)

        val allTemplates = generated ++ Seq(global -> global, globalMinified -> globalMinified)

        Sync.writeInfo(cacheFile,
          Relation.empty[java.io.File, java.io.File] ++ allTemplates,
          allFiles)(FileInfo.lastModified.format)

        allTemplates.map(_._2).distinct.toSeq
      } else {
        previousGeneratedFiles.toSeq
      }
  }
  lazy val EmberAdminViewJsCompiler = (sourceDirectory in Compile, resourceManaged in Compile, cacheDirectory, emberJsPrefix, emberAdminViewJsTemplateFile, emberAdminViewJsFileRegexFrom, emberAdminViewJsFileRegexTo, emberAdminViewJsAssetsDir, emberAdminViewJsAssetsGlob).map {
      (src, resources, cache, prefix, templateFile, fileReplaceRegexp, fileReplaceWith, assetsDir, files) =>
      val version = "1.4.0"
      val cacheFile = cache / "emberjadminviews"
      val templatesDir = resources / "public" / "templates"
      val global = templatesDir /  templateFile
      val globalMinified = templatesDir /  (FilenameUtils.removeExtension(templateFile) + ".min.js")
      //println(s"EmberJsCompiler... with assets dir %s".format(emberJsAssetsDir))

      def naming(name: String) = name.replaceAll(fileReplaceRegexp, fileReplaceWith)

      val latestTimestamp = files.get.sortBy(f => FileInfo.lastModified(f).lastModified).reverse.map(f => FileInfo.lastModified(f)).headOption.getOrElse(FileInfo.lastModified(global))
      val currentInfos = files.get.map(f => f -> FileInfo.lastModified(f))
      val allFiles = (currentInfos ++ Seq(global -> latestTimestamp, globalMinified -> latestTimestamp)).toMap
      //println(s"EmberJsCompiler... aa %s".format(allFiles))

      val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)
      val previousGeneratedFiles = previousRelation._2s

      if (previousInfo != allFiles) {
        val output = new StringBuilder
        output ++= """(function() {
          var template = Ember.Handlebars.template,
              templates = Ember.TEMPLATES = Ember.TEMPLATES || {};
                 """

        val generated:Seq[(File, File)] = (files x relativeTo(assetsDir)).flatMap {
          case (sourceFile, name) => {
            val template = templateName(sourceFile.getPath, assetsDir.getPath)
            val jsSource = if (modificationTimeCache.get(sourceFile.getAbsolutePath).map(time => time != sourceFile.lastModified()).getOrElse(true)) {
              compile(version, template, IO.read(sourceFile)).left.map {
                case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                  msg,
                  Some(line),
                  Some(column))
              }.right.get
            } else {
              IO.read(new File(resources, "public/templates/" + prefix + "adminview_" + naming(name)))
            }
            modificationTimeCache += (sourceFile.getAbsolutePath -> sourceFile.lastModified)

            output ++= "\ntemplates['%s'] = template(%s);\n\n".format(template, jsSource)

            val out = new File(resources, "public/templates/" + prefix + "adminview_" + naming(name))
            IO.write(out, jsSource)
            Seq(sourceFile -> out)
          }
        }

        output ++= "})();\n"
        IO.write(global, output.toString)

        // Minify
        val minified = play.core.jscompile.JavascriptCompiler.minify(output.toString, None)
        IO.write(globalMinified, minified)

        val allTemplates = generated ++ Seq(global -> global, globalMinified -> globalMinified)

        Sync.writeInfo(cacheFile,
          Relation.empty[java.io.File, java.io.File] ++ allTemplates,
          allFiles)(FileInfo.lastModified.format)

        allTemplates.map(_._2).distinct.toSeq
      } else {
        previousGeneratedFiles.toSeq
      }
  }
  lazy val EmberUserJsCompiler = (sourceDirectory in Compile, resourceManaged in Compile, cacheDirectory, emberJsPrefix, emberUserJsTemplateFile, emberUserJsFileRegexFrom, emberUserJsFileRegexTo, emberUserJsAssetsDir, emberUserJsAssetsGlob).map {
      (src, resources, cache, prefix, templateFile, fileReplaceRegexp, fileReplaceWith, assetsDir, files) =>
              val version = "1.4.0"
      val cacheFile = cache / (prefix + "_" + "emberjuserviews")
      val templatesDir = resources / "public" / "templates"
      val global = templatesDir /  templateFile
      val globalMinified = templatesDir /  (FilenameUtils.removeExtension(templateFile) + ".min.js")
      //println(s"EmberJsCompiler... with assets dir %s".format(prefix))

      def naming(name: String) = name.replaceAll(fileReplaceRegexp, fileReplaceWith)

      val latestTimestamp = files.get.sortBy(f => FileInfo.lastModified(f).lastModified).reverse.map(f => FileInfo.lastModified(f)).headOption.getOrElse(FileInfo.lastModified(global))
      val currentInfos = files.get.map(f => f -> FileInfo.lastModified(f))
      val allFiles = (currentInfos ++ Seq(global -> latestTimestamp, globalMinified -> latestTimestamp)).toMap
      //println(s"EmberJsCompiler... aa %s".format(allFiles))

      val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)
      val previousGeneratedFiles = previousRelation._2s

      //println(s"EmberJsCompiler... previousInfo %s vs %s".format(previousInfo, allFiles))
      if (previousInfo != allFiles) {
        //println(s"EmberJsCompiler... previous vs allFiles... changed");
        val output = new StringBuilder
        output ++= """(function() {
          var template = Ember.Handlebars.template,
              templates = Ember.TEMPLATES = Ember.TEMPLATES || {};
                 """

        val generated:Seq[(File, File)] = (files x relativeTo(assetsDir)).flatMap {
          case (sourceFile, name) => {
            val template = templateName(sourceFile.getPath, assetsDir.getPath)

            val jsSource = if (modificationTimeCache.get(sourceFile.getAbsolutePath).map(time => time != sourceFile.lastModified()).getOrElse(true)) {
              compile(version, template, IO.read(sourceFile)).left.map {
                case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                  msg,
                  Some(line),
                  Some(column))
              }.right.get
            } else {
              IO.read(new File(resources, "public/templates/" + prefix + "userjs_" + naming(name)))
            }
            modificationTimeCache += (sourceFile.getAbsolutePath -> sourceFile.lastModified)


            output ++= "\ntemplates['%s'] = template(%s);\n\n".format(template.replaceFirst("base/user/views/","").replaceFirst("base/common/views/","").replaceFirst("custom/user/views/","").replaceFirst("custom/common/views/",""), jsSource)

            //println(s"EmberJsCompiler... create of file %s ".format( "public/templates/" + prefix + "userjs_" + naming(name) ));
            val out = new File(resources, "public/templates/" + prefix + "userjs_" + naming(name))
            IO.write(out, jsSource)
            Seq(sourceFile -> out)
          }
        }


        output ++= "})();\n"

        //println(s"EmberJsCompiler... write global..... %s".format(global));
        IO.write(global, output.toString)


        //val minified = play.core.jscompile.JavascriptCompiler.minify(output.toString, None)
        //IO.write(globalMinified, minified)

        //val allTemplates = generated ++ Seq(global -> global, globalMinified -> globalMinified)
        val allTemplates = generated ++ Seq(global -> global, globalMinified -> globalMinified)

        //println(s"EmberJsCompiler... writeCacheFile......");
        Sync.writeInfo(cacheFile,
          Relation.empty[java.io.File, java.io.File] ++ allTemplates,
          allFiles)(FileInfo.lastModified.format)

        //println(s"EmberJsCompiler... return allTemplates.........");
        allTemplates.map(_._2).distinct.toSeq
      } else {
        //println(s"EmberJsCompiler... return previousGenertaedFiles.........");
        previousGeneratedFiles.toSeq
      }
  }


  lazy val EmberUserJsOverrideCompiler = (sourceDirectory in Compile, resourceManaged in Compile, cacheDirectory, emberJsPrefix, emberUserJsOverrideTemplateFile, emberUserJsOverrideFileRegexFrom, emberUserJsOverrideFileRegexTo, emberUserJsOverrideAssetsDir, emberUserJsOverrideAssetsGlob).map {
    (src, resources, cache, prefix, templateFile, fileReplaceRegexp, fileReplaceWith, overrideassetsDir, files) =>
      val cacheFile = cache / "emberjuserviews_override"
val version = "1.4.0"
      val templatesDir = resources / "public" / "templates"

      val global = templatesDir /  templateFile
      val globalMinified = templatesDir /  (FilenameUtils.removeExtension(templateFile) + ".min.js")
      //println(s"EmberJsCompiler... with assets dir %s".format(emberJsAssetsDir))

      def naming(name: String) = name.replaceAll(fileReplaceRegexp, fileReplaceWith)

      val latestTimestamp = files.get.sortBy(f => FileInfo.lastModified(f).lastModified).reverse.map(f => FileInfo.lastModified(f)).headOption.getOrElse(FileInfo.lastModified(global))
      val currentInfos = files.get.map(f => f -> FileInfo.lastModified(f))
      val allFiles = (currentInfos ++ Seq(global -> latestTimestamp, globalMinified -> latestTimestamp)).toMap
      //println(s"EmberJsCompiler... aa %s".format(allFiles))

      val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)
      val previousGeneratedFiles = previousRelation._2s

      if (previousInfo != allFiles) {
        val output = new StringBuilder
        output ++= """(function() {
          var template = Ember.Handlebars.template,
              templates = Ember.TEMPLATES = Ember.TEMPLATES || {};
                   """

        val generated:Seq[(File, File)] = (files x relativeTo(overrideassetsDir)).flatMap {
          case (sourceFile, name) => {
            val template = templateName(sourceFile.getPath, overrideassetsDir.getPath)

            val jsSource = if (modificationTimeCache.get(sourceFile.getAbsolutePath).map(time => time != sourceFile.lastModified()).getOrElse(true)) {
              compile(version, template, IO.read(sourceFile)).left.map {
                case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                  msg,
                  Some(line),
                  Some(column))
              }.right.get
            } else {
              IO.read(new File(resources, "public/templates/" + prefix + "userjs_override_" + naming(name)))
            }
            modificationTimeCache += (sourceFile.getAbsolutePath -> sourceFile.lastModified)


            output ++= "\ntemplates['%s'] = template(%s);\n\n".format(template, jsSource)

            val out = new File(resources, "public/templates/"+ prefix +"userjs_override_" + naming(name))
            IO.write(out, jsSource)
            Seq(sourceFile -> out)
          }
        }


        output ++= "})();\n"
        IO.write(global, output.toString)


        //val minified = play.core.jscompile.JavascriptCompiler.minify(output.toString, None)
        //IO.write(globalMinified, minified)

        //val allTemplates = generated ++ Seq(global -> global, globalMinified -> globalMinified)
        val allTemplates = generated ++ Seq(global -> global, globalMinified -> globalMinified)

        Sync.writeInfo(cacheFile,
          Relation.empty[java.io.File, java.io.File] ++ allTemplates,
          allFiles)(FileInfo.lastModified.format)

        allTemplates.map(_._2).distinct.toSeq
      } else {
        previousGeneratedFiles.toSeq
      }
  }



  lazy val EmberModelJsCompiler = (sourceDirectory in Compile, resourceManaged in Compile, cacheDirectory, emberJsPrefix, emberModelJsTemplateFile, emberModelJsFileRegexFrom, emberModelJsFileRegexTo, emberModelJsAssetsDir, emberModelJsAssetsGlob).map {
      (src, resources, cache, prefix, templateFile, fileReplaceRegexp, fileReplaceWith, assetsDir, files) =>
              val version = "1.4.0"
      val cacheFile = cache / "emberjsmodel"
      val templatesDir = resources / "public" / "templates"
      val global = templatesDir /  templateFile
      val globalMinified = templatesDir /  (FilenameUtils.removeExtension(templateFile) + ".min.js")

      def naming(name: String) = name.replaceAll(fileReplaceRegexp, fileReplaceWith)

      val latestTimestamp = files.get.sortBy(f => FileInfo.lastModified(f).lastModified).reverse.map(f => FileInfo.lastModified(f)).headOption.getOrElse(FileInfo.lastModified(global))
      val currentInfos = files.get.map(f => f -> FileInfo.lastModified(f))
      val allFiles = (currentInfos ++ Seq(global -> latestTimestamp, globalMinified -> latestTimestamp)).toMap

      val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)
      val previousGeneratedFiles = previousRelation._2s

      if (previousInfo != allFiles) {
        val output = new StringBuilder
        output ++= """

                 """


        val generated:Seq[(File, File)] = (files x relativeTo(assetsDir)).flatMap {
          case (sourceFile, name) => {
            val template = templateName(sourceFile.getPath, assetsDir.getPath)
            val jsSource =
              compileJsOnly(version, template, IO.read(sourceFile)).left.map {
                case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                  msg,
                  Some(line),
                  Some(column))
              }.right.get
            //} else {
//              IO.read(new File(resources, "public/templates/" + naming(name)))
  //          }
            modificationTimeCache += (sourceFile.getAbsolutePath -> sourceFile.lastModified)

            //We do not want to overwrite the models


            //the modelId logic is horrible but it was the only way to make it work for now.. emberJsPrefix doesn't propagate as I though...

            output ++= "\n//Loading model %s ...  \n\n acenteramodels['%s_%s'] = function() { \n\n %s }\n\n".format(template, modelId, template.replaceAll("/","_"), jsSource)
            modelId = modelId+1;

            //Do not get too high in Id's
            if (modelId>=100000000) {
              modelId = 1;
            }

            val out = new File(resources, "public/templates/" +prefix + "commontemplates_" + naming(name))
            //println(s" out is : %s".format(out))
            IO.write(out, jsSource)
            Seq(sourceFile -> out)
          }
        }

        output ++= "// end of load\n"
        IO.write(global, output.toString)

        // Minify
        val minified = play.core.jscompile.JavascriptCompiler.minify(output.toString, None)
        IO.write(globalMinified, minified)

        val allTemplates = generated ++ Seq(global -> global, globalMinified -> globalMinified)

        Sync.writeInfo(cacheFile,
          Relation.empty[java.io.File, java.io.File] ++ allTemplates,
          allFiles)(FileInfo.lastModified.format)

        allTemplates.map(_._2).distinct.toSeq
      } else {
        previousGeneratedFiles.toSeq
      }
  }
  lazy val EmberCommonControllerJsCompiler = (sourceDirectory in Compile, resourceManaged in Compile, cacheDirectory, emberJsPrefix, emberCommonControllerJsTemplateFile, emberCommonControllerJsFileRegexFrom, emberCommonControllerJsFileRegexTo, emberCommonControllerJsAssetsDir, emberCommonControllerJsAssetsGlob).map {
      (src, resources, cache, prefix, templateFile, fileReplaceRegexp, fileReplaceWith, assetsDir, files) =>
              val version = "1.4.0"
      val cacheFile = cache / "emberjscommoncontroller"
      val templatesDir = resources / "public" / "templates"
      val global = templatesDir /  templateFile
      val globalMinified = templatesDir /  (FilenameUtils.removeExtension(templateFile) + ".min.js")

      def naming(name: String) = name.replaceAll(fileReplaceRegexp, fileReplaceWith)

      val latestTimestamp = files.get.sortBy(f => FileInfo.lastModified(f).lastModified).reverse.map(f => FileInfo.lastModified(f)).headOption.getOrElse(FileInfo.lastModified(global))
      val currentInfos = files.get.map(f => f -> FileInfo.lastModified(f))
      val allFiles = (currentInfos ++ Seq(global -> latestTimestamp, globalMinified -> latestTimestamp)).toMap

      val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)
      val previousGeneratedFiles = previousRelation._2s

      if (previousInfo != allFiles) {
        val output = new StringBuilder
        output ++= """

                 """

        val generated:Seq[(File, File)] = (files x relativeTo(assetsDir)).flatMap {
          case (sourceFile, name) => {
            val template = templateName(sourceFile.getPath, assetsDir.getPath)
            val jsSource = compileJsOnly(version, template, IO.read(sourceFile)).left.map {
              case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                msg,
                Some(line),
                Some(column))
            }.right.get
            /*val jsSource = if (modificationTimeCache.get(sourceFile.getAbsolutePath).map(time => time != sourceFile.lastModified()).getOrElse(true)) {
              compileJsOnly(version, template, IO.read(sourceFile)).left.map {
                case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                  msg,
                  Some(line),
                  Some(column))
              }.right.get
            } else {
              IO.read(new File(resources, "public/templates/" + naming(name)))
            }*/
            modificationTimeCache += (sourceFile.getAbsolutePath -> sourceFile.lastModified)

            //output ++= "\n//Loading controller %s ...\n\n%s".format(template, jsSource)
            output ++= "\n//Loading controller %s ...  \n\n acenteracontrollers['%s'] = function() { \n\n %s }\n\n".format(template, template.replaceAll("/","_"), jsSource)

            output ++= "\n//End of loading controller %s \n".format(template)
            val out = new File(resources, "public/templates/" + prefix + "commoncontroller_" + naming(name))
            //println(s" out is : %s".format(out))
            IO.write(out, jsSource)
            Seq(sourceFile -> out)
          }
        }

        output ++= "// end of load\n"
        IO.write(global, output.toString)

        // Minify
        val minified = play.core.jscompile.JavascriptCompiler.minify(output.toString, None)
        IO.write(globalMinified, minified)

        val allTemplates = generated ++ Seq(global -> global, globalMinified -> globalMinified)

        Sync.writeInfo(cacheFile,
          Relation.empty[java.io.File, java.io.File] ++ allTemplates,
          allFiles)(FileInfo.lastModified.format)

        allTemplates.map(_._2).distinct.toSeq
      } else {
        previousGeneratedFiles.toSeq
      }
  }

  lazy val EmberCommonViewJsCompiler = (sourceDirectory in Compile, resourceManaged in Compile, cacheDirectory, emberJsPrefix, emberCommonViewJsTemplateFile, emberCommonViewJsFileRegexFrom, emberCommonViewJsFileRegexTo, emberCommonViewJsAssetsDir, emberCommonViewJsAssetsGlob).map {
    (src, resources, cache, prefix, templateFile, fileReplaceRegexp, fileReplaceWith, assetsDir, files) =>
      val version = "1.4.0"
      val cacheFile = cache / "emberjcommonviews"
      val templatesDir = resources / "public" / "templates"
      val global = templatesDir /  templateFile
      val globalMinified = templatesDir /  (FilenameUtils.removeExtension(templateFile) + ".min.js")
      //println(s"EmberJsCompiler... with assets dir %s".format(emberJsAssetsDir))

      def naming(name: String) = name.replaceAll(fileReplaceRegexp, fileReplaceWith)

      val latestTimestamp = files.get.sortBy(f => FileInfo.lastModified(f).lastModified).reverse.map(f => FileInfo.lastModified(f)).headOption.getOrElse(FileInfo.lastModified(global))
      val currentInfos = files.get.map(f => f -> FileInfo.lastModified(f))
      val allFiles = (currentInfos ++ Seq(global -> latestTimestamp, globalMinified -> latestTimestamp)).toMap
      //println(s"EmberJsCompiler... aa %s".format(allFiles))

      val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)
      val previousGeneratedFiles = previousRelation._2s

      if (previousInfo != allFiles) {
        val output = new StringBuilder
        output ++= """(function() {
          var template = Ember.Handlebars.template,
              templates = Ember.TEMPLATES = Ember.TEMPLATES || {};
                   """

        val generated:Seq[(File, File)] = (files x relativeTo(assetsDir)).flatMap {
          case (sourceFile, name) => {
            val template = templateName(sourceFile.getPath, assetsDir.getPath)
            val jsSource = if (modificationTimeCache.get(sourceFile.getAbsolutePath).map(time => time != sourceFile.lastModified()).getOrElse(true)) {
              compile(version, template, IO.read(sourceFile)).left.map {
                case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                  msg,
                  Some(line),
                  Some(column))
              }.right.get
            } else {
              IO.read(new File(resources, "public/templates/" + prefix + "commonview_" + naming(name)))
            }
            modificationTimeCache += (sourceFile.getAbsolutePath -> sourceFile.lastModified)

            output ++= "\ntemplates['%s'] = template(%s);\n\n".format(template, jsSource)

            val out = new File(resources, "public/templates/" + prefix + "commonview_" + naming(name))
            IO.write(out, jsSource)
            Seq(sourceFile -> out)
          }
        }

        output ++= "})();\n"
        IO.write(global, output.toString)

        // Minify
        val minified = play.core.jscompile.JavascriptCompiler.minify(output.toString, None)
        IO.write(globalMinified, minified)

        val allTemplates = generated ++ Seq(global -> global, globalMinified -> globalMinified)

        Sync.writeInfo(cacheFile,
          Relation.empty[java.io.File, java.io.File] ++ allTemplates,
          allFiles)(FileInfo.lastModified.format)

        allTemplates.map(_._2).distinct.toSeq
      } else {
        previousGeneratedFiles.toSeq
      }
  }


}
