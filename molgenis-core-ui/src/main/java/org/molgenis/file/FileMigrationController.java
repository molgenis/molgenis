package org.molgenis.file;

import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.io.IOUtils.copy;
import static org.molgenis.file.FileMigrationController.URI;
import static org.slf4j.LoggerFactory.getLogger;

@Controller
@RequestMapping(URI)
public class FileMigrationController
{
	private static final Logger LOG = getLogger(FileMigrationController.class);

	public static final String URI = "/migration/files";

	private FileStore fileStore;

	FileMigrationController(FileStore fileStore)
	{
		this.fileStore = requireNonNull(fileStore);
	}

	/**
	 * Call with the following command:
	 * http://localhost:8080/migration/files/export?target="Absolute path"
	 *
	 * @param response
	 * @throws IOException
	 */
	@GetMapping(value = "/export", produces = "application/zip")
	@ResponseBody
	public void exportFileStore(HttpServletResponse response, @RequestParam("target") String targetPath)
			throws IOException
	{
		File target = new File(targetPath);
		ZipUtil.pack(new File(fileStore.getStorageDir()), target);
		try (InputStream is = new FileInputStream(target))
		{
			copy(is, response.getOutputStream());

			String mimeType = "application/zip";

			// set content attributes for the response
			response.setContentType(mimeType);
			response.setContentLength((int) target.length());

			// set headers for the response
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", target.getName());
			response.setHeader(headerKey, headerValue);
			response.flushBuffer();
		}
		catch (IOException ex)
		{
			LOG.info("Error writing file to output stream. Filename was '{}'", target.getName(), ex);
			throw new RuntimeException("IOError writing file to output stream");
		}
	}

	/**
	 * Use the following curl command to  call the filestore migration import API
	 * curl http://localhost:8080/migration/files/import -v -H "Content-Type: multipart/form-data;x-molgenis-token: admin" -X POST -F "file=@filestore_zip_file.zip"
	 *
	 * @param file
	 * @throws IOException
	 */
	@PostMapping(value = "/import", consumes = "multipart/form-data")
	@ResponseBody
	public void importFileStore(@RequestParam("file") MultipartFile file) throws IOException
	{
		File zip = File.createTempFile(UUID.randomUUID().toString(), ".temp");
		try (FileOutputStream o = new FileOutputStream(zip))
		{
			copy(file.getInputStream(), o);
		}
		catch (IOException e)
		{
			LOG.error("Something went wrong: {}", e);
		}
		ZipUtil.unpack(zip, new File(fileStore.getStorageDir()));
		zip.delete();
	}
}
