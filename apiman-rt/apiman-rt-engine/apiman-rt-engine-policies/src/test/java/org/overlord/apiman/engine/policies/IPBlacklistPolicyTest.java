/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.apiman.engine.policies;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.overlord.apiman.engine.policies.config.IPBlacklistConfig;
import org.overlord.apiman.rt.engine.beans.PolicyFailure;
import org.overlord.apiman.rt.engine.beans.PolicyFailureType;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.components.IPolicyFailureFactoryComponent;
import org.overlord.apiman.rt.engine.policy.IPolicyChain;
import org.overlord.apiman.rt.engine.policy.IPolicyContext;

/**
 * Unit test.
 *
 * @author eric.wittmann@redhat.com
 */
public class IPBlacklistPolicyTest {

    /**
     * Test method for {@link org.overlord.apiman.engine.policies.IPBlacklistPolicy#parseConfiguration(java.lang.String)}.
     */
    @Test
    public void testParseConfiguration() {
        IPBlacklistPolicy policy = new IPBlacklistPolicy();
        
        // Empty config test
        String config = "{}"; //$NON-NLS-1$
        Object parsed = policy.parseConfiguration(config);
        Assert.assertNotNull(parsed);
        Assert.assertEquals(IPBlacklistConfig.class, parsed.getClass());
        IPBlacklistConfig parsedConfig = (IPBlacklistConfig) parsed;
        Assert.assertNotNull(parsedConfig.getIpList());
        Assert.assertTrue(parsedConfig.getIpList().isEmpty());
        
        // Single IP address
        config = "{" +  //$NON-NLS-1$
                "  \"ipList\" : [" +  //$NON-NLS-1$
                "    \"1.2.3.4\"" +  //$NON-NLS-1$
                "  ]" +  //$NON-NLS-1$
                "}"; //$NON-NLS-1$
        parsed = policy.parseConfiguration(config);
        parsedConfig = (IPBlacklistConfig) parsed;
        Assert.assertNotNull(parsedConfig.getIpList());
        Assert.assertEquals(1, parsedConfig.getIpList().size());
        Assert.assertEquals("1.2.3.4", parsedConfig.getIpList().iterator().next()); //$NON-NLS-1$

        // Multiple IP addresses
        config = "{" +  //$NON-NLS-1$
                "  \"ipList\" : [" +  //$NON-NLS-1$
                "    \"1.2.3.4\"," +  //$NON-NLS-1$
                "    \"3.4.5.6\"," +  //$NON-NLS-1$
                "    \"10.0.0.11\"" +  //$NON-NLS-1$
                "  ]" +  //$NON-NLS-1$
                "}"; //$NON-NLS-1$
        parsed = policy.parseConfiguration(config);
        parsedConfig = (IPBlacklistConfig) parsed;
        Assert.assertNotNull(parsedConfig.getIpList());
        Assert.assertEquals(3, parsedConfig.getIpList().size());
    }

    /**
     * Test method for {@link org.overlord.apiman.engine.policies.IPBlacklistPolicy#parseConfiguration(java.lang.String)}.
     */
    @Test
    public void testApply() {
        IPBlacklistPolicy policy = new IPBlacklistPolicy();
        String json = "{" +  //$NON-NLS-1$
                "  \"ipList\" : [" +  //$NON-NLS-1$
                "    \"1.2.3.4\"," +  //$NON-NLS-1$
                "    \"3.4.5.6\"," +  //$NON-NLS-1$
                "    \"10.0.0.11\"" +  //$NON-NLS-1$
                "  ]" +  //$NON-NLS-1$
                "}"; //$NON-NLS-1$
        Object config = policy.parseConfiguration(json);
        ServiceRequest request = new ServiceRequest();
        request.setType("GET"); //$NON-NLS-1$
        request.setApiKey("12345"); //$NON-NLS-1$
        request.setRemoteAddr("1.2.3.4"); //$NON-NLS-1$
        request.setDestination("/"); //$NON-NLS-1$
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        final PolicyFailure failure = new PolicyFailure();
        Mockito.when(context.getComponent(IPolicyFailureFactoryComponent.class)).thenReturn(new IPolicyFailureFactoryComponent() {
            @Override
            public PolicyFailure createFailure(PolicyFailureType type, int failureCode, String message) {
                return failure;
            }
        });
        IPolicyChain chain = Mockito.mock(IPolicyChain.class);
        
        // Failure
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doFailure(failure);
        
        // Success
        request.setRemoteAddr("9.8.7.6"); //$NON-NLS-1$
        chain = Mockito.mock(IPolicyChain.class);
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doApply(request);
    }

}
