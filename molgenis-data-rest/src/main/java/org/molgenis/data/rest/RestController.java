package org.molgenis.data.rest;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DATE;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DATE_TIME;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;
import static org.molgenis.data.rest.RestController.BASE_URI;
import static org.molgenis.ui.MolgenisPluginAttributes.KEY_RESOURCE_FINGERPRINT_REGISTRY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityCollection;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Queryable;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.data.rsql.MolgenisRSQL;
import org.molgenis.data.support.AggregateQueryImpl;
import org.molgenis.data.support.DefaultEntityCollection;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.fieldtypes.BoolField;
import org.molgenis.framework.db.EntityNotFoundException;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.token.TokenExtractor;
import org.molgenis.security.token.TokenService;
import org.molgenis.security.token.UnknownTokenException;
import org.molgenis.ui.form.EntityForm;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.molgenis.util.MolgenisDateFormat;
import org.molgenis.util.ResourceFingerprintRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.data.domain.Sort;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import cz.jirutka.rsql.parser.RSQLParserException;

/**
 * Rest endpoint for the DataService
 * 
 * Query, create, update and delete entities.
 * 
 * If a repository isn't capable of doing the requested operation an error is thrown.
 * 
 * Response is json.
 * 
 * @author erwin
 */
@Controller
@RequestMapping(BASE_URI)
public class RestController
{
	private static final Logger LOG = LoggerFactory.getLogger(RestController.class);

	public static final String BASE_URI = "/api/v1";
	private static final Pattern PATTERN_EXPANDS = Pattern.compile("([^\\[^\\]]+)(?:\\[(.+)\\])?");
	private final DataService dataService;
	private final WritableMetaDataService metaDataService;
	private final TokenService tokenService;
	private final AuthenticationManager authenticationManager;
	private final String ENTITY_FORM_MODEL_ATTRIBUTE = "form";
	private final MolgenisPermissionService molgenisPermissionService;
	private final MolgenisRSQL molgenisRSQL;
	private final ResourceFingerprintRegistry resourceFingerprintRegistry;

	@Autowired
	public RestController(DataService dataService, WritableMetaDataService metaDataService, TokenService tokenService,
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
		this.metaDataService = metaDataService;
		this.tokenService = tokenService;
		this.authenticationManager = authenticationManager;
		this.molgenisPermissionService = molgenisPermissionService;
		this.molgenisRSQL = molgenisRSQL;
		this.resourceFingerprintRegistry = resourceFingerprintRegistry;
	}

