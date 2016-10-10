package org.molgenis.data.vcf;

import org.apache.commons.io.FileUtils;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { VcfRepositoryTest.Config.class })
public class VcfRepositoryTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private VcfAttributes vcfAttrs;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	private static File testData;
	private static File testNoData;

	@BeforeClass
	public static void beforeClass() throws IOException
	{
		InputStream in_data = VcfRepositoryTest.class.getResourceAsStream("/testdata.vcf");
		testData = new File(FileUtils.getTempDirectory(), "testdata.vcf");
		FileCopyUtils.copy(in_data, new FileOutputStream(testData));

		InputStream in_no_data = VcfRepositoryTest.class.getResourceAsStream("/testnodata.vcf");
		testNoData = new File(FileUtils.getTempDirectory(), "testnodata.vcf");
		FileCopyUtils.copy(in_no_data, new FileOutputStream(testNoData));
	}

	@Test
	public void metaData() throws IOException
	{
		try (VcfRepository vcfRepository = new VcfRepository(testData, "testData", vcfAttrs, entityTypeFactory,
				attrMetaFactory))
		{
			assertEquals(vcfRepository.getName(), "testData");
			Iterator<Attribute> it = vcfRepository.getEntityType().getAttributes().iterator();
			assertTrue(it.hasNext());
			testAttribute(it.next(), VcfAttributes.CHROM, STRING);
			assertTrue(it.hasNext());
			// TEXT to handle large insertions/deletions
			testAttribute(it.next(), VcfAttributes.ALT, TEXT);
			assertTrue(it.hasNext());
			testAttribute(it.next(), VcfAttributes.POS, INT);
			assertTrue(it.hasNext());
			// TEXT to handle large insertions/deletions
			testAttribute(it.next(), VcfAttributes.REF, TEXT);
			assertTrue(it.hasNext());
			testAttribute(it.next(), VcfAttributes.FILTER, STRING);
			assertTrue(it.hasNext());
			testAttribute(it.next(), VcfAttributes.QUAL, STRING);
			assertTrue(it.hasNext());
			testAttribute(it.next(), VcfAttributes.ID, STRING);
			assertTrue(it.hasNext());
			testAttribute(it.next(), VcfAttributes.INTERNAL_ID, STRING);
			assertTrue(it.hasNext());
			testAttribute(it.next(), VcfAttributes.INFO, COMPOUND);
			assertTrue(it.hasNext());
		}
	}

	private static void testAttribute(Attribute metadata, String name, AttributeType type)
	{
		assertEquals(metadata.getName(), name);
		assertEquals(metadata.getDataType(), type);
	}

	@Test
	public void iterator() throws IOException
	{
		try (VcfRepository vcfRepository = new VcfRepository(testData, "testData", vcfAttrs, entityTypeFactory,
				attrMetaFactory))
		{
			Iterator<Entity> it = vcfRepository.iterator();

			assertTrue(it.hasNext());
			Entity entity = it.next();
			assertEquals(entity.get(VcfAttributes.CHROM), "1");
			assertEquals(entity.get(VcfAttributes.POS), 565286);

			assertTrue(it.hasNext());
			entity = it.next();
			assertEquals(entity.get(VcfAttributes.CHROM), "1");
			assertEquals(entity.get(VcfAttributes.POS), 2243618);
			assertTrue(it.hasNext());

			assertTrue(it.hasNext());
			entity = it.next();
			assertEquals(entity.get(VcfAttributes.CHROM), "1");
			assertEquals(entity.get(VcfAttributes.POS), 3171929);
			assertTrue(it.hasNext());

			assertTrue(it.hasNext());
			entity = it.next();
			assertEquals(entity.get(VcfAttributes.CHROM), "1");
			assertEquals(entity.get(VcfAttributes.POS), 3172062);

			assertTrue(it.hasNext());
			entity = it.next();
			assertEquals(entity.get(VcfAttributes.CHROM), "1");
			assertEquals(entity.get(VcfAttributes.POS), 3172273);

			assertTrue(it.hasNext());
			entity = it.next();
			assertEquals(entity.get(VcfAttributes.CHROM), "1");
			assertEquals(entity.get(VcfAttributes.POS), 6097450);

			assertTrue(it.hasNext());
			entity = it.next();
			assertEquals(entity.get(VcfAttributes.CHROM), "1");
			assertEquals(entity.get(VcfAttributes.POS), 7569187);

			assertFalse(it.hasNext());
		}
	}

	@Test
	public void iterator_noValues() throws IOException
	{
		try (VcfRepository vcfRepository = new VcfRepository(testNoData, "testNoData", vcfAttrs, entityTypeFactory,
				attrMetaFactory))
		{
			Iterator<Entity> it = vcfRepository.iterator();
			assertFalse(it.hasNext());
		}
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.vcf.model" })
	public static class Config
	{

	}
}
