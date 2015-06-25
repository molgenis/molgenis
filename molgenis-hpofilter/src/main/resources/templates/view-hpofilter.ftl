<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=['hpofilter.js']>

<@header css js/>

<#if selectedEntityName??>
<div id="entity-class" class="well clearfix">
    <h3 id="filter-title">HPO Filter</h3>

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
				<div class="input-group">
					<span class="input-group-addon">Save as</span>
					<input type="text" class="form-control" placeholder="Enter target entity name here (optional)" id="name-input">
					<span class="input-group-addon"><span class="glyphicon glyphicon-question-sign" data-toggle="tooltip" data-placement="bottom" title="Leave blank to save as <name>-hpofilter-plugin"></span></span>
				</div>
				<br>
				<button class="btn btn-primary dropdown-toggle btn-block" type="button" id="filter-submit">Submit</button>
				<br>
				<div class="text-center">
				<button class="btn btn-success" id="addgroup">Add group</button>
				<button class="btn btn-danger" id="remgroup">Remove group</button>
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

<script>var selectedEntityName='${selectedEntityName?js_string}';</script>

<#else>
<span>No available catalogues.</span>
<script>var selectedEntityName=undefined;</script>
</#if>
<@footer/>