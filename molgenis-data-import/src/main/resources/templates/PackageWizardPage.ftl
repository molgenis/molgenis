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
        <div class="col-md-4">
            <div class="panel panel-primary">
                <div class="panel-heading">
                    <h4 class="panel-title">Add to package</h4>
                </div>
                <div class="panel-body">
                <#list wizard.packages as packageName>
                    <div class="radio">
                        <label>
                            <input type="radio" name="selectedPackage"
                                   <#if packageName == wizard.packages?first>checked</#if>
                                   value="${packageName?html}">${packageName?html}
                        </label>
                    </div>
                </#list>
                </div>
            </div>
        </div>
    </div>

</form>