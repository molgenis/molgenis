<#-- Bootstrap order list modal for protocol viewer -->
<div id="orderlist-modal" class="modal hide" tabindex="-1">
  <div class="modal-header">
    <button type="button" class="close" data-dismiss="#orderlist-modal" data-backdrop="true" aria-hidden="true">&times;</button>
    <h3>Your Orders</h3>
  </div>
  <div class="modal-body">
  	<div id="order-list-container"></div>
  </div>
  <div class="modal-footer">
    <a href="#" id="orderlist-btn" class="btn btn-primary" aria-hidden="true">Ok</a>
  </div>
</div>
<script type="text/javascript">
	$(function() {
		var modal = $('#orderlist-modal');
		var okBtn = $('#orderlist-btn');
		
  		<#-- modal events -->
  		modal.on('shown', function () {
	  		$.ajax({
				type : 'GET',
				url : '/plugin/orders',
				success : function(data) {
					var container = $('#order-list-container');
					var items = [];
					items.push('<table class="table">');
					items.push('<thead><th>#<th>Study</th><th>Order Date</th><th>Status</th></thead><tbody>');
					$.each(data.orders, function(i, order) {
						var clazz;
						if(order.orderStatus === 'accepted') clazz = 'success';
						else if(order.orderStatus === 'pending') clazz = 'warning';
						else if(order.orderStatus === 'rejected') clazz = 'error';
						else clazz = 'error';
						items.push('<tr class=' + clazz + '>');
						items.push('<td>' + order.id +'</td><td>' + order.name + '</td><td>' + order.orderDate + '</td><td>' + order.orderStatus + '</td>');
						items.push('</tr>');
					});
					items.push('</tbody></table>');
					container.html(items.join(''));
				}
			});	
  		});
  		modal.on('hide', function () {
	  		$('#order-list-container').empty();
	  		
  		});
  		$('.close', modal).click(function(e) {<#-- workaround: Bootstrap closes the whole stack of modals when closing one modal -->
	  		e.preventDefault();
	        modal.modal('hide');
	    });
	    modal.keydown(function(e) {<#-- workaround: Bootstrap closes the whole stack of modals when closing one modal -->
	    	if(e.which == 27) {
		    	e.preventDefault();
			    e.stopPropagation();
			    modal.modal('hide');
	    	}
	    });
	    
	   	okBtn.click(function(e) {
	    	e.preventDefault();
	    	e.stopPropagation();
	    	modal.modal('hide');
	    });
		okBtn.keydown(function(e) { <#-- use keydown, because keypress doesn't work cross-browser -->
			if(e.which == 13) {
	    		e.preventDefault();
			    e.stopPropagation();
			    modal.modal('hide');
	    	}
		});
		
		<#-- CSS -->
		$('#order-list-container').css('max-height', '300px'); //TODO move to css?
		$('#order-list-container').css('overflow','auto');
	});
</script>