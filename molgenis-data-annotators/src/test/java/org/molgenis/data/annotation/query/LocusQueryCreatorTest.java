package org.molgenis.data.annotation.query;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.vcf.VcfRepository;
import org.testng.annotations.Test;

public class LocusQueryCreatorTest
{

	@Test
	public void createQueryEntity()
	{
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put(VcfRepository.CHROM, "3");
		map.put(VcfRepository.POS, 3276424L);

		Entity entity = new MapEntity(map);

		Query q = QueryImpl.EQ(VcfRepository.CHROM, "3").and().eq(VcfRepository.POS, 3276424L);
		assertEquals(q, new LocusQueryCreator().createQuery(entity));
	}

	@Test
	public void getRequiredAttributes()
	{
		assertEquals(Arrays.asList(VcfRepository.CHROM_META, VcfRepository.POS_META),
				new LocusQueryCreator().getRequiredAttributes());
	}
}
