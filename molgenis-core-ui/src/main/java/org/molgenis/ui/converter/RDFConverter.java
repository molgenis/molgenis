package org.molgenis.ui.converter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.apache.jena.riot.RDFFormat.TURTLE;
import static org.molgenis.ui.converter.RDFMediaType.APPLICATION_TRIG;
import static org.molgenis.ui.converter.RDFMediaType.TEXT_TURTLE;

@Component
public class RDFConverter extends AbstractHttpMessageConverter<Model>
{
	public RDFConverter()
	{
		super(TEXT_TURTLE, APPLICATION_TRIG);
	}

	@Override
	protected boolean supports(Class<?> aClass)
	{
		return Model.class.isAssignableFrom(aClass);
	}

	@Override
	protected Model readInternal(Class<? extends Model> aClass, HttpInputMessage httpInputMessage)
			throws IOException, HttpMessageNotReadableException
	{
		throw new HttpMessageNotReadableException("RDF support is readonly!");
	}

	@Override
	@RunAsSystem
	protected void writeInternal(Model model, HttpOutputMessage httpOutputMessage)
			throws IOException, HttpMessageNotWritableException
	{
		RDFDataMgr.write(httpOutputMessage.getBody(), model, TURTLE);
		httpOutputMessage.getBody().close();
	}

}
