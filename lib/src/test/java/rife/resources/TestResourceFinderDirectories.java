/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;
import rife.resources.exceptions.ResourceFinderErrorException;
import rife.tools.ExceptionUtils;
import rife.tools.FileUtils;
import rife.tools.InnerClassException;
import rife.tools.InputStreamUser;
import rife.tools.exceptions.FileUtilsErrorException;

import static org.junit.jupiter.api.Assertions.*;

public class TestResourceFinderDirectories {
    public static String mainResourcesPath() {
        return System.getenv("project.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources";
    }

    public static String testResourcesPath() {
        return System.getenv("project.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources";
    }

    @Test
    void testInstantiation() {
        ResourceFinder resource_finder1 = new ResourceFinderDirectories(new File[]{new File(testResourcesPath())});
        ResourceFinder resource_finder2 = new ResourceFinderDirectories(new File[]{new File(testResourcesPath())});
        assertNotNull(resource_finder1);
        assertNotNull(resource_finder2);
        assertNotSame(resource_finder1, resource_finder2);
    }

    @Test
    void testGetUnknownResource() {
        var resource_finder = new ResourceFinderDirectories(new File[]{
            new File(mainResourcesPath()),
            new File(testResourcesPath())});

        assertNull(resource_finder.getResource("this/resource/doesnt/exist.txt"));
    }

    @Test
    void testGetResourceByName() {
        var resource_finder = new ResourceFinderDirectories(new File[]{
            new File(mainResourcesPath()),
            new File(testResourcesPath())});

        assertNotNull(resource_finder.getResource("resources/test.txt"));
    }

    @Test
    void testGetUnknownStreamByName() {
        var resource_finder = new ResourceFinderDirectories(new File[]{
            new File(mainResourcesPath()),
            new File(testResourcesPath())});

        try {
            resource_finder.useStream("this/resource/doesnt/exist.txt", new InputStreamUser() {
                public Object useInputStream(InputStream stream)
                throws InnerClassException {
                    assertNull(stream);

                    return null;
                }
            });
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetUnknownStreamByResource() {
        var resource_finder = new ResourceFinderDirectories(new File[]{
            new File(mainResourcesPath()),
            new File(testResourcesPath())});

        try {
            resource_finder.useStream(new URL("file://this/resource/doesnt/exist.txt"), new InputStreamUser() {
                public Object useInputStream(InputStream stream)
                throws InnerClassException {
                    assertNull(stream);

                    return null;
                }
            });
        } catch (ResourceFinderErrorException | MalformedURLException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetStreamByName() {
        var resource_finder = new ResourceFinderDirectories(new File[]{
            new File(mainResourcesPath()),
            new File(testResourcesPath())});

        try {
            resource_finder.useStream("resources/test.txt", new InputStreamUser() {
                public Object useInputStream(InputStream stream)
                throws InnerClassException {
                    assertNotNull(stream);
                    try {
                        assertEquals("""
                            This just contains some text to
                            verify if

                            resources can be found

                            and
                            read

                            correctly.
                            """, FileUtils.readString(stream));
                    } catch (FileUtilsErrorException e) {
                        assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
                    }
                    return null;
                }
            });
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetStreamByRkesource() {
        var resource_finder = new ResourceFinderDirectories(new File[]{
            new File(mainResourcesPath()),
            new File(testResourcesPath())});

        var resource = resource_finder.getResource("resources/test.txt");
        try {
            resource_finder.useStream(resource, new InputStreamUser() {
                public Object useInputStream(InputStream stream)
                throws InnerClassException {
                    assertNotNull(stream);
                    try {
                        assertEquals("""
                            This just contains some text to
                            verify if

                            resources can be found

                            and
                            read

                            correctly.
                            """, FileUtils.readString(stream));
                    } catch (FileUtilsErrorException e) {
                        assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
                    }
                    return null;
                }
            });
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetUnknownContentByName() {
        var resource_finder = new ResourceFinderDirectories(new File[]{
            new File(mainResourcesPath()),
            new File(testResourcesPath())});

        try {
            String content = resource_finder.getContent("this/resource/doesnt/exist.txt");
            assertNull(content);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetUnknownContentByResource() {
        var resource_finder = new ResourceFinderDirectories(new File[]{
            new File(mainResourcesPath()),
            new File(testResourcesPath())});

        try {
            String content = resource_finder.getContent(new URL("file://this/resource/doesnt/exist.txt"));
            assertNull(content);
        } catch (ResourceFinderErrorException | MalformedURLException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetContentByName() {
        var resource_finder = new ResourceFinderDirectories(new File[]{
            new File(mainResourcesPath()),
            new File(testResourcesPath())});

        try {
            String content = resource_finder.getContent("resources/test.txt");
            assertNotNull(content);
            assertEquals(content, """
                This just contains some text to
                verify if

                resources can be found

                and
                read

                correctly.
                """);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetContentByResource() {
        var resource_finder = new ResourceFinderDirectories(new File[]{
            new File(mainResourcesPath()),
            new File(testResourcesPath())});

        var resource = resource_finder.getResource("resources/test.txt");
        try {
            String content = resource_finder.getContent(resource);
            assertNotNull(content);
            assertEquals(content, """
                This just contains some text to
                verify if

                resources can be found

                and
                read

                correctly.
                """);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetContentByNameAndEncoding() {
        var resource_finder = new ResourceFinderDirectories(new File[]{
            new File(mainResourcesPath()),
            new File(testResourcesPath())});

        try {
            String content = resource_finder.getContent("resources/test-utf8.txt", "UTF-8");
            assertNotNull(content);
            assertEquals(content, """
                This just contains some text to
                verify if

                resources can be found

                and
                read

                correctly.
                Here are some encoding-specific chars : ¡¢£¤¥¦§¨©ª«¬­®.
                """);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetContentByResourceAndEncoding() {
        var resource_finder = new ResourceFinderDirectories(new File[]{
            new File(mainResourcesPath()),
            new File(testResourcesPath())});

        var resource = resource_finder.getResource("resources/test-utf8.txt");
        try {
            String content = resource_finder.getContent(resource, "UTF-8");
            assertNotNull(content);
            assertEquals(content, """
                This just contains some text to
                verify if

                resources can be found

                and
                read

                correctly.
                Here are some encoding-specific chars : ¡¢£¤¥¦§¨©ª«¬­®.
                """);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetUnknownModificationTimeByName() {
        var resource_finder = new ResourceFinderDirectories(new File[]{
            new File(mainResourcesPath()),
            new File(testResourcesPath())});

        try {
            long time = resource_finder.getModificationTime("this/resource/doesnt/exist.txt");
            assertEquals(-1, time);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetUnknownModificationTimeByResource() {
        var resource_finder = new ResourceFinderDirectories(new File[]{
            new File(mainResourcesPath()),
            new File(testResourcesPath())});

        try {
            long time = resource_finder.getModificationTime(new URL("file://this/resource/doesnt/exist.txt"));
            assertEquals(-1, time);
        } catch (ResourceFinderErrorException | MalformedURLException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetModificationTimeByName() {
        var resource_finder = new ResourceFinderDirectories(new File[]{
            new File(mainResourcesPath()),
            new File(testResourcesPath())});

        try {
            long time = resource_finder.getModificationTime("resources/test.txt");
            assertTrue(time != -1);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testGetModificationTimeByResource() {
        var resource_finder = new ResourceFinderDirectories(new File[]{
            new File(mainResourcesPath()),
            new File(testResourcesPath())});

        var resource = resource_finder.getResource("resources/test.txt");
        try {
            long time = resource_finder.getModificationTime(resource);
            assertTrue(time != -1);
        } catch (ResourceFinderErrorException e) {
            assertFalse(false, ExceptionUtils.getExceptionStackTrace(e));
        }
    }
}
