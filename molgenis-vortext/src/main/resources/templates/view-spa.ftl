<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['spa.css']>
<#assign js=[]>

<@header css js/>
	<script data-main="/js/main" src="/js/require.js"></script>
	
	<script>
   		CSRF_TOKEN = "dGG+9uVIBdMzc4FrzD3zHhO2R6IzI2dop9FQWO4HH8i8LE0jiQA+qEBqFoDWZP7Tp6PA/BIlcvs9hT/8";
  	</script>

	<div id="createForm"></div>
	<div class="row" id="upload">
		<div class="col-md-12">
			<input accept="pdf" style="display:none" name="file" type="file" id="file" />
			<button type="button" id="uploadPdfButton" class="btn btn-primary">Upload a new pdf</button>
		</div>
	</div>

	<div class="row">
		<div class="col-md-12">
			<main>
      			<div id="viewer"></div>
      			<div id="side">
        			<div id="marginalia"></div>
      			</div>
			</main>
		</div>
	</div>

<@footer />