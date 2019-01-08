package org.molgenis.data.meta;

import static java.lang.String.format;
import static org.molgenis.data.meta.SystemEntityTestUtils.getReturnTypes;
import static org.molgenis.data.meta.SystemEntityTestUtils.getTestValue;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.testng.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityFactory;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.util.EntityTypeUtils;
import org.molgenis.data.util.PackageUtils;
import org.molgenis.util.Pair;

public abstract class AbstractSystemEntityTest extends AbstractMolgenisSpringTest {
  private static final String[] GETTER_PREFIXES = new String[] {"get", "is"};
  private static final String[] SETTER_PREFIXES = new String[] {"set"};

  @SuppressWarnings("unused")
  protected abstract void testSystemEntity();

  protected void internalTestAttributes(
      SystemEntityType systemEntityType,
      Class entityClass,
      EntityFactory factory,
      Map<String, Pair<Class, Object>> overrideReturnTypes,
      List<String> excludedAttributes) {

    List<Method> methods = Arrays.asList(entityClass.getMethods());

    testMetadataPackage(systemEntityType);
    StreamSupport.stream(systemEntityType.getAtomicAttributes().spliterator(), false)
        .filter(attr -> !excludedAttributes.contains(attr.getName()))
        .forEach(attr -> testAttributes(attr, methods, factory, overrideReturnTypes));
  }

  private void testAttributes(
      Attribute attr,
      List<Method> methods,
      EntityFactory factory,
      Map<String, Pair<Class, Object>> overrideReturnTypes) {
    testGettersAndSettersExist(attr, methods);
    testGetterReturnType(attr, methods, overrideReturnTypes);
    testSetterArgumentType(attr, methods, overrideReturnTypes);
    testGetterAndSetterMatch(attr, methods, overrideReturnTypes, factory);
  }

  private void testGettersAndSettersExist(Attribute attr, List<Method> methods) {
    testGetterAndSetterExist(attr, methods);
  }

  private void testSetterArgumentType(
      Attribute attr, List<Method> methods, Map<String, Pair<Class, Object>> overrideReturnTypes) {
    Method setter = getMethod(attr.getName(), SETTER_PREFIXES, 1, methods);
    if (setter != null) {
      Class[] expectedTypes = getExpectedJavaType(attr, overrideReturnTypes);
      Class actualType = setter.getParameterTypes()[0];
      if (actualType == Optional.class) {
        actualType = setter.getGenericParameterTypes()[0].getClass();
      }
      testTypesMatch(attr, actualType, expectedTypes, setter.getName());
    }
  }

