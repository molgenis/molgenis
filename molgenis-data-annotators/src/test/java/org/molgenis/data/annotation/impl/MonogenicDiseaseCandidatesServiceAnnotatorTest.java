package org.molgenis.data.annotation.impl;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.impl.CGDAnnotator;
import org.molgenis.data.annotation.entity.impl.ExacAnnotator;
import org.molgenis.data.annotation.entity.impl.GoNLAnnotator;
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
public class MonogenicDiseaseCandidatesServiceAnnotatorTest
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
	public void beforeMethod() throws Exception
	{
		annotator = new MonogenicDiseaseCandidatesServiceAnnotator();

		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataChrom);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataAlt);
		metaDataCanAnnotate.setIdAttribute(attributeMetaDataChrom.getName());
		metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(
				MonogenicDiseaseCandidatesServiceAnnotator.ANNOTATIONFIELD, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(
				ThousandGenomesServiceAnnotator.THGEN_MAF, MolgenisFieldTypes.FieldTypeEnum.DECIMAL));
		metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(ExacAnnotator.EXAC_AF,
				MolgenisFieldTypes.FieldTypeEnum.DECIMAL));
		metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(GoNLAnnotator.BC_GONL_MAF,
				MolgenisFieldTypes.FieldTypeEnum.DECIMAL));
		metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(
				CGDAnnotator.CGDAttributeName.GENERALIZED_INHERITANCE.getAttributeName(),
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(
				CGDAnnotator.CGDAttributeName.INHERITANCE.getAttributeName(),
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
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
		metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(
				MonogenicDiseaseCandidatesServiceAnnotator.ANNOTATIONFIELD, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(
				ThousandGenomesServiceAnnotator.THGEN_MAF, MolgenisFieldTypes.FieldTypeEnum.DECIMAL));
		metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(ExacAnnotator.EXAC_AF,
				MolgenisFieldTypes.FieldTypeEnum.DECIMAL));
		metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(GoNLAnnotator.BC_GONL_MAF,
				MolgenisFieldTypes.FieldTypeEnum.DECIMAL));
		metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(
				CGDAnnotator.CGDAttributeName.GENERALIZED_INHERITANCE.getAttributeName(),
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(
				CGDAnnotator.CGDAttributeName.INHERITANCE.getAttributeName(),
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
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
		entity.set(MonogenicDiseaseCandidatesServiceAnnotator.ANNOTATIONFIELD, "1|2|"
				+ SnpEffAnnotator.Impact.HIGH + "|4|5");
		entity.set(ThousandGenomesServiceAnnotator.THGEN_MAF, 0.1);
		entity.set(ExacAnnotator.EXAC_AF, 0.1);
		entity.set(GoNLAnnotator.BC_GONL_MAF, 0.2);
		entity.set(
				CGDAnnotator.CGDAttributeName.GENERALIZED_INHERITANCE.getAttributeName(),
				"DOMINANT");
		entity.set(CGDAnnotator.CGDAttributeName.INHERITANCE.getAttributeName(), "TEST2");
		entity.set(VcfRepository.SAMPLES, sampleEntities);

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
				MonogenicDiseaseCandidatesServiceAnnotator.MONOGENICDISEASECANDIDATE,
				MolgenisFieldTypes.FieldTypeEnum.STRING));

		DefaultAttributeMetaData compound = new DefaultAttributeMetaData("molgenis_annotated_MONOGENICDISEASE",
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		compound.addAttributePart(attr);

		expectedEntityMetaData.addAttributeMetaData(attr);
		expectedEntityMetaData.addAttributeMetaData(compound);

		MapEntity expectedEntity = new MapEntity(entity, expectedEntityMetaData);

		expectedEntity.set(MonogenicDiseaseCandidatesServiceAnnotator.MONOGENICDISEASECANDIDATE,
				MonogenicDiseaseCandidatesServiceAnnotator.outcome.EXCLUDED);
		return expectedEntity;
	}
}
