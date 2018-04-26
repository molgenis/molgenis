<#include "resource-macros.ftl">
<!DOCTYPE html>
<html>
<head>
    <title>MOLGENIS - 2 factor authentication</title>
<#include "view-2fa-imports.ftl">
    <style>
        .modal-container-padding {
            padding: 0%;
        }
    </style>
</head>
<body>
<script type="application/javascript">
    $(function () {
    <#if errorMessage??>
        $('#alert-container').html($('<div class="alert alert-block alert-danger alert-dismissable fade in"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><strong>Error!</strong> ${errorMessage?html}</div>'))
    </#if>
    <#if isRecoverMode??>
        $('.verification-form-toggle').collapse('toggle')
    </#if>
    })
</script>
<div class="modal" id="2fa-modal" tabindex="-1" aria-labelledby="2fa-modal-label">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header" id="2fa-modal-header">
                <h4 class="modal-title" id="2fa-modal-label">
                    Verification code
                </h4>
            </div>
            <div class="modal-body">
                <div class="container-fluid modal-container-padding">
                    <div id="alert-container"></div>
                    <div class="verification-form-toggle collapse in">
                        <form id="verification-form" role="form" method="POST" action="/2fa/validate">
                            <p>Please enter the authentication code show in the
                                <a href="https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2"
                                   target="_blank">Google Authenticator app</a></p>
                            <div class="input-group">
                                <input id="verification-code-field" type="text"
                                       class="form-control" name="verificationCode">
                                <script>
                                    $('#verification-code-field').pincodeInput({
                                        inputs: 6,
                                        hidedigits: false,
                                        complete: function (value, e) {
                                            $('#verification-form').submit()
                                        }
                                    })
                                    $('input.pincode-input-text.first').attr('autofocus', 'autofocus')
                                </script>
                            </div>
                        </form>

                        <hr>
                        <p>
                            Don't have your phone?
                            <br/>
                            <a href=".verification-form-toggle" data-toggle="collapse">Enter
                                a recovery code</a>
                        </p>
                    </div>

                    <div class="verification-form-toggle collapse">
                        <form id="recover-form" method="POST" action="/2fa/recover">
                            <div class="input-group collapse">
                                <input id="recovery-code-field" type="text" placeholder="Enter recovery code..."
                                       class="form-control" name="recoveryCode" required autofocus>
                                <span class="input-group-btn">
                            <button id="recovery-button" type="submit" class="btn btn-success">Recover</button>
                            </span>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>