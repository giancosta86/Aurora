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
 * Service configuring the project according to Aurora's settings.
 */
class AuroraService {
    public static final String UPLOAD_ARTIFACTS_FOLDER_NAME = "mavenDeploy"


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

        setupUploadArchives()

        setupAppScripts()

        setupTasks()

        setupBintray()
    }


    private def initPluginFlags() {
        project.ext {
            hasScala = project.getPluginManager().hasPlugin("scala")
            hasGroovy = project.getPluginManager().hasPlugin("groovy")
            hasApplication = project.getPluginManager().hasPlugin("application")
            hasMaven = project.getPluginManager().hasPlugin("maven")

            hasBintray = project.getPluginManager().hasPlugin("com.jfrog.bintray")

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
    }


    private void setupProjectProperties() {
        project.ext.url = "https://github.com/${auroraSettings.gitHubUser}/${project.name}"

        if (!project.ext.has("facebookPage")) {
            project.ext.facebookPage = null
        }
    }



    private void setupRepositories() {
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
        def docTask = auroraSettings.docTask

        project.task('sourcesJar', type: Jar, dependsOn: 'classes') {
            classifier = 'sources'
            from project.sourceSets.main.allSource
        }


        project.task('javadocJar', type: Jar, dependsOn: docTask) {
            classifier = 'javadoc'
            from project.tasks[docTask].destinationDir
        }


        project.artifacts {
            archives project.sourcesJar
            archives project.javadocJar
        }
    }


    private void setupUploadArchives() {
        if (!project.hasMaven) {
            return
        }

        project.uploadArchives {
            repositories {
                mavenDeployer {
                    repository(
                            url: "file://" + new File(project.buildDir, UPLOAD_ARTIFACTS_FOLDER_NAME)
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


    private void setupAppScripts() {
        if (!project.hasApplication || auroraSettings.commandLineApp) {
            return
        }

        project.startScripts {
            def originalWindowsTemplateString = windowsStartScriptGenerator.template.asString()
            def javawTemplateString =
                    originalWindowsTemplateString
                            .replace("java.exe", "javaw.exe")
                            .replace("\"%JAVA_EXE%\" %DEFAULT_JVM_OPTS%", "start \"\" /B \"%JAVA_EXE%\" %DEFAULT_JVM_OPTS%")
            def javawTemplate = project.resources.text.fromString(javawTemplateString)
            windowsStartScriptGenerator.template = javawTemplate
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


        boolean canSetNotices = auroraSettings.release && project.hasMoonLicense

        if (canSetNotices) {
            project.compileJava.dependsOn("setNotices")
            project.processResources.dependsOn("setNotices")
        }


        if (project.hasMaven) {
            project.install.dependsOn("check")
        }

        if (project.hasBintray) {
            project.bintrayUpload.dependsOn("checkGit", "uploadArchives", "assertRelease")
        }

        if (project.hasMoonLicense) {
            project.compileJava.dependsOn("generateArtifactInfo")

            if (canSetNotices) {
                project.setNotices.dependsOn("generateArtifactInfo")
            }
        }

        project.generateAppDescriptor.dependsOn("distZip")

        if (project.hasApplication && project.hasMoonDeploy) {
            project.assemble.dependsOn("generateAppDescriptor")
        }
    }


    private void setupBintray() {
        if (!project.hasBintray) {
            return
        }


        ensureBintrayCredentials()

        project.bintray {
            user = auroraSettings.bintraySettings.user
            key = auroraSettings.bintraySettings.key


            filesSpec {
                from("build/${AuroraService.UPLOAD_ARTIFACTS_FOLDER_NAME}") {
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


    private def ensureBintrayCredentials() {
        String sourcePropertyFile = System.getenv("BINTRAY_CREDENTIALS_FILE")
        if (sourcePropertyFile == null) {
            return
        }

        Properties securityProperties = new Properties()

        if (!auroraSettings.bintraySettings.user || !auroraSettings.bintraySettings.key) {
            securityProperties.load(new FileInputStream(sourcePropertyFile))
        }


        if (!auroraSettings.bintraySettings.user) {
            auroraSettings.bintraySettings.user = securityProperties.getProperty("bintrayUser")
        }

        if (!auroraSettings.bintraySettings.key) {
            auroraSettings.bintraySettings.key = securityProperties.getProperty("bintrayKey")
        }


        if (!auroraSettings.bintraySettings.user) {
            throw new AuroraException("Missing Bintray user")
        }

        if (!auroraSettings.bintraySettings.key) {
            throw new AuroraException("Missing Bintray key")
        }


        System.out.println("(Bintray settings ready)")
    }
}
