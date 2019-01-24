package org.molgenis.api.filetransfer;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartException;

@ControllerAdvice
class FileTransferExceptionHandlerAdvice {
  private static final Logger LOG =
      LoggerFactory.getLogger(FileTransferExceptionHandlerAdvice.class);

  @ExceptionHandler
  @ResponseStatus(BAD_REQUEST)
  void handleMultipartException(MultipartException e) {
    LOG.warn("", e);
  }

  @ExceptionHandler
  @ResponseStatus(INTERNAL_SERVER_ERROR)
  void handleRuntimeException(RuntimeException e) {
    LOG.error("", e);
  }

  @ExceptionHandler
  @ResponseStatus(INTERNAL_SERVER_ERROR)
  void handleFileUploadException(FileUploadException e) {
    LOG.error("", e);
  }
}
