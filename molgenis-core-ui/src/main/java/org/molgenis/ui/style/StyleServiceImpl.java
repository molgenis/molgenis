package org.molgenis.ui.style;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.ui.MolgenisPluginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

@Component
public class StyleServiceImpl implements StyleService
{
	private static final String LOCAL_CSS_BOOTSTRAP_THEME_LOCATION = "css/themes/bootstrap-*.css";
	private static final String THEME_NAME_KEY = "name";
	private static final String CSS_MIN_KEY = "cssMin";
	private static final String THEMES_KEY = "themes";
	private static final String BOOTSWATCH_API_URL = "http://api.bootswatch.com/3/";
	private static final String CSS_THEME_KEY = MolgenisPluginInterceptor.MOLGENIS_CSS_THEME;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Override
	@SuppressWarnings("unchecked")
	public List<Style> getAvailableStyles()
	{
		List<Style> availableStyles = new ArrayList<Style>();

		RestTemplate restTemplate = new RestTemplate();
		String jsonString = restTemplate.getForObject(BOOTSWATCH_API_URL, String.class, "");

		Gson gson = new Gson();
		Map<String, List<LinkedTreeMap<String, String>>> bootSwatchApiResponse = gson.fromJson(jsonString, Map.class);

		List<LinkedTreeMap<String, String>> themes = bootSwatchApiResponse.get(THEMES_KEY);
		themes.forEach(tree -> availableStyles.add(Style.createRemote(tree.get(CSS_MIN_KEY), tree.get(THEME_NAME_KEY))));

		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		try
		{
			Resource[] resources = resolver.getResources(LOCAL_CSS_BOOTSTRAP_THEME_LOCATION);

			for (Resource resource : resources)
			{
				availableStyles.add(Style.createLocal(resource.getFilename()));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return availableStyles;
	}

	@Override
	public void setSelectedStyle(String styleName)
	{
		molgenisSettings.setProperty(CSS_THEME_KEY, getStyle(styleName).getLocation());
	}

	@Override
	public Style getSelectedStyle()
	{
		for (Style style : getAvailableStyles())
		{
			if (style.getLocation().equals(molgenisSettings.getProperty(CSS_THEME_KEY)))
			{
				return style;
			}
		}
		return null;
	}

	@Override
	public Style getStyle(String styleName)
	{
		try
		{
			for (Style style : getAvailableStyles())
			{
				if (style.getName().equals(styleName))
				{
					return style;
				}
			}
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException(e + " Selected style was not found");
		}

		return null;
	}
}
