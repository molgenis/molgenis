// package org.molgenis.data.security.audit;
//
// import static com.google.common.collect.Streams.stream;
// import static org.molgenis.data.security.audit.AuthenticationUtils.getUsername;
// import static org.molgenis.data.semantic.Relation.isAssociatedWith;
// import static org.molgenis.data.semantic.Vocabulary.CREATED_BY;
// import static org.molgenis.data.semantic.Vocabulary.CREATED_ON;
// import static org.molgenis.data.semantic.Vocabulary.MODIFIED_BY;
// import static org.molgenis.data.semantic.Vocabulary.MODIFIED_ON;
//
// import java.time.Instant;
// import java.time.LocalDate;
// import java.util.concurrent.atomic.AtomicReference;
// import java.util.stream.Stream;
// import org.eclipse.rdf4j.model.IRI;
// import org.molgenis.data.AbstractRepositoryDecorator;
// import org.molgenis.data.Entity;
// import org.molgenis.data.Repository;
// import org.molgenis.data.meta.model.Attribute;
// import org.molgenis.data.meta.model.Tag;
// import org.molgenis.data.util.EntityTypeUtils;
//
// public class ChangedByRepositoryDecorator extends AbstractRepositoryDecorator<Entity> {
//
//  public ChangedByRepositoryDecorator(Repository<Entity> delegateRepository) {
//    super(delegateRepository);
//  }
//
//  @Override
//  public void update(Entity entity) {
//    if (isNonSystemEntityType()) {
//      setValues(entity, MODIFIED_BY, MODIFIED_ON);
//    }
//
//    delegate().update(entity);
//  }
//
//
//  @Override
//  public void add(Entity entity) {
//    if (isNonSystemEntityType()) {
//      setValues(entity, CREATED_BY, CREATED_ON);
//    }
//
//    delegate().add(entity);
//  }
//
//  @Override
//  public Integer add(Stream<Entity> entities) {
//    if (isNonSystemEntityType()) {
//      entities = setValuesStream(entities, CREATED_BY, CREATED_ON);
//    }
//    return delegate().add(entities);
//  }
//
//  @Override
//  public void update(Stream<Entity> entities) {
//    if (isNonSystemEntityType()) {
//      entities = setValuesStream(entities, MODIFIED_BY, MODIFIED_ON);
//    }
//    delegate().update(entities);
//  }
//
//  private void setValues(Entity entity, IRI byTag, IRI onTag) {
//    if (isNonSystemEntityType()) {
//      delegate()
//          .getEntityType()
//          .getAtomicAttributes()
//          .forEach(attribute ->
//              stream(attribute.getTags())
//                  .filter(tag -> tag.getRelationIri().equals(isAssociatedWith.getIRI()))
//                  .findFirst()
//                  .ifPresent(tag -> {
//                    if (isTag(tag, byTag)) {
//                      setUsername(entity, attribute);
//                    } else if (isTag(tag, onTag)) {
//                      setNow(entity, attribute);
//                    }
//                  }));
//    }
//  }
//
//  private Stream<Entity> setValuesStream(Stream<Entity> entities, IRI byTag, IRI onTag) {
//    var entitiesStream = new AtomicReference<>(entities);
//    delegate()
//        .getEntityType()
//        .getAtomicAttributes()
//        .forEach(attribute ->
//            stream(attribute.getTags())
//                .filter(tag -> tag.getRelationIri().equals(isAssociatedWith.getIRI()))
//                .findFirst()
//                .ifPresent(tag -> {
//                  if (isTag(tag, byTag)) {
//                    entitiesStream.set(entities.filter(entity -> {
//                      setUsername(entity, attribute);
//                      return true;
//                    }));
//                  } else if (isTag(tag, onTag)) {
//                    entitiesStream.set(entities.filter(entity -> {
//                      setNow(entity, attribute);
//                      return true;
//                    }));
//                  }
//                }));
//    return entitiesStream.get();
//  }
//
//  private boolean isNonSystemEntityType() {
//    return !EntityTypeUtils.isSystemEntity(delegate().getEntityType());
//  }
//
//  private boolean isTag(Tag tag, IRI objectIRI) {
//    return objectIRI.toString().equals(tag.getObjectIri());
//  }
//
//  private void setUsername(Entity entity, Attribute attribute) {
//    entity.set(attribute.getName(), getUsername());
//  }
//
//  private void setNow(Entity entity, Attribute attribute) {
//    var type = attribute.getDataType();
//    switch (type) {
//      case DATE:
//        entity.set(attribute.getName(), LocalDate.now());
//        break;
//      case DATE_TIME:
//        entity.set(attribute.getName(), Instant.now());
//        break;
//      default:
//        //TODO add custom exception
//        throw new UnsupportedOperationException();
//    }
//  }
// }
