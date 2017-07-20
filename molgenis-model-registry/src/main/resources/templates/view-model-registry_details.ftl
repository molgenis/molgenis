<#include "view-model-registry_docs-macros.ftl">
<#include "resource-macros.ftl">

<div class="row">
    <div class="col-md-12 hidden-print">
        <button id="search-results-back-btn" class="btn btn-default btn-md pull-left"><span
                class="glyphicon glyphicon-chevron-left"></span> Back to search results
        </button>
        <button id="print-btn" class="btn btn-default btn-md pull-right"><span class="glyphicon glyphicon-print"></span>
        </button>
    </div>
</div>
<div class="row">
    <div class="col-md-12 hidden-print">
        <ul class="nav nav-tabs" role="tablist">
            <li class="active"><a href="#tree" id="tree-tab" role="tab" data-toggle="tab">Tree</a></li>
            <li><a href="#uml" id="uml-tab" role="tab" data-toggle="tab">UML</a></li>
        </ul>
    </div>
</div>
<div class="tab-content">
    <div class="tab-pane active" id="tree">
        <div class="row">
            <div class="col-md-3 hidden-print">
                <div class="well">
                    <div id="package-tree-container" class="panel">
                        <div class="panel-heading">
                            <h4 class="panel-title clearfix">Data item selection</h4>
                        </div>
                        <div class="panel-body">
                            <div id="attribute-selection"></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-9">
                <div id="package-doc-container">
                    <div id="package-index"><#-- for back-to-top -->
                    <#if package??>
                        <@renderPackage package/>
                    <#else>
                        <script language="javascript">
                            window.location = molgenis.getContextUrl() + '?showPackageNotFound=true';
                        </script>
                    </#if>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="tab-pane" id="uml">
        <div id="model-registry-uml"></div>
    </div>
</div>


