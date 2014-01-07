package org.molgenis.studymanager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.log4j.Logger;
import org.molgenis.catalog.Catalog;
import org.molgenis.catalog.CatalogItem;
import org.molgenis.catalog.CatalogModel;
import org.molgenis.catalog.CatalogModelBuilder;
import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.catalogmanager.CatalogManagerService;
import org.molgenis.data.Entity;
import org.molgenis.data.Writable;
import org.molgenis.data.excel.ExcelWriter;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.study.StudyDefinition;
import org.molgenis.study.StudyDefinitionImpl;
import org.molgenis.study.UnknownStudyDefinitionException;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
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
public class StudyManagerController extends MolgenisPluginController
{
	private static final Logger logger = Logger.getLogger(StudyManagerController.class);

	public static final String ID = "studymanager";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
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
	public String getStudyDefinitions(Model model)
	{
		model.addAttribute("dataLoadingEnabled", studyDefinitionManagerService.canLoadStudyData());
		return VIEW_NAME;
	}

	/**
	 * Returns a list of meta data for each study definition
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public StudyDefinitionsMetaResponse getStudyDefinitionsMeta()
	{
		List<StudyDefinition> studyDefinitions = studyDefinitionManagerService.getStudyDefinitions();
		logger.debug("Got [" + studyDefinitions.size() + "] study definitions from service");

		List<StudyDefinitionMetaModel> models = Lists.transform(studyDefinitions,
				new Function<StudyDefinition, StudyDefinitionMetaModel>()
				{
					@Override
					public StudyDefinitionMetaModel apply(StudyDefinition studyDefinition)
					{
						String id = studyDefinition.getId();
						String name = studyDefinition.getName();
						String email = studyDefinition.getAuthorEmail();
						Date date = studyDefinition.getDateCreated();
						boolean loaded;
						try
						{
							loaded = studyDefinitionManagerService.isStudyDataLoaded(id);
						}
						catch (UnknownStudyDefinitionException e)
						{
							throw new RuntimeException(e);
						}
						return new StudyDefinitionMetaModel(id, name, email, date, loaded);
					}
				});

		StudyDefinitionsMetaResponse studyDefinitionsResponse = new StudyDefinitionsMetaResponse();
		studyDefinitionsResponse.setStudyDefinitions(models);
		return studyDefinitionsResponse;
	}

	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	@ResponseBody
	public CatalogModel getStudyDefinitionAsCatalog(@PathVariable String id) throws UnknownCatalogException,
			UnknownStudyDefinitionException
	{
		StudyDefinition studyDefinition = studyDefinitionManagerService.getStudyDefinition(id);
		Catalog catalog = catalogManagerService.getCatalogOfStudyDefinition(studyDefinition.getId());
		return CatalogModelBuilder.create(catalog, studyDefinition, true);
	}

	@RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
	@ResponseBody
	public CatalogModel getCatalogWithStudyDefinition(@PathVariable String id) throws UnknownCatalogException,
			UnknownStudyDefinitionException
	{
		// get study definition and catalog used to create study definition
		StudyDefinition studyDefinition = studyDefinitionManagerService.getStudyDefinition(id);
		Catalog catalog = catalogManagerService.getCatalogOfStudyDefinition(studyDefinition.getId());
		return CatalogModelBuilder.create(catalog, studyDefinition, false);
	}

	@RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateStudyDefinition(@PathVariable String id,
			@Valid @RequestBody StudyDefinitionUpdateRequest updateRequest) throws UnknownStudyDefinitionException,
			UnknownCatalogException
	{
		// get study definition and catalog used to create study definition
		StudyDefinition studyDefinition = studyDefinitionManagerService.getStudyDefinition(id);
		final Catalog catalog = catalogManagerService.getCatalogOfStudyDefinition(studyDefinition.getId());

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
	{
		try
		{
			if (id != null)
			{
				studyDefinitionManagerService.loadStudyData(id);
				model.addAttribute("successMessage", "Studydefinition loaded");
				logger.info("Loaded studydefinition with id [" + id + "]");
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

	@RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
	public void downloadStudyDefinition(@PathVariable String id, HttpServletResponse response)
			throws UnknownStudyDefinitionException, IOException
	{
		StudyDefinition studyDefinition = studyDefinitionManagerService.getStudyDefinition(id);

		// write response headers
		String fileName = "variables_" + new SimpleDateFormat("yyyy-MM-dd_HH.mm").format(new Date()) + ".xls";
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		// TODO remove code duplication (see ProtocolViewerController)
		// write excel file
		List<String> header = Arrays.asList("Id", "Variable", "Description");
		List<CatalogItem> catalogItems = studyDefinition.getItems();
		if (catalogItems != null)
		{
			Collections.sort(catalogItems, new Comparator<CatalogItem>()
			{
				@Override
				public int compare(CatalogItem catalogItem1, CatalogItem catalogItem2)
				{
					return catalogItem1.getId().compareTo(catalogItem2.getId());
				}
			});
		}
		ExcelWriter<Entity> excelWriter = new ExcelWriter<Entity>(response.getOutputStream());
		try
		{
			Writable<Entity> sheetWriter = excelWriter.createWritable("Variables", header);
			try
			{
				for (CatalogItem catalogItem : catalogItems)
				{
					Entity entity = new MapEntity();
					entity.set(header.get(0), catalogItem.getId());
					entity.set(header.get(1), catalogItem.getName());
					entity.set(header.get(2), catalogItem.getDescription());
					sheetWriter.add(entity);
				}
			}
			finally
			{
				sheetWriter.close();
			}
		}
		finally
		{
			excelWriter.close();
		}
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

	public static class StudyDefinitionsMetaResponse
	{
		private List<StudyDefinitionMetaModel> studyDefinitions;

		public List<StudyDefinitionMetaModel> getStudyDefinitions()
		{
			return studyDefinitions;
		}

		public void setStudyDefinitions(List<StudyDefinitionMetaModel> studyDefinitions)
		{
			this.studyDefinitions = studyDefinitions;
		}
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		logger.error(null, e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(
				"An error occured. Please contact the administrator.<br />Message:" + e.getMessage())));
	}

	@ExceptionHandler(Exception.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorMessageResponse handleException(Exception e)
	{
		logger.debug(null, e);
		return new ErrorMessageResponse(Collections.singletonList(new ErrorMessage(e.getMessage())));
	}
}
