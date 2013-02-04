<#--div style="border: 1px #AAA solid;"> 
${input.isNillable()}
<textarea id="${input.id}" <#if input.isNillable() == false>class="required"</#if> name="${input.name}" cols="80" rows="5"> 
<#if input.value?exists>${input.value}</#if>
</textarea>
</div-->
									
<textarea id="${input.name}" name="${input.name}" <#if input.isNillable() == false>class="required" </#if><#if input.readonly>readonly="readonly" </#if> cols="80" rows="5"><#if input.value?exists>${input.value}</#if></textarea>
 
<script type="text/javascript"> 
	var ${input.name}_editor = CodeMirror.fromTextArea(document.getElementById('${input.name}'), {
		width: "551px", 
		height: "139px",
		<#if input.readonly>readOnly: true, </#if>
    	textWrapping: false,
    	iframeClass: "CodeMirror-iframe",
    	parserfile: ${input.parser},
    	stylesheet: "${input.parserStyle}",
    	path: "lib/codemirror-1.0/js/",
    	continuousScanning: 500,
    	autoMatchParens: true,
    	lineNumbers: true,
    	markParen: function(node, ok) { 
        	node.style.backgroundColor = ok ? "#CCF" : "#FCC#";
        	if(!ok) {
            	node.style.color = "red";
        	}
    	},
    	unmarkParen: function(node) { 
        	node.style.backgroundColor = "";
         	node.style.color = "";
    	},
    	indentUnit: 4,
    	onChange: function (n) { ${input.name}_editor.save(); }
	});
  
	$(".CodeMirror-wrapping").resizable();
</script>
