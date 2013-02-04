
	public void prepareFileAttachements(${JavaName(entity)} entity)
	{
<#list updateFields(entity) as field>
	<#if field.type.toString() == "file">
		//set a dummy for the file if it was attached (to evade not null exceptions)
		if(entity.get${JavaName(field)}AttachedFile() != null)
		{
			entity.set${JavaName(field)}("dummy");
		}
</#if>
</#list>
	}

	public void saveFileAttachements(${JavaName(entity)} entity, File baseDir) throws IOException
	{
<#list updateFields(entity) as field>
<#if field.type.toString() == "file">
		//store a file attachement
		if(entity.get${JavaName(field)}AttachedFile() != null)
		{
			String filename = entity.get${JavaName(field)}AttachedFile().toString();
			String extension = filename.substring(filename.lastIndexOf('.'));
			filename = "${JavaName(entiy)}/${JavaName(field)}"+<#list keyFields(entity) as f>entity.get${JavaName(f)}()<#if f_has_next>+"_"+</#if></#list>+extension;	
			entity.set${JavaName(field)}(filename);
	
			FileUtils.copyFile( entity.get${JavaName(field)}AttachedFile(), new File( baseDir.toString()+"/"+ entity.get${JavaName(field)}() ) );
		}
</#if>
</#list>
	}	

	public List<${JavaName(entity)}> createList(int size)
	{
<#if !entity.abstract>
		return new ${JavaName(entity)}List(size); 
<#else>
		return null;
</#if>
	}		
		
		
	public void mapMrefs( Connection connection, List<${JavaName(entity)}> entities ) throws SQLException, IOException		
	{
<#list entity.getAllFields() as f><#if f.type.toString() == "mref" >
<#assign mref_target = JavaName(f.getXRefEntity())/>
<#assign mref_entity = JavaName(entity)+"_"+JavaName(f.getXRefEntity())/>
<#assign mref_from_field = "From"+JavaName(f.getXRefEntity())+JavaName(f.getXRefField())/>
<#assign mref_to_field = "To"+JavaName(entity)+JavaName(keyFields(entity)?first)/>	
		//${f}	
		{
			JDBCMapper<${mref_entity}> mapper = new ${mref_entity}Mapper();		
			for (int i = 0; i < entities.size(); i++)
			{
				${JavaName(entity)} entity = entities.get(i);
			
				//retrieve currently known mrefs
				QueryRule rule = new QueryRule( "${name(mref_from_field)}", QueryRule.Operator.EQUALS, entity.get${JavaName(pkey(entity))}() );
				List<${mref_entity}> existing_mrefs = mapper.find( connection, rule );		
				//assign ids
				List<Integer> ids = new ArrayList<Integer>();
				for(${mref_entity} ref: existing_mrefs)
				{
					ids.add(ref.get${mref_to_field}());
				}	
				entity.set${JavaName(f)}(ids);				
			}
		}
</#if></#list>
	}		
			
	public void storeMrefs( Connection connection, List<${JavaName(entity)}> entities ) throws SQLException, IOException
	{
<#list entity.getAllFields() as f><#if f.type.toString() == "mref" >
<#assign mref_target = JavaName(f.getXRefEntity())/>
<#assign mref_entity = JavaName(entity)+"_"+JavaName(f.getXRefEntity())/>
<#assign mref_from_field = "From"+JavaName(f.getXRefEntity())+JavaName(f.getXRefField())/>
<#assign mref_to_field = "To"+JavaName(entity)+JavaName(keyFields(entity)?first)/>		
		{
			// what mrefs to add/delete
			List<${mref_entity}> toDelete = new ArrayList<${mref_entity}>();
			List<${mref_entity}> toAdd = new ArrayList<${mref_entity}>();
			// mapper to use
			JDBCMapper<${mref_entity}> mapper = new ${mref_entity}Mapper();

			for (${JavaName(entity)} entity : entities)
			{
				//retrieve currently known mrefs
				QueryRule rule = new QueryRule( "${name(mref_from_field)}", QueryRule.Operator.EQUALS, entity.get${JavaName(pkey(entity))}() );
				List<${mref_entity}> existing_mrefs = mapper.find( connection, rule );

				// check for removals
				List<${mref_entity}> new_mrefs = new ArrayList<${mref_entity}>();
				for (${mref_entity} ref : existing_mrefs)
				{
					if (!entity.get${JavaName(f)}().contains( ref.get${mref_to_field}() ))
					{
						toDelete.add( ref );
					}
					new_mrefs.add( ref );
				}

				// check for additions
				for (Integer ref : entity.get${JavaName(f)}())
				{
					${mref_entity} new_mref = new ${mref_entity}();
					new_mref.set${mref_from_field}( entity.get${JavaName(pkey(entity))}() );
					new_mref.set${mref_to_field}( ref );
					if (!existing_mrefs.contains( new_mref ))
					{
						toAdd.add( new_mref );
					}
				}
			}

			// execute
			mapper.add( connection, null, toAdd );
			mapper.remove( connection, toDelete );
		}
</#if></#list>
	}	
	
	public void removeMrefs( Connection connection, List<${JavaName(entity)}> entities ) throws SQLException, IOException
	{
<#list entity.getAllFields() as f><#if f.type.toString() == "mref" >
<#assign mref_target = JavaName(f.getXRefEntity())/>
<#assign mref_entity = JavaName(entity)+"_"+JavaName(f.getXRefEntity())/>
<#assign mref_from_field = "From"+JavaName(f.getXRefEntity())+JavaName(f.getXRefField())/>
<#assign mref_to_field = "To"+JavaName(entity)+JavaName(keyFields(entity)?first)/>		
		{
			// what mrefs to delete
			List<${mref_entity}> toDelete = new ArrayList<${mref_entity}>();
			// mapper to use
			JDBCMapper<${mref_entity}> mapper = new ${mref_entity}Mapper();

			for (${JavaName(entity)} entity : entities)
			{
				//retrieve currently known mrefs
				QueryRule rule = new QueryRule( "${name(mref_from_field)}", QueryRule.Operator.EQUALS, entity.get${JavaName(pkey(entity))}() );
				List<${mref_entity}> existing_mrefs = mapper.find( connection, rule );
				toDelete.addAll(existing_mrefs);
			}

			// execute
			mapper.remove( connection, toDelete );
		}
</#if></#list>
	}					

	public void setAutogeneratedKey(int i, ${JavaName(entity)} entity)
	{
<#list keyFields(entity) as field><#if field.auto && field.type.toString() == "int">
		entity.set${JavaName(field)}(i);
</#if></#list>
	}		

	public ${JavaName(entity)} create()
	{
<#if !entity.abstract>	
		return new ${JavaName(entity)}();
<#else>
		return null; //abstract type, cannot be instantiated
</#if>
	}

