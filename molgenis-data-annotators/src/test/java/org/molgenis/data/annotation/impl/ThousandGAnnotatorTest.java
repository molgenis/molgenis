package org.molgenis.data.annotation.impl;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ThousandGAnnotatorTest
{
	private DefaultEntityMetaData metaDataCanAnnotate;
	private DefaultEntityMetaData metaDataCantAnnotate;
	private ThousandGenomesServiceAnnotator annotator;
	private AttributeMetaData attributeMetaDataChrom;
	private AttributeMetaData attributeMetaDataPos;
	private AttributeMetaData attributeMetaDataRef;
	private AttributeMetaData attributeMetaDataAlt;
	private AttributeMetaData attributeMetaDataCantAnnotateChrom;
	private ArrayList<Entity> input1;
	private Entity entity1;
    private ArrayList<Entity> input2;
    private Entity entity2;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		metaDataCanAnnotate = new DefaultEntityMetaData("test");
        metaDataCantAnnotate = new DefaultEntityMetaData("test");

        File file = new File(ResourceUtils.getFile(getClass(), "/ALL.chr1.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz").getPath());
        String absolutePath = file.getAbsolutePath();
        String filePath = absolutePath.
                substring(0,absolutePath.lastIndexOf(File.separator));

        MolgenisSettings settings = mock(MolgenisSettings.class);
        when(settings.getProperty(ThousandGenomesServiceAnnotator.THGEN_DIRECTORY_LOCATION_PROPERTY)).thenReturn(filePath);

        attributeMetaDataChrom = new DefaultAttributeMetaData(VcfRepository.CHROM, FieldTypeEnum.STRING);
        attributeMetaDataPos = new DefaultAttributeMetaData(VcfRepository.POS, FieldTypeEnum.LONG);
        attributeMetaDataRef = new DefaultAttributeMetaData(VcfRepository.REF, FieldTypeEnum.STRING);
        attributeMetaDataAlt = new DefaultAttributeMetaData(VcfRepository.ALT, FieldTypeEnum.STRING);
        attributeMetaDataCantAnnotateChrom = new DefaultAttributeMetaData(VcfRepository.CHROM, FieldTypeEnum.LONG);

		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataChrom);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataAlt);
		metaDataCanAnnotate.setIdAttribute(attributeMetaDataChrom.getName());

		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataCantAnnotateChrom);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataAlt);

        entity1 = new MapEntity(metaDataCanAnnotate);
        entity1.set(VcfRepository.CHROM,"1");
		entity1.set(VcfRepository.POS, 10352);
		entity1.set(VcfRepository.REF, "T");
		entity1.set(VcfRepository.ALT, "TA");
		input1 = new ArrayList<Entity>();
		input1.add(entity1);

        entity2 = new MapEntity(metaDataCanAnnotate);
        entity2.set(VcfRepository.CHROM,"1");
        entity2.set(VcfRepository.POS, 123456);
        entity2.set(VcfRepository.REF, "G");
        entity2.set(VcfRepository.ALT, "A");
        input2 = new ArrayList<>();
        input2.add(entity2);

		annotator = new ThousandGenomesServiceAnnotator(settings, null);
	}

	@Test
	public void testAnnotate()
	{
        DefaultEntityMetaData annotatedMetadata = new DefaultEntityMetaData("test");
        annotatedMetadata.addAttributeMetaData(attributeMetaDataCantAnnotateChrom);
        annotatedMetadata.addAttributeMetaData(attributeMetaDataPos);
        annotatedMetadata.addAttributeMetaData(attributeMetaDataRef);
        annotatedMetadata.addAttributeMetaData(attributeMetaDataAlt);
        annotatedMetadata.setIdAttribute(attributeMetaDataCantAnnotateChrom.getName());
        annotatedMetadata.addAttributeMetaData(new DefaultAttributeMetaData(ThousandGenomesServiceAnnotator.THGEN_MAF, FieldTypeEnum.DECIMAL));

        Iterator<Entity> results = annotator.annotate(input1);

        MapEntity mapEntity = new MapEntity(entity1, annotatedMetadata);
        mapEntity.set(ThousandGenomesServiceAnnotator.THGEN_MAF,0.4375);

        assertEquals(results.next(),mapEntity);
	}

    @Test
    public void testAnnotateNoResult()
    {
        DefaultEntityMetaData annotatedMetadata = new DefaultEntityMetaData("test");
        annotatedMetadata.addAttributeMetaData(attributeMetaDataCantAnnotateChrom);
        annotatedMetadata.addAttributeMetaData(attributeMetaDataPos);
        annotatedMetadata.addAttributeMetaData(attributeMetaDataRef);
        annotatedMetadata.addAttributeMetaData(attributeMetaDataAlt);
        annotatedMetadata.setIdAttribute(attributeMetaDataCantAnnotateChrom.getName());
        annotatedMetadata.addAttributeMetaData(new DefaultAttributeMetaData(ThousandGenomesServiceAnnotator.THGEN_MAF, FieldTypeEnum.DECIMAL));

        Iterator<Entity> results = annotator.annotate(input2);

        MapEntity mapEntity = new MapEntity(entity2, annotatedMetadata);
        mapEntity.set(ThousandGenomesServiceAnnotator.THGEN_MAF,null);

        assertEquals(results.next(),mapEntity);
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
