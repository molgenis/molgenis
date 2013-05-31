<#-- Bootstrap order details modal -->
<div id="orderdetails-${order.id}-modal" class="modal hide" tabindex="-1">
 	<div class="modal-header">
    	<button type="button" class="close" data-dismiss="#orderdetails-${order.id}-modal" data-backdrop="true" aria-hidden="true">&times;</button>
    	<h4>Orderdetails</h4>
  	</div>
 	<div class="modal-body">
		<h5>Name: ${order.name}</h5>
		<table class="table table-striped table-condensed listtable">
			<thead>
				<th>Variable</th>
				<th>Description</th>
			</thead>
			<tbody>
				<#list order.features as feature>				
					<tr>
						<td>${feature.name}</td>
						<td>
						<#if feature.i18ndescription??>
							${feature.i18ndescription.en}
						<#else>
							null
						</#if>
						</td>
					</tr>
				</#list>	
			</tbody>
		</table>
	</div>
	<div class="modal-footer">
		<a href="#" id="orderdetails-${order.id}-btn-close" class="btn btn-primary" aria-hidden="true">Ok</a>
	</div>
</div>
<script type="text/javascript">
	$(function() {
		var modal = $('#orderdetails-${order.id}-modal');
  		
  		<#-- modal events -->
  		modal.on('shown', function (e) {
  			e.preventDefault();
			e.stopPropagation();
  		});
  		modal.on('hide', function (e) {
			e.stopPropagation();
  		});
  		$('.close', modal).click(function(e) {<#-- workaround: Bootstrap closes the whole stack of modals when closing one modal -->
	  		e.preventDefault();
	        modal.modal('hide');
	    });
	    modal.keyup(function(e) {<#-- workaround: Bootstrap closes the whole stack of modals when closing one modal -->
	    	if(e.which == 27) {
		    	e.preventDefault();
			    e.stopPropagation();
	    	}
	    });
	    modal.keydown(function(e) {<#-- workaround: Bootstrap closes the whole stack of modals when closing one modal -->
	    	if(e.which == 27) {
			    if(modal.data('modal').isShown) {
			    	e.preventDefault();
				    e.stopPropagation();
			    	modal.modal('hide');
			    }
	    	}
	    });
	   	$('#orderdetails-${order.id}-btn-close').click(function() {
		    modal.modal('hide');
		});
	});
</script>