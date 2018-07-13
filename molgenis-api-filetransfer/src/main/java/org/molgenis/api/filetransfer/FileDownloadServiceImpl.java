package org.molgenis.api.filetransfer;

import com.google.common.io.ByteStreams;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.blob.BlobStore;
import org.molgenis.data.file.model.FileMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.file.model.FileMetaMetaData.FILE_META;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@Component
public class FileDownloadServiceImpl implements FileDownloadService
{
	private static final Logger LOG = LoggerFactory.getLogger(FileDownloadServiceImpl.class);

	private final DataService dataService;
	private final BlobStore blobStore;

	FileDownloadServiceImpl(DataService dataService, BlobStore blobStore)
	{
		this.dataService = requireNonNull(dataService);
		this.blobStore = requireNonNull(blobStore);
	}

	@Override
	public ResponseEntity<StreamingResponseBody> download(String fileId)
	{
		FileMeta fileMeta = dataService.findOneById(FILE_META, fileId, FileMeta.class);
		if (fileMeta == null)
		{
			throw new UnknownEntityException(FILE_META, fileId);
		}

		ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
		builder.header(CONTENT_TYPE, fileMeta.getContentType());
		builder.header(CONTENT_DISPOSITION, "attachment; filename=\"" + fileMeta.getFilename() + "\"");

		Long contentLength = fileMeta.getSize();
		if (contentLength != null)
		{
			builder.contentLength(contentLength);
		}

		return builder.body(outputStream ->
		{
			try (ReadableByteChannel fromChannel = blobStore.newChannel(fileId))
			{
				ByteStreams.copy(fromChannel, Channels.newChannel(outputStream));
			}
		});
	}
}