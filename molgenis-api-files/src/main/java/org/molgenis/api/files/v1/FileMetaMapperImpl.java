package org.molgenis.api.files.v1;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import org.molgenis.api.files.v1.model.FileCreateResponse;
import org.molgenis.api.files.v1.model.FilesCreateResponse;
import org.molgenis.data.file.model.FileMeta;
import org.springframework.stereotype.Component;

@Component
public class FileMetaMapperImpl implements FileMetaMapper {
  @Override
  public FilesCreateResponse toFilesUploadResponse(Collection<FileMeta> fileMetaStream) {
    List<FileCreateResponse> fileUploadResponses =
        fileMetaStream.stream().map(this::toFileUploadResponse).collect(toList());
    return FilesCreateResponse.create(fileUploadResponses);
  }

  private FileCreateResponse toFileUploadResponse(FileMeta fileMeta) {
    return FileCreateResponse.builder()
        .setId(fileMeta.getId())
        .setFilename(fileMeta.getFilename())
        .setContentType(fileMeta.getContentType())
        .setSize(fileMeta.getSize())
        .setUrl(fileMeta.getUrl())
        .build();
  }
}
