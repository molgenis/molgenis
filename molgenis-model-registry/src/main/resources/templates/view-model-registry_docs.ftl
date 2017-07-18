<!DOCTYPE html>
<html>
<head>
    <title><#if molgenis_ui.title?has_content>${molgenis_ui.title?html}</#if></title>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1">
<#-- FIXME enable resource_href>
<#--<link rel="icon" href="<@resource_href "/img/molgenis.ico"/>" type="image/x-icon">-->
<#--<link rel="stylesheet" href="<@resource_href "/css/bootstrap.min.css"/>" type="text/css">-->
    <link rel="icon" href="/img/favicon.ico" type="image/x-icon">
    <link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
    <link rel="stylesheet" href="/css/model-registry.css" type="text/css">
    <link rel="stylesheet" href="/css/molgenis.css" type="text/css">
<#--<script src="<@resource_href "/js/jquery-2.1.1.min.js"/>"></script>-->
<#--<script src="<@resource_href "/js/bootstrap.min.js"/>"></script>-->
    <script src="/js/jquery-2.1.1.min.js"></script>
    <script src="/js/bootstrap.min.js"></script>
    <script src="/js/model-registry-doc.js"></script>
</head>
<body>
<#include "view-model-registry_docs-macros.ftl">
<div class="col-md-12 hidden-print">
    <button id="print-doc-btn" class="btn btn-default btn-md pull-right"><span class="glyphicon glyphicon-print"></span>
    </button>
</div>
<div class="row">
    <div class="col-md-8 col-md-offset-2">
        <div class="package-index-container">
            <div id="package-index"><#-- for back-to-top -->
                <h1 id="package-index-title" class="page-header">Model documentation</h1>
                <ul>
                <#list packages as package>
                                <@createPackageListItem package/>
                            </#list>
                </ul>

            <#list packages as package>
                <@renderPackage package/>
            </#list>
            </div>
        </div>
    </div>
</div>
</body>
</html>
