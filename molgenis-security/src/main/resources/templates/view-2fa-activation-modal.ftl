<#include "resource-macros.ftl">
<!DOCTYPE html>
<html>
<head>
    <title>MOLGENIS - Activate 2 factor authentication</title>
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
            var modal = $('#2fa-activation-modal')
            modal.modal()

        <#-- modal events -->
            modal.on('hide.bs.modal', function (e) {
                e.stopPropagation()
                form[0].reset()
                $('.text-error', modal).remove()
                $('.alert', modal).remove()
            })

            modal.on('shown.bs.modal', function (e) {
                $(e.target, '[autofocus]').focus()
            })
        })
    </script>
    <style>
        .modal-container-padding {
            padding: 0%;
        }

        .vertical-spacer {
            padding-top: 10px;
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
<div class="modal" id="2fa-activation-modal" tabindex="-1" aria-labelledby="2fa-activation-modal-label">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header" id="2fa-activation-modal-header">
                <h4 class="modal-title" id="2fa-activation-modal-label">Setup 2 factor authentication</h4>
            </div>
            <div class="modal-body">
                <div class="container-fluid modal-container-padding">
                    <div id="alert-container"></div>
                    <form id="activation-form" method="POST" action="/2fa/activation/authenticate">
                        <div class="form-group">
                            <p>Please configure two factor authentication by scanning the QR code with the <a
                                    href="https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2"
                                    target="_blank">Google Authenticator app</a> and enter the verification code in the
                                field below to confirm.</p>
                        </div>
                        <div class="vertical-spacer"></div>
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
                            <div class="row">
                                <div class="col-md-12 text-center">
                                    <input id="verification-code-field"
                                           type="text"
                                           class="form-control"
                                           name="verificationCode"
                                           required/>
                                    <script>
                                        $('#verification-code-field').pincodeInput({
                                            inputs: 6,
                                            hidedigits: false,
                                            complete: function (value, e) {
                                                $('#activation-form').submit()
                                            }
                                        })
                                        $('input.pincode-input-text.first').attr('autofocus', 'autofocus')
                                    </script>
                                    <input type="hidden" name="secretKey" value="${secretKey}"/>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>