	/**
	 * Checks if an entity exists.
	 */
	@RequestMapping(value = "/{entityName}/exist", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public boolean entityExists(@PathVariable("entityName") String entityName)
	{
		try
		{
			dataService.getRepositoryByEntityName(entityName);
			return true;
		}
		catch (UnknownEntityException e)
		{
			return false;
		}
	}

	/**
	 * Gets the metadata for an entity
	 * 
	 * Example url: /api/v1/person/meta
	 * 
	 * @param entityName
	 * @return EntityMetaData
	 */
	@RequestMapping(value = "/{entityName}/meta", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public EntityMetaDataResponse retrieveEntityMeta(@PathVariable("entityName") String entityName,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributeSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		EntityMetaData meta = dataService.getEntityMetaData(entityName);
		return new EntityMetaDataResponse(meta, attributeSet, attributeExpandSet);
	}

	/**
	 * Same as retrieveEntityMeta (GET) only tunneled through POST.
	 * 
	 * Example url: /api/v1/person/meta?_method=GET
	 * 
	 * @param entityName
	 * @return EntityMetaData
	 */
	@RequestMapping(value = "/{entityName}/meta", method = POST, params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public EntityMetaDataResponse retrieveEntityMetaPost(@PathVariable("entityName") String entityName,
			@Valid @RequestBody EntityMetaRequest request)
	{
		Set<String> attributesSet = toAttributeSet(request != null ? request.getAttributes() : null);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(request != null ? request.getExpand() : null);

		EntityMetaData meta = dataService.getEntityMetaData(entityName);
		return new EntityMetaDataResponse(meta, attributesSet, attributeExpandSet);
	}

	/**
	 * Example url: /api/v1/person/meta/emailaddresses
	 * 
	 * @param entityName
	 * @return EntityMetaData
	 */
	@RequestMapping(value = "/{entityName}/meta/{attributeName}", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public AttributeMetaDataResponse retrieveEntityAttributeMeta(@PathVariable("entityName") String entityName,
			@PathVariable("attributeName") String attributeName,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributeSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		return getAttributeMetaDataPostInternal(entityName, attributeName, attributeSet, attributeExpandSet);
	}

	/**
	 * Same as retrieveEntityAttributeMeta (GET) only tunneled through POST.
	 * 
	 * @param entityName
	 * @return EntityMetaData
	 */
	@RequestMapping(value = "/{entityName}/meta/{attributeName}", method = POST, params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public AttributeMetaDataResponse retrieveEntityAttributeMetaPost(@PathVariable("entityName") String entityName,
			@PathVariable("attributeName") String attributeName, @Valid @RequestBody EntityMetaRequest request)
	{
		Set<String> attributeSet = toAttributeSet(request != null ? request.getAttributes() : null);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(request != null ? request.getExpand() : null);

		return getAttributeMetaDataPostInternal(entityName, attributeName, attributeSet, attributeExpandSet);
	}

	/**
	 * Get's an entity by it's id
	 * 
	 * Examples:
	 * 
	 * /api/v1/person/99 Retrieves a person with id 99
	 * 
	 * @param entityName
	 * @param id
	 * @param attributeExpands
	 * @return
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}/{id:.+}", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> retrieveEntity(@PathVariable("entityName") String entityName,
			@PathVariable("id") Object id, @RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{

		Set<String> attributesSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		EntityMetaData meta = dataService.getEntityMetaData(entityName);
		Entity entity = dataService.findOne(entityName, id);

		if (entity == null)
		{
			throw new UnknownEntityException(entityName + " " + id + " not found");
		}

		return getEntityAsMap(entity, meta, attributesSet, attributeExpandSet);
	}

	/**
	 * Same as retrieveEntity (GET) only tunneled through POST.
	 * 
	 * @param entityName
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{entityName}/{id:.+}", method = POST, params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> retrieveEntity(@PathVariable("entityName") String entityName,
			@PathVariable("id") Object id, @Valid @RequestBody EntityMetaRequest request)
	{
		Set<String> attributesSet = toAttributeSet(request != null ? request.getAttributes() : null);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(request != null ? request.getExpand() : null);

		EntityMetaData meta = dataService.getEntityMetaData(entityName);
		Entity entity = dataService.findOne(entityName, id);

		if (entity == null)
		{
			throw new UnknownEntityException(entityName + " " + id + " not found");
		}

		return getEntityAsMap(entity, meta, attributesSet, attributeExpandSet);
	}

	/**
	 * Get's an XREF entity or a list of MREF entities
	 * 
	 * Example:
	 * 
	 * /api/v1/person/99/address
	 * 
	 * @param entityName
	 * @param id
	 * @param refAttributeName
	 * @param request
	 * @param attributeExpands
	 * @return
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}/{id}/{refAttributeName}", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Object retrieveEntityAttribute(@PathVariable("entityName") String entityName, @PathVariable("id") Object id,
			@PathVariable("refAttributeName") String refAttributeName, @Valid EntityCollectionRequest request,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributesSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		return retrieveEntityAttributeInternal(entityName, id, refAttributeName, request, attributesSet,
				attributeExpandSet);
	}

	/**
	 * Get's an XREF entity or a list of MREF entities
	 * 
	 * Example:
	 * 
	 * /api/v1/person/99/address
	 * 
	 * @param entityName
	 * @param id
	 * @param refAttributeName
	 * @param request
	 * @return
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}/{id}/{refAttributeName}", method = POST, params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Object retrieveEntityAttributePost(@PathVariable("entityName") String entityName,
			@PathVariable("id") Object id, @PathVariable("refAttributeName") String refAttributeName,
			@Valid @RequestBody EntityCollectionRequest request)
	{
		Set<String> attributesSet = toAttributeSet(request != null ? request.getAttributes() : null);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(request != null ? request.getExpand() : null);

		return retrieveEntityAttributeInternal(entityName, id, refAttributeName, request, attributesSet,
				attributeExpandSet);
	}

	public ArrayList<VkglResult> getAlleleQueryResults(VkglAlleleQuery alleleQuery, String reference, String chr,
			int positionStart, int positionEnd, ArrayList<VkglResult> results)
	{

		String entityName = "vkgl_vkgl";
		EntityMetaData entityMeta = dataService.getEntityMetaData(entityName);

		AttributeMetaData posXAttributeMeta = entityMeta.getAttribute("ALLELE1");
		AttributeMetaData posYAttributeMeta = entityMeta.getAttribute("ALLELE2");

		// int positionStart = Integer.parseInt(request.getQuery().getAllele()[i].getStart())
		// + (int) e.get("Start");
		// int positionEnd = Integer.parseInt(request.getQuery().getAllele()[i].getEnd())
		// + (int) e.get("Start");

		for (int position = positionStart; position < positionEnd - 1; position++)

		{

			for (int j = 0; j < alleleQuery.getAllele_sequence().length; j++)
			{
				Query q = new QueryImpl();

				if (alleleQuery.getOperator().equals("IS"))
				{

					q = q.eq("#CHROM", chr).and().rng("POS", positionStart, positionEnd).and().nest()
							.eq("ALLELE1", alleleQuery.getAllele_sequence()).or()
							.eq("ALLELE2", alleleQuery.getAllele_sequence()).unnest();
				}
				else if (alleleQuery.getOperator().equals("NOT"))
				{
					// Query q = new QueryImpl().not().eq(boolAttributeName, value);
					// "queryStatement" : "(#1 & #2) | #3"
					q = q.eq("#CHROM", chr).and().rng("POS", positionStart, positionEnd).and().not()
							.eq("ALLELE1", alleleQuery.getAllele_sequence()[j]).and().not()
							.eq("ALLELE2", alleleQuery.getAllele_sequence());
				}
				AggregateQuery alleleAggregateQuery = new AggregateQueryImpl().attrX(posXAttributeMeta)
						.attrY(posYAttributeMeta).query(q);

				AggregateResult result = dataService.aggregate(entityName, alleleAggregateQuery);

				List<String> resultxLabel = new ArrayList<>();
				List<String> resultyLabel = new ArrayList<>();
				List<List<Long>> resultMatrix = new ArrayList<>();
				if (result.getxLabels().get(0) == null && result.getyLabels().get(0) == null)
				{
					resultxLabel.add("No hits found");
					resultyLabel.add("No hits found");
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
		}

		return results;
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
							// System.out.println("Postitions for id  " + query.getId() + ": " + chr + ":" + start + ":"
							// + reference);
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
							// System.out.println("Postitions for id  " + query.getId() + ": " + chr + ":" + start + ":"
							// + reference);
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
					System.out.println("#queries: "+ allQueries.length);
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
		// System.out.println("new query: " + q);
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
		if (queryStatement.split("(?<=[-+*/\\|\\!])|(?=[-+*/\\|\\!])") == null)
		{
			statement[0] = queryStatement;
		}
		else
		{
			statement = queryStatement.split("(?<=[-+*/\\|\\!])|(?=[-+*/\\|\\!])");
		//	System.out.println("statementarray: " + Arrays.toString(statement));
		}
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
							System.out.println(positionsToIds.get(position).size());
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
			System.out.println("query: " + q);
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
		// System.out.println("new query: " + q);
		return results;
	}

	/**
	 * Do a VKGL aggregate query
	 * 
	 * Will return aggregate of all hits.
	 * 
	 * Returns json
	 * 
	 * @param entityName
	 * @param request
	 * @param attributeExpands
	 * @return
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/vkgl", method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public VkglResponse vkglQueryResponse(@Valid @RequestBody VkglRequest request)
	{
		request = request != null ? request : new VkglRequest();
		// String chr = "";

		ArrayList<VkglResult> results = new ArrayList<>();

		if (request.getQuery().getCoordinate() != null)
		{
			System.out.println("coordinate");
			results = getQAll(request.getQuery().getCoordinate(), request.getQuery().getQueryStatement());
		}

		// if its an allele query
		if (request.getQuery().getAllele() != null)
		{
			System.out.println("allele");
			System.out.println(request.getQuery().getQueryStatement());
			results = getAlleleResults(request.getQuery().getAllele(), request.getQuery().getQueryStatement());

			/**
			 * // build up the query per requested allele for (int i = 0; i < request.getQuery().getAllele().length;
			 * i++) { VkglAlleleQuery alleleQuery = request.getQuery().getAllele()[i]; HashMap<String, Iterable<Entity>>
			 * lookupTables = new HashMap<String, Iterable<Entity>>(); // check if its a HGNC source request if
			 * (request.getQuery().getAllele()[i].getSource().equals("HGNC")) { String geneSymbol =
			 * request.getQuery().getAllele()[i].getReference();
			 * 
			 * // get all lookup tables to translate the HGNC genesymbol to the locations across different builds
			 * lookupTables.put("GRCh37", dataService.findAll("GRCh37", new QueryImpl().eq("HGNC", geneSymbol)));
			 * lookupTables.put("GRCh38", dataService.findAll("GRCh38", new QueryImpl().eq("HGNC", geneSymbol)));
			 * 
			 * // for each look up table execute allele query for (String reference : lookupTables.keySet()) {
			 * System.out.println(reference); Entity lookupTable = lookupTables.get(reference).iterator().next();
			 * 
			 * // for each location between start and stop perform the query to secure every allele1 vs allele2 // per
			 * genetic position for (int position = Integer.parseInt(request.getQuery().getAllele()[i].getStart());
			 * position < Integer .parseInt(request.getQuery().getAllele()[i].getEnd()) - 1; position++) { chr =
			 * (String) lookupTable.get("Chromosome"); // get relative stop position from lookuptable and query position
			 * int positionStart = position + (int) lookupTable.get("Start"); int positionEnd = position + (int)
			 * lookupTable.get("Start") + 2; results = getAlleleQueryResults(alleleQuery, reference, chr, positionStart,
			 * positionEnd, results); System.out.println("New method outcome: " + results.get(0).getReferenceAllele());
			 * } }
			 * 
			 * } // check if its a GRCBUILD source request else if
			 * (request.getQuery().getAllele()[i].getSource().equals("GRCBUILD")) {
			 * 
			 * // for each location between start and stop perform the query to secure every allele1 vs allele2 per //
			 * genetic position for (int position = Integer.parseInt(request.getQuery().getAllele()[i].getStart());
			 * position < Integer .parseInt(request.getQuery().getAllele()[i].getEnd()) - 1; position++) { // chr.build
			 * 
			 * String[] source = request.getQuery().getAllele()[i].getReference().split("\\."); chr = source[0];
			 * System.out.println("chr: " + chr); int positionStart = position; int positionEnd = position + 2; results
			 * = getAlleleQueryResults(alleleQuery, source[1], chr, positionStart, positionEnd, results); } } }
			 */
		}

		VkglResponse vkglResponse = new VkglResponse();
		VkglResponseMetadata vkglMetadata = new VkglResponseMetadata();
		vkglMetadata.setTotal(results.size());
		vkglMetadata.setNum(0);
		vkglMetadata.setHref("not supported");
		vkglMetadata.setNextHref("not supported");
		vkglMetadata.setPrevHref("not supported yet");

		vkglResponse.setMetadata(vkglMetadata);
		vkglResponse.setResults(results);

		return vkglResponse;
	}

