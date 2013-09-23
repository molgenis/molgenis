<!DOCTYPE html>
<html>
	<head>
		<title>Login</title>
		<meta charset="utf-8">
		<link rel="icon" href="/img/molgenis.ico" type="image/x-icon">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/molgenis.css" type="text/css">
		<script src="/js/jquery-1.8.3.min.js"></script>
		<script src="/js/bootstrap.min.js"></script>
		<script src="/js/jquery.validate.min.js"></script>
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
