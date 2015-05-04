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
public class MonogenicDiseaseCandidatesServiceAnnotatorTest
{
    public DefaultEntityMetaData metaDataCanAnnotate = new DefaultEntityMetaData("test");
    public DefaultEntityMetaData metaDataCantAnnotate = new DefaultEntityMetaData("test");

    public AttributeMetaData attributeMetaDataChrom = new DefaultAttributeMetaData(VcfRepository.CHROM,
            MolgenisFieldTypes.FieldTypeEnum.STRING);
    public AttributeMetaData attributeMetaDataPos = new DefaultAttributeMetaData(VcfRepository.POS, MolgenisFieldTypes.FieldTypeEnum.LONG);
    public AttributeMetaData attributeMetaDataRef = new DefaultAttributeMetaData(VcfRepository.REF,
            MolgenisFieldTypes.FieldTypeEnum.STRING);
    public AttributeMetaData attributeMetaDataAlt = new DefaultAttributeMetaData(VcfRepository.ALT,
            MolgenisFieldTypes.FieldTypeEnum.STRING);
    public AttributeMetaData attributeMetaDataCantAnnotateChrom = new DefaultAttributeMetaData(VcfRepository.CHROM,
            MolgenisFieldTypes.FieldTypeEnum.LONG);
    public ArrayList<Entity> input = new ArrayList<Entity>();
    public ArrayList<Entity> input1 = new ArrayList<Entity>();
    public ArrayList<Entity> input2 = new ArrayList<Entity>();
    public ArrayList<Entity> input3 = new ArrayList<Entity>();
    public ArrayList<Entity> input4 = new ArrayList<Entity>();
    public Entity entity;
    public Entity entity1;
    public Entity entity2;
    public Entity entity3;
    public Entity entity4;

    public AttributeMetaData attributeMetaDataCantAnnotateFeature;
    public AttributeMetaData attributeMetaDataCantAnnotatePos;
    public AttributeMetaData attributeMetaDataCantAnnotateRef;
    public AttributeMetaData attributeMetaDataCantAnnotateAlt;

    public MolgenisSettings settings = mock(MolgenisSettings.class);

    public RepositoryAnnotator annotator;
@BeforeMethod
	public void beforeMethod() throws IOException
	{
		annotator = new MonogenicDiseaseCandidatesServiceAnnotator(settings, null);

        metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataChrom);
        metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataPos);
        metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataRef);
        metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataAlt);
        metaDataCanAnnotate.setIdAttribute(attributeMetaDataChrom.getName());
        metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(ThousandGenomesServiceAnnotator.THGEN_MAF, MolgenisFieldTypes.FieldTypeEnum.DECIMAL));
        metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(ExACServiceAnnotator.EXAC_MAF, MolgenisFieldTypes.FieldTypeEnum.DECIMAL));
        metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(GoNLServiceAnnotator.GONL_MAF, MolgenisFieldTypes.FieldTypeEnum.DECIMAL));
        metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(ClinicalGenomicsDatabaseServiceAnnotator.GENERALIZED_INHERITANCE, MolgenisFieldTypes.FieldTypeEnum.TEXT));
        metaDataCanAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(ClinicalGenomicsDatabaseServiceAnnotator.INHERITANCE, MolgenisFieldTypes.FieldTypeEnum.TEXT));

        metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataCantAnnotateChrom);
        metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataPos);
        metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataRef);
        metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataAlt);
        metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(ThousandGenomesServiceAnnotator.THGEN_MAF, MolgenisFieldTypes.FieldTypeEnum.DECIMAL));
        metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(ExACServiceAnnotator.EXAC_MAF, MolgenisFieldTypes.FieldTypeEnum.DECIMAL));
        metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(GoNLServiceAnnotator.GONL_MAF, MolgenisFieldTypes.FieldTypeEnum.DECIMAL));
        metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(ClinicalGenomicsDatabaseServiceAnnotator.GENERALIZED_INHERITANCE, MolgenisFieldTypes.FieldTypeEnum.TEXT));
        metaDataCantAnnotate.addAttributeMetaData(new DefaultAttributeMetaData(ClinicalGenomicsDatabaseServiceAnnotator.INHERITANCE, MolgenisFieldTypes.FieldTypeEnum.TEXT));

        entity = new MapEntity(metaDataCanAnnotate);
        entity1 = new MapEntity(metaDataCanAnnotate);
        entity2 = new MapEntity(metaDataCanAnnotate);
        entity3 = new MapEntity(metaDataCanAnnotate);
        entity4 = new MapEntity(metaDataCanAnnotate);
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
}
