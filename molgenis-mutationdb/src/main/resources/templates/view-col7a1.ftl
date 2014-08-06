<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[
	"jquery.bootstrap.wizard.css",
	"bootstrap-datetimepicker.min.css",
	"ui.fancytree.min.css",
	"jquery-ui-1.9.2.custom.min.css",
	"jquery.molgenis.table.css",
	"select2.css",
	"iThing-min.css",
	"bootstrap-switch.min.css",
	"dataexplorer.css"]>
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
	"col7a1.js"]>	
<@header css js/>
	<div class="row-fluid">
		<p>
			<div class="pull-left">
				<h3>${title}</h3>
			</div>
			<div class="btn-group pull-right">
				<button id="refresh_button" type="button" class="btn btn-primary btn-large">Generate view</button>
			</div>
		</p>
	</div>
	<div class="molgenis-table-container table-striped" id="table-container">
	</div>
<@footer/>
