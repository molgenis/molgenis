<#macro renderTags tags>
    <#if tags?has_content>
        <#list tags as tag>
            <#if tag.objectIri?has_content>
            <span class="label label-primary"><a href='${tag.objectIri?html}'
                                                 target="_blank">${tag.label?html}</a></span>
            <#else>
            <span class="label label-primary">${tag.label?html}</span>
            </#if>
        </#list>
    </#if>
</#macro>

<#macro renderPackage package>
<h2 id="package-${package.id?replace(" ", "_")?html}" class="page-header">${package.id?html}
    <small>(${package.id?html})</small>
</h2>
<div class="package-container">
    <p><#if package.description?has_content>${package.description}</#if></p>
    <@renderTags tags=package.tags/>

<#-- Subpackages -->
    <#if package.children?has_content>
        <h4>Packages</h4>
        <ul class="list-group">
            <#list package.children as subPackage>
                <li class="list-group-item"><a
                        href="#package-${subPackage.id?replace(" ", "_")?html}">${subPackage.id?html}</a>
                </li>
            </#list>
        </ul>
    </#if>

<#-- Entities index -->
    <#if package.entityTypes?has_content>
        <h4 id="entities-${package.id?replace(" ", "_")?html}">Entities</h4>
        <div class="row">
            <div class="col-md-4">
                <ul class="list-group">
                    <#list package.entityTypes as entity>
                        <li class="list-group-item"><a
                                href="#entity-${entity.id?replace(" ", "_")?html}">${entity.label?html}</a>
                        </li>
                    </#list>
                </ul>
                <a href="#package-index">
                    <small>back to top</small>
                </a>
            </div>
        </div>

    <#-- Entities -->
        <#list package.entityTypes as entity>
            <div class="panel" id="entity-${entity.id?replace(" ", "_")?html}">
                <div class="panel-heading">
                    <h3 class="panel-title">${entity.label?html}
                        <small class="panel-title"><#if entity.extends?has_content>
                            extends ${entity.extends.label?html}</#if><#if entity.isAbstract()> (abstract)</#if></small>
                    </h3>
                </div>
                <div class="panel-body">

                    <p><#if entity.description?has_content>${entity.description?html}<#else>No description
                        available</#if></p>
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
                    <a href="#entities-${package.id?replace(" ", "_")?html}">
                        <small>back to entities</small>
                    </a>
                </div>
            </div>
        </#list>
        <a href="#package-index">
            <small>back to top</small>
        </a>
    <#else>
        <p>
            <small>This package does not contain entities</small>
        </p>
    </#if>
</div>
    <#if package.children?has_content>
        <#list package.children as subPackage>
            <@renderPackage subPackage/>
        </#list>
    </#if>
</#macro>
<#macro renderAttribute attribute entity depth>
    <#assign nextDepth = depth + ["x"]/>
    <#assign dataType=attribute.type>
<tr id="attribute-${entity.id?replace(" ", "_")?html}${attribute.name?replace(" ", "_")?html}">
    <td><#list depth as lvl>
        &nbsp;</#list>${attribute.label?html}<#if attribute.isIdAttribute()>
        <em>(id attribute)</em></#if><#if attribute.isLabelAttribute()> <em>(label
        attribute)</em></#if><#if attribute.lookupAttributeIndex??>
        <em>(lookup attribute)</em></#if></td>
    <td><#if attribute.defaultValue?has_content>${attribute.defaultValue?html}</#if></td>
    <td>${dataType?html}<#if dataType == "CATEGORICAL" || dataType == "CATEGORICAL_MREF" || dataType == "MREF" || dataType == "XREF">
        (<a href="#entity-${attribute.refEntityType.id?replace(" ", "_")?html}">${attribute.refEntityType.label?html}</a>)</#if>
    </td>
    <td>
        <#assign constraints = []>
        <#if attribute.isNillable()><#assign constraints = constraints + [ "nullable" ] /></#if>
        <#if attribute.isReadOnly()><#assign constraints = constraints + [ "read-only" ] /></#if>
        <#if attribute.isUnique()><#assign constraints = constraints + [ "unique" ] /></#if>
        <#if !attribute.isVisible()><#assign constraints = constraints + [ "hidden" ] /></#if>
        <#if attribute.isAuto()><#assign constraints = constraints + [ "auto" ] /></#if>
        <#if attribute.isAggregatable()><#assign constraints = constraints + [ "aggregates" ] /></#if>
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
    <#if attribute.type == "COMPOUND">
        <#list attribute.children as attributePart>
            <@renderAttribute attributePart entity nextDepth/>
        </#list>
    </#if>
    <#assign nextDepth = depth/>
</#macro>

<#macro createPackageListItem package>
<li><a href="#package-${package.id?replace(" ", "_")?html}">${package.id?html}</a></li>
    <#if package.children?has_content>
    <li>
        <ul>
            <#list package.children as subPackage>
                    <@createPackageListItem subPackage/>
                </#list>
        </ul>
    </li>
    </#if>
</#macro>