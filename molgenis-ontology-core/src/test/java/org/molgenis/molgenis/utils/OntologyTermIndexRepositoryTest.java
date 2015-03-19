package org.molgenis.molgenis.utils;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.collect.Iterables;
import org.molgenis.data.Entity;
import org.molgenis.ontology.repository.AbstractOntologyRepository;
import org.molgenis.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.ontology.utils.OntologyLoader;
import org.molgenis.ontology.utils.ZipFileUtil;
import org.molgenis.util.ResourceUtils;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OntologyTermIndexRepositoryTest
{
	OntologyLoader loader;
	OntologyTermIndexRepository repository;

	@BeforeMethod
	public void setUp() throws OWLOntologyCreationException, FileNotFoundException, IOException
	{
		File file = ResourceUtils.getFile("test-ontology-loader.owl.zip");
		List<File> uploadedFiles = ZipFileUtil.unzip(file);
		loader = new OntologyLoader("ontology-test", uploadedFiles.get(0));
		repository = new OntologyTermIndexRepository(loader, "Test");
	}

	@Test
	public void getAttributes()
	{
		assertEquals(Iterables.size(repository.getEntityMetaData().getAttributes()), 15);
	}

	@Test
	public void iterator() throws OWLOntologyCreationException
	{
		Map<String, Entity> entities = new HashMap<String, Entity>();
		for (Entity entity : repository)
		{
			entities.put(entity.getString(AbstractOntologyRepository.SYNONYMS), entity);
		}

		{
			Entity entity = entities.get("Person label test");
			assertEquals(entity.getString(AbstractOntologyRepository.ONTOLOGY_TERM), "Person label test");
			assertEquals(entity.getString(AbstractOntologyRepository.ENTITY_TYPE), "ontologyTerm");
			assertEquals(entity.getString(AbstractOntologyRepository.ONTOLOGY_TERM_IRI),
					"http://harmonization/test/owl/1.0.0#Person");
		}

		{
			Entity entity = entities.get("Child");
			assertEquals(entity.getString(AbstractOntologyRepository.ONTOLOGY_TERM_IRI),
					"http://harmonization/test/owl/1.0.0#Child");
		}

		{
			Entity entity = entities.get("Strange childhood");
			assertEquals(entity.getString(AbstractOntologyRepository.ONTOLOGY_TERM_IRI),
					"http://harmonization/test/owl/1.0.0#Child");
		}

		{
			Entity entity = entities.get("Daughter");
			assertEquals(entity.getString(AbstractOntologyRepository.ONTOLOGY_TERM_IRI),
					"http://harmonization/test/owl/1.0.0#Daughter");
		}

		{
			Entity entity = entities.get("Son");
			assertEquals(entity.getString(AbstractOntologyRepository.ONTOLOGY_TERM_IRI),
					"http://harmonization/test/owl/1.0.0#Son");
		}

		{
			Entity entity = entities.get("Parent");
			assertEquals(entity.getString(AbstractOntologyRepository.ONTOLOGY_TERM_IRI),
					"http://harmonization/test/owl/1.0.0#Parent");
		}

		{
			Entity entity = entities.get("Dad");
			assertEquals(entity.getString(AbstractOntologyRepository.ONTOLOGY_TERM_IRI),
					"http://harmonization/test/owl/1.0.0#Father");
		}

		{
			Entity entity = entities.get("Mother");
			assertEquals(entity.getString(AbstractOntologyRepository.ONTOLOGY_TERM_IRI),
					"http://harmonization/test/owl/1.0.0#Mother");
		}

		{
			Entity entity = entities.get("Mummy");
			assertEquals(entity.getString(AbstractOntologyRepository.ONTOLOGY_TERM_IRI),
					"http://harmonization/test/owl/1.0.0#Mother");
		}

		{
			Entity entity = entities.get("Papa");
			assertEquals(entity.getString(AbstractOntologyRepository.ONTOLOGY_TERM_IRI),
					"http://harmonization/test/owl/1.0.0#Father");
		}

		{
			Entity entity = entities.get("Dad");
			assertEquals(entity.getString(AbstractOntologyRepository.ONTOLOGY_TERM_IRI),
					"http://harmonization/test/owl/1.0.0#Father");
		}
	}

}
