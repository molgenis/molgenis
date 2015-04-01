package org.molgenis.fair;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.model.MolgenisModelException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class FairService
{
	// Namespaces
	public static final String MOLGENIS_FAIR_NS = "http://molgenis.org/2015/04/fair#";
	public static final String FAIR_NS = "http://fairdata.org/ontology/FAIR-Data#";

	// FAIR Class IRIs
	public static final String FAIR_PROFILE = FAIR_NS + "FAIRProfile";
	public static final String FAIR_CLASS = FAIR_NS + "FAIRClass";
	public static final String FAIR_PROPERTY = FAIR_NS + "FAIRProperty";

	// FAIR Property IRIs
	public static final String FAIR_HAS_CLASS = FAIR_NS + "hasClass";
	public static final String FAIR_HAS_PROPERTY = FAIR_NS + "hasProperty";
	public static final String FAIR_ON_CLASS_TYPE = FAIR_NS + "onClassType";
	public static final String FAIR_ON_PROPERTY_TYPE = FAIR_NS + "onPropertyType";
	public static final String FAIR_ALLOWED_VALUES = FAIR_NS + "allowedValues";
	public static final String FAIR_MIN_COUNT = FAIR_NS + "minCount";
	public static final String FAIR_MAX_COUNT = FAIR_NS + "maxCount";

	// MOLGENIS Class IRIs
	public static final String MOLGENIS_PACKAGE = MOLGENIS_FAIR_NS + "Package";
	public static final String MOLGENIS_ENTITY = MOLGENIS_FAIR_NS + "Entity";
	public static final String MOLGENIS_ATTRIBUTE = MOLGENIS_FAIR_NS + "Attribute";
	public static final String MOLGENIS_DATATYPE = MOLGENIS_FAIR_NS + "DataType";

	@Autowired
	private MetaDataService meta;

	public Model getProfile(String id)
	{
		OntModel result = ModelFactory.createOntologyModel();

		result.read(getClass().getResourceAsStream("FAIR-schema.owl"), "RDF/XML");
		result.setNsPrefix("mlg", MOLGENIS_FAIR_NS);

		OntClass fairProfile = result.getOntClass(FAIR_PROFILE);
		OntClass fairClass = result.getOntClass(FAIR_CLASS);
		OntClass fairProperty = result.getOntClass(FAIR_PROPERTY);

		Property fairOnClassType = result.getObjectProperty(FAIR_ON_CLASS_TYPE);
		Property fairOnPropertyType = result.getObjectProperty(FAIR_ON_PROPERTY_TYPE);
		Property fairHasClass = result.getObjectProperty(FAIR_HAS_CLASS);
		Property fairHasProperty = result.getObjectProperty(FAIR_HAS_PROPERTY);
		Property fairAllowedValues = result.getProperty(FAIR_ALLOWED_VALUES);
		Property fairMinCount = result.getProperty(FAIR_MIN_COUNT);
		Property fairMaxCount = result.getProperty(FAIR_MAX_COUNT);

		// Property rdfType = result.getProperty(RDF_TYPE);

		OntClass molgenisEntity = result.createClass(MOLGENIS_ENTITY);
		OntClass molgenisAttribute = result.createClass(MOLGENIS_ATTRIBUTE);
		OntClass molgenisDataType = result.createClass(MOLGENIS_DATATYPE);

		Property rdfsSubclassOf = result.getProperty(RDFS_SUBCLASS_OF);

		String profileIRI = MOLGENIS_FAIR_NS + id;
		Individual molgenisProfile = result.createIndividual(profileIRI, fairProfile);

		for (EntityMetaData emd : meta.getEntityMetaDatas())
		{
			if (emd.isAbstract())
			{
				continue;
			}
			Individual emdFairClass = result.createIndividual(profileIRI + "/fair/" + emd.getName(), fairClass);
			result.add(molgenisProfile, fairHasClass, emdFairClass);
			OntClass emdEntity = result.createClass(profileIRI + "/" + emd.getName());
			result.add(emdEntity, rdfsSubclassOf, molgenisEntity);
			// result.add(emdEntity, rdfType, rdfClass);
			if (emd.getDescription() != null)
			{
				emdEntity.addComment(emd.getDescription(), null);
			}
			result.add(emdFairClass, fairOnClassType, molgenisEntity);
			for (AttributeMetaData amd : emd.getAtomicAttributes())
			{
				Individual amdFairProperty = result.createIndividual(
						profileIRI + "/fair/" + emd.getName() + "/" + amd.getName(), fairProperty);
				FieldType dataType = amd.getDataType();
				result.add(emdFairClass, fairHasProperty, amdFairProperty);

				if (amd.isNillable())
				{
					result.add(amdFairProperty, fairMinCount, result.createTypedLiteral(0));
				}
				else
				{
					result.add(amdFairProperty, fairMinCount, result.createTypedLiteral(1));
				}

				if (!(dataType instanceof MrefField))
				{
					result.add(amdFairProperty, fairMaxCount, result.createTypedLiteral(1));
				}

				OntClass amdAttribute = result.createClass(profileIRI + "/" + emd.getName() + "/" + amd.getName());
				result.add(amdAttribute, rdfsSubclassOf, molgenisAttribute);
				result.add(amdFairProperty, fairOnPropertyType, amdAttribute);
				if (dataType instanceof XrefField || dataType instanceof MrefField)
				{
					OntClass refEntity = result.createClass(profileIRI + "/" + amd.getRefEntity().getName());
					result.add(amdAttribute, fairAllowedValues, refEntity);
				}
				else
				{
					Individual dataTypeIndividual = result.createIndividual(
							MOLGENIS_FAIR_NS + "dataType/" + dataType.toString(), molgenisDataType);
					result.add(amdAttribute, fairAllowedValues, dataTypeIndividual);
					result.add(amdAttribute, fairAllowedValues,
							result.createResource(ResourceFactory.createResource(getXSDDataType(dataType))));
				}
			}
		}
		return result;
	}

	public String getXSDDataType(FieldType type)
	{
		try
		{
			return "http://www.w3.org/2001/XMLSchema#" + type.getXsdType();
		}
		catch (MolgenisModelException e)
		{
			// should not happen
			throw new IllegalStateException("Unknown XSD datatype for data type " + type);
		}
	}

}
