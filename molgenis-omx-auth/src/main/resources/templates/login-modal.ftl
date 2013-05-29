<#-- Bootstrap login modal -->
<div id="login-modal" class="modal hide" tabindex="-1">
  <div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
    <h3>Login</h3>
  </div>
  <div class="modal-body">
	<#-- login form -->
	<form id="login-form" class="form-horizontal">
	  <div class="control-group">
	    <label class="control-label" for="loginUsername">Username</label>
	    <div class="controls">
	      <input type="text" id="loginUsername" name="username" required>
	    </div>
	  </div>
	  <div class="control-group">
	    <label class="control-label" for="loginPassword">Password</label>
	    <div class="controls">
	      <input type="password" id="loginPassword" name="password" required>
	    </div>
	  </div>
	  <div class="control-group">
	    <div class="controls">
	      <button id="login-btn" type="submit" class="btn btn-primary">Sign in</button>
	    </div>
	  </div>
	</form>
	<#-- links to other modals -->
	<div id="register-modal-container"></div>
	<p><a class="modal-href" href="/account/register" data-target="register-modal-container"><small>Register</small></a></p>
	<div id="resetpassword-modal-container"></div>
	<p><a class="modal-href" href="/account/password/reset" data-target="resetpassword-modal-container"><small>Forgot password?</small></a></p>
  </div>
</div>
<script type="text/javascript">
  	$(function() {
  		var modal = $('#login-modal');
  		var submitBtn = $('#login-btn');
  		var form = $('#login-form');
  		form.validate();
  		
  		<#-- modal events -->
  		modal.on('shown', function () {
	  		form.find('input:visible:first').focus();
  		});
  		modal.on('hide', function (e) {
  			e.stopPropagation();
	  		form[0].reset();
	  		$('.text-error', modal).remove();
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
	    
  		<#-- form events -->
  		form.submit(function(e) {
	    	e.preventDefault();
	    	e.stopPropagation();
	    	if(form.valid()) {
	    		$('.text-error', form).remove();
		        $.ajax({
		            type: 'POST',
		            url:  '/account/login',
		            data: form.serialize(),
		            success: function () {
		            	$(document).trigger('molgenis-login', 'Welcome to MOLGENIS!');
	            		modal.modal('hide');
		            },
		            error: function() {
		            	$('#loginPassword').after($('<p class="text-error">The username or password you entered is incorrect.</p>'));
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
		
		<#-- submodal events -->
		$(document).on('molgenis-registered', function(e, msg) {
			$('.modal-header', modal).first().after($('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> ' + msg + '</div>'));
		});
		$(document).on('molgenis-passwordresetted', function(e, msg) {
			$('.modal-header', modal).first().after($('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> ' + msg + '</div>'));
		});
    });
</script>