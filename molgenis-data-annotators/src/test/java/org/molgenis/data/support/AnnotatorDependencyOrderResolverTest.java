package org.molgenis.data.support;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class AnnotatorDependencyOrderResolverTest
{
	@Mock
	RepositoryAnnotator annotator1,annotator2,annotator3,annotator4,annotator5;
	@Mock
	AnnotatorInfo annotator1info,annotator2info,annotator3info,annotator4info,annotator5info;

	private AnnotatorDependencyOrderResolver resolver = new AnnotatorDependencyOrderResolver();

	@Mock
	Repository repo;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);

		AttributeMetaData attra = new DefaultAttributeMetaData("A").setDataType(MolgenisFieldTypes.STRING);
		// to check for matching "STRING" attributes to required "TEXT" attributes
		AttributeMetaData attra2 = new DefaultAttributeMetaData("A").setDataType(MolgenisFieldTypes.TEXT);
		AttributeMetaData attrb = new DefaultAttributeMetaData("B").setDataType(MolgenisFieldTypes.STRING);
		AttributeMetaData attrc = new DefaultAttributeMetaData("C").setDataType(MolgenisFieldTypes.STRING);
		AttributeMetaData attrd = new DefaultAttributeMetaData("D").setDataType(MolgenisFieldTypes.STRING);
		AttributeMetaData attre = new DefaultAttributeMetaData("E").setDataType(MolgenisFieldTypes.STRING);
		AttributeMetaData attrf = new DefaultAttributeMetaData("F").setDataType(MolgenisFieldTypes.STRING);
		AttributeMetaData attrg = new DefaultAttributeMetaData("G").setDataType(MolgenisFieldTypes.STRING);
		AttributeMetaData attrh = new DefaultAttributeMetaData("H").setDataType(MolgenisFieldTypes.STRING);
		AttributeMetaData attri = new DefaultAttributeMetaData("I").setDataType(MolgenisFieldTypes.STRING);
		AttributeMetaData attrj = new DefaultAttributeMetaData("J").setDataType(MolgenisFieldTypes.STRING);

		DefaultEntityMetaData emd = new DefaultEntityMetaData("test");
		emd.addAllAttributeMetaData(Arrays.asList(attra, attrb));
		when(repo.getEntityMetaData()).thenReturn(emd);

		when(annotator1.getInfo()).thenReturn(annotator1info);
		when(annotator2.getInfo()).thenReturn(annotator2info);
		when(annotator3.getInfo()).thenReturn(annotator3info);
		when(annotator4.getInfo()).thenReturn(annotator4info);
		when(annotator5.getInfo()).thenReturn(annotator5info);
		when(annotator1.getSimpleName()).thenReturn("annotator1");
		when(annotator2.getSimpleName()).thenReturn("annotator2");
		when(annotator3.getSimpleName()).thenReturn("annotator3");
		when(annotator4.getSimpleName()).thenReturn("annotator4");
		when(annotator5.getSimpleName()).thenReturn("annotator5");

		when(annotator1.getRequiredAttributes()).thenReturn(Arrays.asList(attrd, attrh, attri));
		when(annotator1info.getOutputAttributes()).thenReturn(Arrays.asList(attre, attrc));

		when(annotator2.getRequiredAttributes()).thenReturn(Arrays.asList(attra2, attrb));
		when(annotator2info.getOutputAttributes()).thenReturn(Arrays.asList(attrj));

		when(annotator3.getRequiredAttributes()).thenReturn(Arrays.asList(attra2, attrh));
		when(annotator3info.getOutputAttributes()).thenReturn(Arrays.asList(attrd, attrj));

		when(annotator4.getRequiredAttributes()).thenReturn(Arrays.asList(attra, attrb));
		when(annotator4info.getOutputAttributes()).thenReturn(Arrays.asList(attrh));

		when(annotator5.getRequiredAttributes()).thenReturn(Arrays.asList(attrb, attrd));
		when(annotator5info.getOutputAttributes()).thenReturn(Arrays.asList(attri, attrf));
	}

	@Test
	public void testSucces()
	{
		ArrayList<RepositoryAnnotator> requested = new ArrayList<>();
		requested.add(annotator1);
		requested.add(annotator3);
		requested.add(annotator5);

		ArrayList<RepositoryAnnotator> available = new ArrayList<>();
		available.add(annotator1);
		available.add(annotator2);
		available.add(annotator3);
		available.add(annotator4);
		available.add(annotator5);

		Queue<RepositoryAnnotator> result = resolver.getAnnotatorSelectionDependencyList(available, requested, repo);

		assertEquals(result.size(), 4);
		assertEquals(result.poll().getSimpleName(), "annotator4");
		assertEquals(result.poll().getSimpleName(), "annotator3");
		assertEquals(result.poll().getSimpleName(), "annotator5");
		assertEquals(result.poll().getSimpleName(), "annotator1");

	}

	@Test(expectedExceptions = UnresolvedAnnotatorDependencyException.class)
	public void testFail()
	{
		ArrayList<RepositoryAnnotator> requested = new ArrayList<>();
		requested.add(annotator1);
		requested.add(annotator3);
		requested.add(annotator5);

		ArrayList<RepositoryAnnotator> available = new ArrayList<>();
		available.add(annotator1);
		available.add(annotator2);
		available.add(annotator3);
		available.add(annotator5);

		Queue<RepositoryAnnotator> result = resolver.getAnnotatorSelectionDependencyList(available, requested, repo);
	}

}
