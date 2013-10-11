package org.molgenis.omx.biobankconnect.mesh;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.molgenis.omx.biobankconnect.utils.OntologyCreator;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class MeshLoaderApp
{

	private static final String PREFIX = "http://www.biobankconnect.org/mesh";
	private static final String FULL_SYN = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#FULL_SYN";

	/**
	 * @param args
	 * @throws OWLOntologyCreationException
	 * @throws JAXBException
	 * @throws OWLOntologyStorageException
	 */
	public static void main(String[] args) throws OWLOntologyCreationException, JAXBException,
			OWLOntologyStorageException
	{
		if (args.length > 1)
		{
			MeshLoader loader = new MeshLoader(new File(args[0]));
			MeshTerm topNode = loader.getTopNode();
			OntologyCreator creator = new OntologyCreator(PREFIX);
			for (String path : Arrays.asList("J02", "G07.610.240"))
			{
				recursiveVisit(topNode.get(path), creator);
			}
			creator.saveOntology(new File(args[1]));
		}
	}

	public static void recursiveVisit(MeshTerm parentNode, OntologyCreator creator)
	{
		if (parentNode.hasChildren())
		{
			OWLClass parentClass = null;
			if (parentNode.hasParent())
			{
				Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
				String conceptName = parentNode.getLabel().replaceAll("[^a-zA-Z0-9]", "_");
				parentClass = creator.createOWLClass(PREFIX + "#" + conceptName);
				annotations.add(creator.createLabelAnnotation(parentNode.getLabel()));
				for (String synonym : parentNode.getSynonyms())
				{
					annotations.add(creator.createAnnotation(synonym, FULL_SYN));
				}
				if (parentNode.getDefinition() != null) annotations.add(creator.createDefinitionAnnotation(parentNode
						.getDefinition()));
				creator.addAnnotations(parentClass, annotations);
			}
			for (MeshTerm subNode : parentNode.getChildren())
			{

				String conceptName = subNode.getLabel().replaceAll("[^a-zA-Z0-9]", "_");
				OWLClass cls = creator.createOWLClass(PREFIX + "#" + conceptName);
				creator.addClass(cls, parentClass);

				Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
				annotations.add(creator.createLabelAnnotation(subNode.getLabel()));
				for (String synonym : subNode.getSynonyms())
				{
					annotations.add(creator.createAnnotation(synonym, FULL_SYN));
				}
				if (subNode.getDefinition() != null) annotations.add(creator.createDefinitionAnnotation(subNode
						.getDefinition()));
				creator.addAnnotations(cls, annotations);

				recursiveVisit(subNode, creator);
			}
		}
	}
}
