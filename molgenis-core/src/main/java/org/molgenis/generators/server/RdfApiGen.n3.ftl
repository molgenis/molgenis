<#include "GeneratorHelper.ftl">
@prefix map: <file:///stdout#> .
@prefix db: <> .
@prefix vocab: <http://localhost:8080/molgenis_distro/vocab/resource/> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix d2rq: <http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#> .
@prefix jdbc: <http://d2rq.org/terms/jdbc/> .
@prefix d2r: <http://sites.wiwiss.fu-berlin.de/suhl/bizer/d2r-server/config.rdf#> .

<> a d2r:Server;
    rdfs:label "D2R Server";
    d2r:baseURI <http://localhost:8080/pheno/>;
    d2r:port 8080;
    d2r:documentMetadata [
        rdfs:comment "This comment is custom document metadata.";
    ];
	d2r:vocabularyIncludeInstances true;    
    .

# Should be jndi loaded!
map:database a d2rq:Database;
	d2rq:jdbcDriver "${db_driver}";
	d2rq:jdbcDSN "${db_uri}";
	d2rq:username "${db_user}";
	d2rq:password "${db_password}";
	jdbc:autoReconnect "true";
	jdbc:zeroDateTimeBehavior "convertToNull";
	.
<#list entities as entity><#if !entity.abstract && !entity.system>
# Entity ${entity.name}
map:${entity.name} a d2rq:ClassMap;
	d2rq:dataStorage map:database;
	d2rq:uriPattern "${entity.name}/@@${entity.name}.${pkey(entity).name}@@";
	d2rq:class vocab:${entity.name};<#if entity.label?exists>
	d2rq:classDefinitionLabel "${entity.label?html}";<#else>
	d2rq:classDefinitionLabel "${entity.name?html}";</#if>
	<#if entity.description?exists><@compress single_line=true>d2rq:classDefinitionComment "${entity.description?html}";</@compress></#if>
	.	
# todo: use the xref labels if they are complete
# label for ${entity.name} using primary key
map:${entity.name}__label a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:${entity.name};
	d2rq:property rdfs:label;
	d2rq:pattern "${entity.name} #@@${entity.name}.${pkey(entity).name}@@";
	.
<#list entity.fields as field>
map:${entity.name}_${name(field)} a d2rq:PropertyBridge;
	d2rq:belongsToClassMap map:${entity.name};
	d2rq:property vocab:${entity.name}_${field.name};
	d2rq:propertyDefinitionLabel "${field.label?html}";
	d2rq:propertyDefinitionComment "${field.description?html}";
<#if field.type == "xref">
	d2rq:refersToClassMap map:${field.xrefEntity.name};
	d2rq:join "${entity.name}.${field.name} => ${field.xrefEntity.name}.${field.xrefField.name}";
<#elseif field.type == "mref">
	d2rq:refersToClassMap map:${field.xrefEntity.name};
	d2rq:join "${entity.name}.${pkey(entity).name} <= ${field.mrefName}.${field.mrefLocalid}";
	d2rq:join "${field.mrefName}.${field.mrefRemoteid} => ${field.xrefEntity.name}.${field.xrefField.name}";
<#else>	
	d2rq:column "${entity.name}.${field.name}";
	d2rq:datatype xsd:${xsdType(model,field)};
</#if>
	.
</#list>
</#if></#list>