package org.molgenis.app.promise.client;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.Entity;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Component;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceOperations;
import org.springframework.ws.soap.SoapMessage;

@Component
public class PromiseClientImpl implements PromiseClient
{
	public static final String NAMESPACE_VALUE = "http://tempuri.org/";
	public static final String ACTION_GETDATAFORXML = "getDataForXML";

	private final WebServiceOperations webServiceTemplate;

	@Autowired
	public PromiseClientImpl(WebServiceOperations webServiceTemplate)
	{
		this.webServiceTemplate = requireNonNull(webServiceTemplate);
	}

	/**
	 * Retrieves promise data for credentials and returns getDataForXML content of the response.
	 * 
	 * @throws WebServiceClientException
	 *             if something went wrong on the client side.
	 * @throws XmlMappingException
	 *             if something went wrong with the XML marshalling and unmarshalling
	 */
	@Override
	@RunAsSystem
	public String getDataForXml(Entity credentials, String seqNr)
	{
		requireNonNull(credentials, "Credentials is null");

		String url = credentials.get("URL").toString();
		PromiseRequest request = PromiseRequest.create(credentials, seqNr);
		return (String) webServiceTemplate.marshalSendAndReceive(url, request, this::setAction);
	}

	private void setAction(WebServiceMessage message)
	{
		((SoapMessage) message).setSoapAction(NAMESPACE_VALUE + ACTION_GETDATAFORXML);
	}
}
