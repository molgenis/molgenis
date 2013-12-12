package org.molgenis.omx.biobankconnect.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.molgenis.data.Entity;
import org.molgenis.data.csv.CsvRepository;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class OntologyLoaderApp
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
			CsvRepository repo = null;
			try
			{
				repo = new CsvRepository((new File(variableList)), null);
				for (Entity entity : repo)
				{
					terms.add(entity.getString("name"));
				}
			}
			finally
			{
				if (repo != null) repo.close();
			}
			File outputFile = new File(file.getAbsoluteFile().getParent() + "/" + file.getName() + "_subset.owl");
			manager.copyOntologyContent(terms);
			manager.saveOntology(outputFile);
			System.out.println("finish loading");
		}
	}
}
