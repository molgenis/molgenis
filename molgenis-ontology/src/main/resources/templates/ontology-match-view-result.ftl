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
				<div id="match-result-container" class="col-md-12"></div>
			</div>
			<script>
				$(document).ready(function(){
								
					var request = {
						'entityName' : '${entityName?js_string}',
						'ontologyIri' : '${ontologyIri?js_string}',
						'matched' : <#if isMatched?? && isMatched>true<#else>false</#if>					
					};
					
					var ontologyService = new molgenis.OntologyService($('#match-result-container'), request);
					ontologyService.renderPage();
					
					$('#update-threshold-button').click(function(){
						$(this).parents('form:eq(0)').attr({
							'action' : molgenis.getContextUrl() + '/threshold/${entityName?html}',
							'method' : 'POST'
						}).submit();
					});
				});
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