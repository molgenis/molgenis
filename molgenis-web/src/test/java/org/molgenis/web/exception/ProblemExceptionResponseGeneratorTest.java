package org.molgenis.web.exception;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.web.exception.ExceptionResponseType.PROBLEM;
import static org.testng.Assert.assertEquals;

import java.net.URI;
import org.molgenis.i18n.CodedRuntimeException;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ProblemExceptionResponseGeneratorTest extends AbstractMockitoTest {
  private ProblemExceptionResponseGenerator problemExceptionResponseGenerator;

  @BeforeMethod
  public void setUpBeforeMethod() {
    problemExceptionResponseGenerator = new ProblemExceptionResponseGenerator();

    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  @Test
  public void testGetType() {
    assertEquals(problemExceptionResponseGenerator.getType(), PROBLEM);
  }

  @Test
  public void testCreateExceptionResponse() {
    Exception exception = mock(Exception.class);
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

    Problem problem =
        Problem.builder()
            .setType(URI.create("http://localhost/problem"))
            .setTitle("Bad Request")
            .setStatus(400)
            .build();
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
    ResponseEntity<Problem> responseEntity = new ResponseEntity<>(problem, httpHeaders, httpStatus);
    assertEquals(
        problemExceptionResponseGenerator.createExceptionResponse(exception, httpStatus, false),
        responseEntity);
  }

  @Test
  public void testCreateExceptionResponseErrorCoded() {
    CodedRuntimeException exception = mock(CodedRuntimeException.class);
    when(exception.getErrorCode()).thenReturn("A1");
    when(exception.getLocalizedMessage()).thenReturn("message always exposed");
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

    Problem problem =
        Problem.builder()
            .setType(URI.create("http://localhost/problem"))
            .setTitle("Bad Request")
            .setStatus(400)
            .setDetail("message always exposed")
            .setErrorCode("A1")
            .build();
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
    ResponseEntity<Problem> responseEntity = new ResponseEntity<>(problem, httpHeaders, httpStatus);
    assertEquals(
        problemExceptionResponseGenerator.createExceptionResponse(exception, httpStatus, false),
        responseEntity);
  }

  @Test
  public void testCreateExceptionResponseDevEnvironment() {
    Exception exception = mock(Exception.class);
    when(exception.getLocalizedMessage()).thenReturn("message only exposed in dev environment");
    when(exception.getStackTrace())
        .thenReturn(
            new StackTraceElement[] {
              new StackTraceElement("declaringClass", "methodName", "fileName", 0)
            });
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

    Problem problem =
        Problem.builder()
            .setType(URI.create("http://localhost/problem"))
            .setTitle("Bad Request")
            .setStatus(400)
            .setDetail("message only exposed in dev environment")
            .setStackTrace(singletonList("declaringClass.methodName(fileName:0)"))
            .build();
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
    ResponseEntity<Problem> responseEntity = new ResponseEntity<>(problem, httpHeaders, httpStatus);
    assertEquals(
        problemExceptionResponseGenerator.createExceptionResponse(exception, httpStatus, true),
        responseEntity);
  }
}
