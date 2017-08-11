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

import info.gianlucacosta.aurora.gradle.settings.JavaVersion
import info.gianlucacosta.aurora.gradle.settings.RunArguments
import info.gianlucacosta.aurora.utils.Log
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskAction

/**
 * Generates custom scripts to start the app
 */
class GenerateCustomStartupScripts extends DefaultTask {
    @TaskAction
    def run() {
        if (!project.hasApplication || !project.auroraSettings.customStartupScripts) {
            Log.info("Skipping custom app scripts creation")
            throw new StopExecutionException()
        }

        createAdditionalScripts()
        customizeDefaultScripts()
    }


    private void createAdditionalScripts() {
        File scriptsTempDirectory = new File("src/generated/dist/bin")
        scriptsTempDirectory.mkdirs()

        [
                "RunJava.py",
                "RunJava.js"
        ].forEach({ scriptName ->
            String scriptContent = this.getClass().getResource(scriptName).text
            File scriptFile = new File(scriptsTempDirectory, scriptName)
            scriptFile.text = scriptContent
        })
    }


    private void customizeDefaultScripts() {
        def auroraSettings = project.auroraSettings

        JavaVersion javaVersion = auroraSettings.requiredJavaVersion
        RunArguments runArguments = auroraSettings.runArguments

        project.startScripts {
            windowsStartScriptGenerator.template =
                    project.resources.text.fromString(
                            'cscript //NoLogo "%~dp0RunJava.js" ' +
                                    "${javaVersion.major} ${javaVersion.minor} ${javaVersion.build} ${javaVersion.update} " +

                                    "${project.mainClassName} ${auroraSettings.isCommandLineApp() ? 0 : 1} " +

                                    "${runArguments.jvm.join(" ")} +++ ${runArguments.app.join(" ")}\n"
                    )


            unixStartScriptGenerator.template =
                    project.resources.text.fromString(
                            '#!/usr/bin/env bash\n' +
                                    'python "\\$(dirname "\\$0")/RunJava.py" ' +

                                    "${javaVersion.major} ${javaVersion.minor} ${javaVersion.build} ${javaVersion.update} " +

                                    "${project.mainClassName} " +

                                    "${runArguments.jvm.join(" ")} +++ ${runArguments.app.join(" ")}\n"
                    )
        }
    }
}
