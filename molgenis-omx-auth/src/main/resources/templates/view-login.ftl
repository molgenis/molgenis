<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN">

<html>
<head>
		<link rel="shortcut icon" type="image/x-icon" href="img/molgenis.ico">
		<link rel="stylesheet" href="/css/jquery-ui-1.9.2.custom.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/molgenis-colors.css" type="text/css">
		<link rel="stylesheet" href="/css/molgenis-data.css" type="text/css">
		<!--[if lt IE 8]>
			<link rel="stylesheet" type="text/css" href="css/molgenis-data_ie.css">
		<![endif]-->		
		<link rel="stylesheet" href="/css/molgenis-main.css" type="text/css">
		<link rel="stylesheet" href="/css/molgenis-menu.css" type="text/css">
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
  		<script type="text/javascript" src="/js/jquery-ui-1.9.2.custom.min.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="/js/molgenis-all.js"></script>
		<script type="text/javascript" src="/js/molgenis-menu.js"></script>
		<script type="text/javascript" src="/js/overlib.js"></script>
		<script type="text/javascript" src="/js/jquery.validate.min.js"></script>
	
    <title>Login</title>
</head>

<body>
   <#include "/login-modal.ftl"> 
   <div id="login-modal-container"></div>
   <script type="text/javascript">
     	$(function() {
	  		var modal = $('#login-modal');
	   		modal.show();
	   		});
   </script>
   
   
</body>
</html>
