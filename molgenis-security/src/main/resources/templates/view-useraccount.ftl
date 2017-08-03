<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header/>
<div class="row">
    <div class="col-md-8 col-md-offset-2">
        <form id="account-form" class="form-horizontal" role="form" action="${context_url?html}/update" method="POST">
            <legend>Account information</legend>
            <div class="row">
                <div class="col-md-6">
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="username">Username</label>
                        <div class="col-md-5">
                            <input type="text" class="form-control" name="username" value="${user.username?html}"
                                   disabled>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="emailaddress">Email address</label>
                        <div class="col-md-5">
                            <input type="email" class="form-control" name="emailaddress" value="${user.email?html}"
                                   disabled>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="oldpwd">Old password</label>
                        <div class="col-md-5">
                            <input type="password" class="form-control" name="oldpwd">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="newpwd">New password</label>
                        <div class="col-md-5">
                            <input type="password" class="form-control" id="reg-password" name="newpwd">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="newpwd2">Repeat new password</label>
                        <div class="col-md-5">
                            <input type="password" class="form-control" id="reg-password-confirm" name="newpwd2">
                        </div>
                    </div>
                </div>
            </div>
            <legend>Personal information</legend>
            <div class="row">
                <div class="col-md-6">
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="phone">Phone</label>
                        <div class="col-md-5">
                            <input type="text" class="form-control" name="phone"<#if user.phone??>
                                   value="${user.phone?html}"</#if>>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="phone">Fax</label>
                        <div class="col-md-5">
                            <input type="text" class="form-control" name="fax"<#if user.fax??>
                                   value="${user.fax?html}"</#if>>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="tollFreePhone">Toll-free phone</label>
                        <div class="col-md-5">
                            <input type="text" class="form-control" name="tollFreePhone"<#if user.tollFreePhone??>
                                   value="${user.tollFreePhone?html}"</#if>>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="address">Address</label>
                        <div class="col-md-5">
                            <input type="text" class="form-control" name="address"<#if user.address??>
                                   value="${user.address?html}"</#if>>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="title">Title</label>
                        <div class="col-md-5">
                            <input type="text" class="form-control" name="title"<#if user.title??>
                                   value="${user.title?html}"</#if>>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="firstname">First name</label>
                        <div class="col-md-5">
                            <input type="text" class="form-control" name="firstname"<#if user.firstName??>
                                   value="${user.firstName?html}"</#if>>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="firstname">Middle names</label>
                        <div class="col-md-5">
                            <input type="text" class="form-control" name="middleNames"<#if user.middleNames??>
                                   value="${user.middleNames?html}"</#if>>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="lastname">Last name</label>
                        <div class="col-md-5">
                            <input type="text" class="form-control" name="lastname"<#if user.lastName??>
                                   value="${user.lastName?html}"</#if>>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="institute">Institute</label>
                        <div class="col-md-5">
                            <input type="text" class="form-control" name="institute"<#if user.affiliation??>
                                   value="${user.affiliation?html}"</#if>>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="department">Department</label>
                        <div class="col-md-5">
                            <input type="text" class="form-control" name="department"<#if user.department??>
                                   value="${user.department?html}"</#if>>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="position">Position</label>
                        <div class="col-md-5">
                            <input type="text" class="form-control" name="position"<#if user.role??>
                                   value="${user.role?html}"</#if>>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="city">City</label>
                        <div class="col-md-5">
                            <input type="text" class="form-control" name="city"<#if user.city??>
                                   value="${user.city?html}"</#if>>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-4 control-label" for="country">Country</label>
                        <div class="col-md-5">
                            <select class="form-control" name="country">
                            <#if !user.country??>
                                <option value="" disabled selected>Please Select</option>
                            </#if>
                            <#list countries?keys as countryCode>
                                <option value="${countryCode?html}"<#if user.country?? && user.country == countries[countryCode]>
                                        selected</#if>>${countries[countryCode]?html}</option>
                            </#list>
                            </select>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-md-6 col-md-offset-6">
                    <div class="form-group">
                        <button id="submit-button" class="btn btn-primary pull-right">Apply changes
                        </button>
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>

