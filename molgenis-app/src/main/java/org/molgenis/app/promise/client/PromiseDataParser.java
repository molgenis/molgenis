package org.molgenis.app.promise.client;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PromiseDataParser
{
	private final PromiseClient promiseClient;

	@Autowired
	public PromiseDataParser(PromiseClient promiseClient)
	{
		this.promiseClient = Objects.requireNonNull(promiseClient, "promiseClient is null");
	}

	//TODO: better to do this in the unmarshaller as well I think
	public Iterable<Entity> parse(Entity credentials, Integer seqNr) throws IOException
	{
		String resultXml = promiseClient.getDataForXml(credentials, seqNr.toString());
		String rootElementName = "root";
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append('<');
		strBuilder.append(rootElementName);
		strBuilder.append('>');
		strBuilder.append(resultXml);
		strBuilder.append("</");
		strBuilder.append(rootElementName);
		strBuilder.append('>');

		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		XMLStreamReader xmlContentReader;
		try
		{
			xmlContentReader = xmlInputFactory.createXMLStreamReader(new StringReader(strBuilder.toString()));
			return parseContentContainer(xmlContentReader, rootElementName);
		}
		catch (XMLStreamException e)
		{
			throw new IOException(e);
		}
	}

	private static Iterable<Entity> parseContentContainer(XMLStreamReader xmlStreamReader, String parentLocalName)
			throws XMLStreamException
	{
		List<Entity> entities = new ArrayList<Entity>();

		while (xmlStreamReader.hasNext())
		{
			switch (xmlStreamReader.next())
			{
				case START_ELEMENT:
					if (!xmlStreamReader.getLocalName().equals(parentLocalName))
					{
						// parse study
						Entity entity = parseContent(xmlStreamReader, xmlStreamReader.getLocalName());
						entities.add(entity);
					}
					break;
				default:
					break;
			}
		}

		if (entities.isEmpty()) throw new MolgenisDataException("No ProMISe entities found in response");

		return entities;
	}

	private static Entity parseContent(XMLStreamReader xmlStreamReader, String parentLocalName)
			throws XMLStreamException
	{
		MapEntity entity = new MapEntity();
		boolean parse = true;
		while (parse && xmlStreamReader.hasNext())
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
