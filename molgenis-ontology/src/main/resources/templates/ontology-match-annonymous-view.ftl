<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#include "ontology-match-new-task.ftl">
<#assign css=["jquery-ui-1.9.2.custom.min.css", "bootstrap-fileupload.min.css", "ontology-service.css", "biobank-connect.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "bootstrap-fileupload.min.js", "sorta-result-anonymous.js"]>
<@header css js/>
<form id="ontology-match" class="form-horizontal">
	<br>
	<div class="row">
		<div class="col-md-offset-3 col-md-6">
			<legend><center><strong>SORTA</strong> - <strong>S</strong>ystem for <strong>O</strong>ntology-based <strong>R</strong>e-coding and <strong>T</strong>echnical <strong>A</strong>nnotation</center></legend>
		</div>
	</div>
	<#if showResult??>
	<script type="text/javascript">
		$(document).ready(function(){
			var sorta = new window.top.molgenis.SortaAnonymous($('#ontology-match'));
			sorta.renderPage();
		});
	</script>
	<#else>
	<@ontologyMatchNewTask />
	<script type="text/javascript">
		$(document).ready(function(){
			$('#ontology-match').children('div.row:eq(1)').remove();
		});
	</script>
	</#if>
</form>
<@footer/>