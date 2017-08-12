import re
import os
import sys
import subprocess
import webbrowser


standardJavaDirPattern = re.compile(r"(?:jdk|jre)(\d+)\.(\d+)(?:\.(\d+)(?:_(\d+))?)?(?:-.+)?")

javaVersionOutputPattern = re.compile(r'java version "(\d+)\.(\d+)(?:\.(\d+)(?:_(\d+))?)?(?:-.+)?"')


officialJavaUrl = "https://java.com/download/"

argumentsPartitioner = "+++"



def main():
	scriptDir =  os.path.dirname(os.path.realpath(__file__))
	appBaseDirectory = os.path.dirname(scriptDir)


	requiredJavaVersion = buildVersion(sys.argv[1:5])

	mainClass = sys.argv[5]

	additionalScriptArguments = sys.argv[6:]

	partitionerIndex = additionalScriptArguments.index(argumentsPartitioner)

	additionalJvmArguments = additionalScriptArguments[:partitionerIndex]
	appArguments = additionalScriptArguments[(partitionerIndex + 1):]


	print("")
	print("")

	print("Additional JVM arguments: {}".format(additionalJvmArguments))
	print("App arguments: {}".format(appArguments))
	
	print("")
	print("")


	print("REQUIRED JAVA VERSION: {}".format(requiredJavaVersion))
	print("")
	print("")

	javaInfo = getJavaInfo(requiredJavaVersion)

	print("")
	print("")

	if javaInfo:
		print("COMPATIBLE JAVA VERSION FOUND: {} @ {} ".format(javaInfo["version"], javaInfo["home"]))
		print("")

		print("Main class: {}".format(mainClass))

		print("App base directory: {}".format(appBaseDirectory))

		appLibDirectory = "{}/lib".format(appBaseDirectory)

		classPathArguments = [
				"-cp",
				'"{}/*"'.format(appLibDirectory)
			]

		overallJavaArguments = classPathArguments + additionalJvmArguments
		overallJavaArguments += [mainClass]
		overallJavaArguments += appArguments

		javaCommandLine = getJavaCommandLine(
			javaInfo["home"],			
			overallJavaArguments
		)

		print("")
		print("Now running the Java app with the following command line:")
		print("---> {}".format(javaCommandLine))
		print("")

		try:
			appExitCode = subprocess.call(javaCommandLine, shell=True)

			sys.exit(appExitCode)
		except Exception as ex:
			print("Error while running the app! {}".format(ex))
			sys.exit(1)
		
	else:
		showWarning("No compatible Java version found!\n\nNow opening Java's official download website.")
		webbrowser.open(officialJavaUrl)
		sys.exit(1)



def getJavaInfo(requiredJavaVersion):
	environmentJavaHomeInfo = searchEnvironmentJavaHome(requiredJavaVersion)
	if environmentJavaHomeInfo is not None:
		return environmentJavaHomeInfo

	print("")
	print("")

	systemPathJavaHomeInfo = searchSystemPathJavaHome(requiredJavaVersion)
	if systemPathJavaHomeInfo is not None:
		return systemPathJavaHomeInfo

	return None



def searchEnvironmentJavaHome(requiredJavaVersion):
	try:
		environmentJavaHome = os.environ.get("JAVA_HOME")

		if environmentJavaHome:
			environmentJavaHome = os.path.abspath(os.path.realpath(environmentJavaHome))

			print("JAVA_HOME found: {}".format(environmentJavaHome))

			if os.path.isdir(environmentJavaHome):
				environmentJavaHomeMatch = standardJavaDirPattern.search(environmentJavaHome)

				environmentJavaHomeVersion = None

				if environmentJavaHomeMatch:
					environmentJavaHomeVersion = buildVersion(environmentJavaHomeMatch.group(1, 2, 3, 4))
				else:
					print("It is not a standard Java directory name - now asking Java itself")
					print("")

					environmentJavaHomeVersion = runJavaToGetVersion(environmentJavaHome)


				if environmentJavaHomeVersion:
					print("JAVA_HOME version found: {}".format(environmentJavaHomeVersion))

					if environmentJavaHomeVersion >= requiredJavaVersion:
						return {
							"home": environmentJavaHome,
							"version": environmentJavaHomeVersion
						}
					else:
						print("Obsolete Java version in JAVA_HOME")
					
			else:
				print("JAVA_HOME does not reference an existing directory")
		else:
			print("JAVA_HOME not set!")
	except Exception as ex:
		print("An exception occurred: {}".format(ex))



def runJavaToGetVersion(javaHome):
	javaCommandLine = getJavaCommandLine(
		javaHome,
		["-version"]
	)

	print("Running Java command to get version info:")
	print("---> {}".format(javaCommandLine))

	javaProcess = subprocess.Popen(javaCommandLine, shell=True, stderr=subprocess.PIPE)

	javaVersionLine = javaProcess.stderr.readline()

	print("Detected Java version line --> {}".format(javaVersionLine))

	javaVersionOutputMatch = javaVersionOutputPattern.search(javaVersionLine)

	if javaVersionOutputMatch:
		print("Detected Java version--> {}".format(javaVersionOutputMatch.group(0)))
		return buildVersion(javaVersionOutputMatch.group(1, 2, 3, 4))
	else:
		print("Cannot detect the Java version via its output line")
		return None



def searchSystemPathJavaHome(requiredJavaVersion):
	print("Now searching the system PATH environment variable...")
	print("")

	try:
		javaPath = subprocess.check_output(["which", "java"]).strip()

		if javaPath:
			javaHome = os.path.dirname(
				os.path.dirname(
					javaPath
				)
			)

			print("Java found in PATH! --> {}".format(javaHome))

			javaPathVersion = runJavaToGetVersion(javaHome)

			if javaPathVersion:
				return {
					"home": javaHome,
					"version": javaPathVersion
				}
		else:
			print("Java is not in the system PATH")


	except subprocess.CalledProcessError:
		print("Java is not in the system PATH")



def getJavaCommandLine(javaHomePath, arguments):
	javaExeSubPath =  "/bin/java"

	argumentsString = " ".join(arguments)

	return '"{}{}" {}'.format(javaHomePath, javaExeSubPath, argumentsString)



def showWarning(prompt):	
	print(prompt)

	try:
		subprocess.call(
			'zenity --warning --text="{}"'.format(prompt),
			shell=True,
			stdout=subprocess.PIPE,
			stderr=subprocess.PIPE
		)
	except:
		try:
			subprocess.call(
			r'osascript -e "tell app \"System Events\" to display dialog \"{}\" buttons \"OK\" default button 1 with title \"Warning\" with icon caution"'.format(prompt),
			shell=True,
			stdout=subprocess.PIPE,
			stderr=subprocess.PIPE
		)
		except:
			pass


def buildVersion(versionComponents):
	return tuple([
		int(versionComponent) if versionComponent else 0 for versionComponent in versionComponents
	])


if __name__ == "__main__":
	main()