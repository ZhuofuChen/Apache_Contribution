/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.classloader;

import javax.servlet.ServletContext;

/**
 * A <code>ClassLoader</code> factory, setting up the classpath given a
 * &lt;classpath&gt; configuration.
 *
 * @version $Id: ClassLoaderFactory.java 587751 2007-10-24 02:41:36Z vgritsenko $
 * @since 2.2
 */
public interface ClassLoaderFactory {

    final static String ROLE = ClassLoaderFactory.class.getName();

    ClassLoader createClassLoader(ClassLoader              parent,
                                  ClassLoaderConfiguration config,
                                  ServletContext           servletContext)
    throws Exception;
}