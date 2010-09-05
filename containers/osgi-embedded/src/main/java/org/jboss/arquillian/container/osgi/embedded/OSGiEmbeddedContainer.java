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
package org.jboss.arquillian.container.osgi.embedded;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.jboss.arquillian.container.osgi.AbstractOSGiContainer;
import org.jboss.arquillian.osgi.OSGiContainer;
import org.jboss.arquillian.protocol.jmx.JMXMethodExecutor;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.osgi.spi.framework.OSGiBootstrap;
import org.jboss.osgi.spi.framework.OSGiBootstrapProvider;
import org.jboss.shrinkwrap.api.Archive;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

/**
 * The embedded OSGi container.
 *
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class OSGiEmbeddedContainer extends AbstractOSGiContainer
{
   // Provide logging
   private static final Logger log = Logger.getLogger(OSGiEmbeddedContainer.class);

   private Framework framework;

   private void bootstrapFramework(Context context)
   {
      log.debug("Bootstrap framework ...");
      OSGiBootstrapProvider provider = OSGiBootstrap.getBootstrapProvider();
      framework = provider.getFramework();
      context.add(Framework.class, framework);
   }

   private void startFramework(Context context)
   {
      log.debug("Start framework: " + framework);
      try
      {
         framework.start();
         context.add(BundleContext.class, framework.getBundleContext());
      }
      catch (BundleException ex)
      {
         throw new IllegalStateException("Cannot start embedded OSGi Framework", ex);
      }
   }

   private void stopFramework()
   {
      log.debug("Stop framework: " + framework);
      try
      {
         framework.stop();
         framework.waitForStop(3000);
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Cannot stop embedded OSGi Framework", ex);
      }
      finally
      {
         framework = null;
      }
   }

   public ContainerMethodExecutor deploy(Context context, final Archive<?> archive) throws DeploymentException
   {
      // start the framework lazily as part of @BeforeClass
      if (framework == null)
      {
         bootstrapFramework(context);
         startFramework(context);
         installSupportBundles();
      }

      return super.deploy(context, archive);
   }

   public void undeploy(Context context, Archive<?> archive) throws DeploymentException
   {
      super.undeploy(context, archive);

      // Stop the Framework as part of @AfterClass
      stopFramework();
   }

   @Override
   public ContainerMethodExecutor getMethodExecutor(Properties props)
   {
      MBeanServer mbeanServer = findOrCreateMBeanServer();
      props.put(JMXMethodExecutor.EMBEDDED_EXECUTION, Boolean.TRUE);
      return new JMXMethodExecutor(mbeanServer, props);
   }

   @Override
   public BundleHandle installBundle(Archive<?> archive) throws BundleException
   {
      BundleContext context = framework.getBundleContext();
      Bundle bundle = OSGiContainer.installBundle(context, archive);
      return new BundleHandle(bundle.getBundleId(), bundle.getSymbolicName());
   }

   @Override
   public BundleHandle installBundle(URL bundleURL) throws BundleException, IOException
   {
      BundleContext sysContext = framework.getBundleContext();
      Bundle bundle = sysContext.installBundle(bundleURL.toExternalForm());
      return new BundleHandle(bundle.getBundleId(), bundle.getSymbolicName());
   }

   @Override
   public void uninstallBundle(BundleHandle handle) throws BundleException
   {
      Bundle bundle = getBundle(handle);
      bundle.uninstall();
   }

   @Override
   public int getBundleState(BundleHandle handle)
   {
      Bundle bundle = getBundle(handle);
      return bundle != null ? bundle.getState() : Bundle.UNINSTALLED;
   }
   
   @Override
   public void startBundle(BundleHandle handle) throws BundleException
   {
      Bundle bundle = getBundle(handle);
      bundle.start();
   }

   @Override
   public void stopBundle(BundleHandle handle) throws BundleException
   {
      Bundle bundle = getBundle(handle);
      bundle.stop();
   }

   @Override
   public boolean isBundleInstalled(String symbolicName)
   {
      Bundle[] bundles = framework.getBundleContext().getBundles();
      for (Bundle aux : bundles)
      {
         if (symbolicName.equals(aux.getSymbolicName()))
            return true;
      }
      return false;
   }

   private Bundle getBundle(BundleHandle handle)
   {
      BundleContext sysContext = framework.getBundleContext();
      Bundle bundle = sysContext.getBundle(handle.getBundleId());
      return bundle;
   }

   private MBeanServer findOrCreateMBeanServer()
   {
      MBeanServer mbeanServer = null;

      ArrayList<MBeanServer> serverArr = MBeanServerFactory.findMBeanServer(null);
      if (serverArr.size() > 1)
         log.warn("Multiple MBeanServer instances: " + serverArr);

      if (serverArr.size() > 0)
         mbeanServer = serverArr.get(0);

      if (mbeanServer == null)
      {
         log.debug("No MBeanServer, create one ...");
         mbeanServer = MBeanServerFactory.createMBeanServer();
      }

      return mbeanServer;
   }
}