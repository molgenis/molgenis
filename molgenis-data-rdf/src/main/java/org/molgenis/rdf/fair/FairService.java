package org.molgenis.rdf.fair;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.model.MolgenisModelException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class FairService
{
	// Namespaces
	public static final String FAIR_NS = "http://datafairport.org/ontology/FAIR-schema.owl#";

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

	@Autowired
	private MetaDataService meta;

	public Model getProfile(String restApi)
	{
		OntModel result = ModelFactory.createOntologyModel();

		result.setNsPrefix("fair", FAIR_NS);

		OntClass fairProfile = result.createClass(FAIR_PROFILE);
		OntClass fairClass = result.createClass(FAIR_CLASS);
		OntClass fairProperty = result.createClass(FAIR_PROPERTY);

		Property fairOnClassType = result.createProperty(FAIR_ON_CLASS_TYPE);
		Property fairOnPropertyType = result.createProperty(FAIR_ON_PROPERTY_TYPE);
		Property fairHasClass = result.createProperty(FAIR_HAS_CLASS);
		Property fairHasProperty = result.createProperty(FAIR_HAS_PROPERTY);
		Property fairAllowedValues = result.createProperty(FAIR_ALLOWED_VALUES);
		Property fairMinCount = result.createProperty(FAIR_MIN_COUNT);
		Property fairMaxCount = result.createProperty(FAIR_MAX_COUNT);

		Individual molgenisProfile = fairProfile.createIndividual(restApi + "/fair");

		for (EntityMetaData emd : meta.getEntityMetaDatas())
		{
			if (emd.isAbstract())
			{
				continue;
			}
			Individual emdFairClass = result.createIndividual(restApi + '/' + emd.getName() + "/meta/fair", fairClass);
			result.add(molgenisProfile, fairHasClass, emdFairClass);
			OntClass emdEntity = result.createClass(restApi + "/" + emd.getName() + "/meta");
			result.add(emdFairClass, fairOnClassType, emdEntity);
			for (AttributeMetaData amd : emd.getAtomicAttributes())
			{
				Individual amdFairProperty = fairProperty.createIndividual(restApi + '/' + emd.getName()
						+ "/meta/fair/" + amd.getName());
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

				if (dataType instanceof XrefField || dataType instanceof MrefField)
				{
					ObjectProperty amdAttribute = result.createObjectProperty(restApi + "/" + emd.getName() + "/meta/"
							+ amd.getName());
					result.add(amdFairProperty, fairOnPropertyType, amdAttribute);
					OntClass refEntity = result.createClass(restApi + "/" + amd.getRefEntity().getName() + "/meta");
					result.add(amdFairProperty, fairAllowedValues, refEntity);
				}
				else
				{
					DatatypeProperty amdAttribute = result.createDatatypeProperty(restApi + "/" + emd.getName()
							+ "/meta/" + amd.getName());
					result.add(amdFairProperty, fairOnPropertyType, amdAttribute);
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
