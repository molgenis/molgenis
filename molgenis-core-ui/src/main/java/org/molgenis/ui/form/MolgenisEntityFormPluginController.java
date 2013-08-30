package org.molgenis.ui.form;

import static org.molgenis.ui.form.MolgenisEntityFormPluginController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisPermissionService.Permission;
import org.molgenis.framework.ui.MolgenisPlugin;
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

	@Autowired
	private Database database;

	@Autowired
	private MolgenisPermissionService permissionService;

	public MolgenisEntityFormPluginController()
	{
		super(URI);
	}

	@RequestMapping(method = GET, value = "/{entityName}")
	public String list(@PathVariable("entityName") String entityName, Model model) throws DatabaseException
	{
		Entity entity = database.getMetaData().getEntity(entityName);
		if (entity == null)
		{
			throw new UnknownEntityException("Unknown entity [" + entityName + "]");
		}

		if (!permissionService.hasPermissionOnEntity(entityName, Permission.READ))
		{
			throw new MolgenisEntityFormSecurityException();
		}

		model.addAttribute(MolgenisPluginAttributes.KEY_PLUGIN_ID, getId() + '/' + entityName);
		model.addAttribute("form", new EntityForm(entity));

		return VIEW_NAME_LIST;
	}

}
