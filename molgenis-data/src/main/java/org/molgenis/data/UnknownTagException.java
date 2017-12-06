package org.molgenis.data;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnknownTagException extends UnknownDataException
{
	private static final String ERROR_CODE = "D10";

	private final transient Object tagId;

	public UnknownTagException(Object tagId)
	{
		super(ERROR_CODE);
		this.tagId = requireNonNull(tagId);
	}

	@Override
	public String getMessage()
	{
		return String.format("id:%s", tagId.toString());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { tagId };
	}
}

