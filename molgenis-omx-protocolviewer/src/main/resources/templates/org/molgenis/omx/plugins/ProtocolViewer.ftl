<#macro ProtocolViewer screen>
<#assign model = screen.myModel>
<#assign url_base = "molgenis.do?__target=${screen.name}">
<!-- normally you make one big form for the whole plugin-->
<form method="post" enctype="multipart/form-data" name="${screen.name}" action="">
	<!--needed in every form: to redirect the request to the right screen-->
	<input type="hidden" name="__target" value="${screen.name}">
	<!--needed in every form: to define the action. This can be set by the submit button-->
	<input type="hidden" name="__action" id="test" value="">

	<div class="formscreen">
		<div class="form_header" id="${screen.name}">
			${screen.label}
		</div>
	<#-- optional: mechanism to show messages -->
	<#list screen.getMessages() as message>
		<#if message.success>
		<p class="successmessage">${message.text}</p>
		<#else>
		<p class="errormessage">${message.text}</p>
		</#if>
	</#list>
	<#if !model.authenticated>
		<div class="alert">
			<button type="button" class="close" data-dismiss="alert">&times;</button>
	  		<strong>Warning!</strong> You need to <a data-toggle="modal" href="/login" data-target="#login-modal">login</a> to save your variable selection
		</div>
		<div id="login-modal" class="modal hide">
		  <div class="modal-header">
		    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		    <h3>Login</h3>
		  </div>
		  <div class="modal-body"></div>
		</div>
		<script type="text/javascript">
			$(function() {
				var modal = $('#login-modal');
				modal.on('hide', function (event) {
					var authenticated = modal.data('modal').options.authenticated;
					if(authenticated) {
						$('.alert').alert('close');
						$('#save-selection-button').removeAttr('disabled');
					}
				}); 
			});
		</script>
	</#if>
		<div class="screenbody" id="container-plugin">
			<div class="screenpadding">
			<#if (model.dataSets?size == 0)>
				<span>No available catalogs</span>
			<#else>
				<div class="row-fluid grid" id="dataset-selection">
					<div class="span2">
						<label>Choose a dataset:</label>
					</div>
					<div class="btn-group btn-datasets" data-toggle="buttons-radio">
					<#list model.dataSets as dataSet>
						<button class="btn" id="dataset${dataSet.id?c}">${dataSet.name}</button>
					</#list>
					</div>
					<#-- store dataset ids with dataset input elements -->
					<script type="text/javascript">
						var ids = [<#list model.dataSets as dataset>${dataset.id?c}<#if (dataset_has_next)>, </#if></#list>];
	 					for(i in ids)
	 						$('#dataset' + ids[i]).data('id', ids[i]);
					</script>
				</div>	
				<div class="row-fluid grid">
					<div class="span4">
						<p class="box-title">Browse variables</p>
						<div class="input-append">
							<input id="search-text" type="text" title="Enter your search term">
							<button class="btn" type="button" id="search-button">Search</button>
							<button class="btn" type="button" id="search-clear-button">Clear</button>
						</div>
						<div id="dataset-browser">
						</div>
					</div>
	  				<div class="span8">
  						<div class="row-fluid grid" id="feature-information">
	  						<p class="box-title">Variable description</p>
							<div id="feature-details">
							</div>
						</div>
						<div class="row-fluid grid" id="feature-shopping">
		  					<p class="box-title">Variable selection</p>
							<div id="feature-selection">
							</div>
							<div id="download-controls">
								<button class="btn" id="download-xls-button">Download as Excel</button>
								<#if model.showViewButton>
									<button class="btn" id="view-features-button">View</button>
								</#if>
								<#if model.saveSelectionButton>

								<button class="btn" id="save-selection-button"<#if !model.authenticated> disabled</#if>>Save selection</button>
								
								</#if>
								<button class="btn" id="request-data-button"<#if !model.authenticated> disabled</#if>>Request Data</button>
								
							</div>
		  				</div>
	  				</div>
				</div>
 				<script type="text/javascript"> 					
 					<#-- create event handlers -->
 					$('.btn-datasets button').click(function(e) {
 						e.preventDefault();
 						molgenis.selectDataSet($(this).data('id'));
					});
 					
 					$("#search-text").keyup(function(e){
 						e.preventDefault();
					    if(e.keyCode == 13 || e.which === '13') // enter
					        {$("#search-button").click();}
					});
 					
 					$('#search-button').click(function(e) {
 						e.preventDefault();
 						molgenis.search($('#search-text').val());
 					});
 					
 					$('#search-clear-button').click(function(e) {
 						e.preventDefault();
 						molgenis.clearSearch();
 					});
 					
 					$('#download-xls-button').click(function(e) {
 						e.preventDefault();
 						$.fileDownload('molgenis.do?__target=ProtocolViewer&__action=download_xls', { 
 							httpMethod : "POST",
 							data: { 
 								datasetid : molgenis.getSelectedDataSet(),
 								features : molgenis.getSelectedVariables().join()
 							}
 						});
 					});
 					
 					$('#view-features-button').click(function(e) {
 						e.preventDefault();
 						window.location = 'molgenis.do?__target=ProtocolViewer&__action=download_viewer&datasetid=' + molgenis.getSelectedDataSet() + "&features=" + molgenis.getSelectedVariables().join();
 					});
 					
 					$('#save-selection-button').click(function(e) {
 						e.preventDefault();
 						alert('TODO implement save selection action');
 					});
 					
 					// on ready
					$(function() {
						<#-- disable all form submission -->
						$('form').submit(function() {
							return false;
						});
						
						<#-- prevent user form submission by pressing enter -->
					    $(window).keydown(function(e){
					      if(e.keyCode === 13 || e.which === '13') {
					        e.preventDefault();
					        return false;
					      }
					    });
					    
						$('.btn').button();
						
					<#if (model.dataSets?size == 1)>
						<#-- hide dataset selection -->
						$('#dataset-selection').hide();
					</#if>
					<#if (model.dataSets?size > 0)>
						<#-- select first dataset -->
						$('.btn-datasets button').first().click();
					</#if>
					});
 				</script>
 			</#if>
			</div>
		</div>
	</div>
</form>
</#macro>
