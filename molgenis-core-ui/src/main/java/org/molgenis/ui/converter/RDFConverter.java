package org.molgenis.ui.converter;

import com.google.common.collect.Multimap;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.service.impl.UntypedTagService;
import org.springframework.beans.factory.annotation.Autowired;
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
	private final UntypedTagService untypedTagService;

	@Autowired
	public RDFConverter(UntypedTagService untypedTagService)
	{
		super(TEXT_TURTLE, APPLICATION_TRIG);
		this.untypedTagService = untypedTagService;
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
		EntityType entityType = entity.getEntityType();
		for (Attribute attribute : entityType.getAtomicAttributes())
		{
			Multimap<Relation, LabeledResource> tags = untypedTagService.getTagsForAttribute(entityType, attribute);
			for(LabeledResource tag: tags.get(Relation.isAssociatedWith)){
				writer.write(tag.getIri());
				writer.write(',');
				writer.write(tag.getLabel());
				writer.write(':');
				writer.write(entity.get(attribute.getName()).toString());
				writer.write('\n');
			}
		}
		writer.write(entity.toString());
		writer.close();
	}
}
