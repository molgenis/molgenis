<div class="modal-header">
    <h1>Monogenic disease candidate report for ${datasetRepository.getName()}</h1>
</div>
<div class="modal-body" style="background-color: #FFFFFF; ">



<#assign severelateonset = ["AIP", "ALK", "APC", "AXIN2", "BAP1", "BMPR1A", "BRCA1", "CDH1", "CDK4", "CDKN2A", "CEBPA", "CHEK2", "CTHRC1", "CTNNA1", "DICER1", "EGFR", "FH", "FLCN", "GATA2", "KIT", "MAX", "MLH1", "MLH3", "MSH2", "MSH3", "MSH6", "MUTYH", "NF2", "PAX5", "PDGFRA", "PMS2", "PRKAR1A", "RAD51D", "STK11", "TMEM127", "TP53"]>
<#assign abnArValHpoAllFreq = ["EHMT1", "KANSL1", "SKI", "NOTCH2", "GBA", "GNPTG", "GNPTAB", "BRAF", "TMEM70", "FLNA", "FANCL", "FANCA", "SLX4", "ERCC4", "BRCA2", "BRIP1", "FANCB", "FANCG", "FANCF", "FANCI", "FANCD2", "FANCC", "PALB2", "FANCE", "FANCM", "RAD51C", "GLA", "GBA", "EHMT1", "IDUA", "RPS6KA3", "ELN", "FBLN5", "HGD", "FOXF1", "CHRNG", "FBN1", "LTBP2", "ADAMTS10", "FAM58A", "B3GALT6", "B4GALT7", "FBN1", "ADAMTSL2", "HIRA", "GP1BB", "COMT", "TBX1", "UFD1L", "ARVCF"]>

<#assign all_candidates = {}><#-- all genes + counts per category -->



<#list datasetRepository.iterator() as row>

    <#assign geneName = row.getString("INFO_ANN")?split("|")[3]>
	<#assign denovo = row.getInt("INFO_DENOVO")>
	<#if row.getDouble("INFO_1KGMAF")??><#assign tg_maf = row.getDouble("INFO_1KGMAF")><#else><#assign tg_maf = 0></#if>
	<#if row.getDouble("INFO_GONLMAF")??><#assign gonl_maf = row.getDouble("INFO_GONLMAF")><#else><#assign gonl_maf = 0></#if>
	<#if row.getDouble("INFO_EXACMAF")??><#assign exac_maf = row.getDouble("INFO_EXACMAF")><#else><#assign exac_maf = 0></#if>
	
	
	
	<#--[${geneName}:${denovo}]-->


 	<#assign impact = row.getString("INFO_ANN")?split("|")[2]>


	<#if severelateonset?seq_contains(geneName)>
			<#-- skipping this gene -->
	<#else>

		<#if impact == "MODERATE" || impact == "HIGH">
			
			<#if tg_maf lt 0.01 && gonl_maf lt 0.011 && exac_maf lt 0.01>
	
				<#if all_candidates[geneName]??><#assign all_candidates = all_candidates + {geneName : all_candidates[geneName] + denovo }><#else><#assign all_candidates = all_candidates + {geneName : denovo }></#if>
	
		
	 		</#if>
	
		</#if>
	</#if>

</#list>




<#-- HTML -->
<#list all_candidates?keys as geneName>

		<#if all_candidates[geneName] gt 1>
			<#if abnArValHpoAllFreq?seq_contains(geneName)>
				<b> ${geneName}<#--: ${all_candidates[geneName]}--> </b> <br>
			<#else>
				${geneName}<#--: ${all_candidates[geneName]}--> <br>
			</#if>
				
		</#if>

</#list>


<div id="infoDiv"><h4><i>No gene selected</i></h4></div>

</div>
