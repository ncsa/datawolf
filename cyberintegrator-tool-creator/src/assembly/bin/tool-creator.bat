REM @echo off

REM change to folder of cyberintegrator
cd "%~dp0\.."

REM create some folders just in case
if not exist data\files mkdir data\files

REM start actual webapp
java -Xmx512m -cp "lib/*" edu.illinois.ncsa.cyberintegrator.tool.creator.ToolCreator
