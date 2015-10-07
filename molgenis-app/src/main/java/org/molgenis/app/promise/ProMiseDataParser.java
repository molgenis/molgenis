package org.molgenis.app.promise;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProMiseDataParser
{
	private static final String DATA_CONTAINER_ELEMENT = "getDataForXMLResult";

	private final ProMiseClient promiseClient;

	@Autowired
	public ProMiseDataParser(ProMiseClient promiseClient)
	{
		this.promiseClient = Objects.requireNonNull(promiseClient, "promiseClient is null");
	}

	public Iterable<Entity> parse(String projectName, Integer seqNr) throws IOException
	{
		XMLStreamReader xmlStreamReader = promiseClient.getDataForXml(projectName, seqNr.toString());
		try
		{
			while (xmlStreamReader.hasNext())
			{

				switch (xmlStreamReader.next())
				{
					case START_ELEMENT:
						if (xmlStreamReader.getLocalName().equals(DATA_CONTAINER_ELEMENT))
						{
							String rootElementName = "root";
							StringBuilder strBuilder = new StringBuilder();
							strBuilder.append('<');
							strBuilder.append(rootElementName);
							strBuilder.append('>');
							strBuilder.append(xmlStreamReader.getElementText());
							strBuilder.append("</");
							strBuilder.append(rootElementName);
							strBuilder.append('>');

							XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
							XMLStreamReader xmlContentReader = xmlInputFactory.createXMLStreamReader(new StringReader(
									strBuilder.toString()));
							try
							{
								return parseContentContainer(xmlContentReader, rootElementName);
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
		return Collections.emptyList();
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
