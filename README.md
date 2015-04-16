# Aurora

*Elegant Gradle projects for GitHub and Bintray*


## Introduction


Aurora is a plugin for Gradle whose purpose is to provide a small DSL enabling developers to quickly
configure a project hosted on GitHub and to be deployed to a Bintray Maven repository.

Aurora's goal is to reduce boilerplate configuration, by providing the following automatic features:

* Check project constraints (e.g.: **project.description** must be set)

* Set **project.ext.url** to the GitHub page of the project (according to **project.name**)

* Apply plugins:

  * *maven*
  * *com.jfrog.bintray*
  * *info.gianlucacosta.moonlicense*

* Setup Maven repositories, in the following order:
  1. Maven local
  2. JCenter
  3. Maven Central
  4. [Hephaestus](https://bintray.com/giancosta86/Hephaestus)

* Setup a *sources* and a *Javadoc* jar, for Maven deployment. The Javadoc task must be specified as, for example, Java, Groovy and Scala employ slightly different tasks


* Setup the **uploadArchives** task to deploy to a temporary directory: **${project.buildDir}/mavenDeploy**.
This strategy simplifies deployment to Bintray, as well as performing a deployment to *Maven local*.

* The generated POM automatically includes:
  * **name**
  * **description**
  * **url**
  * **developers**
  * **scm**


* Setup Bintray deployment, using the specified parameters (see below) and a few sensible defaults:
  * **publish** = false
  * **pkg/publicDownloadNumbers** = false
  * **pkg/version/released** = new Date()
  * **pkg/version/gpg/sign** = true
  * **pkg/version/mavenCentralSync/publish** = false

* Create task **checkGit**, ensuring that the project's directory is *clean*

* Setup tasks dependencies:
  * both **compileJava** and **processResources** depend on **setNotices** (which is provided by [MoonLicense-Gradle](https://github.com/giancosta86/MoonLicense-Gradle)).
  * **uploadArchives** depends on **check**. The task is configured so as to deploy its artifact to **${project.buildDir}/mavenDeploy**
  * **_bintrayRecordingCopy** depends on **checkGit** and **uploadArchives**

* DSL-based configuration
  

## Installation

First of all, it's mandatory to add, at the very beginning of your build script:

```
buildscript {
    repositories {
        jcenter()
        
        maven {
            url 'https://dl.bintray.com/giancosta86/Hephaestus'
        }
    }

    dependencies {
        classpath 'com.jfrog.bintray.gradle:gradle-bintray-plugin:1.1'
        classpath 'info.gianlucacosta.moonlicense:moonlicense-gradle:3.0'
        classpath 'info.gianlucacosta.aurora:aurora:1.1'
    }
}
```

Please, note that [MoonLicense-Gradle](https://github.com/giancosta86/MoonLicense-Gradle) is required as well.

In addition to this, you might also want to declare a more recent version of every dependency mentioned above.


Applying the plugin is standard:

```
apply plugin: 'info.gianlucacosta.aurora'
```


## Usage

Aurora will configure your project when reaching the following block:

```
aurora {
    docTask = "javadoc" //or "groovydoc", or "scaladoc", ...
    gitHubUser  = "<GitHub user id>"

    author {
        name = "<Author name as shown in Maven's POM>"
        email = "<Author's e-mail address>"
        url = "<Author's url - OPTIONAL>
    }

    bintray {
        user = "<Bintray API user id>"
        key = "<Bintray API key>"

        repo = "<Target repository on Bintray>"
        
        licenses = ['Apache-2.0'] //The strings should follow Bintray's conventions
        labels = ["testLabel1", "testLabel2"] //Tags following Bintray's conventions
    }
}
```

where:

* **docTask**: the task providing the files for the Javadoc archive. Usual values will be:

  * **javadoc** for Java
  * **groovydoc** for Groovy
  * **scaladoc** for Scala

* **gitHubUser**: GitHub's user hosting the project.
It is assumed that the repository on GitHub is named like the project (more precisely, its name is **project.name**)

* **author** can be used one or more times to add authors to the project. Each **author** block requires:

  * **name**: the author's name
  * **email**: the author's e-mail address

* **bintray**: settings passed to the *bintray* block provided by the Bintray plugin. In particular:

  * **user**: the authentication user id
  * **key**: the authentication key
  * **repo**: the repository name
  * **licenses**: a list of license strings, complying with Bintray's format
  * **labels**: the label tags to be shown on Bintray
