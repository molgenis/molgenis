<#-- Bootstrap login modal -->
<div id="login-modal" class="modal" tabindex="-1" aria-labelledby="login-modal-label" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                <h4 class="modal-title" id="#login-modal-label">Sign in</h4>
            </div>
            <div class="modal-body">
                <div class="row">
                    <div class="col-sm-8 col-sm-offset-2">
                        <div class="row">
                            <#-- login form -->
                            <form id="login-form" class="form-horizontal" role="form" method="POST" action="/login">
                                <div class="form-group">
                                    <label class="col-sm-3 control-label" for="loginUsername">Username</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control" id="loginUsername" name="username" required>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-3 control-label" for="loginPassword">Password</label>
                                    <div class="col-sm-9">
                                        <input type="password" class="form-control" id="loginPassword" name="password" required>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="col-sm-offset-3 col-sm-9">
                                        <button id="login-btn" type="submit" class="btn btn-primary">Sign in</button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
                <#-- links to other modals -->
                <div id="register-modal-container"></div>
                <p><a class="modal-href" href="/account/register" data-target="register-modal-container"><small>Sign up</small></a></p>
                <div id="resetpassword-modal-container"></div>
                <p><a class="modal-href" href="/account/password/reset" data-target="resetpassword-modal-container"><small>Forgot password?</small></a></p>
            </div>
        </div>
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
		
		<#-- submodal events -->
		$(document).on('molgenis-registered', function(e, msg) {
			$('.modal-header', modal).first().after($('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> ' + msg + '</div>'));
		});
		$(document).on('molgenis-passwordresetted', function(e, msg) {
			$('.modal-header', modal).first().after($('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> ' + msg + '</div>'));
		});
    });
</script>