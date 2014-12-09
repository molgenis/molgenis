<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
	<div class="row-fluid">
		<form id="account-form" class="form-horizontal" action="${context_url}/update" method="POST">
			<div class="span8 offset2">
				<legend>Account information</legend>
				<div class="row-fluid">
					<div class="span6">
				        <div class="control-group">
							<label class="control-label" for="username">Username</label>
							<div class="controls">
								<input type="text" name="username" value="${user.username!?html}" disabled>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="emailaddress">Email address</label>
							<div class="controls">
								<input type="email" name="emailaddress" value="${user.email!?html}" disabled>
							</div>
						</div>
					</div>
					<div class="span6">
						<div class="control-group">
							<label class="control-label" for="oldpwd">Old password</label>
							<div class="controls">
								<input type="password" name="oldpwd">
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="newpwd">New password</label>
							<div class="controls">
								<input type="password" id="reg-password" name="newpwd">
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="newpwd2">Repeat new password</label>
							<div class="controls">
								<input type="password" id="reg-password-confirm" name="newpwd2">
							</div>
						</div>
					</div>
				</div>
		        <legend>Personal information</legend>
		        <div class="row-fluid">
					<div class="span6">
						<div class="control-group">
							<label class="control-label" for="phone">Phone</label>
							<div class="controls">
								<input type="text" name="phone"<#if user.phone??> value="${user.phone?html}"</#if>>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="phone">Fax</label>
							<div class="controls">
								<input type="text" name="fax"<#if user.fax??> value="${user.fax?html}"</#if>>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="tollFreePhone">Toll-free phone</label>
							<div class="controls">
								<input type="text" name="tollFreePhone"<#if user.tollFreePhone??> value="${user.tollFreePhone?html}"</#if>>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="address">Address</label>
							<div class="controls">
								<input type="text" name="address"<#if user.address??> value="${user.address?html}"</#if>>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="title">Title</label>
							<div class="controls">
								<input type="text" name="title"<#if user.title??> value="${user.title?html}"</#if>>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="firstname">First name *</label>
							<div class="controls">
								<input type="text" name="firstname"<#if user.firstname??> value="${user.firstname?html}"</#if> required>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="firstname">Middle names</label>
							<div class="controls">
								<input type="text" name="middleNames"<#if user.middleNames??> value="${user.middleNames?html}"</#if>>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="lastname">Last name *</label>
							<div class="controls">
								<input type="text" name="lastname"<#if user.lastname??> value="${user.lastname?html}"</#if> required>
							</div>
						</div>
					</div>
					<div class="span6">
						<div class="control-group">
							<label class="control-label" for="institute">Institute</label>
							<div class="controls">
								<input type="text" name="institute"<#if user.affiliation??> value="${user.affiliation?html}"</#if>>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="department">Department</label>
							<div class="controls">
								<input type="text" name="department"<#if user.department??> value="${user.department?html}"</#if>>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="position">Position</label>
							<div class="controls">
								<input type="text" name="position"<#if user.role??> value="${user.role?html}"</#if>>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="city">City</label>
							<div class="controls">
								<input type="text" name="city"<#if user.city??> value="${user.city?html}"</#if>>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="country">Country</label>
							<div class="controls">
								<select name="country">
								<#if !user.country??>
									<option value="" disabled selected>Please Select</option>
								</#if>
								<#list countries?keys as countryCode>
									<option value="${countryCode?html}"<#if user.country?? && user.country == countries[countryCode]> selected</#if>>${countries[countryCode]?html}</option>
								</#list>
								</select>
							</div>
						</div>
					</div>
				</div>
				<div class="row-fluid">
					<div class="span6 offset6">
						<div class="control-group">
							<div class="controls">
								<button type="submit" id="submit-button" class="btn btn-primary pull-right">Apply changes</button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</form>
	</div>	
	<div class="row-fluid">
		<div class="span8 offset2">
			<legend>Group information</legend>
			<div class="span4">
				A member of the following groups:
			</div>
			<div class="span4">
				<ul id="groups" class="inline">
					<#list groups as group>
						<li>${group.name?html}</li>
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