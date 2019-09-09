package org.molgenis.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.molgenis.web.ErrorMessageResponse.ErrorMessage;

class ErrorMessageResponseTest {

  @Test
  void ErrorMessageResponse() {
    ErrorMessageResponse errorMessageResponse = new ErrorMessageResponse();
    assertEquals(errorMessageResponse.getErrors(), Collections.emptyList());
  }

  @Test
  void ErrorMessageResponseErrorMessage() {
    ErrorMessage errorMessage = new ErrorMessage("message");
    ErrorMessageResponse errorMessageResponse = new ErrorMessageResponse(errorMessage);
    assertEquals(errorMessageResponse.getErrors(), Collections.singletonList(errorMessage));
  }

  @Test
  void ErrorMessageResponseListErrorMessage() {
    List<ErrorMessage> errorMessages =
        Arrays.asList(new ErrorMessage("message1"), new ErrorMessage("message2"));
    ErrorMessageResponse errorMessageResponse = new ErrorMessageResponse(errorMessages);
    assertEquals(errorMessageResponse.getErrors(), errorMessages);
  }

  @Test
  void addErrorMessage() {
    ErrorMessageResponse errorMessageResponse = new ErrorMessageResponse();
    ErrorMessage errorMessage1 = new ErrorMessage("message1");
    ErrorMessage errorMessage2 = new ErrorMessage("message2");
    errorMessageResponse.addErrorMessage(errorMessage1);
    errorMessageResponse.addErrorMessage(errorMessage2);
    assertEquals(
        errorMessageResponse.getErrors(),
        Arrays.asList(new ErrorMessage("message1"), new ErrorMessage("message2")));
  }

  @Test
  void addErrorMessages() {
    ErrorMessage errorMessage1 = new ErrorMessage("message1");
    ErrorMessage errorMessage2 = new ErrorMessage("message2");
    List<ErrorMessage> errorMessages1 = Arrays.asList(errorMessage1, errorMessage2);
    ErrorMessage errorMessage3 = new ErrorMessage("message3");
    ErrorMessage errorMessage4 = new ErrorMessage("message4");
    List<ErrorMessage> errorMessages2 = Arrays.asList(errorMessage3, errorMessage4);
    ErrorMessageResponse errorMessageResponse = new ErrorMessageResponse();
    errorMessageResponse.addErrorMessages(errorMessages1);
    errorMessageResponse.addErrorMessages(errorMessages2);
    assertEquals(
        errorMessageResponse.getErrors(),
        Arrays.asList(errorMessage1, errorMessage2, errorMessage3, errorMessage4));
  }
}
