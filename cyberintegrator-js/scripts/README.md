Node-Proxy-Services is a script that runs both HTTP and HTTP Proxy servers as local instances on a developer's machine.

## Getting Started

Developers frequently need a local web server for development purposes. While [nginx](nginx.org) and [Apache](http://httpd.apache.org/) are industrial-level servers, Node.js and Coffeescript can be used to quickly start a development server for any project. 

Unfortunately, developers often need BOTH a local web server and easy-access to remote data services.
HTTP Proxy servers allow developers to easily develop HTML5, Javascript,and other applications that consume remote dataservices and `bypass` all security issues of cross-domain restrictions/workarounds.

![Screenshot](https://github.com/ThomasBurleson/node-proxy-services/raw/master/docs/violation.png)<br/>

This project provides CoffeeScript files to easily configure and launch a local web servers with support for proxied, remote dataservices.

### Requirements

* [nodeJS](http://github.com/ry/node) ( versions: 0.6.x or later )
* [npm](http://github.com/isaacs/npm)
* [coffee-script](https://github.com/jashkenas/coffee-script) ( installed via NPM; version: 1.2 or later )  
* A WebKit based browser: Chrome, Safari, etc.

### Install

Copy the `run.coffee` and `./node_modules` to a `scripts` directory within you project folders.<br/>
Or copy the above files to a common, easily accessible directory<br/>
Or include the node-proxy-services repository as a subModule within your own git repository.

Open a Terminal window and cd to your project **webroot** [which is the `apps` folder in our case].<br/>
Run the script:

    coffee ../scripts/run.coffee
    
Coffeescript will run the specified script on top of Node.js… it could not get any easier!


![Screenshot](https://github.com/ThomasBurleson/node-proxy-services/raw/master/docs/illustration.png)<br/>


### Configuration

The run.coffee script contains defaults for local and remote URIs:

        # 
        # Include the HTTP and HTTP Proxy classes
		# @see http://nodejs.org/docs/v0.4.2/api/modules.html
		#
		ext = require('httpServers')

		# Main application
		#
		main = (options) ->

			options ||= { 
					'proxy_regexp' : /^\/api\/json/
					
					'local_host'   : '127.0.0.1'
					'local_port'   : 8000
					
					'remote_host'  : 'services.mydomain.com'    
					'remote_port'  : 80
					
					# disable since default value == local_port + 100
					#'silent_port'  : 8100
				}

			new ext.HttpProxyServer() .start( options )

			return	

		# Auto-start the web server
		#
		main()    

Developers should change the `options` to conform to their desired configurations.

### Usage

The `run.coffee` script will launch two (2) servers:

  * HTTP Proxy server listening on `http://localhost:8000`
  * HTTP server listening on `http://localhost:8100`… [this instance is hidden]
  
All applications requests for resources should be directed to the `http://localhost:8000`. If any requests are actually remote data service requests, those requests are proxied to the remote server. All other requests use the silent web server `localhost:8100` for local, non-proxied web assets.

For the above configuration, the option ` proxy_regexp` is used to specify a regular expression that will be used to match part of the URI. 

    http://localhost:8000/index.html           --> forwarded to -->  http://localhost:8100/index.html
    http://localhost:8000/api/json/catalog.xml --> proxied to   -->  http://services.mydomain.com:80/catalog.xml
    
According to our configuration above, any AJAX or other HTTP GET that use `/api/json` in the URL will be routed to the remote server at `http://services.mydomain.com:80/`.  

![Screenshot](https://github.com/ThomasBurleson/node-proxy-services/raw/master/docs/proxy.png)<br/>  

Developers can easily change the local_port (e.g. :8000) or the silent_port (e.g. :8100) to match their own local port requirements.

### Disclaimers

These scripts are NOT meant for production use. These simple facilitate local web servers with cross-domain support.

This project does NOT duplicate the GitHub efforts for Node or any extensions. Rather this project leverages various GitHub libraries to quickly and painlessly configure both an HTTP server and a HTTP Proxy Server.


## Thanks

This project respectfully uses code from and thanks the authors of:

* [CoffeeScript](https://github.com/jashkenas/coffee-script)
* [NodeJS](http://github.com/ry/node)
* [NPM](http://github.com/isaacs/npm)
* [node-http-proxy](https://github.com/nodejitsu/node-http-proxy)
* [AngularJS Seed Project](https://github.com/angular/angular-seed)
* [CafeTownsend - AngularJS](https://github.com/ThomasBurleson/angularJS-CafeTownsend)