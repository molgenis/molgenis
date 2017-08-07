package org.molgenis.ui.style;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Component
public class BootstrapThemePopulator
{
	private static final Logger LOG = LoggerFactory.getLogger(BootstrapThemePopulator.class);

	private static final String LOCAL_CSS_BOOTSTRAP_3_THEME_LOCATION = "classpath*:css/bootstrap-*.min.css";
	private static final String LOCAL_CSS_BOOTSTRAP_4_THEME_LOCATION = "classpath*:css/bootstrap-4/bootstrap-*.min.css";

	private final StyleService styleService;

	public BootstrapThemePopulator(StyleService styleService)
	{
		this.styleService = requireNonNull(styleService);
	}

	/**
	 * Populate the database with the available bootstrap themes found in the jar. This enables us the release the application
	 * with a set of predefined bootstrap themes.
	 * <p>
	 * If a given bootstrap 3 theme is located a matching bootstrap 4 theme is added the the styleSheet row is present.
	 */
	public void populate()
	{
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		try
		{
			Resource[] bootstrap3Themes = resolver.getResources(LOCAL_CSS_BOOTSTRAP_3_THEME_LOCATION);
			Resource[] bootstrap4Themes = resolver.getResources(LOCAL_CSS_BOOTSTRAP_4_THEME_LOCATION);

			for (Resource bootstrap3Resource : bootstrap3Themes)
			{
				String bootstrap3FileName = bootstrap3Resource.getFilename();
				InputStream bootstrap3Data = bootstrap3Resource.getInputStream();

				String bootstrap4FileName = null;
				InputStream bootstrap4Data = null;
				Optional<Resource> bootstrap4Optional = guessMatchingBootstrap4File(bootstrap4Themes,
						bootstrap3FileName);

				if (bootstrap4Optional.isPresent())
				{
					Resource bootstrap4resource = bootstrap4Optional.get();
					bootstrap4FileName = bootstrap4resource.getFilename();
					bootstrap4Data = bootstrap4resource.getInputStream();
				}

				styleService.addStyles(bootstrap3FileName, bootstrap3FileName, bootstrap3Data, bootstrap4FileName,
						bootstrap4Data);
			}
		}
		catch (MolgenisStyleException | IOException e)
		{
			LOG.error("error populating bootstrap themes");
			e.printStackTrace();
		}

	}

	/**
	 * Guess witch bootstrap4 theme file to combine with the bootstrap 3 theme file based on the file name.
	 * It is assumed all resources where resolved using the "bootstrap-[theme-name].min.css" template.
	 * <p>
	 * Not all of the bootstrap 3 themes have a matching bootstrap 4 theme.
	 */
	private Optional<Resource> guessMatchingBootstrap4File(Resource[] bootstrap4Themes, String bootstrap3ThemeFileName)
	{
		return Arrays.stream(bootstrap4Themes)
				.filter(bootstrap4Theme -> bootstrap3ThemeFileName.equals(bootstrap4Theme.getFilename())).findFirst();
	}

}
