	@Override
	public void set(org.molgenis.data.Entity entity, boolean strict)
	{
<#list allFields(entity) as f>
	<#assign type_label = f.getType().toString()>
	<#if f.type == "mref">
		//set ${JavaName(f)}
		if( entity.getEntities("${f.name}", ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class) != null || entity.getEntities("${f.name?lower_case}", ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class) != null ) 
		{
			java.lang.Iterable<${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}> mrefs = entity.getEntities("${f.name}", ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class);
			if(mrefs == null) mrefs = entity.getEntities("${f.name?lower_case}", ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class);
			if(entity.getEntities("${entity.name?lower_case}_${f.name?lower_case}", ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class)!= null) mrefs = entity.getEntities("${entity.name?lower_case}_${f.name?lower_case}", ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class);
			else if(entity.getEntities("${entity.name}_${f.name}", ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class)!= null) mrefs = entity.getEntities("${entity.name}_${f.name}", ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class);									
			this.set${JavaName(f)}(mrefs != null ? com.google.common.collect.Lists.newArrayList(mrefs) : null);
		}				
	<#else>
		//set ${JavaName(f)}
		// query formal name, else lowercase name
		<#if f.type == "xref" || f.type == "categorical">
		if( entity.getEntity("${f.name}", ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class) != null) { 
			this.set${JavaName(f)}(entity.getEntity("${f.name}", ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class));				
		}
		else if( entity.getEntity("${f.name?lower_case}", ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class) != null) { 
			this.set${JavaName(f)}(entity.getEntity("${f.name?lower_case}", ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class));				
		}
		else if( entity.getEntity("${entity.name}_${f.name}", ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class) != null) { 
			this.set${JavaName(f)}(entity.getEntity("${entity.name}_${f.name}", ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class));				
		}
		else if( entity.getEntity("${entity.name?lower_case}_${f.name?lower_case}", ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class) != null) { 
			this.set${JavaName(f)}(entity.getEntity("${entity.name}_${f.name}", ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class));				
		}
		<#else>
		if(entity.get${settertype(f)}("${f.name?lower_case}") != null) this.set${JavaName(f)}(entity.get${settertype(f)}("${f.name?lower_case}"));
		else if(entity.get${settertype(f)}("${f.name}") != null) this.set${JavaName(f)}(entity.get${settertype(f)}("${f.name}"));
		else if(strict) this.set${JavaName(f)}(entity.get${settertype(f)}("${f.name?lower_case}")); // setting null is not an option due to function overloading
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
</#list>
	}
