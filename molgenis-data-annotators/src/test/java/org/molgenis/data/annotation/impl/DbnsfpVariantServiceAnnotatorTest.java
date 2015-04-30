package org.molgenis.data.annotation.impl;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.AbstractAnnotatorTest;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DbnsfpVariantServiceAnnotatorTest extends AbstractAnnotatorTest
{
	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		when(settings.getProperty(DbnsfpVariantServiceAnnotator.CHROMOSOME_FILE_LOCATION_PROPERTY)).thenReturn(
				ResourceUtils.getFile(getClass(), "/dbNSFP_variant_example_chr").getPath());

		entity.set(VcfRepository.CHROM, "Y");
		entity.set(VcfRepository.POS, new Long(2655049));
		entity.set(VcfRepository.REF, "C");
		entity.set(VcfRepository.ALT, "A");

		input.add(entity);

		annotator = new DbnsfpVariantServiceAnnotator(settings, null);
	}

	@Test
	public void annotateTest()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(DbnsfpVariantServiceAnnotator.AAREF, "Q");
		resultMap.put(DbnsfpVariantServiceAnnotator.AAALT, "H");
		resultMap.put(DbnsfpVariantServiceAnnotator.HG18_POS_1_COOR, "2715049");
		resultMap.put(DbnsfpVariantServiceAnnotator.GENENAME, "SRY");
		resultMap.put(DbnsfpVariantServiceAnnotator.UNIPROT_ACC, ".");
		resultMap.put(DbnsfpVariantServiceAnnotator.UNIPROT_ID, ".");
		resultMap.put(DbnsfpVariantServiceAnnotator.UNIPROT_AAPOS, ".");
		resultMap.put(DbnsfpVariantServiceAnnotator.INTERPRO_DOMAIN, ".");
		resultMap.put(DbnsfpVariantServiceAnnotator.CDS_STRAND, "-");
		resultMap.put(DbnsfpVariantServiceAnnotator.REFCODON, "CAG");

		Entity expectedEntity = new MapEntity(resultMap);

		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input);

		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.AAREF),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.AAREF));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.AAALT),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.AAALT));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.HG18_POS_1_COOR),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.HG18_POS_1_COOR));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.GENENAME),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.GENENAME));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.UNIPROT_ACC),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.UNIPROT_ACC));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.UNIPROT_ID),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.UNIPROT_ID));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.UNIPROT_AAPOS),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.UNIPROT_AAPOS));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.INTERPRO_DOMAIN),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.INTERPRO_DOMAIN));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.CDS_STRAND),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.CDS_STRAND));
		assertEquals(resultEntity.get(DbnsfpVariantServiceAnnotator.REFCODON),
				expectedEntity.get(DbnsfpVariantServiceAnnotator.REFCODON));
	}
}
