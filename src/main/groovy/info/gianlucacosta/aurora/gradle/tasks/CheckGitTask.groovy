/*§
  ===========================================================================
  Aurora
  ===========================================================================
  Copyright (C) 2015-2017 Gianluca Costa
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
import info.gianlucacosta.aurora.utils.Log
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskAction

/**
 * Stops the build process whenever the project:
 * <ol>
 *     <li>is in release mode (<b>project.isRelease</b> is true)</li>
 *     <li>is versioned with Git</li>
 *     <li>its Git status is not clean.</li>
 * </ol>
 */
class CheckGitTask extends DefaultTask {
    @TaskAction
    def checkGitStatus() {
        if (!project.isRelease) {
            Log.info("Not in release - skipping Git check")
            throw new StopExecutionException()
        }


        File gitFolder = project.file(".git")
        Log.debug("Expected Git folder: ${gitFolder.getAbsolutePath()}")

        if (!gitFolder.isDirectory()) {
            Log.info("Git folder not found - skipping Git check")
            throw new StopExecutionException()
        }

        def outputBuffer = new ByteArrayOutputStream()

        project.exec {
            workingDir project.projectDir
            executable = 'git'
            args = ["status", "--porcelain"]
            standardOutput = outputBuffer
        }

        String statusOutput = outputBuffer.toString()

        Log.debug("Git status output: ${statusOutput}")

        if (!statusOutput.isEmpty()) {
            throw new AuroraException("The project directory must be clean according to 'git status'")
        }
    }
}
