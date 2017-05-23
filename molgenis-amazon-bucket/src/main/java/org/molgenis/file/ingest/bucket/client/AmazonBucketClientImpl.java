package org.molgenis.file.ingest.bucket.client;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.file.FileStore;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.TreeMap;

@Component
public class AmazonBucketClientImpl implements AmazonBucketClient
{
	@Override
	public AmazonS3 getClient(String accessKey, String secretKey, String region)
	{
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds))
				.withRegion(region).build();
		return s3Client;
	}

	@Override
	public File downloadFile(AmazonS3 s3Client, FileStore fileStore, String jobIdentifier, String bucketName,
			String keyName, boolean isExpression) throws IOException, AmazonClientException
	{
		String key;
		//The key can be a regular expression instead of the actual key.
		//This is indicated by the "isExpression" boolean
		if (isExpression)
		{
			key = this.getMostRecentMatchingKey(s3Client, bucketName, keyName);
		}
		else
		{
			key = keyName;
		}
		S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, key));
		InputStream in = s3Object.getObjectContent();

		return storeFile(fileStore, key, jobIdentifier, in);
	}

	private File storeFile(FileStore fileStore, String key, String jobIdentifier, InputStream in) throws IOException
	{
		String relativePath = "bucket_" + jobIdentifier;
		File folder = new File(fileStore.getStorageDir(), relativePath);
		folder.mkdir();

		String fileRoot = key.replaceAll("[\\/:*?\"<>|]", "_");
		String filename = String.format("%s%s%s.xlsx", relativePath, File.separatorChar, fileRoot);
		return fileStore.store(in, filename);
	}

	//in case of an key expression all matching keys are collected and the most recent file is downloaded.
	private String getMostRecentMatchingKey(AmazonS3 s3Client, String bucketName, String regex)
	{
		ObjectListing objectListing = s3Client.listObjects(bucketName);
		TreeMap<Date, String> keys = new TreeMap<>();
		for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries())
		{
			if (objectSummary.getKey().matches(regex))
			{
				keys.put(objectSummary.getLastModified(), objectSummary.getKey());
			}
		}
		if (keys.size() == 0) throw new MolgenisDataException("No key matching regular expression: " + regex);
		return keys.lastEntry().getValue();
	}
}