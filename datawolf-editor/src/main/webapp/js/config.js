// The editor is served at <context>/editor/, the REST endpoint is at <context>. Work out
// the context from the url so the editor works under any prefix (DATAWOLF_CONTEXT).
datawolfContext = (function() {
	var match = window.location.pathname.match(/^(.*)\/editor(\/|$)/);
	return match ? match[1] : "/datawolf";
})();

datawolfOptions = {
	showRegistration: true,
	// REST Endpoint for models
	rest: datawolfContext,
	// Clowder endpoint
	clowder: "http://localhost:9000",
	authentication: "DataWolf",
	//authentication: "Clowder",
	//serverName: "Clowder",
	//registrationLink: "http://localhost:9000/clowder",
	//authentication: "LDAP",
	//serverName: "NCSA LDAP",
	//registrationLink: "https://identity.ncsa.illinois.edu/",
}
