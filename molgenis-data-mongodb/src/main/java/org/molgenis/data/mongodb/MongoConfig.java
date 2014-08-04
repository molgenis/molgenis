package org.molgenis.data.mongodb;

import java.net.UnknownHostException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.MongoClient;

@Configuration
public class MongoConfig
{
	@Bean
	public MongoClient mongoClient() throws UnknownHostException
	{
		return new MongoClient();
	}

}
