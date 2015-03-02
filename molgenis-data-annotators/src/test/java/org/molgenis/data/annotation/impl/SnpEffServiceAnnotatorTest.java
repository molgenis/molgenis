package org.molgenis.data.annotation.impl;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.*;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.vcf.VcfRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;


import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class SnpEffServiceAnnotatorTest
{

    private SnpEffServiceAnnotator annotator;
    private ArrayList<Entity> entities;

    @BeforeMethod
    public void beforeMethod() throws IOException
    {
        QueryRule chromRule = new QueryRule(VcfRepository.CHROM,
                QueryRule.Operator.EQUALS, "X");
        Query query = new QueryImpl(chromRule).and().eq(VcfRepository.POS, "12345");

        DefaultEntityMetaData metaData = new DefaultEntityMetaData("TestEntity");
        metaData.addAttributeMetaData(new DefaultAttributeMetaData("ID", MolgenisFieldTypes.FieldTypeEnum.STRING));
        metaData.setIdAttribute("ID");
        metaData.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.CHROM, MolgenisFieldTypes.FieldTypeEnum.STRING));
        metaData.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.POS, MolgenisFieldTypes.FieldTypeEnum.LONG));
        metaData.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.REF, MolgenisFieldTypes.FieldTypeEnum.STRING));
        metaData.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.ALT, MolgenisFieldTypes.FieldTypeEnum.STRING));
        Entity entity1 = new MapEntity(metaData);
        entity1.set(VcfRepository.CHROM,"1");
        entity1.set(VcfRepository.POS,1234);
        entity1.set(VcfRepository.REF,"A");
        entity1.set(VcfRepository.ALT,"T");
        Entity entity2 = new MapEntity(metaData);
        entity2.set(VcfRepository.CHROM,"X");
        entity2.set(VcfRepository.POS,12345);
        entity2.set(VcfRepository.REF,"A");
        entity2.set(VcfRepository.ALT,"C");
        Entity entity3 = new MapEntity(metaData);
        entity3.set(VcfRepository.CHROM,"3");
        entity3.set(VcfRepository.POS,123);
        entity3.set(VcfRepository.REF,"G");
        entity3.set(VcfRepository.ALT,"T");
        entities = new ArrayList<>();
        entities.add(entity1);
        entities.add(entity2);
        entities.add(entity3);
        DataService dataService = mock(DataService.class);
        when(dataService.findOne("TestEntity", query)).thenReturn(entity2);
        annotator = new SnpEffServiceAnnotator(null,null,dataService);
    }


    @Test
    public void getInputTempFileTest(){
        try {
            File file = annotator.getInputTempFile(entities, "testfile");
            BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()));

            assertEquals(br.readLine(),"1\t1234\t.\tA\tT");
            assertEquals(br.readLine(),"X\t12345\t.\tA\tC");
            assertEquals(br.readLine(),"3\t123\t.\tG\tT");

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void parseOutputLineToEntityTest() {
        Entity result = annotator.parseOutputLineToEntity("X\t12345\t.\tA\tT\tqual\tfilter\t0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15","TestEntity");
        assertEquals(result.get(SnpEffServiceAnnotator.ANNOTATION),"1");
        assertEquals(result.get(SnpEffServiceAnnotator.PUTATIVE_IMPACT), "2");
        assertEquals(result.get(SnpEffServiceAnnotator.GENE_NAME), "3");
        assertEquals(result.get(SnpEffServiceAnnotator.GENE_ID), "4");
        assertEquals(result.get(SnpEffServiceAnnotator.FEATURE_TYPE), "5");
        assertEquals(result.get(SnpEffServiceAnnotator.FEATURE_ID), "6");
        assertEquals(result.get(SnpEffServiceAnnotator.TRANSCRIPT_BIOTYPE), "7");
        assertEquals(result.get(SnpEffServiceAnnotator.RANK_TOTAL), "8");
        assertEquals(result.get(SnpEffServiceAnnotator.HGVS_C), "9");
        assertEquals(result.get(SnpEffServiceAnnotator.HGVS_P), "10");
        assertEquals(result.get(SnpEffServiceAnnotator.C_DNA_POSITION), "11");
        assertEquals(result.get(SnpEffServiceAnnotator.CDS_POSITION), "12");
        assertEquals(result.get(SnpEffServiceAnnotator.PROTEIN_POSITION), "13");
        assertEquals(result.get(SnpEffServiceAnnotator.DISTANCE_TO_FEATURE), "14");
        assertEquals(result.get(SnpEffServiceAnnotator.ERRORS), "15");
        assertEquals(result.get(SnpEffServiceAnnotator.LOF), "");
        assertEquals(result.get(SnpEffServiceAnnotator.NMD), "");
    }

}
