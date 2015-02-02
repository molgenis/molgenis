package org.molgenis.vkgl.api;

import java.util.List;

public class VkglResponse
{
	private VkglResponseMetadata metadata;
	private List<VkglResult> results;
	
	
	public List<VkglResult> getResults()
	{
		return results;
	}
	public void setResults(List<VkglResult> results)
	{
		this.results = results;
	}
	public VkglResponseMetadata getMetadata()
	{
		return metadata;
	}
	public void setMetadata(VkglResponseMetadata metadata)
	{
		this.metadata = metadata;
	}

}
