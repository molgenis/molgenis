package org.molgenis.data.support;

import static com.google.common.collect.Streams.stream;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.TagType;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache;
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

  public static final String MATH = "molgenis-math";
  private static final Handlebars HANDLEBARS = setupHandlebars();
  private final Attribute attribute;
  private final EntityType entityType;
  private Template template;
  private List<List<String>> variables;

  TemplateExpressionEvaluator(Attribute attribute, EntityType entityType) {
    this.attribute = requireNonNull(attribute);
    this.entityType = requireNonNull(entityType);
  }

  public static Template getTemplate(Attribute attribute) {
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

  private static Handlebars setupHandlebars() {
    return new Handlebars()
        .with(new ConcurrentMapTemplateCache())
        .with(EscapingStrategy.NOOP)
        .registerHelper(MATH, new HandlebarsMathHelper());
  }

  @Override
  public Object evaluate(Entity entity) {
    initTemplate();
    Map<String, Object> tagValues = getVarValues(entity);

    try {
      return template.apply(Context.newContext(tagValues));
    } catch (IOException e) {
      throw new TemplateExpressionException(attribute, e);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getVarValues(Entity entity) {
    Map<String, Object> varValues = new HashMap<>();
    variables.forEach(varParts -> getVarValue(entity, varValues, varParts));
    return varValues;
  }

  private void getVarValue(Entity entity, Map<String, Object> varValues, List<String> varParts) {
    Object value = getTemplateTagValueRecursive(entity, varParts, 0);

    String variableName = varParts.get(0);
    if (varValues.containsKey(variableName)) {
      Object existingValue = varValues.get(variableName);
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
    varValues.put(variableName, value);
  }

  private Object getTemplateTagValueRecursive(Entity entity, List<String> tagParts, int index) {
    String attributeName = tagParts.get(index);

    Attribute tagAttribute = entity.getEntityType().getAttribute(attributeName);
    switch (tagAttribute.getDataType()) {
      case BOOL -> { return entity.getBoolean(attributeName); }
      case CATEGORICAL, FILE, XREF -> { return getXrefVarValue(entity, tagParts, index, attributeName); }
      case CATEGORICAL_MREF, MREF, ONE_TO_MANY -> { return getMrefVarValue(entity, tagParts, index, attributeName); }
      case DATE -> { return entity.getLocalDate(attributeName); }
      case DATE_TIME -> { return entity.getInstant(attributeName); }
      case DECIMAL -> { return entity.getDouble(attributeName); }
      case EMAIL, ENUM, HTML, HYPERLINK, SCRIPT, STRING, TEXT -> { return entity.getString(attributeName); }
      case INT -> { return entity.getInt(attributeName); }
      case LONG ->{ return entity.getLong(attributeName); }
      case COMPOUND -> throw new IllegalAttributeTypeException(tagAttribute.getDataType());
      default -> throw new UnexpectedEnumException(tagAttribute.getDataType());
    }
  }

  private Map<String, Object> getXrefVarValue(Entity entity, List<String> tagParts, int index,
      String attributeName) {
    Entity refEntity = entity.getEntity(attributeName);
    Object refValue =
        refEntity != null ? getTemplateTagValueRecursive(refEntity, tagParts, index + 1) : null;
    String refTag = tagParts.get(index + 1);
    return singletonMap(refTag, refValue);
  }

  private Map<String, Object> getMrefVarValue(Entity entity, List<String> tagParts, int index,
      String attributeName) {
    Object mrefValue =
        stream(entity.getEntities(attributeName))
            .map(mrefEntity -> getTemplateTagValueRecursive(mrefEntity, tagParts, index + 1))
            .map(Object::toString)
            .collect(joining(","));
    String mrefTag = tagParts.get(index + 1);
    return singletonMap(mrefTag, mrefValue);
  }

  private synchronized void initTemplate() {
    if (template == null) {
      template = getTemplate(attribute);
      variables = getTemplateVariables(template);
    }
  }

  private List<List<String>> getTemplateVariables(Template template) {
    List<List<String>> varNames = getVarNames(template);
    validateVars(varNames);
    return varNames;
  }

  private List<List<String>> getVarNames(Template template) {
    List<String> varNames = template.collectReferenceParameters();
    List<String> simpleVarNames = template.collect(TagType.VAR);
    varNames.addAll(simpleVarNames);
    return varNames.stream().filter(s -> !s.equals(MATH)).map(tagName -> asList(tagName.split("\\."))).toList();
  }

  private void validateVars(List<List<String>> varNames) {
    varNames.forEach(this::validateVariable);
  }

  private void validateVariable(List<String> composedVarName) {
    EntityType variableEntityType = entityType;
    for (Iterator<String> iterator = composedVarName.iterator(); iterator.hasNext(); ) {
      String tagPartName = iterator.next();

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

      variableEntityType = tagAttribute.hasRefEntity() ? tagAttribute.getRefEntity() : null;
      if (variableEntityType != null && !iterator.hasNext()) {
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
