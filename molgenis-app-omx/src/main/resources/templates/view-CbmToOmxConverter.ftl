<!DOCTYPE html>
<html>
	<head>
		<title>Home plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
        <#if app_href_css??>
            <link rel="stylesheet" href="${app_href_css}" type="text/css">
        </#if>
		<script type="text/javascript" src="/js/molgenis-all.js"></script>
	</head>
	<body>
	
	<form method="post" enctype="multipart/form-data" name="cbmToOmxConverter" action="/plugin/cbmToOmxConverter/convert">
	
	<div class="formscreen">
		<div class="screenbody">
			<div class="screenpadding">	
				<h1>Import xml data</h1>
				<i>Upload Xml file with your data</i>
				<br /><br />
				<label for="upload">Please upload the data:</label>
				<input type="file" class="textbox" id="cbmFile" name="cbmFile"/><br /><br />
				<input type="submit" value="Create omx files" id="upload_xml";return true;"/><br />
			</div>
		</div>
	</form>
	</body>
</html>
