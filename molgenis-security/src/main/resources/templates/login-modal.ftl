<#-- Bootstrap login modal -->
<#assign googleSignIn = app_settings.googleSignIn && app_settings.signUp && !app_settings.signUpModeration>
<div id="login-modal" class="modal"<#if disableClose?? && disableClose == "true"><#else> tabindex="-1"</#if>
     aria-labelledby="login-modal-label" aria-hidden="true" xmlns="http://www.w3.org/1999/html">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header" id="login-model-header">
            <#if disableClose?? && disableClose == "true">
            <#-- Display close button after login failure, because of the missing fallback page (/login) -->
                <button type="button" class="close" onclick="location.href='/'"><span
                        aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
            <#else>
                <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span
                        class="sr-only">Close</span></button>
            </#if>
                <h4 class="modal-title" id="login-modal-label">
                <#if configured2faHeader??>
                ${configured2faHeader}
                <#elseif initial2faHeader??>
                ${initial2faHeader}
                <#else>
                    Sign in
                </#if>
                </h4>
            </div>
            <div class="modal-body">
                <div id="alert-container"></div>
            <#if googleSignIn>
            <div class="row">
                <div class="col-md-5">
                    <form id="login-google-form" role="form" method="POST" action="/login/google">
                        <input type="hidden" id="google-id-token" name="id_token" value=""/>
                        <div class="g-signin2" data-width="200" data-longtitle="true" data-theme="dark"
                             data-onsuccess="onSignIn"></div>
                    </form>
                    <script>
                        function onSignIn (googleUser) {
                            <#if !(errorMessage??)>
                                $('#google-id-token').val(googleUser.getAuthResponse().id_token)
                                $('#login-google-form').submit()
                            </#if>
                        }
                    </script>
                </div>
            <div class="col-md-6" style="border-left: 1px solid #e5e5e5">
            </#if>
            <#-- login form -->
            <#if is2faConfigured??>
                <div class="verification-form-toggle collapse in">

                    <form id="key-2fa-form" role="form" method="POST" action="/2fa/validate">
                        <div class="input-group">
                            <input id="verification-code-field" placeholder="Enter verification code..."
                                   class="form-control" name="verificationCode" required autofocus>
                            <span class="input-group-btn">
                        <button id="signin-button" class="btn btn-success">Verify</button>
                        </span>
                        </div>
                    </form>
                    <hr>
                    <p>
                        Don't have your phone?
                        <br/>
                        <a href=".verification-form-toggle" data-toggle="collapse">Enter a recovery
                            code</a>
                    </p>
                </div>


                <div class="verification-form-toggle collapse">
                    <form id="recover-form" method="POST" action="/2fa/recover">
                        <div class="input-group collapse">
                            <input id="recovery-code-field" placeholder="Enter recovery code..."
                                   class="form-control" name="recoveryCode" required autofocus>
                            <span class="input-group-btn">
                        <button id="recovery-button" class="btn btn-success">Recover</button>
                        </span>
                        </div>
                        <hr>
                    </form>
                </div>

            <#elseif is2faInitial??>
                <form id="initial-2fa-form" method="POST" action="/2fa/secret">
                    <div class="form-group" style="padding-bottom: 5px;">
                        <b>Two Factor Authentication Set-up</b>
                        <p>Please configure your two factor authentication by scanning the QR code with the <a
                                href="https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2"
                                target="_blank">Google Authenticator app</a> and enter the verification code in the
                            field below to confirm.</p>
                    </div>
                    <div class="form-group">
                        <div class="col-md-12 text-center">
                            <div id="qrcode" style="width: 50%; margin: 0 auto;"></div>
                            <em class="text-muted">${secretKey}</em>
                            <hr>
                        </div>
                        <script type="text/javascript">
                            new QRCode(document.getElementById('qrcode'), '${authenticatorURI}')
                        </script>
                    </div>
                    <div>
                        <div class="input-group col-md-6 col-md-offset-3">
                            <input id="verification-code-field"
                                   placeholder="Enter verification code..."
                                   class="form-control"
                                   name="verificationCode"
                                   required autofocus>
                            <span class="input-group-btn">
                            <button id="verify" class="btn btn-success">Verify</button>
                            </span>
                            <input type="hidden" name="secretKey" value="${secretKey}"/>
                        </div>
                    </div>
                </form>

            <#else>
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
                            <button id="signin-button" type="submit" class="btn btn-success">Sign in</button>
                        </div>
                        <div class="col-md-8">
                            <p class="pull-right"><a class="modal-href" href="/account/password/reset"
                                                     data-target="resetpassword-modal-container">
                                <small>Forgot password?</small>
                            </a></p>
                        </div>
                    </div>
                </form>
            </#if>
            <#if googleSignIn>
            </div>
            </div>
            </#if>
            <#if app_settings.signUp>
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
<div id="register-modal-container"></div>
<div id="resetpassword-modal-container"></div>
<script type="text/javascript">
    $(function () {
        var modal = $('#login-modal')
        var submitBtn = $('#login-btn')
        var form = $('#login-form')
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

        submitBtn.click(function (e) {
            e.preventDefault()
            e.stopPropagation()
            form.submit()
        })

        $('input', form).add(submitBtn).keydown(function (e) { <#-- use keydown, because keypress doesn't work cross-browser -->
            if (e.which == 13) {
                e.preventDefault()
                e.stopPropagation()
                form.submit()
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