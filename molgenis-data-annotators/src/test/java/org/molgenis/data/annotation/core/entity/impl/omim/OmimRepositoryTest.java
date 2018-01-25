package org.molgenis.data.annotation.core.entity.impl.omim;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.data.vcf.config.VcfTestConfig;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.molgenis.data.annotation.core.entity.impl.omim.OmimRepository.*;
import static org.molgenis.util.ResourceUtils.getFile;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { OmimAnnotatorTest.Config.class })
public class OmimRepositoryTest extends AbstractMolgenisSpringTest
{
	@Autowired
	ApplicationContext context;

	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	VcfAttributes vcfAttributes;
	private OmimRepository repo;
	private File omimFile = getFile(getClass(), "/omim/omim.txt");

	private Entity entity1;
	private Entity entity2;
	private Entity entity3;
	private Entity entity4;
	private Entity entity5;

	@BeforeClass
	public void beforeClass() throws IOException
	{
		repo = new OmimRepository(omimFile, entityTypeFactory, attributeFactory);

		entity1 = new DynamicEntity(repo.getEntityType());
		entity1.set(OMIM_PHENOTYPE_COL_NAME, "{Thyroid cancer, nonmedullary, 4}");
		entity1.set(OMIM_GENE_SYMBOLS_COL_NAME, "FOXE1");
		entity1.set(OMIM_MIM_NUMBER_COL_NAME, "602617");
		entity1.set(OMIM_CYTO_LOCATION_COL_NAME, "9q22.33");
		entity1.set(OMIM_ENTRY_COL_NAME, "616534");
		entity1.set(OMIM_TYPE_COL_NAME, "3");

		entity2 = new DynamicEntity(repo.getEntityType());
		entity2.set(OMIM_PHENOTYPE_COL_NAME, "17,20-lyase deficiency, isolated");
		entity2.set(OMIM_GENE_SYMBOLS_COL_NAME, "CYP17A1");
		entity2.set(OMIM_MIM_NUMBER_COL_NAME, "609300");
		entity2.set(OMIM_CYTO_LOCATION_COL_NAME, "10q24.32");
		entity2.set(OMIM_ENTRY_COL_NAME, "202110");
		entity2.set(OMIM_TYPE_COL_NAME, "3");

		entity3 = new DynamicEntity(repo.getEntityType());
		entity3.set(OMIM_PHENOTYPE_COL_NAME, "17,20-lyase deficiency, isolated");
		entity3.set(OMIM_GENE_SYMBOLS_COL_NAME, "CYP17");
		entity3.set(OMIM_MIM_NUMBER_COL_NAME, "609300");
		entity3.set(OMIM_CYTO_LOCATION_COL_NAME, "10q24.32");
		entity3.set(OMIM_ENTRY_COL_NAME, "202110");
		entity3.set(OMIM_TYPE_COL_NAME, "3");

		entity4 = new DynamicEntity(repo.getEntityType());
		entity4.set(OMIM_PHENOTYPE_COL_NAME, "{Thyroid cancer, monmedullary, 1}");
		entity4.set(OMIM_GENE_SYMBOLS_COL_NAME, "NKX2-1");
		entity4.set(OMIM_MIM_NUMBER_COL_NAME, "600635");
		entity4.set(OMIM_CYTO_LOCATION_COL_NAME, "14q13.3");
		entity4.set(OMIM_ENTRY_COL_NAME, "188550");
		entity4.set(OMIM_TYPE_COL_NAME, "3");

		entity5 = new DynamicEntity(repo.getEntityType());
		entity5.set(OMIM_PHENOTYPE_COL_NAME, "{Thyroid cancer, monmedullary, 1}");
		entity5.set(OMIM_GENE_SYMBOLS_COL_NAME, "NMTC1");
		entity5.set(OMIM_MIM_NUMBER_COL_NAME, "600635");
		entity5.set(OMIM_CYTO_LOCATION_COL_NAME, "14q13.3");
		entity5.set(OMIM_ENTRY_COL_NAME, "188550");
		entity5.set(OMIM_TYPE_COL_NAME, "3");
	}

	@AfterClass
	public void shutDown() throws IOException
	{
		repo.close();
	}

	@Test
	public void count()
	{
		assertEquals(repo.count(), 5);
	}

	@Test
	public void iterator()
	{
		Iterator<Entity> it = repo.iterator();
		assertTrue(EntityUtils.equals(it.next(), entity1));
		assertTrue(EntityUtils.equals(it.next(), entity4));
		assertTrue(EntityUtils.equals(it.next(), entity3));
		assertTrue(EntityUtils.equals(it.next(), entity5));
		assertTrue(EntityUtils.equals(it.next(), entity2));

	}

	@Test
	public void findAllWithEmptyQuery() throws IOException
	{
		Iterator<Entity> it = repo.findAll(new QueryImpl<>()).iterator();
		assertTrue(EntityUtils.equals(it.next(), entity1));
		assertTrue(EntityUtils.equals(it.next(), entity4));
		assertTrue(EntityUtils.equals(it.next(), entity3));
		assertTrue(EntityUtils.equals(it.next(), entity5));
		assertTrue(EntityUtils.equals(it.next(), entity2));
	}

	@Test
	public void findAllWithQuery() throws IOException
	{
		Iterator<Entity> it = repo.findAll(new QueryImpl<>().eq(OMIM_GENE_SYMBOLS_COL_NAME, "CYP17A1")).iterator();
		assertTrue(EntityUtils.equals(it.next(), entity2));
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "The only query allowed on this Repository is gene EQUALS")
	public void findAllWithBadQuery()
	{
		repo.findAll(new QueryImpl<>().like(OmimRepository.OMIM_PHENOTYPE_COL_NAME, "test_phenotype"));
	}

	@Configuration
	@Import({ VcfTestConfig.class })
	public static class Config
	{
	}
}
