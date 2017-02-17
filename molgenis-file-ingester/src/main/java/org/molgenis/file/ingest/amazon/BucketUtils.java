package org.molgenis.file.ingest.amazon;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BucketUtils
{
	public static AmazonS3 getClient(String profile)
	{
		return new AmazonS3Client(new ProfileCredentialsProvider(profile).getCredentials());
	}

	public static void displayTextInputStream(InputStream input) throws IOException
	{
		// Read one text line at a time and display.
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		while (true)
		{
			String line = reader.readLine();
			if (line == null) break;

			System.out.println("    " + line);
		}
		System.out.println();
	}

	public static void handleAmazonClientException(AmazonClientException ace)
	{
		System.out.println("Caught an AmazonClientException, which means" + " the client encountered "
				+ "an internal error while trying to " + "communicate with S3, "
				+ "such as not being able to access the network.");
		System.out.println("Error Message: " + ace.getMessage());
	}

	public static void handleAmazonServiceException(AmazonServiceException ase)
	{
		System.out.println("Caught an AmazonServiceException, which" + " means your request made it "
				+ "to Amazon S3, but was rejected with an error response" + " for some reason.");
		System.out.println("Error Message:    " + ase.getMessage());
		System.out.println("HTTP Status Code: " + ase.getStatusCode());
		System.out.println("AWS Error Code:   " + ase.getErrorCode());
		System.out.println("Error Type:       " + ase.getErrorType());
		System.out.println("Request ID:       " + ase.getRequestId());
	}
}
