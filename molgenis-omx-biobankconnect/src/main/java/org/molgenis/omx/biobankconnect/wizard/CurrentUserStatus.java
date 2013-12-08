package org.molgenis.omx.biobankconnect.wizard;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class CurrentUserStatus
{
	private final Map<String, Boolean> userStatus;
	private final Map<String, Long> totalNumberOfQueriesForUser;
	private final Map<String, Integer> finishedNumberOfQueriesForUser;

	public CurrentUserStatus()
	{
		userStatus = new HashMap<String, Boolean>();
		totalNumberOfQueriesForUser = new HashMap<String, Long>();
		finishedNumberOfQueriesForUser = new HashMap<String, Integer>();
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
		if (!getUserstatus(userName)) return 100;
		if (!totalNumberOfQueriesForUser.containsKey(userName) || !finishedNumberOfQueriesForUser.containsKey(userName)) return 0;

		long totalNumber = totalNumberOfQueriesForUser.get(userName);
		int finishedNumber = finishedNumberOfQueriesForUser.get(userName);
		DecimalFormat df = new DecimalFormat("#.##");
		Double percentage = totalNumber == 0 ? new Double(0) : ((double) finishedNumber) / totalNumber;
		percentage = Double.parseDouble(df.format(percentage * 100));
		return percentage.intValue();
	}

	public Boolean getUserstatus(String userName)
	{
		return userStatus.containsKey(userName) && userStatus.get(userName);
	}

	public void setUserStatus(String userName, Boolean status)
	{
		if (status) userStatus.put(userName, status);
		else if (userStatus.containsKey(userName)) userStatus.remove(userName);

		if (totalNumberOfQueriesForUser.containsKey(userName)) totalNumberOfQueriesForUser.remove(userName);
		if (finishedNumberOfQueriesForUser.containsKey(userName)) finishedNumberOfQueriesForUser.remove(userName);
	}
}
