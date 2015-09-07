package org.molgenis.data.annotation.cmd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VcfValidator
{
	@Value("perl-executable:/usr/bin/perl")
	private String perlLocation;
	@Value("vcf-tools-dir")
	private String vcfToolsDirectory;

	/**
	 * Validation method that calls the perl executable of the vcf-validator. Logs vcf validation output into a log file
	 * 
	 * @param vcfFile
	 * @return Message
	 */
	public String validateVCF(File vcfFile)
	{
		try
		{
			String vcfValidator = vcfToolsDirectory + "perl/vcf-validator";
			// Checks if vcf-tools is present
			if (new File(vcfValidator).exists())
			{
				// Set working directory, Vcf.pm should be built here
				String workingDirectory = vcfToolsDirectory + "perl/";

				ProcessBuilder processBuilder = new ProcessBuilder(perlLocation, vcfValidator,
						vcfFile.getAbsolutePath(), "-u", "-d").directory(new File(workingDirectory));

				Process proc = processBuilder.start();

				InputStream inputStream = proc.getInputStream();
				Scanner scanner = new Scanner(inputStream);

				String line = "";
				Integer errorCount = null;
				Pattern p = Pattern.compile("(\\d+)\\s*errors\\s*total");

				File logFile = new File("vcf-validation.log");
				if (!logFile.exists())
				{
					logFile.createNewFile();
				}

				FileWriter fileWriter = new FileWriter(logFile.getAbsoluteFile(), true);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

				Date date = new Date();

				bufferedWriter.write("### Validation report for " + vcfFile.getName() + "\n");
				bufferedWriter.write("### Validation date: " + date + "\n");
				while (proc.isAlive() || scanner.hasNext())
				{
					while (scanner.hasNext())
					{
						line = scanner.nextLine();
						bufferedWriter.write(line + "\n");
						Matcher m = p.matcher(line);
						if (m.find())
						{
							errorCount = Integer.parseInt(m.group(1));
						}
					}
				}

				bufferedWriter.write("\n##################################################\n");
				bufferedWriter.close();

				scanner.close();

				if (errorCount == 0)
				{
					return "VCF file [" + vcfFile.getName() + "] passed validation.";
				}
				else
				{
					return "VCF file [" + vcfFile.getName()
							+ "] did not pass validation, see the log for more details.";
				}
			}
			else
			{
				return "No vcf-validator present, skipping validation.";
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException("Something went wrong: " + e);
		}
	}
}
