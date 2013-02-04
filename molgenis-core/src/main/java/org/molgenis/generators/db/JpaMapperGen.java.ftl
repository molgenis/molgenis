<#include "GeneratorHelper.ftl">
<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* File:        ${model.getName()}/model/${entity.getName()}.java
 * Copyright:   GBIC 2000-${year}, all rights reserved
 * Date:        ${date}
 * Template:	${template}
 * generator:   ${generator} ${version}
 *
 * Jpa Entity Mapper, helper to add, delete and update entities
 * 
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */

package ${package};

public class ${JavaName(entity)}JpaMapper extends org.molgenis.framework.db.jpa.AbstractJpaMapper<${entity.namespace}.${JavaName(entity)}>
{
	private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(${JavaName(entity)}JpaMapper.class);

	public ${JavaName(entity)}JpaMapper(org.molgenis.framework.db.Database db) 
	{
		super(db);
	}
	
	@Override
	public String createFindSqlInclRules(org.molgenis.framework.db.QueryRule[] rules) throws org.molgenis.framework.db.DatabaseException
	{
		return "SELECT <#list viewFields(entity) as f>${SqlName(f.entity)}.${SqlName(f)}<#if f_has_next>"
			+", </#if></#list>"<#list viewFields(entity,"xref") as f><#list f.xrefLabelTree.getAllChildren(true) as path><#if path.value.type != "xref">
			//parent is ${path.getParent()}
			+", xref_${path.getParent().name}.${SqlName(path.value.name)} AS ${SqlName(path.name)}"</#if></#list></#list>
			+" FROM ${SqlName(entity)} "<#list superclasses(entity)?reverse as superclass><#if name(superclass) != name(entity)>
			+" INNER JOIN ${SqlName(superclass)} ON (${SqlName(entity)}.${SqlName(pkey(entity))} = ${SqlName(superclass)}.${SqlName(pkey(entity))})"</#if></#list>
<#--this piece of dark magic that attaches all xref_label possibilities -->

<#list viewFields(entity,"xref") as f>
			
			//label for ${f.name}=${csv(f.xrefLabelNames)}
<#assign pathlist = []/>			
<#list f.xrefLabelTree.getAllChildren(true) as path>
//path==${path.name}. type==${path.value.type}.
<#if path.value.type != "xref" && !pathlist?seq_contains(path.getParent().name)>
//in if path.value.type != "xref" && !pathlist?seq_contains(path.getParent().name)
<#assign pathlist = pathlist + [path.getParent().name]/>
<#if !path.getParent().parent?exists>
		   	+" LEFT JOIN ${SqlName(path.value.entity)} AS xref_${path.getParent().name} " 
			+" ON xref_${SqlName(path.getParent().name)}.${SqlName(pkey(path.value.entity))} = ${SqlName(f.entity)}.${SqlName(f.name)}"
<#elseif path.value.entity == path.getParent().value.xrefEntity>
			//linked via ${path.getParent().value.entity.name}.${path.getParent().value.name}
			+" LEFT JOIN ${SqlName(path.value.entity)} AS xref_${path.getParent().name} " 
			+" ON xref_${SqlName(path.getParent().name)}.${SqlName(pkey(path.value.entity))} = xref_${SqlName(path.getParent().parent.name)}.${SqlName(path.getParent().value.name)}"
<#else>
			//linked ${path.value.entity.name}.${path.value.name} via superclass	
			+" LEFT JOIN ${SqlName(path.value.entity)} AS xref_${path.name}"
			+" ON xref_${SqlName(path.name)}.${SqlName(path.value.name)} = xref_${path.name}.${SqlName(pkey(path.value.entity))}"
		   	+" LEFT JOIN ${SqlName(path.value.entity)} AS xref_${path.getParent().name} " 					
			+" ON xref_${SqlName(path.getParent().name)}.${SqlName(path.getParent().value)} = xref_${SqlName(path.getParent().parent.name)}.${SqlName(pkey(path.value.entity))}"			
</#if></#if></#list>
</#list>;
	}	


<#--	@Override
	public java.util.List<${entity.namespace}.${JavaName(entity)}> findAll() 
	{
		java.util.List<${entity.namespace}.${JavaName(entity)}> result =
			getEntityManager().createNamedQuery("${JavaName(entity)}.findAll", ${entity.namespace}.${JavaName(entity)}.class)
									.getResultList();
		return result;
	}

