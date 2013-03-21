package org.molgenis.ngs.plugins.worksheet;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
//import org.molgenis.framework.ui.html.TupleTable;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.Tuple;

import app.JpaDatabase;

public class Worksheet extends PluginModel<Entity>
{
	private static final long serialVersionUID = 1L;

	List<Tuple> currentRows = new ArrayList<Tuple>();

	public Worksheet(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		return "<link rel=\"stylesheet\" href=\"js/jquery-plugins/jquery.dataTables.css\" type=\"text/css\"/><link rel=\"stylesheet\" href=\"js/jquery-plugins/jquery.dataTables_demo_page.css\" type=\"text/css\"/><script type=\"text/javascript\" language=\"javascript\" src=\"js/jquery-plugins/jquery.dataTables.js\"></script>";
	}

	@Override
	public String getViewName()
	{
		return "plugins_worksheet_Worksheet";
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + Worksheet.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request) throws DatabaseException
	{
	}

	@Override
	public void reload(Database db)
	{
		// currentRows = new ArrayList<Tuple>();
		//
		// WritableTuple row = new KeyValueTuple();
		// row.set("project", "p1");
		// currentRows.add(row);

		try
		{
			this.getMessages().clear();
			// db.getMetaData().getEntity("Project").getFields().get(0).getName();
			currentRows = ((JpaDatabase) db).sql("select ProjectName from project", "ProjectName");
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.setError(e.getMessage());
		}

	}

	@Override
	public boolean isVisible()
	{
		return true;
	}

	public WorksheetModel getMyModel()
	{
		return new WorksheetModel(this.currentRows);
	}
}
