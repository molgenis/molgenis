<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>

<div class="row">
	<div class="col-md-12">
    	<div id="data-table-container"></div>
    </div>
</div>

<div class="row">
	<div class="col-md-6 col-md-offset-3">
		<div class="panel panel-default">
			<!-- Default panel contents -->
			<div class="panel-heading">Promise endpoints</div>

			<!-- Table -->
			<table class="table">
				<tr>
					<td>Radboud</td>
					<td id="radboud-map-btn-container"></td>
					<td id="radboud-status">Succes!!</td>
				</tr>
				<tr>
					<td>IBD</td>
					<td id="ibd-map-btn-container"></td>
					<td>Succes!!</td>
				</tr>
			</table>
		</div>
	</div>
</div>


<script>
	React.render(molgenis.ui.Table({
			entity: "promise_PromiseCredentials",
		}), $('#data-table-container')[0]);

/*
    React.render(molgenis.ui.Button({
        text: 'Load',
        style: 'primary',
        onClick: function() {
            $.post(molgenis.getContextUrl() + '/load');            
        }
    }, 'Load'), $('#load-btn-container')[0]);
*/

    React.render(molgenis.ui.Button({
        text: 'Map',
        style: 'primary',
        onClick: function() {
            $.post(molgenis.getContextUrl() + '/map/RADBOUD');            
        }
    }, 'Map'), $('#radboud-map-btn-container')[0]);
    
    React.render(molgenis.ui.Button({
        text: 'Map',
        style: 'primary',
        onClick: function() {
            $.post(molgenis.getContextUrl() + '/map/IBD');            
        }
    }, 'Map'), $('#ibd-map-btn-container')[0]);
</script>
<@footer/>