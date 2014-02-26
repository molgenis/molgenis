<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#include "view-fileupload-tab.ftl">
<#include "view-annotationselect-tab.ftl">
<#include "view-filterbyregion-tab.ftl">
<#include "view-filterbyphenotype-tab.ftl">

<#assign css=["annotate-ui.css", "jquery-ui-1.10.3.custom.css", "chosen.css", "bootstrap-fileupload.css"]>
<#assign js=["annotation.ui.js", "jquery-ui-1.10.3.custom.min.js", "chosen.jquery.min.js", "bootstrap-fileupload.js", "jquery.bootstrap.wizard.js"]>

<@header css js />

<div class="row-fluid">
	<div class="span12">
		<div id="rootwizard" class="tabbable tabs-left">
		
			<ul>
			  	<li><a href="#tab1" class="tab1" data-toggle="tab">Select or Upload a Dataset</a></li>
				<li><a href="#tab2" class="tab2" data-toggle="tab">Filter by: Genome</a></li>
				<li><a href="#tab3" class="tab3" data-toggle="tab">Filter by: Phenotype</a></li>
				<li><a href="#tab4" class="tab4" data-toggle="tab">Select your annotation</a></li>
		
				<li>
					<button type="submit" form="execute-annotation-app" class="btn">Go</button>
				</li>
			</ul>
		
			<div class="tab-content" style="height:auto;">
				<#--panel code located in seperate macros for readability-->
		    	<@fileupload_tab />
		    	<@filter_by_genomic_region_tab />
		    	<@filter_by_phenotype_tab />
		    	<@annotation_select_tab />			
			</div>
		</div>
	</div>
</div>

<@footer />
