package org.molgenis.ui.converter;

import com.google.common.collect.Multimap;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
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
import java.util.Calendar;

import static org.molgenis.data.support.EntityTypeUtils.isMultipleReferenceType;
import static org.molgenis.data.support.EntityTypeUtils.isSingleReferenceType;
import static org.molgenis.ui.converter.RDFMediaType.APPLICATION_TRIG;
import static org.molgenis.ui.converter.RDFMediaType.TEXT_TURTLE;

@Component
public class RDFConverter extends AbstractHttpMessageConverter<SubjectEntity>
{
	private final TagService<LabeledResource, LabeledResource> tagService;
	private final PrefixMapping prefixMapping = new PrefixMappingImpl();

	@Autowired
	public RDFConverter(TagService<LabeledResource, LabeledResource> tagService)
	{
		super(TEXT_TURTLE, APPLICATION_TRIG);
		this.tagService = tagService;

		prefixMapping.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixMapping.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		prefixMapping.setNsPrefix("dcat", "http://www.w3.org/ns/dcat#");
		prefixMapping.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		prefixMapping.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
		prefixMapping.setNsPrefix("dct", "http://purl.org/dc/terms/");
		prefixMapping.setNsPrefix("lang", "http://id.loc.gov/vocabulary/iso639-1/");
		prefixMapping.setNsPrefix("fdpo", "http://rdf.biosemantics.org/ontologies/fdp-o#");
		prefixMapping.setNsPrefix("gct", "http://purl.org/gc/terms/");
		prefixMapping.setNsPrefix("ldp", "http://www.w3.org/ns/ldp#");
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
					else if (isMultipleReferenceType(attribute))
					{
						Iterable<Entity> mrefs = entity.getEntities(attribute.getName());
						for (Entity mref : mrefs)
						{
							convertIRIToRdf(subjectEntity.getSubject(), tag.getIri(),
									subjectEntity.getSubject() + "/" + mref.getIdValue(), model);
						}
					}
					else if (isSingleReferenceType(attribute))
					{
						convertIRIToRdf(subjectEntity.getSubject(), tag.getIri(),
								subjectEntity.getSubject() + "/" + entity.getEntity(attribute.getName()).getIdValue(),
								model);
					}
					else if (attribute.getDataType().equals(AttributeType.DATE_TIME))
					{
						convertValueToRdf(subjectEntity.getSubject(), tag.getIri(),
								getXmlDateObject(entity, attribute, value), model);
					}
					else
					{
						convertValueToRdf(subjectEntity.getSubject(), tag.getIri(), value, model);
					}
				}
			}
		}
		model.setNsPrefixes(prefixMapping);
		RDFDataMgr.write(httpOutputMessage.getBody(), model, RDFFormat.TURTLE);
		httpOutputMessage.getBody().close();
	}

	private Object getXmlDateObject(Entity entity, Attribute attribute, Object value)
	{

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(entity.getUtilDate(attribute.getName()));
		XSDDateTime xsdDateTime = new XSDDateTime(calendar);
		value = xsdDateTime;
		return value;
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
