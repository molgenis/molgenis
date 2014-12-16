<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=["additional-methods.min.js", "vcfimporter.js"]>
<@header css js/>
<form name="vcf-importer-form" class="form-horizontal" action="${context_url?html}/import" method="POST" enctype="multipart/form-data">
	<div class="alert alert-warning"><strong>Warning</strong> INFO fields are only available in other plugins if specified in VCF metadata</div>
	<div class="well">
        <div class="form-group">
            <label class="col-md-2 control-label" for="name">Dataset name *</label>
            <div class="col-md-10">
                <input type="text" name="name" required>
            </div>
        </div>
        <div class="form-group">
            <label class="col-md-2 control-label" for="file">VCF file *</label>
            <div class="col-md-10 controls">
                <input type="file" id="file" name="file" required>
                <span class="help-inline">Accepted formats are vcf and vcf.gz.</span>
            </div>
        </div>
        <div class="form-group">
            <div class="col-md-10 col-md-offset-2">
                <button type="submit" class="btn btn-primary">Import</button>
            </div>
        </div>
    </div>
</form>
<@footer/>