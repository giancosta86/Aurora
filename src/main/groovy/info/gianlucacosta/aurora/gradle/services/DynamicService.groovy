/*§
  ===========================================================================
  Aurora
  ===========================================================================
  Copyright (C) 2015-2016 Gianluca Costa
  ===========================================================================
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  ===========================================================================
*/

package info.gianlucacosta.aurora.gradle.services

import info.gianlucacosta.aurora.gradle.AuroraException
import info.gianlucacosta.aurora.gradle.AuroraPlugin
import info.gianlucacosta.aurora.gradle.settings.AuroraSettings
import info.gianlucacosta.aurora.gradle.settings.JavaVersion
import info.gianlucacosta.aurora.utils.Log
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Jar


/**
 * Invoked as soon as the "aurora {}" block closes in the build script
 */
class DynamicService {
    private final Project project
    private final AuroraSettings auroraSettings


    DynamicService(Project project) {
        this.project = project
        this.auroraSettings = project.auroraSettings
    }


    def run() {
        initHasFlags()

        checkAuroraSettings()

        checkProjectProperties()

        setupProjectProperties()

        checkArtifactInfo()

        setupArtifacts()

        setupTodo()

        setupApplicationFiles()

        setupJavaVersionCheck()

        setupJavaw()

        setupBintray()

        setupTaskDependencies()
    }


    private void initHasFlags() {
        project.ext {
            hasJava = project.getPluginManager().hasPlugin("java")

            hasScala = project.getPluginManager().hasPlugin("scala")

            hasGroovy = project.getPluginManager().hasPlugin("groovy")

            hasApplication = project.getPluginManager().hasPlugin("application")

            hasMaven = project.getPluginManager().hasPlugin("maven")

            hasBintray = project.getPluginManager().hasPlugin("com.jfrog.bintray")

            hasTodo = project.getPluginManager().hasPlugin("com.autoscout24.gradle.todo")

            hasMoonLicense = project.getPluginManager().hasPlugin("info.gianlucacosta.moonlicense")

            hasMoonDeploy = project.getPluginManager().hasPlugin("info.gianlucacosta.moondeploy")
        }
    }


    private void checkAuroraSettings() {
        if (!auroraSettings.docTask) {
            if (project.hasScala) {
                auroraSettings.docTask = "scaladoc";
            } else if (project.hasGroovy) {
                auroraSettings.docTask = "groovydoc"
            } else {
                auroraSettings.docTask = "javadoc"
            }

            Log.info("Inferred doc task: ${auroraSettings.docTask}")
        }

        if (!auroraSettings.gitHubUser) {
            throw new AuroraException("Missing gitHubUser")
        }

        if (!auroraSettings.authors) {
            throw new AuroraException("At least an author must be specified")
        }


        if (project.hasBintray && !auroraSettings.bintraySettings) {
            throw new AuroraException("Missing bintray block")
        }
    }


    private void checkProjectProperties() {
        if (!project.name) {
            throw new AuroraException("The project name is missing")
        }

        if (!project.description) {
            throw new AuroraException("The project description is missing")
        }

        if (!project.hasJava) {
            throw new AuroraException("Aurora can only be applied to projects having - implicitly or explicitly - the 'java' plugin")
        }

        if (project.hasBintray) {
            if (!project.hasMaven) {
                throw new AuroraException("The 'maven' plugin is required when employing Bintray's plugin with Aurora")
            }
        }
    }


    private void setupProjectProperties() {
        if (!project.ext.has("facebookPage")) {
            project.ext.facebookPage = null
        }

        Log.info("Facebook page: ${project.facebookPage}")

        project.ext {
            groupId = project.group.toString()

            artifactId = project.archivesBaseName.toString()

            isRelease = !project.version.toString().endsWith("-SNAPSHOT")

            url = "https://github.com/${auroraSettings.gitHubUser}/${project.name}"
        }


        if (project.hasScala) {
            project.ext.mainLanguage = "scala"
        } else if (project.hasGroovy) {
            project.ext.mainLanguage = "groovy"
        } else {
            project.ext.mainLanguage = "java"
        }

        Log.debug("Project extensions: ${project.ext.dump()}")
    }



