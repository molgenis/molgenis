<#macro footer>
                    </div><#-- close plugin-container -->
                </div><#-- close col-md-12 -->
            </div><#-- close row -->
        </div><#-- close container-fluid -->
        <div class="footer">
            <div class="container-fluid">
            	<p class="text-muted text-center small">
	            	<#if footerText?? && footerText != "null">
	            		${footerText}
	                </#if>
	            	<em>This database was generated using the open source <a href="http://www.molgenis.org">MOLGENIS database generator</a><#if molgenis_version?has_content> version ${molgenis_version!?html}</#if><#if molgenis_build_date?has_content> build on ${molgenis_build_date!?html}</#if>.<br>Please cite <a href="http://www.ncbi.nlm.nih.gov/pubmed/21210979">Swertz et al (2010)</a> or <a href="http://www.ncbi.nlm.nih.gov/pubmed/17297480">Swertz &amp; Jansen (2007)</a> on use.</em>
	            </p>
            </div>
        </div>
	</body>
	<#if app_tracking_code.footer?has_content>
		<script id="app-tracking-code-footer" type="text/javascript">
			${app_tracking_code.footer?string}
		</script>
	</#if>
</html>
</#macro>