/*lookup caching*/	
<#list entity.getAllFields() as f>	
	<#if f.type.toString() == "xref" || f.type.toString() == "mref" >	
	private List<ValueLabel> ${name(f)}_options = new ArrayList<ValueLabel>();
	</#if>
</#list>	
	
	public void cacheOptions( Connection connection )
	{
<#list entity.getAllFields() as f>	
	<#if f.type.toString() == "xref" || f.type.toString() == "mref" >
			//cache ${f.name}_options
			{
		<#if f.hasFilter()>
				QueryRule rule = new QueryRule("${f.getFilterfield()}", QueryRule.Operator.${f.getFiltertype()}, ${f.getFiltervalue()});
				List<${JavaName(f.XRefEntity)}> result = new ${JavaName(f.XRefEntity)}Mapper().find(connection,rule);
		<#else>
				List<${JavaName(f.XRefEntity)}> result = new ${JavaName(f.XRefEntity)}Mapper().find(connection);
		</#if>		
				${name(f)}_options = new ArrayList<ValueLabel>();
				for(${JavaName(f.getXRefEntity())} e: result)
				{
					String label = String.format("${f.getXRefLabel().getFormat()}"<#list  f.getXRefLabel().getFields() as xref_label_field>	, e.get${JavaName(xref_label_field)}()</#list>);			
					${name(f)}_options.add( new ValueLabel(e.get${JavaName(f.getXRefField())}() , label) );
				}		
			}
	</#if>
</#list>	
	}
	
	public ${JavaName(entity)} setOptions( ${JavaName(entity)} entity, boolean readonly )
	{	
		//TODO refactor to work on a list of entities (to batch the queries to 1)
		
		if(!readonly)
		//use cached lookups
		{
<#list entity.getAllFields() as f>	
	<#if f.type.toString() == "xref" || f.type.toString() == "mref" >
			entity.set${JavaName(f)}Options(${name(f)}_options);
	</#if>
</#list>
		}
		//the selected xrefOption is already loaded on find (via a join)
		return entity;
	}	

