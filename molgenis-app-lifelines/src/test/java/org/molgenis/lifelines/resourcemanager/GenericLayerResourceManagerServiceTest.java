package org.molgenis.lifelines.resourcemanager;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import javax.xml.validation.Schema;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.molgenis.lifelines.catalogue.CatalogInfo;
import org.molgenis.lifelines.utils.SchemaLoader;
import org.molgenis.omx.study.StudyDefinitionInfo;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GenericLayerResourceManagerServiceTest
{
	private HttpClient httpClient;
	private Schema eMeasureSchema;
	private String resourceManagerUrl;

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void GenericLayerResourceManagerService()
	{
		new GenericLayerResourceManagerService(null, null, null);
	}

	@Test
	public void findCatalogs() throws IllegalStateException, IOException
	{
		InputStream catalogReleaseStream = new ByteArrayInputStream(CATALOG_RELEASE_RESPONSE.getBytes(Charset
				.forName("UTF-8")));
		HttpEntity catalogReleaseEntity = when(mock(HttpEntity.class).getContent()).thenReturn(catalogReleaseStream)
				.getMock();
		HttpResponse catalogReleaseResponse = when(mock(HttpResponse.class).getEntity()).thenReturn(
				catalogReleaseEntity).getMock();

		when(httpClient.execute(argThat(new BaseMatcher<HttpGet>()
		{
			@Override
			public boolean matches(Object item)
			{
				return ((HttpGet) item).getURI().toString().equals(resourceManagerUrl + "/catalogrelease");
			}

			@Override
			public void describeTo(Description description)
			{
				throw new UnsupportedOperationException();
			}
		}))).thenReturn(catalogReleaseResponse);

		GenericLayerResourceManagerService resourceManagerService = new GenericLayerResourceManagerService(httpClient,
				resourceManagerUrl, eMeasureSchema);
		List<CatalogInfo> catalogs = resourceManagerService.findCatalogs();
		assertEquals(catalogs.size(), 1);
		assertEquals(catalogs.get(0).getId(), "1");
		assertEquals(catalogs.get(0).getName(), "[Catalog Release 1]");
	}

	@Test
	public void findStudyDefinition() throws IllegalStateException, IOException
	{
		InputStream studyDefinitionStream = new ByteArrayInputStream(STUDYDEFINITION_RESPONSE.getBytes(Charset
				.forName("UTF-8")));
		HttpEntity studyDefinitionEntity = when(mock(HttpEntity.class).getContent()).thenReturn(studyDefinitionStream)
				.getMock();
		HttpResponse studyDefinitionResponse = when(mock(HttpResponse.class).getEntity()).thenReturn(
				studyDefinitionEntity).getMock();
		when(httpClient.execute(argThat(new BaseMatcher<HttpGet>()
		{
			@Override
			public boolean matches(Object item)
			{
				return ((HttpGet) item).getURI().toString().equals(resourceManagerUrl + "/studydefinition/1");
			}

			@Override
			public void describeTo(Description description)
			{
				throw new UnsupportedOperationException();
			}
		}))).thenReturn(studyDefinitionResponse);

		GenericLayerResourceManagerService resourceManagerService = new GenericLayerResourceManagerService(httpClient,
				resourceManagerUrl, eMeasureSchema);
		assertNotNull(resourceManagerService.findStudyDefinition("1"));
	}

	@Test
	public void findStudyDefinitions() throws ClientProtocolException, IOException
	{
		InputStream studyDefinitionsStream = new ByteArrayInputStream(STUDYDEFINITIONS_RESPONSE.getBytes(Charset
				.forName("UTF-8")));
		HttpEntity studyDefinitionsEntity = when(mock(HttpEntity.class).getContent())
				.thenReturn(studyDefinitionsStream).getMock();
		HttpResponse studyDefinitionsResponse = when(mock(HttpResponse.class).getEntity()).thenReturn(
				studyDefinitionsEntity).getMock();
		when(httpClient.execute(argThat(new BaseMatcher<HttpGet>()
		{
			@Override
			public boolean matches(Object item)
			{
				return ((HttpGet) item).getURI().toString().equals(resourceManagerUrl + "/studydefinition");
			}

			@Override
			public void describeTo(Description description)
			{
				throw new UnsupportedOperationException();
			}
		}))).thenReturn(studyDefinitionsResponse);

		GenericLayerResourceManagerService resourceManagerService = new GenericLayerResourceManagerService(httpClient,
				resourceManagerUrl, eMeasureSchema);
		List<StudyDefinitionInfo> studyDefinitions = resourceManagerService.findStudyDefinitions();
		assertEquals(studyDefinitions.size(), 2);
		assertEquals(studyDefinitions.get(0).getId(), "1");
		assertEquals(studyDefinitions.get(0).getName(), "[Study 1]");
		assertEquals(studyDefinitions.get(1).getId(), "2");
		assertEquals(studyDefinitions.get(1).getName(), "[Study Definition 2]");
	}

	private static String CATALOG_RELEASE_RESPONSE;
	private static String STUDYDEFINITIONS_RESPONSE;
	private static String STUDYDEFINITION_RESPONSE;

	@BeforeClass
	private static void setUpBeforeClass()
	{
		CATALOG_RELEASE_RESPONSE = "<feed xmlns=\"http://www.w3.org/2005/Atom\"><entry><link href=\"http://w3dmzas15.dmz.local/tccweb/Umcg.Tcc.GenericLayer.ResourceManagerService-1/ResourceManagerService.svc/catalogrelease/1\"/><id>1</id><content><QualityMeasureDocument xsi:schemaLocation=\"urn:hl7-org:v3 emeasureschemas/EMeasure.xsd\" xmlns=\"urn:hl7-org:v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><typeId root=\"2.16.840.1.113883.1.3\" extension=\"POQM_HD000001\"/><id extension=\"1\" root=\"2.16.840.1.113883.2.4.3.8.1000.54.6\"/><code code=\"57024-2\" codeSystem=\"2.16.840.1.113883.6.1\" displayName=\"Health Quality Measure document\"/><title>Catalog Release 1</title><text>This is an example Catalog Release for the Generic Layer demo on 3-june-2013.</text><statusCode code=\"active\"/><setId root=\"1.1.1\" extension=\"example\"/><versionNumber value=\"1\"/><author typeCode=\"AUT\"><assignedPerson classCode=\"ASSIGNED\"><assignedPerson classCode=\"PSN\" determinerCode=\"INSTANCE\"><name>DataManager X</name></assignedPerson><representedOrganization classCode=\"ORG\" determinerCode=\"INSTANCE\"><id root=\"2.16.840.1.113883.19.5\"/><name>UMCG</name><contactParty classCode=\"CON\" nullFlavor=\"UNK\"/></representedOrganization></assignedPerson></author><custodian typeCode=\"CST\"><assignedPerson classCode=\"ASSIGNED\"/></custodian><component><section><code code=\"57025-9\" codeSystem=\"2.16.840.1.113883.6.1\" displayName=\"Data Criteria section\"/><title>Data criteria</title><text><list><item>BodyWeight</item><item>BodyLength</item><item>BloodPressure</item><item>HeartRate</item><item>LRA</item></list></text><entry typeCode=\"DRIV\"><observation classCode=\"OBS\" moodCode=\"CRT\"><code code=\"363808001\" codeSystem=\"2.16.840.1.113883.6.96\"/></observation></entry><entry typeCode=\"DRIV\"><observation classCode=\"OBS\" moodCode=\"CRT\"><code code=\"8302-2\" codeSystem=\"2.16.840.1.113883.6.1\"/></observation></entry><entry typeCode=\"DRIV\"><observation classCode=\"OBS\" moodCode=\"CRT\"><code code=\"75367002\" codeSystem=\"2.16.840.1.113883.6.96\"/></observation></entry><entry typeCode=\"DRIV\"><observation classCode=\"OBS\" moodCode=\"CRT\"><code code=\"364075005\" codeSystem=\"2.16.840.1.113883.6.96\"/></observation></entry><entry typeCode=\"DRIV\"><observation classCode=\"OBS\" moodCode=\"CRT\"><code code=\"LRA\" codeSystem=\"2.16.840.1.113883.2.4.3.8.1000.54.2\"/></observation></entry></section></component></QualityMeasureDocument></content></entry></feed>";
		STUDYDEFINITIONS_RESPONSE = "<feed xmlns=\"http://www.w3.org/2005/Atom\"><entry><link href=\"http://w3dmzas15.dmz.local/tccweb/Umcg.Tcc.GenericLayer.ResourceManagerService-1/ResourceManagerService.svc/studydefinition/1\"/><id>1</id><content><QualityMeasureDocument xsi:schemaLocation=\"urn:hl7-org:v3 emeasureschemas/EMeasure.xsd\" xmlns=\"urn:hl7-org:v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><typeId root=\"2.16.840.1.113883.1.3\" extension=\"POQM_HD000001\"/><id extension=\"1\" root=\"2.16.840.1.113883.2.4.3.8.1000.54.7\"/><code code=\"57024-2\" codeSystem=\"2.16.840.1.113883.6.1\" displayName=\"Health Quality Measure document\"/><title>Study 1</title><text>This is an example Study Definition for the Generic Layer demo on 3-june-2013.</text><statusCode code=\"active\"/><setId root=\"1.1.1\" extension=\"example\"/><versionNumber value=\"1\"/><author typeCode=\"AUT\"><assignedPerson classCode=\"ASSIGNED\"><assignedPerson classCode=\"PSN\" determinerCode=\"INSTANCE\"><name>Onderzoeker X</name></assignedPerson><representedOrganization classCode=\"ORG\" determinerCode=\"INSTANCE\"><id root=\"2.16.840.1.113883.19.5\"/><name>UMCG</name><contactParty classCode=\"CON\" nullFlavor=\"UNK\"/></representedOrganization></assignedPerson></author><custodian typeCode=\"CST\"><assignedPerson classCode=\"ASSIGNED\"/></custodian><component><section><code code=\"57025-9\" codeSystem=\"2.16.840.1.113883.6.1\" displayName=\"Data Criteria section\"/><title>Data criteria</title><text><list><item>BodyWeight</item></list></text><entry typeCode=\"DRIV\"><observation classCode=\"OBS\" moodCode=\"CRT\"><code code=\"363808001\" codeSystem=\"2.16.840.1.113883.6.96\"/></observation></entry></section></component></QualityMeasureDocument></content></entry><entry><link href=\"http://w3dmzas15.dmz.local/tccweb/Umcg.Tcc.GenericLayer.ResourceManagerService-1/ResourceManagerService.svc/studydefinition/2\"/><id>2</id><content><QualityMeasureDocument xsi:schemaLocation=\"urn:hl7-org:v3 emeasureschemas/EMeasure.xsd\" xmlns=\"urn:hl7-org:v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><typeId root=\"2.16.840.1.113883.1.3\" extension=\"POQM_HD000001\"/><id extension=\"2\" root=\"2.16.840.1.113883.2.4.3.8.1000.54.7\"/><code code=\"57024-2\" codeSystem=\"2.16.840.1.113883.6.1\" displayName=\"Health Quality Measure document\"/><title>Study Definition 2</title><text>This is an example Study Definition for the Generic Layer demo on 3-june-2013.</text><statusCode code=\"active\"/><setId root=\"1.1.1\" extension=\"example\"/><versionNumber value=\"1\"/><author typeCode=\"AUT\"><assignedPerson classCode=\"ASSIGNED\"><assignedPerson classCode=\"PSN\" determinerCode=\"INSTANCE\"><name>Onderzoeker X</name></assignedPerson><representedOrganization classCode=\"ORG\" determinerCode=\"INSTANCE\"><id root=\"2.16.840.1.113883.19.5\"/><name>UMCG</name><contactParty classCode=\"CON\" nullFlavor=\"UNK\"/></representedOrganization></assignedPerson></author><custodian typeCode=\"CST\"><assignedPerson classCode=\"ASSIGNED\"/></custodian><component><section><code code=\"57025-9\" codeSystem=\"2.16.840.1.113883.6.1\" displayName=\"Data Criteria section\"/><title>Data criteria</title><text><list><item>BodyWeight</item><item>BloodPressure</item></list></text><entry typeCode=\"DRIV\"><observation classCode=\"OBS\" moodCode=\"CRT\"><code code=\"363808001\" codeSystem=\"2.16.840.1.113883.6.96\"/></observation></entry><entry typeCode=\"DRIV\"><observation classCode=\"OBS\" moodCode=\"CRT\"><code code=\"75367002\" codeSystem=\"2.16.840.1.113883.6.96\"/></observation></entry></section></component></QualityMeasureDocument></content></entry></feed>";
		STUDYDEFINITION_RESPONSE = "<QualityMeasureDocument xsi:schemaLocation=\"urn:hl7-org:v3 emeasureschemas/EMeasure.xsd\" xmlns=\"urn:hl7-org:v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><typeId root=\"2.16.840.1.113883.1.3\" extension=\"POQM_HD000001\"/><id root=\"2.16.840.1.113883.2.4.3.8.1000.54.7\"/><code code=\"57024-2\" codeSystem=\"2.16.840.1.113883.6.1\" displayName=\"Health Quality Measure document\"/><title>title van de aanvraag</title><text>This is an example Study Definition for the Generic Layer demo on 3-june-2013.</text><statusCode code=\"active\"/><setId root=\"1.1.1\" extension=\"example\"/><versionNumber value=\"1\"/><author typeCode=\"AUT\"><assignedPerson classCode=\"ASSIGNED\"><assignedPerson classCode=\"PSN\" determinerCode=\"INSTANCE\"><name>Onderzoeker X</name></assignedPerson><representedOrganization classCode=\"ORG\" determinerCode=\"INSTANCE\"><id root=\"2.16.840.1.113883.19.5\"/><name>UMCG</name><contactParty classCode=\"CON\" nullFlavor=\"UNK\"/></representedOrganization></assignedPerson></author><custodian typeCode=\"CST\"><assignedPerson classCode=\"ASSIGNED\"/></custodian><component><section><code code=\"57025-9\" codeSystem=\"2.16.840.1.113883.6.1\" displayName=\"Data Criteria section\"/><title>Data criteria</title><text><list><item>Birth length</item><item>Exact birth length</item></list></text><entry typeCode=\"DRIV\"><observation classCode=\"OBS\" moodCode=\"CRT\"><code displayName=\"Birth length\" code=\"UVDEVEL.BIRTH12.6.2\" codeSystem=\"2.16.840.1.113883.2.4.3.8.1000.54.4\"><originalText>Beschrijving van het item....???</originalText></code></observation></entry><entry typeCode=\"DRIV\"><observation classCode=\"OBS\" moodCode=\"CRT\"><code displayName=\"Exact birth length\" code=\"UVDEVEL.BIRTH12A.6.2\" codeSystem=\"2.16.840.1.113883.2.4.3.8.1000.54.4\"><originalText>Beschrijving van het item....???</originalText></code></observation></entry></section></component></QualityMeasureDocument>";
	}

	@BeforeMethod
	public void setUp() throws ClientProtocolException, IOException
	{
		this.resourceManagerUrl = "http://www.dummy.org";
		this.eMeasureSchema = new SchemaLoader("EMeasure.xsd").getSchema();
		this.httpClient = mock(HttpClient.class);
	}
}
