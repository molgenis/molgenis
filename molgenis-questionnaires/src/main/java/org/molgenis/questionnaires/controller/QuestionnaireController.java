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
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.OK;

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

	@GetMapping("/**")
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

	@GetMapping(value = "/start/{id}")
	@ResponseStatus(value = OK)
	public void startQuestionnaire(@PathVariable("id") String id)
	{
		questionnaireService.startQuestionnaire(id);
	}

	@ResponseBody
	@GetMapping("/{id}/thanks")
	public String getQuestionnaireSubmissionText(@PathVariable("id") String id)
	{
		return questionnaireService.getQuestionnaireSubmissionText(id);
	}
}
