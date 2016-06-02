package org.molgenis.data.annotation.query;

public class LocusQueryCreatorTest
{
	//
	//	@Test
	//	public void createQueryEntity()
	//	{
	//		Map<String, Object> map = new LinkedHashMap<String, Object>();
	//		map.put(VcfAttributes.CHROM, "3");
	//		map.put(VcfAttributes.POS, 3276424L);
	//
	//		Entity entity = new MapEntity(map);
	//
	//		Query<Entity> q = QueryImpl.EQ(VcfAttributes.CHROM, "3").and().eq(VcfAttributes.POS, 3276424L);
	//		assertEquals(q, new LocusQueryCreator().createQuery(entity));
	//	}
	//
	//	@Test
	//	public void getRequiredAttributes()
	//	{
	//		assertEquals(Arrays.asList(VcfAttributes.CHROM_META, VcfAttributes.POS_META),
	//				new LocusQueryCreator().getRequiredAttributes());
	//	}
}
