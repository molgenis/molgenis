package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.QueryRule;
import org.molgenis.i18n.CodedRuntimeException;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class UnsupportedQueryException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN04";
	private final List<QueryRule> rules;

	public UnsupportedQueryException(List<QueryRule> rules)
	{
		super(ERROR_CODE);
		this.rules = requireNonNull(rules);
	}

	@Override
	public String getMessage()
	{
		return String.format("rules:%s", rules);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{

		return new Object[] { rules.stream().map(Object::toString).collect(Collectors.joining(", ")) };
	}
}
