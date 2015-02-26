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
		EXCLUDED_FIRST_OF_COMPOUND,
		EXCLUDED_FIRST_OF_COMPOUND_HIGHIMPACT,
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
<#assign other_candidate_genes = {}>

<#-- these need some post-processing -->
<#assign com_high_candidate_raw = {}>
<#assign com_mod_candidate_raw = {}>
<#assign excluded_mod_compound_raw = {}>
<#assign excluded_high_compound_raw = {}>

<#list datasetRepository.iterator() as row>

	<#assign geneName = row.getString("INFO_ANN")?split("|")[3]>

	<#-- INCLUDED_DOMINANT variants -->
	<#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_DOMINANT_HIGHIMPACT"> 
		<#if dom_high_candidate_genes[geneName]??>
			<#assign dom_high_candidate_genes = dom_high_candidate_genes + {geneName: dom_high_candidate_genes[geneName] + [row] } />
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
	
	<#-- INCLUDED_RECESSIVE variants -->
	<#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_RECESSIVE_HIGHIMPACT">
		<#if rec_high_candidate_genes[geneName]??>
			<#assign rec_high_candidate_genes = rec_high_candidate_genes + {geneName: rec_high_candidate_genes[geneName] + [row] } />
		<#else>
			<#assign rec_high_candidate_genes = rec_high_candidate_genes + {geneName : [row]} />
		</#if>
	</#if>
	<#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_RECESSIVE">
		<#if rec_mod_candidate_genes[geneName]??>
			<#assign rec_mod_candidate_genes = rec_mod_candidate_genes + {geneName: rec_mod_candidate_genes[geneName] + [row] } />
		<#else>
			<#assign rec_mod_candidate_genes = rec_mod_candidate_genes + {geneName : [row]} />
		</#if>
	</#if>

	<#-- INCLUDED_RECESSIVE_COMPOUND variants -->
	<#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_RECESSIVE_COMPOUND_HIGHIMPACT">
		<#if com_high_candidate_raw[geneName]??>
			<#assign com_high_candidate_raw = com_high_candidate_raw + {geneName: com_high_candidate_raw[geneName] + [row] } />
		<#else>
			<#assign com_high_candidate_raw = com_high_candidate_raw + {geneName : [row]} />
		</#if>
	</#if>
	<#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_RECESSIVE_COMPOUND">
		<#if com_mod_candidate_raw[geneName]??>
			<#assign com_mod_candidate_raw = com_mod_candidate_raw + {geneName: com_mod_candidate_raw[geneName] + [row] } />
		<#else>
			<#assign com_mod_candidate_raw = com_mod_candidate_raw + {geneName : [row]} />
		</#if>
	</#if>

	<#-- store EXCLUDED_FIRST_OF_COMPOUND / _HIGHIMPACT that will be retrieved once there is a INCLUDED_RECESSIVE_COMPOUND_HIGHIMPACT or INCLUDED_RECESSIVE_COMPOUND for this gene! -->
	<#-- there should be only 1 such variant per compound candidate gene, or else error -->
	<#if row.getString("INFO_MONGENDISCAND") == "EXCLUDED_FIRST_OF_COMPOUND">
		<#if excluded_mod_compound_raw[geneName]??>
			ERROR: multiple EXCLUDED_FIRST_OF_COMPOUND variants for gene ${geneName} !!
		<#else>
			<#assign excluded_mod_compound_raw = excluded_mod_compound_raw + {geneName : [row] } />
		</#if>
	</#if>
	<#if row.getString("INFO_MONGENDISCAND") == "EXCLUDED_FIRST_OF_COMPOUND_HIGHIMPACT">
		<#if excluded_high_compound_raw[geneName]??>
			ERROR: multiple EXCLUDED_FIRST_OF_COMPOUND_HIGHIMPACT variants for gene ${geneName} !!
		<#else>
			<#assign excluded_high_compound_raw = excluded_high_compound_raw + {geneName : [row] } />
		</#if>
	</#if>
	
	<#-- INCLUDED_OTHER variants -->
	<#if row.getString("INFO_MONGENDISCAND") == "INCLUDED_OTHER">
		<#if other_candidate_genes[geneName]??>
			<#assign other_candidate_genes = other_candidate_genes + {geneName: other_candidate_genes[geneName] + [row] } />
		<#else>
			<#assign other_candidate_genes = other_candidate_genes + {geneName : [row] } />
		</#if>
	</#if>
	
</#list>



<#-- POST PROCESSING OF COMPOUND RECESSIVE VARIANTS INTO HIGH AND MODERATE CANDIDATE GENES -->

<#assign com_high_candidate_genes = {}>
<#assign com_mod_candidate_genes = {}>

