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
	<#if (model.dataSets?size > 0)>
		<#if !model.authenticated>
			<div id="login-modal-container"></div>
			<div class="alert">
				<button type="button" class="close" data-dismiss="alert">&times;</button>
		  		<strong>Warning!</strong> You need to <a class="modal-href" href="/account/login" data-target="login-modal-container">login</a> to save your variable selection. (your current selection will be discarded)
			</div>
		</#if>
	</#if>
		<div class="screenbody" id="container-plugin">
			<div class="screenpadding">
			<#if (model.dataSets?size == 0)>
				<span>No available catalogs</span>
			<#else>
				<div class="row-fluid grid">
					<div id="dataset-select-container" class="control-group form-horizontal pull-right">
						<label class="control-label" for="dataset-select">Choose a dataset:</label>
						<div class="controls">
							<select data-placeholder="Choose a Dataset" id="dataset-select">
						<#list model.dataSets as dataset>
								<option value="${dataset.id?c}"<#if dataset_index == 0> selected</#if>>${dataset.name}</option>
						</#list>
							</select>
						</div>
					</div>
				</div>
				<div class="row-fluid grid">
					<div class="span4">
						<p class="box-title">Browse variables</p>
						<div class="input-append">
							<input id="search-text" type="text" title="Enter your search term">
							<button class="btn" type="button" id="search-button"><i class="icon-large icon-search"></i></button>
							<button class="btn" type="button" id="search-clear-button"><i class="icon-large icon-remove"></i></button>
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
		  				</div>
		  				<div class="row-fluid grid" id="feature-shopping-controls">
		  					<div class="span9">
			  					<div class="btn-group pull-left">			
							<#if model.enableViewAction>
									<button class="btn" id="view-features-button">View</button>
							</#if>
							<#if model.enableDownloadAction>
									<button class="btn" id="download-xls-button">Download</button>
							</#if>
								</div>
		  					</div>
							<div class="span3">
							<#if model.enableOrderAction>
								<div id="orderdata-modal-container"></div>
								<div id="ordersview-modal-container"></div>
								<div class="btn-group pull-right">
									<a class="modal-href btn<#if !model.authenticated> disabled</#if>" href="/plugin/orders/view" data-target="ordersview-modal-container" id="ordersview-href-btn">View Orders</a>
									<a class="modal-href btn btn-primary<#if !model.authenticated> disabled</#if>" href="/plugin/order" data-target="orderdata-modal-container" id="orderdata-href-btn">Order</a>
								</div>
							</#if>
							</div>
						</div>
	  				</div>
				</div>
 				<script type="text/javascript">
 					<#-- create event handlers -->
 					$('#dataset-select').change(function() {
 						molgenis.selectDataSet($(this).val());
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
 						var uri = molgenis.getSelectedDataSet().href;
 						$.fileDownload('molgenis.do?__target=${screen.name}&__action=download_xls', { 
 							httpMethod : "POST",
 							data: {
 								datasetid : uri.substring(uri.lastIndexOf('/') + 1),
 								features : $.map(molgenis.getSelectedVariables(), function(obj){return obj.feature}).join(',')
 							}
 						});
 					});
 					
 					$('#view-features-button').click(function(e) {
 						e.preventDefault();
 						var uri = molgenis.getSelectedDataSet().href;
 						window.location = 'molgenis.do?__target=${screen.name}&__action=download_viewer&datasetid=' + uri.substring(uri.lastIndexOf('/') + 1) + "&features=" + $.map(molgenis.getSelectedVariables(), function(obj){return obj.feature}).join(',');
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
						$('#dataset-select-container').hide();
					</#if>
					<#if (model.dataSets?size > 0)>
						<#-- select first dataset -->
						$('#dataset-select').chosen().change();
					</#if>
					});
 				</script>
 			</#if>
			</div>
		</div>
	</div>
</form>
</#macro>
