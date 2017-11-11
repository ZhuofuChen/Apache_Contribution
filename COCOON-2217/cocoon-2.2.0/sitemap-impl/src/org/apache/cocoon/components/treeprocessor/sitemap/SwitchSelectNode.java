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
package org.apache.cocoon.components.treeprocessor.sitemap;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.SimpleSelectorProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.selection.SwitchSelector;

/**
 *
 * @version $Id: SwitchSelectNode.java 587751 2007-10-24 02:41:36Z vgritsenko $
 */
public class SwitchSelectNode extends SimpleSelectorProcessingNode
    implements ParameterizableProcessingNode {

    /** The parameters of this node */
    private Map parameters;

    private ProcessingNode[][] whenNodes;

    private VariableResolver[] whenTests;

    private ProcessingNode[] otherwhiseNodes;

    public SwitchSelectNode(String name) {
        super(Selector.ROLE + "Selector", name);
    }

    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }

    public void setCases(ProcessingNode[][] whenNodes, VariableResolver[] whenTests, ProcessingNode[] otherwhiseNodes) {
        this.whenNodes = whenNodes;
        this.whenTests = whenTests;
        this.otherwhiseNodes = otherwhiseNodes;
    }

    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {
	
      	// Perform any common invoke functionality 
        super.invoke(env, context);

        // Prepare data needed by the action
        final Map objectModel = env.getObjectModel();
        Parameters resolvedParams = VariableResolver.buildParameters(this.parameters, context, objectModel);

        SwitchSelector switchSelector = (SwitchSelector)getComponent();

        Object ctx = switchSelector.getSelectorContext(objectModel, resolvedParams);
       
        try {
            for (int i = 0; i < this.whenTests.length; i++) {
                if (this.executor.invokeSwitchSelector(this, 
                        objectModel, 
                        switchSelector, 
                        whenTests[i].resolve(context, objectModel), 
                        resolvedParams, 
                        ctx)) {
                    return invokeNodes(this.whenNodes[i], env, context);
                }
            }

            if (this.otherwhiseNodes != null) {
                return invokeNodes(this.otherwhiseNodes, env, context);
            }

            return false;
        } finally {
            releaseComponent(switchSelector);
        }
    }
}
