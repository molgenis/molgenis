package org.molgenis.core.ui.messageconverter;

import org.apache.commons.io.IOUtils;
import org.molgenis.core.util.BaseHttpMessageConverter;
import org.molgenis.data.EntityCollection;
import org.molgenis.data.csv.CsvWriter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Converts an EntityCollection to comma separated values
 */
public class CsvHttpMessageConverter extends BaseHttpMessageConverter<EntityCollection>
{

	public CsvHttpMessageConverter()
	{
		super(new MediaType("text", "csv", DEFAULT_CHARSET));
	}

	@Override
	protected void writeInternal(EntityCollection entities, HttpOutputMessage outputMessage) throws IOException
	{
		OutputStreamWriter out = new OutputStreamWriter(outputMessage.getBody(),
				getCharset(outputMessage.getHeaders()));
		CsvWriter writer = new CsvWriter(out);
		try
		{
			writer.writeAttributeNames(entities.getAttributeNames());
			writer.add(entities.stream());
		}
		finally
		{
			IOUtils.closeQuietly(writer);
		}
	}

	@Override
	protected boolean supports(Class<?> clazz)
	{
		return EntityCollection.class.isAssignableFrom(clazz);
	}

	@Override
	protected EntityCollection readInternal(Class<? extends EntityCollection> clazz, HttpInputMessage inputMessage)
			throws IOException
	{
		throw new UnsupportedOperationException();
	}

}
