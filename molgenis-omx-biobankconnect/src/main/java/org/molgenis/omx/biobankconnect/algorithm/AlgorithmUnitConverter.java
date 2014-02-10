package org.molgenis.omx.biobankconnect.algorithm;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jscience.physics.amount.Amount;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;

public class AlgorithmUnitConverter
{
	@Autowired
	private SearchService searchService;
	private static final String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	private static final String ONTOLOGYTERM_SYNONYM = "ontologyTermSynonym";
	private static final String UNIT_DOCUMENT_TYPE = "ontologyTerm-http://purl.obolibrary.org/obo/uo.owl";
	private static final Map<String, Unit<?>> UnitMap = new HashMap<String, Unit<?>>();
	private static final Logger logger = Logger.getLogger(AlgorithmUnitConverter.class);

	public AlgorithmUnitConverter() throws IllegalArgumentException, IllegalAccessException
	{
		initializeUnitMap();
	}

	public void match()
	{

	}

	public String convert(OntologyTerm standardUnitOT, OntologyTerm customUnitOT)
	{
		if (standardUnitOT == null || customUnitOT == null) return StringUtils.EMPTY;
		StringBuilder unitConvertScript = new StringBuilder();
		for (String standardUnitName : findCompositeUnitNames(retrieveUnits(standardUnitOT)))
		{
			for (String customeUnitName : findCompositeUnitNames(retrieveUnits(customUnitOT)))
			{
				unitConvertScript.append(convert(standardUnitName, customeUnitName));
				if (unitConvertScript.length() != 0) return unitConvertScript.toString();
			}
		}
		return unitConvertScript.toString();
	}

	private Set<String> findCompositeUnitNames(Set<String> unitNames)
	{
		if (unitNames.size() == 0) Collections.emptySet();
		Set<String> newUnitNames = new HashSet<String>();
		newUnitNames.addAll(unitNames);
		for (String unitName : unitNames)
		{
			if (unitName.contains("/"))
			{
				newUnitNames.addAll(Arrays.asList(unitName.split("/")));
			}
		}
		return newUnitNames;
	}

	private String convert(String standardUnitName, String customUnitName)
	{
		StringBuilder unitConversionScript = new StringBuilder();
		unitConversionScript.append(compare(getUnit(standardUnitName), getUnit(customUnitName)));
		if (unitConversionScript.length() != 0) return unitConversionScript.toString();
		if (unitConversionScript.length() == 0)
		{
			unitConversionScript.append(compareAdvanced(standardUnitName, customUnitName));
		}

		return unitConversionScript.toString();
	}

	private String compareAdvanced(String unitName1, String unitName2)
	{
		StringBuilder algorithmScript = new StringBuilder();
		Matcher matcherUnitSet1 = findExponentUnit(unitName1);
		if (matcherUnitSet1.find())
		{
			unitName1 = matcherUnitSet1.group(1);
			algorithmScript.append(".pow(").append(matcherUnitSet1.group(2)).append(")");
		}
		Matcher matcherUnitSet2 = findExponentUnit(unitName2);
		if (matcherUnitSet2.find())
		{
			unitName2 = matcherUnitSet2.group(1);
			algorithmScript.append(".root(").append(matcherUnitSet2.group(2)).append(")");
		}
		if (unitName1 != null && unitName2 != null && algorithmScript.length() != 0)
		{
			String compare = convert(unitName1, unitName2);
			algorithmScript.delete(0, algorithmScript.length()).insert(0, compare);
		}
		return algorithmScript.toString();
	}

	private static String compare(Unit<?> unit1, Unit<?> unit2)
	{
		StringBuilder conversionScript = new StringBuilder();
		if (unit1 != null && unit2 != null && unit1.isCompatible(unit2) && !unit1.equals(unit2))
		{
			Amount<?> value2 = Amount.valueOf(1, unit2);
			Amount<?> value1 = value2.to(unit1);
			double estimatedValue1 = value1.getEstimatedValue();
			double estimatedValue2 = value2.getEstimatedValue();

			if (estimatedValue1 > estimatedValue2)
			{
				conversionScript.append(".times(").append(value1.divide(value2).getEstimatedValue()).append(")");
			}
			else
			{
				conversionScript.append(".div(").append(value2.divide(value1).getEstimatedValue()).append(")");
			}
		}
		return conversionScript.toString();
	}

