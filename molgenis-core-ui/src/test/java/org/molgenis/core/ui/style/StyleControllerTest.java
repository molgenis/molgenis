package org.molgenis.core.ui.style;

import org.mockito.Mock;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
public class StyleControllerTest
{

	@Mock
	private StyleService styleService;

	private MockMvc mockMvc;

	@BeforeMethod
	public void before()
	{
		initMocks(this);
		StyleController styleController = new StyleController(styleService);

		mockMvc = MockMvcBuilders.standaloneSetup(styleController).build();
	}

	@Test
	public void testGetTheme() throws Exception
	{
		FileSystemResource themeResource = mock(FileSystemResource.class);
		InputStream inputStream = new ByteArrayInputStream("yo, it's a style, oke ".getBytes(StandardCharsets.UTF_8));
		when(themeResource.getInputStream()).thenReturn(inputStream);
		when(styleService.getThemeData("bootstrap-molgenis.min.css", BootstrapVersion.BOOTSTRAP_VERSION_3)).thenReturn(
				themeResource);

		mockMvc.perform(get("/css/bootstrap-3/bootstrap-molgenis.min.css").with(anonymous())).
				andExpect(status().isOk());

	}

	@Test
	public void testGetBs4Theme() throws Exception
	{
		FileSystemResource themeResource = mock(FileSystemResource.class);
		InputStream inputStream = new ByteArrayInputStream("next level style ".getBytes(StandardCharsets.UTF_8));
		when(themeResource.getInputStream()).thenReturn(inputStream);
		when(styleService.getThemeData("bootstrap-molgenis.min.css", BootstrapVersion.BOOTSTRAP_VERSION_4)).thenReturn(
				themeResource);

		mockMvc.perform(get("/css/bootstrap-4/bootstrap-molgenis.min.css").with(anonymous())).
				andExpect(status().isOk());

	}
}
