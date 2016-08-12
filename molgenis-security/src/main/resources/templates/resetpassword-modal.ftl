<#-- Bootstrap reset password modal -->
<div id="resetpassword-modal" class="modal" tabindex="-1" aria-labelledby="resetpassword-modal-label"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span
                        class="sr-only">Close</span></button>
                <h4 class="modal-title" id="resetpassword-modal-label">Reset password</h4>
            </div>
            <div class="modal-body">
                <div id="error-container"></div>
            <#-- reset password form -->
                <form id="resetpassword-form" class="form-horizontal" role="form">
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="resetpassword-email">Email *</label>
                        <div class="col-md-6">
                            <input type="email" class="form-control" id="resetpassword-email" name="email" required>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <a href="#" id="resetpassword-btn-close" class="btn btn-default" aria-hidden="true">Close</a>
                <a href="#" id="resetpassword-btn" class="btn btn-primary" aria-hidden="true">Reset password</a>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        var modal = $('#resetpassword-modal');
        var submitBtn = $('#resetpassword-btn');
        var form = $('#resetpassword-form');
        form.validate();

    <#-- modal events -->
        modal.on('hide.bs.modal', function (e) {
            e.stopPropagation();
            form[0].reset();
            $('.alert', modal).remove();
        });
        $('#resetpassword-btn-close').click(function () {
            modal.modal('hide');
        });
    <#-- form events -->
        form.submit(function (e) {
            e.preventDefault();
            e.stopPropagation();
            if (form.valid()) {
                $.ajax({
                    type: 'POST',
                    url: '/account/password/reset',
                    data: form.serialize(),
                    global: false, // do not trigger default molgenis error handler
                    success: function () {
                        $(document).trigger('molgenis-passwordresetted', 'Password reset, an email has been sent to you');
                        modal.modal('hide');
                    },
                    error: function (xhr) {
                        $('#error-container').empty();
                        molgenis.createAlert(JSON.parse(xhr.responseText).errors, 'error', $('#error-container', modal));
                    }
                });
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