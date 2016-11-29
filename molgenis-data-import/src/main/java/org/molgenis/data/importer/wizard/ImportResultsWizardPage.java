package org.molgenis.data.importer.wizard;

import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;

@Component
public class ImportResultsWizardPage extends AbstractWizardPage
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getTitle()
	{
		return "Result";
	}

	@Override
	public String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard)
	{
		return null;
	}
}
