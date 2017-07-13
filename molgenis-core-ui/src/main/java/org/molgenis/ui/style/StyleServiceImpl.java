package org.molgenis.ui.style;

import org.molgenis.data.settings.AppSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Component
public class StyleServiceImpl implements StyleService
{
	private static final String LOCAL_CSS_BOOTSTRAP_THEME_LOCATION = "classpath*:css/bootstrap-*.min.css";

	private final AppSettings appSettings;

	@Autowired
	public StyleServiceImpl(AppSettings appSettings)
	{
		this.appSettings = requireNonNull(appSettings);
	}

	@Override
	public Set<Style> getAvailableStyles()
	{
		Set<Style> availableStyles = new HashSet<>();

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
		// Pressing save in the UI without doing a selection returns undefined
		if (!styleName.equals("undefined"))
		{
			String bootstrapTheme = getStyle(styleName).getLocation();
			appSettings.setBootstrapTheme(bootstrapTheme);
		}
	}

	@Override
	public Style getSelectedStyle()
	{
		for (Style style : getAvailableStyles())
		{
			String bootstrapTheme = appSettings.getBootstrapTheme();
			if (style.getLocation().equals(bootstrapTheme))
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
