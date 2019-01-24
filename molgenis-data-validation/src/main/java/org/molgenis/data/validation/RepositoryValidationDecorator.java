package org.molgenis.data.validation;

import static com.google.common.collect.Streams.stream;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.RepositoryCapability.VALIDATE_NOTNULL_CONSTRAINT;
import static org.molgenis.data.RepositoryCapability.VALIDATE_READONLY_CONSTRAINT;
import static org.molgenis.data.RepositoryCapability.VALIDATE_REFERENCE_CONSTRAINT;
import static org.molgenis.data.RepositoryCapability.VALIDATE_UNIQUE_CONSTRAINT;
import static org.molgenis.data.util.EntityTypeUtils.isMultipleReferenceType;
import static org.molgenis.data.util.EntityTypeUtils.isReferenceType;
import static org.molgenis.data.util.EntityTypeUtils.isSingleReferenceType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.HugeMap;
import org.molgenis.util.HugeSet;
import org.molgenis.validation.ConstraintViolation;

public class RepositoryValidationDecorator extends AbstractRepositoryDecorator<Entity> {
  private enum ValidationMode {
    ADD,
    UPDATE
  }

  private final DataService dataService;
  private final EntityAttributesValidator entityAttributesValidator;
  private final DefaultValueReferenceValidator defaultValueReferenceValidator;

  public RepositoryValidationDecorator(
      DataService dataService,
      Repository<Entity> delegateRepository,
      EntityAttributesValidator entityAttributesValidator,
      DefaultValueReferenceValidator defaultValueReferenceValidator) {
    super(delegateRepository);
    this.dataService = requireNonNull(dataService);
    this.entityAttributesValidator = requireNonNull(entityAttributesValidator);
    this.defaultValueReferenceValidator = defaultValueReferenceValidator;
  }

  @Override
  public void update(Entity entity) {
    try (ValidationResource validationResource = new ValidationResource()) {
      validate(entity, validationResource, ValidationMode.UPDATE);
    }
    delegate().update(entity);
  }

  @Override
  public void update(Stream<Entity> entities) {
    try (ValidationResource validationResource = new ValidationResource()) {
      entities = validate(entities, validationResource, ValidationMode.UPDATE);
      delegate().update(entities);
    }
  }

  @Override
  public void add(Entity entity) {
    try (ValidationResource validationResource = new ValidationResource()) {
      validate(entity, validationResource, ValidationMode.ADD);
    }
    delegate().add(entity);
  }

  @Override
  public Integer add(Stream<Entity> entities) {
    try (ValidationResource validationResource = new ValidationResource()) {
      entities = validate(entities, validationResource, ValidationMode.ADD);
      return delegate().add(entities);
    }
  }

  @Override
  public void delete(Entity entity) {
    defaultValueReferenceValidator.validateEntityNotReferenced(entity);
    delegate().delete(entity);
  }

  @Override
  public void deleteById(Object id) {
    defaultValueReferenceValidator.validateEntityNotReferencedById(id, getEntityType());
    delegate().deleteById(id);
  }

  @Override
  public void deleteAll() {
    defaultValueReferenceValidator.validateEntityTypeNotReferenced(getEntityType());
    delegate().deleteAll();
  }

  @Override
  public void delete(Stream<Entity> entities) {
    delegate()
        .delete(
            defaultValueReferenceValidator.validateEntitiesNotReferenced(
                entities, getEntityType()));
  }

  @Override
  public void deleteAll(Stream<Object> ids) {
    delegate()
        .deleteAll(
            defaultValueReferenceValidator.validateEntitiesNotReferencedById(ids, getEntityType()));
  }

  private Stream<Entity> validate(
      Stream<Entity> entities,
      ValidationResource validationResource,
      ValidationMode validationMode) {
    // prepare validation
    initValidation(validationResource, validationMode);

    ValidationProfile validationProfile = new ValidationProfile().invoke();

    // add validation operation to stream
    return entities.filter(
        entity -> {
          validate(entity, validationResource, validationMode, validationProfile);

          return true;
        });
  }

