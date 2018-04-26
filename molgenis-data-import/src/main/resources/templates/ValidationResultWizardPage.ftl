<#if wizard.validationMessage??>
<script>
    $(function () {
        $('.bwizard-steps').after($('<div class="alert alert-block alert-danger">${wizard.validationMessage?js_string}</div>'));
        $('.pager .next').addClass('disabled');
    });
</script>
</#if>

<form method="post" id="wizardForm" name="wizardForm" action="">
<div>

<#if wizard.entitiesImportable??>

    <table class="table table-bordered table-condensed pull-left" style="width: 25%;margin-left:50px">
        <thead>
        <tr>
            <th colspan="2" style="text-align: center;"><h4>Entities</h4></th>
        </tr>
        <tr>
            <th>Name</th>
            <th>Importable</th>
        </tr>
        </thead>
        <tbody>
            <#list wizard.entitiesImportable?keys as entity>
            <tr>
                <td>${entity?html}</td>

                <#if wizard.entitiesImportable[entity] == true>
                    <td class="alert alert-success" style="text-align: center;">Yes</td>
                <#else>
                    <td class="alert alert-danger" style="text-align: center;">No</td>
                </#if>

            </tr>
            </#list>
        </tbody>
    </table>
    <div class="clearfix"></div>
</div>

    <table class="table table-bordered table-condensed">
        <thead>
        <tr>
            <th colspan="5" style="text-align: center;"><h4>Entity fields</h4></th>
        </tr>
        <tr>
            <th>Name</th>
            <th>Detected</th>
            <th>Required</th>
            <th>Available</th>
            <th>Unknown</th>
        </tr>
        </thead>

        <#list wizard.entitiesImportable?keys as entity>
            <#if wizard.entitiesImportable[entity] == true
            && ((wizard.fieldsDetected[entity]?? && wizard.fieldsDetected[entity]?size gt 0)
            || (wizard.fieldsRequired[entity]?? && wizard.fieldsRequired[entity]?size gt 0)
            || (wizard.fieldsAvailable[entity]?? && wizard.fieldsAvailable[entity]?size gt 0)
            || (wizard.fieldsUnknown[entity]?? && wizard.fieldsUnknown[entity]?size gt 0))>
                <tr>
                    <td>${entity?html}</td>
                    <#if wizard.fieldsDetected[entity]?? && wizard.fieldsDetected[entity]?size gt 0>
                        <td class="alert alert-success">
                            <#list wizard.fieldsDetected[entity] as field>
                            ${field?html}<#if field_has_next>, </#if>
                            </#list>
                        </td>
                    <#else>
                        <td class="alert alert-danger">No fields detected</td>
                    </#if>

                    <#if wizard.fieldsRequired[entity]?? && wizard.fieldsRequired[entity]?size gt 0>
                        <td class="alert alert-danger">
                            <#list wizard.fieldsRequired[entity] as field>
                            ${field?html}<#if field_has_next>, </#if>
                            </#list>
                        </td>
                    <#else>
                        <td class="alert alert-success">No missing fields</td>
                    </#if>


                    <#if wizard.fieldsAvailable[entity]?? && wizard.fieldsAvailable[entity]?size gt 0>
                        <td class="alert alert-info">
                            <#list wizard.fieldsAvailable[entity] as field>
                            ${field?html}<#if field_has_next>, </#if>
                            </#list>
                        </td>
                    <#else>
                        <td class="alert alert-success">No optional fields</td>
                    </#if>


                    <#if wizard.fieldsUnknown[entity]?? && wizard.fieldsUnknown[entity]?size gt 0>
                        <td class="alert alert-warning">
                            <#list wizard.fieldsUnknown[entity] as field>
                            ${field?html}<#if field_has_next>, </#if>
                            </#list>
                        </td>
                    <#else>
                        <td class="alert alert-success">No unknown fields</td>
                    </#if>

                </tr>
            </#if>
        </#list>
    </table>
</#if>

</form>
