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

package info.gianlucacosta.aurora.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

/**
 * Service configuring the project according to Aurora's settings
 */
class AuroraService {
    private static final String MAVEN_TEMP_DIRECTORY = "mavenTemp"


    private final Project project
    private final AuroraSettings auroraSettings


    public AuroraService(Project project, AuroraSettings auroraSettings) {
        this.project = project
        this.auroraSettings = auroraSettings
    }


    public void run() {
        initPluginFlags()

        checkAuroraSettings()

        checkProjectSettings()

        setupProjectProperties()

        setupRepositories()

        setupArtifacts()

        setupMavenArtifacts()

        setupBintray()

        setupTodo()

        setupJavaVersionCheck()

        setupGuiJava()

        setupTasks()
    }


    private void initPluginFlags() {
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

            println("Inferred doc task: " + auroraSettings.docTask)
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


    private void checkProjectSettings() {
        if (!project.description) {
            throw new AuroraException("The project description is missing")
        }

        if (!project.hasJava) {
            throw new AuroraException("Aurora can only be applied to projects having the 'java' plugin")
        }

        if (project.hasBintray) {
            if (!project.hasMaven) {
                throw new AuroraException("The 'maven' plugin is required when employing Bintray's plugin with Aurora")
            }
        }
    }


    private void setupProjectProperties() {
        project.ext.url = "https://github.com/${auroraSettings.gitHubUser}/${project.name}"

        if (!project.ext.has("facebookPage")) {
            project.ext.facebookPage = null
        }
    }


    private void setupRepositories() {
        if (!auroraSettings.addDefaultRepositories) {
            return
        }

        project.repositories {
            mavenLocal()

            jcenter()

            mavenCentral()

            maven {
                url "https://dl.bintray.com/giancosta86/Hephaestus"
            }
        }
    }


    private void setupArtifacts() {
        if (!project.hasMaven) {
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


    private void setupMavenArtifacts() {
        if (!project.hasMaven) {
            return
        }

        project.uploadArchives {
            repositories {
                mavenDeployer {
                    repository(
                            url: "file://" + new File(project.buildDir, MAVEN_TEMP_DIRECTORY)
                    )

                    pom.project {
                        name project.name
                        description project.description

                        url project.ext.url


                        developers {
                            for (Author author : auroraSettings.authors) {
                                developer {
                                    name author.name
                                    email author.email

                                    if (author.url) {
                                        url author.url
                                    }
                                }
                            }
                        }

                        scm {
                            url project.ext.url
                            connection "scm:git:git://github.com/${auroraSettings.gitHubUser}/${project.name}.git"
                            developerConnection "scm:git:git@github.com:${auroraSettings.gitHubUser}/${project.name}.git"
                        }

                    }
                }
            }
        }
    }


    private void setupBintray() {
        if (!project.hasBintray) {
            return
        }


        setupBintrayCredentials()

        project.bintray {
            user = auroraSettings.bintraySettings.user
            key = auroraSettings.bintraySettings.key


            filesSpec {
                from("build/${MAVEN_TEMP_DIRECTORY}") {
                    include "**/${project.version}/*.jar"
                    include "**/${project.version}/*.pom"
                }
                into '.'
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
            return
        }

        Properties securityProperties = new Properties()

        if (!auroraSettings.bintraySettings.user || !auroraSettings.bintraySettings.key) {
            File sourcePropertyFile = new File(sourcePropertyFilePath)
            if (sourcePropertyFile.isFile()) {
                securityProperties.load(new FileInputStream(sourcePropertyFile))
            }
        }


        if (!auroraSettings.bintraySettings.user) {
            auroraSettings.bintraySettings.user = securityProperties.getProperty("bintrayUser")
        }

        if (!auroraSettings.bintraySettings.key) {
            auroraSettings.bintraySettings.key = securityProperties.getProperty("bintrayKey")
        }
    }


    private void setupTodo() {
        if (!project.hasTodo) {
            return
        }

        project.todo.failIfFound = auroraSettings.release
    }


    private void setupJavaVersionCheck() {
        if (!project.hasApplication || auroraSettings.requiredJavaVersion == null) {
            return
        }

        JavaVersion javaVersion = auroraSettings.requiredJavaVersion

        project.tasks.create(name: 'createJavaVersionCheckScripts') {
            File scriptsTempDirectory = project.file("${project.buildDir}/checkJava")
            outputs.dir scriptsTempDirectory
            doLast {
                scriptsTempDirectory.mkdirs()

                [
                        "CheckJavaVersion.sh",
                        "CheckJavaVersion.js"

                ].forEach({ scriptName ->
                    String scriptContent = this.getClass().getResource(scriptName).text
                    File scriptFile = new File(scriptsTempDirectory, scriptName)
                    scriptFile.text = scriptContent

                    if (scriptName.endsWith(".sh")) {
                        scriptFile.setExecutable(true)
                    }
                })
            }
        }

        project.distributions {
            main {
                contents {
                    from(project.createJavaVersionCheckScripts) {
                        into "bin"
                    }
                }
            }
        }

        project.startScripts {
            windowsStartScriptGenerator.template =
                    project.resources.text.fromString(
                            "cscript //NoLogo %~dp0CheckJavaVersion.js ${javaVersion.major} ${javaVersion.minor} ${javaVersion.build} ${javaVersion.update}\n"
                                    + "if %errorlevel% neq 0 exit /b %errorlevel%\n\n"
                                    + windowsStartScriptGenerator.template.asString()
                    )


            unixStartScriptGenerator.template =
                    project.resources.text.fromString(
                            "#!/usr/bin/env bash\n"
                            + "if ! \$(dirname \"\$0\")/CheckJavaVersion.sh ${javaVersion.major} ${javaVersion.minor} ${javaVersion.build} ${javaVersion.update}\n"
                            + "then\n"
                            + "\texit 1\n"
                            + "fi\n\n"
                            + unixStartScriptGenerator.template.asString()
                    )
        }
    }


    private void setupGuiJava() {
        if (!project.hasApplication || auroraSettings.commandLineApp) {
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


    private void setupTasks() {
        project.tasks.create(name: "checkGit", type: CheckGitTask)
        project.tasks.create(name: "generateArtifactInfo", type: GenerateArtifactInfoTask)
        project.tasks.create(name: "generateAppDescriptor", type: GenerateAppDescriptorTask)


        project.tasks.create(name: 'assertRelease') << {
            if (!auroraSettings.release) {
                throw new AuroraException("The requested task requires release = true")
            }
        }


        if (project.hasMoonLicense) {
            project.compileJava.dependsOn("setNotices")
            project.processResources.dependsOn("setNotices")

            project.checkGit.dependsOn("setNotices")

            project.setNotices.dependsOn("generateArtifactInfo")
        }


        project.uploadArchives.dependsOn("check")


        if (project.hasMaven) {
            project.install.dependsOn("check")
        }


        if (auroraSettings.release) {
            project.check.dependsOn("checkGit")
        }


        if (project.hasTodo) {
            project.check.dependsOn("checkTodo")
        }


        if (project.hasBintray) {
            project._bintrayRecordingCopy.dependsOn("uploadArchives", "assertRelease")
        }


        if (project.hasApplication) {
            project.distZip.dependsOn("assertRelease")
            project.distZip.dependsOn("check")

            project.distTar.dependsOn("assertRelease")
            project.distTar.dependsOn("check")

            project.generateAppDescriptor.dependsOn("distZip")

            if (project.hasMoonDeploy && project.hasMoonLicense) {
                project.assemble.dependsOn("generateAppDescriptor")
            }
        }
    }
}
