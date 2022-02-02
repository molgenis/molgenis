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
import org.molgenis.data.support.exceptions.TemplateExpressionException;
import org.molgenis.data.support.exceptions.TemplateExpressionInvalidTagException;
import org.molgenis.data.support.exceptions.TemplateExpressionMathInvalidParameterException;
import org.molgenis.data.support.exceptions.TemplateExpressionMathNotEnoughParametersException;
import org.molgenis.data.support.exceptions.TemplateExpressionMathUnknownOperatorException;
import org.molgenis.data.support.exceptions.TemplateExpressionMissingTagException;
import org.molgenis.data.support.exceptions.TemplateExpressionSyntaxException;
import org.molgenis.data.support.exceptions.TemplateExpressionUnknownAttributeException;

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
    String expression = getString(attribute);
    JsonTemplate jsonTemplate = getJsonTemplate(expression);

    try {
      return HANDLEBARS.compileInline(jsonTemplate.getTemplate());
    } catch (IOException | HandlebarsException e) {
      throw new TemplateExpressionSyntaxException(expression, e);
    }
  }

  private static String getString(Attribute attribute) {
    String expression = attribute.getExpression();
    if (expression == null) {
      throw new TemplateExpressionException(attribute);
    } else {
      return expression;
    }
  }

  private static JsonTemplate getJsonTemplate(String expression) {
    JsonTemplate jsonTemplate;
    try {
      jsonTemplate = new Gson().fromJson(expression, JsonTemplate.class);
    } catch (JsonSyntaxException e) {
      throw new TemplateExpressionSyntaxException(expression, e);
    }
    return jsonTemplate;
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
    } catch (IOException exception) {
      throw new TemplateExpressionException(attribute, exception);
    } catch (HandlebarsException exception){
      handleHandleBarsException(exception);
    }
    return null;
  }

  private void handleHandleBarsException(HandlebarsException exception) {
    Throwable cause = exception.getCause();
    String[] split = cause.toString().split(":");
    if(split[0].contains("TemplateExpressionMathNotEnoughParametersException")){
      throw new TemplateExpressionMathNotEnoughParametersException();
    } else if (split[0].contains("TemplateExpressionMathInvalidParameterException")){
      throw new TemplateExpressionMathInvalidParameterException();
    } else if (split[0].contains("TemplateExpressionMathUnknownOperatorException")){
      throw new TemplateExpressionMathUnknownOperatorException(getOperator(split[1]));
    } else {
      throw new TemplateExpressionMathInvalidParameterException();
    }
  }

  private String getOperator(String exceptionMessage) {
    return exceptionMessage.split("'")[1];
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
      if (doesValueNeedsMerging(value, existingValue)) {
        Map<String, Object> mergedValue = new HashMap<>();
        mergedValue.putAll((Map) existingValue);
        mergedValue.putAll((Map) value);
        value = mergedValue;
      }
    }
    varValues.put(variableName, value);
  }

  private boolean doesValueNeedsMerging(Object newValue, Object existingValue) {
    return existingValue != null
        && !Objects.equals(newValue, existingValue)
        && newValue instanceof Map
        && existingValue instanceof Map;
  }

  private Object getTemplateTagValueRecursive(Entity entity, List<String> tagParts, int index) {
    String attributeName = tagParts.get(index);

    Attribute tagAttribute = entity.getEntityType().getAttribute(attributeName);
    return switch (tagAttribute.getDataType()) {
      case BOOL -> entity.getBoolean(attributeName);
      case CATEGORICAL, FILE, XREF -> getXrefVarValue(entity, tagParts, index, attributeName);
      case CATEGORICAL_MREF, MREF, ONE_TO_MANY -> getMrefVarValue(entity, tagParts, index, attributeName);
      case DATE ->  entity.getLocalDate(attributeName);
      case DATE_TIME ->  entity.getInstant(attributeName);
      case DECIMAL ->  entity.getDouble(attributeName);
      case EMAIL, ENUM, HTML, HYPERLINK, SCRIPT, STRING, TEXT ->  entity.getString(attributeName);
      case INT ->  entity.getInt(attributeName);
      case LONG -> entity.getLong(attributeName);
      case COMPOUND -> throw new IllegalAttributeTypeException(tagAttribute.getDataType());
    };
  }

  private Map<String, Object> getXrefVarValue(Entity entity, List<String> tagParts, int index,
      String attributeName) {
    Entity refEntity = entity.getEntity(attributeName);
    Object refValue = refEntity != null ?
      getTemplateTagValueRecursive(refEntity, tagParts, index + 1) : null;
    String refTag = tagParts.get(index + 1);
    return singletonMap(refTag, refValue);
  }

  private Map<String, Object> getMrefVarValue(Entity entity, List<String> tagParts, int index, String attributeName) {
    Object mrefValue = getMrefValue(entity, tagParts, index, attributeName);
    String mrefTag = tagParts.get(index + 1);
    return singletonMap(mrefTag, mrefValue);
  }

  private Object getMrefValue(Entity entity, List<String> tagParts, int index, String attributeName) {
    return stream(entity.getEntities(attributeName))
      .map(mrefEntity -> getTemplateTagValueRecursive(mrefEntity, tagParts, index + 1))
      .map(Object::toString)
      .collect(joining(","));
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
