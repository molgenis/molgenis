<div class="well">
    <div class="permission-table-container">
        <table class="table table-condensed table-borderless" id="entity-type-rls-table">
            <thead>
            <tr>
                <th>Entity Class</th>
                <th>Row-Level Security Enabled</th>
            </tr>
            </thead>
            <tbody>
            <#list entityTypes as entityType>
            <tr>
                <td>${entityType.label?html}</td>
                <td><input type="checkbox" id="${entityType.id?html}"<#if entityType.rlsEnabled>
                           checked</#if><#if entityType.readonly> disabled</#if>></td>
            </tr>
            </#list>
            </tbody>
        </table>
    </div>
</div>