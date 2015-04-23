package org.molgenis.ui.style;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.ui.MolgenisPluginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

public class StyleServiceImpl implements StyleService
{
	@Autowired
	private MolgenisSettings molgenisSettings;

	@Override
	@SuppressWarnings("unchecked")
	public List<Style> getAvailableStyles()
	{
		// TODO check if we can reach api by ping
		List<Style> availableStyles = new ArrayList<Style>();

		RestTemplate restTemplate = new RestTemplate();
		String jsonString = restTemplate.getForObject("http://api.bootswatch.com/3/", String.class, "");

		Gson gson = new Gson();
		Map<String, List<LinkedTreeMap<String, String>>> bootSwatchApiResponse = gson.fromJson(jsonString, Map.class);

		List<LinkedTreeMap<String, String>> themes = bootSwatchApiResponse.get("themes");
		themes.forEach(tree -> availableStyles.add(Style.createRemote(tree.get("css"), tree.get("name"))));

		// TODO Check core-ui css folder for bootstrap-<name>.css to see if there are more styles to use

		return availableStyles;
	}

	@Override
	public void setSelectedStyle(Style style)
	{
		molgenisSettings.setProperty(MolgenisPluginInterceptor.MOLGENIS_CSS_THEME, style.getLocation());
	}

	@Override
	public Style getSelectedStyle()
	{
		Style selectedStyle = null;
		for (Style style : getAvailableStyles())
		{
			if (style.getLocation().equals(molgenisSettings.getProperty(MolgenisPluginInterceptor.MOLGENIS_CSS_THEME)))
			{
				selectedStyle = style;
				return selectedStyle;
			}
		}
		return selectedStyle;
	}
}
