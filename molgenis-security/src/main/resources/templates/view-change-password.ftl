<#include "resource-macros.ftl">
<!DOCTYPE html>
<html>
<head>
    <title>Change password</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" href="<@resource_href "/img/favicon.ico"/>" type="image/x-icon">
    <link rel="stylesheet" href="<@resource_href "/css/bootstrap.min.css"/>" type="text/css">
    <link rel="stylesheet" href="<@resource_href "/css/molgenis.css"/>" type="text/css">
    <script src="/js/dist/molgenis-vendor-bundle.js"></script>
    <script src="/js/dist/molgenis-global.js"></script>
    <script src="/js/jquery.validate.min.js"></script>
    <script type="text/javascript">
        $(function () {
            var modal = $('#change-password-modal');
            modal.modal();
            var submitBtn = $('#change-password-btn');
            var form = $('#change-password-form');
            form.validate({
                rules: {
                    password1: {
                        required: true,
                        minlength: ${min_password_length?js_string}
                    },
                    password2: {
                        required: true,
                        minlength: ${min_password_length?js_string},
                        equalTo: '#password1'
                    }
                },
                messages: {
                    password2: {
                        equalTo: "Passwords don't match"
                    }
                }
            });

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
        });
    </script>
</head>
<body>
<div class="modal" id="change-password-modal" tabindex="-1" aria-labelledby="change-password-modal-label">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title" id="change-password-modal-label">Change password</h4>
            </div>
            <div class="modal-body">
                <form id="change-password-form" class="form-horizontal" method="POST" action="" role="form">
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="loginPassword">Password</label>
                        <div class="col-md-8">
                            <input type="password" class="form-control" id="password1" name="password1">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="loginPassword">Retype password</label>
                        <div class="col-md-8">
                            <input type="password" class="form-control" id="password2" name="password2">
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-md-8 col-md-offset-4">
                            <button id="change-password-btn" type="submit" class="btn btn-primary">Change password
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
</body>
</html>
