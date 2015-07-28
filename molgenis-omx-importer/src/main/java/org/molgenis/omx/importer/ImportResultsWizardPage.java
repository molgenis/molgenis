package org.molgenis.omx.importer;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

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