	@Override
	public java.util.List<${entity.namespace}.${JavaName(entity)}> find(String qlWhereClause, Integer limit, Integer offset) {
		String ql = "SELECT ${name(entity)} FROM ${JavaName(entity)} ${name(entity)} " + qlWhereClause;
		javax.persistence.TypedQuery<${entity.namespace}.${JavaName(entity)}> query = getEntityManager().createQuery(ql, ${entity.namespace}.${JavaName(entity)}.class);
		if(offset != null) {
			query.setFirstResult(offset);
		}
		if(limit != null) {
			query.setMaxResults(limit);
		}
		return query.getResultList();		
	}

	@Override
	public int count(String qlWhereClause)
	{
		final String QUERY_COUNT = "SELECT count(${name(entity)}) FROM ${JavaName(entity)} ${name(entity)} ";
	
		Long result = new Long(-1);

		if(qlWhereClause == null || qlWhereClause.trim().equals("")) {
			result = (Long)getEntityManager().createNamedQuery("${JavaName(entity)}.count")
								.getSingleResult();
		} else {
			String qlQuery = QUERY_COUNT + qlWhereClause;
			result = (Long)getEntityManager().createQuery(qlQuery)
								.getSingleResult();
		}
		return result.intValue();
	}-->

	/** This method first saves the objects that are being refered to by entity, 
	then the entity itself and 
	finally the objects that refer to this object*/
    public void create(${entity.namespace}.${JavaName(entity)} entity) throws org.molgenis.framework.db.DatabaseException {
        try {


<#foreach field in entity.getAllFields()>
	<#assign type_label = field.getType().toString()>
	<#if type_label == "xref">
			//check if the object refered by '${field.name}' is known in the databse
			if(entity.get${JavaName(field)}() != null)
			{
				//if object has been added as xref, but not yet stored (has no id) -> add the refered object
				if(entity.get${JavaName(field)}().getIdValue() == null)
					new ${field.getXrefEntity().namespace}.db.${JavaName(field.getXrefEntity())}JpaMapper(getDatabase()).create(entity.get${JavaName(field)}());
				//if object has id (so is stored) but not in this em -> retrieve proper reference reference
				else if (!getEntityManager().contains(entity.get${JavaName(field)}()) && entity.get${JavaName(field)}().getIdValue() != null)
					entity.set${JavaName(field)}(getEntityManager().getReference(${field.getXrefEntity().namespace}.${JavaName(field.getXrefEntity())}.class, entity.get${JavaName(field)}().getIdValue()));
			} else { //object is reference by xref	
				if(entity.get${JavaName(field)}_${JavaName(field.getXrefField())}() != null) {
					entity.set${JavaName(field)}((${field.getXrefEntity().namespace}.${JavaName(field.getXrefEntity())})getEntityManager().find(${field.getXrefEntity().namespace}.${JavaName(field.getXrefEntity())}.class, entity.get${JavaName(field)}_${JavaName(field.getXrefField())}()));
				}
			}
	<#elseif type_label == "mref">
	    java.util.List<${field.getXrefEntity().namespace}.${JavaName(field.getXrefEntity())}> ${name(field)}List = entity.get${JavaName(field)}();
	    java.util.List<Integer> ${name(field)}Ids = entity.get${JavaName(field)}_Id();
	    for(Integer ${name(field)}Id : ${name(field)}Ids) {
		${field.getXrefEntity().namespace}.${JavaName(field.getXrefEntity())} ${name(field.getXrefEntity())} = getEntityManager().getReference(${field.getXrefEntity().namespace}.${JavaName(field.getXrefEntity())}.class, ${name(field)}Id);
		if(!${name(field)}List.contains(${name(field.getXrefEntity())}))
		    ${name(field)}List.add(${name(field.getXrefEntity())});
	    }
	    entity.set${JavaName(field)}(${name(field)}List);
	</#if>
</#foreach>

			//prevents uncontrolled recursion call of create (stack overflow)
          
          if(entity.getIdValue() != null) {
            entity = getEntityManager().merge(entity);            
          } else {
            getEntityManager().persist(entity);
          }
//inverse association relation
<#list model.entities as e>
    <#if !e.abstract>
	<#list e.fields as f>
	    <#if f.type=="xref" && f.getXrefEntity() == entity.name>
		<#assign multipleXrefs = 0/>
		<#list e.fields as f2>
		    <#if f2.type="xref" && f2.getXrefEntity() == entity.name>
			<#assign multipleXrefs = multipleXrefs+1>
		    </#if>
		</#list>

		<#assign entityName = "${Name(f.entity)}" >
		<#assign entityType = "${Name(f.entity)}" >
		<#if multipleXrefs &gt; 1 >
		    <#assign entityName = "${entityName}${Name(f)}" >
		</#if>
            Collection<${f.entity.namespace}.${Name(f.entity)}> attached${entityName}Collection = new ArrayList<${Name(f.entity)}>();
            if(entity.get${entityName}Collection() != null) {
				for (${entityType} ${name(entityName)} : entity.get${entityName}Collection()) {
					if(${name(entityName)}.getIdValue() == null) {
						if(${name(entityName)}.get${Name(f)}().getIdValue() == null) {
							${name(entityName)}.set${Name(f)}(entity);
						}
						new ${f.entity.namespace}.db.${Name(f.entity)}JpaMapper(em).create(${name(entityName)});
					} else {
						//check if the object realy exists!
						${f.entity.namespace}.${Name(f.entity)} db${Name(entityName)} = getEntityManager().getReference(${xref_entity.namespace}.${name(entityName)}.getClass(), ${name(entityName)}.getIdValue());
					}
					attached${entityName}Collection.add(${name(entityName)});
				}
			}
            entity.set${entityName}Collection( attached${entityName}Collection);
            getEntityManager().persist(entity);

			//remove object references that link to a different object than entity
            if (entity.get${entityName}Collection() != null) {
				for(${entityType} ${name(entityName)} : entity.get${entityName}Collection())
				{
					${JavaName(entity)} old${JavaName(entity)}Collection = ${name(entityName)}.get${Name(f)}();
					if(!old${JavaName(entity)}Collection.getIdValue().equals(entity.getIdValue()))
					{
						${name(entityName)}.set${Name(f)}(entity);
						${name(entityName)} = getEntityManager().merge(${name(entityName)});
						if(old${JavaName(entity)}Collection != null)
						{
							old${JavaName(entity)}Collection.get${entityName}Collection().remove(${name(entityName)});
							old${JavaName(entity)}Collection = getEntityManager().merge(old${JavaName(entity)}Collection);
						}
					}
				}
            }
	    </#if>
	</#list>
    </#if>
</#list>

        } catch (Exception ex) {
            try {
				getEntityManager().getTransaction().rollback();
            } catch (Exception re) {
                throw new org.molgenis.framework.db.DatabaseException("An error occurred attempting to roll back the transaction: "+re.getMessage());
            }
            throw new org.molgenis.framework.db.DatabaseException(ex);
        }
    }

