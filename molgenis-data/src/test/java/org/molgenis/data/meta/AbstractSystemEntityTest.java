package org.molgenis.data.meta;

import static java.lang.String.format;
import static org.molgenis.data.meta.SystemEntityTestUtils.LANGUAGES;
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
import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityFactory;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.util.EntityTypeUtils;
import org.molgenis.util.Pair;

public abstract class AbstractSystemEntityTest extends AbstractMolgenisSpringTest {
  private static final String[] GETTER_PREFIXES = new String[] {"get", "is"};
  private static final String[] SETTER_PREFIXES = new String[] {"set"};

  public abstract void testSystemEntity();

  public void internalTestAttributes(
      SystemEntityType systemEntityType,
      Class entityClass,
      EntityFactory factory,
      Map<String, Pair<Class, Object>> overrideReturnTypes) {

    List<Method> methods = Arrays.asList(entityClass.getDeclaredMethods());

    testMetadataPackage(systemEntityType);
    testGettersAndSettersExist(systemEntityType, methods);

    testGettersReturnTypes(systemEntityType, methods, overrideReturnTypes);
    testSettersArgumentType(systemEntityType, methods, overrideReturnTypes);
    testGettersAndSettersMatch(systemEntityType, methods, overrideReturnTypes, factory);
  }

  private void testGettersAndSettersExist(SystemEntityType systemEntityType, List<Method> methods) {
    for (Attribute attr : systemEntityType.getAtomicAttributes()) {
      testGetterAndSetterExist(attr, methods);
    }
  }

  private void testGettersReturnTypes(
      SystemEntityType systemEntityType,
      List<Method> methods,
      Map<String, Pair<Class, Object>> overrideReturnTypes) {
    for (Attribute attr : systemEntityType.getAtomicAttributes()) {
      testGetterReturnType(attr, methods, overrideReturnTypes);
    }
  }

  protected void testSettersArgumentType(
      SystemEntityType systemEntityType,
      List<Method> methods,
      Map<String, Pair<Class, Object>> overrideReturnTypes) {
    for (Attribute attr : systemEntityType.getAtomicAttributes()) {
      testSetterArgumentType(attr, methods, overrideReturnTypes);
    }
  }

  private void testSetterArgumentType(
      Attribute attr, List<Method> methods, Map<String, Pair<Class, Object>> overrideReturnTypes) {
    Method setter = getMethod(attr.getName(), SETTER_PREFIXES, 1, methods);
    if (setter != null) {
      Class[] expectedTypes = getExpectedJavaType(attr, overrideReturnTypes);
      Class<?>[] actualType = setter.getParameterTypes();
      testTypesMatch(attr, actualType[0], expectedTypes, setter.getName());
    }
  }

  protected void testGettersAndSettersMatch(
      SystemEntityType systemEntityType,
      List<Method> methods,
      Map<String, Pair<Class, Object>> overrideReturnTypes,
      EntityFactory factory) {
    for (Attribute attr : systemEntityType.getAtomicAttributes()) {
      testGetterAndSetterMatch(attr, methods, overrideReturnTypes, factory);
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
      Class returnType = getter.getReturnType();
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
    if (metadata.getPackage() == null
        || !metadata.getPackage().getId().startsWith(PACKAGE_SYSTEM)) {
      fail("SystemEntityTypes should be placed in (a subpackage of)'" + PACKAGE_SYSTEM + "'");
    }
  }

  private void testGetterAndSetterExist(Attribute attr, List<Method> methods) {
    Method getter = getMethod(attr.getName(), GETTER_PREFIXES, 0, methods);
    Method setter = getMethod(attr.getName(), SETTER_PREFIXES, 1, methods);

    testMethodExist(getter, GETTER_PREFIXES, attr);
    testMethodExist(setter, SETTER_PREFIXES, attr);
  }

  private void invokeAndTestGetters(Attribute attr, Method getter, Entity entity, Object value)
      throws IllegalAccessException, InvocationTargetException {
    if (getter.invoke(entity) == null) {
      fail(format("getter returns null value for attr: %s", attr.getName()));
    }
    if (!getter.invoke(entity).equals(value)) {
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

  private boolean testMethodExist(Method method, String[] prefixes, Attribute attr) {
    if (method == null && attr.getDataType() != AttributeType.COMPOUND) {
      // replace with fail once we have i18n tests in place
      if (LANGUAGES.contains(
          attr.getName().substring(attr.getName().length() - 2, attr.getName().length()))) {
        logger.warn(
            format("Missing '%s' method for %s", Arrays.toString(prefixes), attr.getName()));
      } else {
        fail(format("Missing '%s' method for %s", Arrays.toString(prefixes), attr.getName()));
      }
      return false;
    } else if (method != null && attr.getDataType() == AttributeType.COMPOUND) {
      fail(
          format(
              "Method '%s' for %s should not exist since this is a attribute of type '%s'",
              Arrays.toString(prefixes), attr.getName(), AttributeType.COMPOUND));
    }
    return true;
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
