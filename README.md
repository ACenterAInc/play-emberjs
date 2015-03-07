# Ember.js Plugin by (ACenterA)[http://www.acentera.com/]

This plugin provides build time compilation for [Ember.js](https://github.com/emberjs/ember.js) handlebar templates.

Many thanks to @Krumpi that provided a good EmberJS Plugin, unfortunately we wanted to have more flexibility than a single "javascript" file.

This work is based on the original version of https://github.com/krumpi/play-emberjs


# How to install

* play 2.2.x

```
addSbtPlugin("com.acentera" % "emberjs" % "1.0-SNAPSHOT")
```

You may need to add a reference to the Sonatype repository
```
resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
```

Or for snapshots

```
resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
```

* play 2.1.x:

```
addSbtPlugin("com.acentera" % "emberjs" % "1.0.0-SNAPSHOT")
``` 

to your plugin.sbt

# How to Use

* Select your ember version in your Build.scala. Currently supported versions include 1.4.0, 1.3.0, 1.2.0, 1.1.2, 1.0.0, 1.0.0-rc.8, 1.0.0-rc.7, 1.0.0-rc.6, 1.0.0-rc.5, 1.0.0-rc.4, 1.0.0-rc.3, 1.0.0-rc.1 and 1.0.0-pre.2

```scala
  import com.acentera.play.plugins.emberjs.EmberJsKeys
  import sbt._

  object ApplicationBuild extends Build with EmberJsKeys {

    val appName         = "play-emberjs-sample"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq.empty

    val main = play.Project(appName, appVersion, appDependencies).settings(
      emberJsVersion := "1.4.0"
    )

  }
```

* Or if you prefer using build.sbt:


the emberJsPrefix is required, that will map to the /assets/templates/$(emberJsPrefix)/{views,models,controllers}/objects.js


```
name := "<My app name>"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)

lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(PlayJava).settings(
   exportJars := false,
   emberJsPrefix:= "main",
   emberObjects := Seq( Seq("common","objects.js"),
                        Seq("user","objects.js"),
                        Seq("admin","objects.js")
   ),
   watchSources := (watchSources.value
     --- baseDirectory.value / "app/assets/templates" ** "*"
     --- baseDirectory.value / "public"     ** "*").get
).dependsOn(acentera)



play.Project.playScalaSettings
```

* Include any embers.js (except the 2.0) which is not yet supported.
```html
    <script src="@routes.Assets.at("javascripts/jquery-1.8.2.min.js")" type="text/javascript"></script>
    <script src="@routes.Assets.at("javascripts/handlebars-v1.3.0.js")" type="text/javascript"></script>
    <script src="@routes.Assets.at("javascripts/ember.min.js")" type="text/javascript"></script>
```

* Put your handlebar template (.handlebars) files under the ```app/assets/templates/views``` directory
* Put your Ember JS Controllers (.js) files under the ```app/assets/templates/controllers``` directory
* Put your Ember JS Models (.js) files under the ```app/assets/templates/models``` directory

* Reference the generated .js in a  ```<script>``` tag:
```html
	<script src="@routes.Assets.at("templates/common/main/models/objects.js")" type="text/javascript"></script>
        <script src="@routes.Assets.at("templates/common/main/views/objects.js")" type="text/javascript"></script>
        <script src="@routes.Assets.at("templates/common/main/controllers/objects.js")" type="text/javascript"></script>
```

The generated templates.pre.js has the javascript code containing all the precompiled templates in that directory

* **OR** Reference the minified .js in a  ```<script>``` tag:
```
	<script src="@routes.Assets.at("templates/common/main/models/objects.min.js")" type="text/javascript"></script>
        <script src="@routes.Assets.at("templates/common/main/views/objects.min.js")" type="text/javascript"></script>
        <script src="@routes.Assets.at("templates/common/main/controllers/objects.min.js")" type="text/javascript"></script>
```

# How to build
activator clean
activator publishLocal

# Sample

For Samples see the ACenterA Inc. github Enterprise portal demo : https://github.com/ACenterAInc/acentera-web

# Acknowledgments

This plugin was based the work from the blog post [Ember/Handlebars template precompilation with Play](http://eng.netwallet.com/2012/04/25/emberhandlebars-template-precompilation-with-play/)

A good portion of the plugin internals were based on the [Dust.js play plugin](https://github.com/typesafehub/play-plugins/tree/master/dust)

This plugin was based the work from the gitbhut play-emberjs from Krump [GitHub Original Play-EmberJs template precompilation with Play](https://github.com/krumpi/play-emberjs)


# Modifications to ember.js

Ember.js uses modern javascript features that are not properly supported by rhino 1.7R4 which is the one used with the play framework.
This has been documented in [rhino#93](https://github.com/mozilla/rhino/issues/93) and [emberjs#1202](https://github.com/emberjs/ember.js/issues/1202).
The plugin thus need a customized version of the ember-xxx.js file that can be complied by rhino.

The changes required are:

* Ember.js defines a function **ComputedPropertyPrototype.volatile**. **volatile** is a reserved keyword in rhino, so all instances are replaced by **_volatile**
* Ember.js uses sometime the name **char** as a variable name and that isn't supported in rhino either. In each case the variable is renamed to **ch**

# License

This software is licensed under the Apache 2 license, quoted below.

Copyright 2013 by AFrancis Lavalliere  

Special thanks to Carlos Quiroz to have published its emberjs work, this would not of been possible without it.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

# Changelog

* version 1.0-SNAPSHOT Initial release
