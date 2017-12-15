package org.molgenis.data.security.meta;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.i18n.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class EditSystemAttributeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "S02";
	private final String operation;
	private final transient Attribute attr;

	public EditSystemAttributeException(String operation, Attribute attr)
	{
		super(ERROR_CODE);
		this.operation = requireNonNull(operation);
		this.attr = requireNonNull(attr);
	}

	public String getOperation()
	{
		return operation;
	}

	public Attribute getAttr()
	{
		return attr;
	}

	@Override
	public String getMessage()
	{
		return String.format("operation:%s attr:%s", operation, attr.getName());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { operation, attr };
	}
}