	public void destroy(${entity.namespace}.${JavaName(entity)} ${name(entity)}) throws org.molgenis.framework.db.DatabaseException {
		try {
			try {
				${name(entity)} = getEntityManager().getReference(${entity.namespace}.${JavaName(entity)}.class, ${name(entity)}.getIdValue());
			} catch (javax.persistence.EntityNotFoundException enfe) {
				throw new org.molgenis.framework.db.DatabaseException("The ${name(entity)} with id " + ${name(entity)}.getIdField().toString() + " no longer exists: " + enfe.getMessage());
			}
<#--
<#list model.entities as e>
<#if !e.abstract && !e.isAssociation()>
	<#list e.implementedFields as f>
		<#if f.type=="mref" && Name(f.getXrefEntityName()) == Name(entity) >
		<#assign multipleXrefs = model.getNumberOfReferencesTo(entity)/>
		//${multipleXrefs}
			if(${name(entity)}.get${Name(f)}<#if multipleXrefs &gt; 1 >${Name(e)}</#if>Collection().contains(${name(entity)})) {	
				${name(entity)}.get${Name(f)}<#if multipleXrefs &gt; 1 >${Name(e)}</#if>Collection().remove(${name(entity)});
			}
		</#if>
	</#list>
</#if>	
</#list>
-->
			getEntityManager().remove(${name(entity)});
		} catch (Exception ex) {
			try {
				getEntityManager().getTransaction().rollback();
			} catch (Exception re) {
				throw new org.molgenis.framework.db.DatabaseException("An error occurred attempting to roll back the transaction: "+re.getMessage());
			}
			throw new org.molgenis.framework.db.DatabaseException(ex);
		} 
	}


