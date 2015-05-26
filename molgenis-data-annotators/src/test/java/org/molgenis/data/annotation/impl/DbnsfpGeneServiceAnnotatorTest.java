package org.molgenis.data.annotation.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.impl.datastructures.HGNCLocations;
import org.molgenis.data.annotation.provider.HgncLocationsProvider;
import org.molgenis.data.annotation.AbstractAnnotatorTest;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DbnsfpGeneServiceAnnotatorTest extends AbstractAnnotatorTest
{
	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		when(settings.getProperty(DbnsfpGeneServiceAnnotator.GENE_FILE_LOCATION_PROPERTY)).thenReturn(
				ResourceUtils.getFile(getClass(), "/dbNSFP_gene_example.txt").getPath());

		entity.set(VcfRepository.CHROM, "12");
		entity.set(VcfRepository.POS, new Long(6968292));

		input.add(entity);

		AnnotationService annotationService = mock(AnnotationService.class);
		HgncLocationsProvider hgncLocationsProvider = mock(HgncLocationsProvider.class);
		Map<String, HGNCLocations> locationsMap = Collections.singletonMap("USP5", new HGNCLocations("USP5", 6961292l,
				6975796l, "12"));
		when(hgncLocationsProvider.getHgncLocations()).thenReturn(locationsMap);
		annotator = new DbnsfpGeneServiceAnnotator(settings, annotationService, hgncLocationsProvider);
	}

	@Test
	public void annotateTest()
	{
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		resultMap.put(DbnsfpGeneServiceAnnotator.GENE_NAME, "USP5");
		resultMap.put(DbnsfpGeneServiceAnnotator.ENSEMBL_GENE, "ENSG00000111667");
		resultMap.put(DbnsfpGeneServiceAnnotator.CHR, "12");
		resultMap.put(DbnsfpGeneServiceAnnotator.GENE_OLD_NAMES, ".");
		resultMap.put(DbnsfpGeneServiceAnnotator.GENE_OTHER_NAMES, "IsoT");
		resultMap.put(DbnsfpGeneServiceAnnotator.UNIPROT_ACC, "P45974");
		resultMap.put(DbnsfpGeneServiceAnnotator.UNIPROT_ID, "UBP5_HUMAN");
		resultMap.put(DbnsfpGeneServiceAnnotator.ENTREZ_GENE_ID, "8078");
		resultMap.put(DbnsfpGeneServiceAnnotator.CCDS_ID, "CCDS31733.1;CCDS41743.1");
		resultMap.put(DbnsfpGeneServiceAnnotator.REFSEQ_ID, "NM_001098536");
		resultMap.put(DbnsfpGeneServiceAnnotator.UCSC_ID, "uc001qri.4");
		resultMap.put(DbnsfpGeneServiceAnnotator.MIM_ID, "601447");
		resultMap.put(DbnsfpGeneServiceAnnotator.ESSENTIAL_GENE, "E");

		Entity expectedEntity = new MapEntity(resultMap);

		Iterator<Entity> results = annotator.annotate(input);

		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(DbnsfpGeneServiceAnnotator.GENE_NAME),
				expectedEntity.get(DbnsfpGeneServiceAnnotator.GENE_NAME));
		assertEquals(resultEntity.get(DbnsfpGeneServiceAnnotator.ENSEMBL_GENE),
				expectedEntity.get(DbnsfpGeneServiceAnnotator.ENSEMBL_GENE));
		assertEquals(resultEntity.get(DbnsfpGeneServiceAnnotator.CHR),
				expectedEntity.get(DbnsfpGeneServiceAnnotator.CHR));
		assertEquals(resultEntity.get(DbnsfpGeneServiceAnnotator.GENE_OLD_NAMES),
				expectedEntity.get(DbnsfpGeneServiceAnnotator.GENE_OLD_NAMES));
		assertEquals(resultEntity.get(DbnsfpGeneServiceAnnotator.GENE_OTHER_NAMES),
				expectedEntity.get(DbnsfpGeneServiceAnnotator.GENE_OTHER_NAMES));
		assertEquals(resultEntity.get(DbnsfpGeneServiceAnnotator.UNIPROT_ACC),
				expectedEntity.get(DbnsfpGeneServiceAnnotator.UNIPROT_ACC));
		assertEquals(resultEntity.get(DbnsfpGeneServiceAnnotator.UNIPROT_ID),
				expectedEntity.get(DbnsfpGeneServiceAnnotator.UNIPROT_ID));
		assertEquals(resultEntity.get(DbnsfpGeneServiceAnnotator.ENTREZ_GENE_ID),
				expectedEntity.get(DbnsfpGeneServiceAnnotator.ENTREZ_GENE_ID));
		assertEquals(resultEntity.get(DbnsfpGeneServiceAnnotator.CCDS_ID),
				expectedEntity.get(DbnsfpGeneServiceAnnotator.CCDS_ID));
		assertEquals(resultEntity.get(DbnsfpGeneServiceAnnotator.REFSEQ_ID),
				expectedEntity.get(DbnsfpGeneServiceAnnotator.REFSEQ_ID));
		assertEquals(resultEntity.get(DbnsfpGeneServiceAnnotator.UCSC_ID),
				expectedEntity.get(DbnsfpGeneServiceAnnotator.UCSC_ID));
		assertEquals(resultEntity.get(DbnsfpGeneServiceAnnotator.MIM_ID),
				expectedEntity.get(DbnsfpGeneServiceAnnotator.MIM_ID));
		assertEquals(resultEntity.get(DbnsfpGeneServiceAnnotator.ESSENTIAL_GENE),
				expectedEntity.get(DbnsfpGeneServiceAnnotator.ESSENTIAL_GENE));
	}
}