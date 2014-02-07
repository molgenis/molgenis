package org.molgenis.omx.biobankconnect.algorithm;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.wizard.BiobankConnectWizard;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class AlgorithmEditorPage extends AbstractWizardPage
{
	@Autowired
	private DataService dataService;

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
			List<Integer> sourceDataSetIds = biobankConnectWizard.getSelectedBiobanks();
			Integer targetDataSetId = biobankConnectWizard.getSelectedDataSet().getId();
			algorithmGenerator.applyAlgorithm(biobankConnectWizard.getUserName(), targetDataSetId, sourceDataSetIds);
			Collections.sort(sourceDataSetIds);
			StringBuilder derivedDataSetIdentifier = new StringBuilder();
			derivedDataSetIdentifier.append(biobankConnectWizard.getUserName()).append('-').append(targetDataSetId)
					.append('-').append(StringUtils.join(sourceDataSetIds, '-')).append("-derived");
			DataSet derivedDataSet = dataService.findOne(DataSet.ENTITY_NAME,
					new QueryImpl().eq(DataSet.IDENTIFIER, derivedDataSetIdentifier.toString()), DataSet.class);
			biobankConnectWizard.setDerivedDataSet(derivedDataSet);
		}
		return null;
	}
}