	public void edit(${entity.namespace}.${JavaName(entity)} ${name(entity)}) throws org.molgenis.framework.db.DatabaseException {
		try {

			//Fixme: getReference??
			${entity.namespace}.${JavaName(entity)} persistent${JavaName(entity)} = getEntityManager().find(${entity.namespace}.${JavaName(entity)}.class, ${name(entity)}.getIdValue());


<#foreach field in entity.getAllFields()>
	<#assign type_label = field.getType().toString()>

	<#if type_label == "xref" || type_label == "mref">
		<#assign numRef = model.getNumberOfReferencesTo(field.getXrefEntity())>
			<#assign fieldName = name(field) />
	<#--		
			<#if numRef &gt; 1 >
				<#assign fieldName = fieldName + Name(entity)/>
			</#if>
		-->
			//${numRef}
		<#if type_label == "xref">
			${field.getXrefEntity().namespace}.${JavaName(field.getXrefEntity())} ${fieldName}Old = persistent${JavaName(entity)}.get${JavaName(field)}();
			${field.getXrefEntity().namespace}.${JavaName(field.getXrefEntity())} ${fieldName}New = ${name(entity)}.get${JavaName(field)}();

			if (${fieldName}New != null) {
//				${fieldName}New = getEntityManager().getReference(org.hibernate.proxy.HibernateProxyHelper.getClassWithoutInitializingProxy(${fieldName}New), ${fieldName}New.getIdValue());
//				${fieldName}New = getEntityManager().getReference(${fieldName}New.getClass(), ${fieldName}New.getIdValue());
				${fieldName}New = getEntityManager().getReference((Class<${field.getXrefEntity().namespace}.${JavaName(field.getXrefEntity())}>) org.hibernate.Hibernate.getClass(${fieldName}New), ${fieldName}New.getIdValue());
				${name(entity)}.set${JavaName(field)}(${fieldName}New);
			} else { //object is reference by xref		
                            if(${name(entity)}.get${JavaName(field)}_${JavaName(field.xrefField)}() != null) {
                                ${name(entity)}.set${JavaName(field)}((${field.getXrefEntity().namespace}.${JavaName(field.getXrefEntity())})getEntityManager().find(${field.getXrefEntity().namespace}.${JavaName(field.getXrefEntity())}.class, ${name(entity)}.get${JavaName(field)}_${JavaName(field.xrefField)}()));
                            }
			}
    	<#elseif type_label == "mref">
			for(${field.getXrefEntity().namespace}.${JavaName(field.getXrefEntity())} m : ${name(entity)}.get${JavaName(field)}()) {
				if(m.get${Name(pkey(field.getXrefEntity()))}() == null) {
					getEntityManager().persist(m);
				}
				m.get${JavaName(fieldName)}<#if numRef &gt; 1 >${Name(field.getEntity())}</#if>Collection().add(${name(entity)});
			}
			
			for(${pkeyJavaType(field.getXrefEntity())} id : ${name(entity)}.get${JavaName(fieldName)}_Id()) {
				${field.getXrefEntity().namespace}.${JavaName(field.getXrefEntity())} mref = getEntityManager().find(${field.getXrefEntity().namespace}.${JavaName(field.getXrefEntity())}.class, id);
				if(!${name(entity)}.get${JavaName(fieldName)}().contains(mref)) {
					${name(entity)}.get${JavaName(fieldName)}().add(mref);
				}
			}    
		</#if>
	</#if>
</#foreach>
			if(!getEntityManager().contains(${name(entity)})) {
				${name(entity)} = getEntityManager().merge(${name(entity)});
			}
<#--	what does this do? FIXIT		
<#list model.entities as e>
	<#if !e.abstract && !e.isAssociation()>
		<#list e.implementedFields as f>
			<#if f.type=="xref" && entity.isParent(Name(f.getXrefEntity())) >
				<#assign multipleXrefs = e.getNumberOfReferencesTo(entity)/>
			 
			 //${entity.getName()}
			 //${Name(f.getXrefEntity())}
			 
			if (${fieldName}Old != null && !${fieldName}Old.equals(${fieldName}New)) {
				${fieldName}Old.get${f.getXrefEntityName()}Collection().remove(${name(entity)});
				${fieldName}Old = getEntityManager().merge(${fieldName}Old);
			}

			if (${fieldName}New != null && !${fieldName}New.equals(${fieldName}Old)) {
				${fieldName}New.get${f.getXrefEntityName()}Collection().add(${name(entity)});
				${fieldName}New = getEntityManager().merge(${fieldName}New);
			}			 
			
			</#if>
		</#list>
	</#if>
</#list>			
-->	
			
<#foreach field in entity.getAllFields()>
	<#assign type_label = field.getType().toString()>
	<#if type_label == "xref">
		<#assign numRef = model.getNumberOfReferencesTo(field.getXrefEntity())>

			<#assign fieldName = name(field.getXrefEntity()) />
			<#assign methodName = Name(entity) />
			<#if numRef &gt; 1 >
				<#assign fieldName = fieldName + Name(field) />
				<#assign methodName = methodName + Name(field) />
			</#if>

	</#if>
</#foreach>
		} catch (Exception ex) {
			try {
				getEntityManager().getTransaction().rollback();
			} catch (Exception re) {
				throw new org.molgenis.framework.db.DatabaseException("An error occurred attempting to roll back the transaction: " + re.getMessage());
			}
			throw new org.molgenis.framework.db.DatabaseException(ex);
		} 
	}

