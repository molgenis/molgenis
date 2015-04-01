package org.molgenis.rdf;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.model.MolgenisModelException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class RdfService
{
	// NS
	public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String MOLGENIS_NS = "http://www.molgenis.org/2015/04#";
	public static final String OWL_NS = "http://www.w3.org/2002/07/owl#";

	// RDFS
	public static final String RDFS_SUBCLASS_OF = RDFS_NS + "subClassOf";
	public static final String RDFS_MIN_CARDINALITY = RDFS_NS + "minCardinality";
	public static final String RDFS_MAX_CARDINALITY = RDFS_NS + "maxCardinality";

	// RDF
	public static final String RDF_TYPE = RDF_NS + "type";
	public static final String RDF_DATATYPE = RDF_NS + "datatype";

	// OWL
	public static final String OWL_ALL_VALUES_FROM = OWL_NS + "allValuesFrom";

	@Autowired
	private MetaDataService meta;

	public Model getSchema(String apiHref)
	{
		OntModel result = ModelFactory.createOntologyModel();
		result.setNsPrefix("mlg", MOLGENIS_NS);

		ObjectProperty rdfsMinCardinality = result.createObjectProperty(RDFS_MIN_CARDINALITY);
		ObjectProperty rdfsMaxCardinality = result.createObjectProperty(RDFS_MAX_CARDINALITY);
		ObjectProperty owlAllValuesFrom = result.createObjectProperty(OWL_ALL_VALUES_FROM);

		for (EntityMetaData emd : meta.getEntityMetaDatas())
		{
			if (emd.isAbstract())
			{
				continue;
			}
			OntClass emdEntity = result.createClass(apiHref + '/' + emd.getName() + "/meta");
			if (emd.getDescription() != null)
			{
				emdEntity.setComment(emd.getDescription(), null);
			}
			if (emd.getLabel() != null)
			{
				emdEntity.setLabel(emd.getLabel(), null);
			}
			for (AttributeMetaData amd : emd.getAtomicAttributes())
			{
				FieldType dataType = amd.getDataType();
				DatatypeProperty amdAttribute = result.createDatatypeProperty(apiHref + '/' + emd.getName() + "/meta/"
						+ amd.getName());
				if (amd.isNillable())
				{
					result.add(amdAttribute, rdfsMinCardinality, result.createTypedLiteral(0));
				}
				else
				{
					result.add(amdAttribute, rdfsMinCardinality, result.createTypedLiteral(1));
				}

				if (!(dataType instanceof MrefField))
				{
					result.add(amdAttribute, rdfsMaxCardinality, result.createTypedLiteral(1));
				}

				if (dataType instanceof XrefField || dataType instanceof MrefField)
				{
					OntClass refEntity = result.createClass(apiHref + '/' + amd.getRefEntity().getName() + "/meta");
					result.add(amdAttribute, owlAllValuesFrom, refEntity);
				}
				else
				{
					amdAttribute.setRange(ResourceFactory.createResource(getXSDDataType(dataType)));
				}
				if (amd.getDescription() != null)
				{
					amdAttribute.setComment(amd.getDescription(), null);
				}
				if (amd.getLabel() != null)
				{
					amdAttribute.setLabel(amd.getLabel(), null);
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
