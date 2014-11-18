<script src="<@resource_href "/js/jquery-ui-1.9.2.custom.min.js"/>"></script>
<script src="<@resource_href "/js/jquery.bootstrap.pager.js"/>"></script>
<script src="<@resource_href "/js/bootstrap-fileupload.min.js"/>"></script>
<script src="<@resource_href "/js/common-component.js"/>"></script>
<script src="<@resource_href "/js/ontology-annotator.js"/>"></script>
<script src="<@resource_href "/js/mapping-manager.js"/>"></script>
<script src="<@resource_href "/js/biobank-connect.js"/>"></script>
<script src="<@resource_href "/js/algorithm-report.js"/>"></script>
<script src="<@resource_href "/js/simple_statistics.js"/>"></script>
<script src="<@resource_href "/js/d3.min.js"/>"></script>
<script src="<@resource_href "/js/vega.min.js"/>"></script>
<script src="<@resource_href "/js/jstat.min.js"/>"></script>
<script src="<@resource_href "/js/biobankconnect-graph.js"/>"></script>
<script src="<@resource_href "/js/ace-min/ace.js"/>" type="text/javascript" charset="utf-8"></script>
<script src="<@resource_href "/js/ace-min/ext-language_tools.js"/>" type="text/javascript" charset="utf-8"></script>
<link rel="stylesheet" href="<@resource_href "/css/jquery-ui-1.9.2.custom.min.css"/>" type="text/css">
<link rel="stylesheet" href="<@resource_href "/css/biobank-connect.css"/>" type="text/css">
<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal" action="">
	<div class="row">
		<div class="col-md-12 well custom-white-well" style="min-height:500px;">
			<div class="row">
				<div class="col-md-offset-3 col-md-6 text-align-center">
					<legend class="custom-purple-legend"><strong>Report for harmonization</strong></legend>
				</div>
			</div>
			<div class="row">
				<div id="info-div" class="col-md-offset-3 col-md-6 well">
				</div>
			</div>
			<div id="delete-mapping" class="row progress-bar-hidden">
				<div class="col-md-offset-2 col-md-10">
					Deleting existing mappings
				</div>
				<div class="progress progress-striped progress-warning active col-md-offset-2 col-md-8">
					<div class="bar text-align-center"></div>
				</div>
			</div>
			<div id="create-mapping" class="row progress-bar-hidden">
				<div class="col-md-offset-2 col-md-10">
					Create mappings
				</div>
				<div class="progress progress-striped active col-md-offset-2 col-md-8">
					<div class="bar text-align-center"></div>
				</div>
			</div>
			<div id="store-mapping" class="row progress-bar-hidden">
				<div class="col-md-offset-2 col-md-10">
					Store mappings
				</div>
				<div class="progress progress-striped progress-success active col-md-offset-2 col-md-8">
					<div class="bar text-align-center"></div>
				</div>
			</div>
		</div>
	</div>
</form>
<script>
	$(document).ready(function(){
		var molgenis = window.top.molgenis;
		var currentStatus = {};
		currentStatus['DeleteMapping'] = $('#delete-mapping');
		currentStatus['CreateMapping'] = $('#create-mapping');
		currentStatus['StoreMapping'] = $('#store-mapping');
		$('.progress-bar-hidden').hide();
		var algorithmReport = new molgenis.AlgorithmReport();
		algorithmReport.progress($('#info-div'), currentStatus);
		
		$('li.cancel').addClass('disabled').click(function(){
			if(!$(this).hasClass('disabled')){
				$('form').attr({
					'action' : '${context_url}/reset',
					'method' : 'GET'
				}).submit();
			}
			return false;
		});
		$('li.next').addClass('disabled').click(function(){
			if(!$(this).hasClass('disabled')){
				$('form').attr({
					'action' : '${context_url}/next',
					'method' : 'GET',
				}).submit();
			}
			return false;
		});
		$('li.previous').addClass('disabled').click(function(){
			if(!$(this).hasClass('disabled')){
				$('form').attr({
					'action' : '${context_url}/prev',
					'method' : 'GET'
				}).submit();
			}
			return false;
		});
	});
</script>