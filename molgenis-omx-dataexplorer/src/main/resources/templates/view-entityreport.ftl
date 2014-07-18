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
	"jquery.molgenis.xrefsearch.js",
	"dataexplorer.js",
	"jquery.molgenis.table.js"]>

<@header css js/>

<div>
	<h3>Dataset: ${entityName}</h3>
	<table class="table table-condensed">
		<caption>Entity information</caption>
		<thead>
		<#list entityMap?keys as key>
			<th>${key}</th>
		</#list>		
		</thead>
		<tbody>
			<tr>	
			<#list entityMap?keys as key>
				<td>${entityMap[key]}</td>
			</#list>
			</tr>			
		</tbody>	
	</table>
</div>

<@footer/>