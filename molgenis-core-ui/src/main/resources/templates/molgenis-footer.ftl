<#macro footer>
                    </div><#-- close plugin-container -->
                </div><#-- close col-md-12 -->
            </div><#-- close row -->
        </div><#-- close container-fluid -->
        <div class="container-fluid">
        	<div class="row footer">
        		<div class="col-md-12">
            
            		<p class="text-muted text-center small">
	            		<#if footerText?? && footerText != "null">
	            			${footerText}
	             	   </#if>
	            		<em>This database was generated using the open source <a href="http://www.molgenis.org">MOLGENIS database generator</a><#if molgenis_version?has_content> version ${molgenis_version!?html}</#if><#if molgenis_build_date?has_content> build on ${molgenis_build_date!?html}</#if>.<br>Please cite <a href="http://www.ncbi.nlm.nih.gov/pubmed/21210979">Swertz et al (2010)</a> or <a href="http://www.ncbi.nlm.nih.gov/pubmed/17297480">Swertz &amp; Jansen (2007)</a> on use.</em>
					</p>
	           		<#if app_tracking_code.default?has_content>
						<p class="text-muted text-center small">
							<em>We use Google Analytics to review this site's usage and improve our services. To optimally protect your privacy we have signed the Data Processing Amendment, masked parts of your IP address and disabled data sharing with other Google services.</em><br/>			
							<em>Click <a href="javascript:gaOptout()">here</a> to opt-out of Google Analytics.</em>
		            	</p>
					</#if>
            	</div>
            </div>
        </div>
	</body>

	<#if app_tracking_code.default?has_content>
		<#-- Opt-out script -->
		<script>
			// Set to the same value as the web property used on the site
			var gaProperty = '${app_tracking_code.default?string}';
			
			// Disable tracking if the opt-out cookie exists.
			var disableStr = 'ga-disable-' + gaProperty;
			if (document.cookie.indexOf(disableStr + '=true') > -1) {
				window[disableStr] = true;
			}
	
			// Opt-out function
			function gaOptout() {
				document.cookie = disableStr + '=true; expires=Thu, 31 Dec 2099 23:59:59 UTC; path=/';
			 	window[disableStr] = true;
			}
		</script>
	
		<#-- Google Analytics coupling script -->
		<script>
			(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
			(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
			m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
			})(window,document,'script','//www.google-analytics.com/analytics.js','ga');
		 
			ga('create', '${app_tracking_code.default?string}', 'auto'); 
			ga('set', 'forceSSL', true); 
			ga('set', 'anonymizeIp', true); 
			ga('send', 'pageview');
		</script>
	</#if>


	<#if app_tracking_code.footer?has_content>
		<script id="app-tracking-code-footer" type="text/javascript">
			${app_tracking_code.footer?string}
		</script>
	</#if>
</html>
</#macro>