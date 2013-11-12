	@Override
	public void set(org.molgenis.data.Entity entity, boolean strict)
	{
<#list allFields(entity) as f>
	<#assign type_label = f.getType().toString()>
	<#if f.type == "mref">
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
		<#if f.type == "xref">
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
