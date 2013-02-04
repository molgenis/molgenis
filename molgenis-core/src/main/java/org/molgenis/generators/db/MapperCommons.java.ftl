	public java.util.List<${entity.namespace}.${JavaName(entity)}> createList(int size)
	{
<#if !entity.abstract>
		return new java.util.ArrayList<${entity.namespace}.${JavaName(entity)}>(size); 
<#else>
		return null;
</#if>
	}			

	public ${entity.namespace}.${JavaName(entity)} create()
	{
<#if !entity.abstract>	
		return new ${entity.namespace}.${JavaName(entity)}();
<#else>
		return null; //abstract type, cannot be instantiated
</#if>
	}

	@Override
	//Resolve
	public void resolveForeignKeys(java.util.List<${entity.namespace}.${JavaName(entity)}> entities)  throws org.molgenis.framework.db.DatabaseException, java.text.ParseException
	{
<#assign has_xrefs=false>	
<#list allFields(entity) as f>
  	<#if (f.type == 'xref' || f.type == 'mref') &&   f.xrefLabelNames[0] != f.xrefFieldName>
  		<#assign has_xrefs=true>	
		//create foreign key map for field '${name(f)}' to ${name(f.xrefEntity)}.${name(f.xrefField)} using ${csv(f.xrefLabelNames)})	
		//we will use a hash of the values to ensure that entities are only queried once	
		final java.util.Map<String, org.molgenis.framework.db.QueryRule> ${name(f)}Rules = new java.util.LinkedHashMap<String, org.molgenis.framework.db.QueryRule>();
	</#if>
</#list>	
<#if has_xrefs>		
		//create all query rules	
		for(${entity.namespace}.${JavaName(entity)} object: entities)
		{
<#list allFields(entity) as f>
	<#if (f.type == 'xref' || f.type == 'mref') && f.xrefLabelNames[0] != f.xrefFieldName>
		<#if f.xrefLabelNames?size &gt; 1>
			//create xref/mref rule filtering ${f.xrefEntityName} on the combination of labels ${csv(f.xrefLabelNames)} if xref(id)==null and xref(name) exists
			<#if f.type == 'xref'>
			if(object.get${JavaName(f)}_${JavaName(f.xrefField)}() == null && object.get${JavaName(f)}_${JavaName(f.xrefLabelNames[0])}() != null)
			<#else>
			if(object.get${JavaName(f)}_${JavaName(f.xrefField)}().size() == 0 && object.get${JavaName(f)}_${JavaName(f.xrefLabelNames[0])}().size() > 0)
			</#if>
			{
				java.util.List<org.molgenis.framework.db.QueryRule> rules = new java.util.ArrayList<org.molgenis.framework.db.QueryRule>();
				String key = "";

				<#if f.type == 'xref'>
				Object label = object.get${JavaName(f)}_${JavaName(f.xrefLabelNames[0])}();
				<#else>
				for(String label: object.get${JavaName(f)}_${JavaName(f.xrefLabelNames[0])}())
				</#if>
				{
					<#list f.xrefLabelNames as label>
					rules.add(new org.molgenis.framework.db.QueryRule("${label}", org.molgenis.framework.db.QueryRule.Operator.EQUALS, label));	
					key += 	label;
					</#list>			
					org.molgenis.framework.db.QueryRule complexRule = new org.molgenis.framework.db.QueryRule(rules);
					if(!${name(f)}Rules.containsKey(key))
					{
						${name(f)}Rules.put(key, complexRule);
						${name(f)}Rules.put(key+"_OR_", new org.molgenis.framework.db.QueryRule(org.molgenis.framework.db.QueryRule.Operator.OR));
					}
				}
			}
		<#else>
			//create xref/mref rule filtering ${f.xrefEntityName} on the label ${csv(f.xrefLabelNames)}
			<#if f.type == 'xref'>
			if(object.get${JavaName(f)}_${JavaName(f.xrefField)}() == null && object.get${JavaName(f)}_${JavaName(f.xrefLabelNames[0])}() != null)
			<#else>
			if(object.get${JavaName(f)}_${JavaName(f.xrefField)}().size() == 0 && object.get${JavaName(f)}_${JavaName(f.xrefLabelNames[0])}().size() > 0)
			</#if>
			{
				<#if f.type == 'xref'>
				Object label = object.get${JavaName(f)}_${JavaName(f.xrefLabelNames[0])}();
				<#else>
				for(String label: object.get${JavaName(f)}_${JavaName(f.xrefLabelNames[0])}())
				</#if>
				{
					org.molgenis.framework.db.QueryRule xrefFilter = new org.molgenis.framework.db.QueryRule("${f.xrefLabelNames[0]}", org.molgenis.framework.db.QueryRule.Operator.EQUALS, label);
					
					if(label != null && !${name(f)}Rules.containsKey(label))
					{
						${name(f)}Rules.put(""+label, xrefFilter);
						${name(f)}Rules.put(""+label+"_OR_", new org.molgenis.framework.db.QueryRule(org.molgenis.framework.db.QueryRule.Operator.OR));
					}
				}
			}		
		</#if>	
	</#if>
</#list>
		}

<#list allFields(entity) as f>
<#if (f.type == 'xref' || f.type == 'mref') && f.xrefLabelNames[0] != f.xrefFieldName>
		//resolve foreign key field '${name(f)}' to ${name(f.xrefEntity)}.${name(f.xrefField)} using ${csv(f.xrefLabelNames)})
		final java.util.Map<String,${JavaType(f.xrefField)}> ${name(f)}_Labels_to_IdMap = new java.util.TreeMap<String,${JavaType(f.xrefField)}>();
		if(${name(f)}Rules.size() > 0)
		{		
		
			java.util.List<${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}> ${name(f)}List = null;
			try
			{
				${name(f)}List = getDatabase().find(${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)}.class, ${name(f)}Rules.values().toArray(new org.molgenis.framework.db.QueryRule[${name(f)}Rules.values().size()]));
			}
			catch(Exception e)
			{
				// something went wrong while querying for this entities' name field
				// we assume it has no such field, which should have been checked earlier ofcourse
				// regardless, just quit the function now
				throw new org.molgenis.framework.db.DatabaseException(e);
			}
		
			for(${f.xrefEntity.namespace}.${JavaName(f.xrefEntity)} xref :  ${name(f)}List)
			{
				String key = "";
				<#list f.xrefLabelNames as label>
				key += 	xref.get${JavaName(label)}();
				</#list>	
				
				${name(f)}_Labels_to_IdMap.put(key, xref.get${JavaName(f.xrefField)}());
			}
		}
