package org.molgenis.omx.study;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.data.DataService;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.studymanager.OmxStudyDefinition;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

// FIXME tests
public class OmxStudyDefinitionTest
{
	private StudyDataRequest studyDataRequest;

	private DataService dataService;

	@BeforeMethod
	public void setUp()
	{
		studyDataRequest = new StudyDataRequest();
		studyDataRequest.setId(1);
		studyDataRequest.setName("name");

		MolgenisUser molgenisUser = when(mock(MolgenisUser.class).getEmail()).thenReturn("a@b.c").getMock();
		studyDataRequest.setMolgenisUser(molgenisUser);

		Protocol protocol1 = when(mock(Protocol.class).getIdentifier()).thenReturn("feature1").getMock();
		Protocol protocol2 = when(mock(Protocol.class).getIdentifier()).thenReturn("feature2").getMock();
		studyDataRequest.setProtocols(Arrays.asList(protocol1, protocol2));
		dataService = mock(DataService.class);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void OmxStudyDefinition()
	{
		new OmxStudyDefinition(null, dataService);
	}

	@Test
	public void getAuthor()
	{
		assertEquals(new OmxStudyDefinition(studyDataRequest, dataService).getAuthorEmail(), "a@b.c");
	}

	@Test
	public void getId()
	{
		assertEquals(new OmxStudyDefinition(studyDataRequest, dataService).getId(), Integer.valueOf(1).toString());
	}

	@Test
	public void getName()
	{
		assertEquals(new OmxStudyDefinition(studyDataRequest, dataService).getName(), "name");
	}
}
