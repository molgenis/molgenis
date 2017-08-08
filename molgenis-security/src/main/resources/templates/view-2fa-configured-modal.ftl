<#include "resource-macros.ftl">
<!DOCTYPE html>
<html>
<head>
    <title>MOLGENIS - 2 factor authentication</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" href="<@resource_href "/img/favicon.ico"/>" type="image/x-icon">
    <link rel="stylesheet" href="<@resource_href "/css/bootstrap.min.css"/>" type="text/css">
    <link rel="stylesheet" href="<@resource_href "/css/molgenis.css"/>" type="text/css">
    <link rel="stylesheet" href="<@resource_href "/css/bootstrap-pincode-input.css"/>" type="text/css">
    <script src="<@resource_href "/js/dist/molgenis-vendor-bundle.js"/>"></script>
    <script src="<@resource_href "/js/dist/molgenis-global-ui.js"/>"></script>
    <script src="<@resource_href "/js/dist/molgenis-global.js"/>"></script>
    <script src="<@resource_href "/js/bootstrap-pincode-input.js"/>"></script>
    <script src="<@resource_href "/js/qrcode.min.js"/>"></script>
    <script type="application/javascript">
        $(function () {
            var modal = $('#2fa-configured-modal')
            modal.modal()

        <#-- modal events -->
            modal.on('hide.bs.modal', function (e) {
                e.stopPropagation()
                form[0].reset()
                $('.text-error', modal).remove()
                $('.alert', modal).remove()
            })
        })
    </script>
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
        $('#alert-container').html($('<div class="alert alert-block alert-danger alert-dismissable fade in"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><strong>Warning!</strong> ${errorMessage?html}</div>'))
    </#if>
    })
</script>
<div class="modal" id="2fa-configured-modal" tabindex="-1" aria-labelledby="2fa-activation-modal-label">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header" id="2fa-configured-modal-header">
                <h4 class="modal-title" id="2fa-configured-modal-label">
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
                            <a href=".verification-form-toggle" data-toggle="collapse">Enter a recovery code</a>
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