package org.molgenis.api.filetransfer.v1;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.molgenis.api.filetransfer.FileDownloadService;
import org.molgenis.api.filetransfer.FileTransferApiNamespace;
import org.molgenis.api.filetransfer.FileUploadService;
import org.molgenis.api.filetransfer.v1.model.FileUploadResponse;
import org.molgenis.api.filetransfer.v1.model.FilesUploadResponse;
import org.molgenis.data.file.model.FileMeta;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.api.filetransfer.v1.FileTransferApiController.URI_API;

@RestController
@RequestMapping(URI_API)
class FileTransferApiController
{
	private static final String VERSION = "v1";
	static final String URI_API = FileTransferApiNamespace.URI_API + '/' + VERSION;
	static final String PATH_DOWNLOAD = "download";

	private final FileUploadService fileUploadService;
	private final FileDownloadService fileDownloadService;

	FileTransferApiController(FileUploadService fileUploadService, FileDownloadService fileDownloadService)
	{
		this.fileUploadService = requireNonNull(fileUploadService);
		this.fileDownloadService = requireNonNull(fileDownloadService);
	}

	/**
	 * Non-blocking multi-file upload.
	 */
	@PostMapping(value = "/upload", headers = "Content-Type=multipart/form-data")
	public @ResponseBody
	CompletableFuture<FilesUploadResponse> uploadFiles(HttpServletRequest request)
			throws IOException, FileUploadException
	{
		ServletFileUpload servletFileUpload = new ServletFileUpload();
		FileItemIterator fileItemIterator = servletFileUpload.getItemIterator(request);
		return CompletableFuture.supplyAsync(() ->
		{
			List<FileMeta> fileMetaList = fileUploadService.upload(fileItemIterator);
			return FilesUploadResponse.create(fileMetaList.stream().map(this::toFileUploadResponse).collect(toList()));
		});
	}

	private FileUploadResponse toFileUploadResponse(FileMeta fileMeta)
	{
		return FileUploadResponse.builder()
								 .setId(fileMeta.getId())
								 .setFilename(fileMeta.getFilename())
								 .setContentType(fileMeta.getContentType())
								 .setSize(fileMeta.getSize())
								 .setUrl(fileMeta.getUrl())
								 .build();
	}

	/**
	 * Non-blocking file download.
	 */
	@GetMapping(value = '/' + PATH_DOWNLOAD + "/{fileId}")
	public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable("fileId") String fileId)
	{
		return fileDownloadService.download(fileId);
	}
}
