<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["bootstrap-fileupload.min.css", "catalogue-chooser.css", "ontology-matcher.css","biobank-connect.css", "mapping-manager.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "common-component.js", "catalogue-chooser.js", "ontology-annotator.js", "ontology-matcher.js", "biobank-connect.js", "mapping-manager.js",  "simple_statistics.js"]>
<@header css js/>
	<#include viewName />
	<div id="control-container" class="row-fluid div-pager">
		<div class="span1">
			<ul class="pager">
			  <li id="reset-button"><a href="#">Reset</a></li>
			</ul>
		</div>
		<div class="offset3 span4">
			<ul class="pager">
		  		<li id="prev-button"><a href="#">Previous</a></li>
				<li id="next-button"><a href="#">Next</a></li>
			</ul>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function(){
			var molgenis = window.top.molgenis;
			molgenis.setContextURL('${context_url}');
		});
	</script>
<@footer/>