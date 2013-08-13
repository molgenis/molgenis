<!DOCTYPE html>
<html> 
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1"> 
	<meta name="apple-mobile-web-app-capable" content="yes" />
	<meta name="apple-mobile-web-app-status-bar-style" content="black" />
	
	<title>Molgenis Mobile Catalogue</title>

	<link rel="stylesheet" href="/css/jquery.mobile-1.3.1.min.css" /> 
	<link rel="stylesheet" href="/css/jquery.mobile.theme-1.3.1.min.css" />
	<link rel="stylesheet" href="/css/jquery.mobile.structure-1.3.1.min.css" />
	<link rel="stylesheet" href="/css/molgenis-mobile.css" />
	
	<script src="/js/jquery-1.10.2.min.js"></script>
	<script src="/js/jquery.validate.min.js"></script>
	<script src="/js/molgenis-all.js"></script>
	<script src="/js/molgenis-mobile-login.js"></script>
 	<script src="/js/jquery.mobile-1.3.1.min.js"></script>
</head> 

<body> 
	
	<div id="login-page" data-role="page">
		<div data-role="content">
			<div id="logo">
				<img src="/img/logo_molgenis_letterbox.png" />
			</div>
			<form id="login-form">
    			<input id="username" name="username" placeholder="Username" value="" data-clear-btn="true" type="text">
    			<input id="password" name="password" placeholder="Password" value="" data-clear-btn="true" type="password">
    			<input  type="submit" value="login" />
 			</form>
 		</div>
	</div>	
	
	<div id="catalogue-page" data-role="page">
		<div data-role="header" data-position="fixed">
			<h1>Data items</h1>
			<a href="#" data-icon="gear" class="ui-btn-right sign-out">Sign out</a>
		</div>
		<div data-role="content">
			<ul id="protocols" data-inset="true" data-role="listview"></ul>
		</div>
	</div>	
	
</body>

</html>