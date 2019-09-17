package org.molgenis.web;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.molgenis.web.ErrorMessageResponse.ErrorMessage;

class ErrorMessageResponseTest {

  @Test
  void ErrorMessageResponse() {
    ErrorMessageResponse errorMessageResponse = new ErrorMessageResponse();
    assertEquals(emptyList(), errorMessageResponse.getErrors());
  }

  @Test
  void ErrorMessageResponseErrorMessage() {
    ErrorMessage errorMessage = new ErrorMessage("message");
    ErrorMessageResponse errorMessageResponse = new ErrorMessageResponse(errorMessage);
    assertEquals(singletonList(errorMessage), errorMessageResponse.getErrors());
  }

  @Test
  void ErrorMessageResponseListErrorMessage() {
    List<ErrorMessage> errorMessages =
        Arrays.asList(new ErrorMessage("message1"), new ErrorMessage("message2"));
    ErrorMessageResponse errorMessageResponse = new ErrorMessageResponse(errorMessages);
    assertEquals(errorMessages, errorMessageResponse.getErrors());
  }

  @Test
  void addErrorMessage() {
    ErrorMessageResponse errorMessageResponse = new ErrorMessageResponse();
    ErrorMessage errorMessage1 = new ErrorMessage("message1");
    ErrorMessage errorMessage2 = new ErrorMessage("message2");
    errorMessageResponse.addErrorMessage(errorMessage1);
    errorMessageResponse.addErrorMessage(errorMessage2);
    assertEquals(
        asList(new ErrorMessage("message1"), new ErrorMessage("message2")),
        errorMessageResponse.getErrors());
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
        asList(errorMessage1, errorMessage2, errorMessage3, errorMessage4),
        errorMessageResponse.getErrors());
  }
}
