<#macro renderTags tags>
	<#if tags?has_content>
		<#list tags as tag>
			<#if tag.object.iri?has_content>
				<span class="label label-primary"><a href='${tag.object.iri?html}' target="_blank">${tag.object.label?html}</a></span>
			<#else>
				<span class="label label-primary">${tag.object.label?html}</span>
			</#if>
		</#list>
	</#if>
</#macro>

<#macro renderPackage package>
    <h2 id="package-${package.name?replace(" ", "_")?html}" class="page-header">${package.simpleName?html} <small>(${package.name?html})</small></h2>
    <div class="package-container">
        <p><#if package.description?has_content>${package.description}</#if></p>
        <@renderTags tags=package.tags/>
        
        <#-- Subpackages -->
        <#if package.subPackages?has_content>
            <h4>Packages</h4>
            <ul class="list-group">
                <#list package.subPackages as subPackage>
                    <li class="list-group-item"><a href="#package-${subPackage.name?replace(" ", "_")?html}">${subPackage.name?html}</a></li>
                </#list>
            </ul>    
        </#if>
    
        <#-- Entities index -->
        <#if package.entityMetaDatas?has_content>
            <h4 id="entities-${package.name?replace(" ", "_")?html}">Entities</h4>
            <div class="row">
                <div class="col-md-4">
                    <ul class="list-group">
            <#list package.entityMetaDatas as entity>
                        <li class="list-group-item"><a href="#entity-${entity.name?replace(" ", "_")?html}">${entity.label?html}</a></li>
            </#list>
                    </ul>
		            <a href="#package-index"><small>back to top</small></a>
                </div>
            </div>
        
            <#-- Entities -->
            <#list package.entityMetaDatas as entity>
            <div class="panel" id="entity-${entity.name?replace(" ", "_")?html}">
                <div class="panel-heading">
                    <h3 class="panel-title">${entity.label?html}<small class="panel-title"><#if entity.extends?has_content> extends ${entity.extends.label?html}</#if><#if entity.abstract> (abstract)</#if></small></h3>
                </div>
                <div class="panel-body">
            
                    <p><#if entity.description?has_content>${entity.description?html}<#else>No description available</#if></p>
                    <@renderTags tags=tagService.getTagsForEntity(entity)/>
                    
                    <#-- Entity attributes -->
                    <div class="table-responsive">
	                    <table class="table table-condensed">
	                        <thead>
	                            <th>Attribute</th>
	                            <th>Default</th>
	                            <th>Type</th>
	                            <th>Constraints</th>
	                            <th>Description</th>
	                        </thead>
	                        <tbody>
	                            <#assign depth = []/>
	                            <#list entity.attributes as attribute>
	                                <@renderAttribute attribute entity depth/>
	                            </#list>
	                        </tbody>
	                    </table>
	                </div>
                    <a href="#entities-${package.name?replace(" ", "_")?html}"><small>back to entities</small></a>
                </div>
            </div>
            </#list>
        <a href="#package-index"><small>back to top</small></a>
        <#else>
        <p><small>This package does not contain entities</small></p>
        </#if>
    </div>
        <#if package.subPackages?has_content>
            <#list package.subPackages as subPackage>
                <@renderPackage subPackage/>
            </#list>
        </#if>
</#macro>
<#macro renderAttribute attribute entity depth>
    <#assign nextDepth = depth + ["x"]/>
    <#assign dataType=attribute.dataType.enumType>
	<tr id="attribute-${entity.name?replace(" ", "_")?html}${attribute.name?replace(" ", "_")?html}">
        <td><#list depth as lvl>&nbsp;</#list>${attribute.label?html}<#if attribute.idAtrribute> <em>(id attribute)</em></#if><#if attribute.labelAttribute> <em>(label attribute)</em></#if><#if attribute.lookupAttribute> <em>(lookup attribute)</em></#if></td>
    	<td><#if attribute.defaultValue?has_content>${attribute.defaultValue?html}</#if></td>
    	<td>${dataType?html}<#if dataType == "CATEGORICAL" || dataType == "CATEGORICAL_MREF" || dataType == "MREF" || dataType == "XREF"> (<a href="#entity-${attribute.refEntity.name?replace(" ", "_")?html}">${attribute.refEntity.label?html}</a>)</#if></td>
    	<td>
    	    <#assign constraints = []>
            <#if attribute.nillable><#assign constraints = constraints + [ "nillable" ] /></#if>
       		<#if attribute.readonly><#assign constraints = constraints + [ "read-only" ] /></#if>
        	<#if attribute.unique><#assign constraints = constraints + [ "unique" ] /></#if>
        	<#if !attribute.visible><#assign constraints = constraints + [ "hidden" ] /></#if>
        	<#if attribute.auto><#assign constraints = constraints + [ "auto" ] /></#if>
            <#if attribute.aggregateable><#assign constraints = constraints + [ "aggregates" ] /></#if>
        	<#if attribute.range?has_content>
                <#assign range = "range ["> 
                <#if attribute.range.min?has_content>
                    <#assign range = range + "from " + attribute.range.min>
                </#if>
                <#if attribute.range.max?has_content>
                    <#assign range = range + " to " + attribute.range.max>
                </#if>
                <#assign range = range + "]">
                <#assign constraints = constraints + [ range ] />
            </#if>
            <#list constraints as constraint>${constraint?html}<#if constraint_has_next>, </#if></#list>
    	</td>
    	<td class="description-column"><#if attribute.description?has_content>${attribute.description?html}</#if></td>
	</tr>
    <#if attribute.dataType.enumType == "COMPOUND">
        <#list attribute.attributeParts as attributePart>
            <@renderAttribute attributePart entity nextDepth/>
        </#list>
    </#if>
    <#assign nextDepth = depth/>
</#macro>

<#macro createPackageListItem package>
    <li><a href="#package-${package.name?replace(" ", "_")?html}">${package.name?html}</a></li>
    <#if package.subPackages?has_content>
        <li>
            <ul>
                <#list package.subPackages as subPackage>
                    <@createPackageListItem subPackage/>
                </#list>   
            </ul>
        </li>
    </#if>
</#macro>