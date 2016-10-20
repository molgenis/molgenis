package org.molgenis.fair.controller;

import com.google.common.collect.Multimap;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.LiteralImpl;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.molgenis.AttributeType;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.google.common.collect.Iterables.any;
import static org.molgenis.data.support.EntityTypeUtils.isMultipleReferenceType;
import static org.molgenis.data.support.EntityTypeUtils.isSingleReferenceType;
import static org.molgenis.fair.controller.FairController.BASE_URI;
import static org.molgenis.ui.converter.RDFMediaType.TEXT_TURTLE_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(BASE_URI)
/**
 * Serves metadata for the molgenis FAIR DataPoint.
 */ public class FairController
{
	static final String BASE_URI = "/fdp";

	private final DataService dataService;

	public static final String KEYWORD = "http://www.w3.org/ns/dcat#keyword";

	private final TagService<LabeledResource, LabeledResource> tagService;
	private final ValueFactory valueFactory;

	@Autowired
	public FairController(DataService dataService, TagService<LabeledResource, LabeledResource> tagService)
	{
		this.dataService = dataService;
		this.tagService = tagService;
		this.valueFactory = SimpleValueFactory.getInstance();
	}

	private static String getBaseUri(HttpServletRequest request)
	{
		String apiUrl;
		if (StringUtils.isEmpty(request.getHeader("X-Forwarded-Host")))
		{
			apiUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getLocalPort() + BASE_URI;
		}
		else
		{
			apiUrl = request.getScheme() + "://" + request.getHeader("X-Forwarded-Host") + BASE_URI;
		}
		return apiUrl;
	}

	@RequestMapping(method = GET, produces = TEXT_TURTLE_VALUE)
	@ResponseBody
	@RunAsSystem
	public Model getMetadata(HttpServletRequest request)
	{
		String subjectIRI = getBaseUri(request);
		Entity subjectEntity = dataService.findOne("fdp_Metadata", new QueryImpl<>());
		return createRdfModel(subjectIRI, subjectEntity);
	}

	@RequestMapping(method = GET, produces = TEXT_TURTLE_VALUE, value = "/{catalogID:.+}")
	@ResponseBody
	@RunAsSystem
	public Model getCatalog(@PathVariable("catalogID") String catalogID, HttpServletRequest request)
	{
		String subjectIRI = getBaseUri(request) + "/" + catalogID;
		Entity subjectEntity = dataService.findOneById("fdp_Catalog", catalogID);
		return createRdfModel(subjectIRI, subjectEntity);
	}

	@RequestMapping(method = GET, produces = TEXT_TURTLE_VALUE, value = "/{catalogID:.+}/{datasetID:.+}")
	@ResponseBody
	@RunAsSystem
	public Model getDataset(@PathVariable("catalogID") String catalogID, @PathVariable("datasetID") String datasetID,
			HttpServletRequest request)
	{
		String subjectIRI = getBaseUri(request) + "/" + catalogID + "/" + datasetID;
		Entity subjectEntity = dataService.findOneById("fdp_Dataset", datasetID);
		return createRdfModel(subjectIRI, subjectEntity);
	}

	@RequestMapping(method = GET, produces = TEXT_TURTLE_VALUE, value = "/{catalogID:.+}/{datasetID:.+}/{distributionID:.+}")
	@ResponseBody
	@RunAsSystem
	public Model getDistribution(@PathVariable("catalogID") String catalogID,
			@PathVariable("datasetID") String datasetID, @PathVariable("distributionID") String distributionID,
			HttpServletRequest request)
	{

		String subjectIRI = getBaseUri(request) + "/" + catalogID + "/" + datasetID + "/" + distributionID;
		Entity subjectEntity = dataService.findOneById("fdp_Distribution", distributionID);
		return createRdfModel(subjectIRI, subjectEntity);
	}

	private Model createRdfModel(String subjectIRI, Entity entity)
	{
		Model model = new LinkedHashModel();
		EntityType entityType = entity.getEntityType();

		for (Attribute attribute : entityType.getAtomicAttributes())
		{
			Multimap<Relation, LabeledResource> tags = tagService.getTagsForAttribute(entityType, attribute);
			Object value = entity.get(attribute.getName());
			if (value != null)
			{
				for (LabeledResource tag : tags.get(Relation.isAssociatedWith))
				{
					if (attribute.getDataType() == AttributeType.HYPERLINK)
					{
						convertIRIToRdf(subjectIRI, tag.getIri(), entity.getString(attribute.getName()), model);
					}
					else if (isMultipleReferenceType(attribute))
					{
						Iterable<Entity> mrefs = entity.getEntities(attribute.getName());
						for (Entity objectEntity : mrefs)
						{
							convertIRIToRdf(subjectIRI, tag.getIri(), getObjectIri(subjectIRI, objectEntity), model);
						}
					}
					else if (isSingleReferenceType(attribute))
					{
						Entity objectEntity = entity.getEntity(attribute.getName());
						convertIRIToRdf(subjectIRI, tag.getIri(), getObjectIri(subjectIRI, objectEntity), model);
					}
					else if (attribute.getDataType() == AttributeType.DATE_TIME)
					{
						try
						{
							literalToRdf(subjectIRI, tag.getIri(),
									getDateObject(entity.getUtilDate(attribute.getName())), model);
						}
						catch (DatatypeConfigurationException e)
						{
							throw new RuntimeException(e);
						}
					}
					else if (tag.getIri().equals(KEYWORD))
					{
						Arrays.stream(((String) value).split(","))
								.forEach(keyword -> convertValueToRdf(subjectIRI, tag.getIri(), keyword, model));
					}
					else
					{
						convertValueToRdf(subjectIRI, tag.getIri(), value, model);
					}
				}
			}
		}
		return model;
	}

	/**
	 * Creates the object IRI for an object.
	 * If the object has an attribute named IRI, this is the value of that attribute.
	 * Otherwise, we create it by postfixing the subject's IRI with '/' and the object's ID value.
	 */
	private String getObjectIri(String subjectIRI, Entity object)
	{
		if (any(object.getEntityType().getAtomicAttributes(), attr -> "IRI".equals(attr.getName())))
		{
			return object.getString("IRI");
		}
		else
		{
			return subjectIRI + "/" + object.getIdValue();
		}
	}

	private Literal getDateObject(Date date) throws DatatypeConfigurationException
	{
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(date);
		XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().
				newXMLGregorianCalendar(c);
		Literal currentTime = new LiteralImpl(xmlDate.toXMLFormat(), XMLSchema.DATETIME);
		return currentTime;
	}

	public void convertValueToRdf(String subjectIRI, String tag, Object attrValue, Model model)
	{
		Resource subject = valueFactory.createIRI(subjectIRI);
		IRI predicate = valueFactory.createIRI(tag);
		Literal object = valueFactory.createLiteral(attrValue.toString());
		model.add(subject, predicate, object);
	}

	public void literalToRdf(String subjectIRI, String tag, Literal attrValue, Model model)
	{
		Resource subject = valueFactory.createIRI(subjectIRI);
		IRI predicate = valueFactory.createIRI(tag);
		model.add(subject, predicate, attrValue);
	}

	public void convertIRIToRdf(String subjectIRI, String tag, String attrValue, Model model)
	{
		Resource subject = valueFactory.createIRI(subjectIRI);
		IRI predicate = valueFactory.createIRI(tag);
		Resource object = valueFactory.createIRI(attrValue.toString());
		model.add(subject, predicate, object);
	}

}
