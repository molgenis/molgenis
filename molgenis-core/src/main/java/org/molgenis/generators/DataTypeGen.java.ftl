<#--helper functions-->
<#include "GeneratorHelper.ftl">

<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* File:        ${model.getName()}/model/${entity.getName()}.java
 * Generator:   ${generator} ${version}
 *
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */
 

package ${package};

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.Entity;

/**
 * ${Name(entity)}: ${entity.description}.
 * <#if JavaName(entity) == "RuntimeProperty">@deprecated replaced by setting classes that derive from {@link org.molgenis.data.settings.DefaultSettingsEntity}</#if>
 * @author MOLGENIS generator
 */
<#if entity.abstract>
public interface ${JavaName(entity)} extends <#if entity.hasImplements()><#list entity.getImplements() as i> ${i.namespace}.${JavaName(i)}<#if i_has_next>,</#if></#list><#else>org.molgenis.data.Entity</#if>
<#else>
<#-- disables many-to-many relationships (makes it compatible with no-JPA database)   -->
	<#if !entity.description?contains("Link table for many-to-many relationship") >
@javax.persistence.Entity
@javax.persistence.Table(name = "${SqlName(entity)}"<#list entity.getUniqueKeysWithoutPk() as uniqueKeys ><@compress single_line=true>
	<#if uniqueKeys_index = 0 >, uniqueConstraints={
	@javax.persistence.UniqueConstraint( columnNames={<#else>), @javax.persistence.UniqueConstraint( columnNames={</#if>
    <#list key_fields(uniqueKeys) as uniqueFields >
	"${uniqueFields.name}"<#if uniqueFields_has_next>,
		</#if>
    </#list>
	}
    <#if !uniqueKeys_has_next>
    )
   }
    </#if>
</@compress>
</#list>

)

		<#if !entity.hasAncestor() && entity.hasDescendants() >
@javax.persistence.Inheritance(strategy=javax.persistence.InheritanceType.JOINED)
@javax.persistence.DiscriminatorColumn(name="DType", discriminatorType=javax.persistence.DiscriminatorType.STRING)
		</#if>
	</#if>
@javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value={"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification="Exposing internal representation is accepted")
<#if JavaName(entity) == "RuntimeProperty">@Deprecated</#if>
public class ${JavaName(entity)} extends <#if entity.hasAncestor()>${entity.getAncestor().namespace}.${JavaName(entity.getAncestor())}<#else>org.molgenis.data.support.AbstractEntity</#if> implements org.molgenis.data.Entity<#if entity.hasImplements()>,<#list entity.getImplements() as i> ${i.namespace}.${JavaName(i)}<#if i_has_next>,</#if></#list></#if>
</#if>
{
<#if entity.abstract>
<#--interface only has method signatures-->
	<#-- get all fields, excluding fields from parent interfaces -->
	<#foreach field in entity.getFields(false, false, true, false)>
		<#assign type_label = field.getType().toString()>
		<#if (field.name != typefield()) || !entity.hasAncestor()>
	public <#if type_label == "xref" || type_label == "categorical">${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}<#else>${type(field)}</#if> get${JavaName(field)}();
	public void set${JavaName(field)}(<#if field.type == "xref" || field.type == "categorical">${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}<#else>${type(field)}</#if> ${name(field)});
		<#if type_label == "enum">
	public java.util.List<org.molgenis.util.ValueLabel> get${JavaName(field)}Options();					
		<#elseif type_label == "file" || type_label=="image" >
	public java.io.File get${JavaName(field)}File();
	public void set${JavaName(field)}File(java.io.File file);
			</#if>
		</#if>	
	</#foreach>	
<#--concrete class has method bodies-->
<#else>
    /** default serial version ID */
    private static final long serialVersionUID = 1L;
    
    public final static String ENTITY_NAME = "${entity.name}";
    
	// fieldname constants
    <#foreach field in entity.getImplementedFields()>
	public final static String ${field.name?upper_case} = "${field.name}";
	</#foreach>
	
	// member variables (including setters.getters for interface)
	public String getEntityName()
	{
		return ENTITY_NAME;
	}
	
	<#foreach field in entity.getImplementedFields()>
	<#if field.annotations?exists>
	${field.annotations}
	</#if>

	//${field.description}[type=${field.type}]
	<#if !isPrimaryKey(field,entity) || !entity.hasAncestor()>
 			<#if isPrimaryKey(field,entity) && !entity.hasAncestor()>
  				@javax.persistence.Id  
    		</#if>
		</#if>	

        <#if field.type == "date">
    @javax.persistence.Temporal(javax.persistence.TemporalType.DATE)
    	<#elseif field.type == "datetime">
    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    	</#if>
        <#if field.type == "mref">
			<#assign multipleXrefs = model.getNumberOfReferencesTo(field.xrefEntity)/>
	@javax.persistence.OrderColumn 	
    @javax.persistence.ManyToMany(<#if field.jpaCascade??>fetch=javax.persistence.FetchType.LAZY, cascade={${field.jpaCascade}}<#else>fetch=javax.persistence.FetchType.LAZY /*cascade={javax.persistence.CascadeType.MERGE, javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.REFRESH}*/</#if>)
    @javax.persistence.JoinColumn(name="${SqlName(field)}", insertable=true, updatable=true, nullable=${field.isNillable()?string})
			<#if multipleXrefs &gt; 1>
	@javax.persistence.JoinTable(name="${Name(entity)}_${SqlName(field)}", 
			joinColumns=@javax.persistence.JoinColumn(name="${Name(entity)}"), inverseJoinColumns=@javax.persistence.JoinColumn(name="${SqlName(field)}"))
			<#else> 
	@javax.persistence.JoinTable(name="${Name(entity)}_${SqlName(field)}", 
			joinColumns=@javax.persistence.JoinColumn(name="${Name(entity)}"), inverseJoinColumns=@javax.persistence.JoinColumn(name="${SqlName(field)}"))			
			</#if>			
       	<#elseif field.type == "xref" || field.type == "categorical">
    @javax.persistence.ManyToOne(<#if field.jpaCascade??>fetch=javax.persistence.FetchType.EAGER, cascade={${field.jpaCascade}}<#else>fetch=javax.persistence.FetchType.EAGER /*cascade={javax.persistence.CascadeType.MERGE, javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.REFRESH}*/</#if>)
    @javax.persistence.JoinColumn(name="${SqlName(field)}"<#if !field.nillable>, nullable=false</#if>)  
       	<#else>
			<#if isPrimaryKey(field,entity)>
				<#if !entity.hasAncestor()>
    @javax.persistence.Column(name="${SqlName(field)}"<#if field.type == "string">, length=${field.length?c}</#if><#if !field.nillable>, nullable=false</#if>)
	@javax.xml.bind.annotation.XmlElement(name="${name(field)}")
				</#if>
			<#else>
				<#if field.type == "text" || field.type == 'script' >			
	@javax.persistence.Lob
	@javax.persistence.Column(name="${SqlName(field)}"<#if !field.nillable>, nullable=false</#if>)
				<#else>
        <#if SqlName(field) == '__Type'>
	@javax.persistence.Column(name="DType"<#if field.type == "string">, length=${field.length?c}</#if><#if !field.nillable>, nullable=false</#if>)            
        <#else>
	@javax.persistence.Column(name="${SqlName(field)}"<#if field.type == "string">, length=${field.length?c}</#if><#if !field.nillable>, nullable=false</#if>)
        </#if>
	@javax.xml.bind.annotation.XmlElement(name="${name(field)}")
				</#if>
			</#if>   	
       	</#if>
	
		<#assign type_label = field.getType().toString()>
			<#if isPrimaryKey(field,entity)>
				<#if !entity.hasAncestor()>
	private <#if field.type="xref" || field.type="categorical">${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}<#elseif field.type="mref">java.util.List<${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}><#else>${type(field)}</#if> ${name(field)} = <#if field.type == "mref">new java.util.ArrayList<${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}>()<#elseif field.type == "xref" || field.type == "categorical">null<#else> ${default(field)}</#if>;
				</#if>
			<#else>
				

				<#if !field.isNillable() >
	@javax.validation.constraints.NotNull
				</#if>
	private <#if field.type="xref" || field.type == "categorical">${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}<#elseif field.type="mref">java.util.List<${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}><#else>${type(field)}</#if> ${name(field)} = <#if field.type == "mref">new java.util.ArrayList<${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}>()<#elseif field.type == "xref" || field.type == "categorical">null<#else> ${default(field)}</#if>;
			</#if>
		<#if type_label == "enum">
	@javax.persistence.Transient
	private String ${name(field)}_label = null;
	@javax.persistence.Transient
	private static final java.util.List<org.molgenis.util.ValueLabel> ${name(field)}_options;
	
	static {
		${name(field)}_options = new java.util.ArrayList<org.molgenis.util.ValueLabel>();
		<#list field.getEnumOptions() as option>
		${name(field)}_options.add(new org.molgenis.util.ValueLabel("${option}","${option}"));
		</#list>	
	}
		<#elseif type_label == "file" || type_label=="image" >
	@javax.persistence.Lob
	private java.io.File ${name(field)}_file = null;
		</#if>
	</#foreach>	

	//constructors
	public ${JavaName(entity)}()
	{
	<#if entity.hasAncestor() || entity.hasDescendants()>
		//set the type for a new instance
		set${typefield()}(this.getClass().getSimpleName());
	</#if>
	}
	
	//getters and setters
	<#foreach field in entity.getImplementedFields()>
		<#assign type_label = field.getType().toString()>
			<#if isPrimaryKey(field,entity)>
				<#if !entity.hasAncestor()>
	/**
	 * Get the ${field.description}.
	 * @return ${name(field)}.
	 */
	public <#if field.type =="xref" || field.type == "categorical">${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}<#else>${type(field)}</#if> get${JavaName(field)}()
	{
		return this.${name(field)};
	}
	
				</#if>
			<#else>
	/**
	 * Get the ${field.description}.
	 * @return ${name(field)}.
	 */
	public <#if field.type =="xref" || field.type == "categorical">${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}<#elseif field.type == "mref">java.util.List<${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}><#else>${type(field)}</#if> get${JavaName(field)}()
	{
		return this.${name(field)};
	}	
			</#if>
	
			<#if isPrimaryKey(field,entity)>
				<#if !entity.hasAncestor()>
	/**
	 * Set the ${field.description}.
	 * @param ${name(field)}
	 */
	public void set${JavaName(field)}( <#if field.type =="xref" || field.type == "categorical">${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}<#elseif field.type == "mref">java.util.List<${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}><#else>${type(field)}</#if> ${name(field)})
	{
		this.${name(field)} = ${name(field)};
	}
				</#if>
			<#else>
	/**
	 * Set the ${field.description}.
	 * @param ${name(field)}
	 */
	public void set${JavaName(field)}( <#if field.type =="xref" || field.type == "categorical">${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}<#elseif field.type == "mref">java.util.List<${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}><#else>${type(field)}</#if> ${name(field)})
	{
		<#-- hack to solve problem with variable hidden in supertype -->
		<#if entity.hasAncestor()> 
			<#if entity.getAncestor().getField(field.getName(), false, true, true)?exists>
				//hack to solve problem with variable hidden in supertype
				super.set${JavaName(field)}(${name(field)});
			</#if>
			<#if entity.getAncestor().getAllField(field.getName())?exists>
				//2222hack to solve problem with variable hidden in supertype
				super.set${JavaName(field)}(${name(field)});
			</#if>
		</#if>
		
		this.${name(field)} = ${name(field)};
	}
			</#if>

	
	<#-- data type specific methods -->
	<#if type_label =="date">
	/**
	 * Set the ${field.description}. Automatically converts string into date;
	 * @param ${name(field)}
	 */	
	public void set${JavaName(field)}(String datestring) throws java.text.ParseException
	{
		this.set${JavaName(field)}(org.molgenis.data.DataConverter.toDate(datestring));
	}	
	<#elseif type_label == "enum" >
	/**
	 * Get tha label for enum ${JavaName(field)}.
	 */
	public String get${JavaName(field)}Label()
	{
		return this.${name(field)}_label;
	}
	/**
	 * ${JavaName(field)} is enum. This method returns all available enum options.
	 */
	public java.util.List<org.molgenis.util.ValueLabel> get${JavaName(field)}Options()
	{
		return ${name(field)}_options;
	}	
	
	<#elseif type_label == "file"  || type_label=="image" >
	/**
	 * get${JavaName(field)}() is a textual pointer to a file. get${JavaName(field)}AttachedFile() can be used to retrieve the full paht to this file.
	 */
	public java.io.File get${JavaName(field)}AttachedFile()
	{
		return ${name(field)}_file;
	}
	
	/**
	 * ${JavaName(field)} is a pointer to a file. Use set${JavaName(field)}AttachedFile() to attach this file so it can be 
	 * retrieved using get${JavaName(field)}AttachedFile().
	 */
	public void set${JavaName(field)}AttachedFile(java.io.File file)
	{
		${name(field)}_file = file;
	}
	</#if>

</#foreach>	

	/**
	 * Generic getter. Get the property by using the name.
	 */
	@Override
	public Object get(String name)
	{
		name = name.toLowerCase();
		<#foreach field in allFields(entity)>
		if (name.equals("${name(field)?lower_case}"))
			return get${JavaName(field)}();
		<#if field.type == "enum" >	
		if(name.equals("${name(field)?lower_case}_label"))
			return get${JavaName(field)}Label();			
		</#if>
		</#foreach>		
		return null;
	}	
	
	<#include "DataTypeCommons.java.ftl">	
	
	@Override
	public String toString()
	{
		return this.toString(false);
	}
	
	public String toString(boolean verbose)
	{
		StringBuilder sb = new StringBuilder("${JavaName(entity)}(");
<#list allFields(entity) as field>
	<#assign type_label = field.getType().toString()>
		<#if field.type.toString() == "datetime">
		sb.append("${name(field)}='" + (get${JavaName(field)}() == null ? "" : new java.text.SimpleDateFormat("MMMM d, yyyy, HH:mm:ss", java.util.Locale.US).format(get${JavaName(field)}()))+"'<#if field_has_next> </#if>");
		<#elseif field.type.toString() == 'date'>
		sb.append("${name(field)}='" + (get${JavaName(field)}() == null ? "" : new java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.US).format(get${JavaName(field)}()))+"'<#if field_has_next> </#if>");		
		<#else>
		sb.append("${name(field)}='" + get${JavaName(field)}()+"'<#if field_has_next> </#if>");	
		</#if>
</#list>
		sb.append(");");
		return sb.toString();
	}

<#if !entity.abstract>	
	@Override
	public String getIdValue()
	{
		return get${JavaName(pkey(entity))}();
	}		
</#if>
	
<#list model.entities as e>
	<#if !e.abstract && !e.isAssociation()>
		<#list e.implementedFields as f>
			<#if f.type=="mref" && f.getXrefEntityName() == entity.name>
				<#assign multipleXrefs = model.getNumberOfReferencesTo(entity)/>
	//${multipleXrefs}
    @javax.persistence.ManyToMany(fetch=javax.persistence.FetchType.LAZY, mappedBy="${name(f)}"/*, cascade={javax.persistence.CascadeType.MERGE, javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.REFRESH}*/)
    private java.util.Collection<${f.entity.namespace}.${JavaName(f.entity)}> ${name(f)}<#if multipleXrefs &gt; 1 >${JavaName(f.entity)}</#if>Collection;

	@javax.xml.bind.annotation.XmlTransient
	public java.util.Collection<${f.entity.namespace}.${JavaName(f.entity)}> get${JavaName(f)}<#if multipleXrefs &gt; 1 >${JavaName(f.entity)}</#if>Collection()
	{
		if(${name(f)}<#if multipleXrefs &gt; 1 >${JavaName(f.entity)}</#if>Collection == null) ${name(f)}<#if multipleXrefs &gt; 1 >${JavaName(f.entity)}</#if>Collection = new java.util.ArrayList<${f.entity.namespace}.${JavaName(f.entity)}>(); 
        return ${name(f)}<#if multipleXrefs &gt; 1 >${JavaName(f.entity)}</#if>Collection;
	}


    public void set${JavaName(f)}<#if multipleXrefs &gt; 1 >${JavaName(f.entity)}</#if>Collection(java.util.Collection<${f.entity.namespace}.${JavaName(f.entity)}> collection)
    {
		if(${name(f)}<#if multipleXrefs &gt; 1 >${JavaName(f.entity)}</#if>Collection == null) ${name(f)}<#if multipleXrefs &gt; 1 >${JavaName(f.entity)}</#if>Collection = new java.util.ArrayList<${f.entity.namespace}.${JavaName(f.entity)}>();
    	${name(f)}<#if multipleXrefs &gt; 1 >${JavaName(f.entity)}</#if>Collection.addAll(collection);
    }	
			</#if>
		</#list>
	</#if>
</#list>

	@Override
	public Iterable<String> getAttributeNames()
	{
		Set<String> attributeNames = new LinkedHashSet<String>();
		for (AttributeMetaData attr : new ${JavaName(entity)}MetaData().getAttributes())
		{
			attributeNames.add(attr.getName());
		}

		return attributeNames;
	}

	@Override
	public void set(String attributeName, Object value)
	{
<#list allFields(entity) as f>
		if("${f.name}".equalsIgnoreCase(attributeName)) {
		<#assign type_label = f.getType().toString()>
			<#if f.type == "categorical" || f.type == "xref">
			${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)} e = new ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}();
			e.set((Entity)value);
			</#if>
			this.set${JavaName(f)}(<#if f.type == "categorical" || f.type == "xref">e<#elseif f.type="mref">(List<${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}>)value<#else>(${f.type.javaPropertyType})value</#if>); 
			<#--
		<#if f.type == "mref">
			this.set${JavaName(f)}(value);
			
			//set ${JavaName(f)}
			if( entity.get("${f.name}") != null || entity.get("${f.name?lower_case}") != null ) 
			{
				Object mrefs = entity.get("${f.name}");
				if(mrefs == null) mrefs = entity.get("${f.name?lower_case}");
				if(entity.get("${entity.name?lower_case}_${f.name?lower_case}")!= null) mrefs = entity.get("${entity.name?lower_case}_${f.name?lower_case}");
				else if(entity.get("${entity.name}_${f.name}")!= null) mrefs = entity.get("${entity.name}_${f.name}");									
				this.set${JavaName(f)}((java.util.List<${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}>) mrefs );
			}
					
		<#else>
			//set ${JavaName(f)}
			// query formal name, else lowercase name
			<#if f.type == "xref" || f.type == "categorical">
			if( entity.get("${f.name}") != null) { 
				this.set${JavaName(f)}((${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)})entity.get("${f.name}"));				
			}
			else if( entity.get("${f.name?lower_case}") != null) { 
				this.set${JavaName(f)}((${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)})entity.get("${f.name?lower_case}"));				
			}
			else if( entity.get("${entity.name}_${f.name}") != null) { 
				this.set${JavaName(f)}((${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)})entity.get("${entity.name}_${f.name}"));				
			}
			else if( entity.get("${entity.name?lower_case}_${f.name?lower_case}") != null) { 
				this.set${JavaName(f)}((${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)})entity.get("${entity.name}_${f.name}"));				
			}
			<#else>
			if(entity.get${settertype(f)}("${f.name?lower_case}") != null) this.set${JavaName(f)}(entity.get${settertype(f)}("${f.name?lower_case}"));
			else if(entity.get${settertype(f)}("${f.name}") != null) this.set${JavaName(f)}(entity.get${settertype(f)}("${f.name}"));
			if( entity.get${settertype(f)}("${entity.name?lower_case}_${f.name?lower_case}") != null) this.set${JavaName(f)}(entity.get${settertype(f)}("${entity.name?lower_case}_${f.name?lower_case}"));
			else if( entity.get${settertype(f)}("${entity.name}_${f.name}") != null) this.set${JavaName(f)}(entity.get${settertype(f)}("${entity.name}_${f.name}"));
			</#if>
			<#if f.type == "file" || f.type=="image">
			if(entity.getString("filefor_${f.name}") != null)
				this.set${JavaName(f)}AttachedFile(new java.io.File(entity.getString("filefor_${f.name}")));
			else if(entity.getString("filefor_${f.name?lower_case}") != null)
				this.set${JavaName(f)}AttachedFile(new java.io.File(entity.getString("filefor_${f.name?lower_case}")));
			if(entity.getString("filefor_${entity.name}_${f.name}") != null) this.set${JavaName(f)}AttachedFile(new java.io.File(entity.getString("filefor_${entity.name}_${f.name}"))); //FIXME filefor hack
			else if(entity.getString("filefor_${entity.name?lower_case}_${f.name?lower_case}") != null) this.set${JavaName(f)}AttachedFile(new java.io.File(entity.getString("filefor_${entity.name?lower_case}_${f.name?lower_case}"))); //FIXME filefor hack
			</#if>						
		</#if>
		-->			
			return;
		}
</#list>
	}

<#-- Implement equals() and hashCode() using business key equality -->
<#assign uniqueKeys = entity.getUniqueKeysWithoutPk()>
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
<#if entity.hasAncestor()>
		if (!super.equals(obj)) return false;
</#if>
		if (getClass() != obj.getClass()) return false;
<#if uniqueKeys?has_content>
		${JavaName(entity)} other = (${JavaName(entity)}) obj;
	<#list uniqueKeys as uniqueKey>
		<#list key_fields(uniqueKey) as field>
		if (${name(field)} == null)
		{
			if (other.${name(field)} != null) return false;
		}
		else if (!${name(field)}.equals(other.${name(field)})) return false;
		</#list>
	</#list>
</#if>
		return true;
	}
	
	@Override
	public int hashCode()
	{
<#if uniqueKeys?has_content>
		final int prime = 31;
</#if>
<#if entity.hasAncestor()>
		int result = super.hashCode();
<#else>
		int result = 1;
</#if>
<#list uniqueKeys as uniqueKey>
	<#list key_fields(uniqueKey) as field>
		result = prime * result + ((${name(field)} == null) ? 0 : ${name(field)}.hashCode());
	</#list>
</#list>
		return result;
	}
	
	@Override
	public org.molgenis.data.EntityMetaData getEntityMetaData()
	{
		return new ${JavaName(entity)}MetaData();
	}
</#if>
}
