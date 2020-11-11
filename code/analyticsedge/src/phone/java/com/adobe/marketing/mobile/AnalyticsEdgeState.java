/*
 Copyright 2020 Adobe. All rights reserved.
 This file is licensed to you under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software distributed under
 the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 OF ANY KIND, either express or implied. See the License for the specific language
 governing permissions and limitations under the License.
 */

// TODO: placeholder, needs to be cleaned
package com.adobe.marketing.mobile;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link AnalyticsEdgeState} class will encapsulate the analytics config properties used across the analytics handlers,
 * which are retrieved from Configuration SharedState.
 */
final class AnalyticsEdgeState {
    private static final String LOG_TAG = AnalyticsEdgeState.class.getSimpleName();
    // ----------- Configuration properties -----------
    private boolean analyticsForwardingEnabled = AnalyticsEdgeConstants.Default.DEFAULT_FORWARDING_ENABLED;
    private boolean offlineEnabled = AnalyticsEdgeConstants.Default.DEFAULT_OFFLINE_ENABLED;
    private int batchLimit = AnalyticsEdgeConstants.Default.DEFAULT_BATCH_LIMIT;
    private MobilePrivacyStatus privacyStatus = AnalyticsEdgeConstants.Default.DEFAULT_PRIVACY_STATUS;
    private int referrerTimeout = AnalyticsEdgeConstants.Default.DEFAULT_REFERRER_TIMEOUT;
    private boolean assuranceSessionActive = AnalyticsEdgeConstants.Default.DEFAULT_ASSURANCE_SESSION_ENABLED;

    private boolean backdateSessionInfoEnabled =
            AnalyticsEdgeConstants.Default.DEFAULT_BACKDATE_SESSION_INFO_ENABLED;
    private String marketingCloudOrganizationID;
    private String rsids;
    private String server;

    // ----------- Identity properties -----------
    private String marketingCloudID;
    private String locationHint;
    private String advertisingIdentifier;
    private String blob;
    private String serializedVisitorIDsList;

    // ----------- Lifecycle properties ----------
    private String applicationID;
    private Map<String, String> defaultData = new HashMap<String, String>();
    private int sessionTimeout = AnalyticsEdgeConstants.Default.DEFAULT_LIFECYCLE_SESSION_TIMEOUT;
    private long lifecycleMaxSessionLength = 0;
    private long lifecycleSessionStartTimestamp = 0;

    AnalyticsEdgeState() { }

    /**
     * Extracts the configuration data from the provided EventData object
     *
     * @param configuration the eventData map from Config's shared state
     */
    void extractConfigurationInfo(final EventData configuration) {
        if (configuration == null) {
            Log.trace(LOG_TAG, "extractConfigurationInfo - Failed to extract configuration data as event data was null.");
            return;
        }

        server = configuration.optString(AnalyticsEdgeConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_SERVER, null);
        rsids = configuration.optString(AnalyticsEdgeConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_REPORT_SUITES, null);
        analyticsForwardingEnabled = configuration.optBoolean(
                AnalyticsEdgeConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_AAMFORWARDING,
                AnalyticsEdgeConstants.Default.DEFAULT_FORWARDING_ENABLED);
        offlineEnabled = configuration.optBoolean(
                AnalyticsEdgeConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_OFFLINE_TRACKING,
                AnalyticsEdgeConstants.Default.DEFAULT_OFFLINE_ENABLED);
        batchLimit = configuration.optInteger(AnalyticsEdgeConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_BATCH_LIMIT,
                AnalyticsEdgeConstants.Default.DEFAULT_BATCH_LIMIT);
        int referrerTimeoutFromConfig = configuration.optInteger(
                AnalyticsEdgeConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_LAUNCH_HIT_DELAY,
                AnalyticsEdgeConstants.Default.DEFAULT_REFERRER_TIMEOUT);

        if (referrerTimeoutFromConfig >= 0) {
            referrerTimeout = referrerTimeoutFromConfig;
        }

        marketingCloudOrganizationID = configuration.optString(
                AnalyticsEdgeConstants.EventDataKeys.Configuration.CONFIG_EXPERIENCE_CLOUD_ORGID_KEY,
                null);
        backdateSessionInfoEnabled = configuration.optBoolean(
                AnalyticsEdgeConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_BACKDATE_PREVIOUS_SESSION,
                AnalyticsEdgeConstants.Default.DEFAULT_BACKDATE_SESSION_INFO_ENABLED);
        privacyStatus = MobilePrivacyStatus.fromString(configuration.optString(
                AnalyticsEdgeConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
                AnalyticsEdgeConstants.Default.DEFAULT_PRIVACY_STATUS.getValue()));

        sessionTimeout = configuration.optInteger(AnalyticsEdgeConstants.EventDataKeys.Configuration.LIFECYCLE_SESSION_TIMEOUT,
                AnalyticsEdgeConstants.Default.DEFAULT_LIFECYCLE_SESSION_TIMEOUT);
    }

