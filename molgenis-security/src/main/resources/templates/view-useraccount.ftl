<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
	<div class="row">
		<form id="account-form" class="form-horizontal" role="form" action="${context_url}/update" method="POST">
			<div class="col-md-8 col-md-offset-2">
				<legend>Account information</legend>
				<div class="row">
					<div class="col-md-6">
				        <div class="form-group">
							<label class="col-md-4 control-label" for="username">Username</label>
							<div class="col-md-5">
                                <input type="text" class="form-control" name="username" value="${user.username!}" disabled>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label" for="emailaddress">Email address</label>
							<div class="col-md-5">
                                <input type="email" class="form-control" name="emailaddress" value="${user.email!}" disabled>
							</div>
						</div>
					</div>
					<div class="col-md-6">
						<div class="form-group">
							<label class="col-md-4 control-label" for="oldpwd">Old password</label>
							<div class="col-md-5">
                                <input type="password" class="form-control" name="oldpwd">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label" for="newpwd">New password</label>
							<div class="col-md-5">
                                <input type="password" class="form-control" id="reg-password" name="newpwd">
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label" for="newpwd2">Repeat new password</label>
							<div class="col-md-5">
                                <input type="password" class="form-control" id="reg-password-confirm" name="newpwd2">
							</div>
						</div>
					</div>
				</div>
		        <legend>Personal information</legend>
		        <div class="row">
					<div class="col-md-6">
						<div class="form-group">
							<label class="col-md-4 control-label" for="phone">Phone</label>
							<div class="col-md-5">
                                <input type="text" class="form-control" name="phone"<#if user.phone??> value="${user.phone}"</#if>>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label" for="phone">Fax</label>
							<div class="col-md-5">
                                <input type="text" class="form-control" name="fax"<#if user.fax??> value="${user.fax}"</#if>>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label" for="tollFreePhone">Toll-free phone</label>
							<div class="col-md-5">
                                <input type="text" class="form-control" name="tollFreePhone"<#if user.tollFreePhone??> value="${user.tollFreePhone}"</#if>>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label" for="address">Address</label>
							<div class="col-md-5">
                                <input type="text" class="form-control" name="address"<#if user.address??> value="${user.address}"</#if>>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label" for="title">Title</label>
							<div class="col-md-5">
                                <input type="text" class="form-control" name="title"<#if user.title??> value="${user.title}"</#if>>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label" for="firstname">First name</label>
							<div class="col-md-5">
                                <input type="text" class="form-control" name="firstname"<#if user.firstname??> value="${user.firstname}"</#if>>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label" for="firstname">Middle names</label>
							<div class="col-md-5">
                                <input type="text" class="form-control" name="middleNames"<#if user.middleNames??> value="${user.middleNames}"</#if>>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label" for="lastname">Last name</label>
							<div class="col-md-5">
                                <input type="text" class="form-control" name="lastname"<#if user.lastname??> value="${user.lastname}"</#if>>
							</div>
						</div>
					</div>
					<div class="col-md-6">
						<div class="form-group">
							<label class="col-md-4 control-label" for="institute">Institute</label>
							<div class="col-md-5">
                                <input type="text" class="form-control" name="institute"<#if user.affiliation??> value="${user.affiliation}"</#if>>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label" for="department">Department</label>
							<div class="col-md-5">
                                <input type="text" class="form-control" name="department"<#if user.department??> value="${user.department}"</#if>>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label" for="position">Position</label>
							<div class="col-md-5">
                                <input type="text" class="form-control" name="position"<#if user.role??> value="${user.role}"</#if>>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label" for="city">City</label>
							<div class="col-md-5">
                                <input type="text" class="form-control" name="city"<#if user.city??> value="${user.city}"</#if>>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label" for="country">Country</label>
							<div class="col-md-5">
    							<select class="form-control" name="country">
    							<#if !user.country??>
    								<option value="" disabled selected>Please Select</option>
    							</#if>
    							<#list countries?keys as countryCode>
    								<option value="${countryCode}"<#if user.country?? && user.country == countries[countryCode]> selected</#if>>${countries[countryCode]}</option>
    							</#list>
    							</select>
							</div>
						</div>
					</div>
				</div>
				<div class="row">
					<div class="col-md-6 col-md-offset-6">
						<div class="form-group">
							<button type="submit" id="submit-button" class="btn btn-primary pull-right">Apply changes</button>
						</div>
					</div>
				</div>
			</div>
		</form>
	</div>	
	<div class="row">
		<div class="col-md-8 col-md-offset-2">
			<legend>Group information</legend>
			<div class="col-md-4">
				A member of the following groups:
			</div>
			<div class="col-md-4">
				<ul id="groups" class="inline">
					<#list groups as group>
						<li>${group.name}</li>
					</#list>
				</ul>
			</div>
		</div>
	</div>
	<script type="text/javascript">
  	$(function() {
  		var submitBtn = $('#submit-button');
  		var form = $('#account-form');
  		form.validate();
	    
	    $('#reg-password').rules('add', {
			minlength: ${min_password_length}
		});
		$('#reg-password-confirm').rules('add', {
			equalTo: '#reg-password'
		});
		
  		<#-- form events -->
  		form.submit(function(e) {
	    	e.preventDefault();
	    	e.stopPropagation();
	    	if(form.valid()) {
	    		$('.text-error', form).remove();
		        $.ajax({
		            type: form.attr('method'),
		            url:  form.attr('action'),
		            data: form.serialize(),
		            success: function() {
		            	molgenis.createAlert([{'message': 'Your account has been updated.'}], 'success');
		            },
					error: function(xhr) {
						molgenis.createAlert(JSON.parse(xhr.responseText).errors);  	 
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
<@footer/>