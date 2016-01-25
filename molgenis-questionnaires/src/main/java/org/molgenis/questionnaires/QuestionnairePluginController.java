package org.molgenis.questionnaires;

import static org.molgenis.data.support.QueryImpl.EQ;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_ENTITY_WRITE_PREFIX;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(QuestionnairePluginController.URI)
public class QuestionnairePluginController extends MolgenisPluginController
{
	public static final String ID = "questionnaires";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final DataService dataService;
	private final ThankYouTextService thankYouTextService;
	private final LanguageService languageService;

	@Autowired
	public QuestionnairePluginController(DataService dataService, ThankYouTextService thankYouTextService,
			LanguageService languageService)
	{
		super(URI);
		this.dataService = dataService;
		this.thankYouTextService = thankYouTextService;
		this.languageService = languageService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showView(Model model)
	{
		List<Entity> questionnaireMeta = runAsSystem(() -> QuestionnaireUtils.findQuestionnairesMetaData(dataService)
				.collect(Collectors.toList()));

		List<Questionnaire> questionnaires = questionnaireMeta
				.stream()
				.map(e -> e.getString(EntityMetaDataMetaData.FULL_NAME))
				.filter(name -> SecurityUtils.currentUserIsSu()
						|| SecurityUtils.currentUserHasRole(AUTHORITY_ENTITY_WRITE_PREFIX + name.toUpperCase()))
				.map(name -> {
					// Create entity if not yet exists for current user
					EntityMetaData emd = dataService.getMeta().getEntityMetaData(name);
					Entity entity = findQuestionnaireEntity(name);
					if (entity == null) entity = createQuestionnaireEntity(emd, QuestionnaireStatus.NOT_STARTED);

					return toQuestionnaireModel(entity, emd);
				}).collect(Collectors.toList());

		model.addAttribute("questionnaires", questionnaires);

		return "view-my-questionnaires";
	}

	@RequestMapping("/{name}")
	public String showQuestionnairForm(@PathVariable("name") String name, Model model, HttpServletResponse response)
			throws IOException
	{
		EntityMetaData emd = dataService.getMeta().getEntityMetaData(name);
		if (emd == null)
		{
			response.sendError(404);
			return null;
		}

		// Once we showed the questionnaire it's status is 'OPEN'
		Entity entity = findQuestionnaireEntity(name);
		if (entity == null)
		{
			entity = createQuestionnaireEntity(emd, QuestionnaireStatus.OPEN);
		}
		else if (entity.getString(QuestionnaireMetaData.ATTR_STATUS).equals(QuestionnaireStatus.NOT_STARTED.toString()))
		{
			entity.set(QuestionnaireMetaData.ATTR_STATUS, QuestionnaireStatus.OPEN);
			dataService.update(name, entity);
		}

		model.addAttribute("questionnaire", toQuestionnaireModel(entity, emd));

		return "view-questionnaire";
	}

	@RequestMapping("/{name}/thanks")
	public String showThanks(@PathVariable("name") String name, Model model, HttpServletResponse response)
			throws IOException
	{
		EntityMetaData emd = dataService.getMeta().getEntityMetaData(name);
		if (emd == null)
		{
			response.sendError(404);
			return null;
		}

		model.addAttribute("thankYouText", getThankYouText(name));
		return "view-thanks";
	}

	private Entity createQuestionnaireEntity(EntityMetaData emd, QuestionnaireStatus status)
	{
		Entity entity = new DefaultEntity(emd, dataService);
		entity.set(OwnedEntityMetaData.ATTR_OWNER_USERNAME, SecurityUtils.getCurrentUsername());
		entity.set(QuestionnaireMetaData.ATTR_STATUS, status.toString());
		dataService.add(emd.getName(), entity);

		return entity;
	}

	private Questionnaire toQuestionnaireModel(Entity entity, EntityMetaData emd)
	{
		QuestionnaireStatus status = QuestionnaireStatus.valueOf(entity.getString(QuestionnaireMetaData.ATTR_STATUS));
		return new Questionnaire(emd.getName(), emd.getLabel(languageService.getCurrentUserLanguageCode()), status,
				emd.getDescription(languageService.getCurrentUserLanguageCode()), entity.getIdValue());
	}

	private Entity findQuestionnaireEntity(String name)
	{
		return dataService.findOne(name,
				EQ(OwnedEntityMetaData.ATTR_OWNER_USERNAME, SecurityUtils.getCurrentUsername()));
	}

	public String getThankYouText(String questionnaireName)
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

		public Questionnaire(String name, String label, QuestionnaireStatus status, String description, Object id)
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
