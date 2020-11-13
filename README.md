# DataWolf: Open Source Scientific Workflow System

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.2635506.svg)](https://doi.org/10.5281/zenodo.2635506)

## Running DataWolf

There are different options to run DataWolf, the easiest and quickest way to get started is by using docker. To
start DataWolf, you will need to have docker and docker-compose installed on your system. Once docker is installed you 
can start DataWolf using docker-compose and the [docker compose file](docker-compose.yml) provided. To launch DataWolf, 
you will need to run `docker-compose up -d` which will download all the containers and start them in the right order.

## Configuring DataWolf

### Customizing DataWolf in Docker

In the case of Docker, you can override some of the values used by DataWolf as well as all the other containers
by using a .env file that is placed in the same folder as the docker-compose.yml file. When Docker-compose starts,
it will use this file for environment variables specified in the docker-compose.yml file. Docker-compose
will first use the environment variables set when starting the program, next it will look in the .env file, and
finally it will use any default values specified in the docker-compose.yml file. The [env.example](env.example)
file lists all variables that can be set as well as a short description of what it does. You can for example
use this file to setup the list of user account(s) that should be administrators.
