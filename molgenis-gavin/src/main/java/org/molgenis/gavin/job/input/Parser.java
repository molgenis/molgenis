package org.molgenis.gavin.job.input;

import com.google.common.collect.EnumMultiset;
import com.google.common.collect.Multiset;
import org.molgenis.gavin.job.input.model.CaddVariant;
import org.molgenis.gavin.job.input.model.LineType;
import org.molgenis.gavin.job.input.model.Variant;
import org.molgenis.gavin.job.input.model.VcfVariant;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.molgenis.gavin.job.input.Files.getLines;
import static org.molgenis.gavin.job.input.model.LineType.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Parses input lines.
 * Two formats are supported, the output from the online CADD webtool and a VCF of which only the first five columns are read.
 */
@Component
public class Parser
{
	private static final Logger LOG = getLogger(Parser.class);

	private static final int CHROM_INDEX = 0;
	private static final int POS_INDEX = 1;
	private static final int CADD_REF_INDEX = 2;
	private static final int CADD_ALT_INDEX = 3;
	private static final int CADD_RAW_SCORE_INDEX = 4;
	private static final int CADD_PHRED_SCORE = 5;
	private static final int VCF_ID_INDEX = 2;
	private static final int VCF_REF_INDEX = 3;
	private static final int VCF_ALT_INDEX = 4;
	private static final int CADD_NR_OF_COLS = 6;
	private static final int VCF_NR_OF_COLUMNS = 5;
	public static final int MAX_LINES = 100000;

	private static Pattern CHROM_PATTERN = Pattern.compile(
			"([Cc][Hh][Rr])?(?<chrom>([1-9])|(1[0-9])|(2[0-2])|[xX]|[yY])");
	private static Pattern REF_PATTERN = Pattern.compile("[ACTG]+");
	private static Pattern ALT_PATTERN = Pattern.compile("[ACTG]+|\\.");

	/**
	 * Transforms gavin input file.
	 *
	 * @param inputFile the file to transform
	 * @param output    the file to output parsed variants to
	 * @param error     the file to output error lines to
	 * @return Multiset counting the {@link LineType}s of the input file's lines
	 * @throws IOException if the file interaction fails
	 */
	public Multiset<LineType> tryTransform(File inputFile, File output, File error) throws IOException
	{
		LOG.debug("Parsing {}...", inputFile.getAbsolutePath());
		try (Stream<String> lines = getLines(inputFile.toPath(), UTF_8);
				LineSink outputSink = new LineSink(output);
				LineSink errorSink = new LineSink(error))
		{
			Multiset<LineType> lineTypes = transformLines(lines, outputSink, errorSink);
			LOG.info("Parsed {}. LineTypes: {}", inputFile.getAbsolutePath(), lineTypes);
			return lineTypes;
		}
	}

	/**
	 * Transforms a stream of lines and sends them to the error sink
	 *
	 * @param lines      the Stream of lines to transform
	 * @param outputSink {@link LineSink} to write transformed lines to
	 * @param errorSink  {@link LineSink} to write unparseable lines to
	 * @return Multiset counting the {@link LineType}s found in the stream
	 */
	Multiset<LineType> transformLines(Stream<String> lines, LineSink outputSink, LineSink errorSink)
	{
		Multiset<LineType> lineTypes = EnumMultiset.create(LineType.class);
		writeVcfHeader(outputSink);
		lines.map(line -> transformLine(line, lineTypes.size(), countValidLines(lineTypes), outputSink, errorSink))
			 .forEach(lineTypes::add);
		return lineTypes;
	}

	private int countValidLines(Multiset<LineType> lineTypes)
	{
		return lineTypes.count(VCF) + lineTypes.count(CADD);
	}

	private void writeVcfHeader(LineSink outputSink)
	{
		outputSink.accept("##fileformat=VCFv4.0");
		outputSink.accept("##INFO=<ID=CADD,Number=.,Type=String,Description=\"Raw CADD score\">");
		outputSink.accept("##INFO=<ID=CADD_SCALED,Number=.,Type=String,Description=\"Scaled CADD score\">");
		outputSink.accept("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO");
	}

