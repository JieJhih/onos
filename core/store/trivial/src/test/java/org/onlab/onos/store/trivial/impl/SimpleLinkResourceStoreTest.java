/*
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onlab.onos.store.trivial.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.onlab.onos.net.DeviceId.deviceId;
import static org.onlab.onos.net.Link.Type.DIRECT;
import static org.onlab.onos.net.PortNumber.portNumber;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onlab.onos.net.AnnotationKeys;
import org.onlab.onos.net.Annotations;
import org.onlab.onos.net.ConnectPoint;
import org.onlab.onos.net.DefaultAnnotations;
import org.onlab.onos.net.DefaultLink;
import org.onlab.onos.net.Link;
import org.onlab.onos.net.provider.ProviderId;
import org.onlab.onos.net.resource.Bandwidth;
import org.onlab.onos.net.resource.BandwidthResourceAllocation;
import org.onlab.onos.net.resource.LambdaResourceAllocation;
import org.onlab.onos.net.resource.LinkResourceAllocations;
import org.onlab.onos.net.resource.LinkResourceStore;
import org.onlab.onos.net.resource.ResourceAllocation;
import org.onlab.onos.net.resource.ResourceType;

/**
 * Test of the simple LinkResourceStore implementation.
 */
public class SimpleLinkResourceStoreTest {

    private LinkResourceStore store;
    private SimpleLinkResourceStore simpleStore;
    private Link link1;
    private Link link2;
    private Link link3;

    /**
     * Returns {@link Link} object.
     *
     * @param dev1 source device
     * @param port1 source port
     * @param dev2 destination device
     * @param port2 destination port
     * @return created {@link Link} object
     */
    private Link newLink(String dev1, int port1, String dev2, int port2) {
        Annotations annotations = DefaultAnnotations.builder()
                .set(AnnotationKeys.OPTICAL_WAVES, "80")
                .set(AnnotationKeys.BANDWIDTH, "1000000")
                .build();
        return new DefaultLink(
                new ProviderId("of", "foo"),
                new ConnectPoint(deviceId(dev1), portNumber(port1)),
                new ConnectPoint(deviceId(dev2), portNumber(port2)),
                DIRECT, annotations);
    }

    @Before
    public void setUp() throws Exception {
        simpleStore = new SimpleLinkResourceStore();
        simpleStore.activate();
        store = simpleStore;

        link1 = newLink("of:1", 1, "of:2", 2);
        link2 = newLink("of:2", 1, "of:3", 2);
        link3 = newLink("of:3", 1, "of:4", 2);
    }

    @After
    public void tearDown() throws Exception {
        simpleStore.deactivate();
    }

    /**
     * Tests constructor and activate method.
     */
    @Test
    public void testConstructorAndActivate() {
        final Iterable<LinkResourceAllocations> allAllocations = store.getAllocations();
        assertNotNull(allAllocations);
        assertFalse(allAllocations.iterator().hasNext());

        final Iterable<LinkResourceAllocations> linkAllocations =
                store.getAllocations(link1);
        assertNotNull(linkAllocations);
        assertFalse(linkAllocations.iterator().hasNext());

        final Set<ResourceAllocation> res = store.getFreeResources(link2);
        assertNotNull(res);
    }

    /**
     * Picks up and returns one of bandwidth allocations from a given set.
     *
     * @param resources the set of {@link ResourceAllocation}s
     * @return {@link BandwidthResourceAllocation} object if found, null
     *         otherwise
     */
    private BandwidthResourceAllocation getBandwidthObj(Set<ResourceAllocation> resources) {
        for (ResourceAllocation res : resources) {
            if (res.type() == ResourceType.BANDWIDTH) {
                return ((BandwidthResourceAllocation) res);
            }
        }
        return null;
    }

    /**
     * Returns all lambda allocations from a given set.
     *
     * @param resources the set of {@link ResourceAllocation}s
     * @return a set of {@link LambdaResourceAllocation} objects
     */
    private Set<LambdaResourceAllocation> getLambdaObjs(Set<ResourceAllocation> resources) {
        Set<LambdaResourceAllocation> lambdaResources = new HashSet<>();
        for (ResourceAllocation res : resources) {
            if (res.type() == ResourceType.LAMBDA) {
                lambdaResources.add((LambdaResourceAllocation) res);
            }
        }
        return lambdaResources;
    }

    /**
     * Tests initial free bandwidth for a link.
     */
    @Test
    public void testInitialBandwidth() {
        final Set<ResourceAllocation> freeRes = store.getFreeResources(link1);
        assertNotNull(freeRes);

        final BandwidthResourceAllocation alloc = getBandwidthObj(freeRes);
        assertNotNull(alloc);

        assertEquals(Bandwidth.valueOf(1000000.0), alloc.bandwidth());
    }

    /**
     * Tests initial free lambda for a link.
     */
    @Test
    public void testInitialLambdas() {
        final Set<ResourceAllocation> freeRes = store.getFreeResources(link3);
        assertNotNull(freeRes);

        final Set<LambdaResourceAllocation> res = getLambdaObjs(freeRes);
        assertNotNull(res);
        assertEquals(80, res.size());
    }
}
