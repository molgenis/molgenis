package org.molgenis.data.annotation.impl.tabix;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.vcf.VcfRepository.ALT;
import static org.molgenis.data.vcf.VcfRepository.ALT_META;
import static org.molgenis.data.vcf.VcfRepository.CHROM;
import static org.molgenis.data.vcf.VcfRepository.CHROM_META;
import static org.molgenis.data.vcf.VcfRepository.POS;
import static org.molgenis.data.vcf.VcfRepository.POS_META;
import static org.molgenis.data.vcf.VcfRepository.REF;
import static org.molgenis.data.vcf.VcfRepository.REF_META;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotator.tabix.TabixVcfRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TabixVcfRepositoryTest
{
	private TabixVcfRepository tabixVcfRepository;
	private DefaultEntityMetaData repoMetaData;

	@BeforeTest
	public void beforeTest() throws IOException
	{
		repoMetaData = new DefaultEntityMetaData("TabixTest");
		repoMetaData.addAttributeMetaData(CHROM_META);
		repoMetaData.addAttributeMetaData(ALT_META);
		repoMetaData.addAttributeMetaData(POS_META);
		repoMetaData.addAttributeMetaData(REF_META);
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("FILTER", STRING));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("QUAL", STRING));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("ID", STRING));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("INTERNAL_ID", STRING), ROLE_ID);
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("INFO", COMPOUND));
		repoMetaData.addAttribute("INTERNAL_ID").setVisible(false);

		File file = ResourceUtils.getFile(getClass(),
				"/ALL.chr1.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz");
		tabixVcfRepository = new TabixVcfRepository(file, "TabixTest");
	}

	@Test
	public void testGetEntityMetaData()
	{
		assertEquals(tabixVcfRepository.getEntityMetaData(), repoMetaData);
	}

	@Test
	public void testQuery()
	{
		Query query = tabixVcfRepository.query().eq(VcfRepository.CHROM, "1").and().eq(VcfRepository.POS, "10352");

		Iterator<Entity> iterator = tabixVcfRepository.findAll(query).iterator();
		iterator.hasNext();
		Entity other = iterator.next();
		Entity entity = newEntity("1", 10352, "TA", "T", "PASS", "100", "rs145072688", "uVflR7Ra_O8E04Zfj-O_Og");
		boolean equal = true;
		for (AttributeMetaData attr : entity.getEntityMetaData().getAtomicAttributes())
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
		Entity entity = newEntity("1", 10352, "TA", "T", "PASS", "100", "rs145072688", "uVflR7Ra_O8E04Zfj-O_Og");

		Iterator<Entity> iterator = tabixVcfRepository.iterator();
		iterator.hasNext();
		Entity other = iterator.next();
		boolean equal = true;
		for (AttributeMetaData attr : entity.getEntityMetaData().getAtomicAttributes())
		{
			equal = other.get(attr.getName()).equals(entity.get(attr.getName()));
			if (!equal) break;
		}
		assertTrue(equal);
	}

	private MapEntity newEntity(String chrom, int pos, String alt, String ref, String filter, String qual, String id,
			String internalId)
	{
		MapEntity result = new MapEntity(repoMetaData);
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
}