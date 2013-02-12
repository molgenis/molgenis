/**
 * @author Jessica Lundberg
 * @author Robert Wagner
 * @author Erik Roos
 * @date 04-02-2011
 * 
 * This class is a controller for the PermissionManagementPlugin, which allows
 * users to see their rights and also the rights of others on entities
 * they own. 
 */
package org.molgenis.omx.auth.service.permissionmanagement;

import java.text.ParseException;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.security.SimpleLogin;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.omx.auth.MolgenisPermission;
import org.molgenis.util.Entity;

public class PermissionManagementPlugin extends PluginModel<Entity>
{

	private static final long serialVersionUID = -9150476614594665384L;
	private final PermissionManagementModel varmodel;
	private PermissionManagementService service;
	private static Logger logger = Logger.getLogger(PermissionManagementPlugin.class);

	public PermissionManagementPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);

		varmodel = new PermissionManagementModel();
	}

	@Override
	public String getViewName()
	{
		return "org_molgenis_auth_service_permissionmanagement_PermissionManagementPlugin";
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + PermissionManagementPlugin.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{

		service.setDb(db);
		try
		{
			varmodel.setAction(request.getAction());

			if (varmodel.getAction().equals("AddEdit"))
			{
				varmodel.setPermId(request.getInt("id"));
			}
			else if (varmodel.getAction().equals("Remove"))
			{
				varmodel.setPermId(request.getInt("id"));
				service.remove(varmodel.getRole().getId(), varmodel.getPermId());
				this.setMessages(new ScreenMessage("Removal successful", true));
			}
			else if (varmodel.getAction().equals("AddPerm"))
			{
				service.insert(varmodel.getRole().getId(), addPermission(request));
				this.setMessages(new ScreenMessage("Adding successful", true));
			}
			else if (varmodel.getAction().equals("UpdatePerm"))
			{
				service.update(varmodel.getRole().getId(), updatePermission(request));
				this.setMessages(new ScreenMessage("Update successful", true));
			}

		}
		catch (Exception e)
		{
			logger.error("Error occurred: ", e);
			this.setMessages(new ScreenMessage(e != null ? e.getMessage() : "null", false));
		}
	}

	/**
	 * Update a permission based on request
	 * 
	 * @param request
	 * @return
	 * @throws DatabaseException
	 * @throws ParseException
	 */
	private MolgenisPermission updatePermission(MolgenisRequest request) throws DatabaseException, ParseException
	{
		if (request.getString("entity") != null)
		{
			MolgenisPermission perm = new MolgenisPermission();
			perm.setEntity(Integer.parseInt(request.getString("entity")));
			perm.setRole(Integer.parseInt(request.getString("role")));
			perm.setPermission(request.getString("permission"));
			return perm;
		}
		else
		{
			throw new DatabaseException("Cannot update permission: no entity set");
		}
	}

	/**
	 * Insert (add) a permission based on request
	 * 
	 * @param request
	 * @return
	 */
	public MolgenisPermission addPermission(MolgenisRequest request) throws DatabaseException
	{
		if (request.getString("entity") != null)
		{
			MolgenisPermission perm = new MolgenisPermission();
			perm.setEntity(Integer.parseInt(request.getString("entity")));
			perm.setRole(Integer.parseInt(request.getString("role")));
			perm.setPermission(request.getString("permission"));
			return perm;
		}
		else
		{
			throw new DatabaseException("Cannot add permission: no entity set");
		}
	}

	@Override
	public void reload(Database db)
	{

		service = PermissionManagementService.getInstance();
		service.setDb(db);
		try
		{
			varmodel.setRole(service.findRole(db.getLogin().getUserId()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			// TODO: add logger + screen message
		}
	}

	public PermissionManagementModel getVarmodel()
	{
		return varmodel;
	}

	public PermissionManagementService getService()
	{
		return service;
	}

	public void setService(PermissionManagementService service)
	{
		this.service = service;
	}

	@Override
	public boolean isVisible()
	{
		if (this.getController().getApplicationController().getLogin() instanceof SimpleLogin)
		{
			return false;
		}
		return super.isVisible();
	}

}
