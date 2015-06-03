<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['']>
<#assign js=['category-mapping-editor.js', 'bootbox.min.js']>

<@header css js/>

<div class="row">
	<div class="col-md-6">
		<legend>Category mapping editor</legend>
		<h5>
			Map ${sourceAttribute} to ${targetAttribute}. Select the correct category that you want to map the source attribute to from the target attribute dropdown
		</h5>

		<div class="form-group">
			<div class="col-md-2">
				<#-- TODO: show if more than 10 rows --->
				<label>Default value </label>
				<select class="form-control" <#if !hasWritePermission>disabled</#if>>
					<#--If nillable-->
					<option value="9999">None</option>
				<#list targetAttributeRefEntityEntities.iterator() as targetEntity>
					<#--If an algorithm exists and maps to an existing value: selected="selected"-->
					<option value="${targetEntity.get(targetAttributeRefEntityIdAttribute)}">${targetEntity.get(targetAttributeRefEntityLabelAttribute)}</option> 
				</#list>
				</select>
			</div>
		</div>
		
		<table id="category-mapping-table" class="table">
			<thead>
				<th>Source attribute value</th>
				<th>Number of rows containing said value</th>
				<th>Target attribute selection</th>
			</thead>
			<tbody>
			<#--This is for xrefs and categoricals!-->
			<#assign count = 0 />
			<#list sourceAttributeRefEntityEntities.iterator() as sourceEntity>
				<tr id="${sourceEntity.get(sourceAttributeRefEntityIdAttribute)}"> 
					<td>${sourceEntity.get(sourceAttributeRefEntityLabelAttribute)}</td>
					<td>${aggregates[count]!'0'}</td>
					<td>
						<select class="form-control" <#if !hasWritePermission>disabled</#if>>
							<#--If nillable-->
							<option value="9999">None</option>
						<#list targetAttributeRefEntityEntities.iterator() as targetEntity>
							<#--If an algorithm exists and maps to an existing value: selected="selected"-->
							<option value="${targetEntity.get(targetAttributeRefEntityIdAttribute)}">${targetEntity.get(targetAttributeRefEntityLabelAttribute)}</option> 
						</#list>
						</select>
					</td>
				</tr>
			<#assign count = count + 1 />
			</#list>
			
			<#--TODO: Do this for other data types-->
			</tbody>
		</table>

		<hr></hr>

		<button id="save-category-mapping-btn" type="btn" class="btn btn-primary">Save</button>
		<button id="cancel-category-mapping-btn" class="btn btn-default" type="button">Cancel</button>
	</div>
</div>


<@footer />