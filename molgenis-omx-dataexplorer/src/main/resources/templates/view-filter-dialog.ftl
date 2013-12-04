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
							<table id="${protocol.identifier}-table">
								<#if protocol.features?has_content>
								   	<#list protocol.features as feature> 
									  	<tr>
										  	<td>
										  		<a class="test" data-molgenis-url="/api/v1/observablefeature/${feature.id?c}" style="btn">${feature.name}</a>
								  			</td>
						  				</tr>
								   	</#list>
							   	<#else>
							   		No features
						   		</#if>
				   	  	  	</table>
					   </div>
				   </#list>
					<ul class="pager wizard">
						<li class="previous first"><a href="#">First</a></li>
						<li class="previous"><a href="#">Previous</a></li>
						<li class="next last"><a href="#">Last</a></li>
						<li class="next"><a href="#">Next</a></li>
						<li class="next finish" ><a href="#">Finish</a></li>
					</ul>
				</div>
			</div>
			</form>
		</div>

</div>

<script>
$(function() {
	var modal = $('#filter-dialog-modal');

   $('#filter-wizard').bootstrapWizard({onTabShow: function(tab, navigation, index) {
		var $total = navigation.find('li').length;
		var $current = index+1;
		
		// If it's the last tab then hide the last button and show the finish instead
		if($current >= $total) {
			$('#filter-wizard').find('.pager .next').hide();
			$('#filter-wizard').find('.pager .finish').show();
			$('#filter-wizard').find('.pager .finish').removeClass('disabled');
		} else {
			$('#filter-wizard').find('.pager .next').show();
			$('#filter-wizard').find('.pager .finish').hide();
		}
		
	}});
	$('#filter-wizard .finish').click(function(e) {
        modal.modal('hide');
	});
	
  	molgenis.createFeatureFilterField($('#filter-nav-content a.test'));
  	$.each($('#filter-nav-content a.test'), function (index, element) {
  		//molgenis.createFeatureFilterField(element, $(element).attr('data-molgenis-url'));
	});
});
</script>