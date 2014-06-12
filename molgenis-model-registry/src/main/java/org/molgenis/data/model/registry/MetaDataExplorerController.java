package org.molgenis.data.model.registry;

import static org.molgenis.data.model.registry.MetaDataExplorerController.URI;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.JDBCMetaDatabase;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.model.MolgenisModelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.google.common.collect.Lists;

/**
 * Controller class for the model registry
 */
@SessionAttributes("metaDataSearchForm")
@Controller
@RequestMapping(URI)
public class MetaDataExplorerController extends MolgenisPluginController
{
	public static final String ID = "models";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final List<String> ENTITY_CLASS_TYPES;
	private static final int NR_ITEMS_PER_PAGE = 4;
	private final DataService dataService;

	static
	{
		try
		{
			ENTITY_CLASS_TYPES = new JDBCMetaDatabase().getEntity("EntityClass").getField("type").getEnumOptions();
		}
		catch (MolgenisModelException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Autowired
	public MetaDataExplorerController(DataService dataService) throws MolgenisModelException
	{
		super(URI);
		this.dataService = dataService;
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
	public String showMetaDataExplorer(MetaDataSearchForm metaDataSearchForm, Model model)
	{
		List<EntityClass> entityClasses;
		long totalCount = 0;

		if ((metaDataSearchForm.getEntityClassTypes() != null) && !metaDataSearchForm.getEntityClassTypes().isEmpty())
		{
			Query q = new QueryImpl();

			if (StringUtils.isNotBlank(metaDataSearchForm.getSearchTerm()))
			{
				q.search(metaDataSearchForm.getSearchTerm());
			}

			q.in(EntityClass.TYPE, metaDataSearchForm.getEntityClassTypes());

			totalCount = dataService.count(EntityClass.ENTITY_NAME, q);

			q.pageSize(NR_ITEMS_PER_PAGE);
			q.offset((metaDataSearchForm.getPage() - 1) * NR_ITEMS_PER_PAGE);

			entityClasses = Lists.newArrayList(dataService.findAll(EntityClass.ENTITY_NAME, q, EntityClass.class));
		}
		else
		{
			entityClasses = Lists.newArrayList();
		}

		model.addAttribute("nrItems", totalCount);
		model.addAttribute("nrItemsPerPage", NR_ITEMS_PER_PAGE);
		model.addAttribute("entityClasses", entityClasses);
		model.addAttribute("metaDataSearchForm", metaDataSearchForm);

		return "view-metadataexplorer";
	}

	@RequestMapping("{entityClassIdentifier}")
	public String showDetails(@PathVariable("entityClassIdentifier") String entityClassIdentifier, Model model,
			HttpServletResponse response) throws IOException
	{
		Entity entityClass = dataService.findOne(EntityClass.ENTITY_NAME,
				new QueryImpl().eq(EntityClass.ENTITYCLASSIDENTIFIER, entityClassIdentifier));

		if (entityClass == null)
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}

		model.addAttribute("entityClass", entityClass);

		return "view-metadatadetails";
	}
}
