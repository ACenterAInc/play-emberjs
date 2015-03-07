package com.acentera.play.plugins.emberjs

import java.io._
import com.acentera.play.plugins.emberjs.EmberJsPlugin._
import org.apache.commons.io.FilenameUtils
import play.JvmLogger
import sbt.Keys._

import sbt._
import play.PlayExceptions.AssetCompilationException


trait EmberJsTasks extends EmberJsKeys {
  val logger = new JvmLogger(Some("play-emberjs"))
  val modificationTimeCache = scala.collection.mutable.Map.empty[String, Long] // Keeps track of the modification time
  var modelId : Long = 0;
  val versions = Map(
    "1.4.0"       -> (("ember-1.4.0.for-rhino", "handlebars-v1.3.0", "headless-ember-1.1.2", "acentera"))
  )

  private def loadResource(name: String): Option[Reader] = {
    Option(this.getClass.getClassLoader.getResource(name)).map(_.openConnection().getInputStream).map(s => new InputStreamReader(s))
  }

  def compileJsOnly(version:String, name: String, source: String): Either[(String, Int, Int), String] = {
    println(s"Compile JS $name")

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
    ////println(s"Compile handlebars template: $name with ember version $version")

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


  protected def templateName(sourceFile: String, assetsDir: String): String = {
    val sourceFileWithForwardSlashes = FilenameUtils.separatorsToUnix(sourceFile)
    val assetsDirWithForwardSlashes  = FilenameUtils.separatorsToUnix(assetsDir)
    FilenameUtils.removeExtension(
      sourceFileWithForwardSlashes.replace(assetsDirWithForwardSlashes + "/", "")
    )
  }


  def compile(version:String, name: String, source: String): Either[(String, Int, Int), String] = {
    println(s"Compile JS/Handlebars $name")
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

  import Keys._

  lazy val EmberJsViewCompiler = (baseDirectory in Compile, resourceManaged in Compile, cacheDirectory, emberJsPrefix, emberObjects, emberJsAssetsDir).map {
    (src, resources, cache, prefix, eb, assetDir) =>


      val version = "1.4.0"
      def naming(name: String) = s"${FilenameUtils.removeExtension(name)}.handlebars_js"

      var previousGeneratedFiles : scala.collection.Set[java.io.File] = Set()
      val v = eb.map { f =>

            val files =  (assetDir / "base" / f(0) / "views"  ** "*.handlebars")
            val templateFile = f(1)
            val templatesDir = src / "public" / "generated" / prefix / "templates" / f(0)
            val cacheFile : File = cache / "emberjs_views" / templateFile / "_handlebars"


            val global = src / "public" / "templates" / f(0) / prefix / "views" / templateFile
            val globalMinified = templatesDir / s"${FilenameUtils.removeExtension(templateFile)}.min.js"

            val latestTimestamp = files.get.sortBy(f => FileInfo.lastModified(f).lastModified).reverse.map(f => FileInfo.lastModified(f)).headOption.getOrElse(FileInfo.lastModified(global))
            val currentInfos = files.get.map(f => f -> FileInfo.lastModified(f))
            val allFiles = (currentInfos ++ Seq(global -> latestTimestamp, globalMinified -> latestTimestamp)).toMap

            val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)


            ////println(s"EmberJsCompiler... previousInfo %s vs %s".format(previousInfo, allFiles))
            if (previousInfo != allFiles) {
              //println(s"EmberJsCompiler... previous vs allFiles... changed");
              val output = new StringBuilder
              output ++= """(function() {
              var template = Ember.Handlebars.template,
                  templates = Ember.TEMPLATES = Ember.TEMPLATES || {};
                         """

              var hasNewItems = 0;
              val generated:Seq[(File, File)] = (files x relativeTo(assetDir)).flatMap {
                case (sourceFile, name) => {
                  var hasNewItemsTmp = 0;
                  //println("TEST")
                  //println("TET AA %s".format(prefix + "_" + sourceFile.getAbsolutePath))
                  val template = templateName(sourceFile.getPath, assetDir.getPath)

                  val jsSource = if (modificationTimeCache.get(prefix + "_" + sourceFile.getAbsolutePath).map(time => time != sourceFile.lastModified()).getOrElse(true)) {

                    val ff =new File(src, "public/templates/generated/" + prefix + f(0) + "_" + naming(name))
                    if ((ff != null) && (sourceFile.lastModified() > ff.lastModified())) {
                      hasNewItems = 1;
                      hasNewItemsTmp = 1;
                      compile(version, template, IO.read(sourceFile)).left.map {
                        case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                          msg,
                          Some(line),
                          Some(column))
                      }.right.get
                    } else {
                      IO.read(new File(src, "public/templates/generated/" + prefix + f(0) + "_" + naming(name)))
                    }
                  } else {
                    IO.read(new File(src, "public/templates/generated/" + prefix + f(0) + "_" + naming(name)))
                  }
                  modificationTimeCache += (prefix + "_" + sourceFile.getAbsolutePath -> sourceFile.lastModified)


                  output ++= "\ntemplates['%s'] = template(%s);\n\n".format(
                    template.replaceFirst("base/user/views/", "")
                      .replaceFirst("base/common/views/", "")
                      .replaceFirst("custom/user/views/", "")
                      .replaceFirst("custom/common/views/", ""),
                    jsSource)

                  //println(s"EmberJsCompiler... create of file %s ".format("public/templates/" + prefix + f(0) + "_" + naming(name)));
                  val out = new File(src, "public/templates/generated/" + prefix + f(0) + "_" + naming(name))
                  if (hasNewItemsTmp  > 0) {
                    //println(s"EmberJsCompiler... outfile is %s ".format("public/templates/" + prefix + f(0) + "_" + naming(name)))
                    IO.write(out, jsSource)
                  }
                  Seq(sourceFile -> out)
                }
              }


              output ++= "})();\n"

              if (hasNewItems > 0 ) {
                //println(s"EmberJsCompiler... write global..... %s".format(global));
                IO.write(global, output.toString)
              } else {
                //println(s"EmberJsCompiler... DID NOT write global..... %s".format(global));
              }


              //val minified = play.core.jscompile.JavascriptCompiler.minify(output.toString, None)
              //IO.write(globalMinified, minified)

              val allTemplates = Seq(global -> global, globalMinified -> globalMinified)
              if (hasNewItems > 0 ) {
                //val allTemplates = generated ++ Seq(global -> global, globalMinified -> globalMinified)

                ////println(s"EmberJsCompiler... writeCacheFile......");
                Sync.writeInfo(cacheFile,
                  Relation.empty[java.io.File, java.io.File] ++ allTemplates ++ generated,
                  allFiles)(FileInfo.lastModified.format)
              }
              ////println(s"EmberJsCompiler... return allTemplates.........");
              //previousGeneratedFiles  ++= allTemplates.map(_._2).distinct.toSeq

            } else {

              val allTemplates = Seq(global -> global, globalMinified -> globalMinified)
              //println(s"EmberJsCompiler... return previousGenertaedFiles UNCHANGES......... TEST %s".format(allTemplates.map(_._2).distinct.toSeq));
              //previousGeneratedFiles  ++= allTemplates.map(_._2).distinct.toSeq

              //previousGeneratedFiles  ++= previousRelation._2s.toSeq
            }



      }

      //println(s"EmberJsCompiler... COMPLETED RETURN ALL OF %s".format(previousGeneratedFiles.toSeq));
      previousGeneratedFiles.toSeq
  }



