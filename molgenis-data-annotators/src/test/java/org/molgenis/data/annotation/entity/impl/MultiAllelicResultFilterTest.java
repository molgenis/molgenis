package org.molgenis.data.annotation.entity.impl;

import com.google.common.base.Optional;
import org.elasticsearch.common.collect.Lists;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.testng.Assert.assertEquals;

public class MultiAllelicResultFilterTest
{

	private DefaultEntityMetaData emd;
	private DefaultEntityMetaData resultEmd;
	private MapEntity entity1;
	private MapEntity entity2;
	private MapEntity entity3;
	private MapEntity resultEntity1;
	private MapEntity resultEntity2;
	private MapEntity resultEntity3;
	private MapEntity resultEntity4;
	private MapEntity resultEntity5;
	private MapEntity resultEntity6;

	@BeforeMethod
	public void setUp()
	{
		emd = new DefaultEntityMetaData("entity");

		resultEmd = new DefaultEntityMetaData("resultEntity");

		emd.addAttributeMetaData(VcfRepository.CHROM_META);
		emd.addAttributeMetaData(VcfRepository.POS_META);
		emd.addAttributeMetaData(VcfRepository.REF_META);
		emd.addAttributeMetaData(VcfRepository.ALT_META);
		emd.addAttributeMetaData(VcfRepository.ID_META);
		emd.setIdAttribute(VcfRepository.ID_META.getName());

		resultEmd.addAttributeMetaData(VcfRepository.CHROM_META);
		resultEmd.addAttributeMetaData(VcfRepository.POS_META);
		resultEmd.addAttributeMetaData(VcfRepository.REF_META);
		resultEmd.addAttributeMetaData(VcfRepository.ALT_META);
		resultEmd.addAttributeMetaData(VcfRepository.ID_META);
		resultEmd.addAttributeMetaData(new DefaultAttributeMetaData("annotation",
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		resultEmd.setIdAttribute(VcfRepository.ID_META.getName());

		entity1 = new MapEntity(emd);
		entity1.set(VcfRepository.CHROM, "1");
		entity1.set(VcfRepository.POS, 100);
		entity1.set(VcfRepository.REF, "C");
		entity1.set(VcfRepository.ALT, "A");
		entity1.set(VcfRepository.ID, "entity1");
		entity2 = new MapEntity(emd);
		entity2.set(VcfRepository.CHROM, "1");
		entity2.set(VcfRepository.POS, 100);
		entity2.set(VcfRepository.REF, "C");
		entity2.set(VcfRepository.ALT, "A,T");
		entity2.set(VcfRepository.ID, "entity2");
		entity3 = new MapEntity(emd);
		entity3.set(VcfRepository.CHROM, "1");
		entity3.set(VcfRepository.POS, 100);
		entity3.set(VcfRepository.REF, "C");
		entity3.set(VcfRepository.ALT, "A,T,G");
		entity3.set(VcfRepository.ID, "entity3");

		resultEntity1 = new MapEntity(resultEmd);
		resultEntity1.set(VcfRepository.CHROM, "1");
		resultEntity1.set(VcfRepository.POS, 100);
		resultEntity1.set(VcfRepository.REF, "C");
		resultEntity1.set(VcfRepository.ALT, "A");
		resultEntity1.set("annotation", "1");
		resultEntity1.set(VcfRepository.ID, "resultEntity1");

		resultEntity2 = new MapEntity(resultEmd);
		resultEntity2.set(VcfRepository.CHROM, "1");
		resultEntity2.set(VcfRepository.POS, 100);
		resultEntity2.set(VcfRepository.REF, "C");
		resultEntity2.set(VcfRepository.ALT, "T");
		resultEntity2.set("annotation", "2");
		resultEntity2.set(VcfRepository.ID, "resultEntity2");

		resultEntity3 = new MapEntity(resultEmd);
		resultEntity3.set(VcfRepository.CHROM, "1");
		resultEntity3.set(VcfRepository.POS, 100);
		resultEntity3.set(VcfRepository.REF, "C");
		resultEntity3.set(VcfRepository.ALT, "A,T");
		resultEntity3.set("annotation", "3,4");
		resultEntity3.set(VcfRepository.ID, "resultEntity3");

		resultEntity4 = new MapEntity(resultEmd);
		resultEntity4.set(VcfRepository.CHROM, "1");
		resultEntity4.set(VcfRepository.POS, 100);
		resultEntity4.set(VcfRepository.REF, "C");
		resultEntity4.set(VcfRepository.ALT, "T,A");
		resultEntity4.set("annotation", "5,6");
		resultEntity4.set(VcfRepository.ID, "resultEntity4");

		resultEntity5 = new MapEntity(resultEmd);
		resultEntity5.set(VcfRepository.CHROM, "1");
		resultEntity5.set(VcfRepository.POS, 100);
		resultEntity5.set(VcfRepository.REF, "C");
		resultEntity5.set(VcfRepository.ALT, "G,A,T");
		resultEntity5.set("annotation", "7,8,9");
		resultEntity5.set(VcfRepository.ID, "resultEntity5");

		resultEntity6 = new MapEntity(resultEmd);
		resultEntity6.set(VcfRepository.CHROM, "1");
		resultEntity6.set(VcfRepository.POS, 100);
		resultEntity6.set(VcfRepository.REF, "C");
		resultEntity6.set(VcfRepository.ALT, "G,A");
		resultEntity6.set("annotation", "10,11");
		resultEntity6.set(VcfRepository.ID, "resultEntity6");
	}

	@Test
	public void filterResultsTest1()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(new DefaultAttributeMetaData("annotation",
						MolgenisFieldTypes.FieldTypeEnum.STRING)));
		Optional<Entity> result1 = filter.filterResults(Collections.singletonList(resultEntity1), entity1);
		assertEquals(Lists.newArrayList(result1.asSet()).get(0).getString("annotation"), "1");
	}

