<!DOCTYPE html>
<html>
	<head>
		<title>Change password</title>
		<meta charset="utf-8">
		<link rel="icon" href="/img/molgenis.ico" type="image/x-icon">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/molgenis.css" type="text/css">
		<script src="/js/jquery-1.8.3.min.js"></script>
		<script src="/js/bootstrap.min.js"></script>
		<script src="/js/jquery.validate.min.js"></script>
		<script src="/js/molgenis.js"></script>
		<script type="text/javascript">
			$(function() {
		  		var modal = $('#change-password-modal');
	   			modal.modal();
  				var submitBtn = $('#change-password-btn');
  				var form = $('#change-password-form');
  				form.validate({
  					rules: {
  						password1: {
  							required: true,
  							minlength: 5
  						},
  						password2: {
  							required: true,
  							minlength: 5,
  							equalTo: '#password1'
  						}
  					},
  					messages: {
  							password2: {
  								equalTo: "Passwords don't match"
  							}
  						}
  				});
  		
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
	    			if(!form.valid()) {
	    				e.preventDefault();
	    				e.stopPropagation();
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
	</head>
	<body>
		<div id="change-password-modal" class="modal hide" tabindex="-1">
  			<div class="modal-header">
    			<h3>Please change your password</h3>
  			</div>
  			<div class="modal-body">
				<form id="change-password-form" class="form-horizontal" method="POST" action="">
	  				<div class="control-group">
	    				<label class="control-label" for="loginPassword">Password</label>
	    				<div class="controls">
	      					<input type="password" id="password1" name="password1">
	   	 				</div>
	  				</div>
	  				<div class="control-group">
	    				<label class="control-label" for="loginPassword">Retype password</label>
	    				<div class="controls">
	      					<input type="password" id="password2" name="password2">
	   	 				</div>
	  				</div>
	  				<div class="control-group">
	    				<div class="controls">
	      					<button id="change-password-btn" type="submit" class="btn btn-primary">Change password</button>
	    				</div>
	  				</div>
				</form>
			</div>
		</div>
	</body>
	
</html>