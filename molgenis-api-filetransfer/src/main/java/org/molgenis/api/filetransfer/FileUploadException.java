package org.molgenis.api.filetransfer;

public class FileUploadException extends RuntimeException
{
	FileUploadException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