  private void testGetterAndSetterMatch(
      Attribute attr,
      List<Method> methods,
      Map<String, Pair<Class, Object>> overrideReturnTypes,
      EntityFactory factory) {
    Method getter = getMethod(attr.getName(), GETTER_PREFIXES, 0, methods);
    Method setter = getMethod(attr.getName(), SETTER_PREFIXES, 1, methods);
    if (getter != null && setter != null) {
      Class returnType = setter.getParameterTypes()[0];
      //noinspection unchecked
      Entity entity = factory.create("test");
      Object value;
      if (!overrideReturnTypes.containsKey(attr.getName())) {
        ParameterizedType parameterizedType = null;
        if (returnType.equals(Iterable.class)) {
          parameterizedType = (ParameterizedType) getter.getGenericReturnType();
        }
        value = getTestValue(returnType, parameterizedType);
      } else {
        value = overrideReturnTypes.get(attr.getName()).getB();
      }
      try {
        invokeSetter(entity, setter, value);
        invokeAndTestGetters(attr, getter, entity, value);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void testMetadataPackage(SystemEntityType metadata) {
    Package pack = metadata.getPackage();
    if (pack != null && SystemPackage.class.isAssignableFrom(pack.getClass())) {
      SystemPackage systemPackage = (SystemPackage) pack;
      systemPackage.init();
      if (!PackageUtils.isSystemPackage(systemPackage)) {
        fail(
            String.format(
                "SystemEntityTypes should be placed in (a subpackage of) '%s'", PACKAGE_SYSTEM));
      }
    } else {
      fail(
          String.format("SystemEntityTypes should extend %s", SystemPackage.class.getSimpleName()));
    }
  }

  private void testGetterAndSetterExist(Attribute attr, List<Method> methods) {
    Method getter = getMethod(attr.getName(), GETTER_PREFIXES, 0, methods);
    Method setter = getMethod(attr.getName(), SETTER_PREFIXES, 1, methods);

    testCompoundSetter(getter, GETTER_PREFIXES, attr);
    testCompoundSetter(setter, SETTER_PREFIXES, attr);
  }

  private void invokeAndTestGetters(Attribute attr, Method getter, Entity entity, Object value)
      throws IllegalAccessException, InvocationTargetException {
    if (getter.invoke(entity) == null) {
      fail(format("getter returns null value for attr: %s", attr.getName()));
    }
    if (!valueEquals(getter, entity, value)) {
      fail(
          format(
              "getter returns different value than set via setter for attr: %s", attr.getName()));
    }
    if (entity.get(attr.getName()) == null) {
      fail(
          format(
              "generic get(%s) returns null value, value for the attribute is not set by the setter",
              attr.getName()));
    }
  }

  private boolean valueEquals(Method getter, Entity entity, Object expected)
      throws InvocationTargetException, IllegalAccessException {
    Object actual = getter.invoke(entity);
    if (getter.getReturnType() == Optional.class) {
      Optional actualOptional = (Optional) actual;
      Object actualObject = actualOptional.isPresent() ? actualOptional.get() : null;
      return actualObject != null && actualObject.equals(expected);
    } else {
      return actual.equals(expected);
    }
  }

  private void invokeSetter(Entity entity, Method setter, Object value)
      throws IllegalAccessException, InvocationTargetException {
    try {
      setter.invoke(entity, value);
    } catch (IllegalArgumentException e) {
      fail(
          format(
              "'%s' expected argument of type testReturnType '%s' but was '%s'",
              setter.getName(),
              setter.getParameterTypes()[0].getSimpleName(),
              value.getClass().getSimpleName()));
    }
  }

  private void testGetterReturnType(
      Attribute attr, List<Method> methods, Map<String, Pair<Class, Object>> overrideReturnTypes) {
    Method getter = getMethod(attr.getName(), GETTER_PREFIXES, 0, methods);
    if (getter != null) {
      Class actualType = getter.getReturnType();
      if (actualType == Optional.class) {
        actualType =
            (Class) ((ParameterizedType) getter.getGenericReturnType()).getActualTypeArguments()[0];
      }
      Class[] expectedTypes = getExpectedJavaType(attr, overrideReturnTypes);
      testTypesMatch(attr, actualType, expectedTypes, getter.getName());
    }
  }

  private void testTypesMatch(
      Attribute attr, Class actualType, Class[] expectedTypes, String methodName) {
    if (EntityTypeUtils.isSingleReferenceType(attr)) {
      if (!Entity.class.isAssignableFrom(actualType)) {
        fail(
            format(
                "%s has type '%s' but expected '%s' for attribute '%s'",
                methodName,
                actualType.getSimpleName(),
                Arrays.toString(expectedTypes),
                attr.getName()));
      }
    } else if (!ArrayUtils.contains(expectedTypes, actualType)) {
      fail(
          format(
              "%s has type '%s' but expected '%s' for attribute '%s'",
              methodName,
              actualType.getSimpleName(),
              Arrays.toString(expectedTypes),
              attr.getName()));
    }
  }

  private Class[] getExpectedJavaType(
      Attribute attr, Map<String, Pair<Class, Object>> overrideReturnTypes) {
    Class[] expectedReturnTypes;
    if (overrideReturnTypes.containsKey(attr.getName())) {
      expectedReturnTypes = new Class[] {overrideReturnTypes.get(attr.getName()).getA()};
    } else {
      expectedReturnTypes = getReturnTypes(attr);
    }
    return expectedReturnTypes;
  }

  private void testCompoundSetter(Method method, String[] prefixes, Attribute attr) {
    if (method != null && attr.getDataType() == AttributeType.COMPOUND) {
      fail(
          format(
              "Method '%s' for %s should not exist since this is a attribute of type '%s'",
              Arrays.toString(prefixes), attr.getName(), AttributeType.COMPOUND));
    }
  }

  @Nullable
  private Method getMethod(
      String name, String[] prefixes, int nrOfArguments, List<Method> methods) {
    for (Method method : methods) {
      if (isMethodForAttribute(method, prefixes, name, nrOfArguments)) {
        return method;
      }
    }
    return null;
  }

  private boolean isMethodForAttribute(
      Method method, String[] prefixes, String name, int nrOfArguments) {
    for (String prefix : prefixes) {
      if (method.getName().equalsIgnoreCase(prefix + name)
          && method.getParameterCount() == nrOfArguments) {
        return true;
      }
    }
    return false;
  }
}
