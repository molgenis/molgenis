package org.molgenis.omx.importer;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.framework.db.Database;

public class WizardPage implements Serializable
{
	private static final long serialVersionUID = 1L;

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
