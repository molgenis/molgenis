package org.molgenis.semanticmapper.data.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class MappingServiceRequest
{
	@NotNull
	private final String targetEntityName;
	@NotNull
	private final String sourceEntityName;
	@NotNull
	private final String targetAttributeName;
	@NotNull
	private final String algorithm;
	@Min(0)
	private Long offset;
	@Min(1)
	@Max(1000)
	private Long num;

	public MappingServiceRequest(String targetEntityName, String sourceEntityName, String targetAttributeName,
			String algorithm)
	{
		this.targetEntityName = targetEntityName;
		this.sourceEntityName = sourceEntityName;
		this.targetAttributeName = targetAttributeName;
		this.algorithm = algorithm;
	}

	public String getTargetEntityName()
	{
		return targetEntityName;
	}

	public String getSourceEntityName()
	{
		return sourceEntityName;
	}

	public String getTargetAttributeName()
	{
		return targetAttributeName;
	}

	public String getAlgorithm()
	{
		return algorithm;
	}

	public Long getOffset()
	{
		return offset;
	}

	public void setOffset(Long offset)
	{
		this.offset = offset;
	}

	public Long getNum()
	{
		return num;
	}

	public void setNum(Long num)
	{
		this.num = num;
	}
}
