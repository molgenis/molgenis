package org.molgenis.omx.biobankconnect.wizard;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.data.DataService;
import org.molgenis.omx.biobankconnect.algorithm.ApplyAlgorithms;
import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class MappingManagerPage extends AbstractWizardPage
{
	private static final long serialVersionUID = 1L;

	@Autowired
	private CurrentUserStatus currentUserStatus;

	@Autowired
	private DataService dataService;

	@Autowired
	private ApplyAlgorithms algorithmGenerator;

	@Override
	public String getTitle()
	{
		return "Review candidate mappings";
	}

	@Override
	public String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard)
	{
		if (wizard instanceof BiobankConnectWizard)
		{
			BiobankConnectWizard biobankConnectWizard = (BiobankConnectWizard) wizard;
			List<Integer> sourceDataSetIds = biobankConnectWizard.getSelectedBiobanks();
			Integer targetDataSetId = biobankConnectWizard.getSelectedDataSet().getId();
			currentUserStatus.setUserIsRunning(biobankConnectWizard.getUserName(), true);
			algorithmGenerator.applyAlgorithm(biobankConnectWizard.getUserName(), targetDataSetId, sourceDataSetIds);
		}
		return null;
	}
}