<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header/>

<#if feedbackForm??>
	<#if feedbackForm.submitted>
		<div class="hero-unit">
			<h1>Thanks!</h1>
			<p>Thank you for your feedback.</p>
		</div>
	<#else>
		<div class="alert alert-danger">
			<strong>Error!</strong><br/>${feedbackForm.errorMessage}
		</div>
	</#if>
<#elseif adminEmails?has_content>
	<#if isCurrentUserCanEdit?has_content && isCurrentUserCanEdit>
	<div class="row">
        <a href="${context_url}/edit" class="btn btn-default pull-right">Edit page header</a>
	</div>
	</#if>
	<#if content?has_content>
	<div class="page-header">
		${content}
	</div>
	</#if>
	<div class="container">
		<form accept-charset="UTF-8" role="form" method="post" action="feedback" id="feedbackForm">
			<#assign adminEmailsString = "" />
			<#list adminEmails as adminEmail>
				<#assign adminEmailsString = adminEmailsString + adminEmail/>
				<#if adminEmail_has_next>
					<#assign adminEmailsString = adminEmailsString + ', '/>
				</#if>
			</#list>
			<p>
				Feel free to email us at <a href='mailto:${adminEmailsString}'>${adminEmailsString}</a>
			</p>
			<div class="form-group">
				<label class="col-md-3 control-label" for="form_name">Name</label>
                <input class="form-control" name="name" size="30" id="form_name" <#if userName??>value="${userName}"</#if> type="text" />
			</div>
			<div class="form-group">
				<label class="col-md-3 control-label" for="form_email">Email</label>
				<input class="form-control" name="email" id="form_email" size="30" type="email" <#if userEmail??>value="${userEmail}"</#if>/>
			</div>
			<div class="form-group">
				<label class="col-md-3 control-label" for="form_subject">Subject</label>
				<input class="form-control" maxlength="72" name="subject" id="form_subject" size="72" type="text" />
			</div>
			<div class="form-group">
			    <label class="col-md-3 control-label" for="form_feedback">Body</label>
			    <textarea class="form-control" name="feedback" id="form_feedback" required="true" rows="8"></textarea>
            </div>
			<button type="submit" class="btn btn-success">Send</button>
		</form>
		<script>
			$("#feedbackForm").validate();
		</script>
	</div>
<#else>
	<p>Admin email addresses not known.</p>
</#if>

<@footer/>
