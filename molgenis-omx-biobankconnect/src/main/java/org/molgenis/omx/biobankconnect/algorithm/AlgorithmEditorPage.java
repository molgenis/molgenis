package org.molgenis.omx.biobankconnect.algorithm;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.omx.biobankconnect.wizard.BiobankConnectWizard;
import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class AlgorithmEditorPage extends AbstractWizardPage
{

	@Autowired
	private AlgorithmGenerator algorithmGenerator;

	private static final long serialVersionUID = 1L;

	@Override
	public String getTitle()
	{
		return "Edit algorithm";
	}

	@Override
	public String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard)
	{
		if (wizard instanceof BiobankConnectWizard)
		{
			BiobankConnectWizard biobankConnectWizard = (BiobankConnectWizard) wizard;
			algorithmGenerator.applyAlgorithm(biobankConnectWizard.getUserName(), biobankConnectWizard
					.getSelectedDataSet().getId(), biobankConnectWizard.getSelectedBiobanks());
		}
		return null;
	}
}
