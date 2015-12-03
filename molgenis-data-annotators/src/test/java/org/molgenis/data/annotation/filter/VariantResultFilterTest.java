package org.molgenis.data.annotation.filter;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.testng.annotations.Test;

import com.google.common.base.Optional;

public class VariantResultFilterTest
{

	@Test
	public void filterResults()
	{
		Map<String, Object> resultMap1 = new LinkedHashMap<String, Object>();
		resultMap1.put(VcfRepository.REF, "T");
		resultMap1.put(VcfRepository.ALT, "C");
		resultMap1.put("score", 5);
		Entity resultEntity1 = new MapEntity(resultMap1);

		Map<String, Object> resultMap2 = new LinkedHashMap<String, Object>();
		resultMap2.put(VcfRepository.REF, "A");
		resultMap2.put(VcfRepository.ALT, "G");
		resultMap2.put("score", 4);
		Entity resultEntity2 = new MapEntity(resultMap2);

		Map<String, Object> resultMap3 = new LinkedHashMap<String, Object>();
		resultMap3.put(VcfRepository.REF, "T");
		resultMap3.put(VcfRepository.ALT, "C");
		resultMap3.put("score", 3);
		Entity resultEntity3 = new MapEntity(resultMap3);

		Iterable<Entity> results = Arrays.asList(resultEntity1, resultEntity2, resultEntity3);

		Map<String, Object> annotatedMap1 = new LinkedHashMap<String, Object>();
		annotatedMap1.put(VcfRepository.REF, "T");
		annotatedMap1.put(VcfRepository.ALT, "C");
		Entity annotatedEntity1 = new MapEntity(annotatedMap1);

		Map<String, Object> annotatedMap2 = new LinkedHashMap<String, Object>();
		annotatedMap2.put(VcfRepository.REF, "A");
		annotatedMap2.put(VcfRepository.ALT, "G");
		Entity annotatedEntity2 = new MapEntity(annotatedMap2);

		VariantResultFilter filter = new VariantResultFilter();

		assertEquals(Optional.of(resultEntity1), filter.filterResults(results, annotatedEntity1));
		assertEquals(Optional.of(resultEntity2), filter.filterResults(results, annotatedEntity2));
	}

	@Test
	public void getRequiredAttributes()
	{
		assertEquals(Arrays.asList(VcfRepository.REF_META, VcfRepository.ALT_META),
				new VariantResultFilter().getRequiredAttributes());
	}
}
