package org.molgenis.data.annotation;

import org.molgenis.data.meta.model.EntityMetaData;

public abstract class AnnotatorTestData
{
	public EntityMetaData metaDataCanAnnotate = null; //new EntityMetaData("test"); FIXME
	public EntityMetaData metaDataCantAnnotate = null; // new EntityMetaData("test"); FIXME
	//
	//	public AttributeMetaData attributeMetaDataChrom = new AttributeMetaData(CHROM, STRING);
	//	public AttributeMetaData attributeMetaDataPos = new AttributeMetaData(POS, LONG);
	//	public AttributeMetaData attributeMetaDataRef = new AttributeMetaData(REF, STRING);
	//	public AttributeMetaData attributeMetaDataAlt = new AttributeMetaData(ALT, STRING);
	//	public AttributeMetaData attributeMetaDataCantAnnotateChrom = new AttributeMetaData(CHROM,
	//			LONG);
	//	public ArrayList<Entity> input = new ArrayList<>();
	//	public ArrayList<Entity> input1 = new ArrayList<>();
	//	public ArrayList<Entity> input2 = new ArrayList<>();
	//	public ArrayList<Entity> input3 = new ArrayList<>();
	//	public ArrayList<Entity> input4 = new ArrayList<>();
	//	public Entity entity;
	//	public Entity entity1;
	//	public Entity entity2;
	//	public Entity entity3;
	//	public Entity entity4;
	//
	//	public ArrayList<Entity> entities;
	//
	//	public AnnotatorTestData()
	//	{
	//		setValues();
	//	}
	//
	public RepositoryAnnotator annotator;
	//
	//	public void setValues()
	//	{
	//		metaDataCanAnnotate.addAttribute(attributeMetaDataChrom, ROLE_ID);
	//		metaDataCanAnnotate.addAttribute(attributeMetaDataPos);
	//		metaDataCanAnnotate.addAttribute(attributeMetaDataRef);
	//		metaDataCanAnnotate.addAttribute(attributeMetaDataAlt);
	//
	//		metaDataCantAnnotate.addAttribute(attributeMetaDataCantAnnotateChrom);
	//		metaDataCantAnnotate.addAttribute(attributeMetaDataPos);
	//		metaDataCantAnnotate.addAttribute(attributeMetaDataRef);
	//		metaDataCantAnnotate.addAttribute(attributeMetaDataAlt);
	//
	//		entity = new MapEntity(metaDataCanAnnotate);
	//		entity1 = new MapEntity(metaDataCanAnnotate);
	//		entity2 = new MapEntity(metaDataCanAnnotate);
	//		entity3 = new MapEntity(metaDataCanAnnotate);
	//		entity4 = new MapEntity(metaDataCanAnnotate);
	//
	//		entities = new ArrayList<>();
	//		entities.add(entity);
	//	}
}