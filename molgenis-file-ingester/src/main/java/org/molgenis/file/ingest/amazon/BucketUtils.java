package org.molgenis.file.ingest.amazon;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BucketUtils
{
	public static AmazonS3 getClient(String profile)
	{
		return new AmazonS3Client(new ProfileCredentialsProvider(profile).getCredentials());
	}

	public static void uploadFile(AmazonS3 s3client, File file, String bucketName, String keyName)
			throws AmazonClientException
	{
		System.out.println("Uploading a new object to S3 from a file\n");
		ObjectMetadata objectMetadata = new ObjectMetadata();
		// Request server-side encryption.
		objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
		PutObjectRequest putRequest = new PutObjectRequest(bucketName, keyName, file);
		putRequest.setMetadata(objectMetadata);

		s3client.putObject(putRequest);

	}

	public static File downloadFile(AmazonS3 s3Client, File file, String bucketName, String keyName, boolean overwrite)
			throws IOException, AmazonClientException
	{
		S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, keyName));
		InputStream in = s3Object.getObjectContent();
		Path path = file.toPath();
		if (!Files.exists(path) || overwrite)
		{
			if (Files.exists(path)) Files.delete(path);
			Files.copy(in, path);
		}
		else
		{
			throw new FileAlreadyExistsException("The specified file already exists use '-o' to overwrite.");
		}
		return path.toFile();
	}
}
