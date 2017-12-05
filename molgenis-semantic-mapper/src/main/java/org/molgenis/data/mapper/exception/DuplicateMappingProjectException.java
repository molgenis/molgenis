package org.molgenis.data.mapper.exception;

import static java.lang.String.format;

// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public class DuplicateMappingProjectException extends MappingServiceException
{
	private static final String ERROR_CODE = "M07";

	private final String mappingProjectId;

	public DuplicateMappingProjectException(String mappingProjectId)
	{
		super(ERROR_CODE);
		this.mappingProjectId = mappingProjectId;
	}

	public String getMappingProjectId()
	{
		return mappingProjectId;
	}

	@Override
	public String getMessage()
	{
		return format("id:%s", mappingProjectId);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { mappingProjectId };
	}
}
