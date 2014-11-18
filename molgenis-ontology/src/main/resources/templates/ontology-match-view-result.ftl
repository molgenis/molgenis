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
				</div>		
			</div>
			<script type="text/javascript">
				$(document).ready(function(){
					setTimeout(function(){
						$('#ontology-match').attr({
							'action' : molgenis.getContextUrl() + '/result/${entityName}',
							'method' : 'GET'
						}).submit();
					}, 5000);
				});
			</script>
			<#else>
			<div class="row">
				<div class="col-md-12">
					<div class="row">
						<div class="col-md-offset-3 col-md-6 well">
							<div id="matched-container" class="row">
								<div class="col-md-8">
									The total number of matched items is <strong><span id="total-matched"></span></strong>
								</div>
							</div><br>
							<div id="unmatched-container" class="row">
								<div class="col-md-8">
									The total number of unmatched items is <strong><span id="total-unmatched"></span></strong>
								</div>
							</div><br><br>
							<div class="row">
								<button id="option-button" class="btn btn-primary" type="button">Options</button>
							</div>
							<div class="row" id="option-div" style="display:none;">
								<br><legend></legend>
								<button id="download-button" class="btn btn-inverse" type="button">Download</button>
								<button id="finished-button" class="btn" type="button">Finish recoding</button>
								<button id="backup-button" class="btn" type="button" style="float:right;">Backup data	</button>
							</div>		
						</div>
					</div>
				</div>
			</div>
			<script>
				$(document).ready(function(){
					$('#option-button').click(function(){
						if($('#option-div').is(':hidden')){
							$('#option-div').show();
						}else{
							$('#option-div').hide();
						}
					});
				});
			</script>
			</#if>
		</div>
	</div>
</#macro>