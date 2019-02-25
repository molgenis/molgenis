package org.molgenis.data;

class EntityConstructionException extends RuntimeException {

  EntityConstructionException(Throwable cause) {
    super(cause);
  }

  EntityConstructionException(String message, Throwable cause) {
    super(message, cause);
  }
}
