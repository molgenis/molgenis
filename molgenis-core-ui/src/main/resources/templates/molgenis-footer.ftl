<#macro footer>
					</div>
				</div>
			</div><#-- close plugin container-fluid -->
			<div id="push"></div>
		</div><#-- close wrap -->
		<div id="footer"> 
			<p>This database was generated using the open source <a href="http://www.molgenis.org">MOLGENIS database generator</a><#if molgenis_version?has_content> version ${molgenis_version}</#if><#if molgenis_build_date?has_content> build on ${molgenis_build_date}</#if>.<br>
			Please cite <a href="http://www.ncbi.nlm.nih.gov/pubmed/21210979">Swertz et al (2010)</a> or <a href="http://www.ncbi.nlm.nih.gov/pubmed/17297480">Swertz &amp; Jansen (2007)</a> on use.</p> 
		</div>
	</body>
</html>
</#macro>