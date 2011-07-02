<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="yes" />

<xsl:param name="home"/>

<xsl:template match="/LoggerLevel">
	<html>
		<head>
			<title>Set Logger Level</title>
			<link rel="Stylesheet" type="text/css" media="all" href="/LoggerLevelServlet.css"></link>
			<link rel="Stylesheet" type="text/css" media="all" href="/JSPopup.css"></link>
			<script> var home = '<xsl:value-of select="$home"/>';</script>
			<script language="JavaScript" type="text/javascript" src="/JSUtil.js">;</script>
			<script language="JavaScript" type="text/javascript" src="/JSPopup.js">;</script>
			<script language="JavaScript" type="text/javascript" src="/LoggerLevelServlet.js">;</script>
		</head>
		<body>
		<div class="closebox">
			<img src="/icons/home.png"
				 onclick="window.open('{$home}','_self');"
				 title="Return to the home page"/>
			<br/>
			<img src="/icons/save.png"
				 onclick="save();"
				 title="Set the logger level"/>
		</div>

		<h1>Set the Logger Level</h1>

		<form id="formID" method="post" action="" accept-charset="UTF-8">
		<input type="hidden" name="home" value="{$home}"/>

		<p class="note">
			This page can be used to set the logger level for classes in the application.
			It should be used only when directed by a technical support engineer.
			Setting the logger level below INFO can result in the application log growing
			very rapidly.
		</p>

		<p class="note">
			Enter the fully qualified class name of the class
			and select the logger level for that class.</p>
		<center>
			<table border="1">
				<tr>
					<td width="30%">Class name</td>
					<td width="70%"><input class="text" type="text" name="class"/></td>
				</tr>
				<tr>
					<td>Level</td>
					<td>
						<input type="radio" name="level" value="OFF"/>OFF<br/>
						<input type="radio" name="level" value="ERROR"/>ERROR<br/>
						<input type="radio" name="level" value="WARN"/>WARN<br/>
						<input type="radio" name="level" value="INFO"/>INFO<br/>
						<input type="radio" name="level" value="DEBUG"/>DEBUG<br/>
					</td>
				</tr>
			</table>
		</center>

		</form>

		</body>
	</html>
</xsl:template>

</xsl:stylesheet>