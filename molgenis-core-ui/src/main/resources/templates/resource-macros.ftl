<#-- Generates resource href by appending resource name with automatically generated version number -->
<#-- This fingerprint avoids the use of stale browser data -->
<#macro resource_href resource_name>${resource_name}?${resource_fingerprint_registry.getFingerprint(resource_name)}</#macro>