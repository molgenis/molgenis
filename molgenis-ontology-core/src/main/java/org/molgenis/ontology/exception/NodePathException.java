package org.molgenis.data.importer.wizard.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class NodePathException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "O01";
	private String ontologyTerm;

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
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, ontologyTerm);
		}).orElseGet(super::getLocalizedMessage);
	}
}
