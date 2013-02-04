	public void set( org.molgenis.util.tuple.Tuple tuple, boolean strict ) throws Exception
	{
<#list allFields(entity) as f>
	<#assign type_label = f.getType().toString()>
	<#if f.type == "mref">
		//set ${JavaName(f)}
		if( tuple.get("${f.name}") != null || tuple.get("${entity.name}_${f.name}") != null || tuple.get("${f.name?lower_case}") != null || tuple.get("${entity.name?lower_case}_${f.name?lower_case}") != null) 
		{
			java.util.List<${type(f.xrefField)}> values = new java.util.ArrayList<${type(f.xrefField)}>();
			java.util.List<?> mrefs = tuple.getList("${f.name}");
			if(mrefs == null) mrefs = tuple.getList("${f.name?lower_case}");
			if(tuple.get("${entity.name?lower_case}_${f.name?lower_case}")!= null) mrefs = tuple.getList("${entity.name?lower_case}_${f.name?lower_case}");
			else if(tuple.get("${entity.name}_${f.name}")!= null) mrefs = tuple.getList("${entity.name}_${f.name}");
			if(mrefs != null) for(Object ref: mrefs)
			{
			<#if databaseImp = 'JPA'>
				if(ref instanceof String)
					values.add(${type(xrefField(model,f))}.parse${settertype(xrefField(model,f))}((String)ref));
				else if(ref instanceof org.molgenis.util.AbstractEntity) 	
					values.add((${type(xrefField(model,f))})((org.molgenis.util.AbstractEntity)ref).getIdValue() );
				else
					values.add((${type(xrefField(model,f))})ref);		
			<#else>
			  	<#if JavaType(f.xrefField) == "String" >
			  		values.add((${JavaType(f.xrefField)})ref);
			  	<#else>
			  		values.add(${type(f.xrefField)}.parse${settertype(f.xrefField)}((ref.toString())));
			  	</#if>
		  	</#if>
			}											
			this.set${JavaName(f)}_${JavaName(f.xrefField)}( values );
		}
	<#if f.xrefLabelNames[0] != f.xrefFieldName><#list f.xrefLabelNames as label>
		//set labels ${label} for mref field ${JavaName(f)}	
		if( tuple.get("${f.name}_${label}") != null || tuple.get("${entity.name}_${f.name}_${label}")!= null || tuple.get("${f.name?lower_case}_${label?lower_case}") != null || tuple.get("${entity.name?lower_case}_${f.name?lower_case}_${label?lower_case}") != null) 
		{
			java.util.List<${type(f.xrefLabels[label_index])}> values = new java.util.ArrayList<${type(f.xrefLabels[label_index])}>();
			java.util.List<?> mrefs = tuple.getList("${f.name}_${label}");
			if(mrefs == null) mrefs = tuple.getList("${f.name?lower_case}_${label?lower_case}");
			if(tuple.get("${entity.name}_${f.name}_${label}")!= null) mrefs = tuple.getList("${entity.name}_${f.name}_${label}");
			else if(tuple.get("${entity.name?lower_case}_${f.name?lower_case}_${label?lower_case}")!= null) mrefs = tuple.getList("${entity.name?lower_case}_${f.name?lower_case}_${label?lower_case}");
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
		if(tuple.get${settertype(f)}("${f.name}_${f.xrefField.name}") != null) this.set${JavaName(f)}(tuple.get${settertype(f)}("${f.name}_${f.xrefField.name}"));
		else if(tuple.get${settertype(f)}("${f.name?lower_case}_${f.xrefField.name?lower_case}") != null) this.set${JavaName(f)}(tuple.get${settertype(f)}("${f.name?lower_case}_${f.xrefField.name?lower_case}"));
		else if(strict) this.set${JavaName(f)}(tuple.get${settertype(f)}("${f.name}_${f.xrefField.name}")); // setting null is not an option due to function overloading
		
		if( tuple.get${settertype(f)}("${entity.name}_${f.name}_${f.xrefField.name}") != null) this.set${JavaName(f)}(tuple.get${settertype(f)}("${entity.name}_${f.name}_${f.xrefField.name}"));
		else if( tuple.get${settertype(f)}("${entity.name?lower_case}_${f.name?lower_case}_${f.xrefField.name?lower_case}") != null) this.set${JavaName(f)}(tuple.get${settertype(f)}("${entity.name?lower_case}_${f.name?lower_case}_${f.xrefField.name?lower_case}"));
		//alias of xref
		<#if databaseImp = 'JPA'>
		if( tuple.get("${f.name}") != null) { 
			if(org.molgenis.util.AbstractEntity.isObjectRepresentation(tuple.get("${f.name}").toString())) {
				${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)} instance = org.molgenis.util.AbstractEntity.setValuesFromString((String)tuple.get("${f.name}"), ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class);
				this.set${JavaName(f)}(instance);				
			} else {
				this.set${JavaName(f)}_${JavaName(f.xrefField)}(tuple.get${settertype(f.xrefField)}("investigation")); // FIXME hardcoded reference to investigation
			}
		}
		else if( tuple.get("${f.name?lower_case}") != null) { 
			if(org.molgenis.util.AbstractEntity.isObjectRepresentation(tuple.get("${f.name?lower_case}").toString())) {
				${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)} instance = org.molgenis.util.AbstractEntity.setValuesFromString((String)tuple.get("${f.name?lower_case}"), ${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class);
				this.set${JavaName(f)}(instance);				
			} else {
				this.set${JavaName(f)}_${JavaName(f.xrefField)}(tuple.get${settertype(f.xrefField)}("investigation")); // FIXME hardcoded reference to investigation
			}
		}
		if( tuple.get("${entity.name}_${f.name}") != null)
			this.set${JavaName(f)}_${JavaName(f.xrefField)}(tuple.get${settertype(f.xrefField)}("${entity.name}_${f.name}"));			
		else if( tuple.get("${entity.name?lower_case}_${f.name?lower_case}") != null)
			this.set${JavaName(f)}_${JavaName(f.xrefField)}(tuple.get${settertype(f.xrefField)}("${entity.name?lower_case}_${f.name?lower_case}"));
			
		if( tuple.get("${entity.name}.${f.name}") != null) 
			this.set${JavaName(f)}((${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)})tuple.get("${entity.name}.${f.name}_${f.xrefField.name}"));
		else if( tuple.get("${entity.name?lower_case}.${f.name?lower_case}") != null) 
			this.set${JavaName(f)}((${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)})tuple.get("${entity.name?lower_case}.${f.name?lower_case}_${f.xrefField.name?lower_case}"));	
		<#else>			
		if( tuple.get("${f.name}") != null) this.set${JavaName(f)}(tuple.get${settertype(f)}("${f.name}"));
		else if( tuple.get("${f.name?lower_case}") != null) this.set${JavaName(f)}(tuple.get${settertype(f)}("${f.name?lower_case}"));
		if( tuple.get("${entity.name}_${f.name}") != null) this.set${JavaName(f)}(tuple.get${settertype(f)}("${entity.name}_${f.name}"));
		else if( tuple.get("${entity.name?lower_case}_${f.name?lower_case}") != null) this.set${JavaName(f)}(tuple.get${settertype(f)}("${entity.name?lower_case}_${f.name?lower_case}"));
		</#if>
		//set label for field ${JavaName(f)}
		<#if f.xrefLabelNames[0] != f.xrefFieldName><#list f.xrefLabelNames as label>
		if(tuple.get("${f.name}_${label}") != null) this.set${JavaName(f)}_${JavaName(label)}(tuple.get${settertype(f.xrefLabels[label_index])}("${f.name}_${label}"));
		else if(tuple.get("${f.name?lower_case}_${label?lower_case}") != null) this.set${JavaName(f)}_${JavaName(label)}(tuple.get${settertype(f.xrefLabels[label_index])}("${f.name?lower_case}_${label?lower_case}"));
		else if(strict) this.set${JavaName(f)}_${JavaName(label)}(tuple.get${settertype(f.xrefLabels[label_index])}("${f.name}_${label}")); // setting null is not an option due to function overloading
		if( tuple.get("${entity.name}_${f.name}_${label}") != null ) this.set${JavaName(f)}_${JavaName(label)}(tuple.get${settertype(f.xrefLabels[label_index])}("${entity.name}_${f.name}_${label}"));		
		else if( tuple.get("${entity.name?lower_case}_${f.name?lower_case}_${label?lower_case}") != null ) this.set${JavaName(f)}_${JavaName(label)}(tuple.get${settertype(f.xrefLabels[label_index])}("${entity.name?lower_case}_${f.name?lower_case}_${label?lower_case}"));
		</#list></#if>
		<#else>
		if(tuple.get${settertype(f)}("${f.name?lower_case}") != null) this.set${JavaName(f)}(tuple.get${settertype(f)}("${f.name?lower_case}"));
		else if(tuple.get${settertype(f)}("${f.name}") != null) this.set${JavaName(f)}(tuple.get${settertype(f)}("${f.name}"));
		else if(strict) this.set${JavaName(f)}(tuple.get${settertype(f)}("${f.name?lower_case}")); // setting null is not an option due to function overloading
		if( tuple.get${settertype(f)}("${entity.name?lower_case}_${f.name?lower_case}") != null) this.set${JavaName(f)}(tuple.get${settertype(f)}("${entity.name?lower_case}_${f.name?lower_case}"));
		else if( tuple.get${settertype(f)}("${entity.name}_${f.name}") != null) this.set${JavaName(f)}(tuple.get${settertype(f)}("${entity.name}_${f.name}"));
		</#if>
		<#if f.type == "file" || f.type=="image">
		if(tuple.getString("filefor_${f.name}") != null)
			this.set${JavaName(f)}AttachedFile(new java.io.File(tuple.getString("filefor_${f.name}")));
		else if(tuple.getString("filefor_${f.name?lower_case}") != null)
			this.set${JavaName(f)}AttachedFile(new java.io.File(tuple.getString("filefor_${f.name?lower_case}")));
		if(tuple.getString("filefor_${entity.name}_${f.name}") != null) this.set${JavaName(f)}AttachedFile(new java.io.File(tuple.getString("filefor_${entity.name}_${f.name}"))); //FIXME filefor hack
		else if(tuple.getString("filefor_${entity.name?lower_case}_${f.name?lower_case}") != null) this.set${JavaName(f)}AttachedFile(new java.io.File(tuple.getString("filefor_${entity.name?lower_case}_${f.name?lower_case}"))); //FIXME filefor hack
		</#if>						
	</#if>
</#list>
	}