	private Set<String> retrieveUnits(OntologyTerm ot)
	{
		Set<String> extractedUnitObjects = new HashSet<String>();
		QueryImpl query = new QueryImpl();
		query.pageSize(10000);
		query.addRule(new QueryRule(ONTOLOGY_TERM_IRI, Operator.EQUALS, ot.getTermAccession()));
		SearchRequest searchRequest = new SearchRequest(UNIT_DOCUMENT_TYPE, query, null);
		SearchResult result = searchService.search(searchRequest);
		for (Hit hit : result.getSearchHits())
		{
			Map<String, Object> columnValueMap = hit.getColumnValueMap();
			extractedUnitObjects
					.add(processUnitName(columnValueMap.get(ONTOLOGYTERM_SYNONYM).toString().toLowerCase()));
		}
		if (!extractedUnitObjects.contains(ot.getName()))
		{
			extractedUnitObjects.add(processUnitName(ot.getName()));
		}
		return extractedUnitObjects;
	}

	private String processUnitName(String unitName)
	{
		Pattern pattern = Pattern.compile("([a-zA-Z]+\\s+\\d+)");
		Matcher matcher = pattern.matcher(unitName);
		if (matcher.find())
		{
			String modifiedPart = matcher.group(1).trim();
			modifiedPart = modifiedPart.replaceAll(" +", "^[");
			modifiedPart += "]";
			unitName = unitName.replaceAll(matcher.group(1), modifiedPart);
		}
		pattern = Pattern.compile("([a-zA-Z]+\\s+[a-zA-Z]+)\\^\\[\\d+\\]");
		matcher = pattern.matcher(unitName);
		if (matcher.find())
		{
			String modifiedPart = matcher.group(1).trim();
			modifiedPart = modifiedPart.replaceAll(" +", "/");
			unitName = unitName.replaceAll(matcher.group(1), modifiedPart);
		}
		return unitName;
	}

	private Matcher findExponentUnit(String unitName)
	{
		Pattern pattern = Pattern.compile("(\\w+)\\^\\[(\\d+)\\]");
		Matcher matcher = pattern.matcher(unitName);
		return matcher;
	}

	private static Unit<?> getUnit(String unitName)
	{
		if (UnitMap.containsKey(superscript(unitName.toLowerCase())))
		{
			return UnitMap.get(superscript(unitName.toLowerCase()));
		}
		return null;
	}

	private static void initializeUnitMap() throws IllegalArgumentException, IllegalAccessException
	{
		for (Field field : SI.class.getFields())
		{
			Unit<?> unit = (Unit<?>) field.get(null);
			UnitMap.put(unit.toString().toLowerCase(), unit);
			UnitMap.put(field.getName().toLowerCase(), unit);

		}
		for (Field field : NonSI.class.getFields())
		{
			Unit<?> unit = (Unit<?>) field.get(null);
			UnitMap.put(unit.toString().toLowerCase(), unit);
			UnitMap.put(field.getName().toLowerCase(), unit);
		}
	}

	private static String superscript(String str)
	{
		str = str.replaceAll("0", "⁰");
		str = str.replaceAll("1", "¹");
		str = str.replaceAll("2", "²");
		str = str.replaceAll("3", "³");
		str = str.replaceAll("4", "⁴");
		str = str.replaceAll("5", "⁵");
		str = str.replaceAll("6", "⁶");
		str = str.replaceAll("7", "⁷");
		str = str.replaceAll("8", "⁸");
		str = str.replaceAll("9", "⁹");
		return str;
	}
}
