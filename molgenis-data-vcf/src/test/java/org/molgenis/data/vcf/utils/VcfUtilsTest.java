package org.molgenis.data.vcf.utils;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.util.ArrayList;

import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.vcf.model.VcfAttributes.*;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { VcfUtilsTest.Config.class })
public class VcfUtilsTest extends AbstractMolgenisSpringTest
{

	@Autowired
	EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	VcfAttributes vcfAttributes;

	private EntityMetaData annotatedEntityMetadata;
	public EntityMetaData metaDataCanAnnotate;
	public EntityMetaData metaDataCantAnnotate;

	public AttributeMetaData attributeMetaDataChrom;
	public AttributeMetaData attributeMetaDataPos;
	public AttributeMetaData attributeMetaDataRef;
	public AttributeMetaData attributeMetaDataAlt;
	public AttributeMetaData attributeMetaDataCantAnnotateChrom;

	public ArrayList<Entity> input = new ArrayList<Entity>();
	public Entity entity;
	public Entity entity1;
	public Entity entity2;
	public Entity entity3;
	public Entity entity4;

	public ArrayList<Entity> entities;

	@BeforeClass
	public void beforeClass()
	{
		annotatedEntityMetadata = entityMetaDataFactory.create().setName("test");
		metaDataCanAnnotate = entityMetaDataFactory.create().setName("test");
		metaDataCantAnnotate = entityMetaDataFactory.create().setName("test");

		attributeMetaDataChrom = attributeMetaDataFactory.create().setName(CHROM).setDataType(STRING);
		attributeMetaDataPos = attributeMetaDataFactory.create().setName(POS).setDataType(LONG);
		attributeMetaDataRef = attributeMetaDataFactory.create().setName(REF).setDataType(STRING);
		attributeMetaDataAlt = attributeMetaDataFactory.create().setName(ALT).setDataType(STRING);
		attributeMetaDataCantAnnotateChrom = attributeMetaDataFactory.create().setName(CHROM).setDataType(LONG);
	}

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
			/*
			 * 1 10050000 test21 G A . PASS AC=21;AN=22;GTC=0,1,10 1 10050001 test22 G A . PASS AC=22;AN=23;GTC=1,2,11 1
			 * 10050002 test23 G A . PASS AC=23;AN=24;GTC=2,3,12
			 */
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

		metaDataCanAnnotate.addAttribute(attributeMetaDataFactory.create().setName(ID).setDataType(STRING));
		metaDataCanAnnotate.addAttribute(attributeMetaDataFactory.create().setName(QUAL).setDataType(STRING));
		metaDataCanAnnotate.addAttribute(attributeMetaDataFactory.create().setName(FILTER).setDataType(STRING));
		AttributeMetaData INFO = attributeMetaDataFactory.create().setName("INFO").setDataType(COMPOUND);
		AttributeMetaData AC = attributeMetaDataFactory.create().setName("AC").setDataType(STRING);
		AttributeMetaData AN = attributeMetaDataFactory.create().setName("AN").setDataType(STRING);
		AttributeMetaData GTC = attributeMetaDataFactory.create().setName("GTC").setDataType(STRING);
		INFO.addAttributePart(AC);
		INFO.addAttributePart(AN);
		INFO.addAttributePart(GTC);
		metaDataCanAnnotate.addAttribute(INFO);

		annotatedEntityMetadata.addAttribute(attributeMetaDataChrom, ROLE_ID);
		annotatedEntityMetadata.addAttribute(attributeMetaDataPos);
		annotatedEntityMetadata.addAttribute(attributeMetaDataRef);
		annotatedEntityMetadata.addAttribute(attributeMetaDataAlt);

		annotatedEntityMetadata.addAttribute(attributeMetaDataFactory.create().setName(ID).setDataType(STRING));
		annotatedEntityMetadata.addAttribute(attributeMetaDataFactory.create().setName(QUAL).setDataType(STRING));
		annotatedEntityMetadata.addAttribute((attributeMetaDataFactory.create().setName(FILTER).setDataType(STRING))
				.setDescription("Test that description is not: '" + VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION + "'"));
		INFO.addAttributePart(attributeMetaDataFactory.create().setName("ANNO").setDataType(STRING));
		annotatedEntityMetadata.addAttribute(INFO);

		entity1.set(VcfAttributes.CHROM, "1");
		entity1.set(VcfAttributes.POS, 10050000);
		entity1.set(VcfAttributes.ID, "test21");
		entity1.set(VcfAttributes.REF, "G");
		entity1.set(VcfAttributes.ALT, "A");
		entity1.set(VcfAttributes.QUAL, ".");
		entity1.set(VcfAttributes.FILTER, "PASS");
		entity1.set("AC", "21");
		entity1.set("AN", "22");
		entity1.set("GTC", "0,1,10");

		entity2.set(VcfAttributes.CHROM, "1");
		entity2.set(VcfAttributes.POS, 10050001);
		entity2.set(VcfAttributes.ID, "test22");
		entity2.set(VcfAttributes.REF, "G");
		entity2.set(VcfAttributes.ALT, "A");
		entity2.set(VcfAttributes.QUAL, ".");
		entity2.set(VcfAttributes.FILTER, "PASS");

		entity3.set(VcfAttributes.CHROM, "1");
		entity3.set(VcfAttributes.POS, 10050002);
		entity3.set(VcfAttributes.ID, "test23");
		entity3.set(VcfAttributes.REF, "G");
		entity3.set(VcfAttributes.ALT, "A");
		entity3.set(VcfAttributes.QUAL, ".");
		entity3.set(VcfAttributes.FILTER, "PASS");

		entities = new ArrayList<>();
		entities.add(entity1);
		entities.add(entity2);
		entities.add(entity3);
	}

	public void createId()
	{
		assertEquals(VcfUtils.createId(entity1), "yCiiynjHRAtJPcdn7jFDGA");
	}

	@Configuration
	@Import(VcfAttributes.class)
	public static class Config
	{
	}
}