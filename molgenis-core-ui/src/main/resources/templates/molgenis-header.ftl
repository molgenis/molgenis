
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
        
	<#-- Bundle of third party CSS resources used by MOLGENIS: see minify-maven-plugin in molgenis-core-ui/pom.xml for bundle contents -->
        <link rel="stylesheet" href="<@resource_href "/css/molgenis-bundle.min.css"/>" type="text/css">
    
	<#if environment == "development">
        <link rel="stylesheet" href="<@resource_href "/css/component/wrapper/DateTimePicker.css"/>" type="text/css">
        <link rel="stylesheet" href="<@resource_href "/css/component/wrapper/JQRangeSlider.css"/>" type="text/css">
        <link rel="stylesheet" href="<@resource_href "/css/component/Checkbox.css"/>" type="text/css">
        <link rel="stylesheet" href="<@resource_href "/css/component/Radio.css"/>" type="text/css">
        <link rel="stylesheet" href="<@resource_href "/css/component/Table.css"/>" type="text/css">
    <#else>
        <link rel="stylesheet" href="<@resource_href "/css/molgenis-component.min.css"/>" type="text/css">
    </#if>
        
        <link rel="stylesheet" href="<@resource_href "/css/molgenis.css"/>" type="text/css">
    
    <#-- CSS selection, configurable based on RTP setting -->
	<#if molgeniscsstheme??>		
 		<link id="bootstrap-theme" rel="stylesheet" href="<@resource_href "/css/themes/${molgeniscsstheme}"/>" type="text/css">	
    <#else>
		<link rel="stylesheet" href="<@resource_href "/css/themes/bootstrap-molgenis.min.css"/>" type="text/css">
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
    
        <#-- Bundle of third party JavaScript resources used by MOLGENIS: see minify-maven-plugin in molgenis-core-ui/pom.xml for bundle contents -->
		<script src="<@resource_href "/js/es6-promise.min.js"/>"></script>
		<script src="<@resource_href "/js/promise-done-6.1.0.min.js"/>"></script>
		<script src="<@resource_href "/js/promise-done-6.1.0.min.js"/>"></script>
        <script src="<@resource_href "/js/molgenis-bundle.min.js"/>"></script>
        <script src="<@resource_href "/js/jquery.validate.min.js"/>"></script>
        <script src="<@resource_href "/js/handlebars.min.js"/>"></script>
        <script src="<@resource_href "/js/molgenis.js"/>"></script>
        <script src="<@resource_href "/js/molgenis-script-evaluator.js"/>"></script>
    <#if environment == "development">
        <#-- Important: Update minify-maven-plugin configuration in molgenis-core-ui/pom.xml when modifying the list below -->
        <script src="<@resource_href "/js/react-with-addons.js"/>"></script>
        <script src="<@resource_href "/js/component/mixin/AttributeLoaderMixin.js"/>"></script>
        <script src="<@resource_href "/js/component/mixin/DeepPureRenderMixin.js"/>"></script>
        <script src="<@resource_href "/js/component/mixin/EntityInstanceLoaderMixin.js"/>"></script>
        <script src="<@resource_href "/js/component/mixin/EntityLoaderMixin.js"/>"></script>
        <script src="<@resource_href "/js/component/mixin/GroupMixin.js"/>"></script>
        <script src="<@resource_href "/js/component/mixin/ReactLayeredComponentMixin.js"/>"></script>
        <script src="<@resource_href "/js/component/wrapper/Ace.js"/>"></script>
        <script src="<@resource_href "/js/component/wrapper/DateTimePicker.js"/>"></script>
        <script src="<@resource_href "/js/component/wrapper/JQRangeSlider.js"/>"></script>
        <script src="<@resource_href "/js/component/wrapper/JQueryForm.js"/>"></script>
        <script src="<@resource_href "/js/component/wrapper/Select2.js"/>"></script>
        <script src="<@resource_href "/js/component/AlertMessage.js"/>"></script>
        <script src="<@resource_href "/js/component/AttributeControl.js"/>"></script>
        <script src="<@resource_href "/js/component/BoolControl.js"/>"></script>
        <script src="<@resource_href "/js/component/Button.js"/>"></script>
        <script src="<@resource_href "/js/component/CheckboxGroup.js"/>"></script>
        <script src="<@resource_href "/js/component/CodeEditor.js"/>"></script>
        <script src="<@resource_href "/js/component/DateControl.js"/>"></script>
        <script src="<@resource_href "/js/component/Dialog.js"/>"></script>
        <script src="<@resource_href "/js/component/EntitySelectBox.js"/>"></script>
        <script src="<@resource_href "/js/component/Form.js"/>"></script>
        <script src="<@resource_href "/js/component/FormControl.js"/>"></script>
        <script src="<@resource_href "/js/component/FormControlGroup.js"/>"></script>
        <script src="<@resource_href "/js/component/Icon.js"/>"></script>
        <script src="<@resource_href "/js/component/Input.js"/>"></script>
        <script src="<@resource_href "/js/component/Modal.js"/>"></script>
        <script src="<@resource_href "/js/component/Pager.js"/>"></script>
        <script src="<@resource_href "/js/component/Popover.js"/>"></script>
        <script src="<@resource_href "/js/component/Questionnaire.js"/>"></script>
        <script src="<@resource_href "/js/component/RadioGroup.js"/>"></script>
        <script src="<@resource_href "/js/component/RangeSlider.js"/>"></script>
        <script src="<@resource_href "/js/component/Spinner.js"/>"></script>
        <script src="<@resource_href "/js/component/Table.js"/>"></script>
        <script src="<@resource_href "/js/component/TextArea.js"/>"></script>
    <#else>
        <script src="<@resource_href "/js/react-with-addons.min.js"/>"></script>
        <script src="<@resource_href "/js/molgenis-component.min.js"/>"></script>
    </#if>
        
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
	<#if app_tracking_code.footer?has_content || app_tracking_code.header?has_content>
		<script src="<@resource_href "/js/jquery.cookie.js"/>"></script>
		<script src="<@resource_href "/js/molgenis-head-cookies.js"/>"></script>
	</#if>
	<#if app_tracking_code.header?has_content>
		<script id="app-tracking-code-header" type="text/javascript">${app_tracking_code.header?string}</script>
	</#if>
	</head>
	<body>
		<#-- Navbar menu -->
        <#if menu_id??>
            <#if !(plugin_id??)>
                <#assign plugin_id="NULL">
            </#if>
            
            <@topmenu molgenis_ui.getMenu() plugin_id pluginid_with_query_string/>
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
<#--TODO refactor to remove depency on 'Home'-->
<#macro topmenu menu plugin_id pluginid_with_query_string> 
	<nav class="navbar navbar-default navbar-fixed-top" role="navigation">
        <div class="container-fluid">
			<div class="navbar-header">
        		<#-- Logo start -->
	    		<#if app_top_logo?has_content>
    	    		<div id="Intro">
        	    		<img src=${app_top_logo} alt="" border="0" height="150">
        			</div>
    			</#if>
            
            	<#list menu.items as item> 
                	<#if item.type != "MENU" && item.name == "Home" && app_home_logo?has_content>
 						<a class="navbar-brand" href="/menu/${menu.id?html}/${item.url?html}">
		                	<img class="img-responsive" style="max-width:100%;max-height:100%;"
	                		src="<#if molgenis_ui.hrefLogo?has_content>${molgenis_ui.hrefLogo?html}<#else><@resource_href "/img/logo_molgenis_small.png"/></#if>" 
	                		alt="<#if molgenis_ui.title?has_content>${molgenis_ui.title?html}</#if>">
                		</a>						
						<button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-molgenis-navbar">
		                    <span class="sr-only">Toggle navigation</span>
		                    <span class="icon-bar"></span>
        					<span class="icon-bar"></span>
        					<span class="icon-bar"></span>
		                </button>
	       			</#if>
    			</#list>
    			<#-- Logo end -->
			</div>
    		
    		<#-- Navbar items start -->
        	<div class="collapse navbar-collapse" id="bs-molgenis-navbar">
				<ul class="nav navbar-nav">
					<#list menu.items as item>
						
						<#-- Single menu items -->
						<#if item.type != "MENU">	
							<#if item.name != "Home" || !app_home_logo?has_content>
								<#if item.url == pluginid_with_query_string>
									<li class="active"><a href="#">${item.name?html}</a></li>
								<#else>
									<li><a href="/menu/${menu.id?url('UTF-8')}/${item.url?html}">${item.name?html}</a></li>
								</#if>
							</#if>
							
						<#-- Dropdown menu items -->
						<#elseif item.type == "MENU">
							<#assign sub_menu = item>
							<#assign menu_counter = 0>
							<li class="dropdown">
								<a class="dropdown-toggle" data-toggle="dropdown" href="#" role="button" aria-expanded="false">${item.name?html}<b class="caret"></b></a>
								
								<ul class="dropdown-menu" role="menu">
									<@dropdown sub_menu menu_counter />	
								</ul>
							</li>
						</#if>
					</#list>
				</ul>
				
				<#if authenticated?? && authenticated>
					<form class="navbar-form navbar-right" method="post" action="/logout">
						<button id="signout-button" type="submit" class="btn btn-primary">Sign out</button>
					</form>
				<#else>
					<form class="navbar-form navbar-right" method="post" action="/login">
						<a id="open-button" type="btn" class="btn btn-default modal-href" href="/account/login" data-target="login-modal-container-header">Sign in</a>
					</form>
				</#if>
			</div>
			<#-- Navbar items end -->
			
		</div> <#-- close container -->
	</nav> <#-- close navbar -->
</#macro>

<#-- dropdown for entity -->
<#macro dropdown sub_menu menu_counter>
    <#assign this_menu_counter = menu_counter + 1>
    
    <#list sub_menu.items as sub_item>
        <#if sub_item.type != "MENU">
            <li>
                <a <#if this_menu_counter gt 1>style="margin-left: ${this_menu_counter * 12}px;"</#if> href="/menu/${sub_menu.id?url('UTF-8')}/${sub_item.url?html}">${sub_item.name?html}</a>
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