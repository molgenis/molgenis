<#-- write HTML header and plugin menu -->
<#--   css (optional) list of additional stylesheets to include -->
<#--   js  (optional) list of additional js files to include -->
<#include "resource-macros.ftl">
<#macro header css=[] js=[]>
<!DOCTYPE html>
<html>
	<head>
		<title><#if molgenis_ui.title?has_content>${molgenis_ui.title?html}</#if></title>
		<meta charset="utf-8">
		<meta http-equiv="X-UA-Compatible" content="chrome=1">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel="icon" href="<@resource_href "/img/molgenis.ico"/>" type="image/x-icon">
		<link rel="stylesheet" href="<@resource_href "/css/bootstrap.min.css"/>" type="text/css">
		<link rel="stylesheet" href="<@resource_href "/css/molgenis.css"/>" type="text/css">
	<#list css as css_file_name>
		<link rel="stylesheet" href="<@resource_href "/css/${css_file_name?html}"/>" type="text/css">
	</#list>
	<#if molgenis_ui.hrefCss?has_content>
		<link rel="stylesheet" href="<@resource_href "/css/${molgenis_ui.hrefCss?html}"/>" type="text/css">
	</#if>
		<script src="<@resource_href "/js/jquery-1.8.3.min.js"/>"></script>
		<script src="<@resource_href "/js/bootstrap.min.js"/>"></script>
		<script src="<@resource_href "/js/jquery.validate.min.js"/>"></script>
		<script src="<@resource_href "/js/molgenis.js"/>"></script>
	<#if context_url??>
		<script>top.molgenis.setContextUrl('${context_url}');</script>
	</#if>
		<!--[if lt IE 9]>
			<script src="<@resource_href "/js/molgenis-ie8.js"/>"></script>
		<![endif]-->
	<#list js as js_file_name>
		<script src="<@resource_href "/js/${js_file_name?html}"/>"></script>
	</#list>		
	<#if molgenis_ui.hrefJs?has_content>
		<script src="<@resource_href "/js/${molgenis_ui.hrefJs?html}"/>"></script>
	</#if>
	</head>
	<body>
		<div id="wrap">
			<div class="container-fluid">
				<div class="row">
					<#if menu_id??>
						<#if !(plugin_id??)>
							<#assign plugin_id="NULL">
						</#if>
						<@topmenu molgenis_ui.getMenu() plugin_id/>
					</#if>			
				</div>
				<div id="login-modal-container-header"></div>
				<div class="row">
					<div class="datasetsindexerAlerts"></div>
				</div>
				<div class="row">
					<div class="alerts"><#if errorMessage??>
						<#assign message = errorMessage>
						<#assign messageType = "error"> 
					<#elseif warningMessage??>
						<#assign message = warningMessage>
						<#assign messageType = "warning">
					<#elseif successMessage??>
						<#assign message = successMessage>
						<#assign messageType = "success">
					<#elseif infoMessage??>
						<#assign message = infoMessage>
						<#assign messageType = "info">
					</#if>
					<#if messageType??>
						<div class="alert alert-${messageType}"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>${messageType?capitalize}!</strong> ${message}</div>
					</#if>
				</div>
			</div>
			<div class="row">
				<div id="plugin-container" class="container-fluid">
	</#macro>
	<#--topmenu -->
	<#macro topmenu menu plugin_id>
        <nav class="navbar navbar-default navbar-static-top" role="navigation">
			<div class="container-fluid">
                <#-- TODO refactor to remove depency on 'Home' -->
                <#list menu.items as item>
                    <#if item.type != "MENU" && item.name == "Home">
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-molgenis-navbar">
                        <span class="sr-only">Toggle navigation</span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <a class="navbar-brand" href="/menu/${menu.id?html}/${item.url?html}"><img class="img-responsive" src="<#if molgenis_ui.hrefLogo?has_content>${molgenis_ui.hrefLogo?html}<#else><@resource_href "/img/logo_molgenis_small.png"/></#if>" alt="<#if molgenis_ui.title?has_content>${molgenis_ui.title?html}</#if>"></a>
                </div>
                        <#break>
                    </#if>
                </#list>
                <div class="collapse navbar-collapse" id="bs-molgenis-navbar">
    				<ul class="nav navbar-nav">
    					<#list menu.items as item>
    						<#if item.type != "MENU">
                                <#-- TODO refactor to remove depency on 'Home' -->
    							<#if item.name != "Home">
    								<#if item.id == plugin_id>
    									<li class="active"><a href="#">${item.name?html}</a></li>
    								<#else>
    									<li><a href="/menu/${menu.id?html}/${item.url?html}">${item.name?html}</a></li>
    								</#if>
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
    											<li>
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
    				<form class="navbar-form navbar-right" method="post" action="/logout">
    					<button type="submit" class="btn btn-inverse btn-link">Sign out</button>
    				</form>
    				<#else>
    				<a class="modal-href btn btn-default navbar-btn navbar-right" href="/account/login" data-target="login-modal-container-header">Sign in</a>
    				</#if>
				</div>
			</div>
		</nav>
</#macro>