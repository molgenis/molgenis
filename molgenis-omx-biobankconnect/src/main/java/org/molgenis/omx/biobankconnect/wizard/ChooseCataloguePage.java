package org.molgenis.omx.biobankconnect.wizard;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.framework.db.Database;
import org.molgenis.omx.biobankconnect.ontologyannotator.OntologyAnnotator;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class ChooseCataloguePage extends AbstractWizardPage
{
	private static final long serialVersionUID = 1L;
	private final Database database;

	@Autowired
	private OntologyAnnotator ontologyAnnotator;

	@Autowired
	public ChooseCataloguePage(Database database)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.database = database;
	}

	@Override
	public String getTitle()
	{
		return "Choose desired items";
	}

	@Override
	public String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard)
	{

		if (request.getParameter("selectedDataSetId") != null)
		{
			BiobankConnectWizard biobankConnectWizard = (BiobankConnectWizard) wizard;
			Integer selectedDataSetId = Integer.parseInt(request.getParameter("selectedDataSetId"));
			try
			{
				biobankConnectWizard.setSelectedDataSet(database.findById(DataSet.class, selectedDataSetId));
			}
			catch (Exception e)
			{
				new RuntimeException("Could not find dataset based on id : " + selectedDataSetId
						+ "  from the database");
			}
		}
		return null;
	}
}