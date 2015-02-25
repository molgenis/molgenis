<div class="modal-header">
<h1>Monogenic disease candidate report for ${datasetRepository.getName()}</h1>
</div>
<div class="modal-body">


<script type="text/javascript">
	$(".place").click(function () {
	   $(this).toggleClass("green");
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
EXCLUDED_COMPOUND_CANDIDATE,
INCLUDED_DOMINANT,
INCLUDED_DOMINANT_HIGHIMPACT,
INCLUDED_RECESSIVE,
INCLUDED_RECESSIVE_HIGHIMPACT,
INCLUDED_RECESSIVE_COMPOUND,
INCLUDED_OTHER
<-->

<#assign topcandidates = []>

<#assign highcandidates = []>

<#list datasetRepository.iterator() as row>
	<#if
		(
		row.getString("INFO_MONGENDISCAND") == "INCLUDED_DOMINANT_HIGHIMPACT"
		||
		row.getString("INFO_MONGENDISCAND") == "INCLUDED_RECESSIVE_HIGHIMPACT"
		)
		&& row.getDouble("INFO_PHENOMIZERPVAL")??
		&& row.getDouble("INFO_PHENOMIZERPVAL") lt 0.05>
		<#assign topcandidates = topcandidates + [row]>
	</#if>
	
	<#if
		(
		row.getString("INFO_MONGENDISCAND") == "INCLUDED_DOMINANT_HIGHIMPACT"
		||
		row.getString("INFO_MONGENDISCAND") == "INCLUDED_RECESSIVE_HIGHIMPACT"
		)>
		<#assign highcandidates = highcandidates + [row]>
	</#if>
	
	
</#list>





<h2>${topcandidates?size} top candidate<#if topcandidates?size!=1>s</#if>: high impact variants with matching symptoms</h2>
<#list topcandidates as topcandidate>
	${topcandidate.getString("INFO_MONGENDISCAND")}, ${topcandidate.getString("CHROM")}, ${topcandidate.getString("POS")}, Phenomizer p-value ${topcandidate.getDouble("INFO_PHENOMIZERPVAL")}<br>
</#list>

<h2>${highcandidates?size} high impact variants</h2>

<#list highcandidates as highcandidate>
	<div style="display:inline" onmouseover="changeContent('infoDiv', '${highcandidate.getString("INFO_CGDCOND")}, ${highcandidate.getString("#CHROM")}, ${highcandidate.getString("POS")}, ${highcandidate.getString("REF")}, ${highcandidate.getString("ALT")}, <#if highcandidate.getDouble("INFO_PHENOMIZERPVAL")??>${highcandidate.getDouble("INFO_PHENOMIZERPVAL")}</#if>, ${highcandidate.getString("INFO_CGDGIN")}')" onmouseout="changeContent('infoDiv','')" class="place">${highcandidate.getString("INFO_ANN")?split("|")[3]}</div>
</#list>

<div id="infoDiv"> </div>

</div>
<div class="modal-footer">
	<button type="button" class="btn btn-default" data-dismiss="modal">close</button>
</div>