	@Test
	public void filterResultsTest2()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(new DefaultAttributeMetaData("annotation",
						MolgenisFieldTypes.FieldTypeEnum.STRING)));
		Optional<Entity> result2 = filter.filterResults(Collections.singletonList(resultEntity2), entity1);
		assertEquals(Lists.newArrayList(result2.asSet()).get(0).getString("annotation"), "");

	}

	@Test
	public void filterResultsTest3()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(new DefaultAttributeMetaData("annotation",
						MolgenisFieldTypes.FieldTypeEnum.STRING)));
		Optional<Entity> result3 = filter.filterResults(Collections.singletonList(resultEntity3), entity2);
		assertEquals(Lists.newArrayList(result3.asSet()).get(0).getString("annotation"), "3,4");

	}

	@Test
	public void filterResultsTest4()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(new DefaultAttributeMetaData("annotation",
						MolgenisFieldTypes.FieldTypeEnum.STRING)));
		Optional<Entity> result4 = filter.filterResults(Collections.singletonList(resultEntity4), entity2);
		assertEquals(Lists.newArrayList(result4.asSet()).get(0).getString("annotation"), "6,5");

	}

	@Test
	public void filterResultsTest5()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(new DefaultAttributeMetaData("annotation",
						MolgenisFieldTypes.FieldTypeEnum.STRING)));
		Optional<Entity> result5 = filter.filterResults(Collections.singletonList(resultEntity5), entity3);
		assertEquals(Lists.newArrayList(result5.asSet()).get(0).getString("annotation"), "8,9,7");

	}

	@Test
	public void filterResultsTest6()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(new DefaultAttributeMetaData("annotation",
						MolgenisFieldTypes.FieldTypeEnum.STRING)));
		Optional<Entity> result6 = filter.filterResults(Collections.singletonList(resultEntity2), entity2);
		assertEquals(Lists.newArrayList(result6.asSet()).get(0).getString("annotation"), ".,2");

	}

	@Test
	public void filterResultsTest7()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(new DefaultAttributeMetaData("annotation",
						MolgenisFieldTypes.FieldTypeEnum.STRING)));
		Optional<Entity> result7 = filter.filterResults(Collections.singletonList(resultEntity6), entity3);
		assertEquals(Lists.newArrayList(result7.asSet()).get(0).getString("annotation"), "11,.,10");

	}

	@Test
	public void filterResultsTest8()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(new DefaultAttributeMetaData("annotation",
						MolgenisFieldTypes.FieldTypeEnum.STRING)));
		Optional<Entity> result8 = filter.filterResults(Collections.singletonList(resultEntity5), entity1);
		assertEquals(Lists.newArrayList(result8.asSet()).get(0).getString("annotation"), "8");

	}
}
