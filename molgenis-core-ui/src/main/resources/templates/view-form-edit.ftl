<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=['molgenis-form.css']>
<#assign js=[]>
<@header css js/>
<script>
	var forms = [];
</script>

<div id="success-message" class="form-group" style="display: none">
	<div class="col-md-12">
		<div class="alert alert-success">
			<button type="button" class="close">&times;</button>
			<strong>${form.title?html} saved.</strong>
		</div>
	</div>
</div>

<#if back??>
	<a href="${back?html}" class="btn btn-default btn-md"><span class="glyphicon glyphicon-chevron-left"></span> Back to list</a>
</#if>

<div class="row">
    <div class="col-md-offset-3 col-md-6">
        <div id="form-container"></div>
    </div>
</div>    

<script>
    React.render(molgenis.ui.Form({
        entity: '${form.metaData.name?lower_case?html}',
<#if form.primaryKey??>
        entityInstance: '<#if form.primaryKey?is_number>${form.primaryKey?c?html}<#else>${form.primaryKey?html}</#if>',
        mode: 'edit'
<#else>
        mode: 'create'
</#if>        
    }), $('#form-container')[0]);
</script>

<@footer/>