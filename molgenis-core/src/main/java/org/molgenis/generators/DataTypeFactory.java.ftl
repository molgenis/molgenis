<#--this one is included as part of DataTypeGen/JpaDataTypeGen-->

<#if !entity.abstract>
	/** A build to create easily a ${JavaName(entity)}() with shorthand:
	 * <code>${JavaName(entity)} o = ${JavaName(entity)}.new().name("s1").description("a sample").create()</code>
	 * Instead of:
	 * <code>
	 * ${JavaName(entity)} o = new ${JavaName(entity)}();
	 * o.setName("s1");
	 * o.setDescription("a sample");
	 * </code>
	 */
	public static class build
	{
		protected ${JavaName(entity)} o = new ${JavaName(entity)}();
		
<#list entity.getImplementedFields() as f>
		/** Shorthand for o.set${JavaName(f)}(${name(f)}) */
		public build ${name(f)}(${type(f)} ${name(f)})
		{
			o.set${JavaName(f)}(${name(f)});
			return this;
		}
		<#if f.type="xref">			
			<#if f.xrefLabelNames[0] != f.xrefFieldName><#list f.xrefLabelNames as label>
		public build ${name(f)}_${name(label)}(${type(f.xrefLabels[label_index])} ${name(f)}_${label})
		{
			o.set${JavaName(f)}_${JavaName(label)}(${name(f)}_${label});
			return this;
		}
			</#list>
		</#if>		
		<#elseif f.type == "mref">	
			<#if f.xrefLabelNames[0] != f.xrefFieldName><#list f.xrefLabelNames as label>
		public build ${name(f)}_${name(label)}(java.util.List<${type(f.xrefLabels[label_index])}> ${name(f)}_${label}List)
		{
			o.set${JavaName(f)}_${JavaName(label)}(${name(f)}_${label}List);
			return this;		
		}	
			</#list>
		</#if>						
		<#elseif f.type == "file" || f.type =="image" >
		public build set${name(f)}File(File file)
		{
			o.set${JavaName(f)}AttachedFile(file);
			return this;	
		}
		</#if>
		
		
</#list>		
	
		public ${JavaName(entity)} create()
		{
			return o;
		}
		
		public ${JavaName(entity)} create(org.molgenis.framework.db.Database db) throws DatabaseException, IOException
		{
			db.add(o);
			return o;
		}
	
	}
	
<#--	
/*
The code below is a second experiment way to create objects with shorthand notation using static methods and parameter objects
For example:
import static org.molgenis.pheno.Measurement.*;

Measurement m = new Measurement( name("lenght"), description("lenght standing up") );
*/

	/** names of all fields */
	static enum ${JavaName(entity)}OptionName
	{
		<@compress single_line=true><#assign first = true/>
		<#list allFields(entity) as f>
			<#if !first>,<#else><#assign first=false/></#if>
			${name(f)}
		</#list></@compress>;

		private final Object dflt;

		private ${JavaName(entity)}OptionName(Object dflt)
		{
			this.dflt = dflt;
		}

		private ${JavaName(entity)}OptionName()
		{
			this.dflt = null;
		}
	}

	public static class ${JavaName(entity)}Option
	{
		private final ${JavaName(entity)}OptionName name;
		private final Object value;

		private ${JavaName(entity)}Option(${JavaName(entity)}OptionName name, Object value)
		{
			this.name = name;
			this.value = value;
		}
	}

<#list allFields(entity) as f>
	public static ${JavaName(entity)}Option ${name(f)}(String ${name(f)})
	{
		return new ${JavaName(entity)}Option(${JavaName(entity)}OptionName.${name(f)}, ${name(f)});
	}
</#list>

	/**
	 * Constructor that allows surrogate 'named parameters', for example:
	 * <code>
	 * import static ${package}.${JavaName(entity)}.*;
	 * ...
	 * ${JavaName(entity)} o = new ${JavaName(entity)}(<@compress single_line=true><#assign first = true/>
		<#list allFields(entity) as f>
			<#if !first>,<#else><#assign first=false/></#if>
			${name(f)}(my${JavaName(f)})
		</#list></@compress>);
	 * </code>
	 * Advantages
	 * <li>Order doesn't matter
	 * <li>You only provide parameters you want different from default
	 * <li>Code is more readable than using normal contructor.
	 * Caveats:
	 * <li>The method name can be amgibuous. Then use full namespace, e.g. 
	 * <code>
	 * import static ${package}.${JavaName(entity)}.*;
	 * ...
	 * ${JavaName(entity)} o = new ${JavaName(entity)}(<@compress single_line=true><#assign first = true/>
		<#list allFields(entity) as f>
			<#if !first>,<#else><#assign first=false/></#if>
			${JavaName(entity)}.${name(f)}(my${JavaName(f)})
		</#list></@compress>);
	 * </code>	 
	 * @param options
	 */
	public ${JavaName(entity)}(${JavaName(entity)}Option ...options)
	{
		for ( ${JavaName(entity)}Option o : options) {
			switch(o.name) {

<#list allFields(entity) as f>
				case ${name(f)}:
					this.set${JavaName(f)}((${type(f)})o.value);
</#list>
			}
		}
	}
-->
</#if>