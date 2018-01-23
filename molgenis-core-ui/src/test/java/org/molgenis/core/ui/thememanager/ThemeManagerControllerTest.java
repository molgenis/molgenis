package org.molgenis.core.ui.thememanager;

import org.mockito.Mock;
import org.molgenis.core.ui.style.Style;
import org.molgenis.core.ui.style.StyleService;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
public class ThemeManagerControllerTest
{
	@Mock
	private StyleService styleService;

	private MockMvc mockMvc;

	private String bootstrap3FileName = "bs3-file-name.min.css";
	private String bootstrap4FileName = "bs4-file-name.min.css";
	private MockMultipartFile bs3File, bs4File;

	@BeforeMethod
	public void before()
	{
		initMocks(this);
		ThemeManagerController themeManagerController = new ThemeManagerController(styleService);

		mockMvc = MockMvcBuilders.standaloneSetup(themeManagerController).build();

		byte[] bs3Data = "bootstrap3-content".getBytes(StandardCharsets.UTF_8);
		byte[] bs4Data = "bootstrap4-content".getBytes(StandardCharsets.UTF_8);

		bs3File = new MockMultipartFile("bootstrap3-style", bootstrap3FileName, "some-content-type", bs3Data);
		bs4File = new MockMultipartFile("bootstrap4-style", bootstrap4FileName, "some-content-type", bs4Data);
	}

	@Test
	public void addBootstrap3ThemeOnly() throws Exception
	{
		Style newStyle = Style.createLocal("new-style");
		when(styleService.addStyle(eq(bootstrap3FileName), eq(bootstrap3FileName), any(), any(), any())).thenReturn(
				newStyle);

		mockMvc.perform(fileUpload(ThemeManagerController.URI + "/add-bootstrap-theme").file(bs3File))
			   .andExpect(status().isOk());

		verify(styleService).addStyle(eq(bootstrap3FileName), eq(bootstrap3FileName), any(), any(), any());

	}

	@Test
	public void addBootstrap3and4Theme() throws Exception
	{
		Style newStyle = Style.createLocal("new-style");
		when(styleService.addStyle(eq(bootstrap3FileName), eq(bootstrap3FileName), any(), eq(bootstrap4FileName),
				any())).thenReturn(newStyle);

		mockMvc.perform(fileUpload(ThemeManagerController.URI + "/add-bootstrap-theme").file(bs3File)
																					   .file(bs4File)
																					   .with(user("admin").roles("SU")))
			   .
					   andExpect(status().isOk());

		verify(styleService).addStyle(eq(bootstrap3FileName), eq(bootstrap3FileName), any(), eq(bootstrap4FileName),
				any());

	}
}
