<div class="modal-header">
<h1>Monogenic disease candidate report for ${datasetRepository.getName()}</h1>
</div>
<div class="modal-body">


<script type="text/javascript">
	$(".togglediv_grey").click(function () {
	   $(this).toggleClass("red");
	});
	$(".togglediv_lightgreen").click(function () {
	   $(this).toggleClass("red");
	});
	$(".togglediv_green").click(function () {
	   $(this).toggleClass("red");
	});
	
	function changeContent(id, msg) {
		var el = document.getElementById(id);
		if (id) {
			el.innerHTML = msg;
		}
	}
</script>





<#-->
		EXCLUDED,
		EXCLUDED_BUT_COMPOUND_CANDIDATE,
		INCLUDED_DOMINANT,
		INCLUDED_DOMINANT_HIGHIMPACT,
		INCLUDED_RECESSIVE,
		INCLUDED_RECESSIVE_HIGHIMPACT,
		INCLUDED_RECESSIVE_COMPOUND,
		INCLUDED_RECESSIVE_COMPOUND_HIGHIMPACT,
		INCLUDED_OTHER
<-->


<#assign all_candidate_genes = []>
<#assign dom_high_candidate_genes = {}>
<#assign dom_mod_candidate_genes = {}>
<#assign rec_high_candidate_genes = {}>
<#assign rec_mod_candidate_genes = {}>
<#assign com_high_candidate_genes = {}>
<#assign com_mod_candidate_genes = {}>
<#assign other_candidate_genes = {}>

<#list datasetRepository.iterator() as row>

	<#assign geneName = row.getString("INFO_ANN")?split("|")[3]>
	<#if geneName?length == 0>
		<#break>
	</#if>
	
	
	<#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_DOMINANT_HIGHIMPACT">
		<#if dom_high_candidate_genes[geneName]??>
			<#assign dom_high_candidate_genes = dom_high_candidate_genes + {geneName: dom_high_candidate_genes[geneName] + row } />
		<#else>
			<#assign dom_high_candidate_genes = dom_high_candidate_genes + {geneName : [row]} />
		</#if>
	</#if>
	
	<#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_DOMINANT">
		<#if dom_mod_candidate_genes[geneName]??>
			<#assign dom_mod_candidate_genes = dom_mod_candidate_genes + {geneName : dom_mod_candidate_genes[geneName] + [row] } />
		<#else>
			<#assign dom_mod_candidate_genes = dom_mod_candidate_genes + {geneName : [row]} />
		</#if>
	</#if>
	
	<#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_RECESSIVE_HIGHIMPACT">
		<#if dom_high_candidate_genes[geneName]??>
			<#assign rec_high_candidate_genes = rec_high_candidate_genes + {geneName: rec_high_candidate_genes[geneName] + [row] } />
		<#else>
			<#assign rec_high_candidate_genes = rec_high_candidate_genes + {geneName : [row]} />
		</#if>
	</#if>
	
	<#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_RECESSIVE">
		<#if dom_high_candidate_genes[geneName]??>
			<#assign rec_mod_candidate_genes = rec_mod_candidate_genes + {geneName: rec_mod_candidate_genes[geneName] + [row] } />
		<#else>
			<#assign rec_mod_candidate_genes = rec_mod_candidate_genes + {geneName : [row]} />
		</#if>
	</#if>
	
	
</#list>





<h2>${dom_high_candidate_genes?size} candidate<#if dom_high_candidate_genes?size!=1>s</#if>: something</h2>
<#list dom_high_candidate_genes?keys as dom_high_candidate_gene>
	<#--${topcandidate.getString("INFO_MONGENDISCAND")}, ${topcandidate.getString("CHROM")}, ${topcandidate.getString("POS")}, Phenomizer p-value ${topcandidate.getDouble("INFO_PHENOMIZERPVAL")}<br>-->
</#list>

