<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>

<@header css />

<#if feedbackForm??>
    <#if feedbackForm.submitted>
    <div class="hero-unit">
        <h1>Thanks!</h1>
        <p>Thank you for your feedback.</p>
    </div>
    <#else>
    <div class="alert alert-danger">
        <strong>Error!</strong><br/>${feedbackForm.errorMessage?html}
    </div>
    </#if>
<#elseif adminEmails?has_content>
    <#if isCurrentUserCanEdit?has_content && isCurrentUserCanEdit>
    <div class="row">
        <div class="col-md-12">
            <a href="${context_url?html}/edit" class="btn btn-default pull-right">Edit page header</a>
        </div>
    </div>
    </#if>

    <#if content?has_content>
    <div class="row">
        <div class="col-md-12">
        <#-- Do *not* HTML escape content else text formatting won't work -->
            ${content}
        </div>
    </div>
    </#if>

    <#assign adminEmailsString = "" />
    <#list adminEmails as adminEmail>
        <#assign adminEmailsString = adminEmailsString + adminEmail/>
        <#if adminEmail_has_next>
            <#assign adminEmailsString = adminEmailsString + ', '/>
        </#if>
    </#list>

<div class="row">
    <div class="col-md-10 col-md-offset-1">
        <p>Feel free to email us at <a href='mailto:${adminEmailsString?html}'>${adminEmailsString?html}</a></p>
    </div>
</div>

<form accept-charset="UTF-8" role="form" method="post" action="feedback" id="feedbackForm" class="form-horizontal"
      role="form">
    <div class="form-group">
        <div class="col-md-1">
            <label class="control-label pull-right" for="form_name">Name</label>
        </div>

        <div class="col-md-4">
            <input class="form-control" name="name" size="30" id="form_name"
                   <#if userName??>value="${userName?html}"</#if> type="text"/>
        </div>
    </div>

    <div class="form-group">
        <div class="col-md-1">
            <label class="control-label pull-right" for="form_email">Email</label>
        </div>

        <div class="col-md-4">
            <input class="form-control" name="email" id="form_email" size="30" type="email"
                   <#if userEmail??>value="${userEmail?html}"</#if>/>
        </div>
    </div>

    <div class="form-group">
        <div class="col-md-1">
            <label class="control-label pull-right" for="form_subject">Subject</label>
        </div>

        <div class="col-md-4">
            <input class="form-control" maxlength="72" name="subject" id="form_subject" size="72" type="text"/>
        </div>
    </div>

    <div class="form-group">
        <div class="col-md-1">
            <label class="control-label pull-right" for="form_feedback">Body</label>
        </div>

        <div class="col-md-4">
            <textarea class="form-control" name="feedback" id="form_feedback" required="true" rows="8"></textarea>
        </div>
    </div>

    <legend>Code validation</legend>
    <div class="form-group">
        <div class="col-md-10 col-md-offset-1">
            <a href="#" id="captcha-href"><img id="captcha-img" src="/captcha"></a>
        </div>
    </div>

    <div class="form-group">
        <div class="col-md-1">
            <label class="control-label pull-right" for="reg-captcha">Code</label>
        </div>

        <div class="col-md-4">
            <input type="text" class="form-control" id="reg-captcha" name="captcha">
        </div>
    </div>

    <div class="form-group">
        <div class="col-md-1 col-md-offset-1">
            <button type="submit" class="btn btn-success">Send</button>
        </div>
    </div>
</form>

<script>
    $("#feedbackForm").validate();
    $('#reg-captcha').rules('add', {
        required: true,
        remote: {
            url: '/captcha',
            type: 'POST'
        }
    });
    $('#captcha-href').click(function (e) {
        e.preventDefault();
        $('#captcha-img').attr('src', '/captcha?_=' + Date.now());
        $('captcha').val('');
    });
</script>

<#else>
<p>Admin email addresses not known.</p>
</#if>

<@footer/>
