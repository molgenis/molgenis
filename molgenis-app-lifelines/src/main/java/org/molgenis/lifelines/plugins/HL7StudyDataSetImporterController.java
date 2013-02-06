package org.molgenis.lifelines.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.lifelines.hl7.HL7StudyDataSetImporter;
import org.molgenis.util.Entity;

public class HL7StudyDataSetImporterController extends PluginModel<Entity>
{
	private static final long serialVersionUID = 1L;

	public HL7StudyDataSetImporterController(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/org/molgenis/lifelines/plugins/HL7StudyDataSetImporter.ftl";
	}

	@Override
	public String getViewName()
	{
		return "HL7StudyDataSetImporter";
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		StringBuilder s = new StringBuilder();
		s.append("<link rel=\"stylesheet\" href=\"css/bootstrap-fileupload.min.css\" type=\"text/css\" />");
		s.append("<script type=\"text/javascript\" src=\"js/bootstrap-fileupload.min.js\"></script>");
		return s.toString();
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request) throws DatabaseException, IOException
	{
		if (request.getAction().equals("doImport")) doImport(db, request);
		else
			throw new RuntimeException("unsupported action: " + request.getAction());
	}

	private void doImport(Database db, MolgenisRequest request) throws DatabaseException, IOException
	{
		File xmlFile = request.getFile("file");
		FileInputStream fis = new FileInputStream(xmlFile);
		try
		{
			new HL7StudyDataSetImporter(db).importData(fis);
			getMessages().add(new ScreenMessage("Succesfully imported file", true));
		}
		catch (DatabaseException e)
		{
			getMessages().add(new ScreenMessage("Failed to import file", false));
		}
		finally
		{
			fis.close();
		}
	}

	@Override
	public void reload(Database db)
	{
		// noop
	}
}
