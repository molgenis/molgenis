<#-- Bootstrap register modal -->
<div id="register-modal" class="modal hide" tabindex="-1">
  <div class="modal-header">
    <button type="button" class="close" data-dismiss="#register-modal" data-backdrop="true" aria-hidden="true">&times;</button>
    <h3>Sign up</h3>
  </div>
  <div class="modal-body">
  	<#-- register form -->
	<form id="register-form" class="form-horizontal">
	  <div class="control-group">
	    <label class="control-label" for="reg-username">Username *</label>
	    <div class="controls">
	      <input type="text" id="reg-username" name="username" required>
	    </div>
	  </div>
	  <div class="control-group">
	    <label class="control-label" for="reg-password">Password *</label>
	    <div class="controls">
	      <input type="password" id="reg-password" name="password" required>
	    </div>
	  </div>
	  <div class="control-group">
	    <label class="control-label" for="reg-password-confirm">Repeat password *</label>
	    <div class="controls">
	      <input type="password" id="reg-password-confirm" name="confirmPassword" required>
	    </div>
	  </div>
	  <div class="control-group">
	    <label class="control-label" for="reg-email">Email address *</label>
	    <div class="controls">
	      <input type="email" id="reg-email" name="email" required>
	    </div>
	  </div>
	  <hr>
	  <h4>Personal and professional details</h4>
	  <div class="control-group">
	    <label class="control-label" for="reg-phone">Phone</label>
	    <div class="controls">
	      <input type="text" id="reg-phone" name="phone">
	    </div>
	  </div>
	  <div class="control-group">
	    <label class="control-label" for="reg-fax">Fax</label>
	    <div class="controls">
	      <input type="text" id="reg-fax" name="fax">
	    </div>
	  </div>
	  <div class="control-group">
	    <label class="control-label" for="reg-toll-free-phone">Toll-free phone</label>
	    <div class="controls">
	      <input type="text" id="reg-toll-free-phone" name="tollFreePhone">
	    </div>
	  </div> 
	  <div class="control-group">
	    <label class="control-label" for="reg-title">Title</label>
	    <div class="controls">
	      <input type="text" id="reg-title" name="title">
	    </div>
	  </div>
	  <div class="control-group">
	    <label class="control-label" for="reg-last-name">Last name *</label>
	    <div class="controls">
	      <input type="text" id="reg-last-name" name="lastname" required>
	    </div>
	  </div>
	  <div class="control-group">
	    <label class="control-label" for="reg-first-name">First name *</label>
	    <div class="controls">
	      <input type="text" id="reg-first-name" name="firstname" required>
	    </div>
	  </div>
	  <div class="control-group">
	    <label class="control-label" for="reg-department">Department</label>
	    <div class="controls">
	      <input type="text" id="reg-department" name="department">
	    </div>
	  </div>
	  <div class="control-group">
	    <label class="control-label" for="reg-address">Address</label>
	    <div class="controls">
	      <input type="text" id="reg-address" name="address">
	    </div>
	  </div>
	  <div class="control-group">
	    <label class="control-label" for="reg-city">City</label>
	    <div class="controls">
	      <input type="text" id="reg-city" name="city">
	    </div>
	  </div>
	  <div class="control-group">
	    <label class="control-label" for="reg-country">Country</label>
	    <div class="controls">
	      <select id="reg-country" name="country">
	      	<option value="" disabled selected>Please Select</option>
	      <#list countries?keys as countryCode>
			<option value="${countryCode}">${countries[countryCode]}</option>
		  </#list>
		  </select>
	    </div>
	  </div>
	  <hr>
	  <h4>Code validation</h4>
	  <div class="control-group">
	  	<div class="controls">
	  		<a href="#" id="captcha-href"><img id="captcha-img" src="/captcha"></a>
	  	</div>
	  </div>
	  <div class="control-group">
	    <label class="control-label" for="reg-captcha">Code</label>
	    <div class="controls">
	      <input type="text" id="reg-captcha" name="captcha">
	    </div>
	  </div>     
	</form>
  </div>
  <div class="modal-footer">
    <a href="#" id="register-btn-close" class="btn" aria-hidden="true">Close</a>
    <a href="#" id="register-btn" class="btn btn-primary" aria-hidden="true">Sign up</a>
  </div>
</div>
<script type="text/javascript">
  	$(function() {
  	  	var modal = $('#register-modal');
  		var submitBtn = $('#register-btn');
  		var form = $('#register-form');
  		form.validate();
  		
  		$('#reg-password').rules('add', {
			minlength: 6
		});
		$('#reg-password-confirm').rules('add', {
			equalTo: '#reg-password'
		});
		
  		<#-- captcha events -->
		$('#reg-captcha').rules('add', {
			required: true,
			remote: {
	        	url: 'captcha',
	        	type: 'POST'
	        }
		});
		$('#captcha-href').click(function(e){
		    e.preventDefault();
			$('#captcha-img').attr('src', '/captcha?_=' + Date.now());
	 	});

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
	    $('#register-btn-close').click(function() {
		    modal.modal('hide');
		});
    
		<#-- form events -->
		form.submit(function(e) {	
			e.preventDefault();
		    e.stopPropagation();
			if(form.valid()) {
	    	 	$.ajax({
		            type: 'POST',
		            url:  '/account/register',
		            data: form.serialize(),
		            success: function () {
		            	$(document).trigger('molgenis-registered', 'You have successfully registered');
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
		$('input', form).add(submitBtn).keydown(function(e) {
			if(e.which == 13) {
	    		e.preventDefault();
			    e.stopPropagation();
				form.submit();
	    	}
		});
	});
</script>