<#-- if already a candidate HIGH impact variant, always put under 'compound high', and copy over any MODERATE variants, and the EXCLUDED_FIRST_OF_COMPOUND variant (high or mod impact) -->
<#list com_high_candidate_raw?keys as gene>
	<#-- always add candidate -->
	<#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_raw[gene] } />
	<#-- also add any MODERATE candidates-->
	<#if com_mod_candidate_raw[gene]??>
		<#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_genes[gene] + com_mod_candidate_raw[gene] } />
	</#if>
	<#--include FIRST_OF_COMPOUND: either HIGH or MODERATE variant-->
	<#if excluded_high_compound_raw[gene]??>
		<#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_genes[gene] + excluded_high_compound_raw[gene] } />
	<#else>
		<#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_genes[gene] + excluded_mod_compound_raw[gene] } />
	</#if>
</#list>

<#-- if a candidate MODERATE impact variant, put in HIGH anyway when the EXCLUDED_FIRST_OF_COMPOUND has a HIGH impact.  -->
<#list com_mod_candidate_raw?keys as gene>
	<#-- if FIRST_OF_COMPOUND for this gene was HIGH impact, add this variant to HIGH candidates plus the original moderate variants-->
	<#if excluded_high_compound_raw[gene]??>
		<#if com_high_candidate_genes[gene]??>
			<#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_genes[gene] + excluded_high_compound_raw[gene] } />
		<#else>
			<#assign com_high_candidate_genes = com_high_candidate_genes + {gene : excluded_high_compound_raw[gene] } />
		</#if>
		<#assign com_high_candidate_genes = com_high_candidate_genes + {gene : com_high_candidate_genes[gene] + com_mod_candidate_raw[gene] } />
	<#--FIRST_OF_COMPOUND was a MODERATE, and so is the rest! add them to MODERATE COMPOUND candidates -->
	<#else>
		<#if com_mod_candidate_genes[gene]??>
			<#assign com_mod_candidate_genes = com_mod_candidate_genes + {gene : com_mod_candidate_genes[gene] + excluded_mod_compound_raw[gene] } />
		<#else>
			<#assign com_mod_candidate_genes = com_mod_candidate_genes + {gene : excluded_mod_compound_raw[gene] } />
		</#if>
		
		<#assign com_mod_candidate_genes = com_mod_candidate_genes + {gene : com_mod_candidate_genes[gene] + com_mod_candidate_raw[gene]} />
	</#if>
</#list>


<#-- HTML -->

<h2>${dom_high_candidate_genes?size} candidate<#if dom_high_candidate_genes?size!=1>s</#if>: something</h2>
<#list dom_high_candidate_genes?keys as dom_high_candidate_gene>
	<#--${topcandidate.getString("INFO_MONGENDISCAND")}, ${topcandidate.getString("CHROM")}, ${topcandidate.getString("POS")}, Phenomizer p-value ${topcandidate.getDouble("INFO_PHENOMIZERPVAL")}<br>-->
</#list>

<h2>${dom_high_candidate_genes?size} dom high</h2>
<#list dom_high_candidate_genes?keys as gene><@printGene gene dom_high_candidate_genes[gene]/></#list>

<h2>${dom_mod_candidate_genes?size} dom mod</h2>
<#list dom_mod_candidate_genes?keys as gene><@printGene gene dom_mod_candidate_genes[gene]/></#list>

<h2>${rec_high_candidate_genes?size} rec high</h2>
<#list rec_high_candidate_genes?keys as gene><@printGene gene rec_high_candidate_genes[gene]/></#list>

<h2>${rec_mod_candidate_genes?size} rec mod</h2>
<#list rec_mod_candidate_genes?keys as gene><@printGene gene rec_mod_candidate_genes[gene]/></#list>

<h2>${com_high_candidate_genes?size} comp high</h2>
<#list com_high_candidate_genes?keys as gene><@printGene gene com_high_candidate_genes[gene]/></#list>

<h2>${com_mod_candidate_genes?size} comp mod</h2>
<#list com_mod_candidate_genes?keys as gene><@printGene gene com_mod_candidate_genes[gene]/></#list>

<h2>${other_candidate_genes?size} other</h2>
<#list other_candidate_genes?keys as gene><@printGene gene other_candidate_genes[gene]/></#list>


<div id="infoDiv">Gene info</div>

</div>
<div class="modal-footer">
	<button type="button" class="btn btn-default" data-dismiss="modal">close</button>
</div>

	

<#macro printGene geneName geneVariants>
	<div class="togglediv_<#if geneVariants[0].getDouble("INFO_PHENOMIZERPVAL")??><#if geneVariants[0].getDouble("INFO_PHENOMIZERPVAL") lt 0.05>green<#else>lightgreen</#if><#else>grey</#if>" style="display:inline" onmouseover="changeContent('infoDiv', '<#list geneVariants as row>${row.getString("INFO_CGDCOND")}, ${row.getString("#CHROM")}, ${row.getString("POS")}, ${row.getString("REF")}, ${row.getString("ALT")}, ${row.getString("INFO_ANN")}, <#if row.getDouble("INFO_PHENOMIZERPVAL")??>${row.getDouble("INFO_PHENOMIZERPVAL")}</#if>, ${row.getString("INFO_CGDGIN")}<br></#list>')">${geneName}</div>
</#macro>