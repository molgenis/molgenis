<#macro footer>
</div><#-- close plugin-container -->
</div><#-- close col-md-12 -->
</div><#-- close row -->
</div><#-- close container-fluid -->
<div class="container-fluid">
    <div class="row footer">
        <div class="col-md-12">
            <p class="text-muted text-center small">
                <#if app_settings.footer??>${app_settings.footer}</#if>
                <em>This database was generated using the open source <a href="http://www.molgenis.org">MOLGENIS
                    database generator</a><#if molgenis_version?has_content>
                    version ${molgenis_version!?html}</#if><#if molgenis_build_date?has_content> built
                    on ${molgenis_build_date!?html}</#if>.<br>Please cite <a
                        href="http://www.ncbi.nlm.nih.gov/pubmed/21210979">Swertz et al (2010)</a> or <a
                        href="http://www.ncbi.nlm.nih.gov/pubmed/17297480">Swertz &amp; Jansen (2007)</a> on use.</em>
            </p>
            <#if app_settings.googleAnalyticsTrackingId?? || app_settings.googleAnalyticsTrackingIdMolgenis??>
                <p class="text-muted text-center small ga-opt-out">
                    <em>We use Google Analytics to review this site's usage and improve our services.<br/>
                        To optimally protect your privacy we have signed the Data Processing Amendment, masked parts of
                        your IP address and disabled data sharing with other Google services.</em><br/>
                    <em>Click <a href="javascript:gaOptout()">here</a> to opt-out of Google Analytics.</em>
                </p>
                <p class="text-muted text-center small ga-opted-out hidden">
                    <em>You have opted out of Google Analytics.</em>
                </p>
            </#if>
        </div>
    </div>
</div>
    <#if authenticated?? && authenticated>
    <#else>
        <#include "/login-modal.ftl">
    </#if>
</body>
    <#if app_settings.trackingCodeFooter?has_content>
    <script id="app-tracking-code-footer" type="text/javascript">
			${app_settings.trackingCodeFooter?string}
		</script>
    </#if>
</html>
</#macro>