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

import info.gianlucacosta.aurora.gradle.services.DynamicService

import info.gianlucacosta.aurora.gradle.services.StaticService
import info.gianlucacosta.aurora.gradle.settings.AuroraSettings
import org.gradle.BuildAdapter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

/**
 * Aurora's plugin for Gradle, performing the required registrations
 */
class AuroraPlugin implements Plugin<Project> {
    public static final String MAVEN_TEMP_DIRECTORY_NAME = "mavenTemp"

    @Override
    void apply(Project project) {
        StaticService staticService = new StaticService(project)
        staticService.run()

        project.ext.aurora = { Closure closure ->
            AuroraSettings auroraSettings = new AuroraSettings()

            closure.delegate = auroraSettings
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure()

            project.ext.auroraSettings = auroraSettings

            DynamicService dynamicService = new DynamicService(project)
            dynamicService.run()
        }


        project.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void projectsEvaluated(Gradle gradle) {
                ensureAuroraSettings()
            }

            private void ensureAuroraSettings() {
                if (project.auroraSettings == null) {
                    throw new AuroraException("You need to employ the aurora{} block within the build script")
                }
            }
        })
    }
}
