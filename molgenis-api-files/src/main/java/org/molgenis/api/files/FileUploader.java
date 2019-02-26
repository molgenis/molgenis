package org.molgenis.api.files;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import org.molgenis.data.file.model.FileMeta;
import org.springframework.util.MimeType;

public interface FileUploader {
  Collection<MimeType> getSupportedMimeTypes();

  FileMeta upload(HttpServletRequest httpServletRequest);
}
