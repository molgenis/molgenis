<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["jquery-ui-1.9.2.custom.min.css", "bootstrap-fileupload.min.css", "ui.fancytree.min.css", "biobank-connect.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "bootstrap-fileupload.min.js", "jquery.fancytree.min.js", "common-component.js", "ontology-tree-view.js", "ontology.tree.plugin.js", "ontology-service-result.js", "jquery.bootstrap.pager.js", "simple_statistics.js"]>
<@header css js/>
<form class="form-horizontal">
	<div class="row">
		<div class="col-md-12">
			<div class="row">
				<div class="col-md-12">
					<legend><center><h3>Ontology Annotator</h3></center></legend>
				</div>
			</div>
			<#if (total > 0)>
			<div class="row">
				<div class="col-md-1">
					<a id="back-button" href="${context_url}" type="button" class="btn btn-info">Back</a>
				</div>
				<div>
					<a id="download-button" href="${context_url}/match/download" type="button" class="btn btn-primary">Download</a>
				</div>
				<div>
					<a id="show-tree-button" type="button" class="btn float-right">Show tree</a>
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
												<div class="col-md-5 matchterm" style="margin-bottom:-10px;">
													<strong>Matched ontologyterms</strong>
												</div>
												<div class="col-md-5 termurl" style="margin-bottom:-10px;">
													<strong>Ontologyterm Url</strong>
												</div>
												<div class="col-md-2" style="margin-bottom:-10px;">
													<a id="score-explanation"><center>?Score</center></a>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
							<div id="match-result" class="row"></div>
							<div class="row"><div id="pager"></div></div>
						</div>
						<div id="tree-visual-panel" class="col-md-4" style="display:none;">
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
			<div><center>There are not ontology annotations found for the list of terms provided!</center></div>
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
		
		$('#score-explanation').popover({
			'placement' : 'bottom',
			'trigger' : 'hover',
			'title' : 'Explanation',
			'content' : 'This is not an absolute score and thereby cannot be compared among groups. However the scores can be compared within groups!' 
		});
		
		$('#show-tree-button').click(function(){
			if($('#tree-visual-panel').is(':hidden')){
				$('#match-result-panel').removeClass('col-md-12').addClass('col-md-8');
				$('.termurl').hide();
				$('.matchterm').removeClass('col-md-5').addClass('col-md-8');
				$('#tree-visual-panel').show();
				$('#show-tree-button').html('Hide tree');
			}else{
				$('#match-result-panel').removeClass('col-md-8').addClass('col-md-12');
				$('.termurl').show();
				$('.matchterm').removeClass('col-md-8').addClass('col-md-5');
				$('#tree-visual-panel').hide();
				$('#show-tree-button').html('Show tree');
			}
		});
	});
</script>
<@footer/>	