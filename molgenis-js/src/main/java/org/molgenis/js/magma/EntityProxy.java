package org.molgenis.js.magma;

import static com.google.common.collect.Streams.concat;
import static com.google.common.collect.Streams.stream;
import static java.time.ZoneId.systemDefault;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.js.magma.JsMagmaScriptContext.KEY_ID_VALUE;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;

/** Proxies an Entity instance in a guest language. */
public class EntityProxy implements ProxyObject {

  private final Entity entity;

  public EntityProxy(Entity entity) {
    this.entity = entity;
  }

  @Override
  public Object getMember(String key) {
    var value = KEY_ID_VALUE.equals(key) ? entity.getIdValue() : entity.get(key);
    return toGraalValue(value);
  }

  @Override
  public Object getMemberKeys() {
    List<Object> result =
        concat(
                stream(entity.getEntityType().getAtomicAttributes()).map(Attribute::getName),
                Stream.of(KEY_ID_VALUE))
            .collect(toList());
    return ProxyArray.fromList(result);
  }

  @Override
  public boolean hasMember(String key) {
    if (KEY_ID_VALUE.equals(key)) {
      return true;
    }
    return Optional.ofNullable(entity.getEntityType().getAttribute(key))
        .map(Attribute::getDataType)
        .filter(attributeType -> attributeType != COMPOUND)
        .isPresent();
  }

  @Override
  public void putMember(String key, Value value) {
    throw new UnsupportedOperationException("Entity proxies are read-only");
  }

  static Object toGraalValue(Object o) {
    if (o instanceof Entity) return new EntityProxy((Entity) o);
    if (o instanceof Iterable) {
      List<Object> proxies = stream((Iterable<Entity>) o).map(EntityProxy::new).collect(toList());
      return ProxyArray.fromList(proxies);
    }
    if (o instanceof Instant) {
      return ((Instant) o).toEpochMilli();
    }
    if (o instanceof LocalDate) {
      return ((LocalDate) o).atStartOfDay(systemDefault()).toInstant().toEpochMilli();
    }
    return o;
  }
}
