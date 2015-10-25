package org.molgenis.app.promise.client;

import static org.springframework.ws.test.client.RequestMatchers.connectionTo;
import static org.springframework.ws.test.client.RequestMatchers.payload;
import static org.springframework.ws.test.client.ResponseCreators.withPayload;
import static org.testng.Assert.assertEquals;

import java.net.URI;

import javax.xml.transform.Source;

import org.molgenis.app.promise.PromiseConfig;
import org.molgenis.app.promise.model.PromiseCredentialsMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.test.client.MockWebServiceServer;
import org.springframework.xml.transform.StringSource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = PromiseConfig.class)
public class PromiseClientImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private WebServiceTemplate webServiceTemplate;

	@Autowired
	private PromiseClientImpl promiseClient;

	MockWebServiceServer mockServer;

	@BeforeMethod
	public void beforeTest()
	{
		mockServer = MockWebServiceServer.createServer(webServiceTemplate);
	}

	@Test
	public void testGetDataForXml() throws Exception
	{
		Entity credentials = new DefaultEntity(PromiseCredentialsMetaData.INSTANCE, null);
		credentials.set(PromiseCredentialsMetaData.PROJ, "proj");
		credentials.set(PromiseCredentialsMetaData.PWS, "pws");
		credentials.set(PromiseCredentialsMetaData.SECURITYCODE, "securityCode");
		credentials.set(PromiseCredentialsMetaData.USERNAME, "userName");
		credentials.set(PromiseCredentialsMetaData.PASSW, "passw");
		credentials.set(PromiseCredentialsMetaData.URL, "http://promiseurl.org/blah");

		Source requestPayload = new StringSource(
				"<ns2:getDataForXML xmlns:ns2=\"http://tempuri.org\"><proj>proj</proj><PWS>pws</PWS>"
						+ "<SEQNR>10</SEQNR><securitycode>securityCode</securitycode>"
						+ "<username>userName</username><passw>passw</passw></ns2:getDataForXML>");
		Source responsePayload = new StringSource(
				"<tempuri:root xmlns:tempuri=\"http://tempuri.org/\"><tempuri:getDataForXMLResult>"
						+ "&lt;blah/&gt;&lt;blah2/&gt;</tempuri:getDataForXMLResult></tempuri:root>");

		mockServer.expect(payload(requestPayload)).andExpect(connectionTo(new URI("http://promiseurl.org/blah")))
				.andRespond(withPayload(responsePayload));

		assertEquals(promiseClient.getDataForXml(credentials, "10"), "<blah/><blah2/>");

		mockServer.verify();
	}
}
