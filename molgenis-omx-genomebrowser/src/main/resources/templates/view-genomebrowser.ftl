<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js=["dalliance-compiled.js", "patientMutationTable.js", "mutationTable.js", "jquery.dataTables.js"]>
<#assign css=["bootstrap-scoped.css", "dalliance-scoped.css", "patient_mutation_table.css"]>

<@header css js/>
	<script language="javascript">
	<!--instanciate the Dalliance browser with settings from the controller-->
		var dalliance = new Browser({
		${initLocation},
		coordSystem: ${coordSystem},
		chains: ${chains},
		sources: ${sources},
		browserLinks: ${browserLinks},
		searchEndpoint: ${searchEndpoint},
		karyotypeEndpoint: ${karyotypeEndpoint}
		});
	</script>
	<!--The div for the genomeBrowser, name is default for Dalliance-->
	<div id="svgHolder" style="width:1180px"></div>
	<br/><br/>
    <#if genomeBrowserTable??>
        <#if genomeBrowserTable == "Patient">
            <div id="patientMutationTableHolder" style="width:1180px">
                <table id="patientMutationDataTable" border="1">
                </table>
            </div>
        </#if>
        <#if genomeBrowserTable == "Mutation">
            <script language="javascript">
                initMutationTable();
            </script>
            <div id="mutationTableHolder" style="width:1180px">
                <table id="mutationDataTable" border="1">
                </table>
            </div>
        </#if>
    </#if>
<@footer/>
