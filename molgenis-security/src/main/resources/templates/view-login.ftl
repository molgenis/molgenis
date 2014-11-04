<#include "resource-macros.ftl">
<!DOCTYPE html>
<html>
	<head>
		<title>Login</title>
		<meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel="icon" href="<@resource_href "/img/molgenis.ico"/>" type="image/x-icon">
		<link rel="stylesheet" href="<@resource_href "/css/bootstrap.min.css"/>" type="text/css">
		<link rel="stylesheet" href="<@resource_href "/css/molgenis.css"/>" type="text/css">
		<script src="<@resource_href "/js/jquery-1.8.3.min.js"/>"></script>
		<script src="<@resource_href "/js/bootstrap.min.js"/>"></script>
		<script src="<@resource_href "/js/jquery.validate.min.js"/>"></script>
		<script src="<@resource_href "/js/molgenis.js"/>"></script>
	</head>
	<body>
	<#include "/login-modal.ftl">
		<script type="text/javascript">
			$(function() {
		  		$('#login-modal').modal();
		  	<#if errorMessage??>
		  		$('#loginPassword').after($('<p class="text-error">${errorMessage}</p>'));
		  	</#if>
	   		});
	   </script>
	</body>
</html>	
