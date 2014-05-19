<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["jquery-ui-1.9.2.custom.min.css", "bootstrap-fileupload.min.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "bootstrap-fileupload.min.js", "ontology-service-result.js", "jquery.bootstrap.pager.js","simple_statistics.js"]>
<@header css js/>
<form id="evaluationForm" class="form-horizontal" enctype="multipart/form-data">
	<div class="row-fluid">
		<div class="span12">
			<div class="row-fluid">
				<div class="span12">
					<legend><center><h3>Ontology Annotator</h3></center></legend>
				</div>
			</div>
			<#if (total > 0)>
			<div class="row-fluid">
				<div class="span1">
					<a id="back-button" href="${context_url}" type="button" class="btn btn-info">Back</a>
				</div>
				<div>
					<a id="download-button" href="${context_url}/match/download" type="button" class="btn btn-primary">Download</a>
				</div>
			</div>
			<br>
			<div class="row-fluid">
				<div id="match-result" class="span12">
					<div class="row-fluid">
						<div class="span12 well">
							<div class="row-fluid">
								<div class="span4"><strong>Input terms</strong></div>
								<div class="span7">
									<div class="row-fluid">
										<div class="span5" style="margin-bottom:-10px;">
											<strong>Matched ontologyterms</strong>
										</div>
										<div class="span5" style="margin-bottom:-10px;">
											<strong>Ontologyterm Url</strong>
										</div>
										<div class="span2" style="margin-bottom:-10px;">
											<a id="score-explanation"><center>?Score</center></a>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			<#else>
			<div><center>There are not ontology annotations found for the list of terms provided!</center></div>
			</#if>
			<div class="row-fluid">
				<div id="pager" class="offset3 span6 offset3"></div>
			</div>
		</div>
	</div>
</form>
<script type="text/javascript">
	$(document).ready(function(){
		var ontologyService = new molgenis.OntologySerivce();
		var itermsPerPage = 5;
		$('#pager').pager({
			'nrItems' : ${total?c},
			'nrItemsPerPage' : itermsPerPage,
			'onPageChange' : ontologyService.updatePageFunction
		});
		ontologyService.updatePageFunction({
			'page' : 0,
			'start' : 0,
			'end' : ${total?c} < itermsPerPage ? ${total?c} : itermsPerPage
		});
		$('#score-explanation').popover({
			'placement' : 'bottom',
			'trigger' : 'hover',
			'title' : 'Explanation',
			'content' : 'This is not an absolute score and thereby cannot be compared among groups. However the scores can be compared within groups!' 
		});
	});
</script>
<@footer/>	