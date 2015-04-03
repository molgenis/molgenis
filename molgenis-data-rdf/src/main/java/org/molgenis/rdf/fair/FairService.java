package org.molgenis.rdf.fair;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.model.MolgenisModelException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class FairService
{
	// Namespaces
	public static final String FAIR_NS = "http://datafairport.org/ontology/FAIR-schema.owl#";
	public static final String SKOS_NS = "http://www.w3.org/2004/02/skos/core#";

	// SKOS Property IRIs
	public static final String SKOS_PREFERRED_LABEL = SKOS_NS + "preferredLabel";

	// FAIR Class IRIs
	public static final String FAIR_PROFILE = FAIR_NS + "FAIRProfile";
	public static final String FAIR_CLASS = FAIR_NS + "FAIRClass";
	public static final String FAIR_PROPERTY = FAIR_NS + "FAIRProperty";

	// FAIR Property IRIs
	public static final String FAIR_DESCRIBES_USE_OF = FAIR_NS + "describesUseOf";
	public static final String FAIR_HAS_CLASS = FAIR_NS + "hasClass";
	public static final String FAIR_HAS_PROPERTY = FAIR_NS + "hasProperty";
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

		AnnotationProperty skosPreferredLabel = result.createAnnotationProperty(SKOS_PREFERRED_LABEL);
		ObjectProperty fairDescribesUseOf = result.createObjectProperty(FAIR_DESCRIBES_USE_OF);
		ObjectProperty fairHasClass = result.createObjectProperty(FAIR_HAS_CLASS);
		ObjectProperty fairHasProperty = result.createObjectProperty(FAIR_HAS_PROPERTY);
		DatatypeProperty fairAllowedValues = result.createDatatypeProperty(FAIR_ALLOWED_VALUES);
		DatatypeProperty fairMinCount = result.createDatatypeProperty(FAIR_MIN_COUNT);
		DatatypeProperty fairMaxCount = result.createDatatypeProperty(FAIR_MAX_COUNT);

		Individual molgenisProfile = fairProfile.createIndividual(restApi + "/fair");

		for (EntityMetaData emd : meta.getEntityMetaDatas())
		{
			if (emd.isAbstract())
			{
				continue;
			}
			Individual emdFairClass = result.createIndividual(restApi + '/' + emd.getName() + "/meta/fair", fairClass);
			result.add(emdFairClass, skosPreferredLabel, result.createLiteral(emd.getLabel(), null));
			result.add(molgenisProfile, fairHasClass, emdFairClass);
			OntClass emdEntity = result.createClass(restApi + "/" + emd.getName() + "/meta");
			result.add(emdFairClass, fairDescribesUseOf, emdEntity);
			for (AttributeMetaData amd : emd.getAtomicAttributes())
			{
				Individual amdFairProperty = fairProperty.createIndividual(restApi + '/' + emd.getName()
						+ "/meta/fair/" + amd.getName());
				FieldType dataType = amd.getDataType();
				result.add(emdFairClass, fairHasProperty, amdFairProperty);
				result.add(amdFairProperty, skosPreferredLabel, result.createLiteral(amd.getLabel(), null));

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
					result.add(amdFairProperty, fairDescribesUseOf, amdAttribute);
					OntClass refEntity = result.createClass(restApi + "/" + amd.getRefEntity().getName() + "/meta");
					result.add(amdFairProperty, fairAllowedValues, refEntity);
				}
				else
				{
					DatatypeProperty amdAttribute = result.createDatatypeProperty(restApi + "/" + emd.getName()
							+ "/meta/" + amd.getName());
					result.add(amdFairProperty, fairDescribesUseOf, amdAttribute);
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