  private void validate(
      Entity entity, ValidationResource validationResource, ValidationMode validationMode) {
    initValidation(validationResource, validationMode);
    validate(entity, validationResource, validationMode, new ValidationProfile().invoke());
  }

  private void validate(
      Entity entity,
      ValidationResource validationResource,
      ValidationMode validationMode,
      ValidationProfile validationProfile) {
    validationResource.incrementRow();

    validateEntityValueTypes(entity, validationResource);

    // other validation steps might not be able to handle invalid data types, stop here
    if (validationResource.hasViolations()) {
      throw new MolgenisValidationException(validationResource.getViolations());
    }

    if (validationProfile.isValidateRequired()) {
      validateEntityValueRequired(entity, validationResource);
    }

    if (validationProfile.isValidateUniqueness()) {
      validateEntityValueUniqueness(entity, validationResource, validationMode);
    }

    validateEntityValueReferences(entity, validationResource);

    if (validationProfile.isValidateReadonly() && validationMode == ValidationMode.UPDATE) {
      validateEntityValueReadOnly(entity, validationResource);
    }

    if (validationResource.hasViolations()) {
      throw new MolgenisValidationException(validationResource.getViolations());
    }
  }

  private void initValidation(
      ValidationResource validationResource, ValidationMode validationMode) {
    initRequiredValueValidation(validationResource);
    initReferenceValidation(validationResource);
    initUniqueValidation(validationResource);
    if (validationMode == ValidationMode.UPDATE) {
      initReadonlyValidation(validationResource);
    }
  }

  private void initRequiredValueValidation(ValidationResource validationResource) {
    if (!getCapabilities().contains(VALIDATE_NOTNULL_CONSTRAINT)) {
      List<Attribute> requiredValueAttrs =
          stream(getEntityType().getAtomicAttributes())
              .filter(attr -> !attr.isNillable() && attr.getExpression() == null)
              .collect(toList());

      validationResource.setRequiredValueAttrs(requiredValueAttrs);
    }
  }

  private void initReferenceValidation(ValidationResource validationResource) {
    // get reference attrs
    List<Attribute> refAttrs;
    if (!getCapabilities().contains(VALIDATE_REFERENCE_CONSTRAINT)) {
      // get reference attrs
      refAttrs =
          stream(getEntityType().getAtomicAttributes())
              .filter(attr -> isReferenceType(attr) && attr.getExpression() == null)
              .collect(toList());
    } else {
      // validate cross-repository collection reference constraints. the decorated repository takes
      // care of
      // validating other reference constraints
      String backend = dataService.getMeta().getBackend(getEntityType()).getName();
      refAttrs =
          stream(getEntityType().getAtomicAttributes())
              .filter(
                  attr ->
                      isReferenceType(attr)
                          && attr.getExpression() == null
                          && isDifferentBackend(backend, attr))
              .collect(toList());
    }

    // get referenced entity ids
    if (!refAttrs.isEmpty()) {
      Map<String, HugeSet<Object>> refEntitiesIds = new HashMap<>();
      refAttrs.forEach(
          refAttr -> {
            EntityType refEntityType = refAttr.getRefEntity();
            String refEntityName = refEntityType.getId();
            HugeSet<Object> refEntityIds = refEntitiesIds.get(refEntityName);
            if (refEntityIds == null) {
              refEntityIds = new HugeSet<>();
              refEntitiesIds.put(refEntityName, refEntityIds);

              Query<Entity> q =
                  new QueryImpl<>()
                      .fetch(new Fetch().field(refEntityType.getIdAttribute().getName()));
              for (Iterator<Entity> it = dataService.findAll(refEntityName, q).iterator();
                  it.hasNext(); ) {
                refEntityIds.add(it.next().getIdValue());
              }
            }
          });

      validationResource.setRefEntitiesIds(refEntitiesIds);
    }

    validationResource.setSelfReferencing(
        refAttrs
            .stream()
            .anyMatch(refAttr -> refAttr.getRefEntity().getId().equals(getEntityType().getId())));
    validationResource.setRefAttrs(refAttrs);
  }

  private boolean isDifferentBackend(String backend, Attribute attr) {
    EntityType refEntity = attr.getRefEntity();
    String refEntityBackend = dataService.getMeta().getBackend(refEntity).getName();
    return !backend.equals(refEntityBackend);
  }

