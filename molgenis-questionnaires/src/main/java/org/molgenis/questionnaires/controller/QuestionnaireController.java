package org.molgenis.questionnaires.controller;

import org.molgenis.core.ui.controller.VuePluginController;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.questionnaires.response.QuestionnaireResponse;
import org.molgenis.questionnaires.service.QuestionnaireService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Controller
@RequestMapping(QuestionnaireController.URI)
public class QuestionnaireController extends VuePluginController
{
	public static final String ID = "questionnaires";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private static final String QUESTIONNAIRE_VIEW = "view-questionnaire";

	private final QuestionnaireService questionnaireService;

	public QuestionnaireController(QuestionnaireService questionnaireService, MenuReaderService menuReaderService,
			AppSettings appSettings, UserAccountService userAccountService)
	{
		super(URI, menuReaderService, appSettings, userAccountService);
		this.questionnaireService = requireNonNull(questionnaireService);
	}

	@GetMapping
	public String initView(Model model)
	{
		super.init(model, ID);
		return QUESTIONNAIRE_VIEW;
	}

	@ResponseBody
	@GetMapping(value = "/list")
	public List<QuestionnaireResponse> getQuestionnaires()
	{
		return questionnaireService.getQuestionnaires();
	}

	@ResponseBody
	@GetMapping(value = "/{name}")
	public QuestionnaireResponse getQuestionnaire(@PathVariable("name") String name)
	{
		return questionnaireService.getQuestionnaire(name);
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