<h2>${dom_high_candidate_genes?size} dom high</h2>
<#list dom_high_candidate_genes?keys as gene>
	<#--<div style="display:inline" onmouseover="changeContent('infoDiv', '${highcandidate.getString("INFO_CGDCOND")}, ${highcandidate.getString("#CHROM")}, ${highcandidate.getString("POS")}, ${highcandidate.getString("REF")}, ${highcandidate.getString("ALT")}, <#if highcandidate.getDouble("INFO_PHENOMIZERPVAL")??>${highcandidate.getDouble("INFO_PHENOMIZERPVAL")}</#if>, ${highcandidate.getString("INFO_CGDGIN")}')" onmouseout="changeContent('infoDiv','')" class="place">${highcandidate.getString("INFO_ANN")?split("|")[3]}</div>-->
	<@printGene gene dom_high_candidate_genes[gene]/>  
</#list>

<h2>${dom_mod_candidate_genes?size} dom mod</h2>
<#list dom_mod_candidate_genes?keys as gene>
	<#--<div style="display:inline" onmouseover="changeContent('infoDiv', '${highcandidate.getString("INFO_CGDCOND")}, ${highcandidate.getString("#CHROM")}, ${highcandidate.getString("POS")}, ${highcandidate.getString("REF")}, ${highcandidate.getString("ALT")}, <#if highcandidate.getDouble("INFO_PHENOMIZERPVAL")??>${highcandidate.getDouble("INFO_PHENOMIZERPVAL")}</#if>, ${highcandidate.getString("INFO_CGDGIN")}')" onmouseout="changeContent('infoDiv','')" class="place">${highcandidate.getString("INFO_ANN")?split("|")[3]}</div>-->
	<@printGene gene dom_mod_candidate_genes[gene]/>  
</#list>

<h2>${rec_high_candidate_genes?size} rec high</h2>
<#list rec_high_candidate_genes?keys as gene>
	<#--<div style="display:inline" onmouseover="changeContent('infoDiv', '${highcandidate.getString("INFO_CGDCOND")}, ${highcandidate.getString("#CHROM")}, ${highcandidate.getString("POS")}, ${highcandidate.getString("REF")}, ${highcandidate.getString("ALT")}, <#if highcandidate.getDouble("INFO_PHENOMIZERPVAL")??>${highcandidate.getDouble("INFO_PHENOMIZERPVAL")}</#if>, ${highcandidate.getString("INFO_CGDGIN")}')" onmouseout="changeContent('infoDiv','')" class="place">${highcandidate.getString("INFO_ANN")?split("|")[3]}</div>-->
	<@printGene gene rec_high_candidate_genes[gene]/> 
</#list>

<h2>${rec_mod_candidate_genes?size} rec mod</h2>
<#list rec_mod_candidate_genes?keys as gene>
	<#--<div style="display:inline" onmouseover="changeContent('infoDiv', '${highcandidate.getString("INFO_CGDCOND")}, ${highcandidate.getString("#CHROM")}, ${highcandidate.getString("POS")}, ${highcandidate.getString("REF")}, ${highcandidate.getString("ALT")}, <#if highcandidate.getDouble("INFO_PHENOMIZERPVAL")??>${highcandidate.getDouble("INFO_PHENOMIZERPVAL")}</#if>, ${highcandidate.getString("INFO_CGDGIN")}')" onmouseout="changeContent('infoDiv','')" class="place">${highcandidate.getString("INFO_ANN")?split("|")[3]}</div>-->
	<@printGene gene rec_mod_candidate_genes[gene]/>	 
</#list>

<div id="infoDiv">Gene info</div>

</div>
<div class="modal-footer">
	<button type="button" class="btn btn-default" data-dismiss="modal">close</button>
</div>

	

<#macro printGene geneName geneVariants>
	<div class="togglediv_<#if geneVariants[0].getDouble("INFO_PHENOMIZERPVAL")??><#if geneVariants[0].getDouble("INFO_PHENOMIZERPVAL") lt 0.05>green<#else>lightgreen</#if><#else>grey</#if>" style="display:inline" onmouseover="changeContent('infoDiv', '<#list geneVariants as row>${row.getString("INFO_CGDCOND")}, ${row.getString("#CHROM")}, ${row.getString("POS")}, ${row.getString("REF")}, ${row.getString("ALT")}, <#if row.getDouble("INFO_PHENOMIZERPVAL")??>${row.getDouble("INFO_PHENOMIZERPVAL")}</#if>, ${row.getString("INFO_CGDGIN")}<br></#list>')">${geneName}</div>
</#macro>