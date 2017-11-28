package org.molgenis.data.importer.wizard.exception;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class NodePathException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "O01";
	private final String ontologyTerm;

	public NodePathException(String ontologyTerm)
	{
		super(ERROR_CODE);
		this.ontologyTerm = requireNonNull(ontologyTerm);
	}

	public String getOntologyTerm()
	{
		return ontologyTerm;
	}

	@Override
	public String getMessage()
	{
		return String.format("ontologyTerm:%s", ontologyTerm);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { ontologyTerm };
	}
}
