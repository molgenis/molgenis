package org.molgenis.data.annotation.impl;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by charbonb on 04/05/15.
 */
public class GenePanelAnnotatorServiceTest
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
	public Entity sampleEntity;

	public DefaultEntityMetaData metaDataSample;

	public MolgenisSettings settings = mock(MolgenisSettings.class);

	public RepositoryAnnotator annotator;
	private ArrayList<Entity> entities;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		annotator = new GenePanelServiceAnnotator();

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

		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataCantAnnotateChrom);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataAlt);
		metaDataCantAnnotate.addAttributeMetaData(VcfRepository.FILTER_META);
		metaDataCantAnnotate.addAttributeMetaData(VcfRepository.QUAL_META);
		metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.getInfoPrefix() + "ANN",
				MolgenisFieldTypes.FieldTypeEnum.TEXT));

		entity = new MapEntity(metaDataCanAnnotate);

		entity.set(attributeMetaDataChrom.getName(), "X");
		entity.set(attributeMetaDataPos.getName(), 1234567);
		entity.set(attributeMetaDataRef.getName(), "A");
		entity.set(attributeMetaDataAlt.getName(), "T");
		entity.set(SnpEffAnnotator.GENE_NAME, "CHD7");
		entity.set(VcfRepository.getInfoPrefix() + "ANN",
				"X\t12345\t.\tA\tT\tqual\tfilter\t0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15");

		entities = new ArrayList<>();
		entities.add(entity);
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
	public void annotateTest()
	{
		assertEquals(annotator.annotate(entities).next().get(GenePanelServiceAnnotator.PANEL_SEVERELATEONSET),
				getExpectedEntity().get(GenePanelServiceAnnotator.PANEL_SEVERELATEONSET));
		assertEquals(annotator.annotate(entities).next().get(GenePanelServiceAnnotator.PANEL_CHARGE),
				getExpectedEntity().get(GenePanelServiceAnnotator.PANEL_CHARGE));
		assertEquals(annotator.annotate(entities).next().get(GenePanelServiceAnnotator.PANEL_ACMG), getExpectedEntity()
				.get(GenePanelServiceAnnotator.PANEL_ACMG));
	}

	public MapEntity getExpectedEntity()
	{
		DefaultEntityMetaData expectedEntityMetaData = metaDataCanAnnotate;
		DefaultAttributeMetaData attr1 = new DefaultAttributeMetaData(new DefaultAttributeMetaData(
				GenePanelServiceAnnotator.PANEL_SEVERELATEONSET, MolgenisFieldTypes.FieldTypeEnum.STRING));
		DefaultAttributeMetaData attr2 = new DefaultAttributeMetaData(new DefaultAttributeMetaData(
				GenePanelServiceAnnotator.PANEL_CHARGE, MolgenisFieldTypes.FieldTypeEnum.STRING));
		DefaultAttributeMetaData attr3 = new DefaultAttributeMetaData(new DefaultAttributeMetaData(
				GenePanelServiceAnnotator.PANEL_ACMG, MolgenisFieldTypes.FieldTypeEnum.STRING));

		DefaultAttributeMetaData compound = new DefaultAttributeMetaData("molgenis_annotated_DENOVO",
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		compound.addAttributePart(attr1);
		compound.addAttributePart(attr2);
		compound.addAttributePart(attr3);

		expectedEntityMetaData.addAttributeMetaData(compound);

		MapEntity expectedEntity = new MapEntity(entity, expectedEntityMetaData);

		expectedEntity.set(GenePanelServiceAnnotator.PANEL_SEVERELATEONSET, null);
		expectedEntity.set(GenePanelServiceAnnotator.PANEL_CHARGE, "TRUE");
		expectedEntity.set(GenePanelServiceAnnotator.PANEL_ACMG, null);
		return expectedEntity;
	}

}
