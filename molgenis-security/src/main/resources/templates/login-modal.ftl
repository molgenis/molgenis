<#-- Bootstrap login modal -->
<#assign googleSignIn = app_settings.googleSignIn && app_settings.signUp && !app_settings.signUpModeration>
<div id="login-modal" class="modal"<#if disableClose?? && disableClose == "true"><#else> tabindex="-1"</#if>
     aria-labelledby="login-modal-label" aria-hidden="true" xmlns="http://www.w3.org/1999/html">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
            <#if disableClose?? && disableClose == "true">
            <#-- Display close button after login failure, because of the missing fallback page (/login) -->
                <button type="button" class="close" onclick="location.href='/'"><span
                        aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
            <#else>
                <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span
                        class="sr-only">Close</span></button>
            </#if>
                <h4 class="modal-title" id="login-modal-label">Sign in</h4>
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
                        function onSignIn(googleUser) {
                            <#if !(errorMessage??)>
                                $('#google-id-token').val(googleUser.getAuthResponse().id_token);
                                $('#login-google-form').submit();
                            </#if>
                        }
                    </script>
                </div>
            <div class="col-md-6" style="border-left: 1px solid #e5e5e5">
            </#if>
            <#-- login form -->
                <#if is2faEnabled??>
                    <form id="2fakey-form" role="form" method="POST" action="/2fa/validate">
                        <div class="form-group">
                            <input id="text-field" type="text" placeholder="Key" class="form-control" name="key" required>
                        </div>
                        <div class="row">
                            <div class="col-md-4">
                                <button id="signin-button" type="submit" class="btn btn-success">Verify</button>
                            </div>
                        </div>
                    </form>
                <#elseif is2faInitial??>
                    <form id="initial-2fa-form" role="form" method="POST" action="/2fa/secret">
                        <div class="form-group">
                            <img src="https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=otpauth%3A//totp/User%2540Realm%3Fsecret%3DKO5DE2YD3Q2ZXEOO%26issuer%3DIssuer%2520Name&admin=UTF-8" style="height:200px; widht:200px" name="secret"/>
                        </div>
                        <div class="form-group">
                            <input id="text-field" type="text" placeholder="Secret" class="form-control" name="secret" required>
                        </div>
                        <div class="row">
                            <div class="col-md-4">
                                <button id="signin-button" type="submit" class="btn btn-success">Save</button>
                            </div>
                        </div>
                    </form>
                <#else>
                    <form id="login-form" role="form" method="POST" action="/login">
                        <div class="form-group">
                            <input id="username-field" type="text" placeholder="Username" class="form-control"
                                   name="username" required>
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
        form.submit(function (e) {
            if (!form.valid()) {
                e.preventDefault();
                e.stopPropagation();
            }
        });
        submitBtn.click(function (e) {
            e.preventDefault();
            e.stopPropagation();
            form.submit();
        });
        $('input', form).add(submitBtn).keydown(function (e) { <#-- use keydown, because keypress doesn't work cross-browser -->
            if (e.which == 13) {
                e.preventDefault();
                e.stopPropagation();
                form.submit();
            }
        });

    <#-- submodal events -->
        $(document).on('molgenis-registered', function (e, msg) {
            $('.modal-header', modal).first().after($('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> ' + msg + '</div>'));
        });
        $(document).on('molgenis-passwordresetted', function (e, msg) {
            $('#alert-container', modal).empty();
            $('#alert-container', modal).html($('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> ' + msg + '</div>'));
        });
    });
</script>