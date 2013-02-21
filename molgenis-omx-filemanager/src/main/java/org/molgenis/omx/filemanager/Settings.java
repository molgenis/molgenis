package org.molgenis.omx.filemanager;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.omx.services.Report;
import org.molgenis.omx.services.StorageHandler;
import org.molgenis.util.Entity;

public class Settings<E extends Entity> extends PluginModel<E>
{
	private static final long serialVersionUID = 1L;

	private transient Report report;
	private transient StorageHandler sh;

	public Report getReport()
	{
		return report;
	}

	public Settings(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewName()
	{
		return "Settings";
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + Settings.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{
		if (request.getAction() != null)
		{

			try
			{
				if (request.getAction().equals("setFileDirPath"))
				{
					sh.setFileStorage(request.getString("fileDirPath"), db);
				}
				else if (request.getAction().equals("deleteFileDirPath"))
				{
					sh.deleteFileStorage(db);
				}
				else if (request.getAction().equals("validate"))
				{
					sh.validateFileStorage(db);
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
		sh = new StorageHandler(db);

		try
		{
			report = sh.getReport();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			this.setMessages(new ScreenMessage(e.getMessage() != null ? e.getMessage() : "null", false));
		}
	}

}
