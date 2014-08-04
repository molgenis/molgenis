package org.molgenis.data.mongodb;

import java.net.UnknownHostException;

import org.molgenis.data.EntityMetaData;
import org.molgenis.security.runas.SystemSecurityToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.mongodb.MongoClient;

public abstract class AbstractMongoRepositoryTest
{
	private MongoClient mongo;
	protected MongoRepositoryCollection mongoRepositoryCollection;

	@BeforeMethod
	public void beforeMethod()
	{
		mongoRepositoryCollection = new MongoRepositoryCollection(mongo.getDB("molgenis-test"));
	}

	@BeforeClass
	public void beforeClass() throws UnknownHostException
	{
		SecurityContextHolder.getContext().setAuthentication(new SystemSecurityToken());
		mongo = new MongoClient();
	}

	@AfterMethod
	public void afterMethod()
	{
		mongo.dropDatabase("molgenis-test");
	}

	@AfterClass
	public void afterClass()
	{
		mongo.close();
	}

	protected MongoRepository createRepo(EntityMetaData meta)
	{
		return mongoRepositoryCollection.add(meta);
	}

}
