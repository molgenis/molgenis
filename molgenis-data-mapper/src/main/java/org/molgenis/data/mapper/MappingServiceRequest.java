package org.molgenis.data.mapper;

import java.util.List;

public class MappingServiceRequest
{
	private final String targetEntityIdentifier;
	private final String sourceEntityIdentifier;
	private final String targetAttributeIdentifier;
	private final List<String> sourceAttributeIdentifiers;
	private final String algorithm;

	public MappingServiceRequest(String targetEntityIdentifier, String sourceEntityIdentifier,
			String targetAttributeIdentifier, List<String> sourceAttributeIdentifiers, String algorithm)
	{
		this.targetEntityIdentifier = targetEntityIdentifier;
		this.sourceEntityIdentifier = sourceEntityIdentifier;
		this.targetAttributeIdentifier = targetAttributeIdentifier;
		this.sourceAttributeIdentifiers = sourceAttributeIdentifiers;
		this.algorithm = algorithm;
	}

	public String getTargetEntityIdentifier()
	{
		return targetEntityIdentifier;
	}

	public String getSourceEntityIdentifier()
	{
		return sourceEntityIdentifier;
	}

	public String getTargetAttributeIdentifier()
	{
		return targetAttributeIdentifier;
	}

	public List<String> getSourceAttributeIdentifiers()
	{
		return sourceAttributeIdentifiers;
	}

	public String getAlgorithm()
	{
		return algorithm;
	}
}
