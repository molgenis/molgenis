<#include "resource-macros.ftl">
<!DOCTYPE html>
<html>
<head>
    <title>MOLGENIS - Activate 2 factor authentication</title>
    <script src="<@resource_href "/js/qrcode.min.js"/>"></script>
<#include "view-2fa-imports.ftl">
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
        $('#alert-container').html($('<div class="alert alert-block alert-danger alert-dismissable fade in"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><strong>Error!</strong> ${errorMessage?html}</div>'))
    </#if>
    })
</script>
<div class="modal" id="2fa-modal" tabindex="-1" aria-labelledby="2fa-modal-label">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header" id="2fa-modal-header">
                <h4 class="modal-title" id="2fa-modal-label">Setup 2 factor authentication</h4>
            </div>
            <div class="modal-body">
                <div class="container-fluid modal-container-padding">
                    <div id="alert-container"></div>
                    <form id="activation-form" method="POST" action="/2fa/activation/authenticate">
                        <div class="form-group">
                            <p>Please configure two factor authentication by scanning the QR code with an authenticator
                                app (like <a
                                        href="https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2"
                                        target="_blank">Google Authenticator</a>) and enter the verification code in the
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