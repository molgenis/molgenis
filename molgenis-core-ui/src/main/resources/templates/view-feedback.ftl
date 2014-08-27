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
	<div class="row">
	<#if content?has_content>
		<div class="col-sm-10">
			${content}
		</div>
	</#if>
	<#if isCurrentUserCanEdit?has_content && isCurrentUserCanEdit>
		<div class="col-sm-2">
        	<a href="${context_url}/edit" class="btn btn-default">Edit page header</a>
        </div>
	</#if>
	</div>

	<div>
		<form accept-charset="UTF-8" role="form" method="post" action="feedback" id="feedbackForm" class="form-horizontal" role="form">
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
				<label class="col-sm-2 control-label" for="form_name">Name</label>
				<div class="col-sm-10">
                	<input class="form-control" name="name" size="30" id="form_name" <#if userName??>value="${userName}"</#if> type="text" />
                </div>
			</div>
			<div class="form-group">
				<label class="col-sm-2 control-label" for="form_email">Email</label>
				<div class="col-sm-10">
					<input class="form-control" name="email" id="form_email" size="30" type="email" <#if userEmail??>value="${userEmail}"</#if>/>
				</div>
			</div>
			<div class="form-group">
				<label class="col-sm-2 control-label" for="form_subject">Subject</label>
				<div class="col-sm-10">
					<input class="form-control" maxlength="72" name="subject" id="form_subject" size="72" type="text" />
				</div>
			</div>
			<div class="form-group">
			    <label class="col-sm-2 control-label" for="form_feedback">Body</label>
			    <div class="col-sm-10">
			    	<textarea class="form-control" name="feedback" id="form_feedback" required="true" rows="8"></textarea>
			    </div>
            </div>
            <div class="col-sm-10 col-sm-offset-2">
				<button type="submit" class="btn btn-success">Send</button>
			</div>
		</form>
		<script>
			$("#feedbackForm").validate();
		</script>
	</div>
<#else>
	<p>Admin email addresses not known.</p>
</#if>

<@footer/>
