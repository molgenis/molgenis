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
		<link rel="stylesheet" href="<@resource_href "/css/bootstrap-yamm.css"/>" type="text/css">
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
		<<script src="<@resource_href "/js/bootstrap-hover-dropdown.min.js"/>"></script>
	<#if context_url??>
		<script>top.molgenis.setContextUrl('${context_url}');</script>
	</#if>
		<#--[if lt IE 9]>
			<script src="<@resource_href "/js/molgenis-ie8.js"/>"></script>
		<#[endif]-->
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
            
            <#if plugin_id?starts_with("form")>
				<@breadcrumb molgenis_ui.getMenu(menu_id) plugin_id/>
			</#if>	
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
							<div class="alert alert-${messageType}"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>${messageType?capitalize}!</strong> ${message}</div>
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
	<div class="navbar yamm navbar-default navbar-fixed-top" role="navigation">
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
										<a href="/menu/${menu.id?html}/${item.url?html}">${item.name?html}</a>
									</li>
								</#if>
							</#if>
							
						<#-- Dropdown menu items -->
						<#elseif item.type == "MENU">
							<#assign sub_menu = item>
							<#-- Root dropdown toggle -->
							<li class="dropdown">
								<a class="dropdown-toggle" data-hover="dropdown" data-delay="0" data-toggle="dropdown" href="#">
									${item.name?html}<b class="caret"></b>
								</a>
								
								<ul class="dropdown-menu" id="first-dropdown-menu" role="menu">
									<#list sub_menu.items as first_tier_subitem>
										<#if first_tier_subitem.type != "MENU">
											<li>
												<a href="/menu/${sub_menu.id?html}/${first_tier_subitem.url?html}">${first_tier_subitem.name?html}</a>
											</li>
										<#elseif first_tier_subitem.type == "MENU">
											<#-- Second dropdown toggle -->
											<li class="dropdown">
												<a class="dropdown-toggle" data-hover="dropdown" data-delay="200" data-toggle="dropdown" data-close-others="false" href="#">
													${first_tier_subitem.name?html}<b class="caret"></b>
												</a>
												
												<ul class="dropdown-menu drop-right" id="second-dropdown-menu" role="menu">	
													<#list first_tier_subitem.items as second_tier_subitem>
														<#if second_tier_subitem.type != "MENU">
														<li>
															<a tabindex="-1" href="/menu/${first_tier_subitem.id?html}/${second_tier_subitem.url?html}">${second_tier_subitem.name?html}</a>
														<li>
														<#elseif second_tier_subitem.type == "MENU">
															<#-- third dropdown toggle -->
															<li class="dropdown">
																<a class="dropdown-toggle" data-hover="dropdown" data-delay="200" data-close-others="false" data-toggle="dropdown" href="#">
																	${second_tier_subitem.name?html}<b class="caret"></b>
																</a>
											
																<ul class="dropdown-menu" id="third-dropdown-menu" role="menu">						
																	<#list second_tier_subitem.items as third_tier_subitem> 
																		<li>
																			<a tabindex="-1" href="/menu/${second_tier_subitem.id?html}/${third_tier_subitem.url?html}">${third_tier_subitem.name?html}</a>
																		</li>
																	</#list>															
																</ul>
															</li>
														</#if>
													</#list>
												</ul>
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
			<#-- Navbar items end -->
			
		</div> <#-- close container -->
	</div> <#-- close navbar div -->
	
	<#-- This script will active Triple level multi drop-down menus in Bootstrap 3 -->
	<script type="text/javascript">
		$('ul.dropdown-menu [data-toggle=dropdown]').on('click', function(event) {
		    // Avoid following the href location when clicking
		    event.preventDefault(); 
		    // Avoid having the menu to close when clicking
		    event.stopPropagation(); 
		    // Re-add .open to parent sub-menu item
		    $(this).parent().addClass('open');
		    $(this).parent().find("ul").parent().find("li.dropdown").addClass('open');
		});		
	</script>
</#macro>

<#-- breadcrumb -->
<#macro breadcrumb menu plugin_id>
	<div id="form-breadcrumb">
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
