package org.molgenis.data.validation;

import static java.util.Objects.requireNonNull;

import java.util.List;
import javax.annotation.Nonnull;
import org.springframework.lang.Nullable;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

public class RepositoryConstraintViolationException extends RuntimeException implements Errors {
  private final EntityErrors entityErrors;

  public RepositoryConstraintViolationException(EntityErrors entityErrors) {
    this.entityErrors = requireNonNull(entityErrors);
  }

  @Override
  public @Nonnull String getObjectName() {
    return entityErrors.getObjectName();
  }

  @Override
  public void setNestedPath(@Nonnull String nestedPath) {
    entityErrors.setNestedPath(nestedPath);
  }

  @Override
  public @Nonnull String getNestedPath() {
    return entityErrors.getNestedPath();
  }

  @Override
  public void pushNestedPath(@Nonnull String subPath) {
    entityErrors.pushNestedPath(subPath);
  }

  @Override
  public void popNestedPath() {
    entityErrors.popNestedPath();
  }

  @Override
  public void reject(@Nonnull String errorCode) {
    entityErrors.reject(errorCode);
  }

  @Override
  public void reject(@Nonnull String errorCode, @Nonnull String defaultMessage) {
    entityErrors.reject(errorCode, defaultMessage);
  }

  @Override
  public void reject(@Nonnull String errorCode, Object[] errorArgs, String defaultMessage) {
    entityErrors.reject(errorCode, errorArgs, defaultMessage);
  }

  @Override
  public void rejectValue(String field, @Nonnull String errorCode) {
    entityErrors.rejectValue(field, errorCode);
  }

  @Override
  public void rejectValue(String field, @Nonnull String errorCode, @Nonnull String defaultMessage) {
    entityErrors.rejectValue(field, errorCode, defaultMessage);
  }

  @Override
  public void rejectValue(
      String field, @Nonnull String errorCode, Object[] errorArgs, String defaultMessage) {
    entityErrors.rejectValue(field, errorCode, errorArgs, defaultMessage);
  }

  @Override
  public void addAllErrors(@Nonnull Errors errors) {
    this.entityErrors.addAllErrors(errors);
  }

  @Override
  public boolean hasErrors() {
    return entityErrors.hasErrors();
  }

  @Override
  public int getErrorCount() {
    return entityErrors.getErrorCount();
  }

  @Override
  public @Nonnull List<ObjectError> getAllErrors() {
    return entityErrors.getAllErrors();
  }

  @Override
  public boolean hasGlobalErrors() {
    return entityErrors.hasGlobalErrors();
  }

  @Override
  public int getGlobalErrorCount() {
    return entityErrors.getGlobalErrorCount();
  }

  @Override
  public @Nonnull List<ObjectError> getGlobalErrors() {
    return entityErrors.getGlobalErrors();
  }

  @Override
  @Nullable
  public ObjectError getGlobalError() {
    return entityErrors.getGlobalError();
  }

  @Override
  public boolean hasFieldErrors() {
    return entityErrors.hasFieldErrors();
  }

  @Override
  public int getFieldErrorCount() {
    return entityErrors.getFieldErrorCount();
  }

  @Override
  public @Nonnull List<FieldError> getFieldErrors() {
    return entityErrors.getFieldErrors();
  }

  @Override
  @Nullable
  public FieldError getFieldError() {
    return entityErrors.getFieldError();
  }

  @Override
  public boolean hasFieldErrors(@Nonnull String field) {
    return entityErrors.hasFieldErrors(field);
  }

  @Override
  public int getFieldErrorCount(@Nonnull String field) {
    return entityErrors.getFieldErrorCount(field);
  }

  @Override
  public @Nonnull List<FieldError> getFieldErrors(@Nonnull String field) {
    return entityErrors.getFieldErrors(field);
  }

  @Override
  @Nullable
  public FieldError getFieldError(@Nonnull String field) {
    return entityErrors.getFieldError(field);
  }

  @Override
  @Nullable
  public Object getFieldValue(@Nonnull String field) {
    return entityErrors.getFieldValue(field);
  }

  @Override
  @Nullable
  public Class<?> getFieldType(@Nonnull String field) {
    return entityErrors.getFieldType(field);
  }
}
