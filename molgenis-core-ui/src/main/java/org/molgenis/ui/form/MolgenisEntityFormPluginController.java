package org.molgenis.ui.form;

import static org.molgenis.ui.form.MolgenisEntityFormPluginController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.text.ParseException;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisPermissionService.Permission;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.elements.Entity;
import org.molgenis.ui.MolgenisPluginAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URI)
public class MolgenisEntityFormPluginController extends MolgenisPlugin
{
	public static final String URI = MolgenisPlugin.PLUGIN_URI_PREFIX + "form";
	private static final String VIEW_NAME_LIST = "view-form-list";
	private static final String VIEW_NAME_EDIT = "view-form-edit";

	@Autowired
	private Database database;

	@Autowired
	private MolgenisPermissionService permissionService;

	public MolgenisEntityFormPluginController()
	{
		super(URI);
	}

	@RequestMapping(method = GET, value = "/{entityName}")
	public String list(@PathVariable("entityName")
	String entityName, Model model) throws DatabaseException
	{
		Entity entityMetaData = database.getMetaData().getEntity(entityName);
		if (entityMetaData == null)
		{
			throw new UnknownEntityException("Unknown entity [" + entityName + "]");
		}

		if (!permissionService.hasPermissionOnEntity(entityName, Permission.READ))
		{
			throw new MolgenisEntityFormSecurityException();
		}

		boolean hasWritePermission = permissionService.hasPermissionOnEntity(entityName, Permission.WRITE);
		model.addAttribute("form", new EntityForm(entityMetaData, hasWritePermission));
		model.addAttribute(MolgenisPluginAttributes.KEY_PLUGIN_ID, getPluginId(entityName));

		return VIEW_NAME_LIST;
	}

	@RequestMapping(method = GET, value = "/{entityName}/{id}")
	public String edit(@PathVariable("entityName")
	String entityName, @PathVariable("id")
	String id, Model model) throws DatabaseException, MolgenisModelException, ParseException
	{
		Entity entityMetaData = database.getMetaData().getEntity(entityName);

		if (entityMetaData == null)
		{
			throw new UnknownEntityException("Unknown entity [" + entityName + "]");
		}

		if (!permissionService.hasPermissionOnEntity(entityName, Permission.WRITE))
		{
			throw new MolgenisEntityFormSecurityException();
		}

		Class<? extends org.molgenis.util.Entity> entityClass = database.getClassForName(entityName);
		Object entityId = entityMetaData.getPrimaryKey().getType().getTypedValue(id);
		org.molgenis.util.Entity entity = database.findById(entityClass, entityId);
		if (entity == null)
		{
			throw new UnknownEntityException("Unknown entity [" + entityName + "] with id [" + id + "]");
		}

		model.addAttribute("form", new EntityForm(entityMetaData, entity, id, true));
		model.addAttribute(MolgenisPluginAttributes.KEY_PLUGIN_ID, getPluginId(entityName));

		return VIEW_NAME_EDIT;
	}

	@RequestMapping(method = GET, value = "/{entityName}/create")
	public String create(@PathVariable("entityName")
	String entityName, Model model) throws DatabaseException
	{
		Entity entityMetaData = database.getMetaData().getEntity(entityName);
		if (entityMetaData == null)
		{
			throw new UnknownEntityException("Unknown entity [" + entityName + "]");
		}

		if (!permissionService.hasPermissionOnEntity(entityName, Permission.WRITE))
		{
			throw new MolgenisEntityFormSecurityException();
		}

		model.addAttribute("form", new EntityForm(entityMetaData, true));
		model.addAttribute(MolgenisPluginAttributes.KEY_PLUGIN_ID, getPluginId(entityName));

		return VIEW_NAME_EDIT;
	}

	private String getPluginId(String entityName)
	{
		return getId() + '/' + entityName;
	}
}
