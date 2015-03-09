package org.molgenis.questionnaire;

import static org.molgenis.questionnaire.QuestionnaireController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class QuestionnaireController extends MolgenisPluginController
{
	public static final String ID = "questionnaire";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_NAME = "view-questionnaire";
	private static final String PACKAGE_QUESTIONNAIRES = "questionnaires";
	private final DataService dataService;
	private final MetaDataService metaDataService;

	@Autowired
	public QuestionnaireController(DataService dataService, MetaDataService metaDataService)
	{
		super(URI);
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		if (metaDataService == null) throw new IllegalArgumentException("metaDataService is null");
		this.dataService = dataService;
		this.metaDataService = metaDataService;
	}

	@RequestMapping(method = GET)
	public String init()
	{
		return VIEW_NAME;
	}

	@RequestMapping(value = "/questionnaires", method = GET)
	@ResponseBody
	public QuestionnairesResponse getQuestionnaires()
	{
		Package questionnairePackage = metaDataService.getPackage(PACKAGE_QUESTIONNAIRES);

		QuestionnairesResponse questionnaires = new QuestionnairesResponse();
		for (EntityMetaData entityMetaData : questionnairePackage.getEntityMetaDatas())
		{
			String id = entityMetaData.getName();
			String text = entityMetaData.getLabel();
			questionnaires.addQuestionnaire(new QuestionnaireResponse(id, text));
		}
		return questionnaires;
	}

	private static class QuestionnairesResponse
	{
		private final List<QuestionnaireResponse> questionnaires;

		public QuestionnairesResponse()
		{
			this.questionnaires = new ArrayList<QuestionnaireResponse>();
		}

		public void addQuestionnaire(QuestionnaireResponse questionnaire)
		{
			this.questionnaires.add(questionnaire);
		}
	}

	private static class QuestionnaireResponse
	{
		private String id;
		private String text;

		public QuestionnaireResponse(String id, String text)
		{
			this.id = id;
			this.text = text;
		}
	}
}
