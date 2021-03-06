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
package org.jboss.arquillian.api;

/**
 * Describes the different RunModes a Single test can run in. 
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public enum RunModeType
{
   /**
    * In AS_CLIENT mode the @Deployment is processed and deployed to the Container, 
    * but the test is not executed inside the container.  
    */
   AS_CLIENT,
   
   /**
    * In IN_CONTAINER mode the @Deployment is processed and deployed to the container along side the test
    * case and the test case is executed inside the container. <br/>
    * This is the default mode when none specified. 
    */
   IN_CONTAINER
}
