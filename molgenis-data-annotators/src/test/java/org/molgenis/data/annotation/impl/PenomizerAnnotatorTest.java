package org.molgenis.data.annotation.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator;
import org.molgenis.data.annotation.provider.UrlPinger;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by charbonb on 06/05/15.
 */
public class PenomizerAnnotatorTest
{
	public DefaultEntityMetaData metaDataCanAnnotate = new DefaultEntityMetaData("test");
	public DefaultEntityMetaData metaDataCantAnnotate = new DefaultEntityMetaData("test");

	public AttributeMetaData attributeMetaDataChrom = new DefaultAttributeMetaData(VcfRepository.CHROM,
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public AttributeMetaData attributeMetaDataPos = new DefaultAttributeMetaData(VcfRepository.POS,
			MolgenisFieldTypes.FieldTypeEnum.LONG);
	public AttributeMetaData attributeMetaDataRef = new DefaultAttributeMetaData(VcfRepository.REF,
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public AttributeMetaData attributeMetaDataAlt = new DefaultAttributeMetaData(VcfRepository.ALT,
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public AttributeMetaData attributeMetaDataCantAnnotateChrom = new DefaultAttributeMetaData(VcfRepository.CHROM,
			MolgenisFieldTypes.FieldTypeEnum.LONG);
	public ArrayList<Entity> input = new ArrayList<>();
	public Entity entity;

	public MolgenisSettings settings = mock(MolgenisSettings.class);

	public PhenomizerServiceAnnotator annotator;
	private ArrayList<Entity> entities;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataChrom);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataAlt);
		metaDataCanAnnotate.setIdAttribute(attributeMetaDataChrom.getName());
		metaDataCanAnnotate.addAttributeMetaData(VcfRepository.FILTER_META);
		metaDataCanAnnotate.addAttributeMetaData(VcfRepository.QUAL_META);
		metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(SnpEffAnnotator.GENE_NAME,
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.getInfoPrefix() + "ANN",
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(HpoServiceAnnotator.HPO_TERMS,
				MolgenisFieldTypes.FieldTypeEnum.STRING));

		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataCantAnnotateChrom);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataAlt);
		metaDataCantAnnotate.addAttributeMetaData(VcfRepository.FILTER_META);
		metaDataCantAnnotate.addAttributeMetaData(VcfRepository.QUAL_META);
		metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.getInfoPrefix() + "ANN",
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(HpoServiceAnnotator.HPO_TERMS,
				MolgenisFieldTypes.FieldTypeEnum.INT));

		entity = new MapEntity(metaDataCanAnnotate);

		entity.set(attributeMetaDataChrom.getName(), "X");
		entity.set(attributeMetaDataPos.getName(), 1234567);
		entity.set(attributeMetaDataRef.getName(), "A");
		entity.set(attributeMetaDataAlt.getName(), "T");
		entity.set(VcfRepository.getInfoPrefix() + "ANN",
				"X\t12345\t.\tA\tT\tqual\tfilter\t0|1|2|TBP|4|5|6|7|8|9|10|11|12|13|14|15");
		entity.set(HpoServiceAnnotator.HPO_TERMS, "HP:0001300,HP:0007325,HP:0002015");

		entities = new ArrayList<>();
		entities.add(entity);

		UrlPinger urlPinger = mock(UrlPinger.class);
		MolgenisSettings molgenisSettings = mock(MolgenisSettings.class);
		when(molgenisSettings.getProperty(PhenomizerServiceAnnotator.KEY_PHENOMIZER_URL, "")).thenReturn("testUrl1");
		when(urlPinger.ping("testUrl1", 500)).thenReturn(true);

		annotator = new PhenomizerServiceAnnotator(molgenisSettings, urlPinger);
	}

	@Test
	public void canAnnotateTrueTest()
	{
		assertEquals(annotator.canAnnotate(metaDataCanAnnotate), "true");
	}

	@Test
	public void canAnnotateFalseTest()
	{
		assertEquals(annotator.canAnnotate(metaDataCantAnnotate), "a required attribute has the wrong datatype");
	}

	@Test
	public void annotateTest() throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(ResourceUtils.getFile(getClass(), "/phenomizer.txt")));
		List<Entity> results = annotator.annotateEntityWithPhenomizerPvalue(entity, in);
		assertEquals(results.get(0), getExpectedEntity());
	}

	public MapEntity getExpectedEntity()
	{
		DefaultEntityMetaData expectedEntityMetaData = metaDataCanAnnotate;
		DefaultAttributeMetaData attr1 = new DefaultAttributeMetaData(new DefaultAttributeMetaData(
				PhenomizerServiceAnnotator.PHENOMIZEROMIM, MolgenisFieldTypes.FieldTypeEnum.STRING));
		DefaultAttributeMetaData attr2 = new DefaultAttributeMetaData(new DefaultAttributeMetaData(
				PhenomizerServiceAnnotator.PHENOMIZERPVAL, MolgenisFieldTypes.FieldTypeEnum.STRING));

		DefaultAttributeMetaData compound = new DefaultAttributeMetaData("molgenis_annotated_PHENOMIZER",
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		compound.addAttributePart(attr1);
		compound.addAttributePart(attr2);

		expectedEntityMetaData.addAttributeMetaData(compound);

		MapEntity expectedEntity = new MapEntity(entity, expectedEntityMetaData);

		expectedEntity.set(PhenomizerServiceAnnotator.PHENOMIZEROMIM, "OMIM:607136");
		expectedEntity.set(PhenomizerServiceAnnotator.PHENOMIZERPVAL, DataConverter.toDouble(0.0155));
		return expectedEntity;
	}
}
