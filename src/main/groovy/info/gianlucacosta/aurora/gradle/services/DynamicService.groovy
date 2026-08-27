package info.gianlucacosta.aurora.gradle.services

import info.gianlucacosta.aurora.gradle.AuroraException
import info.gianlucacosta.aurora.gradle.AuroraPlugin
import info.gianlucacosta.aurora.gradle.settings.AuroraSettings
import info.gianlucacosta.aurora.utils.Log
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Jar

import java.util.regex.Matcher
import java.util.regex.Pattern


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

        setupApplicationFiles()

        setupTaskDependencies()
    }


    private void initHasFlags() {
        project.ext {
            hasJava = project.getPluginManager().hasPlugin("java")

            hasScala = project.getPluginManager().hasPlugin("scala")

            hasGroovy = project.getPluginManager().hasPlugin("groovy")

            hasApplication = project.getPluginManager().hasPlugin("application")

            hasMaven = project.getPluginManager().hasPlugin("maven")
        }
    }


    private void checkAuroraSettings() {
        if (!auroraSettings.gitHubUser) {
            throw new AuroraException("Missing gitHubUser")
        }

        if (!auroraSettings.authors) {
            throw new AuroraException("At least an author must be specified")
        }

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
    }


    private void setupProjectProperties() {
        project.ext {
            groupId = project.group.toString()

            artifactId = project.archivesBaseName.toString()

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


    private void setupTaskDependencies() {
        project.clean.dependsOn("cleanGenerated")

        project.compileGeneratedJava.dependsOn("generateMainIcons")
        project.compileGeneratedJava.dependsOn("generateArtifactInfo")

        project.processGeneratedResources.dependsOn("generateMainIcons")
        project.processGeneratedResources.dependsOn("generateArtifactInfo")


        if (project.hasMaven) {
            project.install.dependsOn("check")
            project.assemble.dependsOn("generatePom")
        }


        if (project.hasScala) {
            project.scaladoc.dependsOn("setupScaladoc")
        }


        if (project.hasApplication) {
            project.distZip.dependsOn("check")
            project.distZip.dependsOn("generateDistIcons")

            project.distTar.dependsOn("check")
            project.distTar.dependsOn("generateDistIcons")

            project.generateAppDescriptor.dependsOn("distZip")
        }
    }
}
