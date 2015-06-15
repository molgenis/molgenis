package org.molgenis.app.promise;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProMiseDataParser
{
	private static final String DATA_CONTAINER_ELEMENT = "getDataForXMLResult";

	private final ProMiseClient promiseClient;
	private final String project;
	private final String pws;
	private final String securityCode;
	private final String username;
	private final String password;

	@Autowired
	public ProMiseDataParser(ProMiseClient promiseClient, @Value("${promise.project:@null}") String project,
			@Value("${promise.pws:@null}") String pws, @Value("${promise.securityCode:@null}") String securityCode,
			@Value("${promise.username:@null}") String username, @Value("${promise.password:@null}") String password)
	{
		if (promiseClient == null)
		{
			throw new IllegalArgumentException("promiseClient is null");
		}
		if (project == null)
		{
			throw new IllegalArgumentException(
					"project is null, did you define 'promise.project' in molgenis-server.properties?");
		}
		if (pws == null)
		{
			throw new IllegalArgumentException(
					"pws is null, did you define 'promise.pws' in molgenis-server.properties?");
		}
		if (securityCode == null)
		{
			throw new IllegalArgumentException(
					"securityCode is null, did you define 'promise.securityCode' in molgenis-server.properties?");
		}
		if (username == null)
		{
			throw new IllegalArgumentException(
					"username is null, did you define 'promise.username' in molgenis-server.properties?");
		}
		if (password == null)
		{
			throw new IllegalArgumentException(
					"password is null, did you define 'promise.password' in molgenis-server.properties?");
		}
		this.promiseClient = promiseClient;
		this.project = project;
		this.pws = pws;
		this.securityCode = securityCode;
		this.username = username;
		this.password = password;
	}

	public Iterable<Entity> parse() throws IOException
	{
		String seqNr = "0";

		List<Entity> entities = new ArrayList<Entity>();
		XMLStreamReader xmlStreamReader = promiseClient.getDataForXml(project, pws, seqNr, securityCode, username,
				password);
		try
		{
			while (xmlStreamReader.hasNext())
			{

				switch (xmlStreamReader.next())
				{
					case START_ELEMENT:
						if (xmlStreamReader.getLocalName().equals(DATA_CONTAINER_ELEMENT))
						{
							String xmlContent = xmlStreamReader.getElementText();

							XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
							XMLStreamReader xmlContentReader = xmlInputFactory.createXMLStreamReader(new StringReader(
									xmlContent));
							try
							{
								Entity entity = parseStudy(xmlStreamReader, xmlStreamReader.getLocalName());
								entities.add(entity);
							}
							finally
							{
								xmlContentReader.close();
							}
						}
						break;
					default:
						break;
				}
			}
		}
		catch (XMLStreamException e)
		{
			throw new IOException(e);
		}
		finally
		{
			try
			{
				xmlStreamReader.close();
			}
			catch (XMLStreamException e)
			{
				throw new RuntimeException(e);
			}
		}
		return entities;
	}

	private static Entity parseStudy(XMLStreamReader xmlStreamReader, String parentLocalName) throws XMLStreamException
	{
		MapEntity entity = new MapEntity();
		for (boolean parse = true; parse && xmlStreamReader.hasNext();)
		{
			switch (xmlStreamReader.next())
			{
				case START_ELEMENT:
					entity.set(xmlStreamReader.getLocalName(), xmlStreamReader.getElementText());
					break;
				case END_ELEMENT:
					if (xmlStreamReader.getLocalName().equals(parentLocalName))
					{
						parse = false;
					}
					break;
				default:
					break;
			}
		}
		return entity;
	}
}
