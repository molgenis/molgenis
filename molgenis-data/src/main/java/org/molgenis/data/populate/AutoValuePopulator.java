package org.molgenis.data.populate;

import static com.google.common.collect.Streams.stream;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.semantic.Relation.hasIDDigitCount;
import static org.molgenis.data.semantic.Relation.hasIDPrefix;
import static org.molgenis.data.semantic.Relation.type;
import static org.molgenis.data.semantic.Vocabulary.SCRAMBLED;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.util.IntScrambler;
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
   * Generates a new sequence ID if the attribute is tagged with ID prefix and ID digit count. If
   * the ID is also tagged with "scrambled", it will scramble the digit part.
   *
   * @param attribute the ID attribute
   * @return formatted ID, in sequence with
   */
  private Optional<String> generateFormattedSequenceId(Attribute attribute) {
    return stream(attribute.getTags())
        .filter(this::isIdPrefix)
        .findFirst()
        .map(Tag::getValue)
        .flatMap(
            idPrefix ->
                stream(attribute.getTags())
                    .filter(this::isIdDigitCount)
                    .findFirst()
                    .map(Tag::getValue)
                    .map(Integer::parseInt)
                    .map("0"::repeat)
                    .map(zeroes -> idPrefix + zeroes))
        .map(DecimalFormat::new)
        .map(
            format -> {
              int sequence = (int) sequences.generateId(attribute);
              if (stream(attribute.getTags()).anyMatch(this::isScrambled)) {
                var scrambler = IntScrambler.forDecimalFormat(format);
                return format.format(scrambler.scramble(sequence));
              } else {
                return format.format(sequence);
              }
            });
  }

  private boolean isScrambled(Tag tag) {
    return type.getIRI().equals(tag.getRelationIri())
        && SCRAMBLED.toString().equals(tag.getObjectIri());
  }

  private boolean isIdPrefix(Tag tag) {
    return hasIDPrefix.getIRI().equals(tag.getRelationIri());
  }

  private boolean isIdDigitCount(Tag tag) {
    return hasIDDigitCount.getIRI().equals(tag.getRelationIri());
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
