<!--
In cog wheel of data explorer say sys_md_Attribute:model-documentation
-->
<#assign package=[]/>
<#assign entity=[]/>
<#assign entity2=[]/>
<#list datasetRepository?sort_by("entity") as p>
    <#if !package?seq_contains(p.entity.package.id)>
      <h1>Package: ${p.entity.package.label}</h1>
      <p><i>description:</i> <#if p.entity.package.description??>${p.entity.package.description}<#else>NA</#if></p>
        <#assign package = package + [p.entity.package.id]/>
      <!--figure-->
      <img src="http://yuml.me/diagram/plain;dir:TB;scale:120/class/<@compress single-line=true>
<#list datasetRepository?sort_by("entity") as e2>
<#if e2.entity.package.id == p.entity.package.id>
<#if !entity2?seq_contains(e2.entity.id)>
<#assign entity2 = entity2 + [e2.entity.id]/>
[${e2.entity.label}],
<#if e2.entity.extends??>
[${e2.entity.extends.label}]^-[${e2.entity.label}],
</#if>
</#if>
<#if e2.refEntityType??>
[${e2.entity.label}]-${e2.name}(<#if e2.nillable>0<#else>1</#if>..<#if e2.getString("type")?contains("mref")>*<#else>1</#if>)>[${e2.refEntityType.label}],
</#if>
</#if>
</#list></@compress>"/>
      <!--docs of each table-->
        <#list datasetRepository as e>
            <#if e.entity.package.id == p.entity.package.id && !entity?seq_contains(e.entity.id)>
              <h2>${e.entity.label}</h2>
              <p><i>description:</i> <#if e.entity.description??>${e.entity.description}<#else>NA</#if></p>
                <#if e.entity.extends??><p><i>extends: ${e.entity.extends.label}</i></p></#if>
                <#assign entity = entity + [e.entity.id]/>
              Attributes:
              <ul>
                  <#list datasetRepository?sort_by("entity") as a>
                      <#if e.entity.id == a.entity.id>
                        <li><b>${a.label}</b><#if a.nillable = false>*</#if>: ${a.type}<#if a.refEntityType??>(${a.refEntityType.label})</#if><#if a.description??> - <i>${a.description}</#if></i></li>
                      </#if>
                  </#list>
              </ul>
            </#if>
        </#list>
    </#if>
</#list>
