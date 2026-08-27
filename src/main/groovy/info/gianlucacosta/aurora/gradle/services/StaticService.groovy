package info.gianlucacosta.aurora.gradle.services

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import info.gianlucacosta.aurora.gradle.AuroraException
import info.gianlucacosta.aurora.gradle.tasks.*
import info.gianlucacosta.aurora.utils.Log


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
        requireJava8()

        declareAuroraSettings()

        setupRepositories()

        setupSourceSets()

        createTasks()
    }


    private void requireJava8() {
        if (JavaVersion.current() != JavaVersion.VERSION_1_8) {
            throw new AuroraException("Java 1.8 is required to build this project!")
        }
    }


    private void declareAuroraSettings() {
        project.ext.auroraSettings = null
    }


    private void setupRepositories() {
        Log.info("Setting repositories...")

        project.repositories {
            mavenLocal()

            mavenCentral()

            maven {
                url "https://repo.repsy.io/giancosta86/hephaestus"
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
        project.tasks.create(name: "generateArtifactInfo", type: GenerateArtifactInfoTask)
        project.tasks.create(name: "generateMainIcons", type: GenerateMainIconsTask)
        project.tasks.create(name: "generateDistIcons", type: GenerateDistIconsTask)
        project.tasks.create(name: "generatePom", type: GeneratePomTask)
        project.tasks.create(name: "setupScaladoc", type: SetupScaladocTask)
    }
}
