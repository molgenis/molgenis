<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#include "ontology-match-view-result.ftl">
<#include "ontology-match-new-task.ftl">
<#include "ontology-match-list-tasks.ftl">
<#assign css=["jquery-ui-1.9.2.custom.min.css", "bootstrap-fileupload.min.css", "ui.fancytree.min.css", "ontology-service.css", "biobank-connect.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "bootstrap-fileupload.min.js", "jquery.fancytree.min.js", "common-component.js", "ontology-tree-view.js", "ontology.tree.plugin.js", "ontology-service-result.js", "jquery.bootstrap.pager.js", "simple_statistics.js"]>
<@header css js/>
<form id="ontology-match" class="form-horizontal" enctype="multipart/form-data">
	<div class="row">
		<div class="col-md-12">
			<br>
			<div class="row">
				<div class="col-md-offset-3 col-md-6">
					<legend><center><h3>Ontology Annotator</h3></center></legend>
				</div>
			</div>
			<#if existingTasks??>
				<@listTasks />
			<#elseif ontologies??>
				<@ontologyMatchNewTask />
			<#else>
				<@ontologyMatchResult />
			</#if>
		</div>
	</div>
</form>
<@footer/>	