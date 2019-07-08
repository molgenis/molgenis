package org.molgenis.web.exception;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Locale;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.web.ErrorMessageResponse;
import org.molgenis.web.ErrorMessageResponse.ErrorMessage;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Component
class ErrorMessagesResponseGenerator
    implements ExceptionResponseGenerator<ResponseEntity<ErrorMessageResponse>> {

  @Override
  public ExceptionResponseType getType() {
    return ExceptionResponseType.ERROR_MESSAGES;
  }

  @Override
  public ResponseEntity<ErrorMessageResponse> createExceptionResponse(
      Exception exception, HttpStatus httpStatus, boolean isLogStackTrace) {
    String errorCode = ExceptionUtils.getErrorCode(exception).orElse(null);
    ErrorMessageResponse errorMessageResponse = createErrorMessageResponse(exception, errorCode);
    return new ResponseEntity<>(errorMessageResponse, httpStatus);
  }

  static ErrorMessageResponse createErrorMessageResponse(Exception e, String errorCode) {
    if (e instanceof Errors) {
      return getErrorMessageResponse((Errors) e);
    } else if (e instanceof MethodArgumentNotValidException) {
      return getErrorMessageResponse(((MethodArgumentNotValidException) e).getBindingResult());
    } else {
      return new ErrorMessageResponse(new ErrorMessage(e.getLocalizedMessage(), errorCode));
    }
  }

  private static ErrorMessageResponse getErrorMessageResponse(Errors errors) {
    List<ErrorMessage> errorMessages = newArrayList();
    Locale locale = LocaleContextHolder.getLocale();
    MessageSource messageSource = MessageSourceHolder.getMessageSource();
    for (ObjectError objectError : errors.getGlobalErrors()) {
      String message =
          messageSource.getMessage(
              "org.molgenis.web.exception.ObjectError",
              new Object[] {objectError.getObjectName(), objectError},
              locale);
      errorMessages.add(new ErrorMessage(message, objectError.getCode()));
    }
    for (FieldError fieldError : errors.getFieldErrors()) {
      String message =
          messageSource.getMessage(
              "org.molgenis.web.exception.FieldError",
              new Object[] {fieldError.getField(), fieldError.getObjectName(), fieldError},
              locale);
      errorMessages.add(new ErrorMessage(message, fieldError.getCode()));
    }
    return new ErrorMessageResponse(errorMessages);
  }
}
