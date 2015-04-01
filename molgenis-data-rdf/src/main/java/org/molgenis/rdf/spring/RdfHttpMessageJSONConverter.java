package org.molgenis.rdf.spring;

import java.io.IOException;

import org.molgenis.util.BaseHttpMessageConverter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.hp.hpl.jena.rdf.model.Model;

public class RdfHttpMessageJSONConverter extends BaseHttpMessageConverter<Model>
{
	public RdfHttpMessageJSONConverter()
	{
		super(MediaType.APPLICATION_JSON);
	}

	@Override
	protected boolean supports(Class<?> clazz)
	{
		return Model.class.isAssignableFrom(clazz);
	}

	@Override
	protected Model readInternal(Class<? extends Model> clazz, HttpInputMessage inputMessage) throws IOException,
			HttpMessageNotReadableException
	{
		throw new HttpMessageNotReadableException("upload not implemented");
	}

	@Override
	protected void writeInternal(Model t, HttpOutputMessage outputMessage) throws IOException,
			HttpMessageNotWritableException
	{
		t.write(outputMessage.getBody(), "JSON-LD");
	}

}
