<#-- Bootstrap order data modal for protocol viewer -->
<div id="orderdata-modal" class="modal hide" tabindex="-1">
  <div class="modal-header">
    <button type="button" class="close" data-dismiss="#orderdata-modal" data-backdrop="true" aria-hidden="true">&times;</button>
    <h3>SHOPPING CART</h3>
  </div>
  <div class="modal-body">
  	<#-- login form -->
	<form id="orderdata-form" class="form-horizontal" enctype="multipart/form-data">
	  <div class="control-group">
	    <label class="control-label" for="orderdata-name">Project title *</label>
	    <div class="controls">
	      <input type="text" id="orderdata-name" name="name" required>
	    </div>
	  </div>
	  <div class="control-group">
	    <label class="control-label" for="orderdata-file">Request form *</label>
	    <div class="controls">
	      <input type="file" id="orderdata-file" name="file" required>
	    </div>
	  </div>
	</form>
  </div>
  <table id="orderdata-selection-table" class="table table-striped table-condensed"></table>
  <div class="modal-footer">
    <a href="#" id="orderdata-btn-close" class="btn" aria-hidden="true">Cancel</a>
    <a href="#" id="orderdata-btn" class="btn btn-primary" aria-hidden="true">Order</a>
  </div>
</div>
<script type="text/javascript">
	$(function() {
		var modal = $('#orderdata-modal');
  		var submitBtn = $('#orderdata-btn');
  		var form = $('#orderdata-form');
  		form.validate();

  		<#-- modal events -->
  		modal.on('show', function () {
	  		$.ajax({
				type : 'GET',
				url : '/cart',
				success : function(cart) {
					
					var items = [];
					items.push('<thead><th>Name</th><th>Description<th></thead>');
					items.push('<tbody>');
					$.each(cart.features, function(i, feature) {
						items.push('<tr>');
						items.push('<td>' + feature.name + '</td>');
						items.push('<td>' + JSON.parse(feature.description).en + '</td>'); // TODO not safe
						items.push('</tr>');	
					});
					items.push('</tbody>');
					$('#orderdata-selection-table').html(items.join(''));
				}
			});
  		});
  		modal.on('shown', function () {
	  		form.find('input:visible:first').focus();
  		});
  		modal.on('hide', function () {
	  		form[0].reset();
	  		$('#orderdata-selection-table').empty();
	  		
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
	   	$('#orderdata-btn-close').click(function() {
		    modal.modal('hide');
		});
		
	    <#-- form events -->
	    form.submit(function(e){	
			e.preventDefault();
		    e.stopPropagation();
			if(form.valid()) {
				$.ajax({
				  type: 'POST',
				  url: '/plugin/order',
				  data: new FormData($('#orderdata-form')[0]),
				  cache: false,
				  contentType: false,
				  processData: false,
				  success: function () {
					alert("ok");
					modal.modal('hide'); // TODO display success message
				  },
		          error: function() {
		          	alert("error"); // TODO display error message
		          }
				});
		    }
		});
	    submitBtn.click(function(e) {
	    	e.preventDefault();
	    	e.stopPropagation();
	    	form.submit();
	    });
		$('input', form).add(submitBtn).keydown(function(e) { <#-- use keydown, because keypress doesn't work cross-browser -->
			if(e.which == 13) {
	    		e.preventDefault();
			    e.stopPropagation();
				form.submit();
	    	}
		});
	});
</script>