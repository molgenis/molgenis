package org.molgenis.omx.biobankconnect.wizard;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class CurrentUserStatus
{
	public static enum Stage
	{
		DeleteMapping, CreateMapping, StoreMapping;
	}

	private final Map<String, String> currentUsers;
	private final Map<String, Stage> userCurrentStage;
	private final Map<String, Boolean> userCurrentMatching;
	private final Map<String, Long> totalNumberOfQueriesForUser;
	private final Map<String, Integer> finishedNumberOfQueriesForUser;

	public CurrentUserStatus()
	{
		currentUsers = new HashMap<String, String>();
		userCurrentStage = new HashMap<String, Stage>();
		userCurrentMatching = new HashMap<String, Boolean>();
		totalNumberOfQueriesForUser = new HashMap<String, Long>();
		finishedNumberOfQueriesForUser = new HashMap<String, Integer>();
	}

	public void setUserCurrentStage(String userName, Stage stage)
	{
		totalNumberOfQueriesForUser.remove(userName);
		finishedNumberOfQueriesForUser.remove(userName);
		userCurrentStage.put(userName, stage);
	}

	public void setUserTotalNumberOfQueries(String userName, Long totalNumberOfQueries)
	{
		totalNumberOfQueriesForUser.put(userName, totalNumberOfQueries);
	}

	public void incrementFinishedNumberOfQueries(String userName)
	{
		Integer finishedNumber = null;
		if (!finishedNumberOfQueriesForUser.containsKey(userName)) finishedNumber = 0;
		else finishedNumber = finishedNumberOfQueriesForUser.get(userName);
		finishedNumberOfQueriesForUser.put(userName, ++finishedNumber);
	}

	public int getPercentageOfProcessForUser(String userName)
	{
		if (!isUserMatching(userName)) return 100;
		if (!totalNumberOfQueriesForUser.containsKey(userName) || !finishedNumberOfQueriesForUser.containsKey(userName)) return 0;

		long totalNumber = totalNumberOfQueriesForUser.get(userName);
		int finishedNumber = finishedNumberOfQueriesForUser.get(userName);
		DecimalFormat df = new DecimalFormat("#.##");
		Double percentage = totalNumber == 0 ? new Double(0) : ((double) finishedNumber) / totalNumber;
		percentage = Double.parseDouble(df.format(percentage * 100));
		return percentage.intValue();
	}

	public String getUserCurrentStage(String userName)
	{
		return userCurrentStage.containsKey(userName) ? userCurrentStage.get(userName).toString() : null;
	}

	public Boolean isUserLoggedIn(String userName, String requestSessionId)
	{
		return currentUsers.containsKey(userName) && !currentUsers.get(userName).equalsIgnoreCase(requestSessionId);
	}

	public Boolean isUserMatching(String userName)
	{
		return userCurrentMatching.containsKey(userName) && userCurrentMatching.get(userName);
	}

	public void setUserIsRunning(String userName, Boolean status)
	{
		if (status) userCurrentMatching.put(userName, status);
		else
		{
			if (userCurrentMatching.containsKey(userName)) userCurrentMatching.remove(userName);
			if (totalNumberOfQueriesForUser.containsKey(userName)) totalNumberOfQueriesForUser.remove(userName);
			if (finishedNumberOfQueriesForUser.containsKey(userName)) finishedNumberOfQueriesForUser.remove(userName);
			if (userCurrentStage.containsKey(userName)) userCurrentStage.remove(userName);
		}
	}

	public void setUserLoggedIn(String userName, String requestSessionId)
	{
		if (!currentUsers.containsKey(userName)) currentUsers.put(userName, requestSessionId);
	}

	public void removeCurrentUser(String userName, String userIpAddress)
	{
		if (currentUsers.containsKey(userName) && currentUsers.get(userName).equals(userIpAddress)) currentUsers
				.remove(userName);
	}

	public Boolean hasOtherUsers()
	{
		return userCurrentMatching.size() > 1;
	}
}