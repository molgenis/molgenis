package org.molgenis.questionnaires;

import org.molgenis.questionnaires.service.QuestionnaireService;
import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static java.util.Objects.requireNonNull;

@Controller
@RequestMapping(QuestionnaireController.URI)
public class QuestionnaireController extends PluginController
{
	public static final String ID = "questionnaires";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private static final String QUESTIONNAIRE_VIEW = "view-questionnaire";

	private final QuestionnaireService questionnaireService;

	public QuestionnaireController(QuestionnaireService questionnaireService)
	{
		super(URI);
		this.questionnaireService = requireNonNull(questionnaireService);
	}

	@GetMapping
	public String initView(Model model)
	{
		model.addAttribute("questionnaires", questionnaireService.getQuestionnaires());
		return QUESTIONNAIRE_VIEW;
	}

	@ResponseBody
	@GetMapping(value = "/{name}", produces = "application/json")
	public QuestionnaireResponse getQuestionnaire(@PathVariable("name") String name)
	{
		return questionnaireService.getQuestionnare(name);
	}

	//	@GetMapping("/{name}/thanks")
	//	public String showThanks(@PathVariable("name") String name, Model model, HttpServletResponse response)
	//			throws IOException
	//	{
	//		EntityType entityType = dataService.getMeta().getEntityType(name);
	//		if (entityType == null)
	//		{
	//			response.sendError(404);
	//			return null;
	//		}
	//
	//		model.addAttribute("thankYouText", getThankYouText(name));
	//		return "view-thanks";
	//	}
	//
	//	private String getThankYouText(String questionnaireName)
	//	{
	//		return runAsSystem(() -> thankYouTextService.getThankYouText(questionnaireName));
	//	}
}
