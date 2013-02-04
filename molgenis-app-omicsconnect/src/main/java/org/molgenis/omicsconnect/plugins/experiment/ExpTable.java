/* Date:        February 2, 2010
 * Template:	PluginScreenJavaTemplateGen.java.ftl
 * generator:   org.molgenis.generators.ui.PluginScreenJavaTemplateGen 3.3.2-testing
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package org.molgenis.omicsconnect.plugins.experiment;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.omx.observ.target.Panel;
import org.molgenis.omx.organization.Study;
import org.molgenis.util.Entity;

/**
 * Shows table of experiment information for WormQTL
 */
public class ExpTable extends PluginModel<Entity>
{

	private static final long serialVersionUID = 1L;

	private ExpTableModel model = new ExpTableModel();

	public ExpTableModel getMyModel()
	{
		return model;
	}

	public ExpTable(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewName()
	{
		return "ExpTable";
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/org/molgenis/omicsconnect/plugins/experiment/ExpTable.ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{
		if (request.getString("__action") != null)
		{
			String action = request.getString("__action");
			try
			{
				if (action.equals("LoadStudies"))
				{

					// set studies get db object so you can query
					model.setStudies(db.find(Study.class));
					model.setPanels(null);
					// Get the ID of the chromosome.
				}

				if (action.equals("LoadPanels"))
				{
					// set studies get db object so you can query
					model.setPanels(db.find(Panel.class));
					model.setStudies(null);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				this.setMessages(new ScreenMessage(e.getMessage() != null ? e.getMessage() : "null", false));
			}
		}
	}

	@Override
	public void reload(Database db)
	{
		// set studies get db object so you can query
		try
		{
			model.setStudies(db.find(Study.class));
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
			this.setMessages(new ScreenMessage(e.getMessage() != null ? e.getMessage() : "null", false));
		}
	}
}
