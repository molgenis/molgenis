package org.molgenis.questionnaires;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Static content editor for the questionnaires thank you page
 */
@Controller
@RequestMapping(ThankYouPageEditorController.URI)
public class ThankYouPageEditorController extends MolgenisPluginController
{
	public static final String ID = "questionnaireThankYouPage";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final ThankYouTextService thankYouTextService;
	private final DataService dataService;

	@Autowired
	public ThankYouPageEditorController(DataService dataService, ThankYouTextService thankYouTextService)
	{
		super(URI);
		this.thankYouTextService = thankYouTextService;
		this.dataService = dataService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String view(@RequestParam(value = "questionnaireName", required = false) String questionnaireName,
			@RequestParam(value = "edit", required = false, defaultValue = "false") String edit, Model model,
			HttpServletResponse response) throws IOException
	{
		if ((questionnaireName != null) && dataService.getMeta().getEntityType(questionnaireName) == null)
		{
			response.sendError(404);
			return null;
		}
		if (edit.equalsIgnoreCase("true") && (questionnaireName != null)) model.addAttribute("edit", true);

		List<EntityType> questionnaires = QuestionnaireUtils.findQuestionnairesMetaData(dataService)
															.collect(Collectors.toList());
		model.addAttribute("questionnaires", questionnaires);

		if ((questionnaireName == null) && !questionnaires.isEmpty())
		{
			questionnaireName = questionnaires.get(0).getId();
		}

		model.addAttribute("content", thankYouTextService.getThankYouText(questionnaireName));
		model.addAttribute("selectedQuestionnaire", questionnaireName);

		return "view-thank-you-text";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String save(@RequestParam("questionnaireName") String questionnaireName,
			@RequestParam("content") String content, Model model)
	{
		if (dataService.getMeta().getEntityType(questionnaireName) != null)
		{
			thankYouTextService.saveThankYouText(questionnaireName, content);
		}

		List<Entity> questionnaires = QuestionnaireUtils.findQuestionnairesMetaData(dataService)
														.collect(Collectors.toList());
		model.addAttribute("questionnaires", questionnaires);
		model.addAttribute("content", content);
		model.addAttribute("selectedQuestionnaire", questionnaireName);

		return "view-thank-you-text";
	}
}
