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
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class CheckDependenciesTask extends DefaultTask {
    @TaskAction
    def checkDependencies() {
        project.configurations.asList().each { configuration ->
            configuration.getAllDependencies().each { dependency ->
                if (dependency.hasProperty("version")) {
                    if (dependency.version != null && dependency.version.endsWith("-SNAPSHOT")) {
                        throw new AuroraException("You should not depend on a SNAPSHOT library: '${dependency.group}:${dependency.name}:${dependency.version}'")
                    }
                }
            }
        }
    }
}
