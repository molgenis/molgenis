<#macro footer version=1>
            </div><#-- close plugin-container -->
        </div><#-- close col-md-12 -->
    </div><#-- close row -->
</div><#-- close container-fluid -->

<#if version == 1>
    <div id="footer-container">
        <div class="container">
            <p class="text-muted text-center small footer">
                <#if app_settings.footer??>
                <span>
                    ${app_settings.footer}
                </span>
                <br>
                </#if>
                <em>
                    This database was generated using the open source <a href="http://www.molgenis.org">MOLGENIS
                    database generator</a><#if molgenis_version?has_content>
                    <span>version ${molgenis_version!?html}</span></#if><#if molgenis_build_date?has_content><span> built
                    on ${molgenis_build_date!?html}</span></#if>.<br>Please cite <a
                        href="http://www.ncbi.nlm.nih.gov/pubmed/21210979">Swertz et al (2010)</a> or <a
                        href="http://www.ncbi.nlm.nih.gov/pubmed/17297480">Swertz &amp; Jansen (2007)</a> on use.
                </em>
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
<#else>
    <#-- VUE -->
    <div id="molgenis-footer"></div>

    <script type=text/javascript>
        window.molgenisFooter = {
                <#if app_settings.footer??>additionalMessage: '${app_settings.footer}'</#if>
            <#if app_settings.footer?? && (molgenis_version??||molgenis_build_date??)>,</#if>
                <#if molgenis_version??>version: '${molgenis_version}'</#if>
            <#if molgenis_version?? && molgenis_build_date??>,</#if>
                <#if molgenis_build_date??>buildDate: '${molgenis_build_date}'</#if>}
    </script>

    <#-- Include the Vue version of the molgenis footer  -->
    <script type=text/javascript src="<@resource_href "/js/bootstrap-4/footer/molgenis-footer.js"/>"></script>
</#if>


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