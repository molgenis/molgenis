<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["jquery-ui-1.9.2.custom.min.css", "bootstrap-fileupload.min.css", "ui.fancytree.min.css", "ontology-service.css", "biobank-connect.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "bootstrap-fileupload.min.js", "jquery.fancytree.min.js", "common-component.js", "ontology-tree-view.js", "ontology.tree.plugin.js", "ontology-service-result.js", "jquery.bootstrap.pager.js", "simple_statistics.js"]>
<@header css js/>
<form class="form-horizontal">
	<div class="row">
		<div class="col-md-12">
			<br>
			<div class="row">
				<div class="col-md-offset-3 col-md-6">
					<legend><center><h3>Ontology Annotator</h3></center></legend>
				</div>
			</div>
			<#if (total > 0)>
			<div class="row">
				<div class="col-md-1">
					<a id="back-button" href="${context_url}" type="button" class="btn btn-info">Back</a>
				</div>
				<div class="col-md-2">
					<a id="download-button" href="${context_url}/match/download" type="button" class="btn btn-primary">Download</a>
				</div>
				<div class="col-md-9">
					<a id="show-tree-button" type="button" class="btn float-right-align">Show tree</a>
				</div>
			</div>
			<br>
			<div class="row">
				<div class="col-md-12 custom-white-well">
					<div class="row">
						<div id="match-result-panel" class="col-md-12" style="min-height:500px;">
							<div class="row">
								<div class="col-md-12 well">
									<div class="row">
										<div class="col-md-4"><strong>Input terms</strong></div>
										<div class="col-md-8">
											<div class="row">
												<div class="col-md-8 matchterm" style="margin-bottom:-10px;">
													<strong>Matched ontologyterms</strong>
												</div>
												<div class="col-md-3" style="margin-bottom:-10px;">
													<center><strong>Score</strong></center>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
							<div id="match-result" class="row"></div>
							<div class="row"><div id="pager" style="margin-bottom:50px;"></div></div>
						</div>
						<div id="tree-visual-panel" class="col-md-4" style="display:none;padding-left:40px;">
							<div class="row">
								<div class="col-md-12 well">
									<div class="row">
										<div class="col-md-12">
											<strong>Ontology tree</strong>
										</div>
									</div>
								</div>
							</div>
							<div class="row">
								<div id="tree-container" style="position:relative;top:10px;"></div>	
							</div>
						</div>
					</div>
				</div>
			</div>
			<#else>
				<div class="row">
					<div class="col-md-offset-3 col-md-4">
						<a id="back-button" href="${context_url}" type="button" class="btn btn-info">Back</a>
					</div>
				</div>
				<div class="error-report-panel"><center>
				<#if (total == -1)>
					Please check your input file, there are errors in your input file!
				<#else>
					There are not ontology annotations found for the list of terms provided!
				</#if>
				</center></div>
			</#if>
		</div>
	</div>
</form>
<script type="text/javascript">
	$(document).ready(function(){
		var ontologyTree = new molgenis.OntologyTree("tree-container");
		ontologyTree.updateOntologyTree('${ontologyUrl}');
		var ontologyService = new molgenis.OntologySerivce(ontologyTree);
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
		
		$('#show-tree-button').click(function(){
			if($('#tree-visual-panel').is(':hidden')){
				$('#match-result-panel').removeClass('col-md-12').addClass('col-md-8');
				$('.termurl').hide();
				$('.matchterm').removeClass('col-md-4').addClass('col-md-9');
				$('#tree-visual-panel').show();
				$('#show-tree-button').html('Hide tree');
			}else{
				$('#match-result-panel').removeClass('col-md-8').addClass('col-md-12');
				$('.termurl').show();
				$('.matchterm').removeClass('col-md-9').addClass('col-md-4');
				$('#tree-visual-panel').hide();
				$('#show-tree-button').html('Show tree');
			}
		});
	});
</script>
<@footer/>	