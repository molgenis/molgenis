package org.molgenis.api.filetransfer;

import com.google.common.io.ByteStreams;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.molgenis.data.DataService;
import org.molgenis.data.blob.BlobStore;
import org.molgenis.data.blob.WritableBlobChannel;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import static java.nio.channels.Channels.newChannel;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.file.model.FileMetaMetaData.FILE_META;

@Service
class FileUploadServiceImpl implements FileUploadService
{
	private final BlobStore blobStore;
	private final FileMetaFactory fileMetaFactory;
	private final FileDownloadUriGenerator fileDownloadUriGenerator;
	private final DataService dataService;

	FileUploadServiceImpl(BlobStore blobStore, FileMetaFactory fileMetaFactory,
			FileDownloadUriGenerator fileDownloadUriGenerator, DataService dataService)
	{
		this.blobStore = requireNonNull(blobStore);
		this.fileMetaFactory = requireNonNull(fileMetaFactory);
		this.fileDownloadUriGenerator = requireNonNull(fileDownloadUriGenerator);
		this.dataService = requireNonNull(dataService);
	}

	@Transactional
	@Override
	public List<FileMeta> upload(FileItemIterator fileItemIterator)
	{
		List<FileMeta> fileMetaList = new ArrayList<>();
		try
		{
			while (fileItemIterator.hasNext())
			{
				FileMeta fileMeta = upload(fileItemIterator.next());
				fileMetaList.add(fileMeta);
			}
		}
		catch (FileUploadException e)
		{
			throw new UncheckedIOException(new IOException(e));
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
		return fileMetaList;
	}

	private FileMeta upload(FileItemStream fileItemStream) throws IOException
	{
		long nrBytes;
		ReadableByteChannel fromChannel = newChannel(fileItemStream.openStream());
		WritableBlobChannel writableBlobChannel = blobStore.newChannel();
		try (WritableByteChannel toChannel = writableBlobChannel.getWritableByteChannel())
		{
			nrBytes = ByteStreams.copy(fromChannel, toChannel);
		}

		FileMeta fileMeta = fileMetaFactory.create(writableBlobChannel.getBlobId());
		fileMeta.setFilename(fileItemStream.getName());
		fileMeta.setContentType(fileItemStream.getContentType());
		fileMeta.setSize(nrBytes);
		fileMeta.setUrl(fileDownloadUriGenerator.generateUri(fileMeta.getId()));
		dataService.add(FILE_META, fileMeta);

		return fileMeta;
	}
}
