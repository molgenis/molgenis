package org.molgenis.lifelines.studydefinition;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.Characteristic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(StudyDefinitionLoaderController.BASE_URL)
public class StudyDefinitionLoaderController
{
	public static final String BASE_URL = "/plugin/studydefinition";
	public static final String LIST_URI = "/list";
	public static final String LOAD_URI = "/load";
	public static final String VIEW_NAME = "study-definition-loader";
	private static final Logger LOG = Logger.getLogger(StudyDefinitionLoaderController.class);
	private final Database database;
	private final StudyDefinitionLoaderService studyDefinitionLoaderService;

	@Autowired
	public StudyDefinitionLoaderController(Database database, StudyDefinitionLoaderService studyDefinitionLoaderService)
	{
		if (database == null) throw new IllegalArgumentException("Database id null");
		if (studyDefinitionLoaderService == null) throw new IllegalArgumentException(
				"StudyDefinitionLoaderService is null");
		this.database = database;
		this.studyDefinitionLoaderService = studyDefinitionLoaderService;
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
	@RequestMapping(LIST_URI)
	public String listStudyDefinitions(Model model) throws DatabaseException
	{
		List<StudyDefinitionInfo> studyDefinitions = studyDefinitionLoaderService.findStudyDefinitions();
		LOG.debug("Got [" + studyDefinitions.size() + "] catalogs from service");

		List<StudyDefinitionModel> models = new ArrayList<StudyDefinitionModel>(studyDefinitions.size());
		for (StudyDefinitionInfo studyDefinition : studyDefinitions)
		{
			String identifier = StudyDefinitionIdConverter.studyDefinitionIdToOmxIdentifier(studyDefinition.getId());
			Characteristic dataset = Characteristic.findByIdentifier(database, identifier);
			boolean studyDefinitionLoaded = dataset != null;
			models.add(new StudyDefinitionModel(studyDefinition.getId(), studyDefinitionLoaded));
		}

		model.addAttribute("studyDefinitions", models);

		return VIEW_NAME;
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
	@RequestMapping(LOAD_URI)
	public String loadStudyDefinition(@RequestParam(value = "id", required = false)
	String id, Model model) throws DatabaseException
	{
		try
		{
			if (id != null)
			{
				studyDefinitionLoaderService.loadStudyDefinition(id);
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

		return listStudyDefinitions(model);
	}

}
