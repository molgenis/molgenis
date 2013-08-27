<#if enable_spring_ui>
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
	<div class="row-fluid">${app_home_html}</div>
<@footer/>
<#else>
<!DOCTYPE html>
<html>
	<head>
		<title>Home plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
        <#if app_href_css??>
            <link rel="stylesheet" href="${app_href_css}" type="text/css">
        </#if>
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="/js/molgenis-all.js"></script>
	</head>
	<body>
		<div class="container-fluid">
			<div class="row-fluid">${app_home_html}</div>
		</div>
	</body>
</html>
</#if>