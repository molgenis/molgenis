package org.molgenis.app.promise;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProMiseClientImpl implements ProMiseClient
{
	private static final String NAMESPACE_KEY = "tem";
	private static final String NAMESPACE_VALUE = "http://tempuri.org/";
	private static final String ACTION_GETDATAFORXML = "getDataForXML";
	private static final String ACTION_HELLOWORLD = "HelloWorld";

	private final SOAPConnectionFactory soapConnectionFactory;
	private final URL endpoint;

	@Autowired
	public ProMiseClientImpl(SOAPConnectionFactory soapConnectionFactory,
			@Value("${promise.endpoint:@null}") String endpoint)
	{
		if (soapConnectionFactory == null)
		{
			throw new IllegalArgumentException("SOAPConnectionFactory is null");
		}
		if (endpoint == null)
		{
			throw new IllegalArgumentException(
					"endpoint is null, did you define 'promise.endpoint' in molgenis-server.properties?");
		}
		this.soapConnectionFactory = soapConnectionFactory;

		try
		{
			// enable time-out for SOAP calls
			this.endpoint = new URL(null, endpoint, new URLStreamHandler()
			{
				protected URLConnection openConnection(URL url) throws IOException
				{
					// The url is the parent of this stream handler, so must create clone
					URL clone = new URL(url.toString());
					URLConnection connection = clone.openConnection();

					connection.setConnectTimeout(15 * 1000); // 15 sec
					connection.setReadTimeout(15 * 1000); // 15 sec
					return connection;
				}
			});
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException(
					"'promise.endpoint' in molgenis-server.properties contains an invalid URL");
		}
	}

	@Override
	public XMLStreamReader helloWorld() throws IOException
	{
		try
		{
			return executeSOAPRequest(ACTION_HELLOWORLD, Collections.<String, String> emptyMap());
		}
		catch (SOAPException e)
		{
			throw new IOException(e);
		}
	}

	@Override
	public XMLStreamReader getDataForXml(String project, String pws, String seqNr, String securityCode,
			String username, String password) throws IOException
	{
		Map<String, String> args = new LinkedHashMap<String, String>();
		args.put("proj", project);
		args.put("PWS", pws);
		args.put("SEQNR", seqNr);
		args.put("securitycode", securityCode);
		args.put("username", username);
		args.put("passw", password);

		try
		{
			return executeSOAPRequest(ACTION_GETDATAFORXML, args);
		}
		catch (SOAPException e)
		{
			throw new IOException(e);
		}
	}

	private XMLStreamReader executeSOAPRequest(String action, Map<String, String> args) throws SOAPException,
			IOException
	{
		SOAPConnection soapConnection = null;
		try
		{
			// Create SOAP Connection
			soapConnection = soapConnectionFactory.createConnection();

			// Send SOAP Message to SOAP Server
			SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(action, args), endpoint);

			// Fail on SOAP error
			if (soapResponse.getSOAPBody().hasFault())
			{
				throw new IOException(soapResponse.getSOAPBody().getFault().getFaultString());
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			soapResponse.writeTo(bos);

			// Return stream with XML response content
			return createXmlResponse(bos);
		}
		catch (SOAPException e)
		{
			throw new IOException(e);
		}
		catch (XMLStreamException e)
		{
			throw new IOException(e);
		}
		finally
		{
			if (soapConnection != null)
			{
				try
				{
					soapConnection.close();
				}
				catch (SOAPException e)
				{
					throw new IOException(e);
				}
			}
		}
	}

	private SOAPMessage createSOAPRequest(String action, Map<String, String> args) throws SOAPException, IOException
	{
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();

		// SOAP Envelope
		SOAPEnvelope envelope = soapPart.getEnvelope();

		// SOAP Body
		SOAPBody soapBody = envelope.getBody();
		SOAPElement container = soapBody.addChildElement(action, null, NAMESPACE_VALUE);
		if (!args.isEmpty())
		{
			for (Map.Entry<String, String> entry : args.entrySet())
			{
				container.addChildElement(entry.getKey()).addTextNode(entry.getValue());
			}
		}

		MimeHeaders headers = soapMessage.getMimeHeaders();
		headers.addHeader("SOAPAction", NAMESPACE_VALUE + action);

		soapMessage.saveChanges();

		return soapMessage;
	}

	private XMLStreamReader createXmlResponse(ByteArrayOutputStream bos) throws XMLStreamException
	{
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		xmlInputFactory.setProperty("javax.xml.stream.isCoalescing", true);
		return xmlInputFactory.createXMLStreamReader(new ByteArrayInputStream(bos.toByteArray()));
	}
}
