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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MobileCore.class, Log.class})

public class AnalyticsPublicAPITests {
    @Before
    public void before() {
        PowerMockito.mockStatic(MobileCore.class);
        PowerMockito.mockStatic(Log.class);
    }

    @Test
    public void test_extensionVersionAPI() {
        // test
        String extensionVersion = Analytics.extensionVersion();
        assertEquals("The Extension version API returns the correct value", AnalyticsConstants.EXTENSION_VERSION,
                extensionVersion);
    }

    @Test
    public void test_registerExtensionAPI() {
        // test
        Analytics.registerExtension();
        final ArgumentCaptor<ExtensionErrorCallback> callbackCaptor = ArgumentCaptor.forClass(ExtensionErrorCallback.class);

        // The monitor extension should register with core
        PowerMockito.verifyStatic(MobileCore.class, Mockito.times(1));
        MobileCore.registerExtension(ArgumentMatchers.eq(AnalyticsInternal.class), callbackCaptor.capture());

        // verify the callback
        ExtensionErrorCallback extensionErrorCallback = callbackCaptor.getValue();
        assertNotNull("The extension callback should not be null", extensionErrorCallback);

        // should not crash on calling the callback
        extensionErrorCallback.error(ExtensionError.UNEXPECTED_ERROR);
    }

    @Test
    public void test_clearQueue() {
        // test
        Analytics.clearQueue();
        PowerMockito.verifyStatic(Log.class, times(1));

        // verify
        Log.debug("AnalyticsEdge", "clearQueue - is not currently supported with Edge");
    }

    @Test
    public void test_getQueueSize() {
        // setup
        final AdobeError[] error = new AdobeError[1];
        final long[] queueSize = new long[1];
        // test
        Analytics.getQueueSize(new AdobeCallbackWithError<Long>() {
            @Override
            public void fail(AdobeError adobeError) {
                error[0] = adobeError;
            }

            @Override
            public void call(Long aLong) {
                queueSize[0] = aLong;
            }
        });

        // verify
        assertEquals(AdobeError.CALLBACK_NULL, error[0]);
        assertEquals(0, queueSize[0]);
    }

}
