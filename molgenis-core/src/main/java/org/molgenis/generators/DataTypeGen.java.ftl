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

/**
 * ${Name(entity)}: ${entity.description}.
 * @author MOLGENIS generator
 */
<#if entity.abstract>
public interface ${JavaName(entity)} extends <#if entity.hasImplements()><#list entity.getImplements() as i> ${i.namespace}.${JavaName(i)}<#if i_has_next>,</#if></#list><#else>org.molgenis.util.Entity</#if>
<#else>
<#-- disables many-to-many relationships (makes it compatible with no-JPA database)   -->
	<#if !entity.description?contains("Link table for many-to-many relationship") >
@javax.persistence.Entity
//@org.hibernate.search.annotations.Indexed
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

<#if entity.indices?has_content >
@org.hibernate.annotations.Table(appliesTo="${SqlName(entity)}", indexes={
<#foreach index in entity.indices>
    @org.hibernate.annotations.Index(name="${index.name}", columnNames={
			<#foreach field in index.fields>
	"${field}"<#if field_has_next>,</#if>
			</#foreach>
    })<#if index_has_next>,</#if>
</#foreach>    
})
</#if>

		<#if !entity.hasAncestor() && entity.hasDescendants() >
@javax.persistence.Inheritance(strategy=javax.persistence.InheritanceType.JOINED)
@javax.persistence.DiscriminatorColumn(name="DType", discriminatorType=javax.persistence.DiscriminatorType.STRING)
		</#if>
	</#if>
@javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
//@EntityListeners({${package}.db.${JavaName(entity)}EntityListener.class})
public class ${JavaName(entity)} extends <#if entity.hasAncestor()>${entity.getAncestor().namespace}.${JavaName(entity.getAncestor())}<#else>org.molgenis.util.AbstractEntity</#if> <#if entity.hasImplements()>implements<#list entity.getImplements() as i> ${i.namespace}.${JavaName(i)}<#if i_has_next>,</#if></#list></#if>
</#if>
{
<#if entity.abstract>
<#--interface only has method signatures-->
	<#foreach field in entity.getImplementedFields()>
		<#assign type_label = field.getType().toString()>
		<#if (field.name != typefield()) || !entity.hasAncestor()>
	public <#if type_label == "xref">${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}<#else>${type(field)}</#if> get${JavaName(field)}();
	public void set${JavaName(field)}(<#if field.type = "xref">${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}<#else>${type(field)}</#if> ${name(field)});
		<#if type_label == "enum">
	public java.util.List<org.molgenis.util.ValueLabel> get${JavaName(field)}Options();
		<#elseif type_label == "xref">			
        public ${type(field.xrefField)} get${JavaName(field)}_${JavaName(field.xrefField)}();
        public void set${JavaName(field)}_${JavaName(field.xrefField)}(${type(field.xrefField)} ${name(field)});

			<#if field.xrefLabelNames[0] != field.xrefFieldName>
                            <#list field.xrefLabelNames as label>
	public String get${JavaName(field)}_${JavaName(label)}();
	public void set${JavaName(field)}_${JavaName(label)}(String ${name(field)}_${label});
                            </#list>
                        </#if>		
		<#elseif type_label == "mref">	
	public java.util.List<${type(f.xrefField)}> get${JavaName(field)}_${JavaName(f.xrefField)}();	
	public void set${JavaName(field)}_${JavaName(f.xrefField)}(java.util.List<${type(f.xrefField)}> ${JavaName(field)}_${JavaName(f.xrefField)}List);	
			<#if field.xrefLabelNames[0] != field.xrefFieldName><#list field.xrefLabelNames as label>
	public java.util.List<String> get${JavaName(field)}_${JavaName(label)}();
	public void set${JavaName(field)}_${JavaName(label)}(java.util.List<String> ${name(field)}_${label}List);	
			</#list></#if>						
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
	// fieldname constants
    <#foreach field in entity.getImplementedFields()>
	public final static String ${field.name?upper_case} = "${field.name}";<#if field.type == "xref" || field.type == "mref"><#list field.xrefLabelNames as label>
	public final static String ${field.name?upper_case}_${label?upper_case} = "${field.name}_${label}";</#list></#if>
	</#foreach>
	
	//static methods
	/**
	 * Shorthand for db.query(${JavaName(entity)}.class).
	 */
	public static org.molgenis.framework.db.Query<? extends ${JavaName(entity)}> query(org.molgenis.framework.db.Database db)
	{
		return db.query(${JavaName(entity)}.class);
	}
	
	/**
	 * Shorthand for db.find(${JavaName(entity)}.class, org.molgenis.framework.db.QueryRule ... rules).
	 */
	public static java.util.List<? extends ${JavaName(entity)}> find(org.molgenis.framework.db.Database db, org.molgenis.framework.db.QueryRule ... rules) throws org.molgenis.framework.db.DatabaseException
	{
		return db.find(${JavaName(entity)}.class, rules);
	}	
	
<#foreach key in entity.getAllKeys()>	
	/**
	 * 
	 */
	public static ${JavaName(entity)} findBy<#list key.fields as f>${JavaName(f)}</#list>(org.molgenis.framework.db.Database db<#list key.fields as f>, ${type(f)} ${name(f)}</#list>) throws org.molgenis.framework.db.DatabaseException
	{
		org.molgenis.framework.db.Query<${JavaName(entity)}> q = db.query(${JavaName(entity)}.class);
		<#list key.fields as f>q.eq(${JavaName(entity)}.${f.name?upper_case}, ${name(f)});</#list>
		java.util.List<${JavaName(entity)}> result = q.find();
		if(result.size()>0) return result.get(0);
		else return null;
	}

</#foreach>	
	
	// member variables (including setters.getters for interface)
	<#foreach field in entity.getImplementedFields()>
	<#if field.annotations?exists>
	${field.annotations}
	</#if>

	//${field.description}[type=${field.type}]
	<#if !isPrimaryKey(field,entity) || !entity.hasAncestor()>
 			<#if isPrimaryKey(field,entity) && !entity.hasAncestor()>
    			<#if field.auto = true>
	    			<#if jpa_use_sequence >
	@javax.persistence.SequenceGenerator(name="${JavaName(entity)}_Gen", sequenceName="${JavaName(entity)}_Seq"<#if entity.allocationSize??>, allocationSize=${entity.allocationSize?c}</#if>)
    @javax.persistence.Id @javax.persistence.GeneratedValue(generator="${JavaName(entity)}_Gen", strategy=javax.persistence.GenerationType.SEQUENCE)		
    				<#else>
    @javax.persistence.Id @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
    				</#if>   			
    			<#else>
    			@Id
    			</#if>
    		</#if>
		</#if>	
		<#assign key_found = 0>
		<#foreach index in entity.indices>
			<#if key_found == 1>
				<#break>
			</#if>
			<#if index.name == field.name>
//	@org.hibernate.search.annotations.Field(index=org.hibernate.search.annotations.Index.TOKENIZED, store=org.hibernate.search.annotations.Store.NO)
				<#assign key_found = 1>
			</#if>
		</#foreach>
		<#foreach unique in entity.getUniqueKeysWithoutPk()>
			<#if key_found == 1>
				<#break>
			</#if>
			<#foreach unique_field in unique.fields>
				<#if unique_field.name == field.name>
//	@org.hibernate.search.annotations.Field(index=org.hibernate.search.annotations.Index.TOKENIZED, store=org.hibernate.search.annotations.Store.NO)
					<#assign key_found = 1>
				</#if>
			</#foreach>
		</#foreach>
        <#if field.type == "date">
    @javax.persistence.Temporal(javax.persistence.TemporalType.DATE)
    	<#elseif field.type == "datetime">
    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    	</#if>
        <#if field.type == "mref">
			<#assign multipleXrefs = model.getNumberOfReferencesTo(field.xrefEntity)/>
    @javax.persistence.ManyToMany(<#if field.jpaCascade??>fetch=javax.persistence.FetchType.LAZY, cascade={${field.jpaCascade}}<#else>fetch=javax.persistence.FetchType.LAZY /*cascade={javax.persistence.CascadeType.MERGE, javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.REFRESH}*/</#if>)
    @javax.persistence.JoinColumn(name="${SqlName(field)}", insertable=true, updatable=true, nullable=${field.isNillable()?string})
			<#if multipleXrefs &gt; 1>
	@javax.persistence.JoinTable(name="${Name(entity)}_${SqlName(field)}", 
			joinColumns=@javax.persistence.JoinColumn(name="${Name(entity)}"), inverseJoinColumns=@javax.persistence.JoinColumn(name="${SqlName(field)}"))
			<#else> 
	@javax.persistence.JoinTable(name="${Name(entity)}_${SqlName(field)}", 
			joinColumns=@javax.persistence.JoinColumn(name="${Name(entity)}"), inverseJoinColumns=@javax.persistence.JoinColumn(name="${SqlName(field)}"))			
			</#if>			
       	<#elseif field.type == "xref">
    @javax.persistence.ManyToOne(<#if field.jpaCascade??>fetch=javax.persistence.FetchType.LAZY, cascade={${field.jpaCascade}}<#else>fetch=javax.persistence.FetchType.LAZY /*cascade={javax.persistence.CascadeType.MERGE, javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.REFRESH}*/</#if>)
    @javax.persistence.JoinColumn(name="${SqlName(field)}"<#if !field.nillable>, nullable=false</#if>)   	
       	<#else>
			<#if isPrimaryKey(field,entity)>
				<#if !entity.hasAncestor()>
    @javax.persistence.Column(name="${SqlName(field)}"<#if !field.nillable>, nullable=false</#if>)
	@javax.xml.bind.annotation.XmlElement(name="${name(field)}")
				</#if>
			<#else>
				<#if field.type == "text" >			
//	@javax.persistence.Lob()
	@javax.persistence.Column(name="${SqlName(field)}", length=16777216<#if !field.nillable>, nullable=false</#if>)
				<#else>
        <#if SqlName(field) == '__Type'>
	@javax.persistence.Column(name="DType"<#if !field.nillable>, nullable=false</#if>)            
        <#else>
	@javax.persistence.Column(name="${SqlName(field)}"<#if !field.nillable>, nullable=false</#if>)
        </#if>
	@javax.xml.bind.annotation.XmlElement(name="${name(field)}")
				</#if>
			</#if>   	
       	</#if>
	
		<#assign type_label = field.getType().toString()>
			<#if isPrimaryKey(field,entity)>
				<#if !entity.hasAncestor()>
	//@javax.validation.constraints.NotNull
	private <#if field.type="xref">${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}<#elseif field.type="mref">java.util.List<${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}><#else>${type(field)}</#if> ${name(field)} = <#if field.type == "mref">new java.util.ArrayList<${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}>()<#elseif field.type == "xref">null<#else> ${default(field)}</#if>;
				</#if>
			<#else>
				

				<#if !field.isNillable() >
	@javax.validation.constraints.NotNull
				</#if>
	private <#if field.type="xref">${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}<#elseif field.type="mref">java.util.List<${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}><#else>${type(field)}</#if> ${name(field)} = <#if field.type == "mref">new java.util.ArrayList<${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}>()<#elseif field.type == "xref">null<#else> ${default(field)}</#if>;
			</#if>
		<#if type_label == "enum">
	@javax.persistence.Transient
	private String ${name(field)}_label = null;
	@javax.persistence.Transient
	private java.util.List<org.molgenis.util.ValueLabel> ${name(field)}_options = new java.util.ArrayList<org.molgenis.util.ValueLabel>();
		<#elseif type_label == "xref">
	@javax.persistence.Transient
	private ${type(field.xrefField)} ${name(field)}_${name(field.xrefField)} = ${default(field)};	
			<#if field.xrefLabelNames[0] != field.xrefFieldName><#list field.xrefLabelNames as label>
	@javax.persistence.Transient
	private ${type(field.xrefLabels[label_index])} ${name(field)}_${label} = null;						
			</#list></#if>
			<#elseif type_label == "mref">
	@javax.persistence.Transient
	private java.util.List<${type(field.xrefField)}> ${name(field)}_${name(field.xrefField)} = new java.util.ArrayList<${type(field.xrefField)}>();		
			<#if field.xrefLabelNames[0] != field.xrefFieldName><#list field.xrefLabelNames as label>
	@javax.persistence.Transient
	private java.util.List<String> ${name(field)}_${label} = new java.util.ArrayList<String>();
			</#list></#if>	
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
	
	<#list entity.getFields() as f>
		<#if f.type == "enum">
		//options for enum ${JavaName(f)}
			<#list f.getEnumOptions() as option>
		${name(f)}_options.add(new org.molgenis.util.ValueLabel("${option}","${option}"));
			</#list>
		</#if>	
	</#list>
	}
	
	/** copy constructor */
	public ${JavaName(entity)}(${JavaName(entity)} copyMe) throws Exception
	{	
		for(String f : this.getFields())
		{
			this.set(f, copyMe.get(f));
		}	
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
	public <#if field.type =="xref">${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}<#else>${type(field)}</#if> get${JavaName(field)}()
	{
		return this.${name(field)};
	}
	
				</#if>
			<#else>
	/**
	 * Get the ${field.description}.
	 * @return ${name(field)}.
	 */
	public <#if field.type =="xref">${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}<#elseif field.type == "mref">java.util.List<${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}><#else>${type(field)}</#if> get${JavaName(field)}()
	{
		return this.${name(field)};
	}
	
	@Deprecated
	public <#if field.type =="xref">${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}<#elseif field.type == "mref">java.util.List<${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}><#else>${type(field)}</#if> get${JavaName(field)}(org.molgenis.framework.db.Database db)
	{
		throw new UnsupportedOperationException();
	}	
			</#if>
	
			<#if isPrimaryKey(field,entity)>
				<#if !entity.hasAncestor()>
	/**
	 * Set the ${field.description}.
	 * @param ${name(field)}
	 */
	public void set${JavaName(field)}( <#if field.type =="xref">${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}<#elseif field.type == "mref">java.util.List<${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}><#else>${type(field)}</#if> ${name(field)})
	{
		this.${name(field)} = ${name(field)};
	}
				</#if>
			<#else>
	/**
	 * Set the ${field.description}.
	 * @param ${name(field)}
	 */
	public void set${JavaName(field)}( <#if field.type =="xref">${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}<#elseif field.type == "mref">java.util.List<${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)}><#else>${type(field)}</#if> ${name(field)})
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
		this.set${JavaName(field)}(string2date(datestring));
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
	
	<#elseif type_label == "xref">
	
	/**
	 * Set foreign key for field ${name(field)}.
	 * This will erase any foreign key objects currently set.
	 * FIXME: can we autoload the new object?
	 */
	public void set${JavaName(field)}_${JavaName(field.xrefField)}(${type(field.xrefField)} ${name(field)}_${name(field.xrefField)})
	{
		this.${name(field)}_${name(field.xrefField)} = ${name(field)}_${name(field.xrefField)};
	}	

	public void set${JavaName(field)}(${type(field.xrefField)} ${name(field)}_${name(field.xrefField)})
	{
		this.${name(field)}_${name(field.xrefField)} = ${name(field)}_${name(field.xrefField)};
	}
	
	public ${type(field.xrefField)} get${JavaName(field)}_${JavaName(field.xrefField)}()
	{
		
		if(${name(field)} != null) 
		{
			return ${name(field)}.get${JavaName(field.xrefField)}();
		}
		else
		{
			return ${name(field)}_${name(field.xrefField)};
		}
	}	
	 
<#if field.xrefLabelNames[0] != field.xrefFieldName><#list field.xrefLabelNames as label>
	/**
	 * Get a pretty label ${label} for cross reference ${JavaName(field)} to ${JavaName(field.xrefEntity)}.${JavaName(field.xrefField)}.
	 */
	public ${type(field.xrefLabels[label_index])} get${JavaName(field)}_${JavaName(label)}()
	{		
		//FIXME should we auto-load based on get${JavaName(field)}()?	
		if(${name(field)} != null) {
			return ${name(field)}.get${JavaName(label)}();
		} else {
			return ${name(field)}_${label};
		}
	}		
	
	/**
	 * Set a pretty label for cross reference ${JavaName(field)} to <a href="${JavaName(field.xrefEntity)}.html#${JavaName(field.xrefField)}">${JavaName(field.xrefEntity)}.${JavaName(field.xrefField)}</a>.
	 * Implies set${JavaName(field)}(null) until save
	 */
	public void set${JavaName(field)}_${JavaName(label)}(${type(field.xrefLabels[label_index])} ${name(field)}_${label})
	{
		this.${name(field)}_${label} = ${name(field)}_${label};
	}		
</#list></#if>
	 
	
	<#elseif type_label="mref">
	public void set${JavaName(field)}_${JavaName(field.xrefField)}(${type(pkey(field.xrefEntity))} ... ${name(field)})
	{
		this.set${JavaName(field)}_${JavaName(field.xrefField)}(java.util.Arrays.asList(${name(field)}));
	}	
	
	public void set${JavaName(field)}(${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)} ... ${name(field)})
	{
		this.set${JavaName(field)}(java.util.Arrays.asList(${name(field)}));
	}	
	
	/**
	 * Set foreign key for field ${name(field)}.
	 * This will erase any foreign key objects currently set.
	 * FIXME: can we autoload the new object?
	 */
	public void set${JavaName(field)}_${JavaName(field.xrefField)}(java.util.List<${type(field.xrefField)}> ${name(field)}_${name(field.xrefField)})
	{
		this.${name(field)}_${name(field.xrefField)} = ${name(field)}_${name(field.xrefField)};
	}	
	
	public java.util.List<${type(field.xrefField)}> get${JavaName(field)}_${JavaName(field.xrefField)}()
	{
<#if databaseImp = 'JPA'>
		if(${name(field)} != null && !${name(field)}.isEmpty()) {
			java.util.List<${type(field.xrefField)}> result = new java.util.ArrayList<${type(field.xrefField)}>();
//			for(${type(field.xrefField)} xref: ${name(field)}_${name(field.xrefField)}) {
//				result.add(xref);
//			}
			for (int i = 0; i < ${name(field)}.size(); i++)
				result.add(${name(field)}.get(i).getId());
			return result;
		} else {
			if(${name(field)}_${name(field.xrefField)} == null) {
				${name(field)}_${name(field.xrefField)} = new java.util.ArrayList<Integer>();
			}		
			return ${name(field)}_${name(field.xrefField)};
		}
<#else>
		return ${name(field)}_${name(field.xrefField)};
</#if>
	}	
	
<#if field.xrefLabelNames[0] != field.xrefFieldName><#list field.xrefLabelNames as label>	
	/**
	 * Get a pretty label for cross reference ${JavaName(field)} to <a href="${JavaName(field.xrefEntity)}.html#${JavaName(field.xrefField)}">${JavaName(field.xrefEntity)}.${JavaName(field.xrefField)}</a>.
	 */
	public java.util.List<${type(field.xrefLabels[label_index])}> get${JavaName(field)}_${JavaName(label)}()
	{
		if(this.${name(field)} != null && !this.${name(field)}.isEmpty())
		{
			java.util.List<${type(field.xrefLabels[label_index])}> result = new java.util.ArrayList<${type(field.xrefLabels[label_index])}>(this.${name(field)}.size());
			for(${field.xrefEntity.namespace}.${JavaName(field.xrefEntity)} o: ${name(field)}) result.add(o.get${JavaName(label)}().toString());
			return java.util.Collections.unmodifiableList(result);
		}	
		else
		{	
			return ${name(field)}_${label};
		}
	}
	
	/**
	 * Update the foreign key ${JavaName(field)}
	 * This sets ${name(field)} to null until next database transaction.
	 */
	public void set${JavaName(field)}_${JavaName(label)}(java.util.List<String> ${name(field)}_${label})
	{
		this.${name(field)}_${label} = ${name(field)}_${label};
	}		
</#list></#if>		
	
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
	public Object get(String name)
	{
		name = name.toLowerCase();
		<#foreach field in allFields(entity)>
		if (name.equals("${name(field)?lower_case}"))
			return get${JavaName(field)}();
		<#if field.type == "enum" >	
		if(name.equals("${name(field)?lower_case}_label"))
			return get${JavaName(field)}Label();
		<#elseif field.type == "xref" || field.type == "mref">
		if(name.equals("${name(field)?lower_case}_${name(field.xrefField)?lower_case}"))
			return get${JavaName(field)}_${JavaName(field.xrefField)}();
<#if field.xrefLabelNames[0] != field.xrefFieldName><#list field.xrefLabelNames as label>	
		if(name.equals("${name(field)?lower_case}_${label?lower_case}"))
			return get${JavaName(field)}_${JavaName(label)}();
</#list></#if>			
		</#if>
	</#foreach>		
		return null;
	}	
	
	public void validate() throws org.molgenis.framework.db.DatabaseException
	{
	<#list allFields(entity) as field><#if field.nillable == false>
		if(this.get${JavaName(field)}() == null) throw new org.molgenis.framework.db.DatabaseException("required field ${name(field)} is null");
	</#if></#list>
	}
	
	<#include "DataTypeCommons.java.ftl">	

	@Override
	public String toString()
	{
		return this.toString(false);
	}
	
	public String toString(boolean verbose)
	{
		String result = "${JavaName(entity)}(";
<#list allFields(entity) as field>
	<#assign type_label = field.getType().toString()>
		<#if field.type.toString() == "datetime">
		result+= "${name(field)}='" + (get${JavaName(field)}() == null ? "" : new java.text.SimpleDateFormat("MMMM d, yyyy, HH:mm:ss", java.util.Locale.US).format(get${JavaName(field)}()))+"'<#if field_has_next> </#if>";
		result+= "${name(field)}='" + (get${JavaName(field)}() == null ? "" : new java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.US).format(get${JavaName(field)}()))+"'<#if field_has_next> </#if>";		
		<#elseif field.type == "xref" || field.type == "mref">
		result+= " ${name(field)}_${name(field.xrefField)}='" + get${JavaName(field)}_${JavaName(field.xrefField)}()+"' ";	
			<#if field.xrefLabelNames[0] != field.xrefFieldName><#list field.xrefLabelNames as label>		
		result+= " ${name(field)}_${name(label)}='" + get${JavaName(field)}_${JavaName(label)}()+"' ";
			</#list></#if>
		<#else>
		result+= "${name(field)}='" + get${JavaName(field)}()+"'<#if field_has_next> </#if>";	
		</#if>
</#list>
		result += ");";
		return result;

	}

	/**
	 * Get the names of all public properties of ${JavaName(entity)}.
	 */
	public java.util.Vector<String> getFields(boolean skipAutoIds)
	{
		java.util.Vector<String> fields = new java.util.Vector<String>();
	<#list allFields(entity) as field>
		<#if (field.auto && field.type = "int")>
		if(!skipAutoIds)
		</#if>
		{
			<#if field.type="xref" || field.type="mref">
			fields.add("${name(field)}_${name(field.getXrefEntity().getPrimaryKey())}");
			<#else>
			fields.add("${name(field)}");
			</#if>
		}
		<#if field.type="xref" || field.type="mref">
			<#if field.xrefLabelNames[0] != field.xrefFieldName><#list field.xrefLabelNames as label>
		fields.add("${name(field)}_${name(label)}");
			</#list></#if>
		</#if>
	</#list>		
		return fields;
	}	

	public java.util.Vector<String> getFields()
	{
		return getFields(false);
	}

	@Override
	public String getIdField()
	{
		return "${name(pkey(entity))}";
	}
	

	
	@Override
	public java.util.List<String> getLabelFields()
	{
		java.util.List<String> result = new java.util.ArrayList<String>();
		<#if entity.getXrefLabels()?exists><#list entity.getXrefLabels() as label>
		result.add("${label}");
		</#list></#if>
		return result;
	}

	@Deprecated
	public String getFields(String sep)
	{
		return (""
	<#list allFields(entity) as field>
		+ "${name(field)}" <#if field_has_next>+sep</#if>
	</#list>
		);
	}

<#if !entity.abstract>	
	@Override
	public Object getIdValue()
	{
		return get(getIdField());
	}		
	
	
    public String getXrefIdFieldName(String fieldName) {
        <#list allFields(entity) as field>
        	<#if field.type = 'xref' >
        if (fieldName.equalsIgnoreCase("${name(field)}")) {
            return "${name(field.getXrefEntity().getPrimaryKey())}";
        }
        	</#if>
                <#if field.type = 'mref' >
        if (fieldName.equalsIgnoreCase("${name(field)}")) {
            return "${name(field.getXrefEntity().getPrimaryKey())}";
        }
        	</#if>
        </#list>
        
        <#if !(superclasses(entity)??) >
        return super.getXrefIdFieldName(fieldName);
        <#else>
        return null;
        </#if>
    }	
</#if>

	@Override
	public boolean equals(Object obj) {
   		if (obj == null) { return false; }
   		if (obj == this) { return true; }
   		if (obj.getClass() != getClass()) {
     		return false;
   		}
		${JavaName(entity)} rhs = (${JavaName(entity)}) obj;
   		return new org.apache.commons.lang.builder.EqualsBuilder()
<#if entity.hasAncestor()>
             	.appendSuper(super.equals(obj))
</#if>
<#list entity.getUniqueKeysWithoutPk() as uniqueKeys >
	<#list key_fields(uniqueKeys) as uniqueFields >
		//${name(uniqueFields)}
		<#if uniqueFields.type != "mref" && uniqueFields.type != "xref">
			<#if name(uniqueFields) != "type_" >
				.append(${name(uniqueFields)}, rhs.get${JavaName(uniqueFields)}())
			</#if>
		</#if>
	</#list>
</#list>                 
                .isEquals();
  	}

  	@Override
    public int hashCode() {
    	int firstNumber = this.getClass().getName().hashCode();
    	int secondNumber = this.getClass().getSimpleName().hashCode();
    	if(firstNumber % 2 == 0) {
    	  firstNumber += 1;
    	}
    	if(secondNumber % 2 == 0) {
    		secondNumber += 1;
    	}
    
		return new org.apache.commons.lang.builder.HashCodeBuilder(firstNumber, secondNumber)
<#if entity.hasAncestor()>
             	.appendSuper(super.hashCode())
</#if>
<#list entity.getUniqueKeysWithoutPk() as uniqueKeys >
	<#list key_fields(uniqueKeys) as uniqueFields >
		<#if uniqueFields.type != "mref" && uniqueFields.type != "xref">
			<#if name(uniqueFields) != 'type_' >
				.append(${name(uniqueFields)})
			</#if>
		</#if>
	</#list>
</#list>    
   			.toHashCode();
    }  	
  	


	@Deprecated
	public String getValues(String sep)
	{
		java.io.StringWriter out = new java.io.StringWriter();
	<#list allFields(entity) as field>
		{
			Object valueO = get${JavaName(field)}();
			String valueS;
			if (valueO != null)
				valueS = valueO.toString();
			else 
				valueS = "";
			valueS = valueS.replaceAll("\r\n"," ").replaceAll("\n"," ").replaceAll("\r"," ");
			valueS = valueS.replaceAll("\t"," ").replaceAll(sep," ");
			out.write(valueS<#if field_has_next>+sep</#if>);
		}
	</#list>
		return out.toString();
	}
	
	@Override
	public ${JavaName(entity)} create(org.molgenis.util.tuple.Tuple tuple) throws Exception
	{
		${JavaName(entity)} e = new ${JavaName(entity)}();
		e.set(tuple);
		return e;
	}
	
<#list model.entities as e>
	<#if !e.abstract && !e.isAssociation()>
		<#list e.implementedFields as f>
			<#if f.type=="xref" && f.getXrefEntityName() == entity.name>
				<#assign multipleXrefs = model.getNumberOfReferencesTo(entity)/>
//${multipleXrefs}
	@javax.persistence.OneToMany(fetch=javax.persistence.FetchType.LAZY, mappedBy="${name(f)}"/*, cascade={javax.persistence.CascadeType.MERGE, javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.REFRESH}*/)
    private java.util.Collection<${f.entity.namespace}.${JavaName(f.entity)}> ${name(f)}<#if multipleXrefs &gt; 0 >${JavaName(f.entity)}</#if>Collection = new java.util.ArrayList<${f.entity.namespace}.${JavaName(f.entity)}>();

	@javax.xml.bind.annotation.XmlTransient
	public java.util.Collection<${f.entity.namespace}.${JavaName(f.entity)}> get${JavaName(f)}<#if multipleXrefs &gt; 0 >${JavaName(f.entity)}</#if>Collection()
	{
            return ${name(f)}<#if multipleXrefs &gt; 0 >${JavaName(f.entity)}</#if>Collection;
	}

    public void set${JavaName(f)}<#if multipleXrefs &gt; 0 >${JavaName(f.entity)}</#if>Collection(java.util.Collection<${f.entity.namespace}.${JavaName(f.entity)}> collection)
    {
        for (${f.entity.namespace}.${JavaName(f.entity)} ${name(f.entity)} : collection) {
            ${name(f.entity)}.set${JavaName(f)}(this);
        }
        ${name(f)}<#if multipleXrefs &gt; 0 >${JavaName(f.entity)}</#if>Collection = collection;
    }	
		</#if>
	</#list></#if>
</#list>
<#list model.entities as e>
	<#if !e.abstract && !e.isAssociation()>
		<#list e.implementedFields as f>
			<#if f.type=="mref" && f.getXrefEntityName() == entity.name>
				<#assign multipleXrefs = model.getNumberOfReferencesTo(entity)/>
	//${multipleXrefs}
    @javax.persistence.ManyToMany(fetch=javax.persistence.FetchType.LAZY, mappedBy="${name(f)}"/*, cascade={javax.persistence.CascadeType.MERGE, javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.REFRESH}*/)
    private java.util.Collection<${f.entity.namespace}.${JavaName(f.entity)}> ${name(f)}<#if multipleXrefs &gt; 1 >${JavaName(f.entity)}</#if>Collection = new java.util.ArrayList<${f.entity.namespace}.${JavaName(f.entity)}>();

	@javax.xml.bind.annotation.XmlTransient
	public java.util.Collection<${f.entity.namespace}.${JavaName(f.entity)}> get${JavaName(f)}<#if multipleXrefs &gt; 1 >${JavaName(f.entity)}</#if>Collection()
	{
        return ${name(f)}<#if multipleXrefs &gt; 1 >${JavaName(f.entity)}</#if>Collection;
	}

	@javax.xml.bind.annotation.XmlTransient
	public java.util.Collection<${f.entity.namespace}.${JavaName(f.entity)}> get${JavaName(f)}<#if multipleXrefs &gt; 1 >${JavaName(f.entity)}</#if>Collection(org.molgenis.framework.db.Database db)
	{
        return get${JavaName(f)}<#if multipleXrefs &gt; 1 >${JavaName(f.entity)}</#if>Collection();
	}

    public void set${JavaName(f)}<#if multipleXrefs &gt; 1 >${JavaName(f.entity)}</#if>Collection(java.util.Collection<${f.entity.namespace}.${JavaName(f.entity)}> collection)
    {
    	${name(f)}<#if multipleXrefs &gt; 1 >${JavaName(f.entity)}</#if>Collection.addAll(collection);
    }	
			</#if>
		</#list>
	</#if>
</#list>

	
</#if>
}

