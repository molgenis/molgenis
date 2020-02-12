package org.molgenis.data;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

/**
 * Thrown when a data or metadata element is invalid.
 *
 * @see DuplicateValueException
 * @see EntityTypeReferencedException
 * @see InvalidValueTypeException
 * @see ListValueAlreadyExistsException
 * @see ReadonlyValueException
 * @see UnknownEnumValueException
 * @see UnknownValueReferenceException
 * @see ValueAlreadyExistsException
 * @see ValueLengthExceededException
 * @see ValueReferencedException
 * @see ValueRequiredException
 */
@SuppressWarnings("java:MaximumInheritanceDepth")
public abstract class DataConstraintViolationException extends ErrorCodedDataAccessException {
  protected DataConstraintViolationException(
      String errorCode, @Nullable @CheckForNull Throwable cause) {
    super(errorCode, cause);
  }
}
