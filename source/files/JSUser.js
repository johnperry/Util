//Class to encapsulate a user, providing
//access to the user's roles on the server.
function User() {
	this.ip = "";
	this.name = "";
	this.roles = new Object();
	this.isLoggedIn = false;
	this.isLocal = false;
	this.isRemote = false;

	var req = new AJAX();
	req.GET("/user", req.timeStamp(), null);
	if (req.success()) {
		var xml = req.responseXML();
		var root = xml.documentElement;
		this.ip = root.getAttribute("ip");
		this.name = root.getAttribute("name");
		this.isLocal = (root.getAttribute("location") == "local");
		this.isLoggedIn = (this.name != "");
		this.usersImpl = root.getAttribute("usersImpl");

		var roleElements = root.getElementsByTagName("role");
		for (var i=0; i<roleElements.length; i++) {
			var roleName = roleElements[i].firstChild.nodeValue.replace(/^\s+|\s+$/g,"");
			this.roles[roleName] = true;
		}
	}
	this.hasRole = function(role) {
		return (this.roles[role] != null);
	}
}
