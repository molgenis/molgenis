package org.molgenis.data.annotation;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.vcf.model.VcfAttributes.*;

public abstract class AnnotatorTestData extends AbstractMolgenisSpringTest
{
	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	VcfAttributes vcfAttributes;

	public EntityType metaDataCanAnnotate = entityTypeFactory.create("test");
	public EntityType metaDataCantAnnotate = entityTypeFactory.create("test");

	public Attribute attributeChrom = attributeFactory.create().setName(CHROM).setDataType(STRING);
	public Attribute attributePos = attributeFactory.create().setName(POS).setDataType(LONG);
	public Attribute attributeRef = attributeFactory.create().setName(REF).setDataType(STRING);
	public Attribute attributeAlt = attributeFactory.create().setName(ALT).setDataType(STRING);
	public Attribute attributeCantAnnotateChrom = attributeFactory.create().setName(CHROM).setDataType(LONG);
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
		metaDataCanAnnotate.addAttribute(attributeChrom, ROLE_ID);
		metaDataCanAnnotate.addAttribute(attributePos);
		metaDataCanAnnotate.addAttribute(attributeRef);
		metaDataCanAnnotate.addAttribute(attributeAlt);

		metaDataCantAnnotate.addAttribute(attributeCantAnnotateChrom);
		metaDataCantAnnotate.addAttribute(attributePos);
		metaDataCantAnnotate.addAttribute(attributeRef);
		metaDataCantAnnotate.addAttribute(attributeAlt);

		entity = new DynamicEntity(metaDataCanAnnotate);
		entity1 = new DynamicEntity(metaDataCanAnnotate);
		entity2 = new DynamicEntity(metaDataCanAnnotate);
		entity3 = new DynamicEntity(metaDataCanAnnotate);
		entity4 = new DynamicEntity(metaDataCanAnnotate);

		entities = new ArrayList<>();
		entities.add(entity);
	}
}