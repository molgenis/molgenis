<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<script src="<@resource_href "/js/ckeditor/ckeditor.js"/>"></script>
<@header css=[] js=[] version=2/>

<div class="row">
  <div class="col-12">
    <form id="contentForm" method="post" role="form">

      <div class="form-group">
        <div class="col-8 offset-md-2">
                <#if content?has_content>
                  <textarea id="elm1" name="content" form="contentForm"
                            rows="15">${content} <#if succes?has_content>${succes?html}</#if></textarea>
                <#else>
                    <textarea id="elm1" name="content" form="contentForm" rows="15"></textarea>
                </#if>
        </div>
      </div>

      <div class="form-group">
        <div class="col-8 offset-md-2">
          <div class="btn-group float-right">
            <a id="cancelBtn" href="${context_url?html}" class="btn btn-secondary">Close</a>
            <button id="submitBtn" type="submit" class="btn btn-secondary">Save</button>
          </div>
        </div>
      </div>

    </form>
  </div>
</div>

<script>
  CKEDITOR.replace('elm1')
  CKEDITOR.dtd.$removeEmpty['i'] = false
  CKEDITOR.dtd.$removeEmpty['span'] = false
  CKEDITOR.dtd.$removeEmpty['button'] = false
</script>
<@footer version=2/>
