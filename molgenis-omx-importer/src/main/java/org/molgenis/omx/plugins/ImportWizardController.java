package org.molgenis.omx.plugins;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.util.Entity;

/**
 * Import wizard controller
 */
public class ImportWizardController extends PluginModel<Entity>
{
	private static final long serialVersionUID = 6673296137881226020L;
	private ImportWizard importWizard;

	public ImportWizardController(String name, ScreenController<?> parent)
	{
		super(name, parent);
		importWizard = new ImportWizard();
	}

	public ImportWizard getWizard()
	{
		return importWizard;
	}

	@Override
	public String getViewName()
	{
		return ImportWizard.class.getSimpleName();
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + ImportWizard.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		StringBuilder s = new StringBuilder();
		s.append("<link rel=\"stylesheet\" href=\"css/bwizard.min.css\" type=\"text/css\" />");
		s.append("<script type=\"text/javascript\" src=\"js/bwizard.min.js\"></script>");
		return s.toString();
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{
		String action = request.getString("__action");
		if (action == null) return;

		importWizard.setErrorMessage(null);
		importWizard.setValidationMessage(null);
		importWizard.setSuccessMessage(null);

		if (action.equals("next"))
		{
			importWizard.getCurrentPage().handleRequest(db, request);

			if (importWizard.getErrorMessage() == null)
			{
				importWizard.next();
			}
		}
		else if (action.equals("previous"))
		{
			importWizard.previous();
		}
		else if (action.equals("finish") || action.equals("cancel"))
		{
			importWizard = new ImportWizard();
		}
	}

	@Override
	public void reload(Database db)
	{

	}

}