    private void checkArtifactInfo() {
        if (!project.groupId) {
            throw new AuroraException("The group id must NOT be empty")
        }

        if (!project.artifactId) {
            throw new AuroraException("The artifact id (archivesBaseName) must NOT be empty")
        }

        char artifactInitial = project.artifactId.charAt(0)
        if (!Character.isLowerCase(artifactInitial)) {
            throw new AuroraException("The artifact id (archivesBaseName) must be lowercase")
        }

        if (!project.version) {
            throw new AuroraException("The project version must NOT be empty")
        }
    }


    private void setupArtifacts() {
        project.jar {
            from project.sourceSets.generated.output
        }


        if (!project.hasMaven) {
            Log.info("Skipping additional artifacts configuration")
            return
        }


        def docTask = auroraSettings.docTask

        project.task('sourcesJar', type: Jar, dependsOn: 'classes') {
            classifier = 'sources'
            from project.sourceSets.main.allSource
        }


        project.task('docJar', type: Jar, dependsOn: docTask) {
            classifier = 'javadoc'
            from project.tasks[docTask].destinationDir
        }


        project.artifacts {
            archives project.sourcesJar
            archives project.docJar
        }
    }


    private void setupTodo() {
        if (!project.hasTodo) {
            Log.info("Skipping todo configuration")
            return
        }

        project.todo.failIfFound = project.isRelease
    }


    private void setupApplicationFiles() {
        if (!project.hasApplication) {
            Log.info("Skipping the setup of application files")
            return
        }

        project.distributions {
            main {
                contents {
                    from("src/generated/dist")
                }
            }
        }

        project.run {
            classpath += project.sourceSets.generated.output
        }
    }


    private void setupJavaVersionCheck() {
        if (!project.hasApplication || auroraSettings.requiredJavaVersion == null) {
            Log.info("Skipping Java version check configuration")
            return
        }

        JavaVersion javaVersion = auroraSettings.requiredJavaVersion

        project.startScripts {
            windowsStartScriptGenerator.template =
                    project.resources.text.fromString(
                            'cscript //NoLogo "%~dp0CheckJavaVersion.js" ' + "${javaVersion.major} ${javaVersion.minor} ${javaVersion.build} ${javaVersion.update}\n"
                                    + 'if %errorlevel% neq 0 exit /b %errorlevel%\n\n'
                                    + windowsStartScriptGenerator.template.asString()
                    )


            unixStartScriptGenerator.template =
                    project.resources.text.fromString(
                            '#!/usr/bin/env bash\n'
                                    + 'if ! "\\$(dirname "\\$0")/CheckJavaVersion.sh" ' + "${javaVersion.major} ${javaVersion.minor} ${javaVersion.build} ${javaVersion.update}\n"
                                    + 'then\n'
                                    + '\texit 1\n'
                                    + 'fi\n\n'
                                    + unixStartScriptGenerator.template.asString()
                    )
        }
    }


    private void setupJavaw() {
        if (!project.hasApplication || auroraSettings.commandLineApp) {
            Log.info("Skipping Javaw setup")
            return
        }

        project.startScripts {
            windowsStartScriptGenerator.template =
                    project.resources.text.fromString(
                            windowsStartScriptGenerator.template.asString()
                                    .replace(
                                    "java.exe",
                                    "javaw.exe"
                            )
                                    .replace(
                                    "\"%JAVA_EXE%\" %DEFAULT_JVM_OPTS%",
                                    "start \"\" /B \"%JAVA_EXE%\" %DEFAULT_JVM_OPTS%"
                            )
                    )
        }
    }


