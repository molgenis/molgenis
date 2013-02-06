<#-- Factory methods to rapidly create form inputs in MOLGENIS -->
<#macro molgenis_header screen>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
		<title>${screen.getLabel()}</title>
		<link rel="shortcut icon" type="image/x-icon" href="img/molgenis.ico">
		<link rel="stylesheet" href="css/jquery-ui-1.9.2.custom.min.css" type="text/css">
		<link rel="stylesheet" href="css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="css/molgenis-colors.css" type="text/css">
		<link rel="stylesheet" href="css/molgenis-data.css" type="text/css">
		<!--[if lt IE 8]>
			<link rel="stylesheet" type="text/css" href="css/molgenis-data_ie.css">
		<![endif]-->		
		<link rel="stylesheet" href="css/molgenis-main.css" type="text/css">
		<link rel="stylesheet" href="css/molgenis-menu.css" type="text/css">
		<script type="text/javascript" src="js/jquery-1.8.3.min.js"></script>
  		<script type="text/javascript" src="js/jquery-ui-1.9.2.custom.min.js"></script>
		<script type="text/javascript" src="js/bootstrap.min.js"></script>
		<script type="text/javascript" src="js/molgenis-all.js"></script>
		<script type="text/javascript" src="js/molgenis-menu.js"></script>
		<script type="text/javascript" src="js/overlib.js"></script>
		<#if screen.controller??>${screen.controller.getCustomHtmlHeaders()}</#if>
	</head>
	<#if applicationHtmlError?exists>${applicationHtmlError}</#if>
</#macro>
<#macro molgenis_footer>
	</body>
</html>
</#macro>