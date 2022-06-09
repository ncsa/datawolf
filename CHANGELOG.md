# Change Log
All notable changes to this project will be documented in this file. 

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

## [4.5.0] - 2021-12-03

### Added
- Pagination for getting all executions by workflow id and user sorted by newest first 
  [WOLF-299](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-299)

### Fixed
- Dockerfile uses a wildcard to find war files in target directories
  [WOLF-294](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-294)

- Authorization fails if X-Userinfo doesn't contain email address
  [WOLF-291](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-291)

### Changed

## [4.4.0] - 2020-11-23

### Added
- Custom property file to customize datawolf properties
  [WOLF-272](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-272)
- Docker build process and how DataWolf is launched in Docker
  [WOLF-274](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-274)
- Configurable engine queue page size
  [WOLF-289](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-289)

### Fixed
- Changed docker-compose reference to correct image and renamed .env-example to env-example
  [WOLF-293](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-293)
- Persons endpoint was returning an array with null element instead of null when person not found
  [WOLF-283](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-283)

### Changed
- Datasets endpoint only returns users data and allows user to modify
  [WOLF-286](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-286)
- Updated documentation for 4.4 release changes
  [WOLF-236](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-236)
- Set default page size for Datasets endpoint 
  [WOLF-284](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-284)

## [4.3.0] - 2019-01-31

### Added
- Filter executions by user and date
  [WOLF-260](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-260)
- Zenodo entry for citation
  [WOLF-262](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-262)
- Password reset page for DataWolf Editor
  [WOLF-263](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-263)
- LDAP Authentication support
  [WOLF-265](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-265)
- Endpoint to fetch user access token
  [WOLF-268](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-268)

### Changed
- Enable CORS by default
  [WOLF-264](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-264)
- Set login cookie path
  [WOLF-267](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-267)

### Fixed

## [4.2.0] - 2018-03-13

### Added
- Allow users to change passwords
  [WOLF-241](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-241)
- Allow tokens to be in authorization header, added token provider support
  [WOLF-230](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-230)
- Debug attribute to prevent executor cleanup
  [WOLF-91](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-91)

### Changed
- Changed datasets API to allow attaching multiple files
  [WOLF-218](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-218)
- User accounts secured by bcrypt salted passwords
  [WOLF-227](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-227)
- User accounts can be created by specifying an email or person id
  [WOLF-248](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-248)

### Fixed
- HPC Executor lazy initialization exception
  [WOLF-253](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-253)
- Authentication failed if token is expired and user sends Basic auth
  [WOLF-238](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-238)
- Login endpoint returning no content instead of unauthorized for failed login
  [WOLF-255](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-255)

## [4.1.0] - 2017-10-06

### Added
- Accounts have two new attributes: admin attribute to indicate they are admin accounts and an active attribute to 
  enabled/disable, default is disabled unless the account is an administrator. There is also an endpoint for administrators 
  to enable/disable accounts.
  [WOLF-222](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-222)
- Server configuration file contains a list of initial administrators
  [WOLF-226](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-226)
- Documented in help docs how to specify admin accounts
  [WOLF-229](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-229)
- Authentication property now part of the startup script, default is false
  [WOLF-211](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-211)
- Dataset JPA dao now supports paging and find by title, creator and title, etc.
  [WOLF-225](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-225)
- Execution JPA dao now supports paging.
  [WOLF-224](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-224)
- Created initial landing page for datawolf.ncsa.illinois.edu that points to demonstration instance, documentation, etc.
  [WOLF-198](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-198)

### Changed
- Only administrators can delete accounts
  [WOLF-231](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-231)
- Engine uses new pagination for checking unfinished executions if server shutdown before executions were finished
- Web editor no longer requires access to the list of users to determine if account can be created. Error messages were
  added to indicate why account creation failed
  [WOLF-233](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-233)
- Web editor informs user if their account is disabled since non-admin accounts are now disable by default.
  [WOLF-232](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-232)

### Fixed
- Users could overwrite existing account and person information since there was no check for authorization or existing 
  account/person. 
  [WOLF-234](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-234)
- Command line executor was not staging data in the run directory unless it was passed at the command line.
  [WOLF-228](https://opensource.ncsa.illinois.edu/jira/browse/WOLF-228)

## [4.0.0] - 2017-05-04

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