    private void setupBintray() {
        if (!project.hasBintray) {
            Log.info("Skipping Bintray setup")
            return
        }


        setupBintrayCredentials()


        project.bintray {
            user = auroraSettings.bintraySettings.user
            key = auroraSettings.bintraySettings.key

            filesSpec {
                from "${project.buildDir}/libs"

                from("${project.buildDir}/${AuroraPlugin.MAVEN_TEMP_DIRECTORY_NAME}") {
                    include "*.pom"
                }

                into "${project.groupId.replace('.', '/')}/${project.artifactId}/${project.version}"
            }

            dryRun = false
            publish = false

            pkg {
                repo = auroraSettings.bintraySettings.repo

                name = project.name
                desc = project.description

                websiteUrl = project.ext.url
                issueTrackerUrl = "${project.ext.url}/issues"
                vcsUrl = "${project.ext.url}.git"

                licenses = auroraSettings.bintraySettings.licenses
                labels = auroraSettings.bintraySettings.labels

                publicDownloadNumbers = false

                githubRepo = "${auroraSettings.gitHubUser}/${project.name}"

                version {
                    name = project.version
                    vcsTag = "v${project.version}"

                    released = new Date()

                    gpg {
                        sign = true
                    }

                    mavenCentralSync {
                        sync = false
                    }
                }
            }
        }
    }


    private void setupBintrayCredentials() {
        String sourcePropertyFilePath = System.getenv("BINTRAY_CREDENTIALS_FILE")
        if (sourcePropertyFilePath == null) {
            Log.info("Environment variable for Bintray's credentials file not set")
            return
        }

        Properties securityProperties = new Properties()

        if (!auroraSettings.bintraySettings.user || !auroraSettings.bintraySettings.key) {
            File sourcePropertyFile = new File(sourcePropertyFilePath)
            if (sourcePropertyFile.isFile()) {
                Log.info("Bintray credentials file found at: ${sourcePropertyFile.getAbsolutePath()}. Now loading...")
                securityProperties.load(new FileInputStream(sourcePropertyFile))
            }
        }


        if (!auroraSettings.bintraySettings.user) {
            Log.info("bintrayUser recovered from Bintray's credentials file")
            auroraSettings.bintraySettings.user = securityProperties.getProperty("bintrayUser")
        }

        if (!auroraSettings.bintraySettings.key) {
            Log.info("key recovered from Bintray's credentials file")
            auroraSettings.bintraySettings.key = securityProperties.getProperty("bintrayKey")
        }
    }


    private void setupTaskDependencies() {
        project.clean.dependsOn("cleanGenerated")

        project.processGeneratedResources.dependsOn("generateMainIcons")


        if (project.hasMoonLicense) {
            project.compileGeneratedJava.dependsOn("setNotices")
            project.processGeneratedResources.dependsOn("setNotices")

            project.checkGit.dependsOn("setNotices")

            project.setNotices.dependsOn("generateArtifactInfo")
        }


        if (project.hasMaven) {
            project.install.dependsOn("check")
            project.assemble.dependsOn("generatePom")
        }


        if (project.hasScala) {
            project.scaladoc.dependsOn("setupScaladoc")
        }


        project.check.dependsOn("checkGit")


        if (project.hasTodo) {
            project.check.dependsOn("checkTodo")
        }


        if (project.hasBintray) {
            def bintrayDependencies = ["assemble", "check", "assertRelease"]

            project.bintrayUpload.dependsOn(bintrayDependencies)

            Task bintrayRecordingCopy = project.tasks.findByPath("_bintrayRecordingCopy")
            if (bintrayRecordingCopy != null) {
                Log.debug("_bintrayRecordingCopy found. Setting its dependencies as well")
                bintrayRecordingCopy.dependsOn(bintrayDependencies)
            }
        }


        if (project.hasApplication) {
            project.distZip.dependsOn("assertRelease")
            project.distZip.dependsOn("check")
            project.distZip.dependsOn("generateDistIcons")
            project.distZip.dependsOn("generateJavaVersionCheckScripts")

            project.distTar.dependsOn("assertRelease")
            project.distTar.dependsOn("check")
            project.distTar.dependsOn("generateDistIcons")
            project.distTar.dependsOn("generateJavaVersionCheckScripts")

            project.generateAppDescriptor.dependsOn("distZip")

            if (project.hasMoonDeploy && project.hasMoonLicense) {
                project.assemble.dependsOn("generateAppDescriptor")
            }
        }
    }
}
