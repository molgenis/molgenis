package org.molgenis.gavin.controller;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class GavinConfig
{
	@Bean
	public ExecutorService gavinExecutors()
	{
		return Executors.newFixedThreadPool(3,
				new ThreadFactoryBuilder().setNameFormat("molgenis-gavin-job-%d").build());
	}
}
