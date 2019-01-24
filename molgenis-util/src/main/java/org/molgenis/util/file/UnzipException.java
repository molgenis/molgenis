package org.molgenis.util.file;

/** Thrown when unzipping a file fails. */
public class UnzipException extends RuntimeException {
  public UnzipException(Exception cause) {
    super(cause);
  }
}
