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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Generates a MoonDeploy app descriptor having sensible defaults
 */
class GenerateAppDescriptorTask extends DefaultTask {
    @TaskAction
    public void generateDescriptor() {
        ant.moonDeploy(
                baseURL: "${project.url}/releases/latest",

                name: project.moonLicense.productInfo.productName,
                version: project.version,

                description: project.description,
                publisher: project.auroraSettings.authors.first().name,

                skipPackageLevels: 1,

                iconPath: "mainIcon.png"
        ) {
            commandLine {
                param("bash")
                param("bin/${project.name}")
            }

            pkg(name: "${project.name}-${project.version}.zip")

            os(
                    name: "windows",
                    iconPath: "mainIcon.ico"
            ) {
                commandLine {
                    param("bin\\${project.name}.bat")
                }
            }
        }
    }
}
