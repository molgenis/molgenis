package org.molgenis.ui.style;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.ui.MolgenisPluginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
public class StyleServiceImpl implements StyleService
{
	private static final String LOCAL_CSS_BOOTSTRAP_THEME_LOCATION = "css/themes/bootstrap-*.min.css";
	private static final String CSS_THEME_KEY = MolgenisPluginInterceptor.MOLGENIS_CSS_THEME;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Override
	public Set<Style> getAvailableStyles()
	{
		Set<Style> availableStyles = new HashSet<Style>();

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
			molgenisSettings.setProperty(CSS_THEME_KEY, getStyle(styleName).getLocation());
		}
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
