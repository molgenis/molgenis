package org.molgenis.data.annotation.core.resources.impl.tabix;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.data.vcf.config.VcfTestConfig;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.vcf.model.VcfAttributes.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { TabixRepositoryTest.Config.class })
public class TabixRepositoryTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private AttributeFactory attributeFactory;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private VcfAttributes vcfAttributes;

	private TabixRepository tabixRepository;
	private EntityType repoMetaData;

	@BeforeClass
	public void beforeClass() throws IOException
	{
		repoMetaData = entityTypeFactory.create("CaddTest");
		repoMetaData.addAttribute(vcfAttributes.getChromAttribute());
		repoMetaData.addAttribute(vcfAttributes.getPosAttribute());
		repoMetaData.addAttribute(vcfAttributes.getRefAttribute());
		repoMetaData.addAttribute(vcfAttributes.getAltAttribute());
		repoMetaData.addAttribute(attributeFactory.create().setName("CADD").setDataType(DECIMAL));
		repoMetaData.addAttribute(attributeFactory.create().setName("CADD_SCALED").setDataType(DECIMAL));
		repoMetaData.addAttribute(attributeFactory.create().setName("id").setVisible(false));
		File file = ResourceUtils.getFile(getClass(), "/cadd_test.vcf.gz");
		tabixRepository = new TabixRepository(file, repoMetaData);
	}

	@Test
	public void testGetEntityType()
	{
		assertEquals(tabixRepository.getEntityType(), repoMetaData);
	}

	@Test
	public void testQuery()
	{
		Query<Entity> query = tabixRepository.query().eq(VcfAttributes.CHROM, "1").and().eq(VcfAttributes.POS, "100");
		Iterator<Entity> it = tabixRepository.findAll(query).iterator();
		assertTrue(EntityUtils.equals(it.next(), newEntity("1", 100, "C", "T", -0.03, 2.003)));
		assertTrue(EntityUtils.equals(it.next(), newEntity("1", 100, "C", "G", -0.4, 4.321)));
		assertTrue(EntityUtils.equals(it.next(), newEntity("1", 100, "C", "A", 2.102, 43.2)));
	}

	/**
	 * If the chromosome send to the TabixIterator is unknown in the inputfile the TabixIterator throws an
	 * IndexOutOfBoundsException We want to log this, but we don't want the annotationrun to fail The most frequent
	 * example of this is a Variant found in the Mitochondrial DNA, chrom=MT, these are not in our Tabix file for for
	 * example CADD. This test checks if the Annotator does not throw an exception but returns an empty list instead.
	 */
	@Test
	public void testUnknownChromosome()
	{
		Query<Entity> query = tabixRepository.query().eq(VcfAttributes.CHROM, "MT").and().eq(VcfAttributes.POS, "100");
		assertEquals(tabixRepository.findAll(query).collect(toList()), emptyList());
	}

	@Test
	public void testIterator()
	{
		Iterator<Entity> it = tabixRepository.iterator();
		assertTrue(EntityUtils.equals(it.next(), newEntity("1", 100, "C", "T", -0.03, 2.003)));
		assertTrue(EntityUtils.equals(it.next(), newEntity("1", 100, "C", "G", -0.4, 4.321)));
		assertTrue(EntityUtils.equals(it.next(), newEntity("1", 100, "C", "A", 2.102, 43.2)));
		assertTrue(EntityUtils.equals(it.next(), newEntity("2", 200, "A", "T", 2.0, 3.012)));
		assertTrue(EntityUtils.equals(it.next(), newEntity("2", 200, "A", "G", -2.30, 20.2)));
		assertTrue(EntityUtils.equals(it.next(), newEntity("3", 300, "G", "A", 0.2, 23.1)));
		assertTrue(EntityUtils.equals(it.next(), newEntity("3", 300, "G", "T", -2.4, 0.123)));
		assertTrue(EntityUtils.equals(it.next(), newEntity("3", 300, "G", "X", -0.002, 2.3)));
		assertTrue(EntityUtils.equals(it.next(), newEntity("3", 300, "G", "C", 0.5, 14.5)));
		assertTrue(EntityUtils.equals(it.next(), newEntity("3", 300, "GC", "A", 1.2, 24.1)));
		assertTrue(EntityUtils.equals(it.next(), newEntity("3", 300, "GC", "T", -3.4, 1.123)));
		assertTrue(EntityUtils.equals(it.next(), newEntity("3", 300, "C", "GX", -1.002, 3.3)));
		assertTrue(EntityUtils.equals(it.next(), newEntity("3", 300, "C", "GC", 1.5, 15.5)));
	}

	private Entity newEntity(String chrom, int pos, String ref, String alt, double cadd, double caddScaled)
	{
		Entity result = new DynamicEntity(repoMetaData);
		result.set(CHROM, chrom);
		result.set(POS, pos);
		result.set(REF, ref);
		result.set(ALT, alt);
		result.set("CADD", cadd);
		result.set("CADD_SCALED", caddScaled);
		return result;
	}

	@Configuration
	@Import({ VcfTestConfig.class })
	public static class Config
	{
	}

}