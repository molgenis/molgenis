<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[
	"jquery.bootstrap.wizard.css",
	"bootstrap-datetimepicker.min.css",
	"ui.fancytree.min.css",
	"jquery-ui-1.9.2.custom.min.css",
	"select2.css",
	"iThing-min.css",
	"bootstrap-switch.min.css",
	"dataexplorer.css",
	"dataexplorer-filter.css",
	"diseasematcher.css"]>
<#assign js=[
	"jquery-ui-1.9.2.custom.min.js",
	"jquery.bootstrap.wizard.min.js",
	"bootstrap-datetimepicker.min.js",
	"dataexplorer-filter.js",
	"dataexplorer-filter-dialog.js",
	"dataexplorer-filter-wizard.js",
	"jquery.fancytree.min.js",
	"jquery.molgenis.tree.js",
	"select2.min.js",
	"jQEditRangeSlider-min.js",
	"bootstrap-switch.min.js",
	"jquery.molgenis.xrefmrefsearch.js",
	"dataexplorer.js",
	"jquery.molgenis.table.js",
	"handlebars.min.js"]>

<@header css js/>
    <script>
        molgenis.dataexplorer.filter.wizard.setWizardTitle('${wizardtitle}');
   	</script>
    <div id="entity-class" class="well">
			<h3 id="entity-class-name"></h3>
			<span id="entity-class-description"></span>
	</div>
     
    <div class="col-md-4 col-md-offset-8" id="dataset-select-container"<#if hideDatasetSelect??> style="display:none"</#if>>
        <form class="form-horizontal" role="form">
            <div class="form-group">
                <label class="col-md-4 control-label" for="dataset-select">Choose a dataset:</label>
                <div class="col-md-8">
                	<select class="form-control" id="dataset-select" data-placeholder="Choose a Entity (example: dataset, protocol..." id="dataset-select">
                    	<#list entitiesMeta.iterator() as entityMeta>
                        	<option value="/api/v1/${entityMeta.name}" <#if entityMeta.name == selectedEntityName> selected</#if>><#if entityMeta.label?has_content>${entityMeta.label}<#else>${entityMeta.name}</#if></option>
                       	</#list>
                    </select>
               	</div>
         	</div>
     	</form>
   	</div>
     
	<div class="row">
		<div class="col-md-3">
			<div class="well">
				<div class="row">
				    <form role="form">
                        <div class="form-group">
                            <div class="col-md-12 input-group">
                                <input type="text" class="form-control" id="observationset-search" placeholder="Search data values" autofocus="autofocus"<#if searchTerm??> value="${searchTerm}"</#if>>
                                <span class="input-group-btn">
                                    <button class="search-clear-button btn btn-default" type="button"><span class="glyphicon glyphicon-remove"></span></button>
                                    <button class="search-button btn btn-default" type="button"><span class="glyphicon glyphicon-search"></span></button>
                                </span>
                            </div>
                        </div>
                    </form>				
				</div>
				<div class="row">
				    <div class="panel">
                        <div class="panel-heading">Data item filters</div>
                        <div class="panel-body">
                            <div class="row" id="feature-filters"></div>
                            <div class="row">
                                <a href="#" id="filter-wizard-btn" class="btn btn-default btn-small pull-right"><img src="/img/filter-bw.png"> ${wizardbuttontitle}</a>
                            </div>
                        </div>
                    </div>
				</div>
				<div class="row"<#if hideDataItemSelect??> style="display:none"</#if>>
                    <div class="panel">
                        <div class="panel-heading">Data item selection</div>
                        <div class="panel-body">
                            <div id="feature-selection"></div>
                        </div>
                    </div>
                </div>
			</div>		
		</div>
		<div id="module-nav"></div>
	</div>
<@footer/>