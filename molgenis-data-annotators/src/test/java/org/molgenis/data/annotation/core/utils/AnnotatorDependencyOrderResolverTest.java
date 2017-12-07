package org.molgenis.data.annotation.core.utils;

import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.config.EffectsTestConfig;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.exception.UnresolvedAnnotatorDependencyException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.config.VcfTestConfig;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { AnnotatorDependencyOrderResolverTest.Config.class })
public class AnnotatorDependencyOrderResolverTest extends AbstractMolgenisSpringTest
{
	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	VcfAttributes vcfAttributes;
	@Mock
	RepositoryAnnotator annotator1, annotator2, annotator3, annotator4, annotator5;
	@Mock
	AnnotatorInfo annotator1info, annotator2info, annotator3info, annotator4info, annotator5info;

	private AnnotatorDependencyOrderResolver resolver = new AnnotatorDependencyOrderResolver();

	@Mock
	Repository<Entity> repo;

	public AnnotatorDependencyOrderResolverTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		Attribute attra = attributeFactory.create().setName("A").setDataType(STRING);
		// to check for matching "STRING" attributes to required "TEXT" attributes
		Attribute attra2 = attributeFactory.create().setName("A").setDataType(TEXT);
		Attribute attrb = attributeFactory.create().setName("B").setDataType(STRING);
		Attribute attrc = attributeFactory.create().setName("C").setDataType(STRING);
		Attribute attrd = attributeFactory.create().setName("D").setDataType(STRING);
		Attribute attre = attributeFactory.create().setName("E").setDataType(STRING);
		Attribute attrf = attributeFactory.create().setName("F").setDataType(STRING);
		Attribute attrg = attributeFactory.create().setName("G").setDataType(STRING);
		Attribute attrh = attributeFactory.create().setName("H").setDataType(STRING);
		Attribute attri = attributeFactory.create().setName("I").setDataType(STRING);
		Attribute attrj = attributeFactory.create().setName("J").setDataType(STRING);

		EntityType emd = entityTypeFactory.create("test");
		emd.addAttributes(Arrays.asList(attra, attrb));
		when(repo.getEntityType()).thenReturn(emd);

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

		Queue<RepositoryAnnotator> result = resolver.getAnnotatorSelectionDependencyList(available, requested, repo,
				entityTypeFactory);

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

		Queue<RepositoryAnnotator> result = resolver.getAnnotatorSelectionDependencyList(available, requested, repo,
				entityTypeFactory);
	}

	@Configuration
	@Import({ VcfTestConfig.class, EffectsTestConfig.class })
	public static class Config
	{
	}
}
