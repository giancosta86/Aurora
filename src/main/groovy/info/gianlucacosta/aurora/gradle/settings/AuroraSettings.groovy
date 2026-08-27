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
}
