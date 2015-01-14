package org.molgenis.omx.protocolviewer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;

import org.molgenis.catalog.CatalogFolder;
import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.protocolviewer.ProtocolViewerController.SelectedItemsResponse;
import org.molgenis.search.SearchService;
import org.molgenis.study.StudyDefinition;
import org.molgenis.study.UnknownStudyDefinitionException;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ProtocolViewerControllerTest
{

	private ProtocolViewerController protocolViewerController;
	private ProtocolViewerService protocolViewerService;
	private MolgenisSettings molgenisSettings;
	private SearchService searchService;
	private Gson gson = new GsonBuilder().setDateFormat(MolgenisDateFormat.DATEFORMAT_DATETIME).disableHtmlEscaping()
			.setPrettyPrinting().create();

	@BeforeTest
	public void createController()
	{
		protocolViewerService = mock(ProtocolViewerService.class);
		molgenisSettings = mock(MolgenisSettings.class);
		searchService = mock(SearchService.class);
		protocolViewerController = new ProtocolViewerController(protocolViewerService, molgenisSettings, searchService);
	}

	@Test
	public void testGetSelectionNoExcludes() throws UnknownStudyDefinitionException, UnknownCatalogException,
			IOException
	{
		SecurityContextHolder.getContext().setAuthentication(
				new TestingAuthenticationToken("test", "credentials", "ROLE_USER"));
		StudyDefinition studyDefinition = mock(StudyDefinition.class);

		when(protocolViewerService.getStudyDefinitionDraftForCurrentUser("123")).thenReturn(studyDefinition);

		CatalogFolder lifelines = mock(CatalogFolder.class);
		when(lifelines.getId()).thenReturn("ll");
		CatalogFolder group = mock(CatalogFolder.class);
		when(group.getId()).thenReturn("group");
		CatalogFolder folder1 = mock(CatalogFolder.class);
		when(folder1.getId()).thenReturn("1");
		when(folder1.getGroup()).thenReturn("Lifelines->Group->Folder 1");
		when(folder1.getPath()).thenReturn(Arrays.asList(lifelines, group, folder1));
		CatalogFolder folder2 = mock(CatalogFolder.class);
		when(folder2.getId()).thenReturn("2");
		when(folder2.getGroup()).thenReturn("Lifelines->Group->Folder 2");
		when(folder2.getPath()).thenReturn(Arrays.asList(lifelines, group, folder2));
		when(studyDefinition.getItems()).thenReturn(Arrays.asList(folder1, folder2));

		SelectedItemsResponse response = protocolViewerController.getSelection(123, 0, 10000, null);

		Assert.assertEquals(gson.toJson(response), Resources.toString(
				Resources.getResource("org/molgenis/omx/protocolviewer/selectionResponse.json"), Charsets.UTF_8));
	}
}
