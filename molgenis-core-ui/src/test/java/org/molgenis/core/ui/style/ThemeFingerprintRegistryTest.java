package org.molgenis.core.ui.style;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ThemeFingerprintRegistryTest
{
	private StyleService styleService;
	private ThemeFingerprintRegistry themeFingerprintRegistry;

	@BeforeClass
	public void setUp()
	{
		styleService = mock(StyleService.class);
		themeFingerprintRegistry = new ThemeFingerprintRegistry(styleService);
	}

	@Test
	public void getFingerprint() throws IOException, MolgenisStyleException
	{
		String theme = "bootstrap-theme-name.min.css";
		String version = "bootstrap-3";
		String themeUri = "css/theme/" + version + "/" + theme;
		FileSystemResource themeFile = mock(FileSystemResource.class);
		InputStream themeDataStream = IOUtils.toInputStream("yo yo yo data");
		when(themeFile.getInputStream()).thenReturn(themeDataStream);
		when(styleService.getThemeData(theme, BootstrapVersion.BOOTSTRAP_VERSION_3)).thenReturn(themeFile);

		// first call
		String firstResult = themeFingerprintRegistry.getFingerprint(themeUri);

		assertNotNull(firstResult);
		verify(styleService).getThemeData(theme, BootstrapVersion.BOOTSTRAP_VERSION_3);

		// second call
		String secondResult = themeFingerprintRegistry.getFingerprint(themeUri);
		verifyNoMoreInteractions(styleService);

		// verify stable key
		assertEquals(firstResult, secondResult);
	}
}