</#if>
</#list>

		//update objects with the keys
		for(int i = 0; i < entities.size(); i++)
		{
			${entity.namespace}.${JavaName(entity)} object = entities.get(i);		
			<#list allFields(entity) as f>
			<#if (f.type == 'xref'  || f.type == 'mref') && f.xrefLabelNames[0] != f.xrefFieldName && f.xrefLabelNames[0]?exists>
			//update object using label fields ${csv(f.xrefLabelNames)}
			if(object.get${JavaName(f)}_${JavaName(f.xrefField)}() == null <#if f.type == 'mref'>|| object.get${JavaName(f)}_${JavaName(f.xrefField)}().size() == 0</#if>)
			{
				<#if f.type == 'mref'>
				java.util.List<Integer> idList = new java.util.ArrayList<Integer>();
				for(int j = 0; j < object.get${JavaName(f)}_${JavaName(f.xrefLabelNames[0])}().size(); j++)
				{
				</#if>
					String key = "";
					<#list f.xrefLabelNames as label>
					if(object.get${JavaName(f)}_${JavaName(label)}()<#if f.type=='mref'>.get(j)</#if> != null)
						key += 	object.get${JavaName(f)}_${JavaName(label)}()<#if f.type=='mref'>.get(j)</#if>;
					</#list>
					
					if(!"".equals(key) && ${name(f)}_Labels_to_IdMap.get(key) == null) 
					{
						throw new org.molgenis.framework.db.DatabaseException("<#list f.xrefLabelNames as label>${f.name}_${label}<#if label_has_next>,</#if></#list> cannot be resolved: unknown xref='"+key+"'");
					}
					else
					{
						<#if f.type == 'mref'>
						idList.add(${name(f)}_Labels_to_IdMap.get(key));
						<#else>
						object.set${JavaName(f)}_${JavaName(f.xrefField)}(${name(f)}_Labels_to_IdMap.get(key));
						</#if>
					}
				<#if f.type == 'mref'>
				}
				object.set${JavaName(f)}_${JavaName(f.xrefField)}(idList);
				</#if>
			}
			</#if>
			</#list>	
						
		}
</#if>		
	}	
	
	@Override
	public org.molgenis.fieldtypes.FieldType getFieldType(String fieldName)
	{
		<#list viewFields(entity) as f>
			if("${name(f)}".equalsIgnoreCase(fieldName) || "${name(f.entity)}.${name(f)}".equalsIgnoreCase(fieldName)) 
				return new org.molgenis.fieldtypes.${JavaName(f.type.toString())}Field();
		</#list>
		return new org.molgenis.fieldtypes.UnknownField();
	}		
	