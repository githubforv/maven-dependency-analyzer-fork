package org.apache.maven.shared.dependency.analyzer;

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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.test.plugin.BuildTool;
import org.apache.maven.shared.test.plugin.ProjectTool;
import org.apache.maven.shared.test.plugin.TestToolsException;
import org.codehaus.plexus.PlexusTestCase;

/**
 * Tests <code>DefaultProjectDependencyAnalyzer</code>.
 * 
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @version $Id$
 * @see DefaultProjectDependencyAnalyzer
 */
public class DefaultProjectDependencyAnalyzerTest
    extends PlexusTestCase
{
    // fields -----------------------------------------------------------------

    private BuildTool buildTool;

    private ProjectTool projectTool;

    private ProjectDependencyAnalyzer analyzer;

    // TestCase methods -------------------------------------------------------

    /*
     * @see org.codehaus.plexus.PlexusTestCase#setUp()
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();

        buildTool = (BuildTool) lookup( BuildTool.ROLE );

        projectTool = (ProjectTool) lookup( ProjectTool.ROLE );

        analyzer = (ProjectDependencyAnalyzer) lookup( ProjectDependencyAnalyzer.ROLE );
    }

    // tests ------------------------------------------------------------------

    public void testPom()
        throws TestToolsException, ProjectDependencyAnalyzerException
    {
        compileProject( "pom/pom.xml" );

        MavenProject project = getProject( "pom/pom.xml" );

        ProjectDependencyAnalysis actualAnalysis = analyzer.analyze( project );

        ProjectDependencyAnalysis expectedAnalysis = new ProjectDependencyAnalysis();

        assertEquals( expectedAnalysis, actualAnalysis );
    }

    public void testJarWithNoDependencies()
        throws TestToolsException, ProjectDependencyAnalyzerException
    {
        compileProject( "jarWithNoDependencies/pom.xml" );

        MavenProject project = getProject( "jarWithNoDependencies/pom.xml" );

        ProjectDependencyAnalysis actualAnalysis = analyzer.analyze( project );

        ProjectDependencyAnalysis expectedAnalysis = new ProjectDependencyAnalysis();

        assertEquals( expectedAnalysis, actualAnalysis );
    }

    public void testJarWithCompileDependency()
        throws TestToolsException, ProjectDependencyAnalyzerException
    {
        compileProject( "jarWithCompileDependency/pom.xml" );

        MavenProject project2 = getProject( "jarWithCompileDependency/project2/pom.xml" );

        ProjectDependencyAnalysis actualAnalysis = analyzer.analyze( project2 );

        Artifact project1 =
            createArtifact( "org.apache.maven.shared.dependency-analyzer.tests", "jarWithCompileDependency1", "jar",
                            "1.0", "compile" );
        Set usedDeclaredArtifacts = Collections.singleton( project1 );
        ProjectDependencyAnalysis expectedAnalysis = new ProjectDependencyAnalysis( usedDeclaredArtifacts, null, null );

        assertEquals( expectedAnalysis, actualAnalysis );
    }

    public void testJarWithTestDependency()
        throws TestToolsException, ProjectDependencyAnalyzerException
    {
        compileProject( "jarWithTestDependency/pom.xml" );

        MavenProject project2 = getProject( "jarWithTestDependency/project2/pom.xml" );

        ProjectDependencyAnalysis actualAnalysis = analyzer.analyze( project2 );

        Artifact project1 =
            createArtifact( "org.apache.maven.shared.dependency-analyzer.tests", "jarWithTestDependency1", "jar",
                            "1.0", "test" );
        Set usedDeclaredArtifacts = Collections.singleton( project1 );

        // TODO: remove workaround for SUREFIRE-300 when 2.3.1 released
        Artifact junit = createArtifact( "junit", "junit", "jar", "3.8.1", "test" );
        Set unusedDeclaredArtifacts = Collections.singleton( junit );

        ProjectDependencyAnalysis expectedAnalysis =
            new ProjectDependencyAnalysis( usedDeclaredArtifacts, null, unusedDeclaredArtifacts );

        assertEquals( expectedAnalysis, actualAnalysis );
    }

    // private methods --------------------------------------------------------

    private void compileProject( String pomPath )
        throws TestToolsException
    {
        File pom = getTestFile( "target/test-classes/", pomPath );
        Properties properties = new Properties();
        List goals = Arrays.asList( new String[] { "clean", "install" } );
        File log = new File( pom.getParentFile(), "build.log" );

        // TODO: don't install test artifacts to local repository
        InvocationResult result = buildTool.executeMaven( pom, properties, goals, log );

        assertNull( "Error compiling test project", result.getExecutionException() );
        assertEquals( "Error compiling test project", 0, result.getExitCode() );
    }

    private MavenProject getProject( String pomPath )
        throws TestToolsException
    {
        File pom = getTestFile( "target/test-classes/", pomPath );

        return projectTool.readProjectWithDependencies( pom );
    }

    private Artifact createArtifact( String groupId, String artifactId, String type, String version, String scope )
    {
        VersionRange versionRange = VersionRange.createFromVersion( version );
        ArtifactHandler handler = new DefaultArtifactHandler();

        return new DefaultArtifact( groupId, artifactId, versionRange, scope, type, null, handler );
    }
}
