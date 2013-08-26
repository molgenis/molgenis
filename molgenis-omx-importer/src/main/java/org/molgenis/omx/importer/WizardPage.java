package org.molgenis.omx.importer;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;

public class WizardPage
{
	protected final Logger logger = Logger.getLogger(getClass());

	private final String title;
	private ImportWizard wizard;

	public WizardPage(String title)
	{
		super();
		this.title = title;
	}

	protected ImportWizard getWizard()
	{
		return wizard;
	}

	protected void setWizard(ImportWizard wizard)
	{
		this.wizard = wizard;
	}

	public String getTitle()
	{
		return title;
	}

	public String getViewTemplate()
	{
		return this.getClass().getSimpleName() + ".ftl";
	}

	public void handleRequest(Database db, HttpServletRequest request)
	{

	}
}
