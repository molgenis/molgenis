<#--
Common parts for saving multiplicative references (mrefs) to an entity.
-->	
	/** 
	 * This method queries the link tables to load mref fields. For performance reasons this is done for the whole batch.
	 * As a consequence the number of queries equals the number of mref fields. This may be memory hungry.
	 */
	public void mapMrefs( List<${JavaName(entity)}> entities ) throws DatabaseException			
	{
<#if entity.getAllFieldsOf("mref")?size &gt; 0>	
		try
		{
			//list the ${name(entity)} ids to query
			List<${pkeyJavaType(entity)}> ${name(entity)}Ids = new ArrayList<${pkeyJavaType(entity)}>();
			for(${JavaName(entity)} entity: entities)
			{
				${name(entity)}Ids.add(entity.get${JavaName(pkey(entity))}());
			}
			
<#list entity.getAllFieldsOf("mref") as f>
<#assign mref_entity = f.mrefName>
<#assign mref_remote_field = f.mrefRemoteid/>
<#assign mref_local_field = f.mrefLocalid/>			
			//map the ${f.name} mrefs
			List<${JavaName(mref_entity)}> ${name(f)}_mrefs = this.getDatabase().query(${JavaName(mref_entity)}.class).in("${f.mrefLocalid}", ${name(entity)}Ids).sortASC("${pkey(model.getEntity(mref_entity)).name}").find();
			Map<${pkeyJavaType(entity)},List<${pkeyJavaType(f.xrefEntity)}>> ${name(f)}_${name(mref_remote_field)}_map = new LinkedHashMap<${pkeyJavaType(entity)},List<${pkeyJavaType(f.xrefEntity)}>>();
			<#if f.xrefLabelNames[0] != f.xrefFieldName><#list f.xrefLabelNames as label>
			Map<${pkeyJavaType(entity)},List<${JavaType(f.xrefLabels[label_index])}>> ${name(f)}_${label}_map = new LinkedHashMap<${pkeyJavaType(entity)},List<${JavaType(f.xrefLabels[label_index])}>>();
			</#list></#if>
			
			for(${JavaName(mref_entity)} ref: ${name(f)}_mrefs)
			{
				if(${name(f)}_${name(mref_remote_field)}_map.get(ref.get${JavaName(mref_local_field)}_${JavaName(f.xrefField)}()) == null) ${name(f)}_${name(mref_remote_field)}_map.put(ref.get${JavaName(mref_local_field)}_${JavaName(pkey(f.xrefEntity))}(),new ArrayList<${pkeyJavaType(f.xrefEntity)}>()); 
				${name(f)}_${name(mref_remote_field)}_map.get(ref.get${JavaName(mref_local_field)}_${JavaName(f.xrefField)}()).add(ref.get${JavaName(mref_remote_field)}_${JavaName(pkey(f.xrefEntity))}());
				<#if f.xrefLabelNames[0] != f.xrefFieldName><#list f.xrefLabelNames as label>
				if(${name(f)}_${label}_map.get(ref.get${JavaName(mref_local_field)}_${JavaName(f.xrefField)}()) == null)	${name(f)}_${label}_map.put(ref.get${JavaName(mref_local_field)}_${JavaName(pkey(f.xrefEntity))}(),new ArrayList<${JavaType(f.xrefLabels[label_index])}>());
				${name(f)}_${label}_map.get(ref.get${JavaName(mref_local_field)}_${JavaName(f.xrefField)}()).add(ref.get${JavaName(mref_remote_field)}_${JavaName(label)}());
				</#list></#if>
			}
</#list>
			
			//load the mapped data into the entities
			for(${JavaName(entity)} entity: entities)
			{
				${pkeyJavaType(entity)} id = entity.get${JavaName(pkey(entity))}();
<#list entity.getAllFieldsOf("mref") as f>
<#assign mref_entity = f.mrefName>
<#assign mref_remote_field = f.mrefRemoteid/>
<#assign mref_local_field = f.mrefLocalid/>	
				if(${name(f)}_${name(mref_remote_field)}_map.get(id) != null)
				{
					entity.set${JavaName(f)}_${JavaName(pkey(f.xrefEntity))}(${name(f)}_${name(mref_remote_field)}_map.get(id));
				}
				<#if f.xrefLabelNames[0] != f.xrefFieldName><#list f.xrefLabelNames as label>
				if(${name(f)}_${label}_map.get(id) != null)
				{
					entity.set${JavaName(f)}_${JavaName(label)}(${name(f)}_${label}_map.get(id));
				}
				</#list></#if>
</#list>			
			}
		} 
		catch(Exception e)
		{	
			throw new DatabaseException(e);
		}
</#if>
	}		
	
	/**
	 * This method updates the mref entity tables. It deletes existing and adds the new (this to ensure ordering).
	 */		
	public void storeMrefs( List<${JavaName(entity)}> entities ) throws DatabaseException, IOException, ParseException	
	{
<#if entity.getAllFieldsOf("mref")?size &gt; 0>		
		//create an List of ${JavaName(entity)} ids to query for
		List<${pkeyJavaType(entity)}> entityIds = new ArrayList<${pkeyJavaType(entity)}>(); 
		for (${JavaName(entity)} entity : entities) 
		{
			entityIds.add(entity.get${JavaName(pkey(entity))}());		
		}
		
<#list entity.getAllFields() as f><#if f.type.toString() == "mref" >	
<#assign mref_entity = f.mrefName>
<#assign mref_remote_field = f.mrefRemoteid/>
<#assign mref_local_field = f.mrefLocalid/>	
		//delete existing mrefs
		getDatabase().remove(getDatabase().query( ${JavaName(mref_entity)}.class).in("${mref_local_field}", entityIds).find());
		List<${JavaName(mref_entity)}> ${name(mref_entity)}ToAdd = new ArrayList<${JavaName(mref_entity)}>();

</#if></#list>	

		//check for each mref what needs to be added
		for(${JavaName(entity)} entity: entities)
		{
<#list entity.getAllFields() as f><#if f.type.toString() == "mref" >	
<#assign mref_entity = f.mrefName>
<#assign mref_remote_field = f.mrefRemoteid/>
<#assign mref_local_field = f.mrefLocalid/>		
			//remove duplicates using Set
			entity.set${JavaName(f)}_${JavaName(f.xrefField)}(new ArrayList(new LinkedHashSet(entity.get${JavaName(f)}_${JavaName(f.xrefField)}())));
			for(${pkeyJavaType(f.xrefEntity)} id: entity.get${JavaName(f)}_${JavaName(pkey(f.xrefEntity))}())
			{
				${JavaName(mref_entity)} new_mref = new ${JavaName(mref_entity)}();
				new_mref.set${JavaName(mref_local_field )}( entity.get${JavaName(pkey(entity))}() );
				new_mref.set${JavaName(mref_remote_field)}( id );
				${name(mref_entity)}ToAdd.add(new_mref);
			}
			
</#if></#list>
		}
		
<#list entity.getAllFields() as f><#if f.type.toString() == "mref" >	
<#assign mref_entity = f.mrefName>
<#assign mref_remote_field = f.mrefRemoteid/>
<#assign mref_local_field = f.mrefLocalid/>			
		//process changes to ${mref_entity}
		getDatabase().add( ${name(mref_entity)}ToAdd );
</#if></#list>
</#if>
	}
		
	
	public void removeMrefs( List<${JavaName(entity)}> entities ) throws DatabaseException, IOException, ParseException
	{
<#if entity.getAllFieldsOf("mref")?size &gt; 0>		
		//create an list of ${JavaName(entity)} ids to query for
		List<${pkeyJavaType(entity)}> entityIds = new ArrayList<${pkeyJavaType(entity)}>(); 
		for (${JavaName(entity)} entity : entities) 
		{
			entityIds.add(entity.get${JavaName(pkey(entity))}());		
		}	
	
<#list entity.getAllFields() as f><#if f.type.toString() == "mref" >	
<#assign mref_entity = f.mrefName>
<#assign mref_remote_field = f.mrefRemoteid/>
<#assign mref_local_field = f.mrefLocalid/>		
		//remove all ${mref_entity} elements for field entity.${f.name}
		getDatabase().remove( getDatabase().query( ${JavaName(mref_entity)}.class).in("${mref_local_field}", entityIds).find() );
</#if></#list>
</#if>
	}	