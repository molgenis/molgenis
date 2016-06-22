package org.molgenis.app.promise.client;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

@Component
public class PromiseDataParser
{
	private final PromiseClient promiseClient;

	private static final Logger LOG = LoggerFactory.getLogger(PromiseDataParser.class);

	@Autowired
	public PromiseDataParser(PromiseClient promiseClient)
	{
		this.promiseClient = Objects.requireNonNull(promiseClient, "promiseClient is null");
	}

	public void parse(Entity credentials, Integer seqNr, Consumer<Entity> entityConsumer) throws IOException
	{
		promiseClient.getData(credentials, seqNr.toString(), reader -> {
			try
			{
				boolean inDocumentElement = false;
				while (reader.hasNext())
				{
					switch (reader.next())
					{
						case START_ELEMENT:
							if ("DocumentElement".equals(reader.getLocalName()))
							{
								inDocumentElement = true;
							}
							else
							{
								if (inDocumentElement)
								{
									entityConsumer.accept(parseContent(reader));
								}
							}
							break;
						case END_ELEMENT:
							if ("DocumentElement".equals(reader.getLocalName()))
							{
								inDocumentElement = false;
							}
							break;
					}
				}
			}
			catch (XMLStreamException e)
			{
				LOG.error("Something went wrong: ", e);
			}
		});
	}

	/**
	 * Parses one entity from the reader
	 *
	 * @param reader
	 * @return the parsed {@link Entity}
	 * @throws XMLStreamException
	 */
	private static Entity parseContent(XMLStreamReader reader) throws XMLStreamException
	{
		MapEntity entity = new MapEntity();
		while (reader.hasNext())
		{
			switch (reader.next())
			{
				case START_ELEMENT:
					entity.set(reader.getLocalName(), reader.getElementText());
					break;
				case END_ELEMENT:
					return entity;
				default:
					break;
			}
		}
		return entity;
	}
}
