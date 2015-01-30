package org.molgenis.data.mapper;

import java.util.List;

public class MappingServiceRequest
{
	private final String targetEntityName;
	private final String sourceEntityName;
	private final String targetAttributeName;
	private final List<String> sourceAttributeNames;
	private final String algorithm;

	public MappingServiceRequest(String targetEntityIdentifier, String sourceEntityIdentifier,
			String targetAttributeIdentifier, List<String> sourceAttributeIdentifiers, String algorithm)
	{
		this.targetEntityName = targetEntityIdentifier;
		this.sourceEntityName = sourceEntityIdentifier;
		this.targetAttributeName = targetAttributeIdentifier;
		this.sourceAttributeNames = sourceAttributeIdentifiers;
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

	public List<String> getSourceAttributeNames()
	{
		return sourceAttributeNames;
	}

	public String getAlgorithm()
	{
		return algorithm;
	}
}
