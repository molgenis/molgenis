package org.molgenis.ui.form;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.framework.ui.MolgenisPluginFactory;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.ui.MolgenisUiUtils;
import org.springframework.beans.BeanUtils;
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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

@Controller
public class MolgenisEntityFormPluginController extends MolgenisPluginController
{
	public static final String PLUGIN_NAME_PREFIX = "form.";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + PLUGIN_NAME_PREFIX;
	public static final String ENTITY_FORM_MODEL_ATTRIBUTE = "form";
	private static final String VIEW_NAME_LIST = "view-form-list";
	private static final String VIEW_NAME_EDIT = "view-form-edit";

	private final DataService dataService;
	private final MolgenisPermissionService molgenisPermissionService;

	@Autowired
	public MolgenisEntityFormPluginController(final DataService dataService,
			MolgenisPermissionService molgenisPermissionService)
	{
		super(URI, new MolgenisPluginFactory()
		{
			@Override
			public Iterator<MolgenisPlugin> iterator()
			{
				return Iterables.transform(dataService.getEntityNames(), new Function<String, MolgenisPlugin>()
				{
					@Override
					public MolgenisPlugin apply(String entityName)
					{
						String pluginId = PLUGIN_NAME_PREFIX + entityName;
						return new MolgenisPlugin(pluginId, pluginId, "", ""); // FIXME
					}
				}).iterator();
			}
		});
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (molgenisPermissionService == null) throw new IllegalArgumentException("MolgenisPermissionService is null");
		this.dataService = dataService;
		this.molgenisPermissionService = molgenisPermissionService;
	}

	@RequestMapping(method = RequestMethod.GET, value = URI + "{entityName}")
	public String list(@PathVariable("entityName") String entityName,
			@RequestParam(value = "subForms", required = false) String[] subForms, Model model)
			throws MolgenisModelException
	{
		model.addAttribute("current_uri", MolgenisUiUtils.getCurrentUri());

		EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);
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

				EntityMetaData subEntityMetaData = dataService.getEntityMetaData(subEntityName);
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
	public String edit(@PathVariable("entityName") String entityName, @PathVariable("id") Object id,
			@RequestParam(value = "back", required = false) String back, Model model)
	{
		if (StringUtils.isNotBlank(back))
		{
			model.addAttribute("back", back);
		}

		EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);
		Entity entity = findEntityById(entityMetaData, id);
		boolean hasWritePermission = molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.WRITE);
		model.addAttribute(ENTITY_FORM_MODEL_ATTRIBUTE, new EntityForm(entityMetaData, entity, id, hasWritePermission));

		return VIEW_NAME_EDIT;
	}

	@RequestMapping(method = RequestMethod.GET, value = URI + "{entityName}/create")
	public String create(@PathVariable("entityName") String entityName, HttpServletRequest request,
			@RequestParam(value = "back", required = false) String back, Model model) throws Exception
	{
		if (StringUtils.isNotBlank(back))
		{
			model.addAttribute("back", back);
		}

		Repository repo = dataService.getRepository(entityName);
		Entity entity = null;
		if (repo.getEntityMetaData().getEntityClass() != Entity.class) entity = BeanUtils.instantiateClass(repo
				.getEntityMetaData().getEntityClass());
		else entity = new MapEntity();
		EntityMetaData entityMeta = repo.getEntityMetaData();

		Map<String, String[]> parameterMap = request.getParameterMap();
		if (!parameterMap.isEmpty())
		{
			MutablePropertyValues pvs = new ServletRequestParameterPropertyValues(request);
			DataBinder binder = new DataBinder(entity);
			binder.bind(pvs);

			// Set xref prop to preselect dropdown
			for (String fieldName : parameterMap.keySet())
			{
				String value = request.getParameter(fieldName);

				if (StringUtils.isNotBlank(value))
				{
					AttributeMetaData attr = entityMeta.getAttribute(fieldName);
					if ((attr != null) && (attr.getDataType().getEnumType() == MolgenisFieldTypes.FieldTypeEnum.XREF))
					{
						Entity xref = dataService.findOne(attr.getRefEntity().getName(),
								new QueryImpl().eq(attr.getRefEntity().getIdAttribute().getName(), value));

						if (xref != null)
						{
							entity.set(fieldName, xref);
						}
					}

				}
			}
		}

		model.addAttribute(ENTITY_FORM_MODEL_ATTRIBUTE, new EntityForm(entityMeta, true, entity));

		return VIEW_NAME_EDIT;
	}

	private Entity findEntityById(EntityMetaData entityMetaData, Object id)
	{
		String entityName = entityMetaData.getName();
		Object typedId = entityMetaData.getIdAttribute().getDataType().convert(id);
		Entity entity = dataService.findOne(entityName, typedId);

		if (entity == null)
		{
			throw new UnknownEntityException("Unknown entity [" + entityName + "] with id [" + id + "]");
		}

		return entity;
	}
}
