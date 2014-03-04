package org.molgenis.data.annotation.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OmimHpoAnnotatorTest
{
	private EntityMetaData metaDataCanAnnotate;
	private EntityMetaData metaDataCantAnnotate;
	private OmimHpoAnnotator annotator;
	private AttributeMetaData attributeMetaDataChrom;
	private AttributeMetaData attributeMetaDataPos;
	private AttributeMetaData attributeMetaDataCantAnnotateFeature;
	private AttributeMetaData attributeMetaDataCantAnnotateChrom;
	private AttributeMetaData attributeMetaDataCantAnnotatePos;
	private String annotatorOutput;
	private Entity entity;
	private ArrayList<Entity> input;
	
	@BeforeMethod
	public void beforeMethod(){
		annotator = new OmimHpoAnnotator();

		metaDataCanAnnotate = mock(EntityMetaData.class);

		attributeMetaDataChrom = mock(AttributeMetaData.class);
		attributeMetaDataPos = mock(AttributeMetaData.class);

		when(attributeMetaDataChrom.getName()).thenReturn(OmimHpoAnnotator.CHROMOSOME);
		when(attributeMetaDataPos.getName()).thenReturn(OmimHpoAnnotator.POSITION);

		when(attributeMetaDataChrom.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));
		when(attributeMetaDataPos.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.LONG.toString().toLowerCase()));

		when(metaDataCanAnnotate.getAttribute(OmimHpoAnnotator.CHROMOSOME))
				.thenReturn(attributeMetaDataChrom);
		when(metaDataCanAnnotate.getAttribute(OmimHpoAnnotator.POSITION)).thenReturn(attributeMetaDataPos);

		metaDataCantAnnotate = mock(EntityMetaData.class);
		attributeMetaDataCantAnnotateFeature = mock(AttributeMetaData.class);

		when(attributeMetaDataCantAnnotateFeature.getName()).thenReturn("otherID");
		when(attributeMetaDataCantAnnotateFeature.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));

		attributeMetaDataCantAnnotateChrom = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotateChrom.getName()).thenReturn(OmimHpoAnnotator.CHROMOSOME);
		when(attributeMetaDataCantAnnotateFeature.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.INT.toString().toLowerCase()));

		attributeMetaDataCantAnnotatePos = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotatePos.getName()).thenReturn(OmimHpoAnnotator.POSITION);
		when(attributeMetaDataCantAnnotatePos.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));

		when(metaDataCantAnnotate.getAttribute(OmimHpoAnnotator.CHROMOSOME)).thenReturn(
				attributeMetaDataChrom);
		when(metaDataCantAnnotate.getAttribute(OmimHpoAnnotator.POSITION)).thenReturn(
				attributeMetaDataCantAnnotatePos);

		entity = mock(Entity.class);

		when(entity.getString(OmimHpoAnnotator.CHROMOSOME)).thenReturn("2");
		when(entity.getLong(OmimHpoAnnotator.POSITION)).thenReturn(new Long(58453844l));

		input = new ArrayList<Entity>();
		input.add(entity);

		annotatorOutput = "";
	}
	
	@Test
	public void annotateTest()
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();

		
		//Full_info_HPO='[HPOTerm{id='HP:0001639', description='Hypertrophic cardiomyopathy', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000532', description='Chorioretinal abnormality', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000505', description='Visual impairment', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000388', description='Otitis media', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000613', description='Photophobia', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0001956', description='Truncal obesity', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000858', description='Menstrual irregularities', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0001970', description='Tubulointerstitial nephritis', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0002149', description='Hyperuricemia', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0001596', description='Alopecia', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000873', description='Diabetes insipidus', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0200120', description='Chronic active hepatitis', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0001155', description='Abnormality of the hand', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0002910', description='Elevated hepatic transaminases', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000842', description='Hyperinsulinemia', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0002650', description='Scoliosis', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000824', description='Growth hormone deficiency', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000639', description='Nystagmus', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0003233', description='Hypoalphalipoproteinemia', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0002099', description='Asthma', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000083', description='Renal insufficiency', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0001394', description='Cirrhosis', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000548', description='Cone-rod dystrophy', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000121', description='Nephrocalcinosis', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0100820', description='Glomerulopathy', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000822', description='Hypertension', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0007360', description='Aplasia/Hypoplasia of the cerebellum', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0005616', description='Accelerated skeletal maturation', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0002205', description='Recurrent respiratory infections', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000580', description='Pigmentary retinopathy', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000717', description='Autism', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000144', description='Decreased fertility', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000164', description='Abnormality of the teeth', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0001397', description='Hepatic steatosis', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000408', description='Progressive sensorineural hearing impairment', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000956', description='Acanthosis nigricans', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000091', description='Abnormality of the renal tubule', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000123', description='Nephritis', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0001635', description='Congestive heart failure', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0002808', description='Kyphosis', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000518', description='Cataract', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0100543', description='Cognitive impairment', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000618', description='Blindness', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0004438', description='Hyperostosis frontalis interna', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0001644', description='Dilated cardiomyopathy', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000407', description='Sensorineural hearing impairment', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000795', description='Abnormality of the urethra', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000821', description='Hypothyroidism', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0004322', description='Short stature', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0009124', description='Abnormality of adipose tissue', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000771', description='Gynecomastia', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0001409', description='Portal hypertension', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0002240', description='Hepatomegaly', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000076', description='Vesicoureteral reflux', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0100817', description='Renovascular hypertension', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0006532', description='Recurrent pneumonia', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0003119', description='Abnormality of lipid metabolism', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0001250', description='Seizures', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000998', description='Hypertrichosis', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000523', description='Subcapsular cataract', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000815', description='Hypergonadotropic hypogonadism', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000490', description='Deeply set eye', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0001763', description='Pes planus', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000831', description='Insulin-resistant diabetes mellitus', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000855', description='Insulin resistance', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0005978', description='Type II diabetes mellitus', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000055', description='Abnormality of female external genitalia', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000035', description='Abnormality of the testis', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0002092', description='Pulmonary hypertension', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000230', description='Gingivitis', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000311', description='Round face', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000007', description='Autosomal recessive inheritance', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000722', description='Obsessive-compulsive disorder', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0002621', description='Atherosclerosis', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0002093', description='Respiratory insufficiency', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0100626', description='Chronic hepatic failure', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0002155', description='Hypertriglyceridemia', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000826', description='Precocious puberty', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0005987', description='Multinodular goiter', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0002206', description='Pulmonary fibrosis', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0001263', description='Global developmental delay', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0001744', description='Splenomegaly', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}, HPOTerm{id='HP:0000147', description='Polycystic ovaries', diseaseDb='OMIM', diseaseDbEntry=203800, geneName='ALMS1', geneEntrezID=7840}]', Hyperlink_OMIM='<a href="http://www.omim.org/entry/203800">203800</a>', Hyperlinks_HPO='<a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001639">HP:0001639</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000532">HP:0000532</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000505">HP:0000505</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000388">HP:0000388</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000613">HP:0000613</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001956">HP:0001956</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000858">HP:0000858</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001970">HP:0001970</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0002149">HP:0002149</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001596">HP:0001596</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000873">HP:0000873</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0200120">HP:0200120</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001155">HP:0001155</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0002910">HP:0002910</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000842">HP:0000842</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0002650">HP:0002650</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000824">HP:0000824</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000639">HP:0000639</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0003233">HP:0003233</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0002099">HP:0002099</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000083">HP:0000083</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001394">HP:0001394</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000548">HP:0000548</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000121">HP:0000121</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0100820">HP:0100820</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000822">HP:0000822</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0007360">HP:0007360</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0005616">HP:0005616</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0002205">HP:0002205</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000580">HP:0000580</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000717">HP:0000717</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000144">HP:0000144</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000164">HP:0000164</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001397">HP:0001397</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000408">HP:0000408</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000956">HP:0000956</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000091">HP:0000091</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000123">HP:0000123</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001635">HP:0001635</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0002808">HP:0002808</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000518">HP:0000518</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0100543">HP:0100543</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000618">HP:0000618</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0004438">HP:0004438</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001644">HP:0001644</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000407">HP:0000407</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000795">HP:0000795</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000821">HP:0000821</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0004322">HP:0004322</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0009124">HP:0009124</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000771">HP:0000771</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001409">HP:0001409</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0002240">HP:0002240</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000076">HP:0000076</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0100817">HP:0100817</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0006532">HP:0006532</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0003119">HP:0003119</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001250">HP:0001250</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000998">HP:0000998</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000523">HP:0000523</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000815">HP:0000815</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000490">HP:0000490</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001763">HP:0001763</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000831">HP:0000831</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000855">HP:0000855</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0005978">HP:0005978</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000055">HP:0000055</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000035">HP:0000035</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0002092">HP:0002092</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000230">HP:0000230</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000311">HP:0000311</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000007">HP:0000007</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000722">HP:0000722</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0002621">HP:0002621</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0002093">HP:0002093</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0100626">HP:0100626</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0002155">HP:0002155</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000826">HP:0000826</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0005987">HP:0005987</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0002206">HP:0002206</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001263">HP:0001263</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001744">HP:0001744</a> / <a href="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000147">HP:0000147</a>', Disease_OMIM='Alstrom syndrome', Symptoms_HPO='Hypertrophic cardiomyopathy / Chorioretinal abnormality / Visual impairment / Otitis media / Photophobia / Truncal obesity / Menstrual irregularities / Tubulointerstitial nephritis / Hyperuricemia / Alopecia / Diabetes insipidus / Chronic active hepatitis / Abnormality of the hand / Elevated hepatic transaminases / Hyperinsulinemia / Scoliosis / Growth hormone deficiency / Nystagmus / Hypoalphalipoproteinemia / Asthma / Renal insufficiency / Cirrhosis / Cone-rod dystrophy / Nephrocalcinosis / Glomerulopathy / Hypertension / Aplasia/Hypoplasia of the cerebellum / Accelerated skeletal maturation / Recurrent respiratory infections / Pigmentary retinopathy / Autism / Decreased fertility / Abnormality of the teeth / Hepatic steatosis / Progressive sensorineural hearing impairment / Acanthosis nigricans / Abnormality of the renal tubule / Nephritis / Congestive heart failure / Kyphosis / Cataract / Cognitive impairment / Blindness / Hyperostosis frontalis interna / Dilated cardiomyopathy / Sensorineural hearing impairment / Abnormality of the urethra / Hypothyroidism / Short stature / Abnormality of adipose tissue / Gynecomastia / Portal hypertension / Hepatomegaly / Vesicoureteral reflux / Renovascular hypertension / Recurrent pneumonia / Abnormality of lipid metabolism / Seizures / Hypertrichosis / Subcapsular cataract / Hypergonadotropic hypogonadism / Deeply set eye / Pes planus / Insulin-resistant diabetes mellitus / Insulin resistance / Type II diabetes mellitus / Abnormality of female external genitalia / Abnormality of the testis / Pulmonary hypertension / Gingivitis / Round face / Autosomal recessive inheritance / Obsessive-compulsive disorder / Atherosclerosis / Respiratory insufficiency / Chronic hepatic failure / Hypertriglyceridemia / Precocious puberty / Multinodular goiter / Pulmonary fibrosis / Global developmental delay / Splenomegaly / Polycystic ovaries', Full_info_OMIM='[OMIMTerm{entry=203800, name='Alstrom syndrome', type=3, causedBy=606844, cytoLoc='2p13.1', hgncIds=[ALMS1, ALSS, KIAA0328]}]', pos='73679116', chrom='2'}
		resultMap.put(OmimHpoAnnotator.OMIM_ALL, "");
		resultMap.put(OmimHpoAnnotator.OMIM_LINK, "http://www.omim.org/entry/203800");
		resultMap.put(OmimHpoAnnotator.OMIM_DISO, "");
		resultMap.put(OmimHpoAnnotator.HPO_ALL, "");
		resultMap.put(OmimHpoAnnotator.HPO_LINK, "http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001639");
		resultMap.put(OmimHpoAnnotator.HPO_DESC, "");

		Entity expectedEntity = new MapEntity(resultMap);

		expectedList.add(expectedEntity);

		Iterator<Entity> results = annotator.annotate(input.iterator());

		Entity resultEntity = results.next();

		assertEquals(resultEntity.get(OmimHpoAnnotator.OMIM_ALL), expectedEntity.get(OmimHpoAnnotator.OMIM_ALL));
		assertEquals(resultEntity.get(OmimHpoAnnotator.OMIM_LINK),
				expectedEntity.get(OmimHpoAnnotator.OMIM_LINK));
		assertEquals(resultEntity.get(OmimHpoAnnotator.OMIM_DISO),
				expectedEntity.get(OmimHpoAnnotator.OMIM_DISO));
		assertEquals(resultEntity.get(OmimHpoAnnotator.HPO_ALL),
				expectedEntity.get(OmimHpoAnnotator.HPO_ALL));
		assertEquals(resultEntity.get(OmimHpoAnnotator.HPO_LINK),
				expectedEntity.get(OmimHpoAnnotator.HPO_LINK));
		assertEquals(resultEntity.get(OmimHpoAnnotator.HPO_DESC),
				expectedEntity.get(OmimHpoAnnotator.HPO_DESC));
		
	}
	
	@Test
	public void canAnnotateTrueTest()
	{
		assertEquals(annotator.canAnnotate(metaDataCanAnnotate), true);
	}

	@Test
	public void canAnnotateFalseTest()
	{
		assertEquals(annotator.canAnnotate(metaDataCantAnnotate), false);
	}
	
//	 public static void main(String [ ] args) throws Exception
//	    {
//	        //includes a gene without HGNC symbol, and a gene not related to OMIM/HPO terms
//	        List<Locus> loci = new ArrayList<Locus>(Arrays.asList(new Locus("2", 58453844l), new Locus("2", 71892329l), new Locus("2", 73679116l), new Locus("10", 112360316l), new Locus("11", 2017661l), new Locus("1", 18151726l), new Locus("1", -1l), new Locus("11", 6637740l)));
//
//	        List<Entity> inputs = new ArrayList<Entity>();
//	        for(Locus l : loci)
//	        {
//	            HashMap<String, Object> inputMap = new HashMap<String, Object>();
//	            inputMap.put(CHROMOSOME, l.getChrom());
//	            inputMap.put(POSITION, l.getPos());
//	            inputs.add(new MapEntity(inputMap));
//	        }
//
//	        Iterator<Entity> res = new OmimHpoAnnotator().annotate(inputs.iterator());
//	        while(res.hasNext())
//	        {
//	            System.out.println(res.next().toString());
//	        }
//
//	    }

	// sample that doesnt return omim disease: 2	179621019	179621019	.	.	C	51
	// POLG gene no disease?
	
}
