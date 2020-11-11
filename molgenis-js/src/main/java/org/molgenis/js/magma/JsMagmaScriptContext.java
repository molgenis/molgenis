package org.molgenis.js.magma;

import static com.google.common.base.Throwables.getRootCause;
import static java.util.stream.Collectors.toList;
import static org.molgenis.util.ResourceUtils.getString;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.IllegalAttributeTypeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.js.graal.GraalScriptEngine;
import org.molgenis.script.core.ScriptException;
import org.molgenis.util.UnexpectedEnumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsMagmaScriptContext {
  private static final Logger LOGGER = LoggerFactory.getLogger(JsMagmaScriptContext.class);
  public static final int ENTITY_REFERENCE_DEFAULT_FETCHING_DEPTH = 1;
  private static final String KEY_IS_NULL = "_isNull";
  private static final String KEY_NEW_VALUE = "newValue";
  private static final String KEY_DOLLAR = "$";
  private static final String KEY_MAGMA_SCRIPT = "MagmaScript";
  private static final String BIND = "bind";
  public static final String KEY_ID_VALUE = "_idValue";
  private static final List<Source> SOURCES;

  private final Context context;

  static {
    SOURCES = Stream.of("/js/magma.js").map(JsMagmaScriptContext::getSource).collect(toList());
  }

  private static Source getSource(String resourceName) {
    try {
      String script = getString(JsMagmaScriptEvaluator.class, resourceName);
      return Source.newBuilder("js", script, resourceName).build();
    } catch (IOException ex) {
      throw new IllegalStateException("Resource not found: " + resourceName);
    }
  }

  JsMagmaScriptContext(Context context) {
    this.context = Objects.requireNonNull(context);
    prepare(context);
  }

  private void prepare(Context context) {
    LOGGER.debug("preparing context");
    SOURCES.forEach(context::eval);
    Value bindings = context.getBindings("js");
    Value magmaScript = bindings.getMember(KEY_MAGMA_SCRIPT);
    bindings.putMember(KEY_NEW_VALUE, magmaScript.getMember(KEY_NEW_VALUE));
    bindings.putMember(KEY_IS_NULL, magmaScript.getMember(KEY_IS_NULL));
  }

  public Object tryEval(String expression) {
    try {
      return eval(expression);
    } catch (Exception t) {
      return new ScriptException(getRootCause(t).getMessage());
    }
  }

  public Object eval(String expression) {
    return GraalScriptEngine.convertGraalValue(context.eval("js", expression));
  }

  public void bind(Entity entity) {
    bind(entity, ENTITY_REFERENCE_DEFAULT_FETCHING_DEPTH);
  }

  /**
   * Binds to a given Entity.
   *
   * @param entity the entity to bind to the magmascript $ function
   * @param depth maximum depth to follow references when creating the entity value map
   */
  public void bind(Entity entity, int depth) {
    var bindings = context.getBindings("js");
    Value magmaScript = bindings.getMember(KEY_MAGMA_SCRIPT);
    Value dollarFunction = magmaScript.getMember(KEY_DOLLAR);
    var entityMap = toScriptEngineValueMap(context, entity, depth);
    Value boundDollar = dollarFunction.invokeMember(BIND, entityMap);
    bindings.putMember(KEY_DOLLAR, boundDollar);
  }

  /**
   * Convert entity to a JavaScript object. Adds "_idValue" as a special key to every level for
   * quick access to the id value of an entity.
   *
   * @param entity The entity to be flattened, should start with non null entity
   * @param depth Represents the number of reference levels being added to the JavaScript object
   * @return A JavaScript object in Tree form, containing entities and there references
   */
  private Object toScriptEngineValueMap(Context context, Entity entity, int depth) {
    if (entity != null) {
      Object idValue =
          toScriptEngineValue(context, entity, entity.getEntityType().getIdAttribute(), 0);
      if (depth == 0) {
        return idValue;
      } else {
        Map<String, Object> map = Maps.newHashMap();
        entity
            .getEntityType()
            .getAtomicAttributes()
            .forEach(
                attr -> map.put(attr.getName(), toScriptEngineValue(context, entity, attr, depth)));
        map.put(KEY_ID_VALUE, idValue);
        return ProxyObject.fromMap(map);
      }
    } else {
      return null;
    }
  }

  private Object toScriptEngineValue(Context context, Entity entity, Attribute attr, int depth) {
    Object value = null;

    String attrName = attr.getName();
    AttributeType attrType = attr.getDataType();
    switch (attrType) {
      case BOOL:
        value = entity.getBoolean(attrName);
        break;
      case CATEGORICAL:
      case FILE:
      case XREF:
        Entity xrefEntity = entity.getEntity(attrName);
        value = toScriptEngineValueMap(context, xrefEntity, depth - 1);
        break;
      case CATEGORICAL_MREF:
      case MREF:
      case ONE_TO_MANY:
        var values =
            Streams.stream(entity.getEntities(attrName))
                .map(mrefEntity -> toScriptEngineValueMap(context, mrefEntity, depth - 1))
                .collect(toList());
        value = ProxyArray.fromList(values);
        break;
      case DATE:
        LocalDate localDate = entity.getLocalDate(attrName);
        if (localDate != null) {
          value = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        break;
      case DATE_TIME:
        Instant instant = entity.getInstant(attrName);
        if (instant != null) {
          value = instant.toEpochMilli();
        }
        break;
      case DECIMAL:
        value = entity.getDouble(attrName);
        break;
      case EMAIL:
      case ENUM:
      case HTML:
      case HYPERLINK:
      case SCRIPT:
      case STRING:
      case TEXT:
        value = entity.getString(attrName);
        break;
      case INT:
        value = entity.getInt(attrName);
        break;
      case LONG:
        value = entity.getLong(attrName);
        break;
      case COMPOUND:
        throw new IllegalAttributeTypeException(attrType);
      default:
        throw new UnexpectedEnumException(attrType);
    }
    return value;
  }

  void enter() {
    context.enter();
  }

  void leave() {
    context.leave();
  }

  void close() {
    context.close();
  }
}
