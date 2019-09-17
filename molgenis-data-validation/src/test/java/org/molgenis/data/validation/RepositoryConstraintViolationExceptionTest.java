package org.molgenis.data.validation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

public class RepositoryConstraintViolationExceptionTest {
  @Test
  public void getObjectName() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    exception.getObjectName();
    verify(entityErrors).getObjectName();
  }

  @Test
  public void setNestedPath() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    exception.setNestedPath("test");
    verify(entityErrors).setNestedPath("test");
  }

  @Test
  public void getNestedPath() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    when(entityErrors.getNestedPath()).thenReturn("nestedPath");
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertEquals(exception.getNestedPath(), "nestedPath");
  }

  @Test
  public void pushNestedPath() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    exception.pushNestedPath("test");
    verify(entityErrors).pushNestedPath("test");
  }

  @Test
  public void popNestedPath() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    exception.popNestedPath();
    verify(entityErrors).popNestedPath();
  }

  @Test
  public void testReject1() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    exception.reject("test");
    verify(entityErrors).reject("test");
  }

  @Test
  public void testReject2() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    exception.reject("test", "message");
    verify(entityErrors).reject("test", "message");
  }

  @Test
  public void testReject3() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    Object[] objectArray = new Object[] {};
    exception.reject("test", objectArray, "message");
    verify(entityErrors).reject("test", objectArray, "message");
  }

  @Test
  public void testRejectValue1() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    exception.rejectValue("value", "test");
    verify(entityErrors).rejectValue("value", "test");
  }

  @Test
  public void testRejectValue2() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    exception.rejectValue("value", "test", "message");
    verify(entityErrors).rejectValue("value", "test", "message");
  }

  @Test
  public void testRejectValue3() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    Object[] objectArray = new Object[] {};
    exception.rejectValue("value", "test", objectArray, "message");
    verify(entityErrors).rejectValue("value", "test", objectArray, "message");
  }

  @Test
  public void addAllErrors() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);
    Errors errors = mock(Errors.class);

    exception.addAllErrors(errors);
    verify(entityErrors).addAllErrors(errors);
  }

  @Test
  public void hasErrors() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    when(entityErrors.hasErrors()).thenReturn(true);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertTrue(exception.hasErrors());
  }

  @Test
  public void getErrorCount() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    when(entityErrors.getErrorCount()).thenReturn(3);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertEquals(exception.getErrorCount(), 3);
  }

  @Test
  public void getAllErrors() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    List<ObjectError> list = new ArrayList<>();
    list.add(new ObjectError("name1", "message1"));
    list.add(new ObjectError("name2", "message2"));
    when(entityErrors.getAllErrors()).thenReturn(list);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertEquals(exception.getAllErrors(), list);
  }

  @Test
  public void hasGlobalErrors() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    when(entityErrors.hasGlobalErrors()).thenReturn(true);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertTrue(exception.hasGlobalErrors());
  }

  @Test
  public void getGlobalErrorCount() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    when(entityErrors.getGlobalErrorCount()).thenReturn(6);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertEquals(exception.getGlobalErrorCount(), 6);
  }

  @Test
  public void getGlobalErrors() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    List<ObjectError> list = new ArrayList<>();
    list.add(new ObjectError("name1", "message1"));
    list.add(new ObjectError("name2", "message2"));
    when(entityErrors.getGlobalErrors()).thenReturn(list);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertEquals(exception.getGlobalErrors(), list);
  }

  @Test
  public void getGlobalError() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    ObjectError global1 = new ObjectError("name1", "message1");
    when(entityErrors.getGlobalError()).thenReturn(global1);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertEquals(exception.getGlobalError(), global1);
  }

  @Test
  public void hasFieldErrors() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    when(entityErrors.hasFieldErrors()).thenReturn(true);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertTrue(exception.hasFieldErrors());
  }

  @Test
  public void getFieldErrorCount() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    when(entityErrors.getFieldErrorCount()).thenReturn(6);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertEquals(exception.getFieldErrorCount(), 6);
  }

  @Test
  public void getFieldErrors() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    List<FieldError> list = new ArrayList<>();
    list.add(new FieldError("name1", "field1", "message1"));
    list.add(new FieldError("name2", "field2", "message2"));
    when(entityErrors.getFieldErrors()).thenReturn(list);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertEquals(exception.getFieldErrors(), list);
  }

  @Test
  public void getFieldError() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    FieldError fieldError = new FieldError("name1", "field", "message1");
    when(entityErrors.getFieldError()).thenReturn(fieldError);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertEquals(exception.getFieldError(), fieldError);
  }

  @Test
  public void testHasFieldErrors() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    when(entityErrors.hasFieldErrors("field")).thenReturn(true);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertTrue(exception.hasFieldErrors("field"));
  }

  @Test
  public void testGetFieldErrorCount() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    when(entityErrors.getFieldErrorCount("field")).thenReturn(6);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertEquals(exception.getFieldErrorCount("field"), 6);
  }

  @Test
  public void testGetFieldErrors() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    List<FieldError> list = new ArrayList<>();
    list.add(new FieldError("name1", "field1", "message1"));
    list.add(new FieldError("name2", "field2", "message2"));
    when(entityErrors.getFieldErrors("field")).thenReturn(list);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertEquals(exception.getFieldErrors("field"), list);
  }

  @Test
  public void testGetFieldError() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    FieldError fieldError = new FieldError("name1", "field", "message1");
    when(entityErrors.getFieldError("field")).thenReturn(fieldError);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertEquals(exception.getFieldError("field"), fieldError);
  }

  @Test
  public void getFieldValue() {
    EntityErrors entityErrors = mock(EntityErrors.class);
    when(entityErrors.getFieldValue("field")).thenReturn("value");
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertEquals(exception.getFieldValue("field"), "value");
  }

  @Test
  public void getFieldType() {
    Class stringClass = String.class;
    EntityErrors entityErrors = mock(EntityErrors.class);
    when(entityErrors.getFieldType("field")).thenReturn(stringClass);
    RepositoryConstraintViolationException exception =
        new RepositoryConstraintViolationException(entityErrors);

    assertEquals(exception.getFieldType("field"), stringClass);
  }
}
