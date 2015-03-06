<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=["jquery-ui-1.9.2.custom.min.css", "select2.css", "jquery.molgenis.table.css", "analysis.css"]>
<#assign js=["lodash.js", "backbone-min.js", "geometry.min.js", "vectorizer.min.js", "joint.clean.min.js","joint.layout.DirectedGraph.min.js", "jquery-ui-1.9.2.custom.min.js", "select2-patched.js", "jquery.bootstrap.pager.js", "jquery.molgenis.table.js", "analysis.js"]>

<@header css js/>
<#-- Analysis overview -->
<div class="row" id="analysis-overview-container">
	<div class="col-md-offset-2 col-md-8">
<#include "view-analysis-overview.ftl">		
	</div>
</div>
<#-- Analysis details -->
<div class="row hidden" id="analysis-details-container">
	<div class="col-md-offset-2 col-md-8">
<#include "view-analysis-details.ftl">
	</div>
</div>
<#if analysisId??>
<script>
	$(function() {
		molgenis.analysis.changeAnalysis("${analysisId?js_string}");
	});
</script>
</#if>
<@footer/>