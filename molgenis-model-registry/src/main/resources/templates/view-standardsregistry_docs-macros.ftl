<#macro renderPackage package>
    <h2 id="package-${package.name}" class="page-header">${package.simpleName} <small>(${package.name})</small></h2>
    <div class="package-container">
        <p><#if package.description?has_content>${package.description}</#if></p>
        <#list package.tags as tag>
        	<#if tag.relation == 'link'>
        		<span class="label label-primary"><a href='${tag.object.label}' target="_blank">${tag.object.label}</a></span>
        	<#else>
        		<span class="label label-primary">${tag.object.label}</span>
        	</#if>
        </#list>
        
        <#-- Subpackages -->
        <#if package.subPackages?has_content>
            <h4>Packages</h4>
            <ul class="list-group">
                <#list package.subPackages as subPackage>
                    <li class="list-group-item"><a href="#package-${subPackage.name}">${subPackage.name}</a></li>
                </#list>
            </ul>    
        </#if>
    
        <#-- Entities index -->
        <#if package.entityMetaDatas?has_content>
            <h4 id="entities-${package.name}">Entities</h4>
            <div class="row">
                <div class="col-md-4">
                    <ul class="list-group">
            <#list package.entityMetaDatas as entity>
                        <li class="list-group-item"><a href="#entity-${entity.name}">${entity.label}</a></li>
            </#list>
                    </ul>
                </div>
            </div>
        
            <#-- Entities -->
            <#list package.entityMetaDatas as entity>
            <div class="panel" id="entity-${entity.name}">
                <div class="panel-heading">
                    <h3 id="entity-${entity.name}" class="panel-title">${entity.label}<small><#if entity.extends?has_content> extends ${entity.extends.label}</#if><#if entity.abstract> (abstract)</#if></small></h3>
                </div>
                <div class="panel-body">
            
                    <p><#if entity.description?has_content>${entity.description}</#if></p>
                    
                    <#-- Entity attributes -->
                    <table class="table">
                        <thead>
                            <th>Attribute</th>
                            <th>Default</th>
                            <th>Type</th>
                            <th>Constraints</th>
                            <th>Description</th>
                        </thead>
                        <tbody>
                            <#list entity.attributes as attribute>
                                <@renderAttribute attribute entity />
                            </#list>
                        </tbody>
                    </table>
                    <a href="#entities-${package.name}"><small>back to entities</small></a>
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
<#macro renderAttribute attribute entity>
    <#assign dataType=attribute.dataType.enumType>
	<tr id="attribute-${entity.name}${attribute.name}">
        <td>${attribute.label}<#if attribute.idAtrribute> <em>(id attribute)</em></#if><#if attribute.labelAttribute> <em>(label attribute)</em></#if><#if attribute.lookupAttribute> <em>(lookup attribute)</em></#if></td>
    	<td><#if attribute.defaultValue?has_content>${attribute.defaultValue}</#if></td>
    	<td>${dataType}<#if dataType == "CATEGORICAL" || dataType == "MREF" || dataType == "XREF"> (<a href="#entity-${attribute.refEntity.name}">${attribute.refEntity.label}</a>)</#if></td>
    	<td>
    	    <#assign constraints = []>
            <#if attribute.nillable><#assign constraints = constraints + [ "nillable" ] /></#if>
       		<#if attribute.readonly><#assign constraints = constraints + [ "read-only" ] /></#if>
        	<#if attribute.unique><#assign constraints = constraints + [ "unique" ] /></#if>
        	<#if attribute.visible><#assign constraints = constraints + [ "visible" ] /></#if>
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
            <#list constraints as constraint>${constraint}<#if constraint_has_next>, </#if></#list>
    	</td>
    	<td><#if attribute.description?has_content>${attribute.description}</#if></td>
	</tr>
    <#if attribute.dataType.enumType == "COMPOUND">
        <#list attribute.attributeParts as attributePart>
            <@renderAttribute attributePart entity/>
        </#list>
    </#if>
</#macro>

<#macro createPackageListItem package>
    <li><a href="#package-${package.name}">${package.name}</a></li>
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