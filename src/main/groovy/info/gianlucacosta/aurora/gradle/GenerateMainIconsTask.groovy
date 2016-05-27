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

import info.gianlucacosta.aurora.utils.SvgToPngConverter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GenerateMainIconsTask extends DefaultTask {
    @TaskAction
    def generateIcons() {
        File svgSourceFile = project.file("mainIcon.svg")

        if (!svgSourceFile.exists()) {
            return
        }

        File iconResourcesDir = project.file("src/generated/resources/${project.group.toString().replace('.', '/')}/icons")
        iconResourcesDir.mkdirs()

        [16, 32, 64, 128, 512].forEach {iconSize ->
            File outputFile = new File(iconResourcesDir, "mainIcon${iconSize}.png")
            SvgToPngConverter.convert(svgSourceFile, outputFile, iconSize)
        }
    }
}
