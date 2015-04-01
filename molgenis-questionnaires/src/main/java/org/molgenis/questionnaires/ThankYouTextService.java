package org.molgenis.questionnaires;

import org.molgenis.ui.controller.StaticContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ThankYouTextService
{
	private final StaticContentService staticContentService;
	private static final String DEFAULT_THANK_YOU_TEXT = "<h3>Thank you for submitting the questionnaire.</h3>";

	@Autowired
	public ThankYouTextService(StaticContentService staticContentService)
	{
		this.staticContentService = staticContentService;
	}

	public String getThankYouText(String questionnaireName)
	{
		String key = getStaticContentKey(questionnaireName);
		String text = staticContentService.getContent(key);

		if (text == null)
		{
			text = DEFAULT_THANK_YOU_TEXT;
			staticContentService.submitContent(key, text);
		}

		return text;
	}

	public void saveThankYouText(String questionnaireName, String text)
	{
		String key = getStaticContentKey(questionnaireName);
		staticContentService.submitContent(key, text);
	}

	private String getStaticContentKey(String questionnaireName)
	{
		return String.format("%s_thankYouText", questionnaireName);
	}
}