<div class="row">
    <div class="col-md-8 col-md-offset-2">
        <legend>Group information</legend>
        <div class="row">
            <div class="col-md-4">
                A member of the following groups:
            </div>
            <div class="col-md-4">
                <ul id="groups" class="inline">
                <#list groups as group>
                    <li>${group.name?html}</li>
                </#list>
                </ul>
            </div>
        </div>
    </div>
</div>

<#if is_2fa_enabled??>
<div id="security" class="row">
    <div class="col-md-8 col-md-offset-2">
        <legend>Security</legend>
        <div class="row">
            <div class="col-md-12">
                <b>Two Factor Recovery Codes</b>
                <p>
                    Recovery codes can be used to access your account in the event you lose access to your device and
                    cannot receive two-factor authentication codes.
                </p>
            </div>
        </div>
        <div class="row collapse in recovery-code-list-toggle">
            <div class="col-md-4">
                <button id="recovery-codes-button" class="btn btn-primary">Show recovery codes</button>
            </div>
        </div>
        <div id="recovery-codes" class="collapse recovery-code-list-toggle">
            <div class="row">
                <div class="col-md-12">
                    <div class="panel panel-warning">
                        <div class="panel-body">
                            Put these codes in a safe spot. If you lose your device and don't have the recovery codes
                            you
                            will lose access to your account.
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-md-6">
                    <ul id="recovery-codes-list" class="list-group">
                    </ul>
                </div>
                <div class="col-md-6">
                    <div class="panel panel-danger">
                        <div class="panel-body">
                            <div class="form-group">
                                <button id="generate-codes-button" class="btn btn-danger">Generate new recovery codes
                                </button>
                            </div>
                            <p>Generating new recovery codes will replace the existing codes. The old codes will
                                no longer be usable. </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</#if>

<style>
    #recovery-codes-list {
        font-family: Monospace, serif;
        text-align: center;
    }
</style>

<script type="text/javascript">
    $(function () {
        const submitBtn = $('#submit-button')
        const recoveryCodesBtn = $('#recovery-codes-button')
        const generateCodesBtn = $('#generate-codes-button')
        const form = $('#account-form')
        form.validate()

        $('#reg-password').rules('add', {
            minlength: ${min_password_length?js_string}
        })
        $('#reg-password-confirm').rules('add', {
            equalTo: '#reg-password'
        })

        let showCodes = false
        showCodes = ${show_recovery_codes?c}
        if (showCodes === true) {
            showRecoveryCodes()
        }

    <#-- form events -->
        form.submit(function (e) {
            e.preventDefault()
            e.stopPropagation()
            if (form.valid()) {
                $('.text-error', form).remove()
                $.ajax({
                    type: form.attr('method'),
                    url: form.attr('action'),
                    data: form.serialize(),
                    success: function () {
                        molgenis.createAlert([{'message': 'Your account has been updated.'}], 'success')
                    }
                })
            }
        })
        submitBtn.click(function (e) {
            e.preventDefault()
            e.stopPropagation()
            form.submit()
        })
        $('input', form).add(submitBtn).keydown(function (e) { <#-- use keydown, because keypress doesn't work cross-browser -->
            if (e.which === 13) {
                e.preventDefault()
                e.stopPropagation()
                form.submit()
            }
        })

        function listRecoveryCodes (codes) {
            let listItems = []
            const listTagOpen = '<li class="list-group-item">'
            const listTagClose = '</li>'
            $.each(codes, function (i, code) {
                listItems.push(listTagOpen + code + listTagClose)
            })
            $('#recovery-codes-list').html(listItems.join(''))
        }

        function showRecoveryCodes () {
            $.get('${context_url?html}/recoveryCodes', function (codes) {
                listRecoveryCodes(codes)
            })
            $('.recovery-code-list-toggle').collapse('toggle')
        }

        recoveryCodesBtn.click(showRecoveryCodes)

        generateCodesBtn.click(function (e) {
            $.get('${context_url?html}/generateRecoveryCodes', function (codes) {
                listRecoveryCodes(codes)
            })
        })
    })
</script>
<@footer/>