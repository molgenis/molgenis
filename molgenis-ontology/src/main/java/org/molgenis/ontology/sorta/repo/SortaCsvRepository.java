package org.molgenis.ontology.sorta.repo;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.molgenis.data.meta.model.EntityType.AttributeCopyMode.DEEP_COPY_ATTRS;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.file.processor.CellProcessor;
import org.molgenis.data.file.processor.LowerCaseProcessor;
import org.molgenis.data.file.processor.TrimProcessor;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.ontology.sorta.service.impl.SortaServiceImpl;

public class SortaCsvRepository extends AbstractRepository {
  private EntityType entityType = null;
  private final CsvRepository csvRepository;
  private final String entityTypeId;
  private final String entityLabel;
  public static final String ALLOWED_IDENTIFIER = "Identifier";
  private static final List<CellProcessor> LOWERCASE_AND_TRIM =
      Arrays.asList(new LowerCaseProcessor(), new TrimProcessor());

  public SortaCsvRepository(
      File file, EntityTypeFactory entityTypeFactory, AttributeFactory attrMetaFactory) {
    this.csvRepository =
        new CsvRepository(
            file,
            entityTypeFactory,
            attrMetaFactory,
            LOWERCASE_AND_TRIM,
            SortaServiceImpl.DEFAULT_SEPARATOR);
    this.entityTypeId = file.getName();
    this.entityLabel = file.getName();
  }

  public SortaCsvRepository(
      String entityTypeId,
      String entityLabel,
      File uploadedFile,
      EntityTypeFactory entityTypeFactory,
      AttributeFactory attrMetaFactory) {
    this.csvRepository =
        new CsvRepository(
            uploadedFile,
            entityTypeFactory,
            attrMetaFactory,
            LOWERCASE_AND_TRIM,
            SortaServiceImpl.DEFAULT_SEPARATOR);
    this.entityTypeId = entityTypeId;
    this.entityLabel = entityLabel;
  }

  public EntityType getEntityType() {
    if (entityType == null) {
      AttributeFactory attrMetaFactory =
          getApplicationContext()
              .getBean(AttributeFactory.class); // FIXME do not use application context

      entityType =
          EntityType.newInstance(csvRepository.getEntityType(), DEEP_COPY_ATTRS, attrMetaFactory);
      entityType.setId(entityTypeId);
      entityType.setLabel(entityLabel);
      entityType.setBackend("PostgreSQL"); // FIXME do not hardcode backend name
      entityType.addAttribute(
          attrMetaFactory.create().setName(ALLOWED_IDENTIFIER).setNillable(false), ROLE_ID);
      Attribute nameAttribute =
          entityType.getAttribute(SortaServiceImpl.DEFAULT_MATCHING_NAME_FIELD);
      if (nameAttribute != null) {
        nameAttribute.setLabelAttribute(true);
        nameAttribute.setNillable(false);
      }
    }
    return entityType;
  }

  @Override
  public Iterator<Entity> iterator() {
    final AtomicInteger count = new AtomicInteger(0);
    final Iterator<Entity> iterator = csvRepository.iterator();
    return new Iterator<Entity>() {
      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public Entity next() {
        Entity entity = iterator.next();
        if (isEmpty(entity.getString(ALLOWED_IDENTIFIER))) {
          DynamicEntity dynamicEntity = new DynamicEntity(getEntityType());
          dynamicEntity.set(entity);
          entity = dynamicEntity;
          entity.set(ALLOWED_IDENTIFIER, String.valueOf(count.incrementAndGet()));
        }
        return entity;
      }
    };
  }

  @Override
  public Set<RepositoryCapability> getCapabilities() {
    return Collections.emptySet();
  }

  @Override
  public long count() {
    return csvRepository.count();
  }
}
