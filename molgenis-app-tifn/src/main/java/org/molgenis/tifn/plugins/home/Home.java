/* Date:        February 2, 2010
 * Template:	PluginScreenJavaTemplateGen.java.ftl
 * generator:   org.molgenis.generators.ui.PluginScreenJavaTemplateGen 3.3.2-testing
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package org.molgenis.tifn.plugins.home;

import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.util.Entity;

import app.FillMetadata;

/**
 * Shows table of experiment information for WormQTL
 */
public class Home extends PluginModel<Entity>
{

	private static final long serialVersionUID = 1L;

	private final HomeModel model = new HomeModel();

	public HomeModel getMyModel()
	{
		return model;
	}

	public Home(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewName()
	{
		return "Home";
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/org/molgenis/tifn/plugins/home/Home.ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{
		
	}

	@Override
	public void reload(Database db)
	{
		System.out.println("RELOAD aangeroepen");
		List<MolgenisUser> listUsers;
		try {
			listUsers = db.find(MolgenisUser.class, new QueryRule(MolgenisUser.NAME, Operator.EQUALS,"admin"));
		
		System.out.println("listUsers: " + listUsers.size());
		if(listUsers.isEmpty()){
			
			FillMetadata.fillMetadata(db,false);
			System.out.println("BLAAT");
			this.setMessages(new ScreenMessage("User setup complete!",true));
		}
		
		
		} catch (DatabaseException e) {
			this.setMessages(new ScreenMessage("Database error: "+e.getMessage(),false));
			e.printStackTrace();
		}
		catch(Exception e){
			this.setMessages(new ScreenMessage("error: "+e.getMessage(),false));
			e.printStackTrace();
		}
		
		
	}
	public boolean isVisible(){
		return true;
	}

}
