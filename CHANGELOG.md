# Change Log
All notable changes to this project will be documented in this file. 

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [4.0.0] - ?

### Added
- Guice for dependency injection
- Hibernate JPA
- Clowder DAOs use Clowder user credentials
- BrownDog provenance endpoint
- Support for multiple files in a dataset when executing tools
- Documentation for CORS configuration
- PostgreSQL support [WOLF-153](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-153)
- Web editor import/export and delete workflow tools
- Web editor import/export of workflows
- Web editor route to open workflows by id in build/execute views
- Web editor allows editing command line tools

### Changed
- All DAOs use Guice injected entity manager instead of spring
- Medici project renamed Clowder - modified all references
- Clowder Dataset DAO can store data by user instead of anonymous only[WOLF-194](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-194)
- DAOs wired through datawolf.properties file
- Dataset downloads actual file instead of zip from DatasetView when there is only 1 file in the dataset
- Web editor scales for large displays

### Removed
- Removed Spring Data

### Fixed
- MySQL timeout bug that caused engine exception - [WOLF-150](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-150)
- Windows batch file failed to launch correctly
- Command line executor did not handle the case where a dataset referenced multiple files [WOLF-189](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-189)
- Clowder endpoint checks if configured URL ends with a slash
- History view did not display multiple outputs
- Tool creation page did not attach all selected files only the last one
- Execution page was not displaying parameter options as a dropdown menu
- HSQL truncating long strings
- Web editor delete button does not work in History/Executions view

## [3.0.0] - 2015-04-24

First official release of DataWolf - formerly known as Cyberintegrator.
