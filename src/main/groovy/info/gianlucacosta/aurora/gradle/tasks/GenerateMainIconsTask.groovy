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

import info.gianlucacosta.aurora.utils.Log
import info.gianlucacosta.aurora.utils.SvgToPngConverter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskAction

/**
 * Generates a set of main icons (PNG files), which can be used, for example, as window icons
 */
class GenerateMainIconsTask extends DefaultTask {
    @TaskAction
    def run() {
        generateIcons()
        generateHelperClass()
    }


    def generateIcons() {
        File svgSourceFile = project.file("mainIcon.svg")

        if (!svgSourceFile.exists()) {
            throw new StopExecutionException()
        }

        File iconResourcesDir = project.file("src/generated/resources/${project.groupId.replace('.', '/')}/icons")
        iconResourcesDir.mkdirs()

        Log.debug("Source SVG icon file: ${svgSourceFile.getAbsolutePath()}")

        [16, 32, 64, 128, 512].forEach { iconSize ->
            File outputFile = new File(iconResourcesDir, "mainIcon${iconSize}.png")

            Log.debug("Target PNG icon file: ${outputFile.getAbsolutePath()}")

            SvgToPngConverter.convert(svgSourceFile, outputFile, iconSize)
        }
    }

    private def generateHelperClass() {
        String templateString = this.getClass().getResource("MainIcon.${project.mainLanguage}.txt").text
        String helperClassText = templateString.replace("@GROUP_ID@", project.groupId)

        File helperClassPackageDirectory = project.file("src/generated/${project.mainLanguage}/${project.groupId.replace('.', '/')}/icons")
        Log.debug("Helper class package path: ${helperClassPackageDirectory.getAbsolutePath()}")

        helperClassPackageDirectory.mkdirs()

        File helperClassFile = new File(helperClassPackageDirectory, "MainIcon.${project.mainLanguage}")
        Log.debug("Helper class file: ${helperClassFile.getAbsolutePath()}")

        helperClassFile.text = helperClassText
    }
}
