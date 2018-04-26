package org.molgenis.data.annotation.core.entity.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.core.filter.MultiAllelicResultFilter;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.data.vcf.config.VcfTestConfig;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.molgenis.data.meta.AttributeType.STRING;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { MultiAllelicResultFilterTest.Config.class })
public class MultiAllelicResultFilterTest extends AbstractMolgenisSpringTest
{
	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	VcfAttributes vcfAttributes;

	private EntityType emd;
	private EntityType resultEmd;
	private DynamicEntity entity1;
	private DynamicEntity entity2;
	private DynamicEntity entity3;
	private DynamicEntity entity7;
	private DynamicEntity entity8;
	private DynamicEntity entity9;
	private DynamicEntity entity10;
	private DynamicEntity entityNoRef;
	private DynamicEntity entityMismatchChrom;
	private DynamicEntity entityMismatchPos;
	private DynamicEntity resultEntity1;
	private DynamicEntity resultEntity2;
	private DynamicEntity resultEntity3;
	private DynamicEntity resultEntity4;
	private DynamicEntity resultEntity5;
	private DynamicEntity resultEntity6;
	private DynamicEntity resultEntity7;
	private DynamicEntity resultEntity8;
	private DynamicEntity resultEntity9;
	private DynamicEntity resultEntity10;