    /**
     * Extracts the identity data from the provided EventData object
     *
     * @param identityInfo the eventData map from Identity's shared state
     */
    void extractIdentityInfo(final EventData identityInfo) {
        if (identityInfo == null) {
            Log.trace(LOG_TAG, "extractIdentityInfo - Failed to extract identity data as event data was null.");
            return;
        }

        marketingCloudID = identityInfo.optString(AnalyticsEdgeConstants.EventDataKeys.Identity.VISITOR_ID_MID, null);
        blob = identityInfo.optString(AnalyticsEdgeConstants.EventDataKeys.Identity.VISITOR_ID_BLOB, null);
        locationHint = identityInfo.optString(AnalyticsEdgeConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT, null);
        advertisingIdentifier = identityInfo.optString(AnalyticsEdgeConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER, null);

        //TODO: update
//        if (identityInfo.containsKey(AnalyticsEdgeConstants.EventDataKeys.Identity.VISITOR_IDS_LIST)) {
//            try {
//                final List<VisitorID> visitorIdsList = identityInfo.getTypedList(
//                        AnalyticsEdgeConstants.EventDataKeys.Identity.VISITOR_IDS_LIST,
//                        VisitorID.VARIANT_SERIALIZER);
//                serializedVisitorIDsList = AnalyticsRequestSerializer.generateAnalyticsCustomerIdString(visitorIdsList);
//            } catch (VariantException ex) {
//                Log.debug(LOG_TAG, "extractIdentityInfo - The format of the serializedVisitorIDsList list is invalid: %s", ex);
//                return;
//            }
//        }
    }

    /**
     * Extracts the places data from the provided EventData object
     *
     * @param placesInfo the eventData map from Places shared state
     */
    void extractPlacesInfo(final EventData placesInfo) {
        if (placesInfo == null) {
            Log.trace(LOG_TAG, "extractPlacesInfo - Failed to extract places data (event data was null).");
            return;
        }

        Map<String, String> placesContextData = placesInfo.optStringMap(
                AnalyticsEdgeConstants.EventDataKeys.Places.CURRENT_POI, null);

        if (placesContextData == null) {
            return;
        }

        final String regionId = placesContextData.get(AnalyticsEdgeConstants.EventDataKeys.Places.REGION_ID);

        if (!StringUtils.isNullOrEmpty(regionId)) {
            defaultData.put(AnalyticsEdgeConstants.ContextDataKeys.REGION_ID, regionId);
        }

        final String regionName = placesContextData.get(AnalyticsEdgeConstants.EventDataKeys.Places.REGION_NAME);

        if (!StringUtils.isNullOrEmpty(regionName)) {
            defaultData.put(AnalyticsEdgeConstants.ContextDataKeys.REGION_NAME, regionName);
        }

    }

