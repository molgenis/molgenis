package org.molgenis.integrationtest.platform.importservice;

import com.google.common.collect.ImmutableMap;
import org.molgenis.data.Entity;
import org.molgenis.data.file.support.FileRepositoryCollection;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.EntityTypePermissionUtils;
import org.molgenis.data.security.auth.User;
import org.molgenis.integrationtest.platform.TestPermissionPopulator;
import org.molgenis.ontology.core.meta.Ontology;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.test.context.support.WithMockUser;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static org.molgenis.data.DatabaseAction.ADD;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.molgenis.data.security.EntityTypePermission.WRITE;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class OntologyImportServiceIT extends ImportServiceIT
{
	private static final String USERNAME = "ontology_user";

	@Override
	User getTestUser()
	{
		User user = userFactory.create();
		user.setUsername(USERNAME);
		user.setPassword("password");
		user.setEmail("o@mail.com");
		return user;
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testDoImportOboAsNonSuperuser()
	{
		populateUserPermissions();
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
		validateImportReport(importReport,
				ImmutableMap.of("sys_ont_OntologyTermDynamicAnnotation", 0, "sys_ont_OntologyTermSynonym", 5,
						"sys_ont_OntologyTermNodePath", 5, "sys_ont_Ontology", 1, "sys_ont_OntologyTerm", 5),
				emptySet());

		// Verify the import as system as we need write permissions on sys tables to carry out the verification
		runAsSystem(this::verifyOboAsSystem);
	}

	private void verifyOboAsSystem()
	{
		List<Entity> ontologies = dataService.findAll("sys_ont_Ontology").collect(Collectors.toList());
		Ontology ontology = (Ontology) ontologies.get(0);
		assertEquals(ontology.getOntologyName(), "ontology-small");

		List<Entity> synonyms = dataService.findAll("sys_ont_OntologyTerm").collect(Collectors.toList());

		verifyOboRow(synonyms, "molgenis ontology core", "http://purl.obolibrary.org/obo/TEMP#molgenis-ontology-core");

		verifyOboRow(synonyms, "molgenis", "http://purl.obolibrary.org/obo/TEMP#molgenis");

		verifyOboRow(synonyms, "Thing", "http://purl.obolibrary.org/obo/TEMP#Thing");
	}

	private void verifyOboRow(List<Entity> synonyms, String ontologyTermName, String ontologyTermIRI)
	{
		Optional<Entity> molOntCoreOpt = synonyms.stream()
												 .filter(s -> s.getString("ontologyTermName").equals(ontologyTermName))
												 .findFirst();
		assertTrue(molOntCoreOpt.isPresent());
		assertEquals(molOntCoreOpt.get().getString("ontologyTermIRI"), ontologyTermIRI);
	}

	@WithMockUser(username = USERNAME)
	@Test
	public void testDoImportOwlAsNonSuperuser()
	{
		populateUserPermissions();
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
		validateImportReport(importReport,
				ImmutableMap.of("sys_ont_OntologyTermDynamicAnnotation", 4, "sys_ont_OntologyTermSynonym", 9,
						"sys_ont_OntologyTermNodePath", 10, "sys_ont_Ontology", 1, "sys_ont_OntologyTerm", 9),
				emptySet());

		// Verify the import as system as we need write permissions on sys tables to carry out the verification
		runAsSystem(this::verifyOwlAsSystem);
	}

	private void verifyOwlAsSystem()
	{
		// Verify two imported rows (organization and team, as these are interesting examples)
		List<Entity> entities = dataService.findAll("sys_ont_OntologyTerm").collect(Collectors.toList());
		Optional<Entity> organizationOpt = entities.stream()
												   .filter(e -> e.getString("ontologyTermName").equals("organization"))
												   .findFirst();
		assertTrue(organizationOpt.isPresent());
		Entity organization = organizationOpt.get();

		Optional<Entity> teamOpt = entities.stream()
										   .filter(e -> e.getString("ontologyTermName").equals("team"))
										   .findFirst();
		assertTrue(teamOpt.isPresent());
		Entity team = teamOpt.get();

		// Verify organization
		assertEquals(organization.getString("ontologyTermIRI"), "http://www.molgenis.org#Organization");
		assertEquals(organization.getString("ontologyTermName"), "organization");

		// verify organization ontologyTermSynonym
		Iterable<Entity> ontologyTermSynonym = organization.getEntities("ontologyTermSynonym");
		List<Entity> termSynonymRefList = new ArrayList<>();
		ontologyTermSynonym.forEach(termSynonymRefList::add);
		assertEquals(termSynonymRefList.size(), 1);
		Entity organizationOntologyTermSynonym = dataService.findOneById("sys_ont_OntologyTermSynonym",
				termSynonymRefList.get(0).getIdValue());
		assertEquals(organizationOntologyTermSynonym.getString("ontologyTermSynonym"), "organization");

		// verify organization ontology
		Ontology ontology = (Ontology) organization.get("ontology");
		assertEquals(ontology.getOntologyName(), "ontology-small");

		// Verify the team row
		assertEquals(team.getString("ontologyTermIRI"), "http://www.molgenis.org#Team");
		assertEquals(team.getString("ontologyTermName"), "team");

		// verify team dynamic annotations
		Iterable<Entity> dynamicAnnotationItr = team.getEntities("ontologyTermDynamicAnnotation");
		List<Entity> dynamicAnnotations = new ArrayList<>();
		dynamicAnnotationItr.forEach(dynamicAnnotations::add);
		assertEquals(dynamicAnnotations.size(), 2);
		Entity annotationOne = dataService.findOneById("sys_ont_OntologyTermDynamicAnnotation",
				dynamicAnnotations.get(0).getIdValue());
		assertEquals(annotationOne.getString("label"), "friday:2412423");
		Entity annotationTwo = dataService.findOneById("sys_ont_OntologyTermDynamicAnnotation",
				dynamicAnnotations.get(1).getIdValue());
		assertEquals(annotationTwo.getString("label"), "molgenis:1231424");

		// verify team ontology
		ontology = (Ontology) team.get("ontology");
		assertEquals(ontology.getOntologyName(), "ontology-small");
	}

	@Autowired
	private TestPermissionPopulator testPermissionPopulator;

	private void populateUserPermissions()
	{
		CumulativePermission readEntityType = EntityTypePermissionUtils.getCumulativePermission(
				EntityTypePermission.READ);
		CumulativePermission writeEntityType = EntityTypePermissionUtils.getCumulativePermission(WRITE);

		Map<ObjectIdentity, org.springframework.security.acls.model.Permission> permissionMap = new HashMap<>();
		permissionMap.put(new EntityTypeIdentity("sys_ont_OntologyTermDynamicAnnotation"), writeEntityType);
		permissionMap.put(new EntityTypeIdentity("sys_ont_OntologyTermNodePath"), writeEntityType);
		permissionMap.put(new EntityTypeIdentity("sys_ont_OntologyTermSynonym"), writeEntityType);
		permissionMap.put(new EntityTypeIdentity("sys_ont_Ontology"), writeEntityType);
		permissionMap.put(new EntityTypeIdentity("sys_ont_OntologyTerm"), writeEntityType);
		permissionMap.put(new EntityTypeIdentity("sys_dec_DecoratorConfiguration"), readEntityType);

		testPermissionPopulator.populate(permissionMap, SecurityUtils.getCurrentUsername());
	}
}
