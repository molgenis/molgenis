package org.molgenis.integrationtest.platform.importservice;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.data.DataAction.ADD;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.molgenis.data.Entity;
import org.molgenis.data.file.support.FileRepositoryCollection;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.MetadataAction;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.permission.PermissionService;
import org.molgenis.data.security.permission.model.Permission;
import org.molgenis.ontology.core.meta.Ontology;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.SidUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.test.context.support.WithMockUser;

class OntologyImportServiceIT extends ImportServiceIT {
  private static final String USERNAME = "ontology_user";

  @Override
  User getTestUser() {
    User user = userFactory.create();
    user.setUsername(USERNAME);
    user.setPassword("password");
    user.setEmail("o@mail.com");
    return user;
  }

  @WithMockUser(username = USERNAME)
  @Test
  void testDoImportOboAsNonSuperuser() {
    populateUserPermissions();
    testDoImportObo();
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SU})
  @Test
  void testDoImportOboAsSuperuser() {
    testDoImportObo();
  }

  private void testDoImportObo() {
    String fileName = "ontology-small.obo.zip";
    File file = getFile("/obo/" + fileName);
    FileRepositoryCollection repoCollection =
        fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
    ImportService importService = importServiceFactory.getImportService(file, repoCollection);
    EntityImportReport importReport =
        importService.doImport(repoCollection, MetadataAction.IGNORE, ADD, null);
    validateImportReport(
        importReport,
        ImmutableMap.of(
            "sys_ont_OntologyTermDynamicAnnotation",
            0,
            "sys_ont_OntologyTermSynonym",
            5,
            "sys_ont_OntologyTermNodePath",
            5,
            "sys_ont_Ontology",
            1,
            "sys_ont_OntologyTerm",
            5),
        emptySet());

    // Verify the import as system as we need write permissions on sys tables to carry out the
    // verification
    runAsSystem(this::verifyOboAsSystem);
  }

  private void verifyOboAsSystem() {
    List<Entity> ontologies = dataService.findAll("sys_ont_Ontology").collect(Collectors.toList());
    Ontology ontology = (Ontology) ontologies.get(0);
    assertEquals("ontology-small", ontology.getOntologyName());

    List<Entity> synonyms =
        dataService.findAll("sys_ont_OntologyTerm").collect(Collectors.toList());

    verifyOboRow(
        synonyms,
        "molgenis ontology core",
        "http://purl.obolibrary.org/obo/TEMP#molgenis-ontology-core");

    verifyOboRow(synonyms, "molgenis", "http://purl.obolibrary.org/obo/TEMP#molgenis");

    verifyOboRow(synonyms, "Thing", "http://purl.obolibrary.org/obo/TEMP#Thing");
  }

  private void verifyOboRow(
      List<Entity> synonyms, String ontologyTermName, String ontologyTermIRI) {
    Optional<Entity> molOntCoreOpt =
        synonyms.stream()
            .filter(s -> s.getString("ontologyTermName").equals(ontologyTermName))
            .findFirst();
    assertTrue(molOntCoreOpt.isPresent());
    assertEquals(ontologyTermIRI, molOntCoreOpt.get().getString("ontologyTermIRI"));
  }

  @WithMockUser(username = USERNAME)
  @Test
  void testDoImportOwlAsNonSuperuser() {
    populateUserPermissions();
    testDoImportOwl();
  }

  @WithMockUser(
      username = USERNAME,
      roles = {ROLE_SU})
  @Test
  void testDoImportOwlAsSuperuser() {
    testDoImportOwl();
  }

  private void testDoImportOwl() {
    String fileName = "ontology-small.owl.zip";
    File file = getFile("/owl/" + fileName);
    FileRepositoryCollection repoCollection =
        fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
    ImportService importService = importServiceFactory.getImportService(file, repoCollection);
    EntityImportReport importReport =
        importService.doImport(repoCollection, MetadataAction.IGNORE, ADD, null);
    validateImportReport(
        importReport,
        ImmutableMap.of(
            "sys_ont_OntologyTermDynamicAnnotation",
            4,
            "sys_ont_OntologyTermSynonym",
            9,
            "sys_ont_OntologyTermNodePath",
            10,
            "sys_ont_Ontology",
            1,
            "sys_ont_OntologyTerm",
            9),
        emptySet());

    // Verify the import as system as we need write permissions on sys tables to carry out the
    // verification
    runAsSystem(this::verifyOwlAsSystem);
  }

  private void verifyOwlAsSystem() {
    // Verify two imported rows (organization and team, as these are interesting examples)
    List<Entity> entities =
        dataService.findAll("sys_ont_OntologyTerm").collect(Collectors.toList());
    Optional<Entity> organizationOpt =
        entities.stream()
            .filter(e -> e.getString("ontologyTermName").equals("organization"))
            .findFirst();
    assertTrue(organizationOpt.isPresent());
    Entity organization = organizationOpt.get();

    Optional<Entity> teamOpt =
        entities.stream().filter(e -> e.getString("ontologyTermName").equals("team")).findFirst();
    assertTrue(teamOpt.isPresent());
    Entity team = teamOpt.get();

    // Verify organization
    assertEquals("http://www.molgenis.org#Organization", organization.getString("ontologyTermIRI"));
    assertEquals("organization", organization.getString("ontologyTermName"));

    // verify organization ontologyTermSynonym
    Iterable<Entity> ontologyTermSynonym = organization.getEntities("ontologyTermSynonym");
    List<Entity> termSynonymRefList = new ArrayList<>();
    ontologyTermSynonym.forEach(termSynonymRefList::add);
    assertEquals(1, termSynonymRefList.size());
    Entity organizationOntologyTermSynonym =
        dataService.findOneById(
            "sys_ont_OntologyTermSynonym", termSynonymRefList.get(0).getIdValue());
    assertEquals("organization", organizationOntologyTermSynonym.getString("ontologyTermSynonym"));

    // verify organization ontology
    Ontology ontology = (Ontology) organization.get("ontology");
    assertEquals("ontology-small", ontology.getOntologyName());

    // Verify the team row
    assertEquals("http://www.molgenis.org#Team", team.getString("ontologyTermIRI"));
    assertEquals("team", team.getString("ontologyTermName"));

    // verify team dynamic annotations
    Iterable<Entity> dynamicAnnotationItr = team.getEntities("ontologyTermDynamicAnnotation");
    List<Entity> dynamicAnnotations = new ArrayList<>();
    dynamicAnnotationItr.forEach(dynamicAnnotations::add);
    assertEquals(2, dynamicAnnotations.size());
    Entity annotationOne =
        dataService.findOneById(
            "sys_ont_OntologyTermDynamicAnnotation", dynamicAnnotations.get(0).getIdValue());
    assertEquals("friday:2412423", annotationOne.getString("label"));
    Entity annotationTwo =
        dataService.findOneById(
            "sys_ont_OntologyTermDynamicAnnotation", dynamicAnnotations.get(1).getIdValue());
    assertEquals("molgenis:1231424", annotationTwo.getString("label"));

    // verify team ontology
    ontology = (Ontology) team.get("ontology");
    assertEquals("ontology-small", ontology.getOntologyName());
  }

  @Autowired private PermissionService testPermissionService;

  private void populateUserPermissions() {
    Map<ObjectIdentity, PermissionSet> permissionMap = new HashMap<>();
    permissionMap.put(
        new EntityTypeIdentity("sys_ont_OntologyTermDynamicAnnotation"), PermissionSet.WRITE);
    permissionMap.put(new EntityTypeIdentity("sys_ont_OntologyTermNodePath"), PermissionSet.WRITE);
    permissionMap.put(new EntityTypeIdentity("sys_ont_OntologyTermSynonym"), PermissionSet.WRITE);
    permissionMap.put(new EntityTypeIdentity("sys_ont_Ontology"), PermissionSet.WRITE);
    permissionMap.put(new EntityTypeIdentity("sys_ont_OntologyTerm"), PermissionSet.WRITE);
    permissionMap.put(new EntityTypeIdentity("sys_dec_DecoratorConfiguration"), PermissionSet.READ);

    Sid sid = SidUtils.createUserSid(getCurrentUsername());
    for (Entry<ObjectIdentity, PermissionSet> entry : permissionMap.entrySet()) {
      runAsSystem(
          () -> {
            testPermissionService.createPermission(
                Permission.create(entry.getKey(), sid, entry.getValue()));
          });
    }
  }
}
