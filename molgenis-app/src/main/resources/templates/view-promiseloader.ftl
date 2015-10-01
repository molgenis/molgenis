<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>

<div class="row">
	<div class="col-md-12">
		<div class="panel panel-default">
			<div class="panel-heading">Credentials</div>
    		<div id="data-table-container-credentials"></div>
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


<script>
	React.render(molgenis.ui.Table({
			entity: "promise_PromiseCredentials",
		}), $('#data-table-container-credentials')[0]);
		
		React.render(molgenis.ui.Table({
			entity: "promise_PromiseMappingProjects",
		}), $('#data-table-container-mappers')[0]);

    $(function() {
    	$.get(molgenis.getContextUrl() + '/projects', function(projects){
    		if (projects.length === 0){
				$('#promise-control-panel').append('<tr><td>No ProMISe mapping projects found...</td></tr>');
    		}else{
	    		projects.forEach(function(project){
	    			$('#promise-control-panel').append('<tr><td>' + project + '</td><td id="map-btn-container-' + project + '"></td><td id="radboud-status">Succes!!</td></tr>');
					    React.render(molgenis.ui.Button({
					        text: 'Map',
					        style: 'primary',
					        onClick: function() {
					            $.post(molgenis.getContextUrl() + '/map/' + project);            
					        }
					    }, 'Map'), $('#map-btn-container-' + project)[0]);
	    		});
    		}
    	});
	});
</script>
<@footer/>