	@BeforeMethod
	public void setUp()
	{
		emd = entityTypeFactory.create("entity");

		resultEmd = entityTypeFactory.create("resultEntity");

		emd.addAttribute(vcfAttributes.getChromAttribute());
		emd.addAttribute(vcfAttributes.getPosAttribute());
		emd.addAttribute(vcfAttributes.getRefAttribute());
		emd.addAttribute(vcfAttributes.getAltAttribute());
		emd.addAttribute(vcfAttributes.getIdAttribute());

		resultEmd.addAttribute(vcfAttributes.getChromAttribute());
		resultEmd.addAttribute(vcfAttributes.getPosAttribute());
		resultEmd.addAttribute(vcfAttributes.getRefAttribute());
		resultEmd.addAttribute(vcfAttributes.getAltAttribute());
		resultEmd.addAttribute(vcfAttributes.getIdAttribute());
		resultEmd.addAttribute(attributeFactory.create().setName("annotation").setDataType(STRING));

		entity1 = new DynamicEntity(emd);
		entity1.set(VcfAttributes.CHROM, "1");
		entity1.set(VcfAttributes.POS, 100);
		entity1.set(VcfAttributes.REF, "C");
		entity1.set(VcfAttributes.ALT, "A");
		entity1.set(VcfAttributes.ID, "entity1");
		entity2 = new DynamicEntity(emd);
		entity2.set(VcfAttributes.CHROM, "1");
		entity2.set(VcfAttributes.POS, 100);
		entity2.set(VcfAttributes.REF, "C");
		entity2.set(VcfAttributes.ALT, "A,T");
		entity2.set(VcfAttributes.ID, "entity2");
		entity3 = new DynamicEntity(emd);
		entity3.set(VcfAttributes.CHROM, "1");
		entity3.set(VcfAttributes.POS, 100);
		entity3.set(VcfAttributes.REF, "C");
		entity3.set(VcfAttributes.ALT, "A,T,G");
		entity3.set(VcfAttributes.ID, "entity3");
		entity7 = new DynamicEntity(emd);
		entity7.set(VcfAttributes.CHROM, "1");
		entity7.set(VcfAttributes.POS, 100);
		entity7.set(VcfAttributes.REF, "TTCCTCC");
		entity7.set(VcfAttributes.ALT, "TTCC");
		entity7.set(VcfAttributes.ID, "entity7");
		entity8 = new DynamicEntity(emd);
		entity8.set(VcfAttributes.CHROM, "1");
		entity8.set(VcfAttributes.POS, 100);
		entity8.set(VcfAttributes.REF, "TTCCTCCTCC");
		entity8.set(VcfAttributes.ALT, "TTCCTCC");
		entity8.set(VcfAttributes.ID, "entity8");
		entity9 = new DynamicEntity(emd);
		entity9.set(VcfAttributes.CHROM, "1");
		entity9.set(VcfAttributes.POS, 100);
		entity9.set(VcfAttributes.REF, "GA");
		entity9.set(VcfAttributes.ALT, "G");
		entity9.set(VcfAttributes.ID, "entity9");
		entity10 = new DynamicEntity(emd);
		entity10.set(VcfAttributes.CHROM, "1");
		entity10.set(VcfAttributes.POS, 100);
		entity10.set(VcfAttributes.REF, "GAA");
		entity10.set(VcfAttributes.ALT, "GA");
		entity10.set(VcfAttributes.ID, "entity10");
		entityNoRef = new DynamicEntity(emd);
		entityNoRef.set(VcfAttributes.CHROM, "1");
		entityNoRef.set(VcfAttributes.POS, 100);
		entityNoRef.set(VcfAttributes.ID, "entityNoRef");
		entityMismatchChrom = new DynamicEntity(emd);
		entityMismatchChrom.set(VcfAttributes.CHROM, "2");
		entityMismatchChrom.set(VcfAttributes.POS, 100);
		entityMismatchChrom.set(VcfAttributes.REF, "A");
		entityMismatchChrom.set(VcfAttributes.ALT, "C");
		entityMismatchChrom.set(VcfAttributes.ID, "entityMismatchChrom");
		entityMismatchPos = new DynamicEntity(emd);
		entityMismatchPos.set(VcfAttributes.CHROM, "1");
		entityMismatchPos.set(VcfAttributes.POS, 101);
		entityMismatchPos.set(VcfAttributes.REF, "A");
		entityMismatchPos.set(VcfAttributes.ALT, "C");
		entityMismatchPos.set(VcfAttributes.ID, "entityMismatchPos");

		resultEntity1 = new DynamicEntity(resultEmd);
		resultEntity1.set(VcfAttributes.CHROM, "1");
		resultEntity1.set(VcfAttributes.POS, 100);
		resultEntity1.set(VcfAttributes.REF, "C");
		resultEntity1.set(VcfAttributes.ALT, "A");
		resultEntity1.set("annotation", "1");
		resultEntity1.set(VcfAttributes.ID, "resultEntity1");

		resultEntity2 = new DynamicEntity(resultEmd);
		resultEntity2.set(VcfAttributes.CHROM, "1");
		resultEntity2.set(VcfAttributes.POS, 100);
		resultEntity2.set(VcfAttributes.REF, "C");
		resultEntity2.set(VcfAttributes.ALT, "T");
		resultEntity2.set("annotation", "2");
		resultEntity2.set(VcfAttributes.ID, "resultEntity2");

		resultEntity3 = new DynamicEntity(resultEmd);
		resultEntity3.set(VcfAttributes.CHROM, "1");
		resultEntity3.set(VcfAttributes.POS, 100);
		resultEntity3.set(VcfAttributes.REF, "C");
		resultEntity3.set(VcfAttributes.ALT, "A,T");
		resultEntity3.set("annotation", "3,4");
		resultEntity3.set(VcfAttributes.ID, "resultEntity3");

		resultEntity4 = new DynamicEntity(resultEmd);
		resultEntity4.set(VcfAttributes.CHROM, "1");
		resultEntity4.set(VcfAttributes.POS, 100);
		resultEntity4.set(VcfAttributes.REF, "C");
		resultEntity4.set(VcfAttributes.ALT, "T,A");
		resultEntity4.set("annotation", "5,6");
		resultEntity4.set(VcfAttributes.ID, "resultEntity4");

		resultEntity5 = new DynamicEntity(resultEmd);
		resultEntity5.set(VcfAttributes.CHROM, "1");
		resultEntity5.set(VcfAttributes.POS, 100);
		resultEntity5.set(VcfAttributes.REF, "C");
		resultEntity5.set(VcfAttributes.ALT, "G,A,T");
		resultEntity5.set("annotation", "7,8,9");
		resultEntity5.set(VcfAttributes.ID, "resultEntity5");

		resultEntity6 = new DynamicEntity(resultEmd);
		resultEntity6.set(VcfAttributes.CHROM, "1");
		resultEntity6.set(VcfAttributes.POS, 100);
		resultEntity6.set(VcfAttributes.REF, "C");
		resultEntity6.set(VcfAttributes.ALT, "G,A");
		resultEntity6.set("annotation", "10,11");
		resultEntity6.set(VcfAttributes.ID, "resultEntity6");

		resultEntity7 = new DynamicEntity(resultEmd);
		resultEntity7.set(VcfAttributes.CHROM, "1");
		resultEntity7.set(VcfAttributes.POS, 100);
		resultEntity7.set(VcfAttributes.REF, "TTCCTCCTCC");
		resultEntity7.set(VcfAttributes.ALT, "TTGGTCC,TTCCTCC");
		resultEntity7.set("annotation", "12,13");
		resultEntity7.set(VcfAttributes.ID, "resultEntity7");

		resultEntity8 = new DynamicEntity(resultEmd);
		resultEntity8.set(VcfAttributes.CHROM, "1");
		resultEntity8.set(VcfAttributes.POS, 100);
		resultEntity8.set(VcfAttributes.REF, "TTCCTCC");
		resultEntity8.set(VcfAttributes.ALT, "TTGGT,TTCC");
		resultEntity8.set("annotation", "14,15");
		resultEntity8.set(VcfAttributes.ID, "resultEntity8");

		resultEntity9 = new DynamicEntity(resultEmd);
		resultEntity9.set(VcfAttributes.CHROM, "1");
		resultEntity9.set(VcfAttributes.POS, 100);
		resultEntity9.set(VcfAttributes.REF, "GAA");
		resultEntity9.set(VcfAttributes.ALT, "GA,G");
		resultEntity9.set("annotation", "16,17");
		resultEntity9.set(VcfAttributes.ID, "resultEntity9");

		resultEntity10 = new DynamicEntity(resultEmd);
		resultEntity10.set(VcfAttributes.CHROM, "1");
		resultEntity10.set(VcfAttributes.POS, 100);
		resultEntity10.set(VcfAttributes.REF, "GA");
		resultEntity10.set(VcfAttributes.ALT, "GC,G");
		resultEntity10.set("annotation", "18,19");
		resultEntity10.set(VcfAttributes.ID, "resultEntity10");

	}

