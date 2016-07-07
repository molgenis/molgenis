package org.molgenis.data.annotation;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

import static org.molgenis.MolgenisFieldTypes.AttributeType.LONG;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.vcf.model.VcfAttributes.*;

public abstract class AnnotatorTestData extends AbstractMolgenisSpringTest
{
	@Autowired
	AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	VcfAttributes vcfAttributes;

	public EntityMetaData metaDataCanAnnotate = entityMetaDataFactory.create().setName("test");
	public EntityMetaData metaDataCantAnnotate = entityMetaDataFactory.create().setName("test");

	public AttributeMetaData attributeMetaDataChrom = attributeMetaDataFactory.create().setName(CHROM)
			.setDataType(STRING);
	public AttributeMetaData attributeMetaDataPos = attributeMetaDataFactory.create().setName(POS).setDataType(LONG);
	public AttributeMetaData attributeMetaDataRef = attributeMetaDataFactory.create().setName(REF).setDataType(STRING);
	public AttributeMetaData attributeMetaDataAlt = attributeMetaDataFactory.create().setName(ALT).setDataType(STRING);
	public AttributeMetaData attributeMetaDataCantAnnotateChrom = attributeMetaDataFactory.create().setName(CHROM)
			.setDataType(LONG);
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
		metaDataCanAnnotate.addAttribute(attributeMetaDataChrom, ROLE_ID);
		metaDataCanAnnotate.addAttribute(attributeMetaDataPos);
		metaDataCanAnnotate.addAttribute(attributeMetaDataRef);
		metaDataCanAnnotate.addAttribute(attributeMetaDataAlt);

		metaDataCantAnnotate.addAttribute(attributeMetaDataCantAnnotateChrom);
		metaDataCantAnnotate.addAttribute(attributeMetaDataPos);
		metaDataCantAnnotate.addAttribute(attributeMetaDataRef);
		metaDataCantAnnotate.addAttribute(attributeMetaDataAlt);

		entity = new DynamicEntity(metaDataCanAnnotate);
		entity1 = new DynamicEntity(metaDataCanAnnotate);
		entity2 = new DynamicEntity(metaDataCanAnnotate);
		entity3 = new DynamicEntity(metaDataCanAnnotate);
		entity4 = new DynamicEntity(metaDataCanAnnotate);

		entities = new ArrayList<>();
		entities.add(entity);
	}
}