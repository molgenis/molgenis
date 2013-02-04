<#--
Common parts for saving files to an entity.
-->
	public void prepareFileAttachements(java.util.List<${entity.namespace}.${JavaName(entity)}> entities, java.io.File baseDir) throws java.io.IOException
	{
<#if hasFiles(entity)>		
		for(${entity.namespace}.${JavaName(entity)} entity: entities)
		{	
<#list updateFields(entity) as field>
	<#if field.type.toString() == "file" || field.type.toString() == "image">
			//set a dummy for the file if it was attached (to evade not null exceptions)
			if(entity.get${JavaName(field)}AttachedFile() != null)
			{
				entity.set${JavaName(field)}("dummy");
			}
</#if>
</#list>
		}
</#if>
	}

	public boolean saveFileAttachements(java.util.List<${entity.namespace}.${JavaName(entity)}> entities, java.io.File baseDir) throws java.io.IOException
	{
<#if hasFiles(entity)>		
		for(${entity.namespace}.${JavaName(entity)} entity: entities)
		{		
<#list updateFields(entity) as field>
<#if field.type.toString() == "file" || field.type.toString() == "image">
			//store a file attachement
			if(entity.get${JavaName(field)}AttachedFile() != null)
			{
				String filename = entity.get${JavaName(field)}AttachedFile().toString();
				String extension = filename.substring(filename.lastIndexOf('.'));
				filename = "${JavaName(entity)}/${JavaName(field)}"+<#list keyFields(entity) as f>entity.get${JavaName(f)}()<#if f_has_next>+"_"+</#if></#list>+extension;	
				entity.set${JavaName(field)}(filename);
		
				org.apache.commons.io.FileUtils.copyFile( entity.get${JavaName(field)}AttachedFile(), new java.io.File( baseDir.toString()+"/"+ entity.get${JavaName(field)}() ) );
			}
</#if>
</#list>
		}
		return true;
<#else>
		return false;
</#if>
	}