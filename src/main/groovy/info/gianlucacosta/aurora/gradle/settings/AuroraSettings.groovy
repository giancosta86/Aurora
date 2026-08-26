package info.gianlucacosta.aurora.gradle.settings

import info.gianlucacosta.aurora.gradle.AuroraException
import info.gianlucacosta.aurora.utils.Log

/**
 * Settings underlying the <b>aurora</b> block.
 */
class AuroraSettings {
    String gitHubUser

    String docTask

    List<Author> authors = new ArrayList<>()

    BintraySettings bintraySettings

    boolean commandLineApp = false

    JavaVersion requiredJavaVersion

    RunArguments runArguments

    boolean customStartupScripts = true


    def author(Closure closure) {
        Author author = new Author()

        closure.delegate = author
        closure.resolveStrategy = Closure.DELEGATE_FIRST

        closure()

        Log.debug("Author: ${author.dump()}")

        if (!author.name) {
            throw new AuroraException("Missing author name")
        }


        if (!author.email) {
            throw new AuroraException("Missing author email")
        }


        authors << author
    }


    def bintray(Closure closure) {
        BintraySettings bintraySettings = new BintraySettings()

        closure.delegate = bintraySettings
        closure.resolveStrategy = Closure.DELEGATE_FIRST

        closure()

        Log.debug("Bintray settings: ${bintraySettings.dump()}")


        if (!bintraySettings.repo) {
            throw new AuroraException("Missing Bintray repo")
        }

        if (!bintraySettings.licenses) {
            throw new AuroraException("Missing Bintray licenses")
        }

        if (!bintraySettings.labels) {
            throw new AuroraException("Missing Bintray labels")
        }

        this.bintraySettings = bintraySettings
    }


    def javaVersion(Closure closure) {
        JavaVersion requiredJavaVersion = new JavaVersion()

        closure.delegate = requiredJavaVersion
        closure.resolveStrategy = Closure.DELEGATE_FIRST

        closure()

        Log.debug("Required Java version: ${requiredJavaVersion.dump()}")

        this.requiredJavaVersion = requiredJavaVersion
    }


    def runArgs(Closure closure) {
        RunArguments runArguments = new RunArguments()

        closure.delegate = runArguments
        closure.resolveStrategy = Closure.DELEGATE_FIRST

        closure()

        Log.debug("Run arguments: ${runArguments.dump()}")

        this.runArguments = runArguments
    }
}
