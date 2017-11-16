<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=["ckeditor/ckeditor.js"]>
<@header css js/>
<div class="row">
    <div class="col-md-12">
        <form id="contentForm" method="post" role="form">

            <div class="form-group">
                <div class="col-md-8 col-md-offset-2">
                <#if content?has_content>
                    <textarea id="elm1" name="content" form="contentForm"
                              rows="15">${content} <#if succes?has_content>${succes?html}</#if></textarea>
                <#else>
                    <textarea id="elm1" name="content" form="contentForm" rows="15"></textarea>
                </#if>
                </div>
            </div>

            <div class="form-group">
                <div class="col-md-8 col-md-offset-2">
                    <div class="btn-group pull-right">
                        <a id="cancelBtn" href="${context_url?html}" class="btn btn-default">Close</a>
                        <button id="submitBtn" type="submit" class="btn btn-default">Save</button>
                    </div>
                </div>
            </div>

        </form>
    </div>
</div>

<script>
    CKEDITOR.replace('elm1');
    CKEDITOR.dtd.$removeEmpty['i'] = false;
    CKEDITOR.dtd.$removeEmpty['span'] = false;
    CKEDITOR.dtd.$removeEmpty['button'] = false;
</script>
<@footer/>
