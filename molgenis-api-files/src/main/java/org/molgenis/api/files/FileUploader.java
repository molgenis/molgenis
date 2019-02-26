package org.molgenis.api.files;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import org.molgenis.data.file.model.FileMeta;
import org.springframework.util.MimeType;

public interface FileUploader {
  /**
   * @return non-empty collection of MIME types that an uploader can handle.
   */
  Collection<MimeType> getSupportedMimeTypes();

  /**
   * @throws IllegalArgumentException if request does not contain exactly one request
   * @throws java.io.UncheckedIOException if an error occured reading or writing data
   */
  FileMeta upload(HttpServletRequest httpServletRequest);
}
