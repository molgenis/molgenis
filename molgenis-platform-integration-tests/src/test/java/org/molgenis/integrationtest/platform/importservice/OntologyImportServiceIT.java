package org.molgenis.integrationtest.platform.importservice;

import com.google.common.collect.ImmutableMap;
import org.molgenis.auth.User;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.support.FileRepositoryCollection;
import org.springframework.security.test.context.support.WithMockUser;
import org.testng.annotations.Test;

import java.io.File;

import static java.util.Collections.emptySet;
import static org.molgenis.data.DatabaseAction.ADD;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;

public class OntologyImportServiceIT extends ImportServiceIT
{
	private static final String USERNAME = "ontology_user";
	private static final String ROLE_WRITE_ONTOLOGY_TERM_DYNAMIC_ANNOTATION = "ENTITY_WRITE_sys_ont_OntologyTermDynamicAnnotation";
	private static final String ROLE_WRITE_ONTOLOGY_TERM_SYNONYM = "ENTITY_WRITE_sys_ont_OntologyTermSynonym";
	private static final String ROLE_WRITE_ONTOLOGY_TERM_NODE_PATH = "ENTITY_WRITE_sys_ont_OntologyTermNodePath";
	private static final String ROLE_WRITE_ONTOLOGY = "ENTITY_WRITE_sys_ont_Ontology";
	private static final String ROLE_WRITE_ONTOLOGY_TERM = "ENTITY_WRITE_sys_ont_OntologyTerm";

	@Override
	User getTestUser()
	{
		User user = userFactory.create();
		user.setUsername(USERNAME);
		user.setPassword("password");
		user.setEmail("o@mail.com");
		return user;
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE,
			ROLE_WRITE_ONTOLOGY_TERM_DYNAMIC_ANNOTATION, ROLE_WRITE_ONTOLOGY_TERM_SYNONYM,
			ROLE_WRITE_ONTOLOGY_TERM_NODE_PATH, ROLE_WRITE_ONTOLOGY, ROLE_WRITE_ONTOLOGY_TERM })
	@Test
	public void testDoImportOboAsNonSuperuser()
	{
		testDoImportObo();
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void testDoImportOboAsSuperuser()
	{
		testDoImportObo();
	}

	private void testDoImportObo()
	{
		String fileName = "ontology-small.obo.zip";
		File file = getFile("/obo/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap
						.of("sys_ont_OntologyTermDynamicAnnotation", 0, "sys_ont_OntologyTermSynonym", 5,
								"sys_ont_OntologyTermNodePath", 5, "sys_ont_Ontology", 1, "sys_ont_OntologyTerm", 5),
				emptySet());
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_READ_PACKAGE, ROLE_READ_ENTITY_TYPE, ROLE_READ_ATTRIBUTE,
			ROLE_WRITE_ONTOLOGY_TERM_DYNAMIC_ANNOTATION, ROLE_WRITE_ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
			ROLE_WRITE_ONTOLOGY_TERM_SYNONYM, ROLE_WRITE_ONTOLOGY_TERM_NODE_PATH, ROLE_WRITE_ONTOLOGY,
			ROLE_WRITE_ONTOLOGY_TERM })
	@Test
	public void testDoImportOwlAsNonSuperuser()
	{
		testDoImportOwl();
	}

	@WithMockUser(username = USERNAME, roles = { ROLE_SU })
	@Test
	public void testDoImportOwlAsSuperuser()
	{
		testDoImportOwl();
	}

	private void testDoImportOwl()
	{
		String fileName = "ontology-small.owl.zip";
		File file = getFile("/owl/" + fileName);
		FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		ImportService importService = importServiceFactory.getImportService(file, repoCollection);
		EntityImportReport importReport = importService.doImport(repoCollection, ADD, PACKAGE_DEFAULT);
		validateImportReport(importReport, ImmutableMap
						.of("sys_ont_OntologyTermDynamicAnnotation", 4, "sys_ont_OntologyTermSynonym", 9,
								"sys_ont_OntologyTermNodePath", 10, "sys_ont_Ontology", 1, "sys_ont_OntologyTerm", 9),
				emptySet());
	}
}
