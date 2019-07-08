package org.molgenis.web.exception;

import org.springframework.http.HttpStatus;

interface ExceptionResponseGenerator<T> {

  ExceptionResponseType getType();

  T createExceptionResponse(Exception exception, HttpStatus httpStatus, boolean isLogStackTrace);
}
