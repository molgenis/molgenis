package org.molgenis.data.annotation.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.impl.datastructures.HGNCLocations;
import org.molgenis.data.annotation.provider.HgncLocationsProvider;
import org.molgenis.data.annotation.AbstractAnnotatorTest;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HgncSymbolServiceAnnotatorTest extends AbstractAnnotatorTest
{
	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		HgncLocationsProvider hgncLocationsProvider = mock(HgncLocationsProvider.class);
		Map<String, HGNCLocations> hgncLocations = Collections.singletonMap("BRCA1", new HGNCLocations("BRCA1",
				41196312l - 10, 41277500l + 10, "17"));
		when(hgncLocationsProvider.getHgncLocations()).thenReturn(hgncLocations);

		annotator = new HgncSymbolServiceAnnotator(hgncLocationsProvider);

		entity.set(VcfRepository.CHROM, "17");
		entity.set(VcfRepository.POS, new Long(41196312));

		input.add(entity);
	}

	@Test
	public void annotateTest()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(HgncSymbolServiceAnnotator.HGNC_SYMBOL, "BRCA1");

		Entity expectedEntity = new MapEntity(resultMap);

		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input);

		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(HgncSymbolServiceAnnotator.HGNC_SYMBOL),
				expectedEntity.get(HgncSymbolServiceAnnotator.HGNC_SYMBOL));

	}
}
