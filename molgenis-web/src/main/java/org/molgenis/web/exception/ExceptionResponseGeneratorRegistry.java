package org.molgenis.web.exception;

interface ExceptionResponseGeneratorRegistry {
  void registerExceptionResponseGenerator(ExceptionResponseGenerator exceptionResponseGenerator);

  ExceptionResponseGenerator getExceptionResponseGenerator(ExceptionResponseType type);
}
