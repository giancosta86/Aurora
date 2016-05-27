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

        project.description = "A test project"

        project.plugins.apply("scala")
        project.plugins.apply("info.gianlucacosta.aurora")
    }


    private void applyDefaultWith(Closure closure) {
        project.aurora {
            gitHubUser = "anyUser"

            author {
                name = "TestAuthor"
                email = "test@localhost"
                url = "localhost"
            }

            bintray {
                user = "theUser"
                key = "theKey"
                repo = "testRepo"
                licenses = ['Apache-2.0']
                labels = ["testLabel1", "testLabel2"]

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


    void test_withoutDescription() {
        shouldFail(AuroraException) {
            applyDefaultWith {
                project.description = null
            }
        }
    }


    void test_withoutDocTask() {
        applyDefaultWith {}

        assertEquals(
                "scaladoc",
                project.auroraSettings.docTask
        )
    }


    void test_withoutGitHubUser() {
        shouldFail(AuroraException) {
            applyDefaultWith {
                gitHubUser = null
            }
        }
    }


    void test_withoutAuthors() {
        shouldFail(AuroraException) {
            applyDefaultWith {
                authors = null
            }
        }
    }


    void test_withoutBintraySettingsWithBintray() {
        project.plugins.apply("com.jfrog.bintray")

        shouldFail(AuroraException) {
            applyDefaultWith {
                bintraySettings = null
            }
        }
    }


    void test_withoutBintraySettingsWithoutBintray() {
        applyDefaultWith {
            bintraySettings = null
        }
    }


    void test_auroraSettingsAvailability() {
        applyDefaultWith {}

        assertEquals(
                "anyUser",
                project.auroraSettings.gitHubUser
        )
    }

    void test_requiredJavaVersion() {
        applyDefaultWith {
            javaVersion {
                major = 1
                minor = 7
                build = 5
                update = 64
            }
        }

        assertEquals(
                1,
                project.auroraSettings.requiredJavaVersion.major
        )

        assertEquals(
                7,
                project.auroraSettings.requiredJavaVersion.minor
        )

        assertEquals(
                5,
                project.auroraSettings.requiredJavaVersion.build
        )

        assertEquals(
                64,
                project.auroraSettings.requiredJavaVersion.update
        )
    }


    void test_requiredJavaVersionWithDefaults() {
        applyDefaultWith {
            javaVersion {
                major = 1
                minor = 8
                update = 91
            }
        }

        assertEquals(
                1,
                project.auroraSettings.requiredJavaVersion.major
        )

        assertEquals(
                8,
                project.auroraSettings.requiredJavaVersion.minor
        )

        assertEquals(
                0,
                project.auroraSettings.requiredJavaVersion.build
        )

        assertEquals(
                91,
                project.auroraSettings.requiredJavaVersion.update
        )
    }
}
