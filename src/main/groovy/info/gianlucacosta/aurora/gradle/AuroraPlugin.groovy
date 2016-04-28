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

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * The plugin class for Gradle, performing the required registrations.
 */
class AuroraPlugin implements Plugin<Project> {
    @Override
    void apply(Project target) {
        Project project = target

        project.ext.aurora = { Closure closure ->
            AuroraSettings auroraSettings = new AuroraSettings()

            closure.delegate = auroraSettings
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure()

            AuroraService auroraService = new AuroraService(project, auroraSettings)
            auroraService.run()

            project.ext.auroraSettings = auroraSettings
        }

    }
}
