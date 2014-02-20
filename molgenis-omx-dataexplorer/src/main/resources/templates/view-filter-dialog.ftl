<div id="filter-dialog-modal-test" tabindex="-1">
		<div class="modal-header">
			<h3>Filter Wizard </h3>
		</div>
		<div class="modal-body">
			<form>
				<div id="filter-wizard" >
					<ul id="filter-nav">
						<#list entityMetaDataGroups as entityMetaData>
						<li><a href="#${entityMetaData.name}-tab" data-toggle="tab">${entityMetaData.label}</a></li>
						</#list>
					</ul>	
					<div id="filter-nav-content" class="tab-content">
						<#list entityMetaDataGroups as entityMetaData>
							<div class="tab-pane" id="${entityMetaData.name}-tab">
							<#list entityMetaData.attributes as attributeMetaData>
								<#if attributeMetaData.dataType.enumType != "HAS">
								<table id="${attributeMetaData.name}-table">
									<tr>
									  	<td>
									  		<a class="test" data-molgenis-url="/api/v1/${entityMetaData.name}/meta/${attributeMetaData.name}" style="btn">${attributeMetaData.label}</a>
							  			</td>
					  				</tr>
					   	  	  	</table>
					   	  	  	</#if>
							</#list>
						   </div>
					   </#list>
						<ul class="pager wizard">
							<li class="previous"><a href="#">Previous</a></li>
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
   $('#filter-wizard').bootstrapWizard({
   		tabClass: 'bwizard-steps',
   		onTabClick: function(tab, navigation, index){
   		return false;
   		},
   		onTabShow: function(tab, navigation, index) {
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
});
</script>