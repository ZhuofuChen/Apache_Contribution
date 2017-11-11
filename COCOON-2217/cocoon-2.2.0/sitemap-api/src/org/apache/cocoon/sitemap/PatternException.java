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
package org.apache.cocoon.sitemap;

import org.apache.avalon.framework.CascadingException;

/**
 * This exception is thrown by a <code>URIMatcher</code> or by a
 * <code>URITranslator</code> when there's something wrong with the matching or
 * translation patterns.
 *
 * @version $Id: PatternException.java 587751 2007-10-24 02:41:36Z vgritsenko $
 */
public class PatternException extends CascadingException {

    /**
     * Construct a new <code>PatternException</code> instance.
     */
    public PatternException(String message) {
        super(message, null);
    }

    /**
     * Creates a new <code>PatternException</code> instance.
     *
     * @param ex an <code>Exception</code> value
     */
    public PatternException(Exception ex) {
        super(ex.getMessage(), ex);
    }

    /**
     * Construct a new <code>PatternException</code> that references
     * a parent Exception.
     */
    public PatternException(String message, Throwable t) {
        super(message, t);
    }
}
