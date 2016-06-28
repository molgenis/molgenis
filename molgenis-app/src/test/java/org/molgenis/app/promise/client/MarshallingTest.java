package org.molgenis.app.promise.client;

import org.molgenis.app.promise.PromiseConfig;
import org.molgenis.app.promise.model.PromiseCredentialsMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.xml.transform.StringResult;
import org.testng.annotations.Test;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;

import static org.molgenis.app.promise.model.PromiseCredentialsMetaData.PWS;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = PromiseConfig.class)
public class MarshallingTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	Marshaller marshaller;

	@Autowired
	Unmarshaller unmarshaller;

	@Test
	public void testMarshal() throws XmlMappingException, IOException
	{
		Entity credentials = new DefaultEntity(PromiseCredentialsMetaData.INSTANCE, null);
		credentials.set(PromiseCredentialsMetaData.PROJ, "proj");
		credentials.set(PWS, "pws");
		credentials.set(PromiseCredentialsMetaData.SECURITYCODE, "securityCode");
		credentials.set(PromiseCredentialsMetaData.USERNAME, "userName");
		credentials.set(PromiseCredentialsMetaData.PASSW, "passw");
		PromiseRequest request = PromiseRequest.create(credentials, "10");

		StringResult result = new StringResult();
		marshaller.marshal(request, result);

		assertEquals(result.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<getData xmlns=\"http://tempuri.org/\"><proj>proj</proj><PWS>pws</PWS><SEQNR>10</SEQNR>"
				+ "<securitycode>securityCode</securitycode><username>userName</username><passw>passw</passw></getData>");
	}

	@Test
	public void testUnmarshal() throws TransformerException, XmlMappingException, IOException
	{
		String result = "<tempuri:getDataForXMLResult xmlns:tempuri=\"http://tempuri.org/\">"
				+ "&lt;blah/&gt;&lt;blah2/&gt;</tempuri:getDataForXMLResult>";
		String data = (String) unmarshaller.unmarshal(new StreamSource(new StringReader(result)));
		assertEquals(data, "<blah/><blah2/>");
	}
}
