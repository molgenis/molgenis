<#-- Bootstrap login modal -->
<div id="login-modal" class="modal"<#if disableClose?? && disableClose == "true"><#else> tabindex="-1"</#if> aria-labelledby="login-modal-label" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
            <#if disableClose?? && disableClose == "true"><#else>
                <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
            </#if>
                <h4 class="modal-title" id="login-modal-label">Sign in</h4>
            </div>
            <div class="modal-body">
                <div class="row">
                    <div class="col-md-8 col-md-offset-2">
                        <div class="row">
                            <#-- login form -->
                            <form id="login-form" class="form-horizontal" role="form" method="POST" action="/login">
                                <div class="form-group">
                                    <label class="col-md-3 control-label" for="loginUsername">Username</label>
                                    <div class="col-md-9">
                                        <input type="text" class="form-control" id="loginUsername" name="username" required>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-md-3 control-label" for="loginPassword">Password</label>
                                    <div class="col-md-9">
                                        <input type="password" class="form-control" id="loginPassword" name="password" required>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="col-md-offset-3 col-md-9">
                                        <button id="login-btn" type="submit" class="btn btn-primary">Sign in</button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
                <#-- links to other modals -->
                <#if enable_self_registration == true>
                    <p><a class="modal-href" href="/account/register" data-target="register-modal-container"><small>Sign up</small></a></p>
                </#if>
                <p><a class="modal-href" href="/account/password/reset" data-target="resetpassword-modal-container"><small>Forgot password?</small></a></p>
            </div>
        </div>
    </div>
</div>
<div id="register-modal-container"></div>
<div id="resetpassword-modal-container"></div>
<script type="text/javascript">
  	$(function() {
  		var modal = $('#login-modal');
  		var submitBtn = $('#login-btn');
  		var form = $('#login-form');
  		form.validate();
  		
  		<#-- modal events -->
  		modal.on('hide.bs.modal', function (e) {
  			e.stopPropagation();
	  		form[0].reset();
	  		$('.text-error', modal).remove();
	  		$('.alert', modal).remove();
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