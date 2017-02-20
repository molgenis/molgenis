package org.molgenis.fair.controller;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.datatype.DatatypeConfigurationException;

import static com.google.common.collect.Iterables.contains;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

@Component
public class EntityModelWriter
{
	private static final String KEYWORD = "http://www.w3.org/ns/dcat#keyword";

	private final SimpleValueFactory valueFactory;
	private final TagService<LabeledResource, LabeledResource> tagService;

	@Autowired
	public EntityModelWriter(TagService<LabeledResource, LabeledResource> tagService, SimpleValueFactory valueFactory)
			throws DatatypeConfigurationException
	{
		this.valueFactory = requireNonNull(valueFactory);
		this.tagService = requireNonNull(tagService);
	}

	private void setNamespacePrefixes(Model model)
	{
		model.setNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		model.setNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		model.setNamespace("dcat", "http://www.w3.org/ns/dcat#");
		model.setNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
		model.setNamespace("owl", "http://www.w3.org/2002/07/owl#");
		model.setNamespace("dct", "http://purl.org/dc/terms/");
		model.setNamespace("lang", "http://id.loc.gov/vocabulary/iso639-1/");
		model.setNamespace("fdpo", "http://rdf.biosemantics.org/ontologies/fdp-o#");
		model.setNamespace("ldp", "http://www.w3.org/ns/ldp#");
		model.setNamespace("foaf", "http://xmlns.com/foaf/0.1/");
		model.setNamespace("orcid", "http://orcid.org/");
	}

	public Model createRdfModel(String subjectIRI, Entity objectEntity)
	{
		Resource subject = valueFactory.createIRI(subjectIRI);
		Model model = new LinkedHashModel();
		setNamespacePrefixes(model);
		EntityType entityType = objectEntity.getEntityType();

		for (Attribute objectAttribute : entityType.getAtomicAttributes())
		{
			Object value = objectEntity.get(objectAttribute.getName());
			if (value == null)
			{
				continue;
			}
			for (LabeledResource tag : tagService.getTagsForAttribute(entityType, objectAttribute)
					.get(Relation.isAssociatedWith))
			{
				IRI predicate = valueFactory.createIRI(tag.getIri());
				addRelationForAttribute(model, subject, predicate, objectEntity, objectAttribute);
			}
		}
		return model;
	}

	private void addRelationForAttribute(Model model, Resource subject, IRI predicate, Entity objectEntity,
			Attribute objectAttribute)
	{
		String name = objectAttribute.getName();

		switch (objectAttribute.getDataType())
		{
			case MREF:
			case CATEGORICAL_MREF:
				addRelationForMrefTypeAttribute(model, subject, predicate, objectEntity.getEntities(name));
				break;
			case BOOL:
				model.add(subject, predicate, valueFactory.createLiteral(objectEntity.getBoolean(name)));
				break;
			case DATE:
			case DATE_TIME:
				model.add(subject, predicate, valueFactory.createLiteral(objectEntity.getUtilDate(name)));
				break;
			case DECIMAL:
				model.add(subject, predicate, valueFactory.createLiteral(objectEntity.getDouble(name)));
				break;
			case LONG:
				model.add(subject, predicate, valueFactory.createLiteral(objectEntity.getLong(name)));
				break;
			case INT:
				model.add(subject, predicate, valueFactory.createLiteral(objectEntity.getInt(name)));
				break;
			case ENUM:
			case EMAIL:
			case HTML:
			case TEXT:
			case SCRIPT:
			case STRING:
				addRelationForStringTypeAttribute(model, subject, predicate, objectEntity.getString(name));
				break;
			case HYPERLINK:
				model.add(subject, predicate, valueFactory.createIRI(objectEntity.getString(name)));
				break;
			case XREF:
			case CATEGORICAL:
			case FILE:
				addRelationForXrefTypeAttribute(model, subject, predicate, objectEntity.getEntity(name));
				break;
			default:
				throw new RuntimeException("DataType " + objectAttribute.getDataType() + "is not supported");
		}
	}

	private void addRelationForXrefTypeAttribute(Model model, Resource subject, IRI predicate, Entity objectEntity)
	{
		if (contains(objectEntity.getEntityType().getAttributeNames(), "IRI"))
		{
			model.add(subject, predicate, valueFactory.createIRI(objectEntity.getString("IRI")));
		}
		else
		{
			model.add(subject, predicate,
					valueFactory.createIRI(subject.stringValue() + '/' + objectEntity.getIdValue()));
		}
	}

	private void addRelationForStringTypeAttribute(Model model, Resource subject, IRI predicate, String value)
	{
		if (predicate.stringValue().equals(KEYWORD))
		{
			stream(value.split(",")).map(String::trim)
					.forEach(keyword -> model.add(subject, predicate, valueFactory.createLiteral(keyword)));
		}
		else
		{
			model.add(subject, predicate, valueFactory.createLiteral(value));
		}
	}

	private void addRelationForMrefTypeAttribute(Model model, Resource subject, IRI predicate,
			Iterable<Entity> objectEntities)
	{
		for (Entity objectEntity : objectEntities)
		{
			model.add(subject, predicate,
					valueFactory.createIRI(subject.stringValue() + '/' + objectEntity.getIdValue()));

		}
	}
}
