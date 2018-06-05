<form method="post" id="wizardForm" name="wizardForm" action="" role="form">

    <div class="row">
        <div class="col-md-4">
            <div class="panel panel-primary">
                <div class="panel-heading">
                    <h4 class="panel-title">Entities not in a package</h4>
                </div>
                <div class="panel-body">
                <#if wizard.entitiesInDefaultPackage?? && wizard.entitiesInDefaultPackage?size &gt; 0>
                    <#list wizard.entitiesInDefaultPackage as e>
                    ${e?html}<br/>
                    </#list>
                <#else>
                    <i>None</i>
                </#if>
                </div>
            </div>
        </div>
    <#if wizard.entitiesInDefaultPackage?? && wizard.entitiesInDefaultPackage?size &gt; 0>
        <div class="col-md-4">
            <div class="panel panel-primary">
                <div class="panel-heading">
                    <h4 class="panel-title">Add to package</h4>
                </div>
                <div class="panel-body">
                <#list wizard.packages as packageId, packageLabel>
                    <div class="radio">
                        <label>
                            <input type="radio" name="selectedPackage" value="${packageId?html}">${packageLabel?html}
                        </label>
                    </div>
                </#list>
                    <script>
                        $('input[name="selectedPackage"]:first').prop('checked', true);
                    </script>
                </div>
            </div>
        </div>
    </#if>
    </div>

</form>