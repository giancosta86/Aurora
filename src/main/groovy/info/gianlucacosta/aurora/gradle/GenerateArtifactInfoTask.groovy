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

import groovy.json.StringEscapeUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GenerateArtifactInfoTask extends DefaultTask {
    @TaskAction
    def generateAppInfo() {
        String languageDirectoryName

        if (project.hasScala) {
            languageDirectoryName = "scala"
        } else if (project.hasGroovy) {
            languageDirectoryName = "groovy"
        } else {
            languageDirectoryName = "java"

        }

        String sourceExtension = languageDirectoryName
        String templateResourceName = "ArtifactInfo.${sourceExtension}.txt"


        String groupLastComponent = project.group.split("\\.").last()
        String artifactId = project.archivesBaseName


        String sourcePackage

        if (groupLastComponent != artifactId) {
            String artifactIdPackageComponent =
                    artifactId
                            .replaceAll("[^A-Za-z0-9]", "_")
                            .replaceAll("__+", "_")

            sourcePackage = "${project.group}.${artifactIdPackageComponent}"
        } else {
            sourcePackage = project.group
        }


        String sourcePackageRelativePath = sourcePackage.replaceAll("\\.", "/")
        File sourcePackageDirectory = project.file("src/main/${languageDirectoryName}/${sourcePackageRelativePath}")

        if (!sourcePackageDirectory.exists()) {
            if (!sourcePackageDirectory.mkdirs()) {
                throw new AuroraException("Cannot create source package directory: '${sourcePackageDirectory}'")
            }
        }


        URL templateUrl = this.getClass().getResource(templateResourceName)
        String templateString = templateUrl.text
        String sourceFileContent = injectVariables(templateString, sourcePackage)

        String sourceFileName = "ArtifactInfo.${sourceExtension}"
        File sourceFile = new File(sourcePackageDirectory, sourceFileName)

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
                StringEscapeUtils.escapeJava(project.name)
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
                project.auroraSettings.release.toString()

        )
    }
}
