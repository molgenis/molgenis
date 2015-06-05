<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['']>
<#assign js=['category-mapping-editor.js', 'bootbox.min.js']>

<@header css js/>

<div class="row">
	<div class="col-md-6">
		<legend>Category mapping editor</legend>
		<h5>
			Map ${sourceAttribute.label?html} values to ${targetAttribute.label?html} values. Select the correct category that you want to map the source attribute to from the target attribute dropdown
		</h5>

		<#assign showDefault = numberOfSourceAttributes gt 10>

		<#if showDefault>
 			<div class="form-group">
				<div class="col-md-2">	
					<label>Default value </label>
					<select id="default-value" class="form-control" <#if !hasWritePermission>disabled</#if>>
					<#if targetAttribute.nillable>
						<option <#if !categoryMapping.defaultValue?? >selected </#if> value="use-null-value"><em>None</em></option>
					</#if>
					<#list targetAttributeRefEntityEntities.iterator() as targetEntity>
						<option value="${targetEntity.getString(targetAttributeRefEntityIdAttribute)}" 
							<#if categoryMapping.defaultValue??>
								<#if categoryMapping.defaultValue == targetEntity.get(targetAttributeRefEntityIdAttribute)>selected </#if>
							</#if>
							>${targetEntity.get(targetAttributeRefEntityLabelAttribute)}</option> 
					</#list>
					</select>
				</div>
			</div>
		</#if>
		
		<table id="category-mapping-table" class="table">
			<thead>
				<th>${source} attribute value</th>
				<th>Number of rows</th>
				<th>${target} attribute value</th>
			</thead>
			<tbody>
			<#--This is for xrefs and categoricals!-->
			<#assign count = 0 />
			<#list sourceAttributeRefEntityEntities.iterator() as sourceEntity>
				<#assign id = sourceEntity.getString(sourceAttributeRefEntityIdAttribute)>
				<tr id="${id}">
					<td>${sourceEntity.get(sourceAttributeRefEntityLabelAttribute)}</td>
					<td><#if aggregates??>${aggregates[count]!'0'}<#else>NA</#if></td>
					<td>
						<select class="form-control" <#if !hasWritePermission>disabled</#if>>
						<#if showDefault>
							<option value="use-default-option"
								<#if !categoryMapping.map?keys?seq_contains(id) > selected </#if>>use default</option>
						</#if>
						<#if targetAttribute.nillable>
							<#-- if the key exists but the value doesn't, the value is null -->
							<option value="use-null-value"<#if categoryMapping.map?keys?seq_contains(id) && !categoryMapping.map[id]?? > selected </#if>><em>None</em></option>
						</#if>	
						<#list targetAttributeRefEntityEntities.iterator() as targetEntity>
							<option <#if categoryMapping.map[id]?? >
								<#if categoryMapping.map[id]=targetEntity.getString(targetAttributeRefEntityIdAttribute)>selected </#if>
								</#if>
							value="${targetEntity.get(targetAttributeRefEntityIdAttribute)}">${targetEntity.get(targetAttributeRefEntityLabelAttribute)}</option> 
						</#list>
						</select>
						<#--TODO: + button to add a new category to target dropdown-->
					</td>
				</tr>
			<#assign count = count + 1 />
			</#list>
			
			<#if sourceAttribute.nillable>
				<tr id="nullValue">
					<td><em>None</em></td>
					<td><#if aggregates??>${aggregates[count]!'0'}<#else>NA</#if></td>
					<td>
						<select class="form-control" <#if !hasWritePermission>disabled</#if>>
							<#if showDefault>
								<option<#if categoryMapping.nullValueUndefined> selected </#if> value="use-default-option">use default</option>
							</#if>
							<#if targetAttribute.nillable>
								<option<#if !categoryMapping.nullValueUndefined && !categoryMapping.nullValue??> selected </#if> value="use-null-value"><em>None<em></option>
							</#if>
							<#list targetAttributeRefEntityEntities.iterator() as targetEntity>
								<option<#if categoryMapping.nullValue??>
								<#if categoryMapping.nullValue=targetEntity.getString(targetAttributeRefEntityIdAttribute)> selected </#if>
								</#if> value="${targetEntity.get(targetAttributeRefEntityIdAttribute)}">${targetEntity.get(targetAttributeRefEntityLabelAttribute)}</option> 
							</#list>
						</select>
						<#--TODO: + button to add a new category to target dropdown-->
					</td>
				</tr>
			</#if>
			<#--TODO: Do this for other data types-->
			</tbody>
		</table>

		<hr></hr>

		<#--Hidden inputs for the javascript post-->
		<input type="hidden" name="mappingProjectId" value="${mappingProject.identifier}"/>
		<input type="hidden" name="target" value="${target?html}"/>
		<input type="hidden" name="source" value="${source?html}"/>
		<input type="hidden" name="targetAttribute" value="${targetAttribute.name?html}"/>
		<input type="hidden" name="sourceAttribute" value="${sourceAttribute.name?html}"/>

		<button id="save-category-mapping-btn" type="submit" class="btn btn-primary">Save</button>
		<button id="cancel-category-mapping-btn" class="btn btn-default" type="button">Cancel</button>
	</div>
</div>


<@footer />