	/**
	 * Do a query
	 * 
	 * Returns json
	 * 
	 * @param entityName
	 * @param request
	 * @param attributeExpands
	 * @return
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public EntityCollectionResponse retrieveEntityCollection(@PathVariable("entityName") String entityName,
			@Valid EntityCollectionRequest request,
			@RequestParam(value = "attributes", required = false) String[] attributes,
			@RequestParam(value = "expand", required = false) String[] attributeExpands)
	{
		Set<String> attributesSet = toAttributeSet(attributes);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(attributeExpands);

		return retrieveEntityCollectionInternal(entityName, request, attributesSet, attributeExpandSet);
	}

	/**
	 * Same as retrieveEntityCollection (GET) only tunneled through POST.
	 * 
	 * Example url: /api/v1/person?_method=GET
	 * 
	 * Returns json
	 * 
	 * @param request
	 * @param entityName
	 * @return
	 */
	@RequestMapping(value = "/{entityName}", method = POST, params = "_method=GET", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public EntityCollectionResponse retrieveEntityCollectionPost(@PathVariable("entityName") String entityName,
			@Valid @RequestBody EntityCollectionRequest request)
	{
		Set<String> attributesSet = toAttributeSet(request != null ? request.getAttributes() : null);
		Map<String, Set<String>> attributeExpandSet = toExpandMap(request != null ? request.getExpand() : null);

		request = request != null ? request : new EntityCollectionRequest();

		return retrieveEntityCollectionInternal(entityName, request, attributesSet, attributeExpandSet);
	}

	/**
	 * Does a rsql/fiql query, returns the result as csv
	 * 
	 * Parameters:
	 * 
	 * q: the query
	 * 
	 * attributes: the attributes to return, if not specified returns all attributes
	 * 
	 * start: the index of the first row, default 0
	 * 
	 * num: the number of results to return, default 100, max 100000
	 * 
	 * 
	 * Example: /api/v1/csv/person?q=firstName==Piet&attributes=firstName,lastName&start=10&num=100
	 * 
	 * @param entityName
	 * @param attributes
	 * @param req
	 * @param resp
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/csv/{entityName}", method = GET, produces = "text/csv")
	@ResponseBody
	public EntityCollection retrieveEntityCollection(@PathVariable("entityName") String entityName,
			@RequestParam(value = "attributes", required = false) String[] attributes, HttpServletRequest req,
			HttpServletResponse resp) throws IOException
	{
		final Set<String> attributesSet = toAttributeSet(attributes);

		EntityMetaData meta;
		Iterable<Entity> entities;
		try
		{
			meta = dataService.getEntityMetaData(entityName);
			Query q = new QueryStringParser(meta, molgenisRSQL).parseQueryString(req.getParameterMap());

			String[] sortAttributeArray = req.getParameterMap().get("sortColumn");
			if (sortAttributeArray != null && sortAttributeArray.length == 1
					&& StringUtils.isNotEmpty(sortAttributeArray[0]))
			{
				String sortAttribute = sortAttributeArray[0];
				String sortOrderArray[] = req.getParameterMap().get("sortOrder");
				Sort.Direction order = Sort.DEFAULT_DIRECTION;

				if (sortOrderArray != null && sortOrderArray.length == 1 && StringUtils.isNotEmpty(sortOrderArray[0]))
				{
					String sortOrder = sortOrderArray[0];
					if (sortOrder.equals("ASC"))
					{
						order = Sort.Direction.ASC;
					}
					else if (sortOrder.equals("DESC"))
					{
						order = Sort.Direction.DESC;
					}
					else
					{
						throw new RuntimeException("unknown sort order");
					}
				}
				q.sort(order, sortAttribute);
			}

			if (q.getPageSize() == 0)
			{
				q.pageSize(EntityCollectionRequest.DEFAULT_ROW_COUNT);
			}

			if (q.getPageSize() > EntityCollectionRequest.MAX_ROWS)
			{
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Num exceeded the maximum of "
						+ EntityCollectionRequest.MAX_ROWS + " rows");
				return null;
			}

			entities = dataService.findAll(entityName, q);
		}
		catch (ConversionFailedException | RSQLParserException | UnknownAttributeException | IllegalArgumentException
				| UnsupportedOperationException | UnknownEntityException e)
		{
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return null;
		}
		catch (MolgenisDataAccessException e)
		{
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		// Check attribute names
		Iterable<String> attributesIterable = Iterables.transform(meta.getAtomicAttributes(),
				new Function<AttributeMetaData, String>()
				{
					@Override
					public String apply(AttributeMetaData attributeMetaData)
					{
						return attributeMetaData.getName().toLowerCase();
					}
				});

		if (attributesSet != null)
		{
			SetView<String> diff = Sets.difference(attributesSet, Sets.newHashSet(attributesIterable));
			if (!diff.isEmpty())
			{
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown attributes " + diff);
				return null;
			}
		}

		attributesIterable = Iterables.transform(meta.getAtomicAttributes(), new Function<AttributeMetaData, String>()
		{
			@Override
			public String apply(AttributeMetaData attributeMetaData)
			{
				return attributeMetaData.getName();
			}
		});
		// 69881447

		if (attributesSet != null)
		{
			attributesIterable = Iterables.filter(attributesIterable, new Predicate<String>()
			{
				@Override
				public boolean apply(@Nullable String attribute)
				{
					return attributesSet.contains(attribute.toLowerCase());
				}
			});
		}

		return new DefaultEntityCollection(entities, attributesIterable);
	}

	/**
	 * Creates a new entity from a html form post.
	 * 
	 * @param entityName
	 * @param request
	 * @param response
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}", method = POST, headers = "Content-Type=application/x-www-form-urlencoded")
	public void createFromFormPost(@PathVariable("entityName") String entityName, HttpServletRequest request,
			HttpServletResponse response)
	{
		Map<String, Object> paramMap = new HashMap<String, Object>();
		for (String param : request.getParameterMap().keySet())
		{
			String value = request.getParameter(param);
			if (StringUtils.isNotBlank(value))
			{
				paramMap.put(param, value);
			}
		}

		createInternal(entityName, paramMap, response);
	}

	@RequestMapping(value = "/{entityName}", method = POST)
	public void create(@PathVariable("entityName") String entityName, @RequestBody Map<String, Object> entityMap,
			HttpServletResponse response) throws EntityNotFoundException
	{
		if (entityMap == null)
		{
			throw new UnknownEntityException("Missing entity in body");
		}

		createInternal(entityName, entityMap, response);
	}

	/**
	 * Updates an entity using PUT
	 * 
	 * Example url: /api/v1/person/99
	 * 
	 * @param entityName
	 * @param id
	 * @param entityMap
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = PUT)
	@ResponseStatus(OK)
	public void update(@PathVariable("entityName") String entityName, @PathVariable("id") Object id,
			@RequestBody Map<String, Object> entityMap)
	{
		updateInternal(entityName, id, entityMap);
	}

	/**
	 * Updates an entity by tunneling PUT through POST
	 * 
	 * Example url: /api/v1/person/99?_method=PUT
	 * 
	 * @param entityName
	 * @param id
	 * @param entityMap
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = POST, params = "_method=PUT")
	@ResponseStatus(OK)
	public void updatePost(@PathVariable("entityName") String entityName, @PathVariable("id") Object id,
			@RequestBody Map<String, Object> entityMap)
	{
		updateInternal(entityName, id, entityMap);
	}

	@RequestMapping(value = "/{entityName}/{id}/{attributeName}", method = POST, params = "_method=PUT")
	@ResponseStatus(OK)
	public void updateAttribute(@PathVariable("entityName") String entityName,
			@PathVariable("attributeName") String attributeName, @PathVariable("id") Object id,
			@RequestBody Object paramValue)
	{
		Entity entity = dataService.findOne(entityName, id);
		if (entity == null)
		{
			throw new UnknownEntityException("Entity of type " + entityName + " with id " + id + " not found");
		}

		EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);
		AttributeMetaData attr = entityMetaData.getAttribute(attributeName);
		if (attr == null)
		{
			throw new UnknownAttributeException("Attribute '" + attributeName + "' of entity '" + entityName
					+ "' does not exist");
		}

		if (attr.isReadonly())
		{
			throw new MolgenisDataAccessException("Attribute '" + attributeName + "' of entity '" + entityName
					+ "' is readonly");
		}

		Object value = toEntityValue(attr, paramValue);
		entity.set(attributeName, value);
		dataService.update(entityName, entity);
	}

	/**
	 * Updates an entity from a html form post.
	 * 
	 * Tunnels PUT through POST
	 * 
	 * Example url: /api/v1/person/99?_method=PUT
	 * 
	 * @param entityName
	 * @param id
	 * @param request
	 * @throws UnknownEntityException
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = POST, params = "_method=PUT", headers = "Content-Type=application/x-www-form-urlencoded")
	@ResponseStatus(NO_CONTENT)
	public void updateFromFormPost(@PathVariable("entityName") String entityName, @PathVariable("id") Object id,
			HttpServletRequest request)
	{
		Object typedId = dataService.getRepositoryByEntityName(entityName).getEntityMetaData().getIdAttribute()
				.getDataType().convert(id);

		Map<String, Object> paramMap = new HashMap<String, Object>();
		for (String param : request.getParameterMap().keySet())
		{
			paramMap.put(param, request.getParameter(param));
		}

		updateInternal(entityName, typedId, paramMap);
	}

	/**
	 * Deletes an entity by it's id
	 * 
	 * @param entityName
	 * @param id
	 * @throws EntityNotFoundException
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = DELETE)
	@ResponseStatus(NO_CONTENT)
	public void delete(@PathVariable("entityName") String entityName, @PathVariable Object id)
	{
		Object typedId = dataService.getRepositoryByEntityName(entityName).getEntityMetaData().getIdAttribute()
				.getDataType().convert(id);
		dataService.delete(entityName, typedId);
	}

	/**
	 * Deletes an entity by it's id but tunnels DELETE through POST
	 * 
	 * Example url: /api/v1/person/99?_method=DELETE
	 * 
	 * @param entityName
	 * @param id
	 * @throws EntityNotFoundException
	 */
	@RequestMapping(value = "/{entityName}/{id}", method = POST, params = "_method=DELETE")
	@ResponseStatus(NO_CONTENT)
	public void deletePost(@PathVariable("entityName") String entityName, @PathVariable Object id)
	{
		delete(entityName, id);
	}

	/**
	 * Deletes all entities for the given entity name
	 * 
	 * @param entityName
	 * @param id
	 * @throws EntityNotFoundException
	 */
	@RequestMapping(value = "/{entityName}", method = DELETE)
	@ResponseStatus(NO_CONTENT)
	public void deleteAll(@PathVariable("entityName") String entityName)
	{
		dataService.deleteAll(entityName);
	}

	/**
	 * Deletes all entities for the given entity name but tunnels DELETE through POST
	 * 
	 * @param entityName
	 * @param id
	 * @throws EntityNotFoundException
	 */
	@RequestMapping(value = "/{entityName}", method = POST, params = "_method=DELETE")
	@ResponseStatus(NO_CONTENT)
	public void deleteAllPost(@PathVariable("entityName") String entityName)
	{
		dataService.deleteAll(entityName);
	}

	/**
	 * Deletes all entities and entity meta data for the given entity name
	 * 
	 * @param entityName
	 * @param id
	 * @throws EntityNotFoundException
	 */
	@RequestMapping(value = "/{entityName}/meta", method = DELETE)
	@ResponseStatus(NO_CONTENT)
	@Transactional
	public void deleteMeta(@PathVariable("entityName") String entityName)
	{
		deleteMetaInternal(entityName);
	}

	/**
	 * Deletes all entities and entity meta data for the given entity name but tunnels DELETE through POST
	 * 
	 * @param entityName
	 * @param id
	 * @throws EntityNotFoundException
	 */
	@RequestMapping(value = "/{entityName}/meta", method = POST, params = "_method=DELETE")
	@ResponseStatus(NO_CONTENT)
	@Transactional
	public void deleteMetaPost(@PathVariable("entityName") String entityName)
	{
		deleteMetaInternal(entityName);
	}

	private void deleteMetaInternal(String entityName)
	{
		dataService.drop(entityName);
		dataService.removeRepository(entityName);
		metaDataService.removeEntityMetaData(entityName);
		metaDataService.refreshCaches();
	}

	@RequestMapping(value = "/{entityName}/create", method = GET)
	public String createForm(@PathVariable("entityName") String entityName, Model model)
	{

		Repository repo = dataService.getRepositoryByEntityName(entityName);
		Entity entity = null;
		if (repo.getEntityMetaData().getEntityClass() != Entity.class) entity = BeanUtils.instantiateClass(repo
				.getEntityMetaData().getEntityClass());
		else entity = new MapEntity();
		EntityMetaData entityMeta = repo.getEntityMetaData();
		model.addAttribute(ENTITY_FORM_MODEL_ATTRIBUTE, new EntityForm(entityMeta, true, entity));
		model.addAttribute(KEY_RESOURCE_FINGERPRINT_REGISTRY, resourceFingerprintRegistry);
		model.addAttribute("entity", entity);

		return "view-entity-create";
	}

	@RequestMapping(value = "/{entityName}/{id}/edit", method = GET)
	public String editForm(@PathVariable("entityName") String entityName, @PathVariable Object id, Model model)
	{
		Entity entity = dataService.findOne(entityName, id);

		// dataService.getRepositoryByEntityName(entityName);
		EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);

		boolean hasWritePermission = molgenisPermissionService.hasPermissionOnEntity(entityName, Permission.WRITE);
		model.addAttribute(ENTITY_FORM_MODEL_ATTRIBUTE, new EntityForm(entityMetaData, entity, id, hasWritePermission));
		model.addAttribute(KEY_RESOURCE_FINGERPRINT_REGISTRY, resourceFingerprintRegistry);
		model.addAttribute("entity", entity);

		return "view-entity-edit";
	}

