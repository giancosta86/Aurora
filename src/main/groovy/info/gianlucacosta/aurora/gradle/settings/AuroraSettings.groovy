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

package info.gianlucacosta.aurora.gradle.settings

import info.gianlucacosta.aurora.gradle.AuroraException

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


    def author(Closure closure) {
        Author author = new Author()

        closure.delegate = author
        closure.resolveStrategy = Closure.DELEGATE_FIRST

        closure()

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

        this.requiredJavaVersion = requiredJavaVersion
    }
}
