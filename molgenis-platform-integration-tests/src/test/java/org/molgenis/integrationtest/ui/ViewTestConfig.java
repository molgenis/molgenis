package org.molgenis.integrationtest.ui;

import org.molgenis.data.settings.AppSettings;
import org.molgenis.ui.jobs.JobsController;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.ui.menu.MenuReaderServiceImpl;
import org.molgenis.ui.menumanager.MenuManagerServiceImpl;
import org.molgenis.ui.style.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@Import({ JobsController.class, MenuManagerServiceImpl.class, StyleSheetFactory.class, StyleSheetMetadata.class })
public class ViewTestConfig
{
	@Autowired
	private AppSettings appSettings;

	@Bean
	public MenuReaderService menuReaderService()
	{
		return new MenuReaderServiceImpl(appSettings);
	}

	@Bean
	public StyleService styleService()
	{
		return mock(StyleService.class);
	}

	@Bean
	public ThemeFingerprintRegistry themeFingerprintRegistry() throws IOException, MolgenisStyleException
	{
		ThemeFingerprintRegistry themeFingerprintRegistry = mock(ThemeFingerprintRegistry.class);
		when(themeFingerprintRegistry.getFingerprint(anyString())).thenReturn("");
		return themeFingerprintRegistry;
	}
}
