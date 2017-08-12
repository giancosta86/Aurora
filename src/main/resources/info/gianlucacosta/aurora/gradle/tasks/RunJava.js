var shell = WScript.CreateObject("WScript.Shell")
var fileSystemObject = WScript.CreateObject("Scripting.FileSystemObject")


var programFiles64BitPath = "C:/Program Files/Java"
var programFiles32BitPath = "C:/Program Files (x86)/Java" 


var standardJavaDirRegex = /(?:jdk|jre)(\d+)\.(\d+)(?:\.(\d+)(?:_(\d+))?)?(?:-.+)?/

var javaVersionOutpuRegex = /java version "(\d+)\.(\d+)(?:\.(\d+)(?:_(\d+))?)?(?:-.+)?"/

var officialJavaUrl = "https://java.com/download/"

var argumentsPartitioner = "+++"
		


main()


function main() {
	var scriptDir = fileSystemObject.GetParentFolderName(WScript.ScriptFullName)
	var appBaseDirectory = fileSystemObject.GetParentFolderName(scriptDir)


	var scriptArguments = WScript.Arguments

	var requiredJavaVersion = buildVersion(Array(
			scriptArguments(0),
			scriptArguments(1),
			scriptArguments(2),
			scriptArguments(3)
	))

	var mainClass = scriptArguments(4)

	var runWithJavaw = (parseInt(scriptArguments(5)) == 1) 

	var additionalJvmArguments = Array()
	var appArguments = Array()

	var argumentsPartitionerFound = false

	for (i = 6; i < scriptArguments.length; i++)
	{
		var currentScriptArgument = scriptArguments(i)

		if (currentScriptArgument == argumentsPartitioner) {
			argumentsPartitionerFound = true
		} else {
			if (!argumentsPartitionerFound) {
				additionalJvmArguments.push(currentScriptArgument)
			} else {
				appArguments.push(currentScriptArgument)
			}
		}		
	}

	WScript.Echo()
	WScript.Echo()

	WScript.Echo("Additional JVM arguments: " + additionalJvmArguments)
	WScript.Echo("App arguments: " + appArguments)
	
	WScript.Echo()
	WScript.Echo()


	WScript.Echo("REQUIRED JAVA VERSION: " + requiredJavaVersion)

	WScript.Echo()
	WScript.Echo()

	var javaInfo = getJavaInfo(requiredJavaVersion)

	WScript.Echo()
	WScript.Echo()

	if (javaInfo) {
		WScript.Echo("COMPATIBLE JAVA VERSION FOUND: " + javaInfo.version +  " @ " + javaInfo.home)
		WScript.Echo()

		WScript.Echo("Main class: " + mainClass)


		WScript.Echo("App base directory: " + appBaseDirectory)

		var appLibDirectory = appBaseDirectory + "/lib"		

		var classPathArguments = Array(
				"-cp",
				'"' + appLibDirectory + '/*"'				
			)


		var overallJavaArguments = classPathArguments.concat(additionalJvmArguments)
		overallJavaArguments.push(mainClass)
		overallJavaArguments = overallJavaArguments.concat(appArguments)

		var javaCommandLine = getJavaCommandLine(
			javaInfo.home,
			runWithJavaw,
			overallJavaArguments
		)

		WScript.Echo()
		WScript.Echo("Now running the Java app with the following command line:")
		WScript.Echo("---> " + javaCommandLine)
		WScript.Echo()

		try {	
			var appExitCode
			
			if (runWithJavaw) {
				shell.Run(javaCommandLine)
				appExitCode = 0
			} else {
				appExitCode = shell.Run(javaCommandLine, 1, true)
			}
			
			WScript.Quit(appExitCode)
		} catch(ex) {			
			WScript.Echo("Error while running the app! Code: " + ex.number)
			WScript.Quit(1)
		}		
	} else {
		showWarning("No compatible Java version found!\n\nNow opening Java's official download website.")
		shell.Run(officialJavaUrl)
		
		WScript.Quit(1)
	}
}


function getJavaInfo(requiredJavaVersion) {
	var environmentJavaHomeInfo = searchEnvironmentJavaHome(requiredJavaVersion)
	if (environmentJavaHomeInfo) {
		return environmentJavaHomeInfo
	}

	WScript.Echo()
	WScript.Echo()
	
	var programFiles64BitInfo = search64BitProgramFilesDirectory(requiredJavaVersion)
	if (programFiles64BitInfo) {
		return programFiles64BitInfo
	}

	WScript.Echo()
	WScript.Echo()

	var programFiles32BitInfo = search32BitProgramFilesDirectory(requiredJavaVersion)
	if (programFiles32BitInfo) {
		return programFiles32BitInfo
	}

	WScript.Echo()
	WScript.Echo()

	return null
}


