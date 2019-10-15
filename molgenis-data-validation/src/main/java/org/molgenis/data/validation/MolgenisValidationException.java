package org.molgenis.data.validation;

import com.google.common.collect.Collections2;
import java.util.Collections;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.validation.ConstraintViolation;

/** @deprecated use class that extends from {@link CodedRuntimeException} */
@Deprecated
public class MolgenisValidationException extends MolgenisDataException {
  private static final long serialVersionUID = 1L;
  private final Set<ConstraintViolation> violations;

  public MolgenisValidationException(ConstraintViolation violation) {
    this(Collections.singleton(violation));
  }

  public MolgenisValidationException(Set<ConstraintViolation> violations) {
    this.violations = violations;
  }

  public Set<ConstraintViolation> getViolations() {
    return violations;
  }

  @Override
  public String getMessage() {
    if ((violations == null) || (violations.isEmpty())) return "Unknown validation exception.";

    return StringUtils.join(
        Collections2.transform(violations, ConstraintViolation::getMessage), '.');
  }
}