	@Test
	public void filterResultsTest1()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(attributeFactory.create().setName("annotation").setDataType(STRING)),
				vcfAttributes);
		Optional<Entity> result1 = filter.filterResults(Collections.singletonList(resultEntity1), entity1, false);
		assertEquals(Lists.newArrayList(result1.asSet()).get(0).getString("annotation"), "1");
	}

	@Test
	public void filterResultsTest2()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(attributeFactory.create().setName("annotation").setDataType(STRING)),
				vcfAttributes);
		Optional<Entity> result2 = filter.filterResults(Collections.singletonList(resultEntity2), entity1, false);
		Assert.assertTrue(Lists.newArrayList(result2.asSet()).size() == 0);

	}

	@Test
	public void filterResultsTest3()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(attributeFactory.create().setName("annotation").setDataType(STRING)),
				vcfAttributes);
		Optional<Entity> result3 = filter.filterResults(Collections.singletonList(resultEntity3), entity2, false);
		assertEquals(Lists.newArrayList(result3.asSet()).get(0).getString("annotation"), "3,4");

	}

	@Test
	public void filterResultsTest4()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(attributeFactory.create().setName("annotation").setDataType(STRING)),
				vcfAttributes);
		Optional<Entity> result4 = filter.filterResults(Collections.singletonList(resultEntity4), entity2, false);
		assertEquals(Lists.newArrayList(result4.asSet()).get(0).getString("annotation"), "6,5");

	}

	@Test
	public void filterResultsTest5()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(attributeFactory.create().setName("annotation").setDataType(STRING)),
				vcfAttributes);
		Optional<Entity> result5 = filter.filterResults(Collections.singletonList(resultEntity5), entity3, false);
		assertEquals(Lists.newArrayList(result5.asSet()).get(0).getString("annotation"), "8,9,7");

	}

	@Test
	public void filterResultsTest6()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(attributeFactory.create().setName("annotation").setDataType(STRING)),
				vcfAttributes);
		Optional<Entity> result6 = filter.filterResults(Collections.singletonList(resultEntity2), entity2, false);
		assertEquals(Lists.newArrayList(result6.asSet()).get(0).getString("annotation"), ".,2");

	}

	@Test
	public void filterResultsTest7()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(attributeFactory.create().setName("annotation").setDataType(STRING)),
				vcfAttributes);
		Optional<Entity> result7 = filter.filterResults(Collections.singletonList(resultEntity6), entity3, false);
		assertEquals(Lists.newArrayList(result7.asSet()).get(0).getString("annotation"), "11,.,10");

	}

	@Test
	public void filterResultsTest8()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(attributeFactory.create().setName("annotation").setDataType(STRING)),
				vcfAttributes);
		Optional<Entity> result8 = filter.filterResults(Collections.singletonList(resultEntity5), entity1, false);
		assertEquals(Lists.newArrayList(result8.asSet()).get(0).getString("annotation"), "8");

	}

	@Test
	public void filterResultsTest9()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(attributeFactory.create().setName("annotation").setDataType(STRING)),
				vcfAttributes);
		Optional<Entity> result = filter.filterResults(Collections.singletonList(resultEntity7), entity7, false);
		assertEquals(Lists.newArrayList(result.asSet()).get(0).getString("annotation"), "13");
	}

	@Test
	public void filterResultsTest10()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(attributeFactory.create().setName("annotation").setDataType(STRING)),
				vcfAttributes);
		Optional<Entity> result = filter.filterResults(Collections.singletonList(resultEntity8), entity8, false);
		assertEquals(Lists.newArrayList(result.asSet()).get(0).getString("annotation"), "15");

	}

	@Test
	public void filterResultsTest11()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(attributeFactory.create().setName("annotation").setDataType(STRING)),
				vcfAttributes);
		Optional<Entity> result = filter.filterResults(Collections.singletonList(resultEntity9), entity9, false);
		assertEquals(Lists.newArrayList(result.asSet()).get(0).getString("annotation"), "16");

	}

	@Test
	public void filterResultsTest12()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(attributeFactory.create().setName("annotation").setDataType(STRING)),
				vcfAttributes);
		Optional<Entity> result = filter.filterResults(Collections.singletonList(resultEntity10), entity10, false);
		assertEquals(Lists.newArrayList(result.asSet()).get(0).getString("annotation"), "19");

	}

	@Test
	public void filterResultsSourceHasNoRef()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(attributeFactory.create().setName("annotation").setDataType(STRING)),
				vcfAttributes);
		Optional<Entity> result = filter.filterResults(Collections.singletonList(resultEntity10), entityNoRef, false);
		assertEquals(result, Optional.absent());
	}

	@Test
	public void filterResultsMergeMultilineMismatchChrom()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(attributeFactory.create().setName("annotation").setDataType(STRING)), true,
				vcfAttributes);
		try
		{
			filter.filterResults(Arrays.asList(resultEntity10, entityMismatchChrom), entity10, false);
			Assert.fail("Should throw exception for mismatching chromosomes");
		}
		catch (MolgenisDataException actual)
		{
			assertEquals(actual.getMessage(),
					"Mismatch in location! Location{chrom=1, pos=100} vs Location{chrom=2, pos=100}");
		}
	}

	@Test
	public void filterResultsMergeMultilineMismatchPos()
	{
		MultiAllelicResultFilter filter = new MultiAllelicResultFilter(
				Collections.singletonList(attributeFactory.create().setName("annotation").setDataType(STRING)), true,
				vcfAttributes);
		try
		{
			filter.filterResults(Arrays.asList(entityMismatchPos, resultEntity10), entity10, false);
			Assert.fail("Should throw exception for mismatching positions");
		}
		catch (MolgenisDataException actual)
		{
			assertEquals(actual.getMessage(),
					"Mismatch in location! Location{chrom=1, pos=101} vs Location{chrom=1, pos=100}");
		}
	}

	/*
	 * entity list:
	 * 3	300	G	A	0.2|23.1
	 * 3	300	G	T	-2.4|0.123
	 * 3	300	G	X	-0.002|2.3
	 * 3	300	G	C	0.5|14.5
	 * 3	300	GC	A	0.2|23.1
	 * 3	300	GC	T	-2.4|0.123
	 * 3	300	C	GX	-0.002|2.3
	 * 3	300	C	GC	0.5|14.5
	 *
	 * should become:
	 * 3	300	G	A,T,X,C	0.2|23.1,-2.4|0.123,-0.002|2.3,0.5|14.5
	 * 3	300	GC	A,T	0.2|23.1,-2.4|0.123
	 * 3	300	C	GX,GC	-0.002|2.3,0.5|14.5
	 */
	@Test
	public void testMultiLineMerge()
	{

		String customAttrb = "MyAnnotation";
		EntityType multiLineTestEMD = entityTypeFactory.create("entity");

		multiLineTestEMD.addAttribute(vcfAttributes.getChromAttribute());
		multiLineTestEMD.addAttribute(vcfAttributes.getPosAttribute());
		multiLineTestEMD.addAttribute(vcfAttributes.getIdAttribute());
		multiLineTestEMD.addAttribute(vcfAttributes.getRefAttribute());
		multiLineTestEMD.addAttribute(vcfAttributes.getAltAttribute());
		multiLineTestEMD.addAttribute(attributeFactory.create().setName(customAttrb).setDataType(STRING));

		Entity multiLineEntity1 = new DynamicEntity(multiLineTestEMD);
		multiLineEntity1.set(VcfAttributes.CHROM, "3");
		multiLineEntity1.set(VcfAttributes.POS, 300);
		multiLineEntity1.set(VcfAttributes.REF, "G");
		multiLineEntity1.set(VcfAttributes.ALT, "A");
		multiLineEntity1.set(customAttrb, "0.2|23.1");

		Entity multiLineEntity2 = new DynamicEntity(multiLineTestEMD);
		multiLineEntity2.set(VcfAttributes.CHROM, "3");
		multiLineEntity2.set(VcfAttributes.POS, 300);
		multiLineEntity2.set(VcfAttributes.REF, "G");
		multiLineEntity2.set(VcfAttributes.ALT, "T");
		multiLineEntity2.set(customAttrb, "-2.4|0.123");

		Entity multiLineEntity3 = new DynamicEntity(multiLineTestEMD);
		multiLineEntity3.set(VcfAttributes.CHROM, "3");
		multiLineEntity3.set(VcfAttributes.POS, 300);
		multiLineEntity3.set(VcfAttributes.REF, "G");
		multiLineEntity3.set(VcfAttributes.ALT, "X");
		multiLineEntity3.set(customAttrb, "-0.002|2.3");

		Entity multiLineEntity4 = new DynamicEntity(multiLineTestEMD);
		multiLineEntity4.set(VcfAttributes.CHROM, "3");
		multiLineEntity4.set(VcfAttributes.POS, 300);
		multiLineEntity4.set(VcfAttributes.REF, "G");
		multiLineEntity4.set(VcfAttributes.ALT, "C");
		multiLineEntity4.set(customAttrb, "0.5|14.5");

		Entity multiLineEntity5 = new DynamicEntity(multiLineTestEMD);
		multiLineEntity5.set(VcfAttributes.CHROM, "3");
		multiLineEntity5.set(VcfAttributes.POS, 300);
		multiLineEntity5.set(VcfAttributes.REF, "GC");
		multiLineEntity5.set(VcfAttributes.ALT, "A");
		multiLineEntity5.set("MyAnnotation", "0.2|23.1");

		Entity multiLineEntity6 = new DynamicEntity(multiLineTestEMD);
		multiLineEntity6.set(VcfAttributes.CHROM, "3");
		multiLineEntity6.set(VcfAttributes.POS, 300);
		multiLineEntity6.set(VcfAttributes.REF, "GC");
		multiLineEntity6.set(VcfAttributes.ALT, "T");
		multiLineEntity6.set(customAttrb, "-2.4|0.123");

		Entity multiLineEntity7 = new DynamicEntity(multiLineTestEMD);
		multiLineEntity7.set(VcfAttributes.CHROM, "3");
		multiLineEntity7.set(VcfAttributes.POS, 300);
		multiLineEntity7.set(VcfAttributes.REF, "C");
		multiLineEntity7.set(VcfAttributes.ALT, "GX");
		multiLineEntity7.set(customAttrb, "-0.002|2.3");

		Entity multiLineEntity8 = new DynamicEntity(multiLineTestEMD);
		multiLineEntity8.set(VcfAttributes.CHROM, "3");
		multiLineEntity8.set(VcfAttributes.POS, 300);
		multiLineEntity8.set(VcfAttributes.REF, "C");
		multiLineEntity8.set(VcfAttributes.ALT, "GC");
		multiLineEntity8.set(customAttrb, "0.5|14.5");

		Entity expectedResultEntity1 = new DynamicEntity(multiLineTestEMD);
		expectedResultEntity1.set(VcfAttributes.CHROM, "3");
		expectedResultEntity1.set(VcfAttributes.POS, 300);
		expectedResultEntity1.set(VcfAttributes.REF, "G");
		expectedResultEntity1.set(VcfAttributes.ALT, "A,T,X,C");
		expectedResultEntity1.set(customAttrb, "0.2|23.1,-2.4|0.123,-0.002|2.3,0.5|14.5");

		Entity expectedResultEntity2 = new DynamicEntity(multiLineTestEMD);
		expectedResultEntity2.set(VcfAttributes.CHROM, "3");
		expectedResultEntity2.set(VcfAttributes.POS, 300);
		expectedResultEntity2.set(VcfAttributes.REF, "GC");
		expectedResultEntity2.set(VcfAttributes.ALT, "A,T");
		expectedResultEntity2.set(customAttrb, "0.2|23.1,-2.4|0.123");

		Entity expectedResultEntity3 = new DynamicEntity(multiLineTestEMD);
		expectedResultEntity3.set(VcfAttributes.CHROM, "3");
		expectedResultEntity3.set(VcfAttributes.POS, 300);
		expectedResultEntity3.set(VcfAttributes.REF, "C");
		expectedResultEntity3.set(VcfAttributes.ALT, "GX,GC");
		expectedResultEntity3.set(customAttrb, "-0.002|2.3,0.5|14.5");

		Iterable<Entity> multiLineInput = Arrays.asList(multiLineEntity1, multiLineEntity2, multiLineEntity3,
				multiLineEntity4, multiLineEntity5, multiLineEntity6, multiLineEntity7, multiLineEntity8);

		MultiAllelicResultFilter multiAllelicResultFilter = new MultiAllelicResultFilter(
				Lists.newArrayList((multiLineTestEMD.getAttribute(customAttrb))), vcfAttributes);

		Iterable<Entity> expectedResult = Arrays.asList(expectedResultEntity1, expectedResultEntity2,
				expectedResultEntity3);

		Iterable<Entity> actualResult = multiAllelicResultFilter.merge(multiLineInput);

		assertTrue(EntityUtils.equals(actualResult.iterator().next(), expectedResult.iterator().next()));
	}

	@Configuration
	@Import({ VcfTestConfig.class })
	public static class Config
	{
	}
}
