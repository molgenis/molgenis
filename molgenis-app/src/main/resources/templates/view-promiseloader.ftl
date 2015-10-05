<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>

<div class="row">
	<div class="col-md-6 col-md-offset-3">
		<div class="panel panel-default">
			<!-- Default panel contents -->
			<div class="panel-heading">Promise endpoints</div>

			<!-- Table -->
			<table class="table" id="promise-control-panel">
			</table>
		</div>
	</div>
</div>
<div class="row">
	<div class="col-md-6 col-md-offset-3">
		<div class="panel panel-default">
			<div class="panel-heading">Mapping Projects</div>
    		<div id="data-table-container-mappers"></div>
    	</div>
    </div>
</div>
<div class="row">
	<div class="col-md-12">
		<div class="panel panel-default">
			<div class="panel-heading">Credentials</div>
    		<div id="data-table-container-credentials"></div>
    	</div>
    </div>
</div>



<script>

	React.render(molgenis.ui.Table({
			entity: "promise_PromiseCredentials",
		}), $('#data-table-container-credentials')[0]);
	
	var updateControlPanel = function(){
		$('#promise-control-panel').empty();
		$.get(molgenis.getContextUrl() + '/projects', function(projects){
    		if (projects.length === 0){
				$('#promise-control-panel').append('<tr><td>No ProMISe mapping projects found...</td></tr>');
    		}else{
	    		projects.forEach(function(project){
	    			$('#promise-control-panel').append('<tr><td class="col-md-3">' + project + '</td><td class="col-md-2" id="map-btn-container-' + project + '"></td><td class="col-md-1" id="status-' + project + '"></td><td class="col-md-6" id="message-' + project + '"></td></tr>');
					    React.render(molgenis.ui.Button({
						        text: 'Map',
						        style: 'primary',
						        onClick: function(){
									$.ajax({
										url: molgenis.getContextUrl() + '/map/' + project,
  										showSpinner: false,
  										beforeSend: function(){
  											React.render(molgenis.ui.Spinner(), $('#status-'+project)[0]);
  										}
									}).done(function(report){
										var label;
										var message;
										switch(report.status) {
											case "SUCCESS":
												label = '<span class="label label-success">SUCCESS</span>';
												break;
											case "ERROR":
												label = '<span class="label label-danger">FAILED</span>';
												if( report.hasOwnProperty('message') ) {
													message = report.message;
												}
												break;
											default:
												label = '<span class="label label-warning">UNKNOWN</span>'; 
										}
										
										$('#status-'+project).html(label);
										
										if (message) $('#message-'+project).html(message);
									});
						        }			        
					    }, 'Map'), $('#map-btn-container-' + project)[0]);
	    		});
    		}
    	});
	}
		
	React.render(molgenis.ui.Table({
			entity: "promise_PromiseMappingProjects",
			onRowAdd: updateControlPanel,
			onRowEdit: updateControlPanel,
			onRowDelete: updateControlPanel
		}), $('#data-table-container-mappers')[0]);

    $(function() {
    	updateControlPanel();
	});
</script>
<@footer/>