<!DOCTYPE html>
<html> 
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no"> 
	<meta name="apple-mobile-web-app-capable" content="yes" />
	<meta name="apple-mobile-web-app-status-bar-style" content="black" />
	
	<title>Molgenis Mobile Catalogue</title>

	<link rel="stylesheet" href="/css/jquery.mobile-1.3.1.min.css" /> 
	<link rel="stylesheet" href="/css/jquery.mobile.theme-1.3.1.min.css" />
	<link rel="stylesheet" href="/css/jquery.mobile.structure-1.3.1.min.css" />
	<link rel="stylesheet" href="/css/jquery.loadmask.css" />
	<link rel="stylesheet" href="/css/molgenis-mobile.css" />
	
	<script src="/js/jquery-1.10.2.min.js"></script>
	<script src="/js/jquery.validate.min.js"></script>
	<script src="/js/jquery.loadmask.min.js"></script>
	<script src="/js/molgenis-all.js"></script>
	<script src="/js/molgenis-mobile-login.js"></script>
	<script src="/js/molgenis-mobile-data-items.js"></script>
	<script src="/js/molgenis-mobile-data-item-detail.js"></script>
 	<script src="/js/jquery.mobile-1.3.1.min.js"></script>
</head> 

<body> 
	<div id="login-page" data-role="page">
		<div data-role="content">
			<div id="logo">
				<img src="/img/logo_molgenis_letterbox.png" />
			</div>
			<form id="login-form" method="POST" action="/login">
    			<input id="username" name="username" placeholder="Username" value="" data-clear-btn="true" type="text">
    			<input id="password" name="password" placeholder="Password" value="" data-clear-btn="true" type="password">
    			<input id="submit-button" type="submit" value="login" />
 			</form>
 		</div>
	</div>	
	
	<div id="catalogue-page" data-role="page">
		<div data-role="header" data-position="fixed">
			<h1><span id="feature-count"></span> Data items</h1>
			<a href="#" class="ui-btn-right logout">Logout</a>
		</div>
		<div data-role="content">
			<input type="text" data-type="search" id="search-features" value="" data-mini="true" data-clear-btn="true" placeholder="search..." />
			<ul id="features" data-inset="true" data-role="listview"></ul>
		</div>
	</div>	
	
	<div id="feature-page" data-role="page" data-add-back-btn="true">
		<div data-role="header" data-position="fixed">
			<h1></h1>
		</div>
		<div data-role="content">
			<h3 class="feature-name"></h3>
			<ul data-inset="true" data-role="listview">
				<li>Datatype</li> 
				<li id="feature-datatype" class="feature-detail-value"></li>
			</ul>
			<ul data-inset="true" data-role="listview">
				<li>Description</li>
				<li id="feature-description" class="feature-detail-value"></li>
			</ul>
			<div id="categories"></div>
		</div>
	</div>	
</body>

</html>