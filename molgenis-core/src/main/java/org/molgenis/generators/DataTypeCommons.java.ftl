	@Override
	public void set(org.molgenis.data.Entity entity, boolean strict ) throws Exception
	{
<#list allFields(entity) as f>
	<#assign type_label = f.getType().toString()>
	<#if f.type == "mref">
		//set ${JavaName(f)}
		if( entity.get("${f.name}") != null || entity.get("${entity.name}_${f.name}") != null || entity.get("${f.name?lower_case}") != null || entity.get("${entity.name?lower_case}_${f.name?lower_case}") != null) 
		{
			java.util.List<${type(f.xrefField)}> values = new java.util.ArrayList<${type(f.xrefField)}>();
			java.util.List<?> mrefs = entity.getList("${f.name}");
			if(mrefs == null) mrefs = entity.getList("${f.name?lower_case}");
			if(entity.get("${entity.name?lower_case}_${f.name?lower_case}")!= null) mrefs = entity.getList("${entity.name?lower_case}_${f.name?lower_case}");
			else if(entity.get("${entity.name}_${f.name}")!= null) mrefs = entity.getList("${entity.name}_${f.name}");
			if(mrefs != null) for(Object ref: mrefs)
			{
				if(ref instanceof String)
					values.add(${type(xrefField(model,f))}.parse${settertype(xrefField(model,f))}((String)ref));
				else if(ref instanceof org.molgenis.util.AbstractEntity) 	
					values.add((${type(xrefField(model,f))})((org.molgenis.util.AbstractEntity)ref).getIdValue() );
				else
					values.add((${type(xrefField(model,f))})ref);		
			}											
			this.set${JavaName(f)}_${JavaName(f.xrefField)}( values );
		}
	<#if f.xrefLabelNames[0] != f.xrefFieldName><#list f.xrefLabelNames as label>
		//set labels ${label} for mref field ${JavaName(f)}	
		if( entity.get("${f.name}_${label}") != null || entity.get("${entity.name}_${f.name}_${label}")!= null || entity.get("${f.name?lower_case}_${label?lower_case}") != null || entity.get("${entity.name?lower_case}_${f.name?lower_case}_${label?lower_case}") != null) 
		{
			java.util.List<${type(f.xrefLabels[label_index])}> values = new java.util.ArrayList<${type(f.xrefLabels[label_index])}>();
			java.util.List<?> mrefs = entity.getList("${f.name}_${label}");
			if(mrefs == null) mrefs = entity.getList("${f.name?lower_case}_${label?lower_case}");
			if(entity.get("${entity.name}_${f.name}_${label}")!= null) mrefs = entity.getList("${entity.name}_${f.name}_${label}");
			else if(entity.get("${entity.name?lower_case}_${f.name?lower_case}_${label?lower_case}")!= null) mrefs = entity.getList("${entity.name?lower_case}_${f.name?lower_case}_${label?lower_case}");
			if(mrefs != null) 
				for(Object ref: mrefs)
				{
				<#if type(f.xrefLabels[label_index]) == "String">
					String[] refs = ref.toString().split("\\|");
					for(String r : refs) {
						values.add(r);	
					}						
				<#else>
			  		<#if JavaType(f.xrefField) == "String" >
			  		values.add((${JavaType(f.xrefField)})ref);
			  		<#else>
			  		values.add(${type(f.xrefField)}.parse${settertype(f.xrefField)}((ref.toString())));
			  		</#if>						
				</#if>
				}							
			this.set${JavaName(f)}_${JavaName(label)}( values );			
		}	
	</#list></#if>					
	<#else>
		//set ${JavaName(f)}
		// query formal name, else lowercase name
		<#if f.type == "xref">
		if(entity.get${settertype(f)}("${f.name}_${f.xrefField.name}") != null) this.set${JavaName(f)}(entity.get${settertype(f)}("${f.name}_${f.xrefField.name}"));
		else if(entity.get${settertype(f)}("${f.name?lower_case}_${f.xrefField.name?lower_case}") != null) this.set${JavaName(f)}(entity.get${settertype(f)}("${f.name?lower_case}_${f.xrefField.name?lower_case}"));
		else if(strict) this.set${JavaName(f)}(entity.get${settertype(f)}("${f.name}_${f.xrefField.name}")); // setting null is not an option due to function overloading
		
		if( entity.get${settertype(f)}("${entity.name}_${f.name}_${f.xrefField.name}") != null) this.set${JavaName(f)}(entity.get${settertype(f)}("${entity.name}_${f.name}_${f.xrefField.name}"));
		else if( entity.get${settertype(f)}("${entity.name?lower_case}_${f.name?lower_case}_${f.xrefField.name?lower_case}") != null) this.set${JavaName(f)}(entity.get${settertype(f)}("${entity.name?lower_case}_${f.name?lower_case}_${f.xrefField.name?lower_case}"));
		//alias of xref
		if( entity.get("${f.name}") != null) { 
			if(org.molgenis.util.AbstractEntity.isObjectRepresentation(entity.get("${f.name}").toString())) {
				${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)} instance = org.molgenis.util.AbstractEntity.setValuesFromString((String)entity.get("${f.name}"), ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class);
				this.set${JavaName(f)}(instance);				
			} else {
				this.set${JavaName(f)}_${JavaName(f.xrefField)}(entity.get${settertype(f.xrefField)}("${f.name}")); 
			}
		}
		else if( entity.get("${f.name?lower_case}") != null) { 
			if(org.molgenis.util.AbstractEntity.isObjectRepresentation(entity.get("${f.name?lower_case}").toString())) {
				${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)} instance = org.molgenis.util.AbstractEntity.setValuesFromString((String)entity.get("${f.name?lower_case}"), ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class);
				this.set${JavaName(f)}(instance);				
			} else {
				this.set${JavaName(f)}_${JavaName(f.xrefField)}(entity.get${settertype(f.xrefField)}("${f.name}"));
			}
		}
		if( entity.get("${entity.name}_${f.name}") != null)
			this.set${JavaName(f)}_${JavaName(f.xrefField)}(entity.get${settertype(f.xrefField)}("${entity.name}_${f.name}"));			
		else if( entity.get("${entity.name?lower_case}_${f.name?lower_case}") != null)
			this.set${JavaName(f)}_${JavaName(f.xrefField)}(entity.get${settertype(f.xrefField)}("${entity.name?lower_case}_${f.name?lower_case}"));
			
		if( entity.get("${entity.name}.${f.name}") != null) 
			this.set${JavaName(f)}((${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)})entity.get("${entity.name}.${f.name}_${f.xrefField.name}"));
		else if( entity.get("${entity.name?lower_case}.${f.name?lower_case}") != null) 
			this.set${JavaName(f)}((${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)})entity.get("${entity.name?lower_case}.${f.name?lower_case}_${f.xrefField.name?lower_case}"));	
		//set label for field ${JavaName(f)}
		<#if f.xrefLabelNames[0] != f.xrefFieldName><#list f.xrefLabelNames as label>
		if(entity.get("${f.name}_${label}") != null) this.set${JavaName(f)}_${JavaName(label)}(entity.get${settertype(f.xrefLabels[label_index])}("${f.name}_${label}"));
		else if(entity.get("${f.name?lower_case}_${label?lower_case}") != null) this.set${JavaName(f)}_${JavaName(label)}(entity.get${settertype(f.xrefLabels[label_index])}("${f.name?lower_case}_${label?lower_case}"));
		else if(strict) this.set${JavaName(f)}_${JavaName(label)}(entity.get${settertype(f.xrefLabels[label_index])}("${f.name}_${label}")); // setting null is not an option due to function overloading
		if( entity.get("${entity.name}_${f.name}_${label}") != null ) this.set${JavaName(f)}_${JavaName(label)}(entity.get${settertype(f.xrefLabels[label_index])}("${entity.name}_${f.name}_${label}"));		
		else if( entity.get("${entity.name?lower_case}_${f.name?lower_case}_${label?lower_case}") != null ) this.set${JavaName(f)}_${JavaName(label)}(entity.get${settertype(f.xrefLabels[label_index])}("${entity.name?lower_case}_${f.name?lower_case}_${label?lower_case}"));
		</#list></#if>
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
