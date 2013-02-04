/* Date:        November 19, 2010
 * Template:	PluginScreenJavaTemplateGen.java.ftl
 * generator:   org.molgenis.generators.ui.PluginScreenJavaTemplateGen 3.3.3
 * 
 */

package org.molgenis.omicsconnect.plugins.browser;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.util.Entity;

public class GenomeBrowserPlugin<E extends Entity> extends PluginModel<E>
{

	private static final long serialVersionUID = -2848815736940818733L;

	public GenomeBrowserPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewName()
	{
		return "plugins_browser_GenomeBrowserPlugin";
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/org/molgenis/omicsconnect/plugins/browser/GenomeBrowser.ftl";
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
	}

	@Override
	public boolean isVisible()
	{
		// you can use this to hide this plugin, e.g. based on user rights.
		// e.g.
		// if(!this.getLogin().hasEditPermission(myEntity)) return false;
		return true;
	}

	public int getUserId()
	{
		if (this.getLogin().isAuthenticated() == true)
		{
			return this.getLogin().getUserId();
		}
		else
		{
			return 0;
		}
	}
}
