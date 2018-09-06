package org.molgenis.questionnaires.controller;

import static java.util.Objects.requireNonNull;
import static org.molgenis.questionnaires.meta.QuestionnaireStatus.NOT_STARTED;

import java.util.List;
import java.util.stream.Collectors;
import org.molgenis.core.ui.controller.VuePluginController;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.questionnaires.meta.Questionnaire;
import org.molgenis.questionnaires.meta.QuestionnaireStatus;
import org.molgenis.questionnaires.response.QuestionnaireResponse;
import org.molgenis.questionnaires.service.QuestionnaireService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(QuestionnaireController.URI)
public class QuestionnaireController extends VuePluginController {
  public static final String ID = "questionnaires";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

  private static final String QUESTIONNAIRE_VIEW = "view-questionnaire";

  private final QuestionnaireService questionnaireService;

  public QuestionnaireController(
      QuestionnaireService questionnaireService,
      MenuReaderService menuReaderService,
      AppSettings appSettings,
      UserAccountService userAccountService) {
    super(URI, menuReaderService, appSettings, userAccountService);
    this.questionnaireService = requireNonNull(questionnaireService);
  }

  /** Loads the questionnaire view */
  @GetMapping("/**")
  public String initView(Model model) {
    super.init(model, ID);
    model.addAttribute("username", super.userAccountService.getCurrentUser().getUsername());
    return QUESTIONNAIRE_VIEW;
  }

  /**
   *
   *
   * <h1>Internal Questionnaire API</h1>
   *
   * Retrieves a list of all the available questionnaires
   *
   * @return A list of {@link QuestionnaireResponse}
   */
  @ResponseBody
  @GetMapping(value = "/list")
  public List<QuestionnaireResponse> getQuestionnaires() {
    return questionnaireService
        .getQuestionnaires()
        .map(this::createQuestionnaireResponse)
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <h1>Internal Questionnaire API</h1>
   *
   * Starts a questionnaire
   *
   * @param id A questionnaire ID
   */
  @GetMapping(value = "/start/{id}")
  @ResponseBody
  public QuestionnaireResponse startQuestionnaire(@PathVariable("id") String id) {
    return questionnaireService.startQuestionnaire(id);
  }

  /**
   *
   *
   * <h1>Internal Questionnaire API</h1>
   *
   * Retrieves a submission text for a questionnaire
   *
   * @param id A questionnaire ID
   * @return A "thank you" text shown on submit of a questionnaire
   */
  @ResponseBody
  @GetMapping("/submission-text/{id}")
  public String getQuestionnaireSubmissionText(@PathVariable("id") String id) {
    return questionnaireService.getQuestionnaireSubmissionText(id);
  }

  /**
   * Create a {@link QuestionnaireResponse} based on an {@link EntityType} Will set status to {@link
   * QuestionnaireStatus}.OPEN if there is a data entry for the current user.
   *
   * @param entityType A Questionnaire EntityType
   * @return A {@link QuestionnaireResponse}
   */
  private QuestionnaireResponse createQuestionnaireResponse(EntityType entityType) {
    String entityTypeId = entityType.getId();

    QuestionnaireStatus status = NOT_STARTED;
    Questionnaire questionnaireEntity = questionnaireService.findQuestionnaireEntity(entityTypeId);
    if (questionnaireEntity != null) {
      status = questionnaireEntity.getStatus();
    }

    String lng = this.getLanguageCode();

    return QuestionnaireResponse.create(
        entityTypeId, entityType.getLabel(lng), entityType.getDescription(lng), status);
  }
}
