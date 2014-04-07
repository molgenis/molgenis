package org.molgenis.data.model.registry;

import static org.molgenis.data.model.registry.MetaDataExplorerController.URI;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.JDBCMetaDatabase;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.ui.MolgenisUiPluginRegistry;
import org.molgenis.ui.MolgenisUiUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
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
	public static List<String> ENTITY_CLASS_TYPES;
	private static final int NR_ITEMS_PER_PAGE = 4;
	private final DataService dataService;
	private final MolgenisUiPluginRegistry molgenisUiPluginRegistry;
	private final MolgenisPermissionService molgenisPermissionService;

	@Autowired
	public MetaDataExplorerController(DataService dataService, MolgenisUiPluginRegistry molgenisUiPluginRegistry,
			MolgenisPermissionService molgenisPermissionService) throws MolgenisModelException
	{
		super(URI);
		this.dataService = dataService;
		this.molgenisUiPluginRegistry = molgenisUiPluginRegistry;
		this.molgenisPermissionService = molgenisPermissionService;
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
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping
	public String showMetaDataExplorer(MetaDataSearchForm metaDataSearchForm, Model model) throws UnsupportedEncodingException
	{
		List<EntityClassModel> models = Lists.newArrayList();

		MolgenisPlugin dataexplorer = molgenisUiPluginRegistry.getPlugin(DataExplorerController.ID);
		String dataexplorerUri = dataexplorer != null ? dataexplorer.getFullUri() : null;
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

			Iterable<EntityClass> entityClasses = dataService.findAll(EntityClass.ENTITY_NAME, q, EntityClass.class);
			for (EntityClass entityClass : entityClasses)
			{
				// Explore data button
				String dataexplorerPageUri = null;
				if ((dataexplorerUri != null)
						&& dataService.hasRepository(entityClass.getEntityClassIdentifier())
						&& molgenisPermissionService.hasPermissionOnEntity(entityClass.getEntityClassIdentifier(),
								Permission.READ))
				{
					dataexplorerPageUri = String.format("%s?dataset=%s", dataexplorerUri,
							URLEncoder.encode(entityClass.getEntityClassIdentifier(), "UTF-8"));
				}

				// Edit button
				String formUri = null;
				if (molgenisPermissionService.hasPermissionOnEntity(entityClass.getEntityClassIdentifier(),
						Permission.WRITE))
				{
					formUri = String.format("/menu/entities/form.EntityClass/%d?back=%s", entityClass.getId(),
							URLEncoder.encode(MolgenisUiUtils.getCurrentUri(), "UTF-8"));
				}

				models.add(new EntityClassModel(entityClass, dataexplorerPageUri, formUri));
			}
		}

		model.addAttribute("nrItems", totalCount);
		model.addAttribute("nrItemsPerPage", NR_ITEMS_PER_PAGE);
		model.addAttribute("entityClassModels", models);
		model.addAttribute("metaDataSearchForm", metaDataSearchForm);

		return "view-metadataexplorer";
	}
}
