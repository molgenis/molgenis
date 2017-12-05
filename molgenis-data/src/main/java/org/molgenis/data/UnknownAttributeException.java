package org.molgenis.data;

import org.molgenis.data.meta.model.EntityType;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnknownAttributeException extends UnknownDataException
{
	private static final String ERROR_CODE = "D04";
	private final transient EntityType entityType;
	private final String attributeName;

	public UnknownAttributeException(EntityType entityType, String attributeName)
	{
		super(ERROR_CODE);
		this.entityType = requireNonNull(entityType);
		this.attributeName = requireNonNull(attributeName);
	}

	@Override
	public String getMessage()
	{
		return "type:" + entityType.getId() + " attribute:" + attributeName;
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { entityType, attributeName };
	}
}
