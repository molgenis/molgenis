package org.molgenis.amazon.bucket.client;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.excel.ExcelFileExtensions;
import org.molgenis.data.file.FileStore;
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
		return AmazonS3ClientBuilder.standard()
									.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
									.withRegion(region)
									.build();
	}

	@Override
	public File downloadFile(AmazonS3 s3Client, FileStore fileStore, String jobIdentifier, String bucketName,
			String keyName, String extension, boolean isExpression, String targetEntityType) throws IOException
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

		return storeFile(fileStore, key, extension, targetEntityType, jobIdentifier, in);
	}

	private File storeFile(FileStore fileStore, String key, String extension, String targetEntityName,
			String jobIdentifier, InputStream in) throws IOException
	{
		//no extension given, default is excel
		if (!StringUtils.isNotEmpty(extension))
		{
			extension = "xlsx";
		}
		String relativePath = "bucket_" + jobIdentifier;
		File folder = new File(fileStore.getStorageDir(), relativePath);
		folder.mkdir();

		String filename;
		if (StringUtils.isNotEmpty(targetEntityName) && !ExcelFileExtensions.getExcel().contains(extension))
		{
			filename = String.format("%s%s%s.%s", relativePath, File.separatorChar, targetEntityName, extension);
		}
		else
		{
			String fileRoot = key.replaceAll("[\\/:*?\"<>|]", "_");
			filename = String.format("%s%s%s.%s", relativePath, File.separatorChar, fileRoot, extension);
		}

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