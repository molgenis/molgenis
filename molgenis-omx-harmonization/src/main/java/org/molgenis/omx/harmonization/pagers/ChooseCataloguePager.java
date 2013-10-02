package org.molgenis.omx.harmonization.pagers;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.framework.db.Database;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class ChooseCataloguePager extends AbstractWizardPage implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final Database database;

	@Autowired
	public ChooseCataloguePager(Database database)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.database = database;
	}

	@Override
	public String getTitle()
	{
		return "Choose desired catalogue";
	}

	@Override
	public String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard)
	{

		if (request.getParameter("selectedDataSetId") != null)
		{
			try
			{
				BiobankConnectWizard biobankConnectWizard = (BiobankConnectWizard) wizard;
				Integer selectedDataSetId = Integer.parseInt(request.getParameter("selectedDataSetId"));
				biobankConnectWizard.setSelectedDataSet(database.findById(DataSet.class, selectedDataSetId));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}
}