	/**
	 * Login to the api.
	 * 
	 * Returns a json object with a token on correct login else throws an AuthenticationException. Clients can use this
	 * token when calling the api.
	 * 
	 * Example:
	 * 
	 * Request: {username:admin,password:xxx}
	 * 
	 * Response: {token: b4fd94dc-eae6-4d9a-a1b7-dd4525f2f75d}
	 * 
	 * @param login
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/login", method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public LoginResponse login(@Valid @RequestBody LoginRequest login, HttpServletRequest request)
	{
		if (login == null)
		{
			throw new HttpMessageNotReadableException("Missing login");
		}

		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(login.getUsername(),
				login.getPassword());
		authToken.setDetails(new WebAuthenticationDetails(request));

		// Authenticate the login
		Authentication authentication = authenticationManager.authenticate(authToken);
		if (!authentication.isAuthenticated())
		{
			throw new BadCredentialsException("Unknown username or password");
		}

		// User authenticated, log the user in
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// Generate a new token for the user
		String token = tokenService.generateAndStoreToken(authentication.getName(), "Rest api login");

		MolgenisUser user = dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.USERNAME, authentication.getName()), MolgenisUser.class);

		return new LoginResponse(token, user.getUsername(), user.getFirstName(), user.getLastName());
	}

	@RequestMapping("/logout")
	@ResponseStatus(OK)
	public void logout(HttpServletRequest request)
	{
		String token = TokenExtractor.getToken(request);
		if (token == null)
		{
			throw new HttpMessageNotReadableException("Missing token in header");
		}

		tokenService.removeToken(token);
		SecurityContextHolder.getContext().setAuthentication(null);
		request.getSession().invalidate();
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e)
	{
		LOG.error("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(UnknownTokenException.class)
	@ResponseStatus(NOT_FOUND)
	@ResponseBody
	public ErrorMessageResponse handleUnknownTokenException(UnknownTokenException e)
	{
		LOG.debug("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(UnknownEntityException.class)
	@ResponseStatus(NOT_FOUND)
	@ResponseBody
	public ErrorMessageResponse handleUnknownEntityException(UnknownEntityException e)
	{
		LOG.debug("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(UnknownAttributeException.class)
	@ResponseStatus(NOT_FOUND)
	@ResponseBody
	public ErrorMessageResponse handleUnknownAttributeException(UnknownAttributeException e)
	{
		LOG.debug("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(MolgenisValidationException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleMolgenisValidationException(MolgenisValidationException e)
	{
		LOG.info("", e);

		List<ErrorMessage> messages = Lists.newArrayList();
		for (ConstraintViolation violation : e.getViolations())
		{
			messages.add(new ErrorMessage(violation.getMessage()));
		}

		return new ErrorMessageResponse(messages);
	}

	@ExceptionHandler(ConversionException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleConversionException(ConversionException e)
	{
		LOG.info("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(MolgenisDataException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleMolgenisDataException(MolgenisDataException e)
	{
		LOG.error("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(UNAUTHORIZED)
	@ResponseBody
	public ErrorMessageResponse handleAuthenticationException(AuthenticationException e)
	{
		LOG.info("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(MolgenisDataAccessException.class)
	@ResponseStatus(UNAUTHORIZED)
	@ResponseBody
	public ErrorMessageResponse handleMolgenisDataAccessException(MolgenisDataAccessException e)
	{
		LOG.info("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error("", e);
		return new ErrorMessageResponse(new ErrorMessage(e.getMessage()));
	}

	private void updateInternal(String entityName, Object id, Map<String, Object> entityMap)
	{
		EntityMetaData meta = dataService.getEntityMetaData(entityName);
		if (meta.getIdAttribute() == null)
		{
			throw new IllegalArgumentException(entityName + " does not have an id attribute");
		}

		Entity existing = dataService.findOne(entityName, id);
		if (existing == null)
		{
			throw new UnknownEntityException("Entity of type " + entityName + " with id " + id + " not found");
		}

		Entity entity = toEntity(meta, entityMap);
		entity.set(meta.getIdAttribute().getName(), existing.getIdValue());

		dataService.update(entityName, entity);
	}

	private void createInternal(String entityName, Map<String, Object> entityMap, HttpServletResponse response)
	{
		EntityMetaData meta = dataService.getEntityMetaData(entityName);

		Entity entity = toEntity(meta, entityMap);

		dataService.add(entityName, entity);
		Object id = entity.getIdValue();
		if (id != null)
		{
			response.addHeader("Location", String.format(BASE_URI + "/%s/%s", entityName, id));
		}

		response.setStatus(HttpServletResponse.SC_CREATED);
	}

	// Creates a new MapEntity based from a HttpServletRequest
	private Entity toEntity(EntityMetaData meta, Map<String, Object> request)
	{
		Entity entity = new MapEntity();
		if (meta.getIdAttribute() != null) entity = new MapEntity(meta.getIdAttribute().getName());

		for (AttributeMetaData attr : meta.getAtomicAttributes())
		{
			String paramName = attr.getName();
			Object paramValue = request.get(paramName);
			Object value = toEntityValue(attr, paramValue);
			entity.set(attr.getName(), value);
		}

		return entity;
	}

	private Object toEntityValue(AttributeMetaData attr, Object paramValue)
	{
		Object value = null;

		// Treat empty strings as null
		if ((paramValue != null) && (paramValue instanceof String) && StringUtils.isEmpty((String) paramValue))
		{
			paramValue = null;
		}

		// boolean false is not posted (http feature), so if null and required, should be false
		if ((paramValue == null) && (attr.getDataType() instanceof BoolField) && !attr.isNillable())
		{
			value = false;
		}

		if (paramValue != null)
		{
			if (attr.getDataType().getEnumType() == XREF || attr.getDataType().getEnumType() == CATEGORICAL)
			{
				value = dataService.findOne(attr.getRefEntity().getName(), paramValue);
				if (value == null)
				{
					throw new IllegalArgumentException("No " + attr.getRefEntity().getName() + " with id " + paramValue
							+ " found");
				}
			}
			else if (attr.getDataType().getEnumType() == MREF)
			{
				List<Object> ids = DataConverter.toObjectList(paramValue);
				if ((ids != null) && !ids.isEmpty())
				{
					Iterable<Entity> mrefs = dataService.findAll(attr.getRefEntity().getName(), ids);
					List<Entity> mrefList = Lists.newArrayList(mrefs);
					if (mrefList.size() != ids.size())
					{
						throw new IllegalArgumentException("Could not find all referencing ids for  " + attr.getName());
					}

					value = mrefList;
				}
			}
			else
			{
				value = DataConverter.convert(paramValue, attr);
			}
		}
		return value;
	}

	private AttributeMetaDataResponse getAttributeMetaDataPostInternal(String entityName, String attributeName,
			Set<String> attributeSet, Map<String, Set<String>> attributeExpandSet)
	{
		EntityMetaData meta = dataService.getEntityMetaData(entityName);
		AttributeMetaData attributeMetaData = meta.getAttribute(attributeName);
		if (attributeMetaData != null)
		{
			return new AttributeMetaDataResponse(entityName, attributeMetaData, attributeSet, attributeExpandSet);
		}
		else
		{
			throw new UnknownAttributeException(attributeName);
		}
	}

	private Object retrieveEntityAttributeInternal(String entityName, Object id, String refAttributeName,
			EntityCollectionRequest request, Set<String> attributesSet, Map<String, Set<String>> attributeExpandSet)
	{
		EntityMetaData meta = dataService.getEntityMetaData(entityName);

		// Check if the entity has an attribute with name refAttributeName
		AttributeMetaData attr = meta.getAttribute(refAttributeName);
		if (attr == null)
		{
			throw new UnknownAttributeException(entityName + " does not have an attribute named " + refAttributeName);
		}

		// Get the entity
		Entity entity = dataService.findOne(entityName, id);
		if (entity == null)
		{
			throw new UnknownEntityException(entityName + " " + id + " not found");
		}

		String attrHref = String.format(BASE_URI + "/%s/%s/%s", meta.getName(), entity.getIdValue(), refAttributeName);
		switch (attr.getDataType().getEnumType())
		{
			case COMPOUND:
				Map<String, Object> entityHasAttributeMap = new LinkedHashMap<String, Object>();
				entityHasAttributeMap.put("href", attrHref);
				@SuppressWarnings("unchecked")
				Iterable<AttributeMetaData> attributeParts = (Iterable<AttributeMetaData>) entity.get(refAttributeName);
				for (AttributeMetaData attributeMetaData : attributeParts)
				{
					String attrName = attributeMetaData.getName();
					entityHasAttributeMap.put(attrName, entity.get(attrName));
				}
				return entityHasAttributeMap;
			case MREF:
				List<Entity> mrefEntities = new ArrayList<Entity>();
				for (Entity e : entity.getEntities((attr.getName())))
					mrefEntities.add(e);
				int count = mrefEntities.size();
				int toIndex = request.getStart() + request.getNum();
				mrefEntities = mrefEntities.subList(request.getStart(), toIndex > count ? count : toIndex);

				List<Map<String, Object>> refEntityMaps = new ArrayList<Map<String, Object>>();
				for (Entity refEntity : mrefEntities)
				{
					Map<String, Object> refEntityMap = getEntityAsMap(refEntity, attr.getRefEntity(), attributesSet,
							attributeExpandSet);
					refEntityMaps.add(refEntityMap);
				}

				EntityPager pager = new EntityPager(request.getStart(), request.getNum(), (long) count, mrefEntities);
				return new EntityCollectionResponse(pager, refEntityMaps, attrHref);
			case XREF:
				Map<String, Object> entityXrefAttributeMap = getEntityAsMap((Entity) entity.get(refAttributeName),
						attr.getRefEntity(), attributesSet, attributeExpandSet);
				entityXrefAttributeMap.put("href", attrHref);
				return entityXrefAttributeMap;
			default:
				Map<String, Object> entityAttributeMap = new LinkedHashMap<String, Object>();
				entityAttributeMap.put("href", attrHref);
				entityAttributeMap.put(refAttributeName, entity.get(refAttributeName));
				return entityAttributeMap;
		}
	}

	// Handles a Query
	private EntityCollectionResponse retrieveEntityCollectionInternal(String entityName,
			EntityCollectionRequest request, Set<String> attributesSet, Map<String, Set<String>> attributeExpandsSet)
	{
		EntityMetaData meta = dataService.getEntityMetaData(entityName);
		Repository repository = dataService.getRepositoryByEntityName(entityName);

		// TODO non queryable
		List<QueryRule> queryRules = request.getQ() == null ? Collections.<QueryRule> emptyList() : request.getQ();
		Query q = new QueryImpl(queryRules).pageSize(request.getNum()).offset(request.getStart())
				.sort(request.getSort());

		Iterable<Entity> it = dataService.findAll(entityName, q);
		Long count = ((Queryable) repository).count(q);
		EntityPager pager = new EntityPager(request.getStart(), request.getNum(), count, it);

		List<Map<String, Object>> entities = new ArrayList<Map<String, Object>>();
		for (Entity entity : it)
		{
			entities.add(getEntityAsMap(entity, meta, attributesSet, attributeExpandsSet));
		}

		return new EntityCollectionResponse(pager, entities, BASE_URI + "/" + entityName);
	}

	// Transforms an entity to a Map so it can be transformed to json
	private Map<String, Object> getEntityAsMap(Entity entity, EntityMetaData meta, Set<String> attributesSet,
			Map<String, Set<String>> attributeExpandsSet)
	{
		if (null == entity) throw new IllegalArgumentException("entity is null");

		if (null == meta) throw new IllegalArgumentException("meta is null");

		Map<String, Object> entityMap = new LinkedHashMap<String, Object>();
		try
		{
			entityMap
					.put("href", (String.format(BASE_URI + "/%s/%s", meta.getName(),
							new URI(null, DataConverter.toString(entity.getIdValue()), null).toASCIIString(), "UTF-8")));
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}

		// TODO system fields
		for (AttributeMetaData attr : meta.getAtomicAttributes())
		{
			// filter fields
			if (attributesSet != null && !attributesSet.contains(attr.getName().toLowerCase())) continue;

			// TODO remove __Type from jpa entities
			if (attr.isVisible() && !attr.getName().equals("__Type"))
			{
				String attrName = attr.getName();
				FieldTypeEnum attrType = attr.getDataType().getEnumType();

				if (attrType == COMPOUND)
				{
					if (attributeExpandsSet != null && attributeExpandsSet.containsKey(attrName.toLowerCase()))
					{
						Set<String> subAttributesSet = attributeExpandsSet.get(attrName.toLowerCase());
						entityMap.put(attrName, new AttributeMetaDataResponse(meta.getName(), attr, subAttributesSet,
								null));
					}
					else
					{
						String attrHref = String.format(BASE_URI + "/%s/%s/%s", meta.getName(), entity.getIdValue(),
								attrName);
						entityMap.put(attrName, Collections.singletonMap("href", attrHref));
					}
				}
				else if (attrType == DATE)
				{
					Date date = entity.getDate(attrName);
					entityMap
							.put(attrName,
									date != null ? new SimpleDateFormat(MolgenisDateFormat.DATEFORMAT_DATE)
											.format(date) : null);
				}
				else if (attrType == DATE_TIME)
				{
					Date date = entity.getDate(attrName);
					entityMap
							.put(attrName,
									date != null ? new SimpleDateFormat(MolgenisDateFormat.DATEFORMAT_DATETIME)
											.format(date) : null);
				}
				else if (attrType != XREF && attrType != CATEGORICAL && attrType != MREF)
				{
					entityMap.put(attrName, entity.get(attr.getName()));
				}
				else if ((attrType == XREF || attrType == CATEGORICAL) && attributeExpandsSet != null
						&& attributeExpandsSet.containsKey(attrName.toLowerCase()))
				{
					Entity refEntity = entity.getEntity(attr.getName());
					if (refEntity != null)
					{
						Set<String> subAttributesSet = attributeExpandsSet.get(attrName.toLowerCase());
						EntityMetaData refEntityMetaData = dataService.getEntityMetaData(attr.getRefEntity().getName());
						Map<String, Object> refEntityMap = getEntityAsMap(refEntity, refEntityMetaData,
								subAttributesSet, null);
						entityMap.put(attrName, refEntityMap);
					}
				}
				else if (attrType == MREF && attributeExpandsSet != null
						&& attributeExpandsSet.containsKey(attrName.toLowerCase()))
				{
					EntityMetaData refEntityMetaData = dataService.getEntityMetaData(attr.getRefEntity().getName());
					Iterable<Entity> mrefEntities = entity.getEntities(attr.getName());

					Set<String> subAttributesSet = attributeExpandsSet.get(attrName.toLowerCase());
					List<Map<String, Object>> refEntityMaps = new ArrayList<Map<String, Object>>();
					for (Entity refEntity : mrefEntities)
					{
						Map<String, Object> refEntityMap = getEntityAsMap(refEntity, refEntityMetaData,
								subAttributesSet, null);
						refEntityMaps.add(refEntityMap);
					}

					EntityPager pager = new EntityPager(0, new EntityCollectionRequest().getNum(),
							(long) refEntityMaps.size(), mrefEntities);

					String uri = String.format(BASE_URI + "/%s/%s/%s", meta.getName(), entity.getIdValue(), attrName);
					EntityCollectionResponse ecr = new EntityCollectionResponse(pager, refEntityMaps, uri);
					entityMap.put(attrName, ecr);
				}
				else if ((attrType == XREF && entity.get(attr.getName()) != null)
						|| (attrType == CATEGORICAL && entity.get(attr.getName()) != null) || attrType == MREF)
				{
					// Add href to ref field
					Map<String, String> ref = new LinkedHashMap<String, String>();
					ref.put("href",
							String.format(BASE_URI + "/%s/%s/%s", meta.getName(), entity.getIdValue(), attrName));
					entityMap.put(attrName, ref);
				}

			}

		}

		return entityMap;
	}

	/**
	 * 
	 * @param attributes
	 * @return set of lower case attribute names
	 */
	private Set<String> toAttributeSet(String[] attributes)
	{
		return attributes != null && attributes.length > 0 ? Sets.newHashSet(Iterables.transform(
				Arrays.asList(attributes), new Function<String, String>()
				{
					@Override
					public String apply(String attribute)
					{
						return attribute.toLowerCase();
					}
				})) : null;
	}

	/**
	 * expand is of form 'attr1', 'entity1[attr1]', 'entity1[attr1;attr2]'
	 * 
	 * @param expands
	 * @return map from lower case expand names to a attribute set
	 */
	private Map<String, Set<String>> toExpandMap(String[] expands)
	{
		if (expands != null)
		{
			Map<String, Set<String>> expandMap = new HashMap<String, Set<String>>();
			for (String expand : expands)
			{
				// validate
				Matcher matcher = PATTERN_EXPANDS.matcher(expand);
				if (!matcher.matches()) throw new MolgenisDataException("invalid expand value: " + expand);

				// for partial expands, create set
				expand = matcher.group(1);
				String attrsStr = matcher.group(2);
				Set<String> attrSet;
				if (attrsStr != null && !attrsStr.isEmpty())
				{
					attrSet = new HashSet<String>();
					for (String attr : attrsStr.split(";"))
					{
						attrSet.add(attr.toLowerCase());
					}
				}
				else attrSet = null;

				expandMap.put(expand.toLowerCase(), attrSet);
			}
			return expandMap;
		}
		return null;
	}
}
