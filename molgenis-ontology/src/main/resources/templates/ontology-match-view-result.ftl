<#macro ontologyMatchResult>
	<div class="row">
		<div class="col-md-12">
			<#if isRunning?? && isRunning>
			<div class="row">
				<div class="col-md-12">
					<br><br><br>
					<center>
						The data is being processed now, please be patient...
					</center>
					<br><br>
				</div>
			</div>
			<div class="row">
				<div class="col-md-offset-2 col-md-8">
					<div class="progress">
						<div class="progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="width:${progress}%;">
							<span class="sr-only">${progress?html}% Complete</span>
						</div>
					</div>
					<br><br>
				</div>
			</div>
			<script type="text/javascript">
				$(document).ready(function(){
					setTimeout(function(){
						$('#ontology-match').attr({
							'action' : molgenis.getContextUrl() + '/result/${entityName?html}',
							'method' : 'GET'
						}).submit();
					}, 3000);
				});
			</script>
			<#elseif entityName?? & numberOfMatched?? & numberOfMatched??>
			<div class="row" style="margin-bottom:15px;">
				<div class="col-md-offset-3 col-md-6">
					<div class="row">
						<div class="col-md-6">
							<p style="padding-top:5px;margin-left:-15px;">Current threshold : ${threshold?html}%</p>
						</div>
						<div class="col-md-6">
							<input name="threshold" type="text" class="form-control" style="width:50px;float:right;margin-right:-15px;"/>
							<button id="update-threshold-button" class="btn btn-default float-right" type="button">Update</button>
						</div><br>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-md-12">
					<div class="row">
						<div class="col-md-offset-3 col-md-6 well">
							<div id="matched-container" class="row">
								<div class="col-md-8">
									The total number of matched items is <strong><span>${numberOfMatched?html}</span></strong>
								</div>
								<div class="col-md-2 float-right">
									<button id="matched-result-button" type="button" class="btn btn-primary">Show</button>
								</div>
							</div><br>
							<div id="unmatched-container" class="row">
								<div class="col-md-8">
									The total number of unmatched items is <strong><span>${numberOfUnmatched?html}</span></strong>
								</div>
								<div class="col-md-2 float-right">
									<button id="unmatched-result-button" type="button" class="btn btn-info">Show</button>
								</div>
							</div><br>
							<div class="row">
								<div class="col-md-12">
									<button id="download-button" class="btn btn-primary" type="button">Download</button>
								</div>	
							</div>
						</div>
					</div>
				</div>
			</div>
			<div id="pager" class="row"></div>
			<div class="row">
				<div id="match-result-container" class="col-md-12"></div>
			</div>
			<script>
				$(document).ready(function(){
					var request = {
						'entityName' : '${entityName?js_string}',
						'ontologyIri' : '${ontologyIri?js_string}',
					};
					
					$('#unmatched-result-button').click(function(){
						request['matched'] = false;
						initEventHelper(request, ${numberOfUnmatched?c});
					});
					
					$('#matched-result-button').click(function(){
						request['matched'] = true;
						initEventHelper(request, ${numberOfMatched?c});
					});
					
					<#if isMatched?? && isMatched>
					$('#matched-result-button').click();
					<#else>
					$('#unmatched-result-button').click();
					</#if>
					
					$('#update-threshold-button').click(function(){
						$(this).parents('form:eq(0)').attr({
							'action' : molgenis.getContextUrl() + '/threshold/${entityName?html}',
							'method' : 'POST'
						}).submit();
					});
					
					$('#download-button').click(function(){
						$(this).parents('form:eq(0)').attr({
							'action' : molgenis.getContextUrl() + '/match/download/${entityName?html}',
							'method' : 'GET'
						}).submit();
					});
				});
				function initEventHelper(request, totalNumber){
					var ontologyService = new molgenis.OntologyService($('#match-result-container'), request);
					var itermsPerPage = 5;
					$('#pager').pager({
						'nrItems' : totalNumber,
						'nrItemsPerPage' : itermsPerPage,
						'onPageChange' : ontologyService.updatePageFunction
					});
					ontologyService.updatePageFunction({
						'page' : 0,
						'start' : 0,
						'end' : totalNumber < itermsPerPage ? totalNumber : itermsPerPage
					});
				}
			</script>
			<#else>
				<div class="row">
					<div class="col-md-12">
						<br><br><br>
						<center>
							The job name is invalid!
						</center>
					</div>		
				</div>
			</#if>
		</div>
	</div>
</#macro>