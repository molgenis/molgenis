<#import "/spring.ftl" as spring/>
<#-- Bootstrap login modal -->
<div id="login-modal" class="modal" tabindex="0"<#if disableClose?? && disableClose == "true"><#else>
     tabindex="-1"</#if>
     aria-labelledby="login-modal-label" aria-hidden="true" xmlns="http://www.w3.org/1999/html">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header" id="login-model-header">
            <#if disableClose?? && disableClose == "true">
            <#-- Display close button after login failure, because of the missing fallback page (/login) -->
                <button type="button" class="close" onclick="location.href='/'"><span
                        aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
            <#else>
                <button type="button" class="close" data-dismiss="modal" ><span aria-hidden="true">&times;</span><span
                        class="sr-only">Close</span></button>
            </#if>
                <h4 class="modal-title" id="login-modal-label">Sign in</h4>
            </div>
            <div class="modal-body">
                <div class="container-fluid modal-container-padding">
                    <div id="alert-container"></div>
                    <div class="well well-sm">
                        <div class="container">
                            <div class="row">
                                <div class="col-sm w-0">
                                    <input class="form-check-input" type="checkbox" value="" id="privacy-policy-check">
                                </div>
                                <div class="col-sm">
                                    ${privacy_policy_text}
                                </div>
                            </div>
                        </div>
                    </div>

                <#if authentication_oidc_clients?has_content>
                    <div class="row">
                      <div class="col-md-12">
                    <#list authentication_oidc_clients as client>
                        <a href="${client.requestUri}" class="btn btn-primary btn-block disabled" role="button" id="login-with-${client.registrationId}">
                            <!-- configure this label by adding a Localization value for security.oidc.client.${client.registrationId}.buttonText -->
                            <@spring.messageText "security.oidc.client.${client.registrationId}.buttonText" "With ${client.name}" />
                        </a>
                    </#list>
                      </div>
                    </div>
                    <div class="row" style="padding-bottom: 1em; padding-top: 1em;">
                        <div class="col-md-12 text-center">
                            <!-- configure this label by updating the Localization value for security.login.local -->
                            <a href="#" data-toggle="collapse" data-target="#local-user" role="button" id="login-local-user">
                                <small><@spring.message "security.login.local"/></small>
                            </a>
                        </div>
                    </div>
                </#if>
                    <div id="local-user" <#if authentication_oidc_clients?has_content>class="collapse"</#if>>
                    <form id="login-form" role="form" method="POST" action="/login">
                        <div class="form-group">
                            <input id="username-field" type="text" placeholder="Username" class="form-control"
                                   name="username" required autofocus>
                        </div>
                        <div class="form-group">
                            <input id="password-field" type="password" placeholder="Password" class="form-control"
                                   name="password" required>
                        </div>
                        <div class="row">
                            <div class="col-md-4">
                                <button id="signin-button" type="submit" class="btn btn-success" disabled>Sign in</button>
                            </div>
                            <div class="col-md-8">
                                <p class="pull-right"><a class="modal-href" href="/account/password/reset"
                                                         data-target="resetpassword-modal-container">
                                    <small>Forgot password?</small>
                                </a></p>
                            </div>
                        </div>
                    </form>
                    <#if authentication_sign_up_form>
                    <div class="row" style="margin-top: 20px;">
                      <div class="col-md-12 text-center">
                        <small>Don't have an account? <a class="modal-href" href="/account/register"
                                                         data-target="register-modal-container">Sign up</a></small>
                      </div>
                    </div>
                    </#if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="register-modal-container"></div>
<div id="resetpassword-modal-container"></div>

<style>
    .modal-container-padding {
        padding: 0%;
    }
</style>

<script type="text/javascript">
    $(function () {
        var modal = $('#login-modal')
        var submitBtn = $('#signin-button')
        var form = $('#login-form')
        var privacyPolicyCheckbox = $('#privacy-policy-check')
        form.validate()

    <#-- modal events -->
        modal.on('hide.bs.modal', function (e) {
            e.stopPropagation()
            form[0].reset()
            $('.text-error', modal).remove()
            $('.alert', modal).remove()
        })

    <#-- form events -->
        form.submit(function (e) {
            if (!form.valid()) {
                e.preventDefault()
                e.stopPropagation()
            }
        })

        $('input', form).add(submitBtn).keydown(function (e) { <#-- use keydown, because keypress doesn't work cross-browser -->
            if (e.which == 13) {
                e.preventDefault()
                e.stopPropagation()
                form.submit()
            }
        })

        privacyPolicyCheckbox.change(function() {
            if (this.checked){
                submitBtn.prop("disabled", false)
                $('[id^="login-with-"]').removeClass("disabled")
            }else{
                submitBtn.prop("disabled", true)
                $('[id^="login-with-"]').addClass("disabled")
            }
        })

    <#-- submodal events -->
        $(document).on('molgenis-registered', function (e, msg) {
            $('#alert-container', modal).empty()
            $('#alert-container', modal).html($('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> ' + msg + '</div>'))
        })
        $(document).on('molgenis-passwordresetted', function (e, msg) {
            $('#alert-container', modal).empty()
            $('#alert-container', modal).html($('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> ' + msg + '</div>'))
        })

    })
</script>