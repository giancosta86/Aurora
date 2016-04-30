var shell = WScript.CreateObject("WScript.Shell")

var arguments = WScript.Arguments

var requiredMajor = arguments(0)
var requiredMinor = arguments(1)
var requiredBuild = arguments(2)
var requiredUpdate = arguments(3)


WScript.Echo("---=== REQUIRED JAVA VERSION ===---")
WScript.Echo()
WScript.Echo("Major version: " + requiredMajor)
WScript.Echo("Minor version: " + requiredMinor)
WScript.Echo("Build version: " + requiredBuild)
WScript.Echo("Update version: " + requiredUpdate)

WScript.Echo()
WScript.Echo()

var SleepMilliseconds = 50
var Running = 0
var JavaUrl = "https://java.com/download/"

function showWarning(prompt) {
  WScript.Echo(prompt)
  shell.Popup(prompt, 0, "Warning", 0x30)
}


function showJavaWarning(prompt) {
  showWarning(prompt)
  shell.Run(JavaUrl)
}


function showCompatibilityWarning() {
  var prompt = "Java "

  if (requiredUpdate > 0) {
     prompt += requiredMajor + "." + requiredMinor + "." + requiredBuild + "_" + requiredUpdate
   } else {
     prompt += requiredMajor + "." + requiredMinor + "." + requiredBuild
   }

  prompt += " or later is required to run this program."

  showJavaWarning(prompt)

  WScript.Quit(1)
}


function commandExists(command) {
  var whereCommand = 'where "' + command + '"'

  var whereProcess = shell.Exec(whereCommand)

  while (whereProcess.Status == Running) {
    WScript.Sleep(SleepMilliseconds)
  }

  return whereProcess.ExitCode == 0
}



if (!commandExists("java")) {
  showJavaWarning("Cannot detect Java. Is it installed and in your PATH variable?")
  WScript.Quit(1)
}


var javaVersionProcess = shell.Exec("java -version")
while (javaVersionProcess.Status == Running) {
  WScript.Sleep(SleepMilliseconds)
}

if (javaVersionProcess.ExitCode != 0) {
  showJavaWarning("Error while detecting Java version. Is it correctly installed?")
  WScript.Quit(1)
}

var javaVersionOutput = javaVersionProcess.StdErr.ReadLine()

var javaVersionRegex = /(\d+)\.(\d+)(?:\.(\d+)(?:_(\d+))?)?/

var versionMatch = javaVersionRegex.exec(javaVersionOutput)

if (versionMatch == null) {
  showJavaWarning("Error while retrieving Java version. Is Java correctly installed?")
  WScript.Quit(1)
}


var majorVersion = versionMatch[1]
var minorVersion = versionMatch[2]
var buildVersion = versionMatch[3]
if (!buildVersion) {
  buildVersion = 0
}

var updateVersion = versionMatch[4]
if (!updateVersion) {
  updateVersion = 0
}


WScript.Echo("---=== JAVA VERSION ===---")
WScript.Echo()
WScript.Echo("Major version: " + majorVersion)
WScript.Echo("Minor version: " + minorVersion)
WScript.Echo("Build version: " + buildVersion)
WScript.Echo("Update version: " + updateVersion)


if (majorVersion > requiredMajor) {
  WScript.Quit(0)
}

if (majorVersion < requiredMajor) {
  showCompatibilityWarning()
}


if (minorVersion > requiredMinor) {
  WScript.Quit(0)
}


if (minorVersion < requiredMinor) {
  showCompatibilityWarning()
}


if (buildVersion > requiredBuild) {
  WScript.Quit(0)
}

if (buildVersion < requiredBuild) {
  showCompatibilityWarning()
}


if (updateVersion > requiredUpdate) {
  WScript.Quit(0)
}

if (updateVersion < requiredUpdate) {
  showCompatibilityWarning()
}
