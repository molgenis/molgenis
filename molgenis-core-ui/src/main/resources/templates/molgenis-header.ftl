<#-- write HTML header and plugin menu -->
<#--   css (optional) list of additional stylesheets to include -->
<#--   js  (optional) list of additional js files to include -->
<#macro header css=[] js=[]>
<!DOCTYPE html>
<html>
	<head>
		<title>${molgenis_ui.title?html}</title>
		<meta charset="utf-8">
		<meta http-equiv="X-UA-Compatible" content="chrome=1">
		<link rel="icon" href="/img/molgenis.ico" type="image/x-icon">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/molgenis.css" type="text/css">
	<#list css as css_file_name>
		<link rel="stylesheet" href="/css/${css_file_name?html}" type="text/css">
	</#list>
	<#if molgenis_ui.hrefCss?has_content>
		<link rel="stylesheet" href="/css/${molgenis_ui.hrefCss?html}" type="text/css">
	</#if>
		<script src="/js/jquery-1.8.3.min.js"></script>
		<script src="/js/bootstrap.min.js"></script>
		<script src="/js/jquery.validate.min.js"></script>
		<script src="/js/molgenis.js"></script>
	<#if context_url??>
		<script>top.molgenis.setContextUrl('${context_url}');</script>
	</#if>
		<!--[if lt IE 9]>
			<script src="/js/molgenis-ie8.js"></script>
		<![endif]-->
	<#list js as js_file_name>
		<script src="/js/${js_file_name?html}"></script>
	</#list>		
	<#if molgenis_ui.hrefJs?has_content>
		<script src="/js/${molgenis_ui.hrefJs?html}"></script>
	</#if>
	</head>
	<body>
		<div class="container-fluid">
			<div class="row-fluid">
				<#if menu_id??>
					<#if !(plugin_id??)>
						<#assign plugin_id="NULL">
					</#if>
						<@topmenu molgenis_ui.getMenu() plugin_id/>
					<#if plugin_id?starts_with("form")>
						<@submenu molgenis_ui.getMenu(menu_id) plugin_id/>
					</#if>	
				</#if>			
			</div>
			<div id="login-modal-container-header"></div>
			<div class="row-fluid">
				<div class="datasetsindexerAlerts"></div>
			</div>
			<div class="row-fluid">
				<div class="alerts"><#if errorMessage??>
					<#assign message = errorMessage>
					<#assign messageType = "error"> 
				<#elseif warningMessage??>
					<#assign message = warningMessage>
					<#assign messageType = "warning">
				<#elseif successMessage??>
					<#assign message = successMessage>
					<#assign messageType = "success">
				</#if>
				<#if messageType??>
					<div class="alert alert-${messageType}"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>${messageType?capitalize}!</strong> ${message}</div>
				</#if>
			</div>
		</div>
		<div class="row-fluid">
			<div id="plugin-container" class="container-fluid">
	</#macro>
	<!--topmenu -->
	<#macro topmenu menu plugin_id>
		<#--TODO: put navbar-fixed-top back-->
		<div class="navbar"> 
			<div class="navbar navbar-inner"> 	
				<ul class="nav">
					<li><a><img src="${molgenis_ui.hrefLogo?html}"></img></a></li>
					<#list menu.items as item>
						<#if item.type != "MENU">
							<#if item.id == plugin_id>
								<li class="active"><a href="#">${item.name?html}</a></li>
							<#else>
								<li><a href="/menu/${menu.id?html}/${item.url?html}">${item.name?html}</a></li>
							</#if>
						<#elseif item.type == "MENU">
							<#assign sub_menu = item>
							<li class="dropdown">
								<a class="dropdown-toggle" data-toggle="dropdown" href="#">${item.name?html}<b class="caret"></b></a>
								<ul class="dropdown-menu">
									<#list sub_menu.items as subitem>
										<#if subitem.type != "MENU">
											<li><a href="/menu/${sub_menu.id?html}/${subitem.url?html}">${subitem.name?html}</a></li>
										<#elseif subitem.type == "MENU">
											<li >
												<a tabindex="-1" href="/menu/${subitem.id?html}">${subitem.name?html}</a>
											</li>
										</#if>
									</#list>
								</ul>
							</li>
						</#if>
					</#list>
				</ul>
				<#if authenticated?? && authenticated>
				<form class="navbar-form pull-right" method="post" action="/logout">
					<button type="submit" class="btn btn-inverse btn-link">Sign out</button>
				</form>
				<#else>
				<a class="modal-href pull-right btn btn-inverse btn-link" href="/account/login" data-target="login-modal-container-header">Sign in</a>
				</#if>
			</div>
		</div>
	</#macro>
<!--submenu -->
	<#macro submenu menu plugin_id>
	<div id="submenu">
		<ul id="submenu-menu" class="nav nav-tabs">
			<#list menu.items as item>	
				<#if item.type != "MENU">
					<#if item.id == plugin_id>
						<li class="active"><a href="#">${item.name?html}</a></li>
					<#else>
						<li><a href="/menu/${menu.id?html}/${item.url?html}">${item.name?html}</a></li>
					</#if>
				<#elseif item.type == "MENU">
					<#assign sub_menu = item>
					<li class="dropdown">
						<a class="dropdown-toggle" data-toggle="dropdown" href="#">${item.name?html}<b class="caret"></b></a>
						<ul class="dropdown-menu">
							<#list sub_menu.items as item>
								<#if item.type != "MENU">
									<li><a href="/menu/${sub_menu.id?html}/${item.url?html}">${item.name?html}</a></li>
								<#elseif item.type == "MENU">
									<a tabindex="-1" href="/menu/${item.id?html}">${item.name?html}</a>
								</#if>
							</#list>
						</ul>
					</li>
				</#if>
			</#list>
		</ul>
	<#assign breadcrumb = menu.breadcrumb>
		<#if (breadcrumb?size > 1)>
			<ul id="molgenis-breadcrumb" class="breadcrumb">
				<#list breadcrumb as menu>
					<#if menu_has_next>
						<li><a href="/menu/${menu.id?html}">${menu.name?html}</a> <span class="divider">/</span></li>
					<#else>
						<li class="active">${menu.name?html}</li>
					</#if>	
				</#list>
			</ul>
		</#if>
	</div>
</#macro>


