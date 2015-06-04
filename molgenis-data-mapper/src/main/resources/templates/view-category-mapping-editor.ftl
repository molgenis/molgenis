<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['']>
<#assign js=['category-mapping-editor.js', 'bootbox.min.js']>

<@header css js/>

<div class="row">
	<div class="col-md-6">
		<legend>Category mapping editor</legend>
		<#if categoryMapping??><code>${categoryMapping.algorithm}</code></#if>
		<h5>
			Map ${sourceAttribute.label?html} values to ${targetAttribute.label?html} values. Select the correct category that you want to map the source attribute to from the target attribute dropdown
		</h5>

		<#if numberOfSourceAttributes gt 10> 
 			<div class="form-group">
				<div class="col-md-2">	
					<label>Default value </label>
					<select id="default-value" class="form-control" <#if !hasWritePermission>disabled</#if>>
					<#if targetAttribute.nillable>
						<option <#if !categoryMapping.defaultValue?? >selected </#if> val="">None</option>
					</#if>
					<#list targetAttributeRefEntityEntities.iterator() as targetEntity>
						<option value="${targetEntity.getString(targetAttributeRefEntityIdAttribute)}" 
							<#if categoryMapping.defaultValue??>
								<#if categoryMapping.defaultValue = targetEntity.getString(targetAttributeRefEntityIdAttribute) >selected </#if>
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
				<tr id="${sourceEntity.get(sourceAttributeRefEntityIdAttribute)}"> 
					<td>${sourceEntity.get(sourceAttributeRefEntityLabelAttribute)}</td>
					<td><#if aggregates??>${aggregates[count]!'0'}<#else>NA</#if></td>
					<td>
						<select class="form-control" <#if !hasWritePermission>disabled</#if>>
						<#if numberOfSourceAttributes gt 10>
							<option val="use-default-option" 
								<#if !categoryMapping?keys?seq_contains(sourceEntity.getString(sourceAttributeRefEntityIdAttribute)) >selected </#if>
								>use default</option>
						</#if>
						<#if targetAttribute.nillable>
							<option val="" <#if !categoryMapping.map[sourceEntity.getString(sourceAttributeRefEntityIdAttribute)]?? >selected </#if>>None</option>
						</#if>	
						<#list targetAttributeRefEntityEntities.iterator() as targetEntity>
							<option <#if categoryMapping.map[sourceEntity.getString(sourceAttributeRefEntityIdAttribute)]?? >
								<#if categoryMapping.map[sourceEntity.getString(sourceAttributeRefEntityIdAttribute)]=targetEntity.getString(targetAttributeRefEntityIdAttribute)>selected </#if>
								</#if>
							value="${targetEntity.get(targetAttributeRefEntityIdAttribute)}">${targetEntity.get(targetAttributeRefEntityLabelAttribute)}</option> 
						</#list>
						</select>
						<#--TODO: + button to add a new category to target dropdown-->
					</td>
				</tr>
			<#assign count = count + 1 />
			</#list>
			
			<#if targetAttribute.nillable>
				<tr id="nullValue">
					<td><i>no value</i></td>
					<td><#if aggregates??>${aggregates[count]!'0'}<#else>NA</#if></td>
					<td>
						<select class="form-control" <#if !hasWritePermission>disabled</#if>>
							<#if numberOfSourceAttributes gt 10>
								<option <#if categoryMapping.nullValue?? >
								<#if categoryMapping.nullValue=targetEntity.getString(targetAttributeRefEntityIdAttribute)>selected </#if>
								</#if> value="use-default-option">use default</option>
							</#if>
							<#if targetAttribute.nillable>
								<option val="">None</option>
							</#if>
							<#list targetAttributeRefEntityEntities.iterator() as targetEntity>
								<option <#if categoryMapping.nullValue?? >
								<#if categoryMapping.nullValue=targetEntity.getString(targetAttributeRefEntityIdAttribute)>selected </#if>
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