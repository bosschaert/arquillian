/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.impl.handler;

import java.util.Arrays;

import org.jboss.arquillian.impl.context.ClassContext;
import org.jboss.arquillian.impl.context.SuiteContext;
import org.jboss.arquillian.impl.context.TestContext;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.TestEnricher;
import org.jboss.arquillian.spi.event.suite.TestEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Verifies that the TestEnricher SPI is called.
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCaseEnricherTestCase
{
   @Mock
   private ServiceLoader serviceLoader;
   
   @Mock
   private TestEnricher enricher;
   
   @Test
   public void shouldCallAllEnrichers() throws Exception
   {
      Mockito.when(serviceLoader.all(TestEnricher.class)).thenReturn(Arrays.asList(enricher, enricher));
      
      TestContext context = new TestContext(new ClassContext(new SuiteContext(serviceLoader)));
      TestEvent event = new TestEvent(this, getClass().getMethod("shouldCallAllEnrichers"));
      
      TestCaseEnricher handler = new TestCaseEnricher();
      handler.callback(context, event);
      
      Mockito.verify(enricher, Mockito.times(2)).enrich(context, this);
   }
}
