package org.molgenis.data.populate;

import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Streams.stream;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.semantic.Relation.hasIDDigitCount;
import static org.molgenis.data.semantic.Relation.hasIDPrefix;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.stereotype.Component;

/** Populate entity values for auto attributes */
@Component
public class AutoValuePopulator {
  private final IdGenerator idGenerator;
  private final Sequences sequences;

  public AutoValuePopulator(IdGenerator idGenerator, Sequences sequences) {
    this.idGenerator = requireNonNull(idGenerator);
    this.sequences = requireNonNull(sequences);
  }

  /**
   * Populates an entity with auto values
   *
   * @param entity populated entity
   */
  public void populate(Entity entity) {
    // auto date
    generateAutoDateOrDateTime(singletonList(entity), entity.getEntityType().getAtomicAttributes());

    // auto id
    Attribute idAttr = entity.getEntityType().getIdAttribute();
    if (idAttr != null
        && idAttr.isAuto()
        && entity.getIdValue() == null
        && (idAttr.getDataType() == STRING)) {
      var id = generateFormattedSequenceId(idAttr).orElseGet(idGenerator::generateId);
      entity.set(idAttr.getName(), id);
    }
  }

  /**
   * Generates a new sequence ID if the attribute is tagged with ID prefix and ID digit count
   *
   * @param attribute the ID attribute
   * @return formatted ID, in sequence with
   */
  private Optional<String> generateFormattedSequenceId(Attribute attribute) {
    return Optional.ofNullable(attribute.getTags())
        .flatMap(
            tags ->
                tryFind(tags, tag -> hasIDPrefix.getIRI().equals(tag.getRelationIri()))
                    .toJavaUtil()
                    .map(Tag::getLabel)
                    .flatMap(
                        idPrefix ->
                            tryFind(
                                    tags,
                                    tag -> hasIDDigitCount.getIRI().equals(tag.getRelationIri()))
                                .toJavaUtil()
                                .map(Tag::getLabel)
                                .map(Integer::parseInt)
                                .map("0"::repeat)
                                .map(zeroes -> idPrefix + zeroes))
                    .map(DecimalFormat::new)
                    .map(format -> format.format((int) sequences.generateId(attribute))));
  }

  private static void generateAutoDateOrDateTime(
      Iterable<? extends Entity> entities, Iterable<Attribute> attrs) {
    // get auto date and datetime attributes
    Iterable<Attribute> autoAttrs =
        stream(attrs)
            .filter(
                attr -> {
                  if (attr.isAuto()) {
                    AttributeType type = attr.getDataType();
                    return type == DATE || type == DATE_TIME;
                  } else {
                    return false;
                  }
                })
            .toList();

    // set current date for auto date and datetime attributes
    for (Entity entity : entities) {
      for (Attribute attr : autoAttrs) {
        AttributeType type = attr.getDataType();
        switch (type) {
          case DATE:
            entity.set(attr.getName(), LocalDate.now(ZoneId.systemDefault()));
            break;
          case DATE_TIME:
            entity.set(attr.getName(), Instant.now());
            break;
          default:
            throw new UnexpectedEnumException(type);
        }
      }
    }
  }
}
