package org.molgenis.pathways;

import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.dataWikiPathways.WSPathwayInfo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.cache.LoadingCache;

@Service
public class AsyncWikiPathwayLoader
{
	public final static Logger logger = Logger.getLogger(AsyncWikiPathwayLoader.class);

	@Async
	public void asyncLoadPathways(List<WSPathwayInfo> listOfPathwayInfo,
			LoadingCache<String, byte[]> cachedPathwayImages)
	{
		try
		{
			for (WSPathwayInfo wsPathwayInfo : listOfPathwayInfo)
			{
				System.out.println(wsPathwayInfo.getName());
				String pathwayId = wsPathwayInfo.getId();
				cachedPathwayImages.get(pathwayId);
				Thread.sleep(1000);
				logger.info("Pathway : " + wsPathwayInfo.getName() + " has been loaded!");
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e.getMessage());
		}
	}
}
