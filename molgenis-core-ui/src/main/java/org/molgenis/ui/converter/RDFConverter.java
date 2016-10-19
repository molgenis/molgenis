package org.molgenis.ui.converter;

import org.molgenis.data.Entity;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import static org.molgenis.ui.converter.RDFMediaType.APPLICATION_TRIG;
import static org.molgenis.ui.converter.RDFMediaType.TEXT_TURTLE;

@Component
public class RDFConverter extends AbstractHttpMessageConverter<Entity>
{
	public RDFConverter()
	{
		super(TEXT_TURTLE, APPLICATION_TRIG);
	}

	@Override
	protected boolean supports(Class<?> aClass)
	{
		return Entity.class.isAssignableFrom(aClass);
	}

	@Override
	protected Entity readInternal(Class<? extends Entity> aClass, HttpInputMessage httpInputMessage)
			throws IOException, HttpMessageNotReadableException
	{
		throw new HttpMessageNotReadableException("RDF support is readonly!");
	}

	@Override
	protected void writeInternal(Entity entity, HttpOutputMessage httpOutputMessage)
			throws IOException, HttpMessageNotWritableException
	{
		Writer writer = new OutputStreamWriter(httpOutputMessage.getBody(),
				Charset.forName("UTF-8"));
		writer.write("TODO: turn into RDF!\n");
		writer.write(entity.toString());
		writer.close();
	}
}
