@echo off
setlocal enabledelayedexpansion

REM change to folder of datawolf
cd "%~dp0\.."

REM port for the jetty server
set PORT=8888

REM context root for datawolf server, needed when behind nginx
set CONTEXT=/datawolf

REM log file, leave blank for console
REM set LOG=--out %cd%\log\datawolf-yyyy_mm_dd.log

REM create some folders just in case
if not exist log mkdir log
if not exist data\db mkdir data\db
if not exist data\files mkdir data\files

REM create some folders just in case
if not exist data\files mkdir data\files

REM setup for the server
set SERVER=""
FOR %%W in (lib\datawolf-webapp-all*.war) do (
  set SERVER="%cd%\conf\server.xml"
  set WAR=%cd%\%%W
  set WAR_URI=file:///!WAR:\=/!
  set APP=%cd%\conf\applicationContext.xml
  set APP_URI=file:///!APP:\=/!
  echo ^<Configure class="org.eclipse.jetty.webapp.WebAppContext"^> > !SERVER!
  echo ^<Set name="contextPath"^>%CONTEXT%/^</Set^> >> !SERVER!
  echo ^<Set name="war"^>!WAR_URI!^</Set^> >> !SERVER!
  echo  ^<Call name="setInitParameter"^> >> !SERVER!
  echo    ^<Arg^>contextConfigLocation^</Arg^> >> !SERVER!
  echo    ^<Arg^>!APP_URI!^</Arg^> >> !SERVER!
  echo  ^</Call^> >> !SERVER!
  echo ^</Configure^> >> !SERVER!
)

REM setup for the editor
set EDITOR=""
FOR %%W in (lib\datawolf-editor*.war) do (
  set EDITOR="%cd%\conf\editor.xml"
  set WAR=%cd%\%%W
  set WAR_URI=file:///!WAR:\=/!
  echo ^<Configure class="org.eclipse.jetty.webapp.WebAppContext"^> > !EDITOR!
  echo ^<Set name="contextPath"^>%CONTEXT%/editor^</Set^> >> !EDITOR!
  echo ^<Set name="war"^>!WAR_URI!^</Set^> >> !EDITOR!
  echo ^</Configure^> >> !EDITOR!
)

REM start actual webapp
java -Xmx512m -Ddatawolf.properties=%cd%/conf/datawolf.properties -Dlog4j.configuration="file:///%cd:\=/%/conf/log4j.properties" -jar "%cd:\=/%/lib/jetty-runner.jar" --port %PORT% %LOG% %SERVER% %EDITOR%
