<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=["bbmrinltoericmapper.js"]>
<@header css js/>
<div class="row">
    <div class="col-md-10 col-md-offset-1 well">
        <p>Enable BBMRI-NL to BBMRI-ERIC mapper scheduler that nightly converts current BBMRI-NL data.</p>
        <p class="warning">Once enabled the mapper scheduler cannot be disabled</p>
        <div id="enable-mapper-scheduler-btn-container"></span>   
    </div>
</div>