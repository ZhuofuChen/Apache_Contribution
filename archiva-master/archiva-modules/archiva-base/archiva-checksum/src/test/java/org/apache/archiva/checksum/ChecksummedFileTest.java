package org.apache.archiva.checksum;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.archiva.common.utils.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * ChecksummedFileTest
 *
 *
 */

public class ChecksummedFileTest
    extends AbstractChecksumTestCase
{
    /**
     * SHA1 checksum from www.ibiblio.org/maven2, incuding file path
     */
    private static final String SERVLETAPI_SHA1 = "bcc82975c0f9c681fcb01cc38504c992553e93ba";

    private static final String REMOTE_METADATA_SHA1 = "da39a3ee5e6b4b0d3255bfef95601890afd80709";

    private static final String REMOTE_METADATA_MD5 = "d41d8cd98f00b204e9800998ecf8427e";

    private static final Charset FILE_ENCODING = Charset.forName( "UTF-8" );


    @Before
    public void cleanTestDir()
    {
        try
        {
            FileUtils.deleteDirectory( getTestOutputDir() );
        }
        catch ( IOException ex )
        {
            LoggerFactory.getLogger( ChecksummedFileTest.class ).warn( ex.getMessage(), ex );
        }
    }

    private Path createTestableJar( String filename )
        throws IOException
    {
        Path srcFile = getTestResource( filename );
        Path destFile = getTestOutputDir( ).resolve( srcFile.getFileName());
        Files.copy( srcFile, destFile, StandardCopyOption.REPLACE_EXISTING );
        return destFile;
    }

    private Path createTestableJar( String filename, boolean copySha1, boolean copyMd5 )
        throws IOException
    {
        Path srcFile = getTestResource( filename );
        Path jarFile = getTestOutputDir().resolve(srcFile.getFileName() );
        Files.copy( srcFile, jarFile, StandardCopyOption.REPLACE_EXISTING );

        if ( copySha1 )
        {
            Path srcSha1 = srcFile.resolveSibling( srcFile.getFileName() + ".sha1" );
            Path sha1File = jarFile.resolveSibling( jarFile.getFileName() + ".sha1" );

            Files.copy( srcSha1, sha1File, StandardCopyOption.REPLACE_EXISTING );
        }

        if ( copyMd5 )
        {
            Path srcMd5 = srcFile.resolveSibling( srcFile.getFileName() + ".md5" );
            Path md5File = jarFile.resolveSibling( jarFile.getFileName() + ".md5" );

            Files.copy( srcMd5, md5File, StandardCopyOption.REPLACE_EXISTING );
        }

        return jarFile;
    }

    @Test
    public void testCalculateChecksumMd5()
        throws IOException
    {
        Path testfile = getTestResource( "examples/redback-authz-open.jar" );
        ChecksummedFile checksummedFile = new ChecksummedFile( testfile );
        String expectedChecksum = "f42047fe2e177ac04d0df7aa44d408be";
        String actualChecksum = checksummedFile.calculateChecksum( ChecksumAlgorithm.MD5 );
        assertEquals( expectedChecksum, actualChecksum );
    }

    @Test
    public void testCalculateChecksumSha1()
        throws IOException
    {
        Path testfile = getTestResource( "examples/redback-authz-open.jar" );
        ChecksummedFile checksummedFile = new ChecksummedFile( testfile );
        String expectedChecksum = "2bb14b388973351b0a4dfe11d171965f59cc61a1";
        String actualChecksum = checksummedFile.calculateChecksum( ChecksumAlgorithm.SHA1 );
        assertEquals( expectedChecksum, actualChecksum );
    }

    @Test
    public void testCreateChecksum()
        throws IOException
    {
        Path testableJar = createTestableJar( "examples/redback-authz-open.jar" );
        ChecksummedFile checksummedFile = new ChecksummedFile( testableJar );
        checksummedFile.createChecksum( ChecksumAlgorithm.SHA1 );
        Path hashFile = checksummedFile.getChecksumFile( ChecksumAlgorithm.SHA1 );
        assertTrue( "ChecksumAlgorithm file should exist.", Files.exists(hashFile) );
        String hashContents = org.apache.commons.io.FileUtils.readFileToString( hashFile.toFile() );
        hashContents = StringUtils.trim( hashContents );
        assertEquals( "2bb14b388973351b0a4dfe11d171965f59cc61a1  redback-authz-open.jar", hashContents );
    }

    @Test
    public void testFixChecksum()
        throws IOException
    {
        Path jarFile = createTestableJar( "examples/redback-authz-open.jar" );
        Path sha1File = jarFile.resolveSibling( jarFile.getFileName()+ ".sha1" );

        // A typical scenario seen in the wild.
        org.apache.commons.io.FileUtils.writeStringToFile( sha1File.toFile(), "sha1sum: redback-authz-open.jar: No such file or directory" );

        ChecksummedFile checksummedFile = new ChecksummedFile( jarFile );
        assertFalse( "ChecksummedFile.isValid(SHA1) == false",
                     checksummedFile.isValidChecksum( ChecksumAlgorithm.SHA1 ) );

        boolean fixed = checksummedFile.fixChecksums( new ChecksumAlgorithm[]{ ChecksumAlgorithm.SHA1 } );
        assertTrue( "ChecksummedFile.fixChecksums() == true", fixed );

        assertTrue( "ChecksummedFile.isValid(SHA1) == true",
                    checksummedFile.isValidChecksum( ChecksumAlgorithm.SHA1 ) );
    }

    @Test
    public void testGetChecksumFile()
    {
        ChecksummedFile checksummedFile = new ChecksummedFile( Paths.get( "test.jar" ) );
        assertEquals( "test.jar.sha1", checksummedFile.getChecksumFile( ChecksumAlgorithm.SHA1 ).getFileName().toString() );
    }

    @Test
    public void testIsValidChecksum()
        throws IOException
    {
        Path jarFile = createTestableJar( "examples/redback-authz-open.jar", true, false );

        ChecksummedFile checksummedFile = new ChecksummedFile( jarFile );
        assertTrue( "ChecksummedFile.isValid(SHA1)", checksummedFile.isValidChecksum( ChecksumAlgorithm.SHA1 ) );
    }

    @Test
    public void testIsValidChecksumInvalidSha1Format()
        throws IOException
    {
        Path jarFile = createTestableJar( "examples/redback-authz-open.jar" );
        Path sha1File = jarFile.resolveSibling( jarFile.getFileName() + ".sha1" );

        // A typical scenario seen in the wild.
        FileUtils.writeStringToFile( sha1File, FILE_ENCODING, "sha1sum: redback-authz-open.jar: No such file or directory" );

        ChecksummedFile checksummedFile = new ChecksummedFile( jarFile );
        assertFalse( "ChecksummedFile.isValid(SHA1)", checksummedFile.isValidChecksum( ChecksumAlgorithm.SHA1 ) );

    }

    @Test
    public void testIsValidChecksumNoChecksumFiles()
        throws IOException
    {
        Path jarFile = createTestableJar( "examples/redback-authz-open.jar", false, false );

        ChecksummedFile checksummedFile = new ChecksummedFile( jarFile );
        assertFalse( "ChecksummedFile.isValid(SHA1,MD5)", checksummedFile.isValidChecksums(
            new ChecksumAlgorithm[]{ ChecksumAlgorithm.SHA1, ChecksumAlgorithm.MD5 } ) );

    }

    @Test
    public void testIsValidChecksumSha1AndMd5()
        throws IOException
    {
        Path jarFile = createTestableJar( "examples/redback-authz-open.jar", true, true );

        ChecksummedFile checksummedFile = new ChecksummedFile( jarFile );
        assertTrue( "ChecksummedFile.isValid(SHA1,MD5)", checksummedFile.isValidChecksums(
            new ChecksumAlgorithm[]{ ChecksumAlgorithm.SHA1, ChecksumAlgorithm.MD5 } ) );
    }

    @Test
    public void testIsValidChecksumSha1NoMd5()
        throws IOException
    {
        Path jarFile = createTestableJar( "examples/redback-authz-open.jar", true, false );

        ChecksummedFile checksummedFile = new ChecksummedFile( jarFile );
        assertTrue( "ChecksummedFile.isValid(SHA1)", checksummedFile.isValidChecksums(
            new ChecksumAlgorithm[]{ ChecksumAlgorithm.SHA1, ChecksumAlgorithm.MD5 } ) );

    }

    @Test
    public void testParseChecksum()
        throws IOException
    {
        String expected = SERVLETAPI_SHA1
            + "  /home/projects/maven/repository-staging/to-ibiblio/maven2/servletapi/servletapi/2.4/servletapi-2.4.pom";

        Path testfile = getTestResource( "examples/redback-authz-open.jar" );
        ChecksummedFile checksummedFile = new ChecksummedFile( testfile );
        String s = checksummedFile.parseChecksum( expected, ChecksumAlgorithm.SHA1,
                                                  "servletapi/servletapi/2.4/servletapi-2.4.pom" );
        assertEquals( "Checksum doesn't match", SERVLETAPI_SHA1, s );

    }

    @Test
    public void testParseChecksumAltDash1()
        throws IOException
    {
        String expected = SERVLETAPI_SHA1 + "  -";
        Path testfile = getTestResource( "examples/redback-authz-open.jar" );
        ChecksummedFile checksummedFile = new ChecksummedFile( testfile );
        String s = checksummedFile.parseChecksum( expected, ChecksumAlgorithm.SHA1,
                                                  "servletapi/servletapi/2.4/servletapi-2.4.pom" );
        assertEquals( "Checksum doesn't match", SERVLETAPI_SHA1, s );
    }

    @Test
    public void testParseChecksumAltDash2()
        throws IOException
    {
        String expected = "SHA1(-)=" + SERVLETAPI_SHA1;
        Path testfile = getTestResource( "examples/redback-authz-open.jar" );
        ChecksummedFile checksummedFile = new ChecksummedFile( testfile );
        String s = checksummedFile.parseChecksum( expected, ChecksumAlgorithm.SHA1,
                                                  "servletapi/servletapi/2.4/servletapi-2.4.pom" );
        assertEquals( "Checksum doesn't match", SERVLETAPI_SHA1, s );
    }

    @Test
    public void testRemoteMetadataChecksumFilePathSha1()
        throws IOException
    {
        String expected = REMOTE_METADATA_SHA1 + "  /home/test/repository/examples/metadata/maven-metadata.xml";
        Path testfile = getTestResource( "examples/metadata/maven-metadata-remote.xml" );
        ChecksummedFile checksummedFile = new ChecksummedFile( testfile );

        try
        {
            String s = checksummedFile.parseChecksum( expected, ChecksumAlgorithm.SHA1, "maven-metadata-remote.xml" );
            assertEquals( "Checksum doesn't match", REMOTE_METADATA_SHA1, s );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            fail( "IOException should not occur." );
        }
    }

    @Test
    public void testRemoteMetadataChecksumFilePathMd5()
        throws IOException
    {
        String expected = REMOTE_METADATA_MD5 + "  ./examples/metadata/maven-metadata.xml";
        Path testfile = getTestResource( "examples/metadata/maven-metadata-remote.xml" );
        ChecksummedFile checksummedFile = new ChecksummedFile( testfile );

        try
        {
            String s = checksummedFile.parseChecksum( expected, ChecksumAlgorithm.MD5, "maven-metadata-remote.xml" );
            assertEquals( "Checksum doesn't match", REMOTE_METADATA_MD5, s );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            fail( "IOException should not occur." );
        }
    }
}
