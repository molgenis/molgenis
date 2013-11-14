package org.molgenis.ui.form;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisPermissionService.Permission;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.model.MolgenisModelException;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MolgenisEntityFormPluginController extends MolgenisPluginController
{
	public static final String PLUGIN_NAME_PREFIX = "form.";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + PLUGIN_NAME_PREFIX;
	public static final String ENTITY_FORM_MODEL_ATTRIBUTE = "form";
	private static final String VIEW_NAME_LIST = "view-form-list";
	private static final String VIEW_NAME_EDIT = "view-form-edit";
	private static final Logger logger = Logger.getLogger(MolgenisEntityFormPluginController.class);

	private final DataService dataService;
	private final MolgenisPermissionService molgenisPermissionService;

	@Autowired
	public MolgenisEntityFormPluginController(DataService dataService,
			MolgenisPermissionService molgenisPermissionService)
	{
		super(URI);
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (molgenisPermissionService == null) throw new IllegalArgumentException("MolgenisPermissionService is null");
		this.dataService = dataService;
		this.molgenisPermissionService = molgenisPermissionService;
	}

	@RequestMapping(method = RequestMethod.GET, value = URI + "{entityName}")
	public String list(@PathVariable("entityName")
	String entityName, @RequestParam(value = "subForms", required = false)
	String[] subForms, Model model) throws MolgenisModelException
	{
		model.addAttribute("current_uri", MolgenisUiUtils.getCurrentUri());

		EntityMetaData entityMetaData = createAndValidateEntity(entityName, Permission.READ);
		boolean hasWritePermission = molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.WRITE);

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

				EntityMetaData subEntityMetaData = createAndValidateEntity(subEntityName, Permission.READ);
				boolean found = false;
				for (AttributeMetaData attr : subEntityMetaData.getAttributes())
				{
					if ((attr.getRefEntity() != null) && attr.getName().equals(xrefFieldName))
					{
						found = true;
						break;
					}
				}

				if (!found)
				{
					throw new UnknownEntityException();
				}

				boolean hasWritePermissionSubEntity = molgenisPermissionService.hasPermissionOnEntity(subEntityName,
						Permission.WRITE);

				SubEntityForm subEntityForm = new SubEntityForm(subEntityMetaData, hasWritePermissionSubEntity,
						xrefFieldName);
				form.addSubForm(subEntityForm);
			}

		}

		return VIEW_NAME_LIST;
	}

	@RequestMapping(method = RequestMethod.GET, value = URI + "{entityName}/{id}")
	public String edit(@PathVariable("entityName")
	String entityName, @PathVariable("id")
	Integer id, @RequestParam(value = "back", required = false)
	String back, Model model)
	{
		if (StringUtils.isNotBlank(back))
		{
			model.addAttribute("back", back);
		}

		EntityMetaData entityMetaData = createAndValidateEntity(entityName, Permission.READ);
		Entity entity = findEntityById(entityMetaData, id);
		boolean hasWritePermission = molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.WRITE);
		model.addAttribute(ENTITY_FORM_MODEL_ATTRIBUTE, new EntityForm(entityMetaData, entity, id, hasWritePermission));

		return VIEW_NAME_EDIT;
	}

	@RequestMapping(method = RequestMethod.GET, value = URI + "{entityName}/create")
	public String create(@PathVariable("entityName")
	String entityName, HttpServletRequest request, @RequestParam(value = "back", required = false)
	String back, Model model) throws Exception
	{
		if (StringUtils.isNotBlank(back))
		{
			model.addAttribute("back", back);
		}

		Repository<? extends Entity> repo = createAndValidateEntity(entityName, Permission.WRITE);
		Entity entity = BeanUtils.instantiateClass(repo.getEntityClass());

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
					AttributeMetaData attr = repo.getAttribute(fieldName);
					if ((attr != null) && (attr.getDataType().getEnumType() == MolgenisFieldTypes.FieldTypeEnum.XREF))
					{
						EntityMetaData xrefEntityMetadata = attr.getRefEntity();
						Entity xref = null;
						try
						{

							xref = dataService.findOne(xrefEntityMetadata.getName(),
									new QueryImpl().eq(xrefEntityMetadata.getIdAttribute().getName(), value));
						}
						catch (Exception e)
						{
							// Probably pk is of wrong type, could be that user entered an invalid value
							logger.debug("Exception getting entity [" + xrefEntityMetadata.getName()
									+ "] by primarykey with value [" + value + "]", e);
						}

						if (xref != null)
						{
							new BeanWrapperImpl(entity).setPropertyValue(fieldName, xref);
						}
					}

				}
			}
		}

		model.addAttribute(ENTITY_FORM_MODEL_ATTRIBUTE, new EntityForm(repo, true, entity));

		return VIEW_NAME_EDIT;
	}

	private Entity findEntityById(EntityMetaData entityMetaData, Integer id)
	{
		String entityName = entityMetaData.getName();
		Entity entity = dataService.findOne(entityName, id);

		if (entity == null)
		{
			throw new UnknownEntityException("Unknown entity [" + entityName + "] with id [" + id + "]");
		}

		return entity;
	}

	private Repository<? extends Entity> createAndValidateEntity(String entityName, Permission permission)
	{
		Repository<? extends Entity> repo = dataService.getRepositoryByEntityName(entityName);

		if (!molgenisPermissionService.hasPermissionOnEntity(entityName, permission))
		{
			throw new MolgenisEntityFormSecurityException();
		}

		return repo;
	}

}
