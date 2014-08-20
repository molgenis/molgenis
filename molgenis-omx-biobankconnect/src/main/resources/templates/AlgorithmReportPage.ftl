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
	<div class="row-fluid">
		<div class="span12 well custom-white-well" style="min-height:500px;">
			<div class="row-fluid">
				<div class="offset3 span6 text-align-center">
					<legend class="custom-purple-legend"><strong>Report for harmonization</strong></legend>
				</div>
			</div>
			<div class="row-fluid">
				<div id="info-div" class="offset3 span6 well">
				</div>
			</div>
			<div id="delete-mapping" class="row-fluid progress-bar-hidden">
				<div class="offset2 span10">
					Deleting existing mappings
				</div>
				<div class="progress progress-striped progress-warning active offset2 span8">
					<div class="bar text-align-center"></div>
				</div>
			</div>
			<div id="create-mapping" class="row-fluid progress-bar-hidden">
				<div class="offset2 span10">
					Create mappings
				</div>
				<div class="progress progress-striped active offset2 span8">
					<div class="bar text-align-center"></div>
				</div>
			</div>
			<div id="store-mapping" class="row-fluid progress-bar-hidden">
				<div class="offset2 span10">
					Store mappings
				</div>
				<div class="progress progress-striped progress-success active offset2 span8">
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