package com.netsim.standard.HTTP;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link HTTP} protocol adapter.
 */
public class HTTPTest {

    private HTTP httpGet;
    private HTTP httpPost;

    @Before
    public void setUp() {
        httpGet  = new HTTP(HTTPMethods.GET,  "/test",    "example.com");
        httpPost = new HTTP(HTTPMethods.POST, "/submit", "api.example.com");
    }

    /**
     * HTTP constructor should throw if method is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullMethodThrows() {
        new HTTP(null, "/path", "host");
    }

    /**
     * HTTP constructor should throw if path is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullPathThrows() {
        new HTTP(HTTPMethods.GET, null, "host");
    }

    /**
     * HTTP constructor should throw if host is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullHostThrows() {
        new HTTP(HTTPMethods.GET, "/path", null);
    }
    

    /**
     * encapsulate should throw if content is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void encapsulateNullContentThrows() {
        httpGet.encapsulate(null);
    }

    /**
     * encapsulate should return a valid HTTPRequest containing the same content.
     */
    @Test
    public void encapsulateReturnsRequestWithContent() {
        String content = "hello world";
        HTTPRequest req = httpGet.encapsulate(content);
        assertNotNull("HTTPRequest should not be null", req);
        assertEquals("getContent() should return original content",
                     content, req.getContent());
    }

    /**
     * decapsulate should extract the original content from HTTPRequest.
     */
    @Test
    public void decapsulateReturnsOriginalContent() {
        String body = "payload data";
        HTTPRequest req = httpPost.encapsulate(body);
        String result = httpPost.decapsulate(req);
        assertEquals("decapsulate should return original body",
                     body, result);
    }

    /**
     * decapsulate should throw if request is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void decapsulateNullThrows() {
        httpGet.decapsulate(null);
    }
}
