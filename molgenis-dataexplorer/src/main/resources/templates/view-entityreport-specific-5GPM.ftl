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

<h4>Candidate genes</h4>
<p>
	<div style="display:inline" class="togglediv_green">Green</div> genes have a strong Phenomizer symptom match (<i>p</i> < 0.05), <div style="display:inline" class="togglediv_lightgreen">light green</div> is a weak symptom match (<i>p</i> > 0.05), and <div style="display:inline" class="togglediv_grey">grey</div> genes do not a match. Hover over a gene to see details and the variants for this candidate below. Click on a gene to 'exclude' this candidate by flagging it with a <div style="display:inline" class="togglediv_red">red</div> color.
</p>
<table class="table table-bordered table-condensed table-striped">
	<tr>
		<th></th>
		<th><h4>Dominant</h4></th>
		<th><h4>Recessive</h4></th>
		<th><h4>Compound</h4></th>
	</tr>
	<tr>
		<td><h4>High impact</h4></td>
		<td><@printGenes dom_high_candidate_genes /></td>
		<td><@printGenes rec_high_candidate_genes /></td>
		<td><@printGenes com_high_candidate_genes /></td>
	</tr>
	<tr>
		<td><h4>Moderate impact</h4></td>
		<td><@printGenes dom_mod_candidate_genes /></td>
		<td><@printGenes rec_mod_candidate_genes /></td>
		<td><@printGenes com_mod_candidate_genes /></td>
	</tr>
	<tr>
		<td><h4>Other</h4></td>
		<td colspan="3"><@printGenes other_candidate_genes /></td>
	</tr>
</table>

<div id="infoDiv"><h4><i>No gene selected</i></h4></div>

</div>
<div class="modal-footer">
	<button type="button" class="btn btn-default" data-dismiss="modal">close</button>
</div>

	

<#macro printGenes genes>
	<#list genes?keys as geneName>
		<#--div class="togglediv_<#if genes[geneName][0].getDouble("INFO_PHENOMIZERPVAL")??><#if genes[geneName][0].getDouble("INFO_PHENOMIZERPVAL") lt 0.05>green<#else>lightgreen</#if><#else>grey</#if>" style="display:inline" onmouseover="changeContent('infoDiv', '<#list genes[geneName] as row>${row.getString("INFO_CGDCOND")}, ${row.getString("#CHROM")}, ${row.getString("POS")}, ${row.getString("REF")}, ${row.getString("ALT")}, ${row.getString("INFO_ANN")}, <#if row.getDouble("INFO_PHENOMIZERPVAL")??>${row.getDouble("INFO_PHENOMIZERPVAL")}</#if>, ${row.getString("INFO_CGDGIN")}<br></#list>')">${geneName}</div-->
	
	<@compress single_line=true>
		<div class="togglediv_<#if genes[geneName][0].getDouble("INFO_PHENOMIZERPVAL")??><#if genes[geneName][0].getDouble("INFO_PHENOMIZERPVAL") lt 0.05>green<#else>lightgreen</#if><#else>grey</#if>" style="display:inline" onmouseover="changeContent('infoDiv', '
		<h4>Gene details</h4>
		<table class=&quot;table table-bordered table-condensed table-striped&quot;>
			<tr>
				<th>Name</th>
				<th>Disorder</th>
				<th>Inheritance</th>
				<th>Phenomizer</th>
			</tr>
			<tr>
				<td>${geneName}</td>
				<td>${genes[geneName][0].getString("INFO_CGDCOND")}</td>
				<td>${genes[geneName][0].getString("INFO_CGDINH")}</td>
				<td><#if genes[geneName][0].getDouble("INFO_PHENOMIZERPVAL")??>${genes[geneName][0].getDouble("INFO_PHENOMIZERPVAL")}</#if></td>
			</tr>
		</table>
		<h4>Variant details</h4>
		<table class=&quot;table table-bordered table-condensed table-striped&quot;>
			<tr>
				<th>Chr</th>
				<th>Pos</th>
				<th>Ref</th>
				<th>Alt</th>
				<th>Effect</th>
				<th>Impact</th>
				<th>Genotype</th>
			</tr>
			<#list genes[geneName] as row>
			<tr>
				<td>${row.getString("#CHROM")}</td>
				<td>${row.getString("POS")}</td>
				<td>${row.getString("REF")}</td>
				<td>${row.getString("ALT")}</td>
				<td>${row.getString("INFO_ANN")?split("|")[1]}</td>
				<td>${row.getString("INFO_ANN")?split("|")[2]}</td>
				<td><#list row.getEntities("SAMPLES").iterator() as sample>${sample.getString("GT")} </#list></td>
			</tr>
			</#list>
		</table>
		')">${geneName}</div>
	</@compress>
	
	</#list>
</#macro>