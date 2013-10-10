package org.molgenis.omx.biobankconnect.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.molgenis.io.csv.CsvReader;
import org.molgenis.util.tuple.Tuple;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class OntologyLoaderMain
{
	public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException, IOException,
			OWLOntologyStorageException, JAXBException
	{
		if (args.length > 1)
		{
			String inputFileName = args[0];
			String variableList = args[1];
			File file = new File(inputFileName);
			System.out.println("start loading");
			OntologyManager manager = new OntologyManager();
			manager.loadExistingOntology(file);

			Set<String> terms = new HashSet<String>();
			CsvReader reader = new CsvReader(new File(variableList));
			Iterator<Tuple> iterator = reader.iterator();
			while (iterator.hasNext())
			{
				terms.add(iterator.next().getString("name"));
			}
			reader.close();
			File outputFile = new File(file.getAbsoluteFile().getParent() + "/" + file.getName() + "_subset.owl");
			manager.copyOntologyContent(terms);
			manager.saveOntology(outputFile);
			System.out.println("finish loading");
		}
	}
}
