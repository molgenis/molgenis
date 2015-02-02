package org.molgenis.vkgl.api;

import static org.molgenis.data.rest.RestController.BASE_URI;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.Arrays;
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

import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.token.TokenService;

import org.molgenis.util.ResourceFingerprintRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(BASE_URI)
public class VkglRest
{

	private static final Logger LOG = LoggerFactory.getLogger(RestController.class);
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
	{

		Map<String, List<String>> positionsToQueryId = new HashMap<>();
		for (VkglAlleleQuery query : allQueries)
		{
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
								positionsToQueryId.get(pos).add(query.getId());
							}
							else
							{
								List<String> iDs = new ArrayList<>();
								iDs.add(query.getId());
								positionsToQueryId.put(pos, iDs);
							}

						}

					}
				}

			}
			if (query.getSource().equals("GRCBUILD"))
			{
				for (int start = Integer.parseInt(query.getStart()); start < Integer.parseInt(query.getEnd()); start++)
				{
					String chr = query.getReference().split("\\.")[0];

					String pos = chr + ":" + start + ":" + query.getReference().split("\\.")[1];

					if (positionsToQueryId.containsKey(pos))
					{

						positionsToQueryId.get(pos).add(query.getId());
					}
					else
					{
						List<String> iDs = new ArrayList<>();
						iDs.add(query.getId());
						positionsToQueryId.put(pos, iDs);
					}
				}
			}

		}

		return positionsToQueryId;
	}

	public Map<String, List<String>> getPositions(VkglCoordinateQuery[] allQueries)
	{

		Map<String, List<String>> positionsToQueryId = new HashMap<>();
		for (VkglCoordinateQuery query : allQueries)
		{
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
								positionsToQueryId.get(pos).add(query.getId());
							}
							else
							{
								List<String> iDs = new ArrayList<>();
								iDs.add(query.getId());
								positionsToQueryId.put(pos, iDs);
							}

						}

					}
				}

			}
			if (query.getSource().equals("GRCBUILD"))
			{
				for (int start = Integer.parseInt(query.getStart()); start < Integer.parseInt(query.getEnd()); start++)
				{
					String chr = query.getReference().split("\\.")[0];

					String pos = chr + ":" + start + ":" + query.getReference().split("\\.")[1];

					if (positionsToQueryId.containsKey(pos))
					{

						positionsToQueryId.get(pos).add(query.getId());
					}
					else
					{
						List<String> iDs = new ArrayList<>();
						iDs.add(query.getId());
						positionsToQueryId.put(pos, iDs);
					}
				}
			}

		}

		return positionsToQueryId;
	}

	public ArrayList<VkglResult> getQAll(VkglCoordinateQuery[] allQueries, String queryStatement)
	{
		String entityName = "vkgl_vkgl";
		EntityMetaData entityMeta = dataService.getEntityMetaData(entityName);
		String reference = "";
		AttributeMetaData posXAttributeMeta = entityMeta.getAttribute("ALLELE1");
		AttributeMetaData posYAttributeMeta = entityMeta.getAttribute("ALLELE2");
		int positionStart = 0;
		int positionEnd = 0;
		String chr = "";
		String[] statement = queryStatement.split("(?<=[-+*/\\|\\!])|(?=[-+*/\\|\\!])");
		System.out.println("statementarray: " + Arrays.toString(statement));

		ArrayList<VkglResult> results = new ArrayList<>();
		Map<String, List<String>> positionsToIds = getPositions(allQueries);
		boolean equationFlag = false;
		for (String position : positionsToIds.keySet())
		{
			Query q = new QueryImpl();
			for (String qEl : statement)
			{

				System.out.println(qEl);
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
				}
				// doesn't contain correct chr and start and end positions

				for (VkglCoordinateQuery query : allQueries)
				{
					System.out.println("#queries: " + allQueries.length);
					for (String id : positionsToIds.get(position))
					{

						if (query.getId().equals(qEl) && query.getId().equals(id))
						{
							System.out.println("id's are correct");
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
							System.out.println("inner query: " + q);
						}
					}
				}

			}
			AggregateQuery newAggregateQuery = new AggregateQueryImpl().attrX(posXAttributeMeta)
					.attrY(posYAttributeMeta).query(q);
			System.out.println("query: " + q);
			AggregateResult result = dataService.aggregate(entityName, newAggregateQuery);

			List<String> resultxLabel = new ArrayList<>();
			List<String> resultyLabel = new ArrayList<>();
			List<List<Long>> resultMatrix = new ArrayList<>();

			// System.out.println(" NEw result: " + result);
			if (result.getxLabels().get(0) == null && result.getyLabels().get(0) == null)
			{
				resultxLabel.add("Less then 10 found");
				resultyLabel.add("Less then 10 found");
				System.out.println(dataService.count(entityName, new QueryImpl()));
				List<Long> totalLengthRepo = new ArrayList<Long>();
				totalLengthRepo.add(dataService.count(entityName, new QueryImpl()));
				resultMatrix.add(totalLengthRepo);
			}
			else
			{
				resultxLabel = result.getxLabels();
				resultyLabel = result.getyLabels();
				resultMatrix = result.getMatrix();
			}

			VkglResult vkglResult = new VkglResult();
			vkglResult.setChromosome(chr);
			vkglResult.setPosition(positionStart + 1);
			vkglResult.setResultType("coordinate");
			vkglResult.setResult(resultMatrix);
			vkglResult.setReferenceAllele(resultxLabel);
			vkglResult.setAlternativeAllele(resultyLabel);

			vkglResult.setReference(reference);

			results.add(vkglResult);

		}
	
		return results;
	}

	public ArrayList<VkglResult> getAlleleResults(VkglAlleleQuery[] allQueries, String queryStatement)
	{
		String entityName = "vkgl_vkgl";
		EntityMetaData entityMeta = dataService.getEntityMetaData(entityName);
		String reference = "";
		AttributeMetaData posXAttributeMeta = entityMeta.getAttribute("ALLELE1");
		AttributeMetaData posYAttributeMeta = entityMeta.getAttribute("ALLELE2");
		int positionStart = 0;
		int positionEnd = 0;
		String chr = "";
		String[] statement = null;

		statement = queryStatement.split("(?<=[-+*/\\|\\!])|(?=[-+*/\\|\\!])");

		ArrayList<VkglResult> results = new ArrayList<>();
		Map<String, List<String>> positionsToIds = getAllelePositions(allQueries);
		boolean equationFlag = false;
		for (String position : positionsToIds.keySet())
		{
			Query q = new QueryImpl();
			for (String qEl : statement)
			{

				System.out.println(qEl);
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
				}
				// doesn't contain correct chr and start and end positions

				for (VkglAlleleQuery query : allQueries)
				{
					for (String id : positionsToIds.get(position))
					{
						if (query.getId().equals(qEl) && query.getId().equals(id))
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
								for (String allele : query.getAllele_sequence())
								{

									q = q.and().nest().eq("ALLELE1", allele).or().eq("ALLELE2", allele).unnest();
								}
								q.unnest();

							}
							else if (query.getOperator().equals("NOT"))
							{
								q = q.nest().eq("#CHROM", chr).and().rng("POS", positionStart, positionEnd);
								for (String allele : query.getAllele_sequence())
								{
									q = q.and().not().eq("ALLELE1", allele).and().not().eq("ALLELE2", allele);
								}
								q = q.unnest();
							}
						}
					}
				}

			}
			
			AggregateQuery newAggregateQuery = new AggregateQueryImpl().attrX(posXAttributeMeta)
					.attrY(posYAttributeMeta).query(q);

			AggregateResult result = dataService.aggregate(entityName, newAggregateQuery);

			List<String> resultxLabel = new ArrayList<>();
			List<String> resultyLabel = new ArrayList<>();
			List<List<Long>> resultMatrix = new ArrayList<>();

			// System.out.println(" NEw result: " + result);
			if (result.getxLabels().get(0) == null && result.getyLabels().get(0) == null)
			{
				resultxLabel.add("Less then 10 found");
				resultyLabel.add("Less then 10 found");
				
				List<Long> totalLengthRepo = new ArrayList<Long>();
				totalLengthRepo.add(dataService.count(entityName, new QueryImpl()));
				resultMatrix.add(totalLengthRepo);
			}
			else
			{
				resultxLabel = result.getxLabels();
				resultyLabel = result.getyLabels();
				resultMatrix = result.getMatrix();
			}

			VkglResult vkglResult = new VkglResult();
			vkglResult.setChromosome(chr);
			vkglResult.setPosition(positionStart + 1);
			vkglResult.setResultType("coordinate");
			vkglResult.setResult(resultMatrix);
			vkglResult.setReferenceAllele(resultxLabel);
			vkglResult.setAlternativeAllele(resultyLabel);

			vkglResult.setReference(reference);

			results.add(vkglResult);

		}
		
		return results;
	}

	@RequestMapping(value = "/getAggregate", method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public VkglResponse vkglQueryResponse(@Valid @RequestBody VkglRequest request)
	{
		request = request != null ? request : new VkglRequest();

		ArrayList<VkglResult> results = new ArrayList<>();

		if (request.getQuery().getCoordinate() != null)
		{
			
			results = getQAll(request.getQuery().getCoordinate(), request.getQuery().getQueryStatement());
		}

		// if its an allele query
		if (request.getQuery().getAllele() != null)
		{
			
			results = getAlleleResults(request.getQuery().getAllele(), request.getQuery().getQueryStatement());
		}

		VkglResponse vkglResponse = new VkglResponse();
		VkglResponseMetadata vkglMetadata = new VkglResponseMetadata();
		vkglMetadata.setTotal(results.size());
		/** TODO
		 *  add support for paging on this request
		 *  
		 */
		vkglMetadata.setNum(0);
		vkglMetadata.setHref("not supported");
		vkglMetadata.setNextHref("not supported");
		vkglMetadata.setPrevHref("not supported yet");

		vkglResponse.setMetadata(vkglMetadata);
		vkglResponse.setResults(results);

		return vkglResponse;
 
	}
}
