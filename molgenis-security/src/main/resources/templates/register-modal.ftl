<#-- Bootstrap register modal -->
<div id="register-modal" class="modal" tabindex="-1" role="dialog" aria-labelledby="register-modal-label"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span
                        class="sr-only">Close</span></button>
                <h4 class="modal-title" id="register-modal-label">Sign up</h4>
            </div>
            <div class="modal-body">
            <#-- register form -->
                <form id="register-form" class="form-horizontal" role="form">
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="reg-username">Username *</label>
                        <div class="col-md-6">
                            <input type="text" class="form-control" id="reg-username" name="username" required>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="reg-password">Password *</label>
                        <div class="col-md-6">
                            <input type="password" class="form-control" id="reg-password" name="password" required>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="reg-password-confirm">Repeat password *</label>
                        <div class="col-md-6">
                            <input type="password" class="form-control" id="reg-password-confirm" name="confirmPassword"
                                   required>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="reg-email">Email address *</label>
                        <div class="col-md-6">
                            <input type="email" class="form-control" id="reg-email" name="email" required>
                        </div>
                    </div>
                    <hr>
                    <h4>Personal and professional details</h4>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="reg-phone">Phone</label>
                        <div class="col-md-6">
                            <input type="text" class="form-control" id="reg-phone" name="phone">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="reg-fax">Fax</label>
                        <div class="col-md-6">
                            <input type="text" class="form-control" id="reg-fax" name="fax">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="reg-toll-free-phone">Toll-free phone</label>
                        <div class="col-md-6">
                            <input type="text" class="form-control" id="reg-toll-free-phone" name="tollFreePhone">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="reg-title">Title</label>
                        <div class="col-md-6">
                            <input type="text" class="form-control" id="reg-title" name="title">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="reg-last-name">Last name</label>
                        <div class="col-md-6">
                            <input type="text" class="form-control" id="reg-last-name" name="lastname">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="reg-first-name">First name</label>
                        <div class="col-md-6">
                            <input type="text" class="form-control" id="reg-first-name" name="firstname">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="reg-department">Department</label>
                        <div class="col-md-6">
                            <input type="text" class="form-control" id="reg-department" name="department">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="reg-address">Address</label>
                        <div class="col-md-6">
                            <input type="text" class="form-control" id="reg-address" name="address">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="reg-city">City</label>
                        <div class="col-md-6">
                            <input type="text" class="form-control" id="reg-city" name="city">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="reg-country">Country</label>
                        <div class="col-md-6">
                            <select class="form-control" id="reg-country" name="country">
                                <option value="" disabled selected>Please Select</option>
                            <#list countries?keys as countryCode>
                                <option value="${countryCode}?html">${countries[countryCode]?html}</option>
                            </#list>
                            </select>
                        </div>
                    </div>
                    <hr>
                    <h4>Code validation</h4>
                    <div class="form-group">
                        <div class="col-md-6 col-md-offset-4">
                            <a href="#" id="captcha-href"><img id="captcha-img"/></a>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="reg-captcha">Code</label>
                        <div class="col-md-6">
                            <input type="text" class="form-control" id="reg-captcha" name="captcha">
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <a href="#" id="register-btn-close" class="btn btn-default" aria-hidden="true">Close</a>
                <a href="#" id="register-btn" class="btn btn-primary" aria-hidden="true">Sign up</a>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        var modal = $('#register-modal');
        var submitBtn = $('#register-btn');
        var form = $('#register-form');
        form.validate();

        $('#reg-password').rules('add', {
            minlength: ${min_password_length?js_string}
        });
        $('#reg-password-confirm').rules('add', {
            equalTo: '#reg-password'
        });

    <#-- captcha events -->
        $('#reg-captcha').rules('add', {
            required: true,
            remote: {
                url: 'captcha',
                type: 'POST'
            }
        });
        $('#captcha-href').click(function (e) {
            e.preventDefault();
            $('#captcha-img').attr('src', '/captcha?_=' + Date.now());
            $('captcha').val('');
        });

    <#-- modal events -->
        modal.on('show.bs.modal', function (e) {
            $('#captcha-img').attr('src', '/captcha?_=' + Date.now());
        });

        modal.on('hide.bs.modal', function (e) {
            e.stopPropagation();
            form[0].reset();
            $('.alert', modal).remove();
        });
        $('#register-btn-close').click(function () {
            modal.modal('hide');
        });

    <#-- form events -->
        form.submit(function (e) {
            e.preventDefault();
            e.stopPropagation();
            $('.alert', modal).remove();

            if (form.valid() && !submitBtn.attr('disabled')) {
                submitBtn.attr('disabled', 'disabled');

                $.ajax({
                    type: 'POST',
                    url: '/account/register',
                    data: form.serialize(),
                    global: false, // do not trigger default molgenis error handler
                    success: function (data) {
                        $(document).trigger('molgenis-registered', data.message);
                        modal.modal('hide');
                        submitBtn.removeAttr('disabled');
                    },
                    error: function (xhr) {
                        if (xhr.responseText) {
                            molgenis.createAlert(JSON.parse(xhr.responseText).errors, 'error', $('.modal-body', modal));
                        }
                        submitBtn.removeAttr('disabled');
                    }
                });
            }
        });
        submitBtn.click(function (e) {
            e.preventDefault();
            e.stopPropagation();
            form.submit();
        });
        $('input', form).add(submitBtn).keydown(function (e) {
            if (e.which == 13) {
                e.preventDefault();
                e.stopPropagation();
                form.submit();
            }
        });
    });
</script>