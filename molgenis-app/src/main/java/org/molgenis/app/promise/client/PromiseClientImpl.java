package org.molgenis.app.promise.client;

import org.molgenis.data.Entity;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceMessageExtractor;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.support.MarshallingUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static org.molgenis.app.promise.client.PromiseRequest.create;
import static org.molgenis.app.promise.model.PromiseCredentialsMetaData.URL;

@Component
public class PromiseClientImpl implements PromiseClient
{
	public static final String NAMESPACE_VALUE = "http://tempuri.org/";
	public static final String ACTION_GETDATA = "getData";

	private static final Logger LOG = LoggerFactory.getLogger(PromiseClientImpl.class);

	private final WebServiceTemplate webServiceTemplate;

	@Autowired
	public PromiseClientImpl(WebServiceTemplate webServiceTemplate)
	{
		this.webServiceTemplate = requireNonNull(webServiceTemplate);
	}

	/**
	 * Retrieves promise data for credentials and returns getData content of the response.
	 *
	 * @throws WebServiceClientException if something went wrong on the client side.
	 * @throws XmlMappingException       if something went wrong with the XML marshalling and unmarshalling
	 */
	@Override
	@RunAsSystem
	public void getData(Entity credentials, String seqNr, Consumer<XMLStreamReader> consumer)
	{
		requireNonNull(credentials, "Credentials is null");

		String url = credentials.get(URL).toString();
		PromiseRequest request = create(credentials, seqNr);

		WebServiceMessageCallback requestCallback = webServiceMessage -> {
			MarshallingUtils.marshal(webServiceTemplate.getMarshaller(), request, webServiceMessage);
			((SoapMessage) webServiceMessage).setSoapAction(NAMESPACE_VALUE + ACTION_GETDATA);
		};

		WebServiceMessageExtractor extractor = webServiceMessage -> {
			Source source = webServiceMessage.getPayloadSource();
			try
			{
				XMLStreamReader streamReader = XMLInputFactory.newInstance().createXMLStreamReader(source);
				consumer.accept(streamReader);
				streamReader.close();
			}
			catch (XMLStreamException e)
			{
				LOG.error("Error parsing data", e);
			}
			return null;
		};

		webServiceTemplate.sendAndReceive(url, requestCallback, extractor);
	}
}
