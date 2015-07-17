package org.molgenis.data.annotation.impl;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.AbstractAnnotatorTest;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

/**
 * Created by charbonb on 04/05/15.
 */
public class DeNovoAnnotatorTest
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
		annotator = new DeNovoAnnotator();

		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataChrom);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataAlt);
		metaDataCanAnnotate.setIdAttribute(attributeMetaDataChrom.getName());
		metaDataCanAnnotate.addAttributeMetaData(VcfRepository.FILTER_META);
		metaDataCanAnnotate.addAttributeMetaData(VcfRepository.QUAL_META);
		metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.getInfoPrefix() + "ANN",
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData("ABHet",
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData("ABHom",
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData("SB",
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.SAMPLES,
				MolgenisFieldTypes.FieldTypeEnum.MREF));

		metaDataSample = new DefaultEntityMetaData("sample");
		metaDataSample
				.addAttributeMetaData(new DefaultAttributeMetaData("ID", MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaDataSample
				.addAttributeMetaData(new DefaultAttributeMetaData("GT", MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaDataSample.setIdAttribute("ID");

		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataCantAnnotateChrom);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataAlt);
		metaDataCantAnnotate.addAttributeMetaData(VcfRepository.FILTER_META);
		metaDataCantAnnotate.addAttributeMetaData(VcfRepository.QUAL_META);
		metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.getInfoPrefix() + "ANN",
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData("ABHet",
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData("ABHom",
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData("SB",
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.SAMPLES,
				MolgenisFieldTypes.FieldTypeEnum.MREF));

		entity = new MapEntity(metaDataCanAnnotate);
		sampleEntity = new MapEntity(metaDataSample);
		sampleEntity.set("ID", "id");
		sampleEntity.set("GT", "1/1");
		ArrayList<Entity> sampleEntities = new ArrayList<>();
		sampleEntities.add(sampleEntity);

		entity.set(attributeMetaDataChrom.getName(), "X");
		entity.set(attributeMetaDataPos.getName(), 1234567);
		entity.set(attributeMetaDataRef.getName(), "A");
		entity.set(attributeMetaDataAlt.getName(), "T");

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
		assertEquals(annotator.annotate(entities).next(), getExpectedEntity());
	}

	public MapEntity getExpectedEntity()
	{
		DefaultEntityMetaData expectedEntityMetaData = metaDataCanAnnotate;
		DefaultAttributeMetaData attr = new DefaultAttributeMetaData(new DefaultAttributeMetaData(
				DeNovoAnnotator.DENOVO, MolgenisFieldTypes.FieldTypeEnum.STRING));

		DefaultAttributeMetaData compound = new DefaultAttributeMetaData("molgenis_annotated_DENOVO",
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		compound.addAttributePart(attr);

		expectedEntityMetaData.addAttributeMetaData(compound);

		MapEntity expectedEntity = new MapEntity(entity, expectedEntityMetaData);

		expectedEntity.set(DeNovoAnnotator.DENOVO, 0);
		return expectedEntity;
	}
}
