package org.molgenis.questionnaires;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.data.support.QueryImpl.EQ;
import static org.molgenis.questionnaires.QuestionnaireMetaData.ATTR_STATUS;
import static org.molgenis.questionnaires.QuestionnaireStatus.NOT_STARTED;
import static org.molgenis.questionnaires.QuestionnaireStatus.OPEN;
import static org.molgenis.questionnaires.QuestionnaireUtils.findQuestionnairesMetaData;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.*;
import static org.molgenis.security.owned.OwnedEntityType.OWNER_USERNAME;

@Controller
@RequestMapping(QuestionnairePluginController.URI)
public class QuestionnairePluginController extends MolgenisPluginController
{
	public static final String ID = "questionnaires";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final DataService dataService;
	private final ThankYouTextService thankYouTextService;
	private final LanguageService languageService;
	private final EntityManager entityManager;

	@Autowired
	public QuestionnairePluginController(DataService dataService, ThankYouTextService thankYouTextService,
			LanguageService languageService, EntityManager entityManager)
	{
		super(URI);
		this.dataService = requireNonNull(dataService);
		this.thankYouTextService = requireNonNull(thankYouTextService);
		this.languageService = requireNonNull(languageService);
		this.entityManager = requireNonNull(entityManager);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showView(Model model)
	{
		model.addAttribute("questionnaires", getQuestionnaires());
		return "view-my-questionnaires";
	}

	private List<Questionnaire> getQuestionnaires()
	{

		List<Questionnaire> questionnaires;
		List<EntityType> questionnaireMeta = runAsSystem(
				() -> findQuestionnairesMetaData(dataService).collect(toList()));

		questionnaires = questionnaireMeta.stream()
										  .map(EntityType::getId)
										  .filter(name -> currentUserIsSu() || currentUserHasRole(
												  AUTHORITY_ENTITY_WRITE_PREFIX + name))
										  .map(name ->
										  {
											  // Create entity if not yet exists for current user
											  EntityType entityType = dataService.getMeta().getEntityType(name);
											  Entity entity = findQuestionnaireEntity(name);
											  if (entity == null)
											  {
												  entity = createQuestionnaireEntity(entityType, NOT_STARTED, name);
											  }

											  return toQuestionnaireModel(entity, entityType);
										  })
										  .collect(toList());
		return questionnaires;
	}

	@RequestMapping("/{name}")
	public String showQuestionnairForm(@PathVariable("name") String name, Model model, HttpServletResponse response)
			throws IOException
	{
		EntityType entityType = dataService.getMeta().getEntityType(name);
		if (entityType == null)
		{
			response.sendError(404);
			return null;
		}

		// Once we showed the questionnaire it's status is 'OPEN'
		Entity entity = findQuestionnaireEntity(name);
		if (entity == null)
		{
			entity = createQuestionnaireEntity(entityType, OPEN, name);
		}
		else if (entity.getString(ATTR_STATUS).equals(NOT_STARTED.toString()))
		{
			entity.set(ATTR_STATUS, OPEN.toString());
			dataService.update(name, entity);
		}

		model.addAttribute("questionnaire", toQuestionnaireModel(entity, entityType));

		return "view-questionnaire";
	}

	@RequestMapping("/{name}/thanks")
	public String showThanks(@PathVariable("name") String name, Model model, HttpServletResponse response)
			throws IOException
	{
		EntityType entityType = dataService.getMeta().getEntityType(name);
		if (entityType == null)
		{
			response.sendError(404);
			return null;
		}

		model.addAttribute("thankYouText", getThankYouText(name));
		return "view-thanks";
	}

	private synchronized Entity createQuestionnaireEntity(EntityType entityType, QuestionnaireStatus status,
			String name)
	{
		Entity entity = findQuestionnaireEntity(name);
		if (entity == null)
		{
			entity = entityManager.create(entityType, POPULATE);
			entity.set(OWNER_USERNAME, getCurrentUsername());
			entity.set(ATTR_STATUS, status.toString());
			dataService.add(entityType.getId(), entity);
		}
		return entity;
	}

	private Questionnaire toQuestionnaireModel(Entity entity, EntityType entityType)
	{
		QuestionnaireStatus status = QuestionnaireStatus.valueOf(entity.getString(ATTR_STATUS));
		return new Questionnaire(entityType.getId(), entityType.getLabel(languageService.getCurrentUserLanguageCode()),
				status, entityType.getDescription(languageService.getCurrentUserLanguageCode()), entity.getIdValue());
	}

	private Entity findQuestionnaireEntity(String name)
	{
		return dataService.findOne(name, EQ(OWNER_USERNAME, getCurrentUsername()));
	}

	private String getThankYouText(String questionnaireName)
	{
		return runAsSystem(() -> thankYouTextService.getThankYouText(questionnaireName));
	}

	public static class Questionnaire
	{
		private String name;
		private String label;
		private QuestionnaireStatus status;
		private String description;
		private Object id;

		Questionnaire(String name, String label, QuestionnaireStatus status, String description, Object id)
		{
			this.name = name;
			this.label = label;
			this.status = status;
			this.description = description;
			this.id = id;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getLabel()
		{
			return label;
		}

		public void setLabel(String label)
		{
			this.label = label;
		}

		public QuestionnaireStatus getStatus()
		{
			return status;
		}

		public void setStatus(QuestionnaireStatus status)
		{
			this.status = status;
		}

		public String getDescription()
		{
			return description;
		}

		public void setDescription(String description)
		{
			this.description = description;
		}

		public Object getId()
		{
			return id;
		}

		public void setId(Object id)
		{
			this.id = id;
		}
	}
}
