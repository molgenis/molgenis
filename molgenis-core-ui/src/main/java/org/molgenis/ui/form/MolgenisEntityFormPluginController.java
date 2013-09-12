package org.molgenis.ui.form;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisPermissionService.Permission;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Field;
import org.molgenis.ui.MolgenisUiUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.ServletRequestParameterPropertyValues;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MolgenisEntityFormPluginController extends MolgenisPlugin
{
	public static final String PLUGIN_NAME_PREFIX = "form.";
	public static final String URI = MolgenisPlugin.PLUGIN_URI_PREFIX + PLUGIN_NAME_PREFIX;
	public static final String ENTITY_FORM_MODEL_ATTRIBUTE = "form";
	private static final String VIEW_NAME_LIST = "view-form-list";
	private static final String VIEW_NAME_EDIT = "view-form-edit";
	private static final Logger logger = Logger.getLogger(MolgenisEntityFormPluginController.class);

	@Autowired
	private Database database;

	@Autowired
	private MolgenisPermissionService permissionService;

	public MolgenisEntityFormPluginController()
	{
		super(URI);
	}

	@RequestMapping(method = GET, value = URI + "{entityName}")
	public String list(@PathVariable("entityName")
	String entityName, @RequestParam(value = "subForms", required = false)
	String[] subForms, Model model) throws DatabaseException, MolgenisModelException
	{
		model.addAttribute("current_uri", MolgenisUiUtils.getCurrentUri());

		Entity entityMetaData = createAndValidateEntity(entityName, Permission.READ);
		boolean hasWritePermission = permissionService.hasPermissionOnEntity(entityName, Permission.WRITE);

		EntityForm form = new EntityForm(entityMetaData, hasWritePermission);
		model.addAttribute(ENTITY_FORM_MODEL_ATTRIBUTE, form);

		if (subForms != null)
		{
			for (String subForm : subForms)
			{
				if (!subForm.contains("."))
				{
					throw new UnknownEntityException();
				}

				String[] subFormParts = subForm.split("[\\.]");
				String subEntityName = subFormParts[0];
				String xrefFieldName = subFormParts[1];

				Entity subEntityMetaData = createAndValidateEntity(subEntityName, Permission.READ);
				boolean found = false;
				for (Field field : subEntityMetaData.getFields())
				{
					if (field.isXRef() && field.getName().equals(xrefFieldName))
					{
						found = true;
						break;
					}
				}

				if (!found)
				{
					throw new UnknownEntityException();
				}

				boolean hasWritePermissionSubEntity = permissionService.hasPermissionOnEntity(subEntityName,
						Permission.WRITE);

				SubEntityForm subEntityForm = new SubEntityForm(subEntityMetaData, hasWritePermissionSubEntity,
						xrefFieldName);
				form.addSubForm(subEntityForm);
			}

		}

		return VIEW_NAME_LIST;
	}

	@RequestMapping(method = GET, value = URI + "{entityName}/{id}")
	public String edit(@PathVariable("entityName")
	String entityName, @PathVariable("id")
	String id, @RequestParam(value = "back", required = false)
	String back, Model model) throws DatabaseException, MolgenisModelException, ParseException
	{
		if (StringUtils.isNotBlank(back))
		{
			model.addAttribute("back", back);
		}

		Entity entityMetaData = createAndValidateEntity(entityName, Permission.READ);
		org.molgenis.util.Entity entity = findEntityById(entityMetaData, id);
		boolean hasWritePermission = permissionService.hasPermissionOnEntity(entityName, Permission.WRITE);
		model.addAttribute(ENTITY_FORM_MODEL_ATTRIBUTE, new EntityForm(entityMetaData, entity, id, hasWritePermission));

		return VIEW_NAME_EDIT;
	}

	@RequestMapping(method = GET, value = URI + "{entityName}/create")
	public String create(@PathVariable("entityName")
	String entityName, HttpServletRequest request, @RequestParam(value = "back", required = false)
	String back, Model model) throws Exception
	{
		if (StringUtils.isNotBlank(back))
		{
			model.addAttribute("back", back);
		}

		Entity entityMetaData = createAndValidateEntity(entityName, Permission.WRITE);

		// Requestparameters are used to prefill the form (for example in case of subform)
		// Create an entity of the correct type and set the field values from the request params
		Class<? extends org.molgenis.util.Entity> entityClass = database.getClassForName(entityName);
		org.molgenis.util.Entity entity = BeanUtils.instantiateClass(entityClass);

		Map<String, String[]> parameterMap = request.getParameterMap();
		if (!parameterMap.isEmpty())
		{
			MutablePropertyValues pvs = new ServletRequestParameterPropertyValues(request);
			DataBinder binder = new DataBinder(entity);
			binder.bind(pvs);

			// Resolve xrefs TODO mref
			for (String fieldName : parameterMap.keySet())
			{
				String value = request.getParameter(fieldName);

				if (StringUtils.isNotBlank(value))
				{
					Field field = entityMetaData.getAllField(fieldName);
					if ((field != null) && field.isXRef() && !field.isMRef())
					{
						List<? extends org.molgenis.util.Entity> results = null;
						String pkFieldName = field.getXrefEntity().getPrimaryKey().getName();
						Class<? extends org.molgenis.util.Entity> xrefEntityClass = database.getClassForName(field
								.getXrefEntityName());
						try
						{
							results = database.query(xrefEntityClass).equals(pkFieldName, value).find();
						}
						catch (Exception e)
						{
							// Probably pk is of wrong type, could be that user entered an invalid value
							logger.debug("Exception getting entity [" + xrefEntityClass
									+ "] by primarykey with value [" + value + "]", e);
						}

						if ((results != null) && !results.isEmpty())
						{
							new BeanWrapperImpl(entity).setPropertyValue(fieldName, results.get(0));
						}
					}

				}
			}
		}

		model.addAttribute(ENTITY_FORM_MODEL_ATTRIBUTE, new EntityForm(entityMetaData, true, entity));

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

		if (entityMetaData == null || entityMetaData.isSystem())
		{
			throw new UnknownEntityException("Unknown entity [" + entityName + "]");
		}

		if (!permissionService.hasPermissionOnEntity(entityName, permission))
		{
			throw new MolgenisEntityFormSecurityException();
		}

		return entityMetaData;
	}

}
