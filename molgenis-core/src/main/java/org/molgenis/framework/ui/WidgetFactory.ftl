<#--date-->
<#macro date name label="null" value=widgetfactory.now() nillable=false readonly=false>
${widgetfactory.date(name,label,value,nillable,readonly)}
</#macro>

<#--datetime-->
<#macro datetime name label="null" value=widgetfactory.now() nillable=false readonly=false>
${widgetfactory.datetime(name,label,value,nillable,readonly)}
</#macro>

<#--string-->
<#macro string name label="null" value="null" nillable=false readonly=false>
${widgetfactory.string(name,label,value,nillable,readonly)}
</#macro>

<#--action-->
<#macro action name label="null">
${widgetfactory.action(name,label)}
</#macro>

<#--int-->
<#macro int name label="null" value="null" nillable=false readonly=false>
${widgetfactory.integer(name,label,value,nillable,readonly)}
</#macro>

<#--double-->
<#macro double name label="null" value="null" nillable=false readonly=false>
${widgetfactory.double(name,label,value,nillable,readonly)}
</#macro>

<#--xref-->
<#macro xref name entity label="null" value="null" nillable=false readonly=false>
${widgetfactory.xref(name,entity,label,value,nillable,readonly)}
</#macro>

<#--mref-->
<#macro mref name entity label="null" value="null" nillable=false readonly=false>
${widgetfactory.mref(name,entity,label,value,nillable,readonly)}
</#macro>

<#--file-->
<#macro file name label="null" value="null" nillable=false readonly=false>
${widgetfactory.file(name,label,value,nillable,readonly)}
</#macro>

<#--bool-->
<#macro bool name label="null" value=false nillable=false readonly=false>
${widgetfactory.bool(name,label,value,nillable,readonly)}
</#macro>

<#--checkbox-->
<#macro checkbox name options optionlabels=[] label="null" value=false nillable=false readonly=false>
${widgetfactory.bool(name,label,value,options,optionlabels,nillable,readonly)}
</#macro>


