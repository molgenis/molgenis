package org.molgenis.omx.biobankconnect.wizard;

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
public class OntologyMatcherPage extends AbstractWizardPage
{
	private static final long serialVersionUID = 1L;

	@Autowired
	private OntologyMatcher ontologyMatcher;

	@Override
	public String getTitle()
	{
		return "Select catalogues";
	}

	@Override
	public String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard)
	{
		if (wizard instanceof BiobankConnectWizard)
		{
			BiobankConnectWizard biobankConnectWizard = (BiobankConnectWizard) wizard;
			Integer selectedDataSetId = ((BiobankConnectWizard) wizard).getSelectedDataSet().getId();
			List<Integer> selectedTargetDataSetIds = new ArrayList<Integer>();

			for (String id : request.getParameter("selectedTargetDataSets").split(","))
			{
				selectedTargetDataSetIds.add(Integer.parseInt(id));
			}
			biobankConnectWizard.setSelectedBiobanks(selectedTargetDataSetIds);

			try
			{
				ontologyMatcher.match(biobankConnectWizard.getUserName(), selectedDataSetId, selectedTargetDataSetIds,
						null);

			}
			catch (Exception e)
			{
				throw new RuntimeException("Error occurs when matching dataset " + selectedDataSetId
						+ " with datasets " + selectedTargetDataSetIds);
			}
		}
		else
		{
			throw new RuntimeException("The generic wizard has gone wrong");
		}

		return null;
	}
}