package org.molgenis.file;

import org.molgenis.data.DataService;
import org.molgenis.file.model.FileMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.molgenis.file.FileDownloadController.URI;
import static org.molgenis.file.model.FileMetaMetaData.FILE_META;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(URI)
public class FileDownloadController
{
	public static final String URI = "/files";

	private final FileStore fileStore;
	private final DataService dataService;

	@Autowired
	public FileDownloadController(FileStore fileStore, DataService dataService)
	{
		this.fileStore = fileStore;
		this.dataService = dataService;
	}

	@RequestMapping(value = "/{id:.+}", method = GET)
	public void getFile(@PathVariable("id") String id, HttpServletResponse response) throws IOException
	{
		FileMeta fileMeta = dataService.findOneById(FILE_META, id, FileMeta.class);
		if (fileMeta == null)
		{
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
		else
		{
			// Not so nice but keep to serve old legacy files
			File fileStoreFile = fileStore.getFile(fileMeta.getFilename());
			if (!fileStoreFile.exists())
			{
				fileStoreFile = fileStore.getFile(id);
			}
			if (!fileStoreFile.exists())
			{
				response.setStatus(HttpStatus.NOT_FOUND.value());
			}

			// if file meta data exists for this file
			String outputFilename = fileMeta.getFilename();

			String contentType = fileMeta.getContentType();
			if (contentType != null)
			{
				response.setContentType(contentType);
			}

			Long size = fileMeta.getSize();
			if (size != null)
			{
				response.setContentLength(size.intValue());
			}

			response.setHeader("Content-Disposition", "attachment; filename=" + outputFilename.replace(" ", "_"));

			try (InputStream is = new FileInputStream(fileStoreFile))
			{
				FileCopyUtils.copy(is, response.getOutputStream());
			}
		}
	}
}
