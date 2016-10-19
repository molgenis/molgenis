package org.molgenis.ui.converter;

import com.google.common.collect.Multimap;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.ui.model.SubjectEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import static org.molgenis.ui.converter.RDFMediaType.APPLICATION_TRIG;
import static org.molgenis.ui.converter.RDFMediaType.TEXT_TURTLE;

@Component
public class RDFConverter extends AbstractHttpMessageConverter<SubjectEntity>
{
	private final TagService<LabeledResource, LabeledResource> tagService;

	@Autowired
	public RDFConverter(TagService<LabeledResource, LabeledResource> tagService)
	{
		super(TEXT_TURTLE, APPLICATION_TRIG);
		this.tagService = tagService;
	}

	@Override
	protected boolean supports(Class<?> aClass)
	{
		return SubjectEntity.class.isAssignableFrom(aClass);
	}

	@Override
	protected SubjectEntity readInternal(Class<? extends SubjectEntity> aClass, HttpInputMessage httpInputMessage)
			throws IOException, HttpMessageNotReadableException
	{
		throw new HttpMessageNotReadableException("RDF support is readonly!");
	}

	@Override
	protected void writeInternal(SubjectEntity subjectEntity, HttpOutputMessage httpOutputMessage)
			throws IOException, HttpMessageNotWritableException
	{
		Entity entity = subjectEntity.getEntity();
		Writer writer = new OutputStreamWriter(httpOutputMessage.getBody(), Charset.forName("UTF-8"));
		EntityType entityType = entity.getEntityType();
		Model model = ModelFactory.createDefaultModel();
		for (Attribute attribute : entityType.getAtomicAttributes())
		{
			Multimap<Relation, LabeledResource> tags = tagService.getTagsForAttribute(entityType, attribute);
			for (LabeledResource tag : tags.get(Relation.isAssociatedWith))
			{
				Object value = entity.get(attribute.getName());
				if(value != null)
				{
					convertToRdf(subjectEntity.getSubject() ,tag.getIri(), value.toString(), model);
				}
			}
		}
		RDFDataMgr.write(writer, model, RDFFormat.TURTLE);
		writer.close();

	}

	public String convertToRdf(String subjectString, String tag, String attrName, Model model)
	{
		StringWriter stringWriter = new StringWriter();
		try
		{
			Resource subject = model.createResource(subjectString);
			Property predicate = model.createProperty(tag);
			Resource object = model.createResource(attrName);
			connect(subject, predicate, object, model);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return stringWriter.toString();
	}

	private static Statement connect(Resource subject, Property predicate, Resource object, Model model)
	{
		model.add(subject, predicate, object);
		return model.createStatement(subject, predicate, object);
	}
}