    /**
     * Extracts the lifecycle data from the provided EventData object
     *
     * @param lifecycleData the eventData map from Lifecycle's shared state
     */
    void extractLifecycleInfo(final EventData lifecycleData) {

        if (lifecycleData == null) {
            Log.trace(LOG_TAG, "extractLifecycleInfo - Failed to extract lifecycle data (event data was null).");
            return;
        }

        lifecycleSessionStartTimestamp = lifecycleData.optLong(
                AnalyticsEdgeConstants.EventDataKeys.Lifecycle.SESSION_START_TIMESTAMP, 0L);
        lifecycleMaxSessionLength = lifecycleData.optLong(AnalyticsEdgeConstants.EventDataKeys.Lifecycle.MAX_SESSION_LENGTH, 0L);

        final Map<String, String> lifecycleContextData = lifecycleData.optStringMap(
                AnalyticsEdgeConstants.EventDataKeys.Lifecycle.LIFECYCLE_CONTEXT_DATA, null);

        if (lifecycleContextData == null || lifecycleContextData.isEmpty()) {
            return;
        }

        final String osVersion = lifecycleContextData.get(AnalyticsEdgeConstants.EventDataKeys.Lifecycle.OPERATING_SYSTEM);

        if (!StringUtils.isNullOrEmpty(osVersion)) {
            defaultData.put(AnalyticsEdgeConstants.ContextDataKeys.OPERATING_SYSTEM, osVersion);
        }

        final String deviceName = lifecycleContextData.get(AnalyticsEdgeConstants.EventDataKeys.Lifecycle.DEVICE_NAME);

        if (!StringUtils.isNullOrEmpty(deviceName)) {
            defaultData.put(AnalyticsEdgeConstants.ContextDataKeys.DEVICE_NAME, deviceName);
        }

        final String deviceResolution = lifecycleContextData.get(AnalyticsEdgeConstants.EventDataKeys.Lifecycle.DEVICE_RESOLUTION);

        if (!StringUtils.isNullOrEmpty(deviceResolution)) {
            defaultData.put(AnalyticsEdgeConstants.ContextDataKeys.DEVICE_RESOLUTION, deviceResolution);
        }

        final String carrier = lifecycleContextData.get(AnalyticsEdgeConstants.EventDataKeys.Lifecycle.CARRIER_NAME);

        if (!StringUtils.isNullOrEmpty(carrier)) {
            defaultData.put(AnalyticsEdgeConstants.ContextDataKeys.CARRIER_NAME, carrier);
        }

        final String runMode = lifecycleContextData.get(AnalyticsEdgeConstants.EventDataKeys.Lifecycle.RUN_MODE);

        if (!StringUtils.isNullOrEmpty(runMode)) {
            defaultData.put(AnalyticsEdgeConstants.ContextDataKeys.RUN_MODE, runMode);
        }

        final String appId = lifecycleContextData.get(AnalyticsEdgeConstants.EventDataKeys.Lifecycle.APP_ID);

        if (!StringUtils.isNullOrEmpty(appId)) {
            defaultData.put(AnalyticsEdgeConstants.ContextDataKeys.APPLICATION_IDENTIFIER, appId);
            this.applicationID = appId;
        }
    }

    /**
     * Extracts the Assurance data from the provided EventData object
     *
     * @param assuranceInfo the eventData map from Assurance's shared state
     */
    void extractAssuranceInfo(final EventData assuranceInfo) {
        if (assuranceInfo == null) {
            Log.trace(LOG_TAG, "extractAssuranceInfo - Failed to extract assurance data (event data was null).");
            return;
        }

        String assuranceSessionId = assuranceInfo.optString(
                AnalyticsEdgeConstants.EventDataKeys.Assurance.SESSION_ID,
                null);

        // assurance sessionId non empty non null means session is active
        assuranceSessionActive = !StringUtils.isNullOrEmpty(assuranceSessionId);
    }

    /**
     * Extracts the visitor id blob, locationHint and marketing could id (mid) in a map mid is not null
     *
     * @return the resulted map or an empty map if mid is null
     */
    final Map<String, String> getAnalyticsIdVisitorParameters() {
        final Map<String, String> analyticsIdVisitorParameters = new HashMap<String, String>();

        if (StringUtils.isNullOrEmpty(this.marketingCloudID)) {
            return analyticsIdVisitorParameters;
        }

        analyticsIdVisitorParameters.put(AnalyticsEdgeConstants.ANALYTICS_PARAMETER_KEY_MID, this.marketingCloudID);

        if (!StringUtils.isNullOrEmpty(this.blob)) {
            analyticsIdVisitorParameters.put(AnalyticsEdgeConstants.ANALYTICS_PARAMETER_KEY_BLOB, this.blob);
        }

        if (!StringUtils.isNullOrEmpty(this.locationHint)) {
            analyticsIdVisitorParameters.put(AnalyticsEdgeConstants.ANALYTICS_PARAMETER_KEY_LOCATION_HINT, this.locationHint);
        }

        return analyticsIdVisitorParameters;
    }

