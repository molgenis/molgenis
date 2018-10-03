package org.molgenis.data.support;

import static com.google.common.collect.Streams.stream;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.TagType;
import com.github.jknack.handlebars.Template;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.IllegalAttributeTypeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;

public class TemplateExpressionEvaluator implements ExpressionEvaluator {
  private static final Handlebars HANDLEBARS = new Handlebars();

  private final Attribute attribute;
  private final EntityType entityType;
  private Template template;
  private List<List<String>> templateTags;

  TemplateExpressionEvaluator(Attribute attribute, EntityType entityType) {
    this.attribute = requireNonNull(attribute);
    this.entityType = requireNonNull(entityType);
  }

  @Override
  public Object evaluate(Entity entity) {
    initTemplate();

    Map<String, Object> tagValues = getTemplateTagValue(entity);

    try {
      return template.apply(Context.newContext(tagValues));
    } catch (IOException e) {
      throw new TemplateExpressionException(attribute, e);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getTemplateTagValue(Entity entity) {
    Map<String, Object> tagValues = new HashMap<>();
    templateTags.forEach(
        tagParts -> {
          Object value = getTemplateTagValueRec(entity, tagParts, 0);

          String tagPart = tagParts.get(0);
          if (tagValues.containsKey(tagPart)) {
            Object existingValue = tagValues.get(tagPart);
            if (existingValue != null
                && !Objects.equals(value, existingValue)
                && value instanceof Map
                && existingValue instanceof Map) {
              Map<String, Object> mergedValue = new HashMap<>();
              mergedValue.putAll((Map) existingValue);
              mergedValue.putAll((Map) value);
              value = mergedValue;
            }
          }
          tagValues.put(tagParts.get(0), value);
        });
    return tagValues;
  }

  private Object getTemplateTagValueRec(Entity entity, List<String> tagParts, int index) {
    String attributeName = tagParts.get(index);

    Object value;
    Attribute tagAttribute = entity.getEntityType().getAttribute(attributeName);
    switch (tagAttribute.getDataType()) {
      case BOOL:
        value = entity.getBoolean(attributeName);
        break;
      case CATEGORICAL:
      case FILE:
      case XREF:
        Entity refEntity = entity.getEntity(attributeName);
        Object refValue =
            refEntity != null ? getTemplateTagValueRec(refEntity, tagParts, index + 1) : null;

        String refTag = tagParts.get(index + 1);
        value = singletonMap(refTag, refValue);
        break;
      case CATEGORICAL_MREF:
      case MREF:
      case ONE_TO_MANY:
        Object mrefValue =
            stream(entity.getEntities(attributeName))
                .map(mrefEntity -> getTemplateTagValueRec(mrefEntity, tagParts, index + 1))
                .map(Object::toString)
                .collect(joining(","));

        String mrefTag = tagParts.get(index + 1);
        value = singletonMap(mrefTag, mrefValue);
        break;
      case COMPOUND:
        throw new IllegalAttributeTypeException(tagAttribute.getDataType());
      case DATE:
        value = entity.getLocalDate(attributeName);
        break;
      case DATE_TIME:
        value = entity.getInstant(attributeName);
        break;
      case DECIMAL:
        value = entity.getDouble(attributeName);
        break;
      case EMAIL:
      case ENUM:
      case HTML:
      case HYPERLINK:
      case SCRIPT:
      case STRING:
      case TEXT:
        value = entity.getString(attributeName);
        break;
      case INT:
        value = entity.getInt(attributeName);
        break;
      case LONG:
        value = entity.getLong(attributeName);
        break;
      default:
        throw new UnexpectedEnumException(tagAttribute.getDataType());
    }
    return value;
  }

  private synchronized void initTemplate() {
    if (template == null) {
      template = getTemplate(attribute);
      templateTags = getTemplateVariables(template);
    }
  }

  private Template getTemplate(Attribute attribute) {
    String expression = attribute.getExpression();
    if (expression == null) {
      throw new TemplateExpressionException(attribute);
    }

    JsonTemplate jsonTemplate;
    try {
      jsonTemplate = new Gson().fromJson(expression, JsonTemplate.class);
    } catch (JsonSyntaxException e) {
      throw new TemplateExpressionSyntaxException(expression, e);
    }

    try {
      return HANDLEBARS.compileInline(jsonTemplate.getTemplate());
    } catch (IOException | HandlebarsException e) {
      throw new TemplateExpressionSyntaxException(expression, e);
    }
  }

  private List<List<String>> getTemplateVariables(Template template) {
    List<String> tagNames = template.collect(TagType.VAR);
    List<List<String>> composedTagNames =
        tagNames.stream().map(tagName -> asList(tagName.split("\\."))).collect(toList());
    composedTagNames.forEach(this::validateTemplateVariable);
    return composedTagNames;
  }

  private void validateTemplateVariable(List<String> composedTagName) {
    EntityType variableEntityType = entityType;
    for (Iterator<String> it = composedTagName.iterator(); it.hasNext(); ) {
      String tagPartName = it.next();

      if (variableEntityType == null) {
        throw new TemplateExpressionInvalidTagException(attribute.getExpression(), tagPartName);
      }

      Attribute tagAttribute = variableEntityType.getAttribute(tagPartName);
      if (tagAttribute == null) {
        throw new TemplateExpressionUnknownAttributeException(
            attribute.getExpression(), tagPartName);
      }
      if (tagAttribute.getDataType() == AttributeType.COMPOUND) {
        throw new TemplateExpressionAttributeTypeException(
            attribute.getExpression(), tagPartName, tagAttribute);
      }

      variableEntityType = tagAttribute.getRefEntity();
      if (variableEntityType != null && !it.hasNext()) {
        throw new TemplateExpressionMissingTagException(attribute.getExpression(), tagPartName);
      }
    }
  }

  private static class JsonTemplate {
    private String template;

    public String getTemplate() {
      return template;
    }

    public void setTemplate(String template) {
      this.template = template;
    }
  }
}
