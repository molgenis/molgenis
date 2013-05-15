package org.molgenis.compute5.db.api;

import java.io.IOException;

/**
 * Client for the compute db api
 * 
 * @author erwin
 * 
 */
public class ComputeDbApiClient
{
	private final ComputeDbApiConnection computeDbApiConnection;

	public ComputeDbApiClient(ComputeDbApiConnection computeDbApiConnection)
	{
		this.computeDbApiConnection = computeDbApiConnection;
	}

	/**
	 * Start database polling to start pilots
	 * 
	 * @param request
	 * @return
	 * @throws ApiException
	 */
	public ApiResponse start(StartRunRequest request) throws ApiException
	{
		return computeDbApiConnection.doRequest(request, "/start", ApiResponse.class);
	}

	/**
	 * Stop database polling
	 * 
	 * @param request
	 * @return
	 * @throws ApiException
	 */
	public ApiResponse stop(StopRunRequest request) throws ApiException
	{
		return computeDbApiConnection.doRequest(request, "/stop", ApiResponse.class);
	}

	/**
	 * Get the status of all jobs (tasks) of a run
	 * 
	 * @param request
	 * @return
	 * @throws ApiException
	 */
	public RunStatusResponse getRunStatus(RunStatusRequest request) throws ApiException
	{
		return computeDbApiConnection.doRequest(request, "/get-run-status", RunStatusResponse.class);
	}

	/**
	 * Get all available backends
	 * 
	 * @return
	 * @throws ApiException
	 */
	public GetBackendsResponse getBackends() throws ApiException
	{
		return computeDbApiConnection.doRequest(null, "/get-backends", GetBackendsResponse.class);
	}

	/**
	 * Create a new run with tasks
	 * 
	 * @param request
	 * @return
	 * @throws ApiException
	 */
	public ApiResponse createRun(CreateRunRequest request) throws ApiException
	{
		return computeDbApiConnection.doRequest(request, "/create-run", ApiResponse.class);
	}

	/**
	 * Resubmit all failed tasks of a run
	 * 
	 * @param request
	 * @return
	 * @throws ApiException
	 */
	public ResubmitFailedTasksResponse resubmitFailedTasks(ResubmitFailedTasksRequest request) throws ApiException
	{
		return computeDbApiConnection.doRequest(request, "/resubmit-failed-tasks", ResubmitFailedTasksResponse.class);
	}

	public static void main(String[] args) throws ApiException, IOException
	{
		ComputeDbApiConnection con = new HttpClientComputeDbApiConnection("localhost", 8080, "/api/v1", "admin",
				"admin");
		try
		{
			ComputeDbApiClient client = new ComputeDbApiClient(con);
			ResubmitFailedTasksResponse response = client.resubmitFailedTasks(new ResubmitFailedTasksRequest("nbic25"));
			System.out.println(response);
		}
		finally
		{
			con.close();
		}
	}
}
