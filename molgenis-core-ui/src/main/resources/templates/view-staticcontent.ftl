<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header css=[] js=[] version=2/>

<#if content?has_content>
<div class="row">
  <div class="col-12">
  <#-- Do *not* HTML escape content else text formatting won't work -->
    ${content}
  </div>
</div>
</#if>

<#if isCurrentUserCanEdit?has_content && isCurrentUserCanEdit>
<div class="row">
  <div class="col-12">
    <hr></hr>
    <a href="${context_url?html}/edit" class="btn btn-secondary float-left">Edit page</a>
  </div>
</div>
</#if>

<@footer version=2/>
