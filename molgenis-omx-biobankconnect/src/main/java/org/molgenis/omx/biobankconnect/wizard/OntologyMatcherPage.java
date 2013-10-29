package org.molgenis.omx.biobankconnect.wizard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.omx.biobankconnect.ontologymatcher.OntologyMatcher;
import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class OntologyMatcherPage extends AbstractWizardPage implements Serializable
{
	@Autowired
	private OntologyMatcher ontologyMatcher;

	private static final long serialVersionUID = 1L;

	@Override
	public String getTitle()
	{
		return "Select catalogues";
	}

	@Override
	public String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard)
	{
		try
		{
			BiobankConnectWizard biobankConnectWizard = (BiobankConnectWizard) wizard;
			List<Integer> selectedTargetDataSetIds = new ArrayList<Integer>();
			for (String id : request.getParameter("selectedTargetDataSets").split(","))
			{
				selectedTargetDataSetIds.add(Integer.parseInt(id));
			}
			biobankConnectWizard.setSelectedBiobanks(selectedTargetDataSetIds);
			ontologyMatcher.matchCatalogue(biobankConnectWizard.getSelectedDataSet().getId(), selectedTargetDataSetIds);

		}
		catch (Exception e)
		{
			new RuntimeException(e);
		}
		return null;
	}
}