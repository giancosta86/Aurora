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
    private TemporaryFolder tempFolder = new TemporaryFolder()
    private Project project

    @Override
    void setUp() {
        super.setUp()

        tempFolder.create()

        project = ProjectBuilder.builder()
                .withName("AuroraTest")
                .withProjectDir(tempFolder.getRoot())
                .build()

        project.description = "A test project"

        project.plugins.apply("java")
        project.plugins.apply("info.gianlucacosta.aurora")
    }


    private void runDefaultWith(Closure closure) {
        project.aurora {
            docTask = "javadoc"
            gitHubUser = "anyUser"

            release = true

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
        runDefaultWith {}
    }


    void test_withoutDescription() {
        shouldFail(AuroraException) {
            runDefaultWith {
                project.description = null
            }
        }
    }


    void test_withoutDocTask() {
        runDefaultWith {}

        assertEquals(
                "javadoc",
                project.auroraSettings.docTask
        )
    }


    void test_withoutGitHubUser() {
        shouldFail(AuroraException) {
            runDefaultWith {
                gitHubUser = null
            }
        }
    }


    void test_withoutAuthors() {
        shouldFail(AuroraException) {
            runDefaultWith {
                authors = null
            }
        }
    }


    void test_withoutBintray() {
        shouldFail(AuroraException) {
            runDefaultWith {
                bintraySettings = null
            }
        }
    }


    void test_auroraSettingsAvailability() {
        runDefaultWith {}

        assertEquals(
                "anyUser",
                project.auroraSettings.gitHubUser
        )
    }
}