function searchEnvironmentJavaHome(requiredJavaVersion) {
	try {
		var environmentJavaHome = shell.ExpandEnvironmentStrings("%JAVA_HOME%")

		if (environmentJavaHome != "%JAVA_HOME%")  {
			WScript.Echo("JAVA_HOME found: " + environmentJavaHome)

			var javaExeFilePath = getJavaExeFilePath(environmentJavaHome)

			if (!fileSystemObject.FileExists(javaExeFilePath)) {
				WScript.Echo("This JAVA_HOME does not actually contain a Java interpreter!")
				return null
			}


			var environmentJavaHomeMatch = standardJavaDirRegex.exec(environmentJavaHome)

			var environmentJavaHomeVersion = null

			if (environmentJavaHomeMatch) {
				environmentJavaHomeVersion = buildVersion(environmentJavaHomeMatch.slice(1))
			} else {
				WScript.Echo("It is not a standard Java directory name - now asking Java itself")
				WScript.Echo()

				var javaCommandLine = getJavaCommandLine(
					environmentJavaHome, 
					false, 
					Array("-version")
				)


				WScript.Echo("Running Java command to get version info:")
				WScript.Echo("---> " + javaCommandLine)

			
				var javaExec = shell.Exec(javaCommandLine)
		
				var javaVersionLine = javaExec.StdErr.ReadLine()			

				if (javaExec.ExitCode == 0) {
					WScript.Echo("Detected Java version line --> " + javaVersionLine)

					var javaVersionOutputMatch = javaVersionOutpuRegex.exec(javaVersionLine)

					if (javaVersionOutputMatch) {
						environmentJavaHomeVersion = buildVersion(javaVersionOutputMatch.slice(1))
						WScript.Echo("Detected Java version--> " + environmentJavaHomeVersion)
					} else {
						WScript.Echo("Cannot detect the Java version via its output line")
					}
				} else {
					WScript.Echo("Execution error while trying to detect Java version")
				}
			}

			if (environmentJavaHomeVersion) {
				WScript.Echo("JAVA_HOME version: " + environmentJavaHomeVersion)

				if (compareVersions(environmentJavaHomeVersion, requiredJavaVersion) >= 0) {				
						return {
							home: environmentJavaHome,
							version: environmentJavaHomeVersion
						}
					} else {
						WScript.Echo("Obsolete Java version in JAVA_HOME")
					}
			}
		} else {
			WScript.Echo("JAVA_HOME not set!")
		}
	} catch(ex) {
		WScript.Echo("An exception occurred: " + ex.description)
	}
}



function getJavaExeFilePath(javaHome) {
	return javaHome + "/bin/java.exe"
}


function getJavawExeFilePath(javaHome) {
	return javaHome + "/bin/javaw.exe"
}


function search64BitProgramFilesDirectory(requiredJavaVersion) {
	WScript.Echo("Now scanning for 64-bit Java in '" + programFiles64BitPath + "'...")

	return scanForTheLatestJavaDirectory(programFiles64BitPath, requiredJavaVersion)
}


function search32BitProgramFilesDirectory(requiredJavaVersion) {
	WScript.Echo("Now scanning for 32-bit Java in '" + programFiles32BitPath + "'...")

	return scanForTheLatestJavaDirectory(programFiles32BitPath, requiredJavaVersion)
}


function scanForTheLatestJavaDirectory(rootDirectoryPath, requiredJavaVersion) {
	try {
		var rootDirectory = fileSystemObject.GetFolder(rootDirectoryPath)	

		var highestJavaInfo = null	

		for (var subFoldersEnum = new Enumerator(rootDirectory.SubFolders); !subFoldersEnum.atEnd(); subFoldersEnum.moveNext()){	
			var subFolder = subFoldersEnum.item()
			var currentMatch = standardJavaDirRegex.exec(subFolder.name)
			
			if (currentMatch) {
				var currentRelativePath = currentMatch[0]
				var currentVersion = buildVersion(currentMatch.slice(1))

				WScript.Echo("Java found ------> " + currentVersion + " @ " + currentRelativePath)

				if (
					!highestJavaInfo || (				
						compareVersions(currentVersion, highestJavaInfo.version) > 0
					)
				) {			
					var currentAbsolutePath = fileSystemObject.GetFolder(rootDirectory + "/" + currentRelativePath).path
					var javaExeFilePath = getJavaExeFilePath(currentAbsolutePath)

					if (fileSystemObject.FileExists(javaExeFilePath)) {
						highestJavaInfo = {
							home: currentAbsolutePath,
							version: currentVersion
						}
					} else {
						WScript.Echo("(Skipping this directory, as it does not contain a Java interpreter)")
						WScript.Echo()
					}
				}
			}
		}
		
		WScript.Echo()

		if (
			highestJavaInfo && 
			(compareVersions(highestJavaInfo.version, requiredJavaVersion) >= 0)
		) {
			return highestJavaInfo
		} else {
			WScript.Echo("No updated Java version found!")

			return null
		}
	} catch(ex) {
		WScript.Echo("An exception occurred: " + ex.description)
	}
}



function getJavaCommandLine(javaHome, useJavaw, arguments) {
	var interpreterFilePath =
		useJavaw ?
			getJavawExeFilePath(javaHome)
			: 
			getJavaExeFilePath(javaHome)

	var argumentsString = arguments.join(" ")

	return '"' + interpreterFilePath + '"' + " " + argumentsString
}


function showWarning(prompt) {
  WScript.Echo(prompt)
  shell.Popup(prompt, 0, "Warning", 0x30)  
}


function buildVersion(versionComponents) {
	var result = Array()

	for (var i = 0; i <= 3; i++) {
		result.push(
			versionComponents[i] ?
				parseInt(versionComponents[i])
				:
				0
		)
	}

	return result
}


function compareVersions(thisVersion, thatVersion) {
	for (var i = 0; i <= 3; i++) {
		var componentDifference = thisVersion[i] - thatVersion[i]
		if (componentDifference != 0) {
			return componentDifference
		}
	}

	return 0
}