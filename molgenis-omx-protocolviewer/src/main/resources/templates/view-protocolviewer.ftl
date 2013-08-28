<#if enable_spring_ui>
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["ui.dynatree.css", "chosen.css", "protocolviewer.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "chosen.jquery.min.js", "protocolviewer.js", "jquery.dynatree.min.js", "jquery.fileDownload-min.js", "jquery.validate.min.js"]>
<@header css js/>
	<div class="row-fluid">
				<#if (model.dataSets?size > 0)>
				<#if !model.authenticated>
					<div id="login-modal-container"></div>
					<div class="alert">
						<button type="button" class="close" data-dismiss="alert">&times;</button>
		  				<strong>Warning!</strong> You need to <a class="modal-href" href="/account/login" data-target="login-modal-container">login</a> to save your variable selection. (your current selection will be discarded)
					</div>
				</#if>
			</#if>
			
			
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
									<a class="modal-href btn<#if !model.authenticated> disabled</#if>" href="/plugin/study/orders/view" data-target="ordersview-modal-container" id="ordersview-href-btn">View Orders</a>
									<a class="modal-href btn btn-primary<#if !model.authenticated> disabled</#if>" href="/plugin/study/order" data-target="orderdata-modal-container" id="orderdata-href-btn">Order</a>
								</div>
							</#if>
							</div>
						</div>
	  				</div>
				</div>
 				<script type="text/javascript">
 					<#-- create event handlers -->
 					$('#dataset-select').change(function() {
 						window.top.molgenis.selectDataSet($(this).val());
					});
 					
 					$("#search-text").keyup(function(e){
 						e.preventDefault();
					    if(e.keyCode == 13 || e.which === '13') // enter
					        {$("#search-button").click();}
					});
 					
 					$('#search-button').click(function(e) {
 						e.preventDefault();
 						window.top.molgenis.search($('#search-text').val());
 					});
 					
 					$('#search-clear-button').click(function(e) {
 						e.preventDefault();
 						window.top.molgenis.clearSearch();
 					});
 					
 					$('#download-xls-button').click(function(e) {
 						e.preventDefault();
 						var uri = window.top.molgenis.getSelectedDataSet().href;
 						var features = window.top.molgenis.getSelectedFeatures();
 						if(features!=null){
	 						$.fileDownload('${context_url}/download', { 
	 							httpMethod : "POST",
	 							data: {
	 								datasetid : uri.substring(uri.lastIndexOf('/') + 1),
	 								features : $.map(features, function(obj){return obj.feature}).join(',')
	 							}
	 						});
 						}
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
<@footer/>
<#else>
<html>
	<head>
		<title>Catalogue</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/jquery-ui-1.9.2.custom.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/ui.dynatree.css" type="text/css">
		<link rel="stylesheet" href="/css/chosen.css" type="text/css">
        <link rel="stylesheet" href="/css/protocolviewer.css" type="text/css">
        
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="/js/jquery-ui-1.9.2.custom.min.js"></script>
		<script type="text/javascript" src="/js/chosen.jquery.min.js"></script>
		<script type="text/javascript" src="/js/molgenis-all.js"></script>
		<script type="text/javascript" src="/js/protocolviewer.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="/js/jquery.dynatree.min.js"></script>
		<script type="text/javascript" src="/js/jquery.fileDownload-min.js"></script>
		<script type="text/javascript" src="/js/jquery.validate.min.js"></script>
	</head>
	<body>
		
		<div class="container-fluid" id="plugin-container">
			<#if (model.dataSets?size > 0)>
				<#if !model.authenticated>
					<div id="login-modal-container"></div>
					<div class="alert">
						<button type="button" class="close" data-dismiss="alert">&times;</button>
		  				<strong>Warning!</strong> You need to <a class="modal-href" href="/account/login" data-target="login-modal-container">login</a> to save your variable selection. (your current selection will be discarded)
					</div>
				</#if>
			</#if>
			
			
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
									<a class="modal-href btn<#if !model.authenticated> disabled</#if>" href="/plugin/study/orders/view" data-target="ordersview-modal-container" id="ordersview-href-btn">View Orders</a>
									<a class="modal-href btn btn-primary<#if !model.authenticated> disabled</#if>" href="/plugin/study/order" data-target="orderdata-modal-container" id="orderdata-href-btn">Order</a>
								</div>
							</#if>
							</div>
						</div>
	  				</div>
				</div>
 				<script type="text/javascript">
 					<#-- create event handlers -->
 					$('#dataset-select').change(function() {
 						window.top.molgenis.selectDataSet($(this).val());
					});
 					
 					$("#search-text").keyup(function(e){
 						e.preventDefault();
					    if(e.keyCode == 13 || e.which === '13') // enter
					        {$("#search-button").click();}
					});
 					
 					$('#search-button').click(function(e) {
 						e.preventDefault();
 						window.top.molgenis.search($('#search-text').val());
 					});
 					
 					$('#search-clear-button').click(function(e) {
 						e.preventDefault();
 						window.top.molgenis.clearSearch();
 					});
 					
 					$('#download-xls-button').click(function(e) {
 						e.preventDefault();
 						var uri = window.top.molgenis.getSelectedDataSet().href;
 						var features = window.top.molgenis.getSelectedFeatures();
 						if(features!=null){
	 						$.fileDownload('${context_url}}/download', { 
	 							httpMethod : "POST",
	 							data: {
	 								datasetid : uri.substring(uri.lastIndexOf('/') + 1),
	 								features : $.map(features, function(obj){return obj.feature}).join(',')
	 							}
	 						});
 						}
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
		
	</body>
</html>
</#if>