  private void initUniqueValidation(ValidationResource validationResource) {
    if (!getCapabilities().contains(VALIDATE_UNIQUE_CONSTRAINT)) {
      // get unique attributes
      List<Attribute> uniqueAttrs =
          stream(getEntityType().getAtomicAttributes())
              .filter(attr -> attr.isUnique() && attr.getExpression() == null)
              .collect(toList());

      // get existing values for each attributes
      if (!uniqueAttrs.isEmpty()) {
        Map<String, HugeMap<Object, Object>> uniqueAttrsValues = new HashMap<>();

        Fetch fetch = new Fetch();
        uniqueAttrs.forEach(
            uniqueAttr -> {
              uniqueAttrsValues.put(uniqueAttr.getName(), new HugeMap<>());
              fetch.field(uniqueAttr.getName());
            });

        Query<Entity> q = new QueryImpl<>().fetch(fetch);
        delegate()
            .findAll(q)
            .forEach(
                entity ->
                    uniqueAttrs.forEach(
                        uniqueAttr -> {
                          HugeMap<Object, Object> uniqueAttrValues =
                              uniqueAttrsValues.get(uniqueAttr.getName());
                          Object attrValue = entity.get(uniqueAttr.getName());
                          if (attrValue != null) {
                            if (isSingleReferenceType(uniqueAttr)) {
                              attrValue = ((Entity) attrValue).getIdValue();
                            }
                            uniqueAttrValues.put(attrValue, entity.getIdValue());
                          }
                        }));

        validationResource.setUniqueAttrsValues(uniqueAttrsValues);
      }

      validationResource.setUniqueAttrs(uniqueAttrs);
    }
  }

  private void initReadonlyValidation(ValidationResource validationResource) {
    if (!getCapabilities().contains(VALIDATE_READONLY_CONSTRAINT)) {
      String idAttrName = getEntityType().getIdAttribute().getName();
      List<Attribute> readonlyAttrs =
          stream(getEntityType().getAtomicAttributes())
              .filter(
                  attr ->
                      attr.isReadOnly()
                          && attr.getExpression() == null
                          && !attr.isMappedBy()
                          && !attr.getName().equals(idAttrName))
              .collect(toList());

      validationResource.setReadonlyAttrs(readonlyAttrs);
    }
  }

  private void validateEntityValueRequired(Entity entity, ValidationResource validationResource) {
    validationResource
        .getRequiredValueAttrs()
        .forEach(
            nonNillableAttr -> {
              Object value = entity.get(nonNillableAttr.getName());
              if (value == null
                  || (isMultipleReferenceType(nonNillableAttr)
                      && !entity.getEntities(nonNillableAttr.getName()).iterator().hasNext())) {
                ConstraintViolation constraintViolation =
                    new ConstraintViolation(
                        format(
                            "The attribute '%s' of entity '%s' can not be null.",
                            nonNillableAttr.getName(), getName()),
                        (long) validationResource.getRow());
                validationResource.addViolation(constraintViolation);
              }
            });
  }

  private void validateEntityValueTypes(Entity entity, ValidationResource validationResource) {
    // entity attributes validation
    Set<ConstraintViolation> attrViolations =
        entityAttributesValidator.validate(entity, getEntityType());
    if (attrViolations != null && !attrViolations.isEmpty()) {
      attrViolations.forEach(validationResource::addViolation);
    }
  }

