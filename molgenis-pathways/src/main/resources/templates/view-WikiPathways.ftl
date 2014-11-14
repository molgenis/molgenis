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
	<div class="col-md-6 col-md-offset-3">
		<div id="ajax-callback-placeholder"></div> <#--Placeholder for the callback of our javascript ajax call-->
	<#-->	<select class="form-control" id="pathway-select" data-placeholder="Choose a pathway">
			<option val=""></option> <#--HTML5 bug does not show data-placeholder without empty option-->

			
<#-->   			<#list listOfPathwayNames as pathwayName> <#-- For x in y -->
<#-->   				<option value="${pathwayName}">${pathwayName}</option>
   			</#list>
		</select> 
		-->
	</div>
</div>
<div class="row">
	<div class="col-md-6 col-md-offset-0"> <#--Remember, bootstrap grid system consists of 12 columns-->
		<p></p>
		<div id="pathway-svg-image"></div>
	</div>
</div>
<@footer />
