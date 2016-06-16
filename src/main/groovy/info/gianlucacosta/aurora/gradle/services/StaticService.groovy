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

import info.gianlucacosta.aurora.gradle.tasks.AssertReleaseTask
import info.gianlucacosta.aurora.gradle.tasks.CheckGitTask
import info.gianlucacosta.aurora.gradle.tasks.CheckDependenciesTask
import info.gianlucacosta.aurora.gradle.tasks.CleanGeneratedTask
import info.gianlucacosta.aurora.gradle.tasks.SetupScaladocTask
import info.gianlucacosta.aurora.gradle.tasks.GenerateAppDescriptorTask
import info.gianlucacosta.aurora.gradle.tasks.GenerateArtifactInfoTask
import info.gianlucacosta.aurora.gradle.tasks.GenerateDistIconsTask
import info.gianlucacosta.aurora.gradle.tasks.GenerateJavaVersionCheckScriptsTask
import info.gianlucacosta.aurora.gradle.tasks.GenerateMainIconsTask
import info.gianlucacosta.aurora.gradle.tasks.GeneratePomTask
import info.gianlucacosta.aurora.utils.Log
import org.gradle.api.Project

/**
 * Invoked as soon as the plugin is applied - therefore, it contains
 * configuration-independent activities
 */
class StaticService {
    private final Project project

    StaticService(Project project) {
        this.project = project
    }


    def run() {
        declareAuroraSettings()

        setupRepositories()

        setupSourceSets()

        createTasks()
    }


    private void declareAuroraSettings() {
        project.ext.auroraSettings = null
    }


    private void setupRepositories() {
        Log.info("Setting repositories...")

        project.repositories {
            mavenLocal()

            jcenter()

            mavenCentral()

            maven {
                url "https://dl.bintray.com/giancosta86/Hephaestus"
            }
        }
    }


    private void setupSourceSets() {
        project.plugins.apply("java")

        Log.debug("Defining the source sets...")


        project.sourceSets {
            generated

            main {
                compileClasspath += project.sourceSets.generated.output
                runtimeClasspath += project.sourceSets.generated.output
            }

            test {
                compileClasspath += project.sourceSets.generated.output
                runtimeClasspath += project.sourceSets.generated.output
            }
        }
    }


    private void createTasks() {
        Log.debug("Creating tasks...")

        project.tasks.create(name: "cleanGenerated", type: CleanGeneratedTask)
        project.tasks.create(name: 'assertRelease', type: AssertReleaseTask)
        project.tasks.create(name: "checkGit", type: CheckGitTask)
        project.tasks.create(name: "checkDependencies", type: CheckDependenciesTask)
        project.tasks.create(name: "generateArtifactInfo", type: GenerateArtifactInfoTask)
        project.tasks.create(name: "generateAppDescriptor", type: GenerateAppDescriptorTask)
        project.tasks.create(name: "generateMainIcons", type: GenerateMainIconsTask)
        project.tasks.create(name: "generateDistIcons", type: GenerateDistIconsTask)
        project.tasks.create(name: "generatePom", type: GeneratePomTask)
        project.tasks.create(name: "generateJavaVersionCheckScripts", type: GenerateJavaVersionCheckScriptsTask)
        project.tasks.create(name: "setupScaladoc", type: SetupScaladocTask)
    }
}