  private void validateEntityValueUniqueness(
      Entity entity, ValidationResource validationResource, ValidationMode validationMode) {
    validationResource
        .getUniqueAttrs()
        .forEach(
            uniqueAttr -> {
              Object attrValue = entity.get(uniqueAttr.getName());
              if (attrValue != null) {
                if (isSingleReferenceType(uniqueAttr)) {
                  attrValue = ((Entity) attrValue).getIdValue();
                }

                HugeMap<Object, Object> uniqueAttrValues =
                    validationResource.getUniqueAttrsValues().get(uniqueAttr.getName());
                Object existingEntityId = uniqueAttrValues.get(attrValue);
                if ((validationMode == ValidationMode.ADD && existingEntityId != null)
                    || (validationMode == ValidationMode.UPDATE
                        && existingEntityId != null
                        && !existingEntityId.equals(entity.getIdValue()))) {
                  ConstraintViolation constraintViolation =
                      new ConstraintViolation(
                          format(
                              "Duplicate value '%s' for unique attribute '%s' from entity '%s'",
                              attrValue, uniqueAttr.getName(), getName()),
                          (long) validationResource.getRow());
                  validationResource.addViolation(constraintViolation);
                } else {
                  uniqueAttrValues.put(attrValue, entity.getIdValue());
                }
              }
            });
  }

  private void validateEntityValueReferences(Entity entity, ValidationResource validationResource) {
    validationResource
        .getRefAttrs()
        .forEach(
            refAttr -> {
              HugeSet<Object> refEntityIds =
                  validationResource.getRefEntitiesIds().get(refAttr.getRefEntity().getId());

              Iterable<Entity> refEntities;
              if (isSingleReferenceType(refAttr)) {
                Entity refEntity = entity.getEntity(refAttr.getName());
                if (refEntity != null) {
                  refEntities = singleton(refEntity);
                } else {
                  refEntities = emptyList();
                }
              } else {
                refEntities = entity.getEntities(refAttr.getName());
              }

              for (Entity refEntity : refEntities) {
                if (!refEntityIds.contains(refEntity.getIdValue())) {
                  boolean selfReference =
                      entity.getEntityType().getId().equals(refAttr.getRefEntity().getId());
                  if (!(selfReference && entity.getIdValue().equals(refEntity.getIdValue()))) {
                    String message =
                        String.format(
                            "Unknown xref value '%s' for attribute '%s' of entity '%s'.",
                            DataConverter.toString(refEntity.getIdValue()),
                            refAttr.getName(),
                            getName());

                    ConstraintViolation constraintViolation =
                        new ConstraintViolation(message, (long) validationResource.getRow());
                    validationResource.addViolation(constraintViolation);
                  }
                }
              }

              // only do if self reference
              if (validationResource.isSelfReferencing()) {
                validationResource.addRefEntityId(getName(), entity.getIdValue());
              }
            });
  }

  @SuppressWarnings("unchecked")
  private void validateEntityValueReadOnly(Entity entity, ValidationResource validationResource) {
    if (validationResource.getReadonlyAttrs().isEmpty()) {
      return;
    }

    Entity entityToUpdate = findOneById(entity.getIdValue());
    validationResource
        .getReadonlyAttrs()
        .forEach(
            readonlyAttr -> {
              Object value = entity.get(readonlyAttr.getName());

              Object existingValue = entityToUpdate.get(readonlyAttr.getName());

              if (isSingleReferenceType(readonlyAttr)) {
                if (value != null) {
                  value = ((Entity) value).getIdValue();
                }
                if (existingValue != null) {
                  existingValue = ((Entity) existingValue).getIdValue();
                }
              } else if (isMultipleReferenceType(readonlyAttr)) {
                value =
                    stream(entity.getEntities(readonlyAttr.getName()))
                        .map(Entity::getIdValue)
                        .collect(toList());

                existingValue =
                    stream(entityToUpdate.getEntities(readonlyAttr.getName()))
                        .map(Entity::getIdValue)
                        .collect(toList());
              }

              if (value != null && existingValue != null && !value.equals(existingValue)) {
                validationResource.addViolation(
                    new ConstraintViolation(
                        format(
                            "The attribute '%s' of entity '%s' can not be changed it is readonly.",
                            readonlyAttr.getName(), getName()),
                        (long) validationResource.getRow()));
              }
            });
  }

  /** Container with validation data used during stream validation */
  private static class ValidationResource implements AutoCloseable {
    private AtomicInteger rowNr;
    private List<Attribute> requiredValueAttrs;
    private List<Attribute> refAttrs;
    private Map<String, HugeSet<Object>> refEntitiesIds;
    private List<Attribute> uniqueAttrs;
    private Map<String, HugeMap<Object, Object>> uniqueAttrsValues;
    private List<Attribute> readonlyAttrs;
    private boolean selfReferencing;
    private Set<ConstraintViolation> violations;

