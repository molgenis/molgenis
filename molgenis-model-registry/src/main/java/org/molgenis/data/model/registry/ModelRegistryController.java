package org.molgenis.data.model.registry;

import static org.molgenis.data.model.registry.ModelRegistryController.URI;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.JDBCMetaDatabase;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.model.MolgenisModelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.Lists;

/**
 * Controller class for the model registry
 */
@Controller
@RequestMapping(URI)
public class ModelRegistryController extends MolgenisPluginController
{
	public static final String ID = "models";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static List<String> ENTITY_CLASS_TYPES;
	private final DataService dataService;

	@Autowired
	public ModelRegistryController(DataService dataService) throws MolgenisModelException
	{
		super(URI);
		this.dataService = dataService;
		ENTITY_CLASS_TYPES = new JDBCMetaDatabase().getEntity("EntityClass").getField("type").getEnumOptions();
	}

	@ModelAttribute("entityClassTypes")
	public List<String> getEntityClassTypes() throws MolgenisModelException
	{
		return ENTITY_CLASS_TYPES;
	}

	/**
	 * Show the page that lists all entityClasses (models)
	 * 
	 * @param model
	 * @return the viewname
	 */
	@RequestMapping
	public String showModelExplorer(SearchForm searchForm, Model model)
	{
		List<EntityClass> entityClasses = Lists.newArrayList();

		if ((searchForm.getEntityClassTypes() != null) && !searchForm.getEntityClassTypes().isEmpty())
		{
			Query q = new QueryImpl();

			if (StringUtils.isNotBlank(searchForm.getSearchTerm()))
			{
				q.nest().search(searchForm.getSearchTerm()).unnest();
				q.and();
			}

			for (int i = 0; i < searchForm.getEntityClassTypes().size(); i++)
			{
				if (i > 0)
				{
					q.or();
				}

				q.eq(EntityClass.TYPE, searchForm.getEntityClassTypes().get(i));
			}

			entityClasses
					.addAll(Lists.newArrayList(dataService.findAll(EntityClass.ENTITY_NAME, q, EntityClass.class)));
		}

		model.addAttribute("entityClasses", entityClasses);
		model.addAttribute("form", searchForm);

		return "view-modelexplorer";
	}
}
