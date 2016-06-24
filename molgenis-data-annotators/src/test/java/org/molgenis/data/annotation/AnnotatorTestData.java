package org.molgenis.data.annotation;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import java.util.ArrayList;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;

public abstract class AnnotatorTestData
{
	public DefaultEntityMetaData metaDataCanAnnotate = new DefaultEntityMetaData("test");
	public DefaultEntityMetaData metaDataCantAnnotate = new DefaultEntityMetaData("test");

	public AttributeMetaData attributeMetaDataChrom = new DefaultAttributeMetaData(VcfRepository.CHROM,
			FieldTypeEnum.STRING);
	public AttributeMetaData attributeMetaDataPos = new DefaultAttributeMetaData(VcfRepository.POS, FieldTypeEnum.LONG);
	public AttributeMetaData attributeMetaDataRef = new DefaultAttributeMetaData(VcfRepository.REF,
			FieldTypeEnum.STRING);
	public AttributeMetaData attributeMetaDataAlt = new DefaultAttributeMetaData(VcfRepository.ALT,
			FieldTypeEnum.STRING);
	public AttributeMetaData attributeMetaDataCantAnnotateChrom = new DefaultAttributeMetaData(VcfRepository.CHROM,
			FieldTypeEnum.LONG);
	public ArrayList<Entity> input = new ArrayList<>();
	public ArrayList<Entity> input1 = new ArrayList<>();
	public ArrayList<Entity> input2 = new ArrayList<>();
	public ArrayList<Entity> input3 = new ArrayList<>();
	public ArrayList<Entity> input4 = new ArrayList<>();
	public Entity entity;
	public Entity entity1;
	public Entity entity2;
	public Entity entity3;
	public Entity entity4;

	public ArrayList<Entity> entities;

	public AnnotatorTestData()
	{
		setValues();
	}

	public RepositoryAnnotator annotator;

	public void setValues()
	{
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataChrom, ROLE_ID);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataAlt);

		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataCantAnnotateChrom);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataAlt);

		entity = new MapEntity(metaDataCanAnnotate);
		entity1 = new MapEntity(metaDataCanAnnotate);
		entity2 = new MapEntity(metaDataCanAnnotate);
		entity3 = new MapEntity(metaDataCanAnnotate);
		entity4 = new MapEntity(metaDataCanAnnotate);

		entities = new ArrayList<>();
		entities.add(entity);
	}
}