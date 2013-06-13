package org.molgenis.compute.db.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.service.RunService;
import org.molgenis.compute.runtime.ComputeBackend;
import org.molgenis.compute5.db.api.ApiResponse;
import org.molgenis.compute5.db.api.Backend;
import org.molgenis.compute5.db.api.CreateRunRequest;
import org.molgenis.compute5.db.api.GetBackendsResponse;
import org.molgenis.compute5.db.api.ResubmitFailedTasksRequest;
import org.molgenis.compute5.db.api.ResubmitFailedTasksResponse;
import org.molgenis.compute5.db.api.RunStatus;
import org.molgenis.compute5.db.api.RunStatusRequest;
import org.molgenis.compute5.db.api.RunStatusResponse;
import org.molgenis.compute5.db.api.StartRunRequest;
import org.molgenis.compute5.db.api.StopRunRequest;
import org.molgenis.framework.db.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Json api for all compute db commands
 * 
 * @author erwin
 * 
 */
@Scope("request")
@Controller
@RequestMapping("/api/v1")
public class ApiController
{
	private static final Logger LOG = Logger.getLogger(ApiController.class);
	private final RunService runService;
	private final Database database;

	@Autowired
	public ApiController(RunService runService, Database database)
	{
		this.runService = runService;
		this.database = database;
	}

	/**
	 * Start database polling for starting pilots for a run
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/start", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<ApiResponse> start(@RequestBody
	StartRunRequest request)
	{
		LOG.info("Recieved start request for run [" + request.getRunName() + "]");

		ApiResponse response = new ApiResponse();
		try
		{
			runService.start(request.getRunName(), request.getUsername(), request.getPassword());
		}
		catch (Exception e)
		{
			LOG.error("Exception starting run [" + request.getRunName() + "]", e);
			response.setErrorMessage(e.getMessage());
		}

		return new ResponseEntity<ApiResponse>(response, HttpStatus.OK);
	}

	/**
	 * Stop database polling
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/stop", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<ApiResponse> stop(@RequestBody
	StopRunRequest request)
	{
		LOG.info("Recieved stop request for run [" + request.getRunName() + "]");

		ApiResponse response = new ApiResponse();
		try
		{
			runService.stop(request.getRunName());
		}
		catch (Exception e)
		{
			LOG.error("Exception stopping run [" + request.getRunName() + "]", e);
			response.setErrorMessage(e.getMessage());
		}

		return new ResponseEntity<ApiResponse>(response, HttpStatus.OK);
	}

	/**
	 * Get the status of all jobs (tasks) of a run
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/get-run-status", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<RunStatusResponse> getRunStatus(@RequestBody
	RunStatusRequest request)
	{
		LOG.info("Recieved status request for run [" + request.getRunName() + "]");

		RunStatusResponse response = new RunStatusResponse();
		try
		{
			RunStatus runStatus = runService.getStatus(request.getRunName());
			response.setRunStatus(runStatus);
		}
		catch (Exception e)
		{
			LOG.error("Exception stopping run [" + request.getRunName() + "]", e);
			response.setErrorMessage(e.getMessage());
		}

		return new ResponseEntity<RunStatusResponse>(response, HttpStatus.OK);
	}

	/**
	 * Create a new run
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/create-run", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<ApiResponse> createRun(@RequestBody
	CreateRunRequest request)
	{
		LOG.info("Create run request for runname [" + request.getRunName() + "]");

		ApiResponse response = new ApiResponse();
		try
		{
			runService.create(request.getRunName(), request.getBackendName(), request.getPollDelay(),
					request.getTasks(), request.getEnvironment(), request.getUserName());
		}
		catch (Exception e)
		{
			LOG.error("Exception creating run [" + request.getRunName() + "]", e);
			response.setErrorMessage(e.getMessage());
		}

		return new ResponseEntity<ApiResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/get-backends", method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<GetBackendsResponse> getBackends()
	{
		LOG.info("Recieved getBackends request");

		GetBackendsResponse response = new GetBackendsResponse();
		try
		{
			List<ComputeBackend> computeBackends = database.find(ComputeBackend.class);
			List<Backend> backends = Lists.transform(computeBackends, new Function<ComputeBackend, Backend>()
			{
				@Override
				public Backend apply(ComputeBackend cb)
				{
					return new Backend(cb.getName(), cb.getBackendUrl(), cb.getHostType(), cb.getCommand());
				}
			});

			response.setBackends(backends);
		}
		catch (Exception e)
		{
			LOG.error("Exception getting backends");
			response.setErrorMessage(e.getMessage());
		}

		return new ResponseEntity<GetBackendsResponse>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/resubmit-failed-tasks", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<ResubmitFailedTasksResponse> resubmitFailedTasks(@RequestBody
	ResubmitFailedTasksRequest request)
	{
		LOG.info("Resubmit failed tasks for run [" + request.getRunName() + "]");

		ResubmitFailedTasksResponse response = new ResubmitFailedTasksResponse();
		try
		{
			int count = runService.resubmitFailedTasks(request.getRunName());
			response.setNrOfResubmittedTasks(count);
		}
		catch (Exception e)
		{
			LOG.error("Exception resubmit failed tasks for run [" + request.getRunName() + "]", e);
			response.setErrorMessage(e.getMessage());
		}

		return new ResponseEntity<ResubmitFailedTasksResponse>(response, HttpStatus.OK);
	}
}
