	public List<${Name(entity)}> createList(int size)
	{
<#if !entity.abstract>
		return new ${Name(entity)}List(size); 
<#else>
		return null;
</#if>
	}	