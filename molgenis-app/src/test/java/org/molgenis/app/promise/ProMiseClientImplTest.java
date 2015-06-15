package org.molgenis.app.promise;

import static org.testng.Assert.assertTrue;

import java.io.IOException;

import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration
public class ProMiseClientImplTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public ProMiseClient promiseClient()
		{
			return new ProMiseClientImpl(soapConnectionFactory(),
					"https://clinicalresearch.nl/ProMISe/S/WS/ProMISeWS.asmx");
		}

		@Bean
		public SOAPConnectionFactory soapConnectionFactory()
		{
			try
			{
				return SOAPConnectionFactory.newInstance();
			}
			catch (UnsupportedOperationException | SOAPException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	@Autowired
	private ProMiseClient promiseClient;

	@Test
	public void helloWorld() throws IOException, XMLStreamException
	{
		boolean hasHelloWorldResult = false;
		XMLStreamReader xmlStreamReader = promiseClient.helloWorld();
		try
		{
			while (xmlStreamReader.hasNext())
			{
				if (xmlStreamReader.next() == XMLStreamReader.START_ELEMENT)
				{
					if (xmlStreamReader.getLocalName().equals("HelloWorldResult"))
					{
						hasHelloWorldResult = true;
						break;
					}
				}
			}
		}
		finally
		{
			xmlStreamReader.close();
		}
		assertTrue(hasHelloWorldResult, "XML response for helloWorld is missing HelloWorldResult element");
	}
}
