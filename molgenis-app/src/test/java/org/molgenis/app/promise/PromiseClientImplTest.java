package org.molgenis.app.promise;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

@ContextConfiguration
public class PromiseClientImplTest extends AbstractTestNGSpringContextTests
{
	// @Configuration
	// static class Config
	// {
	// @Bean
	// public PromiseClient promiseClient()
	// {
	// return new PromiseClientImpl(soapConnectionFactory());
	// }
	//
	// @Bean
	// public SOAPConnectionFactory soapConnectionFactory()
	// {
	// try
	// {
	// return SOAPConnectionFactory.newInstance();
	// }
	// catch (UnsupportedOperationException | SOAPException e)
	// {
	// throw new RuntimeException(e);
	// }
	// }
	// }
	//
	// @Autowired
	// private PromiseClient promiseClient;
	//
	// @Test
	// public void helloWorld() throws IOException, XMLStreamException
	// {
	// boolean hasHelloWorldResult = false;
	// XMLStreamReader xmlStreamReader = promiseClient.helloWorld();
	// try
	// {
	// while (xmlStreamReader.hasNext())
	// {
	// if (xmlStreamReader.next() == XMLStreamReader.START_ELEMENT)
	// {
	// if (xmlStreamReader.getLocalName().equals("HelloWorldResult"))
	// {
	// hasHelloWorldResult = true;
	// break;
	// }
	// }
	// }
	// }
	// finally
	// {
	// xmlStreamReader.close();
	// }
	// assertTrue(hasHelloWorldResult, "XML response for helloWorld is missing HelloWorldResult element");
	// }
}
