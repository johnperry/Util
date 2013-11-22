<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="yes" />

<xsl:param name="home"/>

<xsl:template match="/LoggerLevel">
	<html>
		<head>
			<title>Set Logger Level</title>
			<link rel="Stylesheet" type="text/css" media="all" href="/BaseStyles.css"></link>
			<link rel="Stylesheet" type="text/css" media="all" href="/LoggerLevelServlet.css"></link>
			<script> var home = '<xsl:value-of select="$home"/>';</script>
			<script language="JavaScript" type="text/javascript" src="/JSUtil.js">;</script>
			<script language="JavaScript" type="text/javascript" src="/LoggerLevelServlet.js">;</script>
		</head>
		<body>
		<div class="closebox">
			<xsl:if test="$home">
				<img src="/icons/home.png"
					 onclick="window.open('{$home}','_self');"
					 title="Return to the home page"/>
				<br/>
			</xsl:if>
			<img src="/icons/save.png"
				 onclick="save();"
				 title="Set the logger level"/>
		</div>

		<h1>Set Logger Level</h1>

		<form id="formID" method="post" action="" accept-charset="UTF-8">
		<xsl:if test="not($home)">
			<input type="hidden" name="suppress" value=""/>
		</xsl:if>

		<p class="note">
			This page can be used to set the logger level for classes in the application.
			It should be used only when directed by a technical support engineer.
			Setting the logger level below INFO can result in the application log growing
			very rapidly.
		</p>

		<p class="note">
			Select the class from the pull-down field or enter
			the fully qualified class name of the class in the
			text field. Then select the logger level for the
			selected class.</p>
		<center>
			<table border="1">
				<tr>
					<td width="30%">Class name</td>
					<td width="70%">
						<select id="ClassSelector" onchange="setClass()">
							<option value=""/>
							<option value="org.rsna.servlets.ApplicationServer">ApplicationServer</option>
							<option value="org.rsna.ctp.stdplugins.AuditLog">AuditLog</option>
							<option value="org.rsna.ctp.stdstages.DicomAnonymizer">DicomAnonymizer</option>
							<option value="org.rsna.ctp.stdstages.anonymizer.dicom.DICOMAnonymizer">DICOMAnonymizer</option>
							<option value="org.rsna.ctp.stdstages.DicomAuditLogger">DicomAuditLogger</option>
							<option value="org.rsna.ctp.stdstages.DicomCorrector">DicomCorrector</option>
							<option value="org.rsna.ctp.stdstages.anonymizer.dicom.DICOMCorrector">DICOMCorrector</option>
							<option value="org.rsna.ctp.stdstages.DicomExportService">DicomExportService</option>
							<option value="org.rsna.ctp.stdstages.DicomImportService">DicomImportService</option>
							<option value="org.rsna.ctp.stdstages.DicomMammoPixelAnonymizer">DicomMammoPixelAnonymizer</option>
							<option value="org.rsna.ctp.stdstages.anonymizer.dicom.DICOMMammoPixelAnonymizer">DICOMMammoPixelAnonymizer</option>
							<option value="org.rsna.ctp.stdstages.DicomPixelAnonymizer">DicomPixelAnonymizer</option>
							<option value="org.rsna.ctp.stdstages.anonymizer.dicom.DICOMPixelAnonymizer">DICOMPixelAnonymizer</option>
							<option value="org.rsna.ctp.stdstages.dicom.DicomStorageSCP">DicomStorageSCP</option>
							<option value="org.rsna.ctp.stdstages.dicom.DicomStorageSCU">DicomStorageSCU</option>
							<option value="org.rsna.ctp.stdstages.DirectoryExportService">DirectoryExportService</option>
							<option value="org.rsna.ctp.stdstages.DirectoryImportService">DirectoryImportService</option>
							<option value="org.rsna.ctp.stdstages.FileStorageService">FileStorageService</option>
							<option value="org.rsna.ctp.stdstages.FtpExportService">FtpExportService</option>
							<option value="org.rsna.ctp.stdstages.HttpExportService">HttpExportService</option>
							<option value="org.rsna.ctp.stdstages.HttpImportService">HttpImportService</option>
							<option value="mirc.users.MircUserManagerServlet">MircUserManagerServlet</option>
							<option value="org.rsna.servlets.UserManagerServlet">UserManagerServlet</option>
						</select>
						<br/>
						<input class="text" type="text" id="class" name="class"/>
					</td>
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