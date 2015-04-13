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
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EbiServiceAnnotatorTest
{
	private DefaultEntityMetaData metaDataCanAnnotate;
	private EntityMetaData metaDataCantAnnotate;
	private EbiServiceAnnotator annotator;
	private AttributeMetaData attributeMetaDataCanAnnotate;
	private AttributeMetaData attributeMetaDataCantAnnotate;
	private AttributeMetaData attributeMetaDataCantAnnotate2;
	private Entity entity;
	private HttpClient httpClient;
	private static String SERVICE_RESPONSE;
	private ArrayList<Entity> input;

	@BeforeMethod
	public void beforeMethod()
	{
		this.httpClient = mock(HttpClient.class);
		annotator = new EbiServiceAnnotator(httpClient);

		metaDataCanAnnotate = new DefaultEntityMetaData("test");
		attributeMetaDataCanAnnotate = mock(AttributeMetaData.class);
		when(attributeMetaDataCanAnnotate.getName()).thenReturn("uniprot_id");
		when(attributeMetaDataCanAnnotate.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));

		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataCanAnnotate);
		metaDataCanAnnotate.setIdAttribute(attributeMetaDataCanAnnotate.getName());
		metaDataCantAnnotate = mock(EntityMetaData.class);
		attributeMetaDataCantAnnotate = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotate.getName()).thenReturn("otherID");
		when(attributeMetaDataCantAnnotate.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.STRING.toString().toLowerCase()));
		attributeMetaDataCantAnnotate2 = mock(AttributeMetaData.class);
		when(attributeMetaDataCantAnnotate2.getName()).thenReturn("uniprot_id");
		when(attributeMetaDataCantAnnotate2.getDataType()).thenReturn(
				MolgenisFieldTypes.getType(FieldTypeEnum.DATE.toString().toLowerCase()));

		when(metaDataCantAnnotate.getAttribute("uniprot_id")).thenReturn(attributeMetaDataCantAnnotate2);

		entity = new MapEntity(metaDataCanAnnotate);
		entity.set("uniprot_id","Q13936");
		input = new ArrayList<Entity>();
		input.add(entity);

        SERVICE_RESPONSE = "{\"target\": {\"targetType\": \"SINGLE PROTEIN\", \"chemblId\": \"CHEMBL1940\", \"geneNames\": \"Unspecified\", \"description\": \"Voltage-gated L-type calcium channel alpha-1C subunit\", \"compoundCount\": 171, \"bioactivityCount\": 239, \"proteinAccession\": \"Q13936\", \"synonyms\": \"CCHL1A1,CACNL1A1,Calcium channel, L type, alpha-1 polypeptide, isoform 1, cardiac muscle,Voltage-gated calcium channel subunit alpha Cav1.2,CACNA1C,CACN2,CACH2 ,Voltage-dependent L-type calcium channel subunit alpha-1C\", \"organism\": \"Homo sapiens\", \"preferredName\": \"Voltage-gated L-type calcium channel alpha-1C subunit\"}}";
	}

	@Test
	public void annotateTest() throws IllegalStateException, IOException
	{
		List<Entity> expectedList = new ArrayList<Entity>();
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		Entity expectedEntity = new MapEntity(resultMap);
		expectedEntity.set("targetType", "SINGLE PROTEIN");
		expectedEntity.set("chemblId", "CHEMBL1940");
		expectedEntity.set("geneNames", "Unspecified");
		expectedEntity.set("description", "Voltage-gated L-type calcium channel alpha-1C subunit");
		expectedEntity.set("compoundCount", 171.0);
		expectedEntity.set("bioactivityCount", 239.0);
		expectedEntity.set("proteinAccession", "Q13936");
		expectedEntity
				.set("synonyms",
						"CCHL1A1,CACNL1A1,Calcium channel, L type, alpha-1 polypeptide, isoform 1, cardiac muscle,Voltage-gated calcium channel subunit alpha Cav1.2,CACNA1C,CACN2,CACH2 ,Voltage-dependent L-type calcium channel subunit alpha-1C");
		expectedEntity.set("organism", "Homo sapiens");
		expectedEntity.set("preferredName", "Voltage-gated L-type calcium channel alpha-1C subunit");
		expectedEntity.set("uniprot_id", "Q13936");
		expectedList.add(expectedEntity);

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
				return ((HttpGet) item).getURI().toString()
						.equals("https://www.ebi.ac.uk/chemblws/targets/uniprot/Q13936.json");
			}

			@Override
			public void describeTo(Description description)
			{
				throw new UnsupportedOperationException();
			}
		}))).thenReturn(catalogReleaseResponse);

		Iterator<Entity> results = annotator.annotate(input);

		assertEquals(results.next().getString("chemblId"), expectedEntity.getString("chemblId"));
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