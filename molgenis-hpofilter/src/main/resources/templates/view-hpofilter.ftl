<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=['hpofilter.js']>

<@header css js/>

<#if selectedEntityName??>
<div id="entity-class" class="well clearfix">
    <h3 id="filter-title">HPO Filter</h3>
    <span id="header-tip">Select an entity to begin</span>

    <#if showEntitySelect?string('true', 'false') == 'true'>
        <div class="dropdown pull-right">
            <button class="btn btn-default dropdown-toggle" type="button" id="dropdown-menu-entities" data-toggle="dropdown">
                Choose an entity <span class="caret"></span>
            </button>
            <ul class="dropdown-menu scrollable-menu" role="menu" aria-labelledby="dropdown-menu-entities">
                <#list entitiesMeta as entityMeta>
                    <li role="presentation">
                        <a role="menuitem" tabindex="-1" href="#" id="/api/v1/${entityMeta.name?html}" class="entity-dropdown-item">${entityMeta.label?html}</a>
                    </li>
                </#list>
            </ul>
        </div>
    </#if>
</div>

<div class="well clearfix">
	<div class="row">
		<div class="col-md-4">
			<div class="dropdown">
				<div class="input-group" id="name-input-group">
					<span class="input-group-addon">Save as</span>
					<input type="text" class="form-control" placeholder="Enter target entity name here (optional)" id="name-input" data-toggle="tooltip" data-placement="bottom" title="Leave blank to save as <name>_hpofilter_plugin">
  					<span class="form-control-feedback" aria-hidden="true" id="name-input-icon"></span>
				</div>
				<br>
				<button class="btn btn-primary dropdown-toggle btn-block" type="button" id="filter-submit">Submit</button>
				<br>
				<div class="text-center">
					<div class="btn-group">
						<button class="btn btn-success" id="addgroup">Add group</button>
						<button class="btn btn-primary" data-toggle="modal" data-target="#grouphelpmodal"><span class="glyphicon glyphicon-question-sign"></span></button>
					</div>
				</div>
				<br>
				<div id="inputs">
				</div>
				<ul class="dropdown-menu scrollable-menu" id="ac-menu">
					<li>1</li>
					<li>2</li>
					<li>3</li>
					<li>4</li>
					<li>5</li>
				</ul>
			</div>
		</div>
	
		<div class="col-md-8" id="results-col">
			Results go here maybe
		</div>
	</div>
</div>

<div class="modal fade" id="grouphelpmodal">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title">Help: term grouping</h4>
			</div>
			<div class="modal-body">
				<p>The HPO filter will filter genes that are common between HPO terms in a group.</p>
				
				<p>Example:</p>
				
				<p>Group 1 has terms HP:1 and HP:2.<br>
				HP:1 contains genes A and B, while HP:2 contains B and C.<br>
				Group 1 will only contain gene B.</p>
				
				<p>Filtering is done using groups rather than individual terms. If you wish to filter by individual terms, assign one term per group.</p>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
			</div>
		</div>
	</div>
</div>

<script>var selectedEntityName='${selectedEntityName?js_string}';</script>

<#else>
<span>No available catalogues.</span>
<script>var selectedEntityName=undefined;</script>
</#if>
<@footer/>