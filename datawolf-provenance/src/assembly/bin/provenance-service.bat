@echo off
setlocal enabledelayedexpansion

REM change to folder of datawolf
cd "%~dp0\.."

REM port for the jetty server
set PORT=8889

REM context root for datawolf server, needed when behind nginx
set CONTEXT=/datawolf

REM setup for the viewer
set EDITOR=""
FOR %%W in (lib\datawolf-provenance*.war) do (
  set EDITOR="%cd%\conf\provenance.xml"
  set WAR=%cd%\%%W
  set WAR_URI=file:///!WAR:\=/!
  echo ^<Configure class="org.eclipse.jetty.webapp.WebAppContext"^> > !EDITOR!
  echo ^<Set name="contextPath"^>%CONTEXT%/provenance^</Set^> >> !EDITOR!
  echo ^<Set name="war"^>!WAR_URI!^</Set^> >> !PROVENANCE!
  echo ^</Configure^> >> !PROVENANCE!
)

REM start actual webapp
java -Xmx512m -jar "%cd:\=/%/lib/jetty-runner.jar" --port %PORT% %EDITOR%