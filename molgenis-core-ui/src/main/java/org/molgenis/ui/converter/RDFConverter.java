package org.molgenis.ui.converter;

import com.google.common.collect.Multimap;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.molgenis.AttributeType;
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
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Calendar;

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
			Object value = entity.get(attribute.getName());
			for (LabeledResource tag : tags.get(Relation.isAssociatedWith))
			{
				if (value != null)
				{
					if (attribute.getDataType().equals(AttributeType.HYPERLINK))
					{
						convertIRIToRdf(subjectEntity.getSubject(), tag.getIri(), entity.getString(attribute.getName()),
								model);
					}
					else
					{
						if (attribute.getDataType().equals(AttributeType.DATE_TIME))
						{
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(entity.getUtilDate(attribute.getName()));
							XSDDateTime xsdDateTime = new XSDDateTime(calendar);
							value = xsdDateTime;
						}
						convertValueToRdf(subjectEntity.getSubject(), tag.getIri(), value, model);
					}
				}
			}
		}
		RDFDataMgr.write(writer, model, RDFFormat.TURTLE);
		writer.close();
	}

	public void convertValueToRdf(String subjectString, String tag, Object attrValue, Model model)
	{
		try
		{
			Resource subject = model.createResource(subjectString);
			Property predicate = model.createProperty(tag);
			Literal object = model.createTypedLiteral(attrValue);
			model.add(subject, predicate, object);
			model.createStatement(subject, predicate, object);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public void convertIRIToRdf(String subjectString, String tag, String attrValue, Model model)
	{
		try
		{
			Resource subject = model.createResource(subjectString);
			Property predicate = model.createProperty(tag);
			Resource object = model.createResource(attrValue);
			model.add(subject, predicate, object);
			model.createStatement(subject, predicate, object);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
