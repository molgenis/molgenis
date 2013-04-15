/* Date:        January 6, 2011
 * Template:	PluginScreenJavaTemplateGen.java.ftl
 * generator:   org.molgenis.generators.ui.PluginScreenJavaTemplateGen 3.3.3
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package org.molgenis.compute.db.ui;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.util.Entity;

public class ComputeHeader extends PluginModel<Entity>
{

	private static final long serialVersionUID = -6399721492853956608L;

	public ComputeHeader(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewName()
	{
		return "plugins_header_ComputeHeader";
	}

	@Override
	public String getViewTemplate()
	{
		return "ComputeHeader.ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{
		// replace example below with yours
		// try
		// {
		// Database db = this.getDatabase();
		// String action = request.getString("__action");
		//
		// if( action.equals("do_add") )
		// {
		// Experiment e = new Experiment();
		// e.set(request);
		// db.add(e);
		// }
		// } catch(Exception e)
		// {
		// //e.g. show a message in your form
		// }
	}

	@Override
	public void reload(Database db)
	{
		// try
		// {
		// Database db = this.getDatabase();
		// Query q = db.query(Experiment.class);
		// q.like("name", "test");
		// List<Experiment> recentExperiments = q.find();
		//
		// //do something
		// }
		// catch(Exception e)
		// {
		// //...
		// }
	}

	@Override
	public boolean isVisible()
	{
		// you can use this to hide this plugin, e.g. based on user rights.
		// e.g.
		// if(!this.getLogin().hasEditPermission(myEntity)) return false;
		return true;
	}
}
