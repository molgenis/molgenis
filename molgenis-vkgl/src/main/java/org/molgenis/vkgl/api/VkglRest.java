package org.molgenis.vkgl.api;

import static org.molgenis.data.rest.RestController.BASE_URI;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.data.rest.RestController;
import org.molgenis.data.rsql.MolgenisRSQL;
import org.molgenis.data.support.AggregateQueryImpl;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.exceptions.MalformedQueryException;
import org.molgenis.exceptions.MissingValueException;
import org.molgenis.exceptions.NoHitsFoundException;
import org.molgenis.exceptions.SearchWindowTooBigException;
import org.molgenis.exceptions.StartPositionBeforeEndPositionException;
import org.molgenis.exceptions.TooManyQueriesException;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.token.TokenService;
import org.molgenis.util.ResourceFingerprintRegistry;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(BASE_URI)
public class VkglRest
{

	private static final Logger LOG = LoggerFactory.getLogger(RestController.class);
	private static final String ALLELE1 = "ALLELE1";
	private static final String ALLELE2 = "ALLELE2";
	public static final String BASE_URI = "/vkgl/api/v1";
	private final DataService dataService;
	private final AuthenticationManager authenticationManager;

	@Autowired
	public VkglRest(DataService dataService, WritableMetaDataService metaDataService, TokenService tokenService,
			AuthenticationManager authenticationManager, MolgenisPermissionService molgenisPermissionService,
			MolgenisRSQL molgenisRSQL, ResourceFingerprintRegistry resourceFingerprintRegistry)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		if (metaDataService == null) throw new IllegalArgumentException("metaDataService is null");
		if (tokenService == null) throw new IllegalArgumentException("tokenService is null");
		if (authenticationManager == null) throw new IllegalArgumentException("authenticationManager is null");
		if (molgenisPermissionService == null) throw new IllegalArgumentException("molgenisPermissionService is null");
		if (resourceFingerprintRegistry == null) throw new IllegalArgumentException(
				"resourceFingerprintRegistry is null");

