<div class="modal-header">
<h1>Tessa report for ${datasetRepository.getName()}</h1>
</div>
<div class="modal-body">

<table>

<#-- Example of a custom report. We get the Phenomizer value for each unique gene. -->

<#assign geneVariantMap = {}>

<#list datasetRepository.iterator() as row>
	<#assign geneName = row.getString("INFO_ANN")?split("|")[3]>
	<#if !geneVariantMap[geneName]??>
		<#if row.getDouble("INFO_PHENOMIZERPVAL")??>
		<#-- save 1 variant only -->
			<#assign geneVariantMap = geneVariantMap + {geneName : [row]} />
		</#if>
	</#if>	
</#list>

<#-- Print results in a table -->
<table>
<#list geneVariantMap?keys as gene>
	<tr>
		<td>${gene}</td>
		<td>${geneVariantMap[gene][0].getDouble("INFO_PHENOMIZERPVAL")}</td>
	</tr>
</#list>
</table>

