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

package com.adobe.marketing.mobile;

/**
 * Listens for {@link EventType#GENERIC_TRACK}, {@link EventSource#REQUEST_CONTENT} events.
 *
 * <p>
 * Monitor Generic Track events for sending track requests to the Experience Edge.
 * @see AnalyticsEdgeInternal
 */
public class GenericTrackRequestContentListener extends ExtensionListener {

    GenericTrackRequestContentListener(final ExtensionApi extensionApi, final String type, final String source) {
        super(extensionApi, type, source);
    }

    @Override
    public void hear(final Event event) {

        if (event == null || event.getEventData() == null) {
            Log.debug(AnalyticsEdgeConstants.LOG_TAG, "Event or Event data is null.");
            return;
        }

        final AnalyticsEdgeInternal parentExtension = (AnalyticsEdgeInternal) super.getParentExtension();

        if (parentExtension == null) {
            return;
        }

        parentExtension.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                parentExtension.queueEvent(event);
                parentExtension.processEvents();
            }
        });
    }
}
