<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["jquery-ui-1.9.2.custom.min.css", "bootstrap-fileupload.min.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "bootstrap-fileupload.min.js", "jquery.bootstrap.pager.js", "simple_statistics.js"]>
<@header css js/>
<form id="evaluationForm" class="form-horizontal" enctype="multipart/form-data">
	<div class="row-fluid">
		<div class="span12">
			<div class="row-fluid">
				<div class="span12">
					<legend><center><h3>Ontology Annotator</h3></center></legend>
				</div>
			</div>
			<div class="row-fluid btn-group">
				<a id="download-button" href="${context_url}/match/download" type="button" class="btn btn-primary">Download</a>
				<a id="back-button" href="${context_url}" type="button" class="btn btn">Back</a>
			</div>
			<br><br>
			<#if (total > 0)>
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
	
	function updatePageFunction(page){
		var molgenis = window.top.molgenis;
		var request = {
			'start' : page.start,
			'num' : page.end - page.start
		};
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/match/retrieve',
			async : false,
			data : JSON.stringify(request),
			contentType : 'application/json',
			success : function(data, textStatus, request) {
				createResults(data.items)
			},
			error : function(request, textStatus, error){
				console.log(error);
			} 
		});
	}
	
	function createResults(entities){
		var container = $('#match-result');
		container.children('div:gt(0)').empty();
		$.each(entities, function(index, entity){
			var layoutDiv = $('<div />').addClass('row-fluid');
			var termDiv = $('<div />').addClass('span4').append(entity.term).appendTo(layoutDiv);
			var ontologyTermMatchDiv= $('<div />').addClass('span7 div-expandable').appendTo(layoutDiv);
			$.each(entity.results.searchHits, function(index, hit){
				if(index >= 20) return false;
				var ontologyTermName = $('<div />').addClass('span5').css('margin-bottom', '-6px').append(hit.columnValueMap.ontologyTerm);
				var ontologyTermUrl = $('<div />').addClass('span5').css('margin-bottom', '-6px').append('<a href="' + hit.columnValueMap.ontologyTermIRI + '" target="_blank">' + hit.columnValueMap.ontologyTermIRI + '</a>');
				var matchScore = $('<div />').addClass('span2').css('margin-bottom', '-6px').append('<center>' + hit.columnValueMap.combinedScore.toFixed(2) + '</center>');
				$('<div />').addClass('row-fluid').append(ontologyTermName).append(ontologyTermUrl).append(matchScore).appendTo(ontologyTermMatchDiv);
			});
			var divWithColor = $('<div />').addClass('span12 well').append(layoutDiv);
			$('<div />').addClass('row-fluid').append(divWithColor).appendTo(container);
		});
		initToggle();
	}
	
	function initToggle(){
		$('div.div-expandable').each(function(index, element){
			if($(element).children().length > 1){
				$(element).children('div:gt(0)').hide();
				var toggle = $('<i class="icon-plus"></icon>').css('cursor','pointer');
				var buttonDiv = $('<div />').css('float','right').append(toggle);
				$(element).before(buttonDiv);
				toggle.click(function(){
					if($(this).hasClass('icon-plus')){
						$(element).children().show();
						$(this).removeClass('icon-plus').addClass('icon-minus');
					}else{
						$(element).children('div:gt(0)').hide();
						$(this).removeClass('icon-minus').addClass('icon-plus');
					}
				});
			}
		});
	}
	
	$(document).ready(function(){
		var itermsPerPage = 5;
		$('#pager').pager({
			'nrItems' : ${total?c},
			'nrItemsPerPage' : itermsPerPage,
			'onPageChange' : updatePageFunction
		});
		updatePageFunction({
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