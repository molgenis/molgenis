package org.molgenis.data.vcf;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.vcf.test.AbstractMolgenisSpringTest;
import org.molgenis.fieldtypes.FieldType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class VcfRepositoryTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

	private static File testdata;
	private static File testnodata;

	@BeforeClass
	public static void beforeClass() throws IOException
	{
		InputStream in_data = VcfRepositoryTest.class.getResourceAsStream("/testdata.vcf");
		testdata = new File(FileUtils.getTempDirectory(), "testdata.vcf");
		FileCopyUtils.copy(in_data, new FileOutputStream(testdata));

		InputStream in_no_data = VcfRepositoryTest.class.getResourceAsStream("/testnodata.vcf");
		testnodata = new File(FileUtils.getTempDirectory(), "testnodata.vcf");
		FileCopyUtils.copy(in_no_data, new FileOutputStream(testnodata));
	}

	@Test
	public void metaData() throws IOException
	{
		VcfAttributes vcfAttrs = mock(VcfAttributes.class);
		VcfRepository vcfRepository = new VcfRepository(testdata, "testdata", vcfAttrs, entityMetaFactory,
				attrMetaFactory);
		try
		{
			assertEquals(vcfRepository.getName(), "testdata");
			Iterator<AttributeMetaData> it = vcfRepository.getEntityMetaData().getAttributes().iterator();
			assertTrue(it.hasNext());
			testAttribute(it.next(), VcfAttributes.CHROM, MolgenisFieldTypes.STRING);
			assertTrue(it.hasNext());
			// TEXT to handle large insertions/deletions
			testAttribute(it.next(), VcfAttributes.ALT, MolgenisFieldTypes.TEXT);
			assertTrue(it.hasNext());
			testAttribute(it.next(), VcfAttributes.POS, MolgenisFieldTypes.LONG);
			assertTrue(it.hasNext());
			// TEXT to handle large insertions/deletions
			testAttribute(it.next(), VcfAttributes.REF, MolgenisFieldTypes.TEXT);
			assertTrue(it.hasNext());
			testAttribute(it.next(), VcfAttributes.FILTER, MolgenisFieldTypes.STRING);
			assertTrue(it.hasNext());
			testAttribute(it.next(), VcfAttributes.QUAL, MolgenisFieldTypes.STRING);
			assertTrue(it.hasNext());
			testAttribute(it.next(), VcfAttributes.ID, MolgenisFieldTypes.STRING);
			assertTrue(it.hasNext());
			testAttribute(it.next(), VcfAttributes.INTERNAL_ID, MolgenisFieldTypes.STRING);
			assertTrue(it.hasNext());
			testAttribute(it.next(), VcfAttributes.INFO, MolgenisFieldTypes.COMPOUND);
			assertTrue(it.hasNext());
		}
		finally
		{
			IOUtils.closeQuietly(vcfRepository);
		}
	}

	void testAttribute(AttributeMetaData metadata, String name, FieldType type)
	{
		assertEquals(metadata.getName(), name);
		assertEquals(metadata.getDataType(), type);
	}

	@Test
	public void iterator() throws IOException
	{
		VcfRepository vcfRepository = null;
		try
		{
			RepositoryCollection collection = new VcfRepositoryCollection(testdata);
			vcfRepository = (VcfRepository) collection.getRepository("testdata");
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
		finally
		{
			IOUtils.closeQuietly(vcfRepository);
		}
	}

	@Test
	public void iterator_noValues() throws IOException
	{
		RepositoryCollection collection = new VcfRepositoryCollection(testnodata);
		VcfRepository vcfRepository = (VcfRepository) collection.getRepository("testnodata");
		try
		{
			Iterator<Entity> it = vcfRepository.iterator();
			assertFalse(it.hasNext());
		}
		finally
		{
			vcfRepository.close();
		}
	}

}
