<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[""]>
<#assign js=['category-mapping-editor.js']>

<@header css js/>

<div class="row">
	<div class="col-md-6">
		<legend>Category mapping editor</legend>
		<h5>Map ${sourceAttribute} to ${targetAttribute}. Select the correct category that you want to map the source attribute to from the target attribute dropdown</h5>
		<#--Only show when selecting xref or categorical-->

		<#-- TODO: show if more than 10 rows --->
		<p>default</p>
		<select <#if !hasWritePermission>disabled</#if>>
			<#--If nillable-->
			<option value="null">Not mapped</option>
			<option value="0">Male</option>
			<option value="1">Female</option>
		</select>
		<table id="category-mapping-table" class="table">
			<thead>
				<th>Source attribute value</th>
				<th>Number of rows</th>
				<th>Target attribute selection</th>
			</thead>
			<tbody>
				
				<tr>
					<td>Male</td>
					<td>12</td>
					<td>
						<select <#if !hasWritePermission>disabled</#if>>
							<#--If nillable-->
							<option value="null">Use default</option>
							<option value="0">Male</option>
							<option value="1">Female</option>
						</select>
					</td>
				</tr>
				<tr>
					<td>Female</td>
					<td>15</td>
					<td>
						<select <#if !hasWritePermission>disabled</#if>>
							<#--If nillable-->
							<option value="null">Use default</option>
							<option value="0">Male</option>
							<option value="1">Female</option>
						</select>
					</td>
				</tr>
				<tr>
					<td>NA</td>
					<td>1</td>
					<td>
						<select <#if !hasWritePermission>disabled</#if>>
							<#--If nillable-->
							<option value="null">None</option>							
							<option value="0">Male</option>
							<option value="1">Female</option>
						</select>
					</td>
				</tr>
			</tbody>
		</table>
		<hr></hr>
		<form id="save-category-mapping-form" method="POST" action="${context_url}/savecategorymapping">
			<input type="hidden" name="mappingProjectId" value="${mappingProject.identifier}"/>
			<input type="hidden" name="target" value="${entityMapping.targetEntityMetaData.name?html}"/>
			<input type="hidden" name="source" value="${entityMapping.name?html}"/>
			<input type="hidden" name="targetAttribute" value="${attributeMapping.targetAttributeMetaData.name?html}"/>

			<button type="submit" class="btn btn-primary">Save</button> 
			<button type="reset" class="btn btn-warning">Reset</button>
			
			<button id="cancel-edit-btn" class="btn btn-default" type="button" onclick="history.back();" value="Back">Cancel</button>
		</form>
	</div>
</div>


<@footer />