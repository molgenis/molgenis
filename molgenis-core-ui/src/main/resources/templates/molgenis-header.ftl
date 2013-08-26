<#-- write HTML header with plugin menu -->
<#--   css (optional) list of additional stylesheets to include -->
<#--   js  (optional) list of additional js files to include -->
<#macro header css=[] js=[]>
<!DOCTYPE html>
<html>
	<head>
		<title>${molgenis_ui.title?html}</title>
		<meta charset="utf-8">
		<link rel="shortcut icon" type="image/x-icon" href="/img/molgenis.ico">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
	<#list css as css_file_name>
		<link rel="stylesheet" href="/css/${css_file_name?html}" type="text/css">
	</#list>
	<#if molgenis_ui.hrefCss?has_content>
		<link rel="stylesheet" href="/css/${molgenis_ui.hrefCss?html}" type="text/css">
	</#if>
		<script src="/js/jquery-1.8.3.min.js"></script>
		<script src="/js/bootstrap.min.js"></script>
		<script src="/js/jquery.validate.min.js"></script>
		<script src="/js/molgenis-all.js"></script>
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
				<a href="/"><img src="${molgenis_ui.hrefLogo?html}"></a>
			</div>
			<div class="row-fluid">
				<div id="login-modal-container-header"></div>
				<div class="pull-right">
				<#if !authenticated>
					<a class="modal-href" href="/account/login" data-target="login-modal-container-header">login/register</a>
				<#else>
					<a href="/account/logout">logout</a>
				</#if>
				</div>
	<#assign breadcrumb = [molgenis_ui.menu]>
	<@molgenis_menu molgenis_ui.menu plugin_id breadcrumb/>
			</div>
</#macro>

<#macro molgenis_menu menu plugin_id breadcrumb>
	<#list menu.items as item>
		<#if item.type != "MENU" && item.id == plugin_id>
			<@build_menu menu plugin_id breadcrumb/>
			<#return>
		</#if>
	</#list>
	<#list menu.items as item>
		<#if item.type == "MENU">
			<#assign sub_menu = item>
			<#assign breadcrumb_new = breadcrumb + [sub_menu] />
			<@molgenis_menu sub_menu plugin_id breadcrumb_new/>
		</#if>
	</#list>
</#macro>

<#macro build_menu menu plugin_id breadcrumb>
				<ul class="nav nav-tabs">
	<#list menu.items as item>
		<#if item.type != "MENU">
			<#if item.id == plugin_id>
					<li class="active"><a href="#">${item.name?html}</a></li>
			<#else>
					<li><a href="/plugin/${item.id?html}">${item.name?html}</a></li>
			</#if>
		<#elseif item.type == "MENU">
			<#assign sub_menu = item>
					<li class="dropdown">
						<a class="dropdown-toggle" data-toggle="dropdown" href="#">${item.name?html}<b class="caret"></b></a>
						<ul class="dropdown-menu">
			<#list sub_menu.items as item>
				<#if item.type != "MENU">
							<li><a href="/plugin/${item.id?html}">${item.name?html}</a></li>
				<#elseif item.type == "MENU">
							<li><a href="/plugin/${item.activeItem.id?html}">${item.name?html}</a></li>
				</#if>
			</#list>
						</ul>
					</li>
		</#if>
	</#list>
				</ul>
	<#if breadcrumb?size != 1>				
				<ul class="breadcrumb">
		<#list breadcrumb as menu>
			<#if menu_has_next>
				<#if menu.activeItem??>
					<li><a href="/plugin/${menu.activeItem.id?html}">${menu.name?html}</a> <span class="divider">/</span></li>
				<#else>
					<li>${menu.name?html} <span class="divider">/</span></li>
				</#if>
			<#else>
					<li class="active">${menu.name?html}</li>
			</#if>	
		</#list>
				</ul>
	</#if>
</#macro>
