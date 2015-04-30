package org.molgenis.data.annotation.impl;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.AbstractAnnotatorTest;
import org.molgenis.data.annotation.AnnotatorTestData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CosmicServiceAnnotatorTest extends AnnotatorTestData
{
	public DefaultEntityMetaData cosmicMetaDataCanAnnotate = new DefaultEntityMetaData("test");
	public DefaultEntityMetaData cosmicMetaDataCantAnnotate = new DefaultEntityMetaData("test");

	public AttributeMetaData attributeMetaDataCanAnnotate;
	public AttributeMetaData attributeMetaDataCantAnnotate;

	private HttpClient httpClient;
	private static String SERVICE_RESPONSE;

	@BeforeMethod
	public void beforeMethod()
	{
		this.httpClient = mock(HttpClient.class);
		annotator = new CosmicServiceAnnotator(this.httpClient);

		attributeMetaDataCanAnnotate = new DefaultAttributeMetaData(CosmicServiceAnnotator.ENSEMBLE_ID,
				FieldTypeEnum.STRING);
		cosmicMetaDataCanAnnotate.addAttributeMetaData(attributeMetaDataCanAnnotate);
		cosmicMetaDataCanAnnotate.setIdAttribute(CosmicServiceAnnotator.ENSEMBLE_ID);

		attributeMetaDataCantAnnotate = new DefaultAttributeMetaData(CosmicServiceAnnotator.ENSEMBLE_ID,
				FieldTypeEnum.BOOL);
		cosmicMetaDataCantAnnotate.addAttributeMetaData(attributeMetaDataCantAnnotate);
		cosmicMetaDataCantAnnotate.setIdAttribute(CosmicServiceAnnotator.ENSEMBLE_ID);

		entity.set("ensemblId", "ENSG00000186092");
		input.add(entity);

		SERVICE_RESPONSE = "[{\"ID\":\"COSM911918\",\"feature_type\":\"somatic_variation\",\"alt_alleles\":[\"C\",\"A\"],\"end\":69345,\"seq_region_name\":\"1\",\"consequence_type\":\"synonymous_variant\",\"strand\":1,\"start\":69345},{\"ID\":\"COSM426644\",\"feature_type\":\"somatic_variation\",\"alt_alleles\":[\"G\",\"T\"],\"end\":69523,\"seq_region_name\":\"1\",\"consequence_type\":\"missense_variant\",\"strand\":1,\"start\":69523},{\"ID\":\"COSM75742\",\"feature_type\":\"somatic_variation\",\"alt_alleles\":[\"G\",\"A\"],\"end\":69538,\"seq_region_name\":\"1\",\"consequence_type\":\"missense_variant\",\"strand\":1,\"start\":69538}]";
	}

	@Test
	public void annotateTest() throws IllegalStateException, IOException
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap1 = new LinkedHashMap<String, Object>();
		resultMap1.put(CosmicServiceAnnotator.ID, "COSM911918");
		resultMap1.put(CosmicServiceAnnotator.FEATURE_TYPE, "somatic_variation");
		resultMap1.put(CosmicServiceAnnotator.ALT_ALLELES, "C,A");
		resultMap1.put(CosmicServiceAnnotator.END, 69345);
		resultMap1.put(CosmicServiceAnnotator.SEQ_REGION_NAME, "1");
		resultMap1.put(CosmicServiceAnnotator.CONSEQUENCE_TYPE, "synonymous_variant");
		resultMap1.put(CosmicServiceAnnotator.STRAND, 1);
		resultMap1.put(CosmicServiceAnnotator.START, 69345);
		resultMap1.put(CosmicServiceAnnotator.ENSEMBLE_ID, "ENSG00000186092");

		Map<String, Object> resultMap2 = new LinkedHashMap<String, Object>();
		resultMap2.put(CosmicServiceAnnotator.ID, "COSM426644");
		resultMap2.put(CosmicServiceAnnotator.FEATURE_TYPE, "somatic_variation");
		resultMap2.put(CosmicServiceAnnotator.ALT_ALLELES, "G,T");
		resultMap2.put(CosmicServiceAnnotator.END, 69523);
		resultMap2.put(CosmicServiceAnnotator.SEQ_REGION_NAME, "1");
		resultMap2.put(CosmicServiceAnnotator.CONSEQUENCE_TYPE, "missense_variant");
		resultMap2.put(CosmicServiceAnnotator.STRAND, 1);
		resultMap2.put(CosmicServiceAnnotator.START, 69523);
		resultMap2.put(CosmicServiceAnnotator.ENSEMBLE_ID, "ENSG00000186092");

		Map<String, Object> resultMap3 = new LinkedHashMap<String, Object>();
		resultMap3.put(CosmicServiceAnnotator.ID, "COSM75742");
		resultMap3.put(CosmicServiceAnnotator.FEATURE_TYPE, "somatic_variation");
		resultMap3.put(CosmicServiceAnnotator.ALT_ALLELES, "G,A");
		resultMap3.put(CosmicServiceAnnotator.END, 69538);
		resultMap3.put(CosmicServiceAnnotator.SEQ_REGION_NAME, "1");
		resultMap3.put(CosmicServiceAnnotator.CONSEQUENCE_TYPE, "missense_variant");
		resultMap3.put(CosmicServiceAnnotator.STRAND, 1);
		resultMap3.put(CosmicServiceAnnotator.START, 69538);
		resultMap3.put(CosmicServiceAnnotator.ENSEMBLE_ID, "ENSG00000186092");

		Entity expectedEntity1 = new MapEntity(resultMap1);
		Entity expectedEntity2 = new MapEntity(resultMap2);
		Entity expectedEntity3 = new MapEntity(resultMap3);

		expectedList.add(expectedEntity1);
		expectedList.add(expectedEntity2);
		expectedList.add(expectedEntity3);

		InputStream ServiceStream = new ByteArrayInputStream(SERVICE_RESPONSE.getBytes(Charset.forName("UTF-8")));
		HttpEntity catalogReleaseEntity = when(mock(HttpEntity.class).getContent()).thenReturn(ServiceStream).getMock();
		HttpResponse catalogReleaseResponse = when(mock(HttpResponse.class).getEntity()).thenReturn(
				catalogReleaseEntity).getMock();
		StatusLine statusLine = when(mock(StatusLine.class).getStatusCode()).thenReturn(200).getMock();
		when(catalogReleaseResponse.getStatusLine()).thenReturn(statusLine);

		when(httpClient.execute(argThat(new BaseMatcher<HttpGet>()
		{
			@Override
			public boolean matches(Object item)
			{
				return ((HttpGet) item)
						.getURI()
						.toString()
						.equals("http://beta.rest.ensembl.org/feature/id/ENSG00000186092.json?feature=somatic_variation");
			}

			@Override
			public void describeTo(Description description)
			{
				throw new UnsupportedOperationException();
			}
		}))).thenReturn(catalogReleaseResponse);

		Iterator<Entity> results = annotator.annotate(input);

		Entity resultEntity = results.next();
		assertEquals(resultEntity.get(CosmicServiceAnnotator.ID), expectedEntity1.get(CosmicServiceAnnotator.ID));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.NAME), expectedEntity1.get(CosmicServiceAnnotator.NAME));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.SEQ_REGION_NAME),
				expectedEntity1.get(CosmicServiceAnnotator.SEQ_REGION_NAME));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.ALT_ALLELES),
				expectedEntity1.get(CosmicServiceAnnotator.ALT_ALLELES));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.CONSEQUENCE_TYPE),
				expectedEntity1.get(CosmicServiceAnnotator.CONSEQUENCE_TYPE));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.END), expectedEntity1.get(CosmicServiceAnnotator.END));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.FEATURE_TYPE),
				expectedEntity1.get(CosmicServiceAnnotator.FEATURE_TYPE));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.START), expectedEntity1.get(CosmicServiceAnnotator.START));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.STRAND),
				expectedEntity1.get(CosmicServiceAnnotator.STRAND));
		resultEntity = results.next();
		assertEquals(resultEntity.get(CosmicServiceAnnotator.ID), expectedEntity2.get(CosmicServiceAnnotator.ID));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.NAME), expectedEntity2.get(CosmicServiceAnnotator.NAME));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.SEQ_REGION_NAME),
				expectedEntity2.get(CosmicServiceAnnotator.SEQ_REGION_NAME));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.ALT_ALLELES),
				expectedEntity2.get(CosmicServiceAnnotator.ALT_ALLELES));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.CONSEQUENCE_TYPE),
				expectedEntity2.get(CosmicServiceAnnotator.CONSEQUENCE_TYPE));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.END), expectedEntity2.get(CosmicServiceAnnotator.END));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.FEATURE_TYPE),
				expectedEntity2.get(CosmicServiceAnnotator.FEATURE_TYPE));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.START), expectedEntity2.get(CosmicServiceAnnotator.START));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.STRAND),
				expectedEntity2.get(CosmicServiceAnnotator.STRAND));
		resultEntity = results.next();
		assertEquals(resultEntity.get(CosmicServiceAnnotator.ID), expectedEntity3.get(CosmicServiceAnnotator.ID));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.NAME), expectedEntity3.get(CosmicServiceAnnotator.NAME));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.SEQ_REGION_NAME),
				expectedEntity3.get(CosmicServiceAnnotator.SEQ_REGION_NAME));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.ALT_ALLELES),
				expectedEntity3.get(CosmicServiceAnnotator.ALT_ALLELES));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.CONSEQUENCE_TYPE),
				expectedEntity3.get(CosmicServiceAnnotator.CONSEQUENCE_TYPE));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.END), expectedEntity3.get(CosmicServiceAnnotator.END));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.FEATURE_TYPE),
				expectedEntity3.get(CosmicServiceAnnotator.FEATURE_TYPE));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.START), expectedEntity3.get(CosmicServiceAnnotator.START));
		assertEquals(resultEntity.get(CosmicServiceAnnotator.STRAND),
				expectedEntity3.get(CosmicServiceAnnotator.STRAND));
	}

	@Test
	public void canAnnotateTrueTest()
	{
		assertEquals(annotator.canAnnotate(cosmicMetaDataCanAnnotate), "true");
	}

	@Test
	public void canAnnotateFalseTest()
	{
		assertEquals(annotator.canAnnotate(cosmicMetaDataCantAnnotate), "a required attribute has the wrong datatype");
	}
}
