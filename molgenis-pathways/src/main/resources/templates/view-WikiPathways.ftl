<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=["select2.css"]>
<#assign js=[
	"pathway.js",
	"select2.min.js"
	]>	
<@header css js/>

<div class="row">
	<div class="col-md-6 col-md-offset-3"> <#--Remember, bootstrap grid system consists of 12 columns-->
		<h5>Search for pathway (gene, description): </h5>
		<form>
			<input type="text" name="geneName" id="gene-search" data-placeholder="enter a gene or description">	
		<#-->	<option val=""></option>-->
			<button type="btn" id="submit-genename-btn">submit</button>
		</form>
		</p>
		<h5>Select a pathway: </h5>
		<input id="pathway-select" value="" /> 
	</div>
</div>
<div class="row">
	<div class="col-md-6"> <#--Remember, bootstrap grid system consists of 12 columns-->
		</p>
		<div id="pathway-svg-image"></div>
	</div>
</div>
<div class="row">
	<div class="col-md-6 col-md-offset-3">	
		</p>
		<form>
			<h5>Select a vcf file:</h5>
   			<select class="form-control" id="dataset-select" data-placeholder="Choose an Entity">
		   		<#if entitiesMeta?has_content>
		        	<#list entitiesMeta.iterator() as entityMeta>
		            	<option value="${entityMeta.name}" <#if entityMeta.name == selectedEntityName> selected</#if>><#if entityMeta.label?has_content>${entityMeta.label}<#else>${entityMeta.name}</#if></option>
		        	</#list>
		   		</#if>
	  		</select>
	  	</p>	  
		<button type="btn" id="submit-vcfFile-btn">submit</button>
		</form> 
	</div>
</div>
<div class="row">
	<div class="col-md-6 col-md-offset-3" id="hiding-select2">	
		</p></p>
		<h5>Select a pathway: </h5>
		<input id="pathway-select2" value="" /> 
	</div>
</div>
<@footer />
