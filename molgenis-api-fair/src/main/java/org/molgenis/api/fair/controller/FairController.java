package org.molgenis.api.fair.controller;

import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.converter.RDFMediaType.TEXT_TURTLE_VALUE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.molgenis.api.ApiNamespace;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Serves metadata for the molgenis FAIR DataPoint. */
@Controller
@RequestMapping(FairController.BASE_URI)
public class FairController {
  private static final Logger LOG = LoggerFactory.getLogger(FairController.class);

  static final String BASE_URI = ApiNamespace.API_PATH + "/fdp";

  private final DataService dataService;
  private final MetaDataService metaDataService;
  private final EntityModelWriter entityModelWriter;

  FairController(
      DataService dataService,
      MetaDataService metaDataService,
      EntityModelWriter entityModelWriter) {
    this.dataService = requireNonNull(dataService);
    this.metaDataService = requireNonNull(metaDataService);
    this.entityModelWriter = requireNonNull(entityModelWriter);
  }

  @GetMapping(produces = TEXT_TURTLE_VALUE)
  @ResponseBody
  @RunAsSystem
  public Model getMetadata() {
    Entity subjectEntity = dataService.findOne("fdp_Metadata", new QueryImpl<>());
    return entityModelWriter.createRdfModel(subjectEntity);
  }

  @GetMapping(produces = TEXT_TURTLE_VALUE, value = "/{entityType}/{entity}")
  @ResponseBody
  @RunAsSystem
  public Model getResource(
      @PathVariable("entityType") String entityType, @PathVariable("entity") String entity) {
    var type = metaDataService.getEntityType(entityType).filter(entityModelWriter::isADcatResource);
    if (!type.isPresent()) {
      throw new IllegalArgumentException("Entitytype is not a resource");
    }
    Entity subjectEntity = dataService.findOneById(entityType, entity);
    if (subjectEntity == null) {
      throw new UnknownEntityException(entityType, entity);
    }
    return entityModelWriter.createRdfModel(subjectEntity);
  }

  @ExceptionHandler(UnknownEntityException.class)
  @ResponseBody
  @ResponseStatus(BAD_REQUEST)
  public Model handleUnknownEntityException(UnknownEntityException e) {
    LOG.warn(e.getMessage(), e);
    return new LinkedHashModel();
  }
}
