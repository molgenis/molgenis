package org.molgenis.data.annotation.core.resources.impl.tabix;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.Attribute;
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

import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.vcf.model.VcfAttributes.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { TabixVcfRepositoryTest.Config.class })
public class TabixVcfRepositoryTest extends AbstractMolgenisSpringTest
{
	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	VcfAttributes vcfAttributes;

	private TabixVcfRepository tabixVcfRepository;
	private EntityType repoMetaData;

	@BeforeClass
	public void before() throws IOException
	{
		repoMetaData = entityTypeFactory.create("TabixTest");
		repoMetaData.addAttribute(vcfAttributes.getChromAttribute());
		repoMetaData.addAttribute(vcfAttributes.getAltAttribute());
		repoMetaData.addAttribute(vcfAttributes.getPosAttribute());
		repoMetaData.addAttribute(vcfAttributes.getRefAttribute());
		repoMetaData.addAttribute(vcfAttributes.getFilterAttribute());
		repoMetaData.addAttribute(vcfAttributes.getQualAttribute());
		repoMetaData.addAttribute(vcfAttributes.getIdAttribute());
		repoMetaData.addAttribute(
				attributeFactory.create().setName("INTERNAL_ID").setDataType(STRING).setVisible(false), ROLE_ID);
		repoMetaData.addAttribute(attributeFactory.create().setName("INFO").setDataType(COMPOUND));

		File file = ResourceUtils.getFile(getClass(), "/tabixtest.vcf.gz");
		tabixVcfRepository = new TabixVcfRepository(file, "TabixTest", vcfAttributes, entityTypeFactory,
				attributeFactory);
	}

	@Test
	public void testGetEntityType()
	{
		EntityType vcfMetaData = tabixVcfRepository.getEntityType();

		vcfMetaData.setId("dummyId");
		vcfMetaData.getOwnAllAttributes().forEach(attr -> attr.setIdentifier(null));
		repoMetaData.setId("dummyId");
		repoMetaData.setLabel("TabixTest");
		repoMetaData.getOwnAllAttributes().forEach(attr -> attr.setIdentifier(null));
		assertTrue(EntityUtils.equals(vcfMetaData, repoMetaData));
	}

	@Test
	public void testQuery()
	{
		Query<Entity> query = tabixVcfRepository.query()
												.eq(VcfAttributes.CHROM, "1")
												.and()
												.eq(VcfAttributes.POS, "249240543");

		Iterator<Entity> iterator = tabixVcfRepository.findAll(query).iterator();
		iterator.hasNext();
		Entity other = iterator.next();
		Entity entity = newEntity("1", 249240543, "A", "AGG", "PASS", "100", "", "zG7SPcGIh_8_IicI1uLeoQ");
		boolean equal = true;
		for (Attribute attr : entity.getEntityType().getAtomicAttributes())
		{
			equal = other.get(attr.getName()).equals(entity.get(attr.getName()));
			if (!equal) break;
		}
		assertTrue(equal);
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testIterator()
	{
		Entity entity = newEntity("1", 249240543, "A", "AGG", "PASS", "100", "", "zG7SPcGIh_8_IicI1uLeoQ");

		Iterator<Entity> iterator = tabixVcfRepository.iterator();
		iterator.hasNext();
		Entity other = iterator.next();
		boolean equal = true;
		for (Attribute attr : entity.getEntityType().getAtomicAttributes())
		{
			equal = other.get(attr.getName()).equals(entity.get(attr.getName()));
			if (!equal)
			{
				System.out.println(attr.getName());
				break;
			}
		}
		assertTrue(equal);
	}

	private DynamicEntity newEntity(String chrom, int pos, String alt, String ref, String filter, String qual,
			String id, String internalId)
	{
		DynamicEntity result = new DynamicEntity(repoMetaData);
		result.set(CHROM, chrom);
		result.set(ALT, alt);
		result.set(POS, pos);
		result.set(REF, ref);
		result.set("FILTER", filter);
		result.set("QUAL", qual);
		result.set("INTERNAL_ID", internalId);
		result.set("ID", id);
		return result;
	}

	@Configuration
	@Import({ VcfTestConfig.class })
	public static class Config
	{
	}
}