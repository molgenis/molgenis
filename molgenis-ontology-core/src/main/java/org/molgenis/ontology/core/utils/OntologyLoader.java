package org.molgenis.ontology.core.utils;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class OntologyLoader {
  private static String DB_ID_PATTERN = "(\\w*):(\\d*)";
  private String ontologyIRI = null;
  private String ontologyName = null;
  private File ontologyFile = null;
  private OWLDataFactory factory = null;
  private OWLOntology ontology = null;
  private OWLOntologyManager manager = null;
  private Set<String> synonymsProperties;

  {
    synonymsProperties =
        new HashSet<>(
            Arrays.asList(
                "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#FULL_SYN",
                "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P90",
                "http://www.geneontology.org/formats/oboInOwl#hasExactSynonym",
                "http://www.ebi.ac.uk/efo/alternative_term"));
  }

  private Set<String> owlObjectProperties;

  {
    owlObjectProperties =
        new HashSet<>(
            Arrays.asList("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#is_associated_with"));
  }

  private Set<String> ontologyTermDefinitions;

  {
    ontologyTermDefinitions =
        new HashSet<>(
            Arrays.asList(
                "http://purl.obolibrary.org/obo/",
                "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#DEFINITION"));
  }

  private Map<String, OWLClass> hashToRetrieveClass = new HashMap<>();

  public OntologyLoader(String ontologyName, File ontologyFile)
      throws OWLOntologyCreationException {
    this.ontologyFile = ontologyFile;
    this.manager = OWLManager.createOWLOntologyManager();
    this.factory = manager.getOWLDataFactory();
    this.ontologyName = ontologyName;
    this.ontology = loadOwlOntology();
    this.ontologyIRI = ontology.getOntologyID().getOntologyIRI().toString();
  }

  private OWLOntology loadOwlOntology() throws OWLOntologyCreationException {
    FileDocumentSource fileDocumentSource = new FileDocumentSource(ontologyFile);
    // use silent import handling strategy to enable ontology loading without internet access
    // see: https://github.com/molgenis/molgenis/issues/5301
    OWLOntologyLoaderConfiguration owlOntologyLoaderConfiguration =
        new OWLOntologyLoaderConfiguration()
            .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
    return manager.loadOntologyFromOntologyDocument(
        fileDocumentSource, owlOntologyLoaderConfiguration);
  }

  public Set<OWLClass> getRootClasses() {
    Set<OWLClass> listOfTopClasses = new HashSet<>();
    for (OWLClass cls : ontology.getClassesInSignature()) {
      if (ontology.getSubClassAxiomsForSubClass(cls).isEmpty()
          && ontology.getEquivalentClassesAxioms(cls).isEmpty()) listOfTopClasses.add(cls);
    }
    return listOfTopClasses;
  }

  public OWLClass getTopClass() {
    return factory.getOWLThing();
  }

  public Set<OWLClass> getChildClass(OWLClass cls) {
    Set<OWLClass> listOfClasses = new HashSet<>();
    for (OWLSubClassOfAxiom axiom : ontology.getSubClassAxiomsForSuperClass(cls)) {
      OWLClassExpression expression = axiom.getSubClass();
      if (!expression.isAnonymous()) {
        OWLClass asOWLClass = expression.asOWLClass();
        listOfClasses.add(asOWLClass);
      }
    }
    return listOfClasses;
  }

  private boolean ifExistsAnnotation(String propertyUrl, String keyword) {
    String pattern = "[\\W_]*" + keyword + "[\\W_]*";
    // Use # as the separator
    String[] urlFragments = propertyUrl.split("[#/]");
    if (urlFragments.length > 1) {
      String label = urlFragments[urlFragments.length - 1].replaceAll("[\\W]", "_");
      for (String token : label.split("_")) {
        if (token.matches(pattern)) return true;
      }
    }
    return false;
  }

  public Set<String> getSynonyms(OWLClass cls) {
    Set<String> listOfSynonyms = new HashSet<>();
    for (String eachSynonymProperty : synonymsProperties) {
      listOfSynonyms.addAll(getAnnotation(cls, eachSynonymProperty));
    }
    listOfSynonyms.add(getLabel(cls));
    return listOfSynonyms;
  }

  public String getLabel(OWLEntity entity) {
    Set<String> annotation = getAnnotation(entity, OWLRDFVocabulary.RDFS_LABEL.toString());
    if (!annotation.isEmpty()) {
      return annotation.iterator().next();
    } else {
      return extractOWLClassId(entity);
    }
  }

  private Set<String> getAnnotation(OWLEntity entity, String property) {
    Set<String> annotations = new HashSet<>();
    try {
      OWLAnnotationProperty owlAnnotationProperty =
          factory.getOWLAnnotationProperty(IRI.create(property));
      for (OWLAnnotation annotation : entity.getAnnotations(ontology, owlAnnotationProperty)) {
        if (annotation.getValue() instanceof OWLLiteral) {
          OWLLiteral val = (OWLLiteral) annotation.getValue();
          annotations.add(val.getLiteral());
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to get label for OWLClass " + entity);
    }
    return annotations;
  }

  // TODO : FIXME replace the getAllDatabaseIds later on
  public Set<String> getDatabaseIds(OWLClass entity) {
    Set<String> dbAnnotations = new HashSet<>();
    for (OWLAnnotation annotation : entity.getAnnotations(ontology)) {
      if (annotation.getValue() instanceof OWLLiteral) {
        OWLLiteral val = (OWLLiteral) annotation.getValue();
        String value = val.getLiteral();
        if (value.matches(DB_ID_PATTERN)) {
          dbAnnotations.add(value);
        }
      }
    }
    return dbAnnotations;
  }

  public String getOntologyLabel() {
    OWLAnnotationProperty labelProperty =
        factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
    String ontologyLabel = StringUtils.EMPTY;
    for (OWLAnnotation annotation : ontology.getAnnotations()) {
      if (annotation.getProperty().equals(labelProperty)
          && annotation.getValue() instanceof OWLLiteral) {
        OWLLiteral val = (OWLLiteral) annotation.getValue();
        ontologyLabel = val.getLiteral();
      }
    }
    return ontologyLabel;
  }

  public String extractOWLClassId(OWLEntity cls) {
    StringBuilder stringBuilder = new StringBuilder();
    String clsIri = cls.getIRI().toString();
    // Case where id is separated by #
    String[] split = null;
    if (clsIri.contains("#")) {
      split = clsIri.split("#");
    } else {
      split = clsIri.split("/");
    }
    stringBuilder.append(split[split.length - 1]);
    return stringBuilder.toString();
  }

  public String getOntologyIRI() {
    return ontologyIRI;
  }

  public String getOntologyName() {
    return ontologyName;
  }

  public String getOntologyFilePath() {
    return ontologyFile.getAbsolutePath();
  }

  public Map<String, OWLClass> getHashToRetrieveClass() {
    return hashToRetrieveClass;
  }

  public OWLClass createClass(String iri, Set<OWLClass> rootClasses) {
    OWLClass owlClass = factory.getOWLClass(IRI.create(iri));
    for (OWLClass rootClass : rootClasses) {
      if (rootClass != owlClass) addClass(rootClass, owlClass);
    }
    return owlClass;
  }

  public void addClass(OWLClass cls, OWLClass parentClass) {
    if (parentClass == null) parentClass = factory.getOWLThing();
    manager.applyChange(new AddAxiom(ontology, factory.getOWLSubClassOfAxiom(cls, parentClass)));
  }

  public Set<OWLClass> getAllclasses() {
    return ontology.getClassesInSignature();
  }
}