    String getSerializedVisitorIDsList() {
        return serializedVisitorIDsList;
    }

    Map<String, String> getDefaultData() {
        return defaultData;
    }

    String getMarketingCloudId() {
        return this.marketingCloudID;
    }

    String getAdvertisingIdentifier() {
        return advertisingIdentifier;
    }

    String getApplicationID() {
        return applicationID;
    }

    void setApplicationID(final String applicationID) {
        this.applicationID = applicationID;
    }

    int getReferrerTimeout() {
        return referrerTimeout;
    }

    /**
     * Checks if rsids and tracking server are configured for the analytics module
     *
     * @return true if both conditions are met, false otherwise
     */
    boolean isAnalyticsConfigured() {
        return !StringUtils.isNullOrEmpty(this.rsids) && !StringUtils.isNullOrEmpty(server);
    }

    /**
     * Generates and returns the base URL for an Analytics request.
     *
     * @param sdkVersion {@link String} containing the version of the SDK, as provided by the {@link SystemInfoService}
     * @return {@code String} containing the base URL for an Analytics request
     */
    String getBaseURL(final String sdkVersion) {
        final URLBuilder urlBuilder = new URLBuilder();
        urlBuilder.enableSSL(true)
                .setServer(server)
                .addPath("b")
                .addPath("ss")
                .addPath(rsids)
                .addPath(getAnalyticsResponseType())
                .addPath(sdkVersion)
                .addPath("s");
        String url = urlBuilder.build();

        if (url == null) {
            return "";
        }

        return url;
    }

    boolean isVisitorIDServiceEnabled() {
        return !StringUtils.isNullOrEmpty(marketingCloudOrganizationID);
    }

    private String getAnalyticsResponseType() {
        return analyticsForwardingEnabled ? "10" : "0";
    }

    boolean isAnalyticsForwardingEnabled() {
        return analyticsForwardingEnabled;
    }

    void setAnalyticsForwardingEnabled(final boolean analyticsForwardingEnabled) {
        this.analyticsForwardingEnabled = analyticsForwardingEnabled;
    }

    boolean isOfflineTrackingEnabled() {
        return offlineEnabled;
    }

    void setOfflineEnabled(final boolean offlineEnabled) {
        this.offlineEnabled = offlineEnabled;
    }

    int getBatchLimit() {
        return batchLimit;
    }

    void setBatchLimit(final int batchLimit) {
        this.batchLimit = batchLimit;
    }

    void setReferrerTimeout(final int referrerTimeout) {
        this.referrerTimeout = referrerTimeout;
    }

    String getMarketingCloudOrganizationID() {
        return marketingCloudOrganizationID;
    }

    void setMarketingCloudOrganizationID(final String marketingCloudOrganizationID) {
        this.marketingCloudOrganizationID = marketingCloudOrganizationID;
    }

    String getRsids() {
        return rsids;
    }

    void setRsids(final String rsids) {
        this.rsids = rsids;
    }

    String getServer() {
        return server;
    }

    void setServer(final String server) {
        this.server = server;
    }

    MobilePrivacyStatus getPrivacyStatus() {
        return privacyStatus;
    }

    void setPrivacyStatus(final MobilePrivacyStatus privacyStatus) {
        this.privacyStatus = privacyStatus;
    }

    boolean isOptIn() {
        return privacyStatus == MobilePrivacyStatus.OPT_IN;
    }

    void setBackdateSessionInfoEnabled(final boolean backdateSessionInfoEnabled) {
        this.backdateSessionInfoEnabled = backdateSessionInfoEnabled;
    }

    boolean isBackdateSessionInfoEnabled() {
        return backdateSessionInfoEnabled;
    }

    boolean isAssuranceSessionActive() {
        return assuranceSessionActive;
    }

    int getSessionTimeout() {
        return sessionTimeout;
    }

    long getLifecycleMaxSessionLength() {
        return lifecycleMaxSessionLength;
    }

    long getLifecycleSessionStartTimestamp() {
        return lifecycleSessionStartTimestamp;
    }
}