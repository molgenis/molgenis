<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['font-awesome.min.css','foundation.css','style.css','spa.css']>
<#assign js=['bootstrap.min.js']>

<@header css js/>
	<script data-main="/js/main" src="/js/require.js"></script>
	
	<script>
		//TODO if vortex server is running
   		CSRF_TOKEN = "dummy";
   		FILE_META_ID = "${publicationPdfFileMetaId!}";
   	</script>

	<div class="row">
		<div class="col-md-12">
			<div id="buttonBar"></div>
      			
			<div id="viewer"></div>
			
      		<div id="side">
      			<div class="clearfix"></div>
        		<div id="marginalia"></div>
      		</div>
		</div>
	</div>
	
	
<@footer />