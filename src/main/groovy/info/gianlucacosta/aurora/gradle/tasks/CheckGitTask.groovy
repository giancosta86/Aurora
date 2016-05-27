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

package info.gianlucacosta.aurora.gradle.tasks

import info.gianlucacosta.aurora.gradle.AuroraException
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Task stopping the process whenever the project is versioned with Git <strong>and</strong> its Git status is not clean
 */
class CheckGitTask extends DefaultTask {
    @TaskAction
    def checkGitStatus() {
        File gitFolder = project.file(".git")

        if (!gitFolder.isDirectory()) {
            return
        }

        def outputBuffer = new ByteArrayOutputStream()

        project.exec {
            workingDir project.projectDir
            executable = 'git'
            args = ["status", "--porcelain"]
            standardOutput = outputBuffer
        }

        String statusOutput = outputBuffer.toString()

        if (!statusOutput.isEmpty()) {
            throw new AuroraException("The project directory must be clean according to 'git status'")
        }
    }
}
