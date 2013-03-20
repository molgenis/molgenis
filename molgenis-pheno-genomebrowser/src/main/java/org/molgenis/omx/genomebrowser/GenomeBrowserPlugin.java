/* Date:        November 19, 2010
 * Template:	PluginScreenJavaTemplateGen.java.ftl
 * generator:   org.molgenis.generators.ui.PluginScreenJavaTemplateGen 3.3.3
 * 
 */

package org.molgenis.omx.genomebrowser;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.util.Entity;

public class GenomeBrowserPlugin<E extends Entity> extends PluginModel<E>
{

	private static final long serialVersionUID = -2848815736940818733L;
	private GenomeBrowserPluginModel myModel;

	public GenomeBrowserPluginModel getMyModel()
	{
		return myModel;
	}

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
		return "templates/org/molgenis/omx/genomebrowser/GenomeBrowser.ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{
		// replace example below with yours
		// try
		// {
		// Database db = this.getDatabase();
		System.out.println("handleRequest");
		String action = request.getAction();
		System.out.println("action= "+action);
		if (action.equals("changePosition"))
		{
			String startPosition = request.getString("startPosition");
			String endPosition = request.getString("endPosition");
			String chromosome = request.getString("chromosome");
			this.myModel.setStartPosition(startPosition);
			this.myModel.setEndPosition(endPosition);
			this.myModel.setChromosome(chromosome);
		}
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
		if (this.myModel == null)
		{
			this.myModel = new GenomeBrowserPluginModel();
		}
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
