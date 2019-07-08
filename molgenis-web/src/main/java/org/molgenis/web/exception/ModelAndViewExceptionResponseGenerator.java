package org.molgenis.web.exception;

import java.util.HashMap;
import java.util.Map;
import org.molgenis.web.ErrorMessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
class ModelAndViewExceptionResponseGenerator implements ExceptionResponseGenerator<ModelAndView> {
  private static final String KEY_ERROR_MESSAGE_RESPONSE = "errorMessageResponse";
  private static final String KEY_HTTP_STATUS_CODE = "httpStatusCode";
  private static final String KEY_STACK_TRACE = "stackTrace";
  private static final String VIEW_EXCEPTION = "view-exception";

  @Override
  public ExceptionResponseType getType() {
    return ExceptionResponseType.MODEL_AND_VIEW;
  }

  @Override
  public ModelAndView createExceptionResponse(
      Exception exception, HttpStatus httpStatus, boolean isLogStackTrace) {
    String errorCode = ExceptionUtils.getErrorCode(exception).orElse(null);
    ErrorMessageResponse errorMessageResponse =
        ErrorMessagesResponseGenerator.createErrorMessageResponse(exception, errorCode);

    Map<String, Object> model = new HashMap<>();
    model.put(KEY_ERROR_MESSAGE_RESPONSE, errorMessageResponse);
    model.put(KEY_HTTP_STATUS_CODE, httpStatus.value());
    if (isLogStackTrace) {
      model.put(KEY_STACK_TRACE, exception.getStackTrace());
    }
    return new ModelAndView(VIEW_EXCEPTION, model, httpStatus);
  }
}
