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
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<meta http-equiv="X-UA-Compatible" content="chrome=1">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel="icon" href="<@resource_href "/img/molgenis.ico"/>" type="image/x-icon">
		<link rel="stylesheet" href="<@resource_href "/css/bootstrap.min.css"/>" type="text/css">
        <link rel="stylesheet" href="<@resource_href "/css/molgenis.css"/>" type="text/css">
        <#if molgeniscsstheme??>
            <link rel="stylesheet" href="<@resource_href "/css/${molgeniscsstheme}"/>" type="text/css">
        </#if>
    <#if app_top_logo?has_content>
        <link rel="stylesheet" href="<@resource_href "/css/molgenis-top-logo.css"/>" type="text/css">
    </#if>
	<#list css as css_file_name>
		<link rel="stylesheet" href="<@resource_href "/css/${css_file_name?html}"/>" type="text/css">
	</#list>
	<#if molgenis_ui.hrefCss?has_content>
		<link rel="stylesheet" href="<@resource_href "/css/${molgenis_ui.hrefCss?html}"/>" type="text/css">
	</#if>
    <!--[if lt IE 9]>
        <script src="<@resource_href "/js/molgenis-ie8.js"/>"></script>
    <![endif]-->
		<script src="<@resource_href "/js/jquery-2.1.1.min.js"/>"></script>
		<script src="<@resource_href "/js/bootstrap.min.js"/>"></script>
		<script src="<@resource_href "/js/jquery.validate.min.js"/>"></script>
		<script src="<@resource_href "/js/handlebars.min.js"/>"></script>
		<script src="<@resource_href "/js/molgenis.js"/>"></script>
    <!--[if IE 9]>
        <#-- used to disable the genomebrowser in IE9 -->
        <script>top.molgenis.ie9 = true;</script>
        <#-- required by dalliance-compiled.js to load the genomebrowsers in IE9 -->        
        <script src="<@resource_href "/js/typedarray.min.js"/>"></script>
    <![endif]-->		
	<#if context_url??>
		<script>top.molgenis.setContextUrl('${context_url?js_string}');</script>
	</#if>
	<#list js as js_file_name>
		<script src="<@resource_href "/js/${js_file_name?html}"/>"></script>
	</#list>		
	<#if molgenis_ui.hrefJs?has_content>
		<script src="<@resource_href "/js/${molgenis_ui.hrefJs?html}"/>"></script>
	</#if>
	</head>
	<body>
		<#-- Navbar menu -->
        <#if menu_id??>
            <#if !(plugin_id??)>
                <#assign plugin_id="NULL">
            </#if>
            
            <@topmenu molgenis_ui.getMenu() plugin_id/>
        </#if>
        
		<#-- Start application content -->
        <div class="container-fluid">
			<div class="row">
			    <div class="col-md-12">
                    <div id="login-modal-container-header"></div>		
				</div>
			</div>
			
			<div class="row">
			    <div class="col-md-12">
                    <div class="datasetsindexerAlerts"></div>
                </div>	
			</div>
			
			<div class="row">
                <div class="col-md-12">
					<div class="alerts">
						<#if errorMessage??>
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
							<div class="alert alert-${messageType}"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>${messageType?capitalize}!</strong> ${message?html}</div>
						</#if>
				   </div>
                </div>
			</div>
			
			<div class="row">
                <div class="col-md-12">
                    <div id="plugin-container">
</#macro>


<#-- Topmenu -->
<#macro topmenu menu plugin_id> <#--TODO refactor to remove depency on 'Home'-->
    <#if app_top_logo?has_content>
        <div id="Intro">
            <img src=${app_top_logo} alt="" border="0" height="150">
        </div>
    </#if>
	<div class="navbar navbar-default navbar-fixed-top" role="navigation">
		<div class="container-fluid">
			<#-- Logo start -->
            <#list menu.items as item> 
                <#if item.type != "MENU" && item.name == "Home"> 
					<div class="navbar-header">
						<button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-molgenis-navbar">
		                    <span class="sr-only">Toggle navigation</span>
		                    <span class="icon-bar"></span>
        					<span class="icon-bar"></span>
        					<span class="icon-bar"></span>
		                </button>
		                <a class="navbar-brand" href="/menu/${menu.id?html}/${item.url?html}">
		                	<img class="img-responsive" 
	                			src="<#if molgenis_ui.hrefLogo?has_content>${molgenis_ui.hrefLogo?html}<#else><@resource_href "/img/logo_molgenis_small.png"/></#if>" 
	                			alt="<#if molgenis_ui.title?has_content>${molgenis_ui.title?html}</#if>">
                		</a>
					</div>
        		</#if>
    		</#list>
    		<#-- Logo end -->
    		
    		<#-- Navbar items start -->
        	<div class="navbar-collapse collapse" id="bs-molgenis-navbar">
				<ul class="nav navbar-nav">
					<#list menu.items as item>
						
						<#-- Single menu items -->
						<#if item.type != "MENU">	
							<#if item.name != "Home">
								<#if item.id == plugin_id>
									<li class="active">
										<a href="#">${item.name?html}</a>
									</li>
								<#else>
									<li>
										<a href="/menu/${menu.id?url('UTF-8')}/${item.url}">${item.name?html}</a>
									</li>
								</#if>
							</#if>
							
						<#-- Dropdown menu items -->
						<#elseif item.type == "MENU">
							<#assign sub_menu = item>
							<#assign menu_counter = 0>
							<li class="dropdown">
								<a class="dropdown-toggle" data-toggle="dropdown" href="#">
									${item.name?html}<b class="caret"></b>
								</a>
								
								<ul class="dropdown-menu" role="menu">
									<@dropdown sub_menu menu_counter />	
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
			<#-- Navbar items end -->
			
		</div> <#-- close container -->
	</div> <#-- close navbar div -->
</#macro>

<#-- dropdown for entity -->
<#macro dropdown sub_menu menu_counter>
	<#assign this_menu_counter = menu_counter + 1>
	
	<#list sub_menu.items as sub_item>
		<#if sub_item.type != "MENU">
			<li>
				<a <#if this_menu_counter gt 1>style="margin-left: ${this_menu_counter * 12}px;"</#if> href="/menu/${sub_menu.id?url('UTF-8')}/${sub_item.url?url('UTF-8')}">${sub_item.name?html}</a>
			</li>
		<#elseif sub_item.type == "MENU">
			<li class="dropdown-header disabled sub-menu-${this_menu_counter}" role="presentation">
				<a <#if this_menu_counter gt 1>style="margin-left: ${this_menu_counter * 12}px;"</#if> href="#">${sub_item.name?html}</a>
			</li>
			
			<@dropdown sub_item this_menu_counter />
			<#assign this_menu_counter = this_menu_counter - 1>
		</#if>
	</#list>
</#macro>