		this.dataService = dataService;
		this.authenticationManager = authenticationManager;

	}

	public Map<String, List<String>> getAllelePositions(VkglAlleleQuery[] allQueries)
			throws SearchWindowTooBigException, StartPositionBeforeEndPositionException
	{

		Map<String, List<String>> positionsToQueryId = new HashMap<>();
		for (VkglAlleleQuery query : allQueries)
		{
			if (Integer.parseInt(query.getEnd()) < Integer.parseInt(query.getStart())) throw new StartPositionBeforeEndPositionException(
					"Start position occurs after stop position. ParameterID: " + query.getParameterID());

			if ((Integer.parseInt(query.getEnd()) - Integer.parseInt(query.getStart())) > 5000) throw new SearchWindowTooBigException(
					"Difference between start and end cannot exceed 5000 bp. ParameterID: " + query.getParameterID());

			if (query.getSource().equals("HGNC"))
			{

				String geneSymbol = query.getReference();

				HashMap<String, Iterable<Entity>> lookupTables = new HashMap<>();
				lookupTables.put("GRCh37", dataService.findAll("GRCh37", new QueryImpl().eq("HGNC", geneSymbol)));
				lookupTables.put("GRCh38", dataService.findAll("GRCh38", new QueryImpl().eq("HGNC", geneSymbol)));

				for (String reference : lookupTables.keySet())
				{
					Entity e = lookupTables.get(reference).iterator().next();

					for (int position = Integer.parseInt(query.getStart()); position < Integer.parseInt(query.getEnd()) - 1; position++)
					{

						String chr = (String) e.get("Chromosome");
						int positionStart = position + (int) e.get("Start");
						int positionStop = position + (int) e.get("End");

						for (int start = positionStart; start < positionStop; start++)
						{
							String pos = chr + ":" + start + ":" + reference;

							if (positionsToQueryId.get(pos) != null)
							{
								positionsToQueryId.get(pos).add(query.getParameterID());
							}
							else
							{
								List<String> iDs = new ArrayList<>();
								iDs.add(query.getParameterID());
								positionsToQueryId.put(pos, iDs);
							}

						}

					}
				}

			}
			if (query.getSource().equals("GRCBUILD"))
			{

				for (int start = Integer.parseInt(query.getStart()); start < Integer.parseInt(query.getEnd()) - 1; start++)
				{
					String chr = query.getReference().split("\\.")[0];

					String pos = chr + ":" + start + ":" + query.getReference().split("\\.")[1];

					if (positionsToQueryId.containsKey(pos))
					{

						positionsToQueryId.get(pos).add(query.getParameterID());
					}
					else
					{
						List<String> iDs = new ArrayList<>();
						iDs.add(query.getParameterID());
						positionsToQueryId.put(pos, iDs);
					}
				}
			}

		}

		return positionsToQueryId;
	}

	public Map<String, List<String>> getCoordinatePositions(VkglCoordinateQuery[] allQueries)
			throws SearchWindowTooBigException, StartPositionBeforeEndPositionException
	{

		Map<String, List<String>> positionsToQueryId = new HashMap<>();
		for (VkglCoordinateQuery query : allQueries)
		{
			if (Integer.parseInt(query.getEnd()) < Integer.parseInt(query.getStart())) throw new StartPositionBeforeEndPositionException(
					"Start position occurs after stop position. ParameterID: " + query.getParameterID());

			if ((Integer.parseInt(query.getEnd()) - Integer.parseInt(query.getStart())) > 5000) throw new SearchWindowTooBigException(
					"Difference between start and end cannot exceed 5000 bp. ParameterID: " + query.getParameterID());

			if (query.getSource().equals("HGNC"))
			{

				String geneSymbol = query.getReference();

				HashMap<String, Iterable<Entity>> lookupTables = new HashMap<>();
				lookupTables.put("GRCh37", dataService.findAll("GRCh37", new QueryImpl().eq("HGNC", geneSymbol)));
				lookupTables.put("GRCh38", dataService.findAll("GRCh38", new QueryImpl().eq("HGNC", geneSymbol)));

				for (String reference : lookupTables.keySet())
				{
					Entity e = lookupTables.get(reference).iterator().next();

					for (int position = Integer.parseInt(query.getStart()); position < Integer.parseInt(query.getEnd()) - 1; position++)
					{

						String chr = (String) e.get("Chromosome");
						int positionStart = position + (int) e.get("Start");
						int positionStop = position + (int) e.get("End");

						for (int start = positionStart; start < positionStop; start++)
						{
							String pos = chr + ":" + start + ":" + reference;

							if (positionsToQueryId.get(pos) != null)
							{
								positionsToQueryId.get(pos).add(query.getParameterID());
							}
							else
							{
								List<String> iDs = new ArrayList<>();
								iDs.add(query.getParameterID());
								positionsToQueryId.put(pos, iDs);
							}

						}

					}
				}

			}
			if (query.getSource().equals("GRCBUILD"))
			{

				for (int start = Integer.parseInt(query.getStart()); start < Integer.parseInt(query.getEnd()) - 1; start++)
				{

					String chr = query.getReference().split("\\.")[0];

					String pos = chr + ":" + start + ":" + query.getReference().split("\\.")[1];

					if (positionsToQueryId.containsKey(pos))
					{

						positionsToQueryId.get(pos).add(query.getParameterID());
					}
					else
					{
						List<String> iDs = new ArrayList<>();
						iDs.add(query.getParameterID());
						positionsToQueryId.put(pos, iDs);
					}
				}
			}

		}

		return positionsToQueryId;
	}

	public ArrayList<VkglResult> getCoordinateResults(VkglCoordinateQuery[] allQueries, String queryStatement)
			throws MissingValueException, MalformedQueryException, SearchWindowTooBigException,
			StartPositionBeforeEndPositionException, NoHitsFoundException
	{
		String entityName = "vkgl_vkgl";
		EntityMetaData entityMeta = dataService.getEntityMetaData(entityName);
		String reference = "";
		AttributeMetaData posXAttributeMeta = entityMeta.getAttribute(ALLELE1);
		AttributeMetaData posYAttributeMeta = entityMeta.getAttribute(ALLELE2);
		int positionStart = 0;
		int positionEnd = 0;
		String chr = "";
		Long totalNumberOfHits = (long) 0;
		String[] statement = queryStatement.split("(?<=[-+*/\\|\\!\\(])|(?=[-+*/\\|\\!\\)])");

		// no query statement found
		if (statement == null || statement.length < 1 || statement[0].length() == 0) throw new MissingValueException(
				"No query statement found please supply one.");

		// check if all id's are represented in the query statement
		for (VkglCoordinateQuery q : allQueries)
		{
			boolean containsIds = false;
			for (String qEl : statement)
			{
				if (q.getParameterID().equals(qEl)) containsIds = true;
			}
			if (!containsIds) throw new MalformedQueryException(
					"Id in statement not found in qeuries please check your query. ParameterID id "
							+ q.getParameterID() + " not found in query statement.");
		}

		ArrayList<VkglResult> results = new ArrayList<>();
		Map<String, List<String>> positionsToIds = getCoordinatePositions(allQueries);

		// sequential building of query according to the query statement
		for (String position : positionsToIds.keySet())
		{
			boolean equationFlag = true;
			Query q = new QueryImpl();
			for (String qEl : statement)
			{
				if (equationFlag)
				{
					if (qEl.equals("!"))
					{
						q = q.and().not();
					}
					else if (qEl.equals("|"))
					{
						q = q.or();
					}
					else if (qEl.equals("("))
					{
						q = q.nest();
					}
					else if (qEl.equals(")"))
					{
						q = q.unnest();
					}
				}

				for (VkglCoordinateQuery query : allQueries)
				{

					for (String id : positionsToIds.get(position))
					{
						// if not the same something went wrong when comparing the ID's
						if (query.getParameterID().equals(qEl) && query.getParameterID().equals(id))
						{

							if (positionsToIds.get(position).size() > 1)
							{
								equationFlag = true;
							}
							else
							{
								equationFlag = false;
							}

							String[] splittedPos = position.split(":");
							chr = splittedPos[0];
							positionStart = Integer.parseInt(splittedPos[1]);
							reference = splittedPos[2];
							positionEnd = positionStart + 2;

							if (query.getOperator().equals("IS"))
							{
								q = q.nest().eq("#CHROM", chr).and().rng("POS", positionStart, positionEnd).unnest();

							}
							else if (query.getOperator().equals("NOT"))
							{
								q = q.nest().eq("#CHROM", chr).and().rng("POS", positionStart, positionEnd).unnest();
							}

						}
					}
				}
			}
			q = q.unnestAll();

			AggregateQuery newAggregateQuery = new AggregateQueryImpl().attrX(posXAttributeMeta)
					.attrY(posYAttributeMeta).query(q);

			AggregateResult aggregateQueryResults = dataService.aggregate(entityName, newAggregateQuery);

			for (List<Long> aggregateRowResult : aggregateQueryResults.getMatrix())
			{
				for (Long aggregateResult : aggregateRowResult)
				{
					totalNumberOfHits = totalNumberOfHits + aggregateResult;
				}
			}

			VkglResult vkglResult = new VkglResult();
			vkglResult.setChromosome(chr);
			// start position and stop position indicate a window capturing everything in between these number but excluding themselves.
			vkglResult.setPosition(positionStart + 1);
			vkglResult.setResultType("allele");
			vkglResult.setResult(aggregateQueryResults.getMatrix());
			vkglResult.setXAxisAlleles(aggregateQueryResults.getxLabels());
			vkglResult.setYAxisAlleles(aggregateQueryResults.getyLabels());

			vkglResult.setReference(reference);

			results.add(vkglResult);
		}
		if (totalNumberOfHits < 10) throw new NoHitsFoundException("Less then ten have been found");
		return results;
	}

	public ArrayList<VkglResult> getAlleleResults(VkglAlleleQuery[] allQueries, String queryStatement)
			throws MissingValueException, NoHitsFoundException, SearchWindowTooBigException,
			StartPositionBeforeEndPositionException
	{
		String entityName = "vkgl_vkgl";
		EntityMetaData entityMeta = dataService.getEntityMetaData(entityName);
		String reference = "";
		AttributeMetaData posXAttributeMeta = entityMeta.getAttribute(ALLELE1);
		AttributeMetaData posYAttributeMeta = entityMeta.getAttribute(ALLELE2);
		int positionStart = 0;
		int positionEnd = 0;
		Long totalNumberOfHits = (long) 0;

		String chr = "";

		String[] statement = queryStatement.split("(?<=[-+*/\\|\\!\\(])|(?=[-+*/\\|\\!\\)])");

		if (statement == null || statement.length < 1 || statement[0].length() == 0) throw new MissingValueException(
				"No query statement found please supply one ");

		ArrayList<VkglResult> results = new ArrayList<>();
		Map<String, List<String>> positionsToIds = getAllelePositions(allQueries);

		for (VkglAlleleQuery q : allQueries)
		{
			boolean containsIds = false;
			for (String qEl : statement)
			{
				if (q.getParameterID().equals(qEl)) containsIds = true;
			}
			if (!containsIds) throw new MissingValueException(
					"Id in statement not found in qeuries please check your query. Query id " + q.getParameterID()
							+ " not found in query statement");
		}
		// sequential building of query according to the query statement
		for (String position : positionsToIds.keySet())
		{
			boolean equationFlag = true;

			Query q = new QueryImpl();
			for (String qEl : statement)
			{

				if (equationFlag)
				{
					if (qEl.equals("!"))
					{
						q = q.and().not();
					}
					else if (qEl.equals("|"))
					{
						q = q.or();
					}
					else if (qEl.equals("("))
					{
						q = q.nest();
					}
					else if (qEl.equals(")"))
					{
						q = q.unnest();
					}
				}

				for (VkglAlleleQuery query : allQueries)
				{
					for (String id : positionsToIds.get(position))
					{
						if (query.getParameterID().equals(qEl) && query.getParameterID().equals(id))
						{
							if (positionsToIds.get(position).size() > 1)
							{
								equationFlag = true;
							}
							else
							{
								equationFlag = false;
							}
							String[] splittedPos = position.split(":");
							chr = splittedPos[0];
							positionStart = Integer.parseInt(splittedPos[1]);
							reference = splittedPos[2];
							positionEnd = positionStart + 2;

							if (query.getOperator().equals("IS"))
							{
								q = q.nest().eq("#CHROM", chr).and().rng("POS", positionStart, positionEnd);
								if (query.getAlleleSequence() != null)
								{
									for (String allele : query.getAlleleSequence())
									{
										q = q.and().nest().eq("ALLELE1", allele).or().eq("ALLELE2", allele).unnest();
									}
								}
								q.unnest();

							}
							else if (query.getOperator().equals("NOT"))
							{
								q = q.nest().eq("#CHROM", chr).and().rng("POS", positionStart, positionEnd);
								if (query.getAlleleSequence() != null)
								{
									for (String allele : query.getAlleleSequence())
									{
										q = q.and().not().eq("ALLELE1", allele).and().not().eq("ALLELE2", allele);
									}
								}
								q = q.unnest();
							}
						}

					}
				}

			}

			q = q.unnestAll();

			AggregateQuery newAggregateQuery = new AggregateQueryImpl().attrX(posXAttributeMeta)
					.attrY(posYAttributeMeta).query(q);

			AggregateResult aggregateQueryResults = dataService.aggregate(entityName, newAggregateQuery);

			for (List<Long> aggregateRowResult : aggregateQueryResults.getMatrix())
			{
				for (Long aggregateResult : aggregateRowResult)
				{
					totalNumberOfHits = totalNumberOfHits + aggregateResult;
				}
			}

			VkglResult vkglResult = new VkglResult();
			vkglResult.setChromosome(chr);
			vkglResult.setPosition(positionStart + 1);
			vkglResult.setResultType("allele");
			vkglResult.setResult(aggregateQueryResults.getMatrix());
			vkglResult.setXAxisAlleles(aggregateQueryResults.getxLabels());
			vkglResult.setYAxisAlleles(aggregateQueryResults.getyLabels());

			vkglResult.setReference(reference);

			results.add(vkglResult);

		}
		if (totalNumberOfHits < 10) throw new NoHitsFoundException("Less then ten have been found");
		return results;
	}

	@RequestMapping(value = "/getAggregate", method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public VkglResponse vkglQueryResponse(@Valid @RequestBody VkglRequest request) throws MissingValueException,
			MalformedQueryException, SearchWindowTooBigException, NoHitsFoundException, TooManyQueriesException,
			StartPositionBeforeEndPositionException
	{
		if (request == null) throw new MissingValueException("No request found");
		// request = request != null ? request : new VkglRequest();

		ArrayList<VkglResult> results = new ArrayList<>();
		// if its a coordinate query
		if (request.getQuery().getCoordinate() != null)
		{
			if (request.getQuery().getCoordinate().length >= 5) throw new TooManyQueriesException(
					"No more then 5 queries can be chained");
			results = getCoordinateResults(request.getQuery().getCoordinate(), request.getQueryStatement());
		}

		// if its an allele query
		if (request.getQuery().getAllele() != null)
		{
			if (request.getQuery().getAllele().length >= 5) throw new TooManyQueriesException(
					"No more then 5 queries can be chained at one give time");
			results = getAlleleResults(request.getQuery().getAllele(), request.getQueryStatement());
		}

		VkglResponse vkglResponse = new VkglResponse();
		VkglResponseMetadata vkglMetadata = new VkglResponseMetadata();
		vkglMetadata.setTotal(results.size());
		/**
		 * TODO add support for paging on this request
		 * 
		 */
		vkglMetadata.setQueryId(request.getQueryMetadata().getQueryId());
		vkglMetadata.setNum(0);
		vkglMetadata.setHref("not supported");
		vkglMetadata.setNextHref("not supported");
		vkglMetadata.setPrevHref("not supported yet");

		vkglResponse.setMetadata(vkglMetadata);
		vkglResponse.setResults(results);

		return vkglResponse;

	}

	@ExceptionHandler(MissingValueException.class)
	@ResponseStatus(NOT_FOUND)
	@ResponseBody
	public VkglErrorResponse handleMissingEquationStatementException(MissingValueException e)
	{
		LOG.debug("Missing value detected: ", e);
		return new VkglErrorResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(MalformedQueryException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public VkglErrorResponse handleMalformedQueryException(MalformedQueryException e)
	{
		LOG.debug("Malformed query statement detected: ", e);
		return new VkglErrorResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(SearchWindowTooBigException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public VkglErrorResponse handleSearchWindowTooBigException(SearchWindowTooBigException e)
	{
		LOG.debug("Too large search window: ", e);
		return new VkglErrorResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(NoHitsFoundException.class)
	@ResponseStatus(NOT_FOUND)
	@ResponseBody
	public VkglErrorResponse handleNoHitsFoundException(NoHitsFoundException e)
	{
		LOG.debug("No hits found: ", e);
		return new VkglErrorResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(TooManyQueriesException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public VkglErrorResponse handleTooManyQueriesException(TooManyQueriesException e)
	{
		LOG.debug("No hits found: ", e);
		return new VkglErrorResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(StartPositionBeforeEndPositionException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public VkglErrorResponse handleStartPositionBeforeEndPositionException(StartPositionBeforeEndPositionException e)
	{
		LOG.debug("No hits found: ", e);
		return new VkglErrorResponse(new ErrorMessage(e.getMessage()));
	}

}
