<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=["idcardbiobankindexer.js"]>
<@header css js/>
<div class="row">
	<div class="col-md-10 col-md-offset-1 well">
		<legend>RD-Connect ID-Card indexer</legend>
        <div class="row">
            <div class="col-md-6">
                <h4>Introduction</h4>
                <p><a href="http://catalogue.rd-connect.eu/" target="_blank">ID-Card</a> is an online catalogue listing biobanks and patient registries connected to <a href="http://rd-connect.eu/" target="_blank">RD-Connect</a>.
                <p>MOLGENIS indexes biobanks and patient registries from ID-Card such that they can be queried, <a href="/menu/main/dataexplorer?entity=${id_card_biobank_registry_entity_name?html}" target="_blank">displayed</a> and referred to from other entities. Querying biobank and patient registries is performed on the index build by MOLGENIS, the resulting entities are retrieved <strong>live</strong> from ID-Card for display.</p>
                <h4>How to use</h4>
                <p>Indexing biobanks and patient registries is disabled by default and can be enabled/disabled by opening the settings panel (by selecting the settings cog on the top right). Additionally the index frequency can be changed in the settings panel.</p>
                <p>Select the Reindex button in case you want to perform a manual index rebuild. Note that reindexing might take a couple of minutes and does not cancel any scheduled index rebuilds.<p>
                <span id="index-btn-container"></span>
            </div>
            <div class="col-md-6">
                <h4>Indexing history</h4>
                <div id="index-event-table-container"></div>
            </div>
	</div>
</div>
<@footer/>