	/**
	 * Transforms a single line.
	 *
	 * @param line          the line to parse
	 * @param numValidLines the number of valid lines already parsed
	 * @param outputSink    {@link LineSink} to write parsed variants to
	 * @param errorSink     {@link LineSink} to write lines to that we cannot parse
	 * @return LineType of the parsed line
	 */
	public LineType transformLine(String line, int numLines, int numValidLines, LineSink outputSink, LineSink errorSink)
	{
		if (numValidLines >= MAX_LINES)
		{
			return SKIPPED;
		}
		if (isComment(line))
		{
			return COMMENT;
		}
		Variant variant = tryParseVariant(line);
		if (variant == null)
		{
			errorSink.accept(format("Line %d:\t%s", numLines + 1, line));
			return ERROR;
		}
		if (variant.getLineType() == INDEL_NOCADD)
		{
			// Don't process indels without cadd annotation
			errorSink.accept(format("Line %d:\t%s", numLines + 1, line));
		}
		else
		{
			outputSink.accept(variant.toString());
		}
		return variant.getLineType();
	}

	/**
	 * Determines if a line is a comment line. Comment lines start with "#".
	 *
	 * @param line the line that may be a comment line
	 * @return true if the line is a comment line
	 */
	public boolean isComment(String line)
	{
		return line != null && line.startsWith("#");
	}

	/**
	 * Parses a line into a {@link Variant}. It may be one of two formats, CADD output or five VCF columns.
	 *
	 * @param line the line to parse
	 * @return parsed Variant, or null if the line could not be parsed
	 */
	public Variant tryParseVariant(String line)
	{
		try
		{
			return parseVariant(line);
		}
		catch (Exception ex)
		{
			LOG.debug("Error parsing line {}", line, ex);
			return null;
		}
	}

	private Variant parseVariant(String line)
	{
		String[] columns = line.split("\t");
		Variant caddVariant = parseCaddLine(columns);
		return caddVariant != null ? caddVariant : parseVcfLine(columns);
	}

	/**
	 * Determines if any of the values are null
	 *
	 * @param values the values that may be null
	 * @return true if any of the values was null
	 */
	private boolean anyNull(Object... values)
	{
		return Arrays.stream(values).anyMatch(Objects::isNull);
	}

	/**
	 * Attempts to parse a line as a CADD output record.
	 *
	 * @param columns the columns of the line
	 * @return parsed {@link CaddVariant}, or null if parsing failed
	 */
	private CaddVariant parseCaddLine(String[] columns)
	{
		if (columns.length != CADD_NR_OF_COLS)
		{
			return null;
		}
		String chrom = parseChrom(columns[CHROM_INDEX].trim());
		Long pos = parsePos(columns[POS_INDEX].trim());

		if (anyNull(chrom, pos))
		{
			return null;
		}
		try
		{
			String ref = parseRef(columns[CADD_REF_INDEX].trim());
			String alt = parseAlt(columns[CADD_ALT_INDEX].trim());
			Double rawScore = parseDouble(columns[CADD_RAW_SCORE_INDEX].trim());
			Double phred = parseDouble(columns[CADD_PHRED_SCORE].trim());
			if (anyNull(ref, alt))
			{
				return null;
			}
			return CaddVariant.create(chrom, pos, ref, alt, rawScore, phred);
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}

	/**
	 * Attempts to parse a line as a VCF record.
	 *
	 * @param columns the columns of the line
	 * @return parsed {@link VcfVariant}
	 */
	private VcfVariant parseVcfLine(String[] columns)
	{
		if (columns.length < VCF_NR_OF_COLUMNS)
		{
			return null;
		}
		String chrom = parseChrom(columns[CHROM_INDEX].trim());
		Long pos = parsePos(columns[POS_INDEX].trim());
		String id = columns[VCF_ID_INDEX].trim();
		if (isEmpty(id))
		{
			id = ".";
		}
		String ref = parseRef(columns[VCF_REF_INDEX].trim());
		String alt = parseAlt(columns[VCF_ALT_INDEX].trim());

		if (anyNull(chrom, pos, ref, alt))
		{
			return null;
		}
		return VcfVariant.create(chrom, pos, id, ref, alt);
	}

	private Double parseDouble(String doubleString)
	{
		return isEmpty(doubleString) ? null : Double.parseDouble(doubleString);
	}

	String parseChrom(String chrom)
	{
		Matcher m = CHROM_PATTERN.matcher(chrom);
		return !m.matches() ? null : m.group("chrom").toUpperCase();
	}

	private Long parsePos(String pos)
	{
		try
		{
			return Long.parseLong(pos);
		}
		catch (NumberFormatException ex)
		{
			return null;
		}
	}

	private String parseRef(String value)
	{
		if (!REF_PATTERN.matcher(value).matches())
		{
			return null;
		}
		return value;
	}

	private String parseAlt(String value)
	{
		if (!ALT_PATTERN.matcher(value).matches())
		{
			return null;
		}
		return value;
	}
}