    ValidationResource() {
      rowNr = new AtomicInteger();
    }

    public int getRow() {
      return rowNr.get();
    }

    void incrementRow() {
      rowNr.incrementAndGet();
    }

    List<Attribute> getRequiredValueAttrs() {
      return requiredValueAttrs != null ? unmodifiableList(requiredValueAttrs) : emptyList();
    }

    void setRequiredValueAttrs(List<Attribute> requiredValueAttrs) {
      this.requiredValueAttrs = requiredValueAttrs;
    }

    List<Attribute> getRefAttrs() {
      return unmodifiableList(refAttrs);
    }

    void setRefAttrs(List<Attribute> refAttrs) {
      this.refAttrs = refAttrs;
    }

    Map<String, HugeSet<Object>> getRefEntitiesIds() {
      return refEntitiesIds != null ? unmodifiableMap(refEntitiesIds) : emptyMap();
    }

    void setRefEntitiesIds(Map<String, HugeSet<Object>> refEntitiesIds) {
      this.refEntitiesIds = refEntitiesIds;
    }

    void addRefEntityId(String name, Object idValue) {
      HugeSet<Object> refEntityIds = refEntitiesIds.get(name);
      // only add entity id if this validation run requires entity
      if (refEntityIds != null) {
        refEntityIds.add(idValue);
      }
    }

    List<Attribute> getUniqueAttrs() {
      return uniqueAttrs != null ? unmodifiableList(uniqueAttrs) : emptyList();
    }

    void setUniqueAttrs(List<Attribute> uniqueAttrs) {
      this.uniqueAttrs = uniqueAttrs;
    }

    Map<String, HugeMap<Object, Object>> getUniqueAttrsValues() {
      return uniqueAttrsValues != null ? unmodifiableMap(uniqueAttrsValues) : emptyMap();
    }

    void setUniqueAttrsValues(Map<String, HugeMap<Object, Object>> uniqueAttrsValues) {
      this.uniqueAttrsValues = uniqueAttrsValues;
    }

    List<Attribute> getReadonlyAttrs() {
      return readonlyAttrs != null ? unmodifiableList(readonlyAttrs) : emptyList();
    }

    void setReadonlyAttrs(List<Attribute> readonlyAttrs) {
      this.readonlyAttrs = readonlyAttrs;
    }

    void setSelfReferencing(boolean selfReferencing) {
      this.selfReferencing = selfReferencing;
    }

    boolean isSelfReferencing() {
      return selfReferencing;
    }

    boolean hasViolations() {
      return violations != null && !violations.isEmpty();
    }

    void addViolation(ConstraintViolation constraintViolation) {
      if (violations == null) {
        violations = new LinkedHashSet<>();
      }
      violations.add(constraintViolation);
    }

    public Set<ConstraintViolation> getViolations() {
      return violations != null ? unmodifiableSet(violations) : emptySet();
    }

    @Override
    public void close() {
      if (refEntitiesIds != null) {
        for (HugeSet<Object> refEntityIds : refEntitiesIds.values()) {
          try {
            refEntityIds.close();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
      if (uniqueAttrsValues != null) {
        for (HugeMap<Object, Object> uniqueAttrValues : uniqueAttrsValues.values()) {
          try {
            uniqueAttrValues.close();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
  }

  private class ValidationProfile {
    private boolean validateRequired;
    private boolean validateUniqueness;
    private boolean validateReadonly;

    boolean isValidateRequired() {
      return validateRequired;
    }

    boolean isValidateUniqueness() {
      return validateUniqueness;
    }

    boolean isValidateReadonly() {
      return validateReadonly;
    }

    public ValidationProfile invoke() {
      validateRequired = !getCapabilities().contains(VALIDATE_NOTNULL_CONSTRAINT);
      validateUniqueness = !getCapabilities().contains(VALIDATE_UNIQUE_CONSTRAINT);
      validateReadonly = !getCapabilities().contains(VALIDATE_READONLY_CONSTRAINT);
      return this;
    }
  }
}
