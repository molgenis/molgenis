package org.molgenis.ui.form;

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
public class MolgenisEntityFormPluginController extends MolgenisPlugin
{
	public static final String URI = MolgenisPlugin.PLUGIN_URI_PREFIX + "form";
	public static final String ENTITY_FORM_MODEL_ATTRIBUTE = "form";
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

	@RequestMapping(method = GET, value = URI + ".{entityName}")
	public String list(@PathVariable("entityName")
	String entityName, Model model) throws DatabaseException
	{
		Entity entityMetaData = createAndValidateEntity(entityName, Permission.READ);
		boolean hasWritePermission = permissionService.hasPermissionOnEntity(entityName, Permission.WRITE);
		model.addAttribute(ENTITY_FORM_MODEL_ATTRIBUTE, new EntityForm(entityMetaData, hasWritePermission));
		model.addAttribute(MolgenisPluginAttributes.KEY_PLUGIN_ID, getPluginId(entityName));

		return VIEW_NAME_LIST;
	}

	@RequestMapping(method = GET, value = URI + ".{entityName}/{id}")
	public String edit(@PathVariable("entityName")
	String entityName, @PathVariable("id")
	String id, Model model) throws DatabaseException, MolgenisModelException, ParseException
	{
		Entity entityMetaData = createAndValidateEntity(entityName, Permission.WRITE);
		org.molgenis.util.Entity entity = findEntityById(entityMetaData, id);
		model.addAttribute(ENTITY_FORM_MODEL_ATTRIBUTE, new EntityForm(entityMetaData, entity, id, true));
		model.addAttribute(MolgenisPluginAttributes.KEY_PLUGIN_ID, getPluginId(entityName));

		return VIEW_NAME_EDIT;
	}

	@RequestMapping(method = GET, value = URI + ".{entityName}/create")
	public String create(@PathVariable("entityName")
	String entityName, Model model) throws DatabaseException
	{
		Entity entityMetaData = createAndValidateEntity(entityName, Permission.WRITE);
		model.addAttribute(ENTITY_FORM_MODEL_ATTRIBUTE, new EntityForm(entityMetaData, true));
		model.addAttribute(MolgenisPluginAttributes.KEY_PLUGIN_ID, getPluginId(entityName));

		return VIEW_NAME_EDIT;
	}

	private org.molgenis.util.Entity findEntityById(Entity entityMetaData, String id) throws DatabaseException,
			ParseException, MolgenisModelException
	{
		String entityName = entityMetaData.getName();
		Class<? extends org.molgenis.util.Entity> entityClass = database.getClassForName(entityName);
		Object entityId = entityMetaData.getPrimaryKey().getType().getTypedValue(id);
		org.molgenis.util.Entity entity = database.findById(entityClass, entityId);
		if (entity == null)
		{
			throw new UnknownEntityException("Unknown entity [" + entityName + "] with id [" + id + "]");
		}

		return entity;
	}

	private Entity createAndValidateEntity(String entityName, Permission permission) throws DatabaseException
	{
		Entity entityMetaData = database.getMetaData().getEntity(entityName);

		if (entityMetaData == null)
		{
			throw new UnknownEntityException("Unknown entity [" + entityName + "]");
		}

		if (!permissionService.hasPermissionOnEntity(entityName, permission))
		{
			throw new MolgenisEntityFormSecurityException();
		}

		return entityMetaData;
	}

	private String getPluginId(String entityName)
	{
		return getId() + '/' + entityName;
	}
}
