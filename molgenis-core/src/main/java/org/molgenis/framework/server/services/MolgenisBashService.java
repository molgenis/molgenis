package org.molgenis.framework.server.services;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisResponse;
import org.molgenis.framework.server.MolgenisService;

public class MolgenisBashService implements MolgenisService
{
	public MolgenisBashService(MolgenisContext mc)
	{
	}

	/**
	 * Delegate to handle request for the R api.
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@Override
	public void handleRequest(MolgenisRequest request, MolgenisResponse response) throws IOException
	{

		// Utils.console("starting RApiServlet");
		OutputStream outs = response.getResponse().getOutputStream();
		PrintStream out = new PrintStream(new BufferedOutputStream(outs), false, "UTF8"); // 1.4

		String s = "";

		s += "server="
				+ (request.getAppLocation().endsWith("/") ? request.getAppLocation() : request.getAppLocation() + "/")
				+ "\n";
		s += "\n";
		s += "messagebox=$( printf \"%90s\" );\n";
		s += "\n";
		s += "function printError {\n";
		s += "  echo ${messagebox// /#}\n";
		s += "  echo \"ERROR:\" $1\n";
		s += "  echo ${messagebox// /#}\n";
		s += "}\n";
		s += "\n";
		s += "function printWarning {\n";
		s += "  echo ${messagebox// /#}\n";
		s += "  echo \"WARNING:\" $1\n";
		s += "  echo ${messagebox// /#}\n";
		s += "}\n";
		s += "\n";
		s += "function MOLGENIS.login {\n";
		s += "  if [ -z \"$1\" ]\n";
		s += "    then\n";
		s += "    printError \"Missing parameter: username\"\n";
		s += "    return\n";
		s += "  fi\n";
		s += "  if [ -z \"$2\" ]\n";
		s += "    then\n";
		s += "    printError \"Missing parameter: password\"\n";
		s += "    return\n";
		s += "  fi\n";
		s += "  curl --cookie-jar cookies.txt --user-agent Mozilla/4.0 --data \"__target=UserLogin&__action=Login&username=$1&password=$2\" $server\"molgenis.do\" -v -k -D header\n";
		s += "}\n";
		s += "\n";
		s += "function MOLGENIS.logout {\n";
		s += "  curl --cookie-jar cookies.txt --user-agent Mozilla/4.0 --data \"__target=UserLogin&__action=Logout\" $server\"molgenis.do\" -v -k -D header\n";
		s += "}\n";
		s += "\n";
		s += "function getFile {\n";
		s += "  if [ -z \"$1\" ]\n";
		s += "    then\n";
		s += "    printError \"Missing parameter: database file name\"\n";
		s += "    return\n";
		s += "  fi\n";
		s += "  if [ -z \"$2\" ]\n";
		s += "    then\n";
		s += "    printError \"Missing parameter: local 'save as' file name\"\n";
		s += "    return\n";
		s += "  fi\n";
		s += "  curl -b cookies.txt --user-agent Mozilla/4.0 $server\"downloadfile?name=$1\" -v > $2\n";
		s += "}\n";
		s += "\n";
		s += "function putFile {\n";
		s += "  if [ -z \"$1\" ]\n";
		s += "    then\n";
		s += "    printError \"Missing parameter: local file name\"\n";
		s += "    return\n";
		s += "  fi\n";
		s += "  if [ -z \"$2\" ]\n";
		s += "    then\n";
		s += "   	printWarning \"Missing parameter: database 'save as' file name\"\n";
		s += "    return\n";
		s += "  fi\n";
		s += "   if [ -z \"$3\" ]\n";
		s += "    then\n";
		s += "    printWarning \"No third parameter: you may need to provide more information to store this file, such as investigation or a type. E.g. add \\\"-F \\\"investigation_id=1\\\"\\\".\"\n";
		s += "  fi\n";
		s += "  curl -v -b cookies.txt --user-agent Mozilla/4.0 -F \"file=@$1\" -F \"name=$2\" $3 $server\"uploadfile\"\n";
		s += "}\n";
		s += "\n";
		s += "\n";
		s += "## EXAMPLE SCRIPT ##\n";
		s += "\n";
		s += "## essential: connect to the Bash API\n";
		s += "#curl --user-agent Mozilla/4.0 http://localhost:8080/xqtl_lifelines/api/bash -v -k -D header > bashapi.txt\n";
		s += "#source bashapi.txt\n";
		s += "\n";
		s += "## login as 'admin', password 'admin'\n";
		s += "#MOLGENIS.login admin admin\n";
		s += "\n";
		s += "## select some phenotype to analyse\n";
		s += "#pheno=Health19\n";
		s += "\n";
		s += "## download some genotypes\n";
		s += "#getFile llrp_fake_geno llrp_geno.ped\n";
		s += "#getFile llrp_fake_snp llrp_geno.map\n";
		s += "\n";
		s += "## download some phenotypes (custom, but uses server location)\n";
		s += "#curl -b cookies.txt --user-agent Mozilla/4.0 -v -F \'name=$pheno\' -F \'plinkformat=true\' $server\'downloadpheno\' > llrp_pheno_$pheno.txt\n";
		s += "\n";
		s += "## run some analysis\n";
		s += "#../geno/plink-1.07-mac-intel/plink --noweb --file llrp_geno --pheno llrp_pheno_$pheno.txt --assoc --maf 0.05 --hwe -0.001 --1 --allow-no-sex --out results_for_$pheno\n";
		s += "\n";
		s += "## upload some results\n";
		s += "#putFile results_for_$pheno.assoc results_for_$pheno.assoc \'-F \'investigation_name=LifeLines\' -F \'investigation_id=1\' -F \'type=InvestigationFile\' -F \'description=Plink_assoc_results\'\'\n";
		s += "\n";
		s += "## logout\n";
		s += "#MOLGENIS.logout\n";
		s += "\n";

		writeResponse(response, s, out);
		// Utils.console("closed & flushed");

	}

	private void writeResponse(MolgenisResponse response, String responseLine, PrintStream out) throws IOException
	{
		response.getResponse().setStatus(HttpServletResponse.SC_OK);
		response.getResponse().setContentLength(responseLine.length());
		response.getResponse().setCharacterEncoding("UTF8");
		response.getResponse().setContentType("text/plain");
		out.print(responseLine);
		out.flush();
		out.close();
		response.getResponse().flushBuffer();
	}

}
