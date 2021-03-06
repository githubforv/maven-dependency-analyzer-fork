package org.apache.maven.shared.dependency.analyzer.asm;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.shared.dependency.analyzer.ClassFileVisitor;
import org.objectweb.asm.ClassReader;

/**
 * 
 * 
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @version $Id: DependencyClassFileVisitor.java 661727 2008-05-30 14:21:49Z bentmann $
 */
public class DependencyClassFileVisitor
    implements ClassFileVisitor
{
    // fields -----------------------------------------------------------------

    private final Set dependencies;

    // constructors -----------------------------------------------------------

    public DependencyClassFileVisitor()
    {
        dependencies = new HashSet();
    }

    // ClassFileVisitor methods -----------------------------------------------

    /*
     * @see org.apache.maven.shared.dependency.analyzer.ClassFileVisitor#visitClass(java.lang.String,
     *      java.io.InputStream)
     */
    public void visitClass( String className, InputStream in )
    {
        try
        {
            ClassReader reader = new ClassReader( in );
            DependencyVisitor visitor = new DependencyVisitor();

            reader.accept( visitor, 0 );

            dependencies.addAll( visitor.getClasses() );
        }
        catch ( IOException exception )
        {
            exception.printStackTrace();
        }
        catch (IndexOutOfBoundsException e)
        {
            //some bug inside ASM causes an IOB exception. Log it and move on?
            //this happens when the class isn't valid.
            System.out.println("Unable to process: "+ className);
        }
    }

    // public methods ---------------------------------------------------------

    public Set getDependencies()
    {
        return dependencies;
    }
}
