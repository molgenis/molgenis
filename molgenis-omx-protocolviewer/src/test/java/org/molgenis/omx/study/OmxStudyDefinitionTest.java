package org.molgenis.omx.study;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.filter.StudyDataRequest;
import org.molgenis.omx.observ.ObservableFeature;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OmxStudyDefinitionTest
{
	private StudyDataRequest studyDataRequest;

	@BeforeMethod
	public void setUp()
	{
		studyDataRequest = new StudyDataRequest();
		studyDataRequest.setIdentifier("identifier");
		studyDataRequest.setName("name");

		MolgenisUser molgenisUser = when(mock(MolgenisUser.class).getEmail()).thenReturn("a@b.c").getMock();
		studyDataRequest.setMolgenisUser(molgenisUser);

		ObservableFeature feature1 = when(mock(ObservableFeature.class).getIdentifier()).thenReturn("feature1")
				.getMock();
		ObservableFeature feature2 = when(mock(ObservableFeature.class).getIdentifier()).thenReturn("feature2")
				.getMock();
		studyDataRequest.setFeatures(Arrays.asList(feature1, feature2));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void OmxStudyDefinition()
	{
		new OmxStudyDefinition(null);
	}

	@Test
	public void getAuthor()
	{
		assertEquals(new OmxStudyDefinition(studyDataRequest).getAuthor().getEmail(), "a@b.c");
	}

	@Test
	public void getFeatures()
	{
		Iterator<ObservableFeature> features = new OmxStudyDefinition(studyDataRequest).getFeatures().iterator();
		assertTrue(features.hasNext());
		assertEquals(features.next().getIdentifier(), "feature1");
		assertTrue(features.hasNext());
		assertEquals(features.next().getIdentifier(), "feature2");
		assertFalse(features.hasNext());
	}

	@Test
	public void getId()
	{
		assertEquals(new OmxStudyDefinition(studyDataRequest).getId(), "identifier");
	}

	@Test
	public void getName()
	{
		assertEquals(new OmxStudyDefinition(studyDataRequest).getName(), "name");
	}
}
