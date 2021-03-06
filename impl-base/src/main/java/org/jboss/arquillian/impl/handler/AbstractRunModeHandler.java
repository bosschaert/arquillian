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

import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;

/**
 * Abstract RunMode Handler for checking and delegating the different run modes to 
 * sub classes. 
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public abstract class AbstractRunModeHandler<T extends ClassEvent> implements EventHandler<T>
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.event.suite.EventHandler#callback(org.jboss.arquillian.spi.Context, java.lang.Object)
    */
   public final void callback(Context context, ClassEvent event) throws Exception
   {
      boolean isClientMode = false;
      if(event.getTestClass().isAnnotationPresent(Run.class))
      {
         RunModeType runModeType = event.getTestClass().getAnnotation(Run.class).value();
         if(RunModeType.AS_CLIENT == runModeType) 
         {
            isClientMode = true;
         }
      }
      if(isClientMode)
      {
         hasClientRunMode(context);
      } 
      else
      {
         hasContainerRunMode(context);
      }
   }
   
   protected abstract void hasClientRunMode(Context context);
   
   protected abstract void hasContainerRunMode(Context context);
}