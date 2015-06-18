<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['style.css', 'foundation.css']>
<#assign js=['scripts/modernizr.js']>

<@header css js/>
	<script data-main="/js/scripts/main" src="/js/scripts/require.js"></script>
	
	<script>
   		CSRF_TOKEN = "dGG+9uVIBdMzc4FrzD3zHhO2R6IzI2dop9FQWO4HH8i8LE0jiQA+qEBqFoDWZP7Tp6PA/BIlcvs9hT/8";
  	</script>

	<main>
      <div id="viewer"></div>
      <div id="side">
        <div id="marginalia"></div>
      </div>
	</main>
<@footer />