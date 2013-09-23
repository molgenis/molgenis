<#-- Bootstrap reset password modal -->
<div id="resetpassword-modal" class="modal hide" tabindex="-1">
  <div class="modal-header">
    <button type="button" class="close" data-dismiss="#resetpassword-modal" data-backdrop="true" aria-hidden="true">&times;</button>
    <h3>Reset password</h3>
  </div>
  <div class="modal-body">
  	<#-- reset password form -->
	<form id="resetpassword-form" class="form-horizontal">
	  <div class="control-group">
	    <label class="control-label" for="resetpassword-email">Email *</label>
	    <div class="controls">
	      <input type="email" id="resetpassword-email" name="email" required>
	    </div>
	  </div>
	</form>
  </div>
  <div class="modal-footer">
    <a href="#" id="resetpassword-btn-close" class="btn" aria-hidden="true">Close</a>
    <a href="#" id="resetpassword-btn" class="btn btn-primary" aria-hidden="true">Reset password</a>
  </div>
</div>
<script type="text/javascript">
	$(function() {
		var modal = $('#resetpassword-modal');
  		var submitBtn = $('#resetpassword-btn');
  		var form = $('#resetpassword-form');
  		form.validate();

  		<#-- modal events -->
  		modal.on('shown', function (e) {
  			e.preventDefault();
			e.stopPropagation();
	  		form.find('input:visible:first').focus();
  		});
  		modal.on('hide', function (e) {
			e.stopPropagation();
	  		form[0].reset();
	  		$('.alert', modal).remove();
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
	   	$('#resetpassword-btn-close').click(function() {
		    modal.modal('hide');
		});
	    <#-- form events -->
	    form.submit(function(e){	
			e.preventDefault();
		    e.stopPropagation();
			if(form.valid()) {
			 	$.ajax({
		            type: 'POST',
		            url:  '/account/password/reset',
		            data: form.serialize(),
		            success: function () {
		            	$(document).trigger('molgenis-passwordresetted', 'Password resetted, an email has been send to you');
		        		modal.modal('hide');
		            },
		            error: function() {
		            	$('.modal-header', modal).after($('<div class="alert alert-error"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Error!</strong> Unknown error occurred</div>'));
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