  lazy val EmberJsControllerCompiler = (baseDirectory in Compile, resourceManaged in Compile, cacheDirectory, emberJsPrefix, emberObjects, emberJsAssetsDir).map {
    (src, resources, cache, prefix, eb, assetDir) =>


      val version = "1.4.0"
      def naming(name: String) = s"${FilenameUtils.removeExtension(name)}.handlebars_js"

      var previousGeneratedFiles : scala.collection.Set[java.io.File] = Set()
      val v = eb.map { f =>

        val files =  (assetDir / "base" / f(0) / "controllers"  ** "*.js")
        val templateFile = f(1)
        val templatesDir = src / "public" / "generated" / prefix / "templates" / f(0)
        val cacheFile : File = cache / "emberjs_controllers" / templateFile / "_js"


        val global = src / "public" / "templates" / f(0) / prefix / "controllers" / templateFile
        val globalMinified = templatesDir / s"${FilenameUtils.removeExtension(templateFile)}.min.js"

        val latestTimestamp = files.get.sortBy(f => FileInfo.lastModified(f).lastModified).reverse.map(f => FileInfo.lastModified(f)).headOption.getOrElse(FileInfo.lastModified(global))
        val currentInfos = files.get.map(f => f -> FileInfo.lastModified(f))
        val allFiles = (currentInfos ++ Seq(global -> latestTimestamp, globalMinified -> latestTimestamp)).toMap

        val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)


        ////println(s"EmberJsCompiler... previousInfo %s vs %s".format(previousInfo, allFiles))
        if (previousInfo != allFiles) {
          //println(s"EmberJsCompiler... previous vs allFiles... changed");
          val output = new StringBuilder
          output ++= """
                     """


          var hasNewItems = 0;
          val generated:Seq[(File, File)] = (files x relativeTo(assetDir)).flatMap {
            case (sourceFile, name) => {
              var hasNewItemsTmp = 0;
              //println("TEST")
              //println("TET AA %s".format(prefix + "_" + sourceFile.getAbsolutePath))
              val template = templateName(sourceFile.getPath, assetDir.getPath)

              val jsSource = if (modificationTimeCache.get(prefix + "_" + sourceFile.getAbsolutePath).map(time => time != sourceFile.lastModified()).getOrElse(true)) {

                val ff =new File(src, "public/templates/generated/" + prefix + f(0) + "controller_" + naming(name))
                if ((ff != null) && (sourceFile.lastModified() > ff.lastModified())) {
                  hasNewItems = 1;
                  hasNewItemsTmp = 1;
                  val jsSource = compileJsOnly(version, template, IO.read(sourceFile)).left.map {
                    case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                      msg,
                      Some(line),
                      Some(column))
                  }.right.get
                  jsSource
                } else {
                  IO.read(new File(src, "public/templates/generated/" + prefix + f(0) + "controller_" + naming(name)))
                }
              } else {
                IO.read(new File(src, "public/templates/generated/" + prefix + f(0) + "controller_" + naming(name)))
              }
              modificationTimeCache += (prefix + "_" + sourceFile.getAbsolutePath -> sourceFile.lastModified)


              output ++= "\n//Loading user controller %s ...  \n\n acenteracontrollers['%s'] = function() { \n\n %s }\n\n".format(template, template.replaceAll("/","_"), jsSource)

              output ++= "\n//End of loading controller %s \n".format(template)



              val out = new File(src, "public/templates/generated/" + prefix + f(0) + "controller_" + naming(name))
              if (hasNewItemsTmp  > 0) {
                //println(s"EmberJsCompiler... create of file %s ".format("public/templates/" + prefix + f(0) + "controller_" + naming(name)));
                IO.write(out, jsSource)
              }
              Seq(sourceFile -> out)
            }
          }


          output ++= "// end of load. Framework by http://www.acentera.com/\n"

          if (hasNewItems > 0 ) {
            //println(s"EmberJsCompiler... write global..... %s".format(global));
            IO.write(global, output.toString)
          } else {
            //println(s"EmberJsCompiler... DID NOT write global..... %s".format(global));
          }


          //val minified = play.core.jscompile.JavascriptCompiler.minify(output.toString, None)
          //IO.write(globalMinified, minified)

          val allTemplates = Seq(global -> global, globalMinified -> globalMinified)
          if (hasNewItems > 0 ) {
            //val allTemplates = generated ++ Seq(global -> global, globalMinified -> globalMinified)

            ////println(s"EmberJsCompiler... writeCacheFile......");
            Sync.writeInfo(cacheFile,
              Relation.empty[java.io.File, java.io.File] ++ allTemplates ++ generated,
              allFiles)(FileInfo.lastModified.format)
          }
          ////println(s"EmberJsCompiler... return allTemplates.........");
          //previousGeneratedFiles  ++= allTemplates.map(_._2).distinct.toSeq

        } else {

          val allTemplates = Seq(global -> global, globalMinified -> globalMinified)
          //println(s"EmberJsCompiler... return previousGenertaedFiles UNCHANGES......... TEST %s".format(allTemplates.map(_._2).distinct.toSeq));
          //previousGeneratedFiles  ++= allTemplates.map(_._2).distinct.toSeq

          //previousGeneratedFiles  ++= previousRelation._2s.toSeq
        }



      }

      //println(s"EmberJsCompiler... COMPLETED RETURN ALL OF %s".format(previousGeneratedFiles.toSeq));
      previousGeneratedFiles.toSeq
  }






  lazy val EmberJsModelControllerCompiler = (baseDirectory in Compile, resourceManaged in Compile, cacheDirectory, emberJsPrefix, emberObjects, emberJsAssetsDir).map {
    (src, resources, cache, prefix, eb, assetDir) =>


      val version = "1.4.0"
      def naming(name: String) = s"${FilenameUtils.removeExtension(name)}.handlebars_js"

      var previousGeneratedFiles : scala.collection.Set[java.io.File] = Set()
      val v = eb.map { f =>

        //println("EMBER MODEL WILL PROCESS %s / models *.js.. ".format(f(0)))
        val files =  (assetDir / "base" / f(0) / "models"  ** "*.js")
        val templateFile = f(1)
        val templatesDir = src / "public" / "generated" / prefix / "templates" / f(0)
        val cacheFile : File = cache / "emberjsmodels_" / templateFile / "_models_js"


        val global = src / "public" / "templates" / f(0) / prefix / "models" / templateFile
        val globalMinified = templatesDir / s"${FilenameUtils.removeExtension(templateFile)}.min.js"

        val latestTimestamp = files.get.sortBy(f => FileInfo.lastModified(f).lastModified).reverse.map(f => FileInfo.lastModified(f)).headOption.getOrElse(FileInfo.lastModified(global))
        val currentInfos = files.get.map(f => f -> FileInfo.lastModified(f))
        val allFiles = (currentInfos ++ Seq(global -> latestTimestamp, globalMinified -> latestTimestamp)).toMap

        val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)


        ////println(s"EmberJsCompiler... previousInfo %s vs %s".format(previousInfo, allFiles))
        if (previousInfo != allFiles) {
          //println(s"EmberJsCompiler... previous vs allFiles... changed");
          val output = new StringBuilder
          output ++= """
                     """


          var hasNewItems = 0;
          val generated:Seq[(File, File)] = (files x relativeTo(assetDir)).flatMap {
            case (sourceFile, name) => {
              var hasNewItemsTmp = 0;
              //println("TEST")
              //println("TET AA %s".format(prefix + "_" + sourceFile.getAbsolutePath))
              val template = templateName(sourceFile.getPath, assetDir.getPath)

              val jsSource = if (modificationTimeCache.get(prefix + "_" + sourceFile.getAbsolutePath).map(time => time != sourceFile.lastModified()).getOrElse(true)) {
                val ff =new File(src, "public/templates/generated/" + prefix + f(0) + "models_" + naming(name))
                if ((ff != null) && (sourceFile.lastModified() > ff.lastModified())) {
                  hasNewItems = 1;
                  hasNewItemsTmp = 1;
                  val jsSource = compileJsOnly(version, template, IO.read(sourceFile)).left.map {
                    case (msg, line, column) => throw AssetCompilationException(Some(sourceFile),
                      msg,
                      Some(line),
                      Some(column))
                  }.right.get
                  jsSource
                } else {
                  IO.read(new File(src, "public/templates/generated/" + prefix + f(0) + "models_" + naming(name)))
                }
              } else {
                IO.read(new File(src, "public/templates/generated/" + prefix + f(0) + "models_" + naming(name)))
              }
              modificationTimeCache += (prefix + "_" + sourceFile.getAbsolutePath -> sourceFile.lastModified)


              output ++= "\n//Loading model %s ...  \n\n acenteramodels['%s_%s_%s'] = function() { \n\n %s }\n\n".format(template, prefix, modelId, (prefix + f(0) + "models_" + naming(name)).replaceAll("/","_"), jsSource)
              modelId = modelId+1;

              //Do not get too high in Id's
              if (modelId>=100000000) {
                modelId = 1;
              }



              val out = new File(src, "public/templates/generated/" + prefix + f(0) + "models_" + naming(name))
              if (hasNewItemsTmp  > 0) {
                //println(s"EmberJsCompiler... create of file %s ".format("public/templates/" + prefix + f(0) + "models_" + naming(name)));
                IO.write(out, jsSource)
              }
              Seq(sourceFile -> out)
            }
          }


          output ++= "\n// Built using www.acentera.com framework"

          if (hasNewItems > 0 ) {
            //println(s"EmberJsCompiler... write global..... %s".format(global));
            IO.write(global, output.toString)
          } else {
            //println(s"EmberJsCompiler... DID NOT write global..... %s".format(global));
          }


          //val minified = play.core.jscompile.JavascriptCompiler.minify(output.toString, None)
          //IO.write(globalMinified, minified)

          val allTemplates = Seq(global -> global, globalMinified -> globalMinified)
          if (hasNewItems > 0 ) {
            //val allTemplates = generated ++ Seq(global -> global, globalMinified -> globalMinified)

            //println(s"EmberJsCompiler... writeCacheFile......");
            Sync.writeInfo(cacheFile,
              Relation.empty[java.io.File, java.io.File] ++ allTemplates ++ generated,
              allFiles)(FileInfo.lastModified.format)
          }
          ////println(s"EmberJsCompiler... return allTemplates.........");
          //previousGeneratedFiles  ++= allTemplates.map(_._2).distinct.toSeq

        } else {

          val allTemplates = Seq(global -> global, globalMinified -> globalMinified)
          //println(s"EmberJsCompiler... return previousGenertaedFiles UNCHANGES......... TEST %s".format(allTemplates.map(_._2).distinct.toSeq));
          //previousGeneratedFiles  ++= allTemplates.map(_._2).distinct.toSeq

          //previousGeneratedFiles  ++= previousRelation._2s.toSeq
        }



      }

      //println(s"EmberJsCompiler... COMPLETED RETURN ALL OF %s".format(previousGeneratedFiles.toSeq));
      previousGeneratedFiles.toSeq
  }




}
