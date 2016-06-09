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

import groovy.json.StringEscapeUtils
import info.gianlucacosta.aurora.gradle.AuroraException
import info.gianlucacosta.aurora.utils.Log
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Generates a class providing the information for the current artifact
 */
class GenerateArtifactInfoTask extends DefaultTask {
    @TaskAction
    def generateAppInfo() {
        String languageDirectoryName = project.mainLanguage
        Log.debug("Language directory name: ${languageDirectoryName}")

        String sourceExtension = languageDirectoryName
        String templateResourceName = "ArtifactInfo.${sourceExtension}.txt"


        String groupLastComponent = project.groupId.split("\\.").last()
        String artifactId = project.artifactId


        String sourcePackage

        if (groupLastComponent != artifactId) {
            String artifactIdPackageComponent =
                    artifactId
                            .replaceAll("[^A-Za-z0-9]", "_")
                            .replaceAll("__+", "_")

            sourcePackage = "${project.groupId}.${artifactIdPackageComponent}"
        } else {
            sourcePackage = project.groupId
        }

        Log.debug("Source package: ${sourcePackage}")


        String sourcePackageRelativePath = sourcePackage.replaceAll("\\.", "/")
        Log.debug("Source package relative path: ${sourcePackageRelativePath}")


        File sourcePackageDirectory = project.file("src/generated/${languageDirectoryName}/${sourcePackageRelativePath}")
        Log.debug("Source package directory: ${sourcePackageDirectory}")

        if (!sourcePackageDirectory.exists()) {
            if (!sourcePackageDirectory.mkdirs()) {
                throw new AuroraException("Cannot create source package directory: '${sourcePackageDirectory}'")
            }
        }


        String templateString = this.getClass().getResource(templateResourceName).text
        String sourceFileContent = injectVariables(templateString, sourcePackage)

        String sourceFileName = "ArtifactInfo.${sourceExtension}"
        File sourceFile = new File(sourcePackageDirectory, sourceFileName)

        Log.debug("Artifact Info source file: ${sourceFile}")

        sourceFile.text = sourceFileContent
    }


    private def injectVariables(String templateString, String sourcePackage) {
        return templateString
                .replace(
                "@PACKAGE@",
                sourcePackage
        )

                .replace(
                "@NAME@",
                StringEscapeUtils.escapeJava(project.moonLicense.productInfo.productName)
        )

                .replace(
                "@VERSION@",
                StringEscapeUtils.escapeJava(project.version.toString())
        )

                .replace(
                "@COPYRIGHT_YEARS@",
                StringEscapeUtils.escapeJava(project.moonLicense.getCopyrightYears())
        )

                .replace(
                "@COPYRIGHT_HOLDER@",
                StringEscapeUtils.escapeJava(project.moonLicense.productInfo.copyrightHolder)
        )

                .replace(
                "@LICENSE@",
                StringEscapeUtils.escapeJava(project.moonLicense.license.name)
        )

                .replace(
                "@WEBSITE@",
                StringEscapeUtils.escapeJava(project.url.toString())
        )

                .replace(
                "@FACEBOOK_PAGE@",
                project.facebookPage != null ?
                        "\"${StringEscapeUtils.escapeJava(project.facebookPage)}\""
                        :
                        "null"
        )

                .replace(
                "@RELEASE@",
                project.isRelease.toString()

        )
    }
}
