package org.molgenis.app.promise;

import org.molgenis.app.promise.client.PromiseClient;
import org.molgenis.app.promise.client.PromiseClientImpl;
import org.molgenis.app.promise.client.PromiseDataUnmarshaller;
import org.molgenis.app.promise.client.PromiseRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;

@Configuration
public class PromiseConfig
{
	@Bean
	PromiseClient promiseClient()
	{
		return new PromiseClientImpl(webServiceTemplate());
	}

	@Bean
	public WebServiceTemplate webServiceTemplate()
	{
		WebServiceTemplate webServiceTemplate = new WebServiceTemplate(marshaller(), unmarshaller());
		AxiomSoapMessageFactory axiomSoapMessageFactory = new AxiomSoapMessageFactory();

		// Disables caching. If you want to debug the SOAP response, set this to true
		axiomSoapMessageFactory.setPayloadCaching(false);

		webServiceTemplate.setMessageFactory(axiomSoapMessageFactory);

		return webServiceTemplate;
	}

	@Bean
	public Marshaller marshaller()
	{
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(PromiseRequest.class);
		return marshaller;
	}

	@Bean
	public Unmarshaller unmarshaller()
	{
		return new PromiseDataUnmarshaller();
	}

}
