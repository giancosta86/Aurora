/*§
  ===========================================================================
  Aurora
  ===========================================================================
  Copyright (C) 2015 Gianluca Costa
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
        checkAuroraSettings()

        checkProjectSettings()

        setupProjectProperties()

        applyPlugins()

        setupRepositories()

        setupArtifacts()

        setupUploadArchives()

        setupBintray()

        setupTasks()
    }


    private void checkAuroraSettings() {
        if (!auroraSettings.docTask) {
            throw new AuroraException("Missing docTask (eg: 'javadoc', 'groovydoc', 'scaladoc', ...)")
        }

        if (!auroraSettings.gitHubUser) {
            throw new AuroraException("Missing gitHubUser")
        }

        if (!auroraSettings.authors) {
            throw new AuroraException("At least an author must be specified")
        }

        if (!auroraSettings.bintraySettings) {
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
    }


    private void applyPlugins() {
        project.plugins.apply("maven")
        project.plugins.apply("com.jfrog.bintray")
        project.plugins.apply("info.gianlucacosta.moonlicense")
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


    private void setupBintray() {
        project.bintray {

            user = auroraSettings.bintraySettings.user
            key = auroraSettings.bintraySettings.key


            filesSpec {
                from("build/${UPLOAD_ARTIFACTS_FOLDER_NAME}") {
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


    private void setupTasks() {
        project.tasks.create(name: "checkGit", type: CheckGitTask)

        project.compileJava.dependsOn("setNotices")

        project.processResources.dependsOn("setNotices")

        project.uploadArchives.dependsOn("check")

        project._bintrayRecordingCopy.dependsOn(["checkGit", "uploadArchives"])
    }
}
