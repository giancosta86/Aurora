package info.gianlucacosta.aurora.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.rules.TemporaryFolder

class DslTest extends GroovyTestCase {
    private TemporaryFolder tempFolder
    private Project project

    @Override
    void setUp() {
        super.setUp()

        tempFolder = new TemporaryFolder()
        tempFolder.create()

        project = ProjectBuilder.builder()
                .withName("AuroraTest")
                .withProjectDir(tempFolder.getRoot())
                .build()

        project.plugins.apply("scala")
        project.plugins.apply("info.gianlucacosta.aurora")

        project.group = "alpha"
        project.archivesBaseName = "beta"

        project.description = "A test project"
    }


    private void applyDefaultWith(Closure closure) {
        project.aurora {
            gitHubUser = "anyUser"

            author {
                name = "TestAuthor"
                email = "test@localhost"
                url = "localhost"
            }

            closure.delegate = delegate
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure()
        }
    }


    @Override
    void tearDown() {
        tempFolder.delete()

        super.tearDown();
    }


    void test_defaultConfiguration() {
        applyDefaultWith {}
    }


    void test_auroraSettingsAvailability() {
        applyDefaultWith {}

        assertEquals(
            "anyUser",
            project.auroraSettings.gitHubUser
        )
    }
}
