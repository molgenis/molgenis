package org.molgenis.studymanager;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.log4j.Logger;
import org.molgenis.catalog.Catalog;
import org.molgenis.catalog.CatalogItem;
import org.molgenis.catalog.CatalogModel;
import org.molgenis.catalog.CatalogModelBuilder;
import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.catalogmanager.CatalogManagerService;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.study.StudyDefinition;
import org.molgenis.study.StudyDefinitionImpl;
import org.molgenis.study.StudyDefinitionMeta;
import org.molgenis.study.UnknownStudyDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Controller
@RequestMapping(StudyManagerController.URI)
public class StudyManagerController extends MolgenisPlugin
{
	private static final Logger LOG = Logger.getLogger(StudyManagerController.class);

	public static final String URI = MolgenisPlugin.PLUGIN_URI_PREFIX + "studymanager";
	public static final String LOAD_LIST_URI = "/load-list";
	public static final String VIEW_NAME = "view-studymanager";

	private final StudyManagerService studyDefinitionManagerService;
	private final CatalogManagerService catalogManagerService;

	@Autowired
	public StudyManagerController(StudyManagerService studyDefinitionManagerService,
			CatalogManagerService catalogManagerService)
	{
		super(URI);
		if (studyDefinitionManagerService == null) throw new IllegalArgumentException(
				"Study definition manager service is null");
		if (catalogManagerService == null) throw new IllegalArgumentException("Catalog manager service is null");
		this.studyDefinitionManagerService = studyDefinitionManagerService;
		this.catalogManagerService = catalogManagerService;
	}

	/**
	 * Show the available studydefinitions.
	 * 
	 * StudyDefinitions are exposed via a 'studyDefinitions' model attribute that contains a list of
	 * StudyDefinitionModel objects.
	 * 
	 * @param model
	 * @return
	 * @throws DatabaseException
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String getStudyDefinitions(Model model) throws DatabaseException
	{
		List<StudyDefinitionMeta> studyDefinitions = studyDefinitionManagerService.getStudyDefinitions();
		LOG.debug("Got [" + studyDefinitions.size() + "] study definitions from service");

		List<StudyDefinitionMetaModel> models = Lists.transform(studyDefinitions,
				new Function<StudyDefinitionMeta, StudyDefinitionMetaModel>()
				{
					@Override
					public StudyDefinitionMetaModel apply(StudyDefinitionMeta studyDefinitionMeta)
					{
						String id = studyDefinitionMeta.getId();
						String name = studyDefinitionMeta.getName();
						boolean loaded;
						try
						{
							loaded = studyDefinitionManagerService.isStudyDataLoaded(id);
						}
						catch (UnknownStudyDefinitionException e)
						{
							throw new RuntimeException(e);
						}
						return new StudyDefinitionMetaModel(id, name, loaded);
					}
				});

		model.addAttribute("studyDefinitions", models);

		return VIEW_NAME;
	}

	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	@ResponseBody
	public CatalogModel getStudyDefinitionAsCatalog(@PathVariable String id) throws UnknownCatalogException,
			UnknownStudyDefinitionException
	{
		Catalog catalog = catalogManagerService.getCatalogOfStudyDefinition(id);
		StudyDefinition studyDefinition = studyDefinitionManagerService.getStudyDefinition(id);
		return CatalogModelBuilder.create(catalog, studyDefinition, true);
	}

	@RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
	@ResponseBody
	public CatalogModel getCatalogWithStudyDefinition(@PathVariable String id) throws UnknownCatalogException,
			UnknownStudyDefinitionException
	{
		// get study definition and catalog used to create study definition
		Catalog catalog = catalogManagerService.getCatalogOfStudyDefinition(id);
		StudyDefinition studyDefinition = studyDefinitionManagerService.getStudyDefinition(id);
		return CatalogModelBuilder.create(catalog, studyDefinition, false);
	}

	@RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateStudyDefinition(@PathVariable String id,
			@Valid @RequestBody StudyDefinitionUpdateRequest updateRequest) throws UnknownStudyDefinitionException,
			UnknownCatalogException
	{
		// get study definition and catalog used to create study definition
		final Catalog catalog = catalogManagerService.getCatalogOfStudyDefinition(id);
		StudyDefinition studyDefinition = studyDefinitionManagerService.getStudyDefinition(id);

		// create updated study definition
		StudyDefinitionImpl updatedStudyDefinition = new StudyDefinitionImpl(studyDefinition);
		updatedStudyDefinition.setItems(Lists.transform(updateRequest.getCatalogItemIds(),
				new Function<String, CatalogItem>()
				{
					@Override
					public CatalogItem apply(String catalogItemId)
					{
						CatalogItem catalogItem = catalog.findItem(catalogItemId);
						if (catalogItem == null) throw new RuntimeException("unknown catalog item id: " + catalogItemId);
						return catalogItem;
					}
				}));

		// update study definition
		studyDefinitionManagerService.updateStudyDefinition(updatedStudyDefinition);
	}

	/**
	 * Loads a studydefinition by it's id.
	 * 
	 * If an error occurred an 'errorMessage' model attribute is exposed.
	 * 
	 * If the studydefinition was successfully loaded a 'successMessage' model attribute is exposed.
	 * 
	 * @param id
	 * @param model
	 * @return
	 * @throws DatabaseException
	 */
	@RequestMapping(value = "/load", method = RequestMethod.POST)
	public String loadStudyDefinition(@RequestParam(value = "id", required = false) String id, Model model)
			throws DatabaseException
	{
		try
		{
			if (id != null)
			{
				studyDefinitionManagerService.loadStudyData(id);
				model.addAttribute("successMessage", "Studydefinition loaded");
				LOG.info("Loaded studydefinition with id [" + id + "]");
			}
			else
			{
				model.addAttribute("errorMessage", "Please select a studydefinition");
			}
		}
		catch (UnknownStudyDefinitionException e)
		{
			model.addAttribute("errorMessage", e.getMessage());
		}

		return getStudyDefinitions(model);
	}

	public static class StudyDefinitionUpdateRequest
	{
		@NotNull
		private List<String> catalogItemIds;

		public List<String> getCatalogItemIds()
		{
			return catalogItemIds;
		}

		public void setCatalogItemIds(List<String> catalogItemIds)
		{
			this.catalogItemIds = catalogItemIds;
		}
	}

	@ExceptionHandler(
	{ Exception.class, RuntimeException.class })
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, String> handleRuntimeException(Exception e)
	{
		return Collections.singletonMap("errorMessage", e.getMessage());
	}
}
