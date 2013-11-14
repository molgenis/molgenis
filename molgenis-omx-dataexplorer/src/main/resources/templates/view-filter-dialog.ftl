<div id="filter-dialog-modal-test" tabindex="-1">
	<div class="modal-header">
		<h3>Filter Wizard </h3>
	</div>
	<div class="modal-body">
	<form>
		<div id="filter-wizard" >
			<ul id="filter-nav">
				<#list listOfallProtocols as protocol>
			   <li><a href="#${protocol.identifier}-tab" data-toggle="tab">${protocol.name}</a></li>
			   </#list>
			</ul>	
			<div id="filter-nav-content" class="tab-content">
				<#list listOfallProtocols as protocol>
					<div class="tab-pane" id="${protocol.identifier}-tab">	
						<table  id="${protocol.identifier}-table">
						   	<#list protocol.features as feature> 
							  	<tr>
								  	<td>
								  		<a class="test" data-molgenis-url="/api/v1/observablefeature/${feature.id}" style="btn">${feature.name}</a>
						  			</td>
				  				</tr>
						   	</#list>
			   	  	  	</table>
				   </div>
			   </#list>
				<ul class="pager wizard">
					<li class="previous first" style="display:none;"><a href="#">First</a></li>
					<li class="previous"><a href="#">Previous</a></li>
					<li class="next last" style="display:none;"><a href="#">Last</a></li>
					<li class="next"><a href="#">Next</a></li>
				</ul>
			</div>
		</div>
		</form>
	</div>
	<div class="modal-footer">
	</div>
</div>

<script>
$(document).ready(function() {
  	$('#filter-wizard').bootstrapWizard();
  	$.each($('#filter-nav-content a.test'), function (index, element) {
  		console.log(element);
  		molgenis.createFeatureFilterField(element, $(element).attr('data-molgenis-url'));
		
	});
});
</script>