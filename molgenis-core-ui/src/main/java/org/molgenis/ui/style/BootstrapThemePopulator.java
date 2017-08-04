package org.molgenis.ui.style;


import org.molgenis.data.DataService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;


import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class BootstrapThemePopulator
{
	private static final String LOCAL_CSS_BOOTSTRAP_THEME_LOCATION = "classpath*:css/bootstrap-*.min.css";

	private final StyleService styleService;

	public BootstrapThemePopulator(StyleService styleService)
	{
		this.styleService = requireNonNull(styleService);
	}

	public void populate() {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		try
		{
			Resource [] resources = resolver.getResources(LOCAL_CSS_BOOTSTRAP_THEME_LOCATION);

			//TODO check if bs 4 style exsists with the same name

			for (Resource resource : resources)
			{
				FileSystemResource fileSystemResource = new FileSystemResource(resource.getFile());

				styleService.addStyles(resource.getFilename(), resource.getFilename(), fileSystemResource.getInputStream(),
						null, null);
			}
		}
		catch (MolgenisStyleException | IOException e)
		{
			e.printStackTrace();
		}

	}
}
