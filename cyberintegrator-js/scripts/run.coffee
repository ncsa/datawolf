# ************************************************
# Build HTTP and HTTP_PROXY servers
#
#  Note: to debug Node.js scripts, 
#        see https://github.com/dannycoates/node-inspector
#
# Copyright 2012     Mindspace, LLC.
# ************************************************

		# Include the HTTP and HTTP Proxy classes
		# @see http://nodejs.org/docs/v0.4.2/api/modules.html
		#
		ext = require('httpServers')


		# Main application
		#
		main = (options) ->

			options ||= { 
					'proxy_regexp' : /^\/(workflows|persons|executions|datasets|files)(\/.*)?$/
					'local_port'   : 8001
					'local_host'   : '127.0.0.1' 		
					'remote_port'  : 8888
					'remote_host'  : 'rapid.ncsa.illinois.edu'  

					# Only used to explicity define the local, hidden web server port
					#'silent_port'  : 8000
				}

			# Primary server, proxies specific GETs to remote web 
			# server or to local web server
			new ext.HttpProxyServer() .start( options )

			return	

		# Auto-start
		#
		main()