	@Override
	public int executeAdd(java.util.List<? extends ${entity.namespace}.${JavaName(entity)}> entities) throws org.molgenis.framework.db.DatabaseException
	{	
		int count = 0;
		
		try 
		{
			for (${entity.namespace}.${JavaName(entity)} ${name(entity)} : entities) 
			{
				create(${name(entity)});
				++count;
			}
		}
		catch (org.hibernate.exception.SQLGrammarException sge)
		{
			log.error("Message: " + sge.getMessage());
			log.error("SQL: " + sge.getSQL());
			log.error("SQLState: " + sge.getSQLState());
			log.error("SQLException: " + sge.getSQLException());
			sge.printStackTrace();
			throw new org.molgenis.framework.db.DatabaseException(sge);
		}
		catch (Exception ex) 
		{
            throw new org.molgenis.framework.db.DatabaseException(ex);
        }
		return count;
	}

	@Override
	public int executeUpdate(java.util.List<? extends ${entity.namespace}.${JavaName(entity)}> entities) throws org.molgenis.framework.db.DatabaseException
	{
		int count = 0;

		try
		{
			for (${entity.namespace}.${JavaName(entity)} ${name(entity)} : entities) 
			{
				edit(${name(entity)});
				++count;
			} 
			return count;
		}
		catch (Exception ex) 
		{
            throw new org.molgenis.framework.db.DatabaseException(ex);
        }		
	}

	@Override
	public int executeRemove(java.util.List<? extends ${entity.namespace}.${JavaName(entity)}> entities) throws org.molgenis.framework.db.DatabaseException
	{
		int count = 0;		
		try 
		{
			for (${entity.namespace}.${JavaName(entity)} ${name(entity)} : entities) 
			{
				destroy(${name(entity)});
				++count;
			}
		} 
		catch (Exception ex) 
		{
            throw new org.molgenis.framework.db.DatabaseException(ex);
        }
		return count;
	}

<#--	public ${JavaName(entity)} create()
	{
<#if !entity.abstract>	
		return new ${JavaName(entity)}();
<#else>
		return null; //abstract type, cannot be instantiated
</#if>
	}-->
	
	@Override
	public String getTableFieldName(String fieldName)
	{
		<#list viewFields(entity) as f>
		<#assign type= f.type>
		if("${f.name}".equalsIgnoreCase(fieldName)) return "${SqlName(f)}";
		if("${entity.name}_${f.name}".equalsIgnoreCase(fieldName)) return "${SqlName(f)}";
		</#list>	
		<#list viewFields(entity,"xref") as f>	
		if("${f.name}_${f.xrefField.name}".equalsIgnoreCase(fieldName)) return "${SqlName(f)}";
		if("${entity.name}_${f.name}_${f.xrefField.name}".equalsIgnoreCase(fieldName)) return "${SqlName(f)}";
		<#list f.xrefLabelTree.getTreeElements()?values as path><#if path.value.type != "xref">
		if("${path.name}".equalsIgnoreCase(fieldName)) return "${path.getParent().name}.${SqlName(path.value.name)}";	
		if("${entity.name}_${path.name}".equalsIgnoreCase(fieldName)) return "${path.getParent().name}.${SqlName(path.value.name)}";
		</#if></#list></#list>
		<#--
		<#assign xref_entity = f.xrefEntity/> 
		<#assign xref_field = f.xrefField/>
		//alias for query on id field of xref entity
		if("${name(f)}_${name(xref_field)}".equalsIgnoreCase(fieldName)) return "${SqlName(f)}";
		//alias(es) for query on label of the xref entity
			<#list f.xrefLabelNames as label>
		if("${name(f)}_${name(label)}".equalsIgnoreCase(fieldName)) return "xref_${label}.${SqlName(label)}";
			</#list>
		</#list>
		-->		  		
		return fieldName;
	}
	
	<#include "MapperCommons.java.ftl">
	
	<#include "MapperFileAttachments.java.ftl">
	
}
