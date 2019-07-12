package org.molgenis.web.exception;

enum ExceptionResponseType {
  MODEL_AND_VIEW,
  ERROR_MESSAGES,
  /** RFC (https://tools.ietf.org/html/rfc7807) */
  PROBLEM
}
