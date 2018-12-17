/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.hono.application;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.eclipse.hono.client.HonoClient;
import org.eclipse.hono.client.impl.HonoClientImpl;
import org.eclipse.hono.config.ClientConfigProperties;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;

@RunWith(VertxUnitRunner.class)
public class ApplicationTest {

    static HonoClient honoClient;
    static String tenantId = "kapua-sys";

    @Test
    public void testTenantAPI(final TestContext ctx) {
        final ClientConfigProperties config = new ClientConfigProperties();
        config.setPort(25671);
        config.setUsername("hono-client@HONO");
        config.setPassword("secret");
        config.setRequestTimeout(2000);
        honoClient = new HonoClientImpl(Vertx.vertx(), config);

        final Async testComplete = ctx.async();

        honoClient.connect().compose(
                connected -> connected.getOrCreateTenantClient()
                        .compose(client -> client.get(tenantId))
                        .map(tenantObject -> {
                            System.out.println("Tenant " + tenantObject.getTenantId());
                            ctx.assertEquals(tenantId, tenantObject.getTenantId());
                            testComplete.complete();
                            return tenantObject;
                        }).otherwise(throwable -> {
                            throwable.printStackTrace();
                            ctx.fail("No such tenant: " + tenantId);
                            return null;
                        })

        );

        testComplete.await();
    }

    @AfterClass
    public static void shutdown(final TestContext ctx) {
       if (honoClient != null) {
           honoClient.shutdown();
       }
    }

}
