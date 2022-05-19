package integration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;

import java.io.File;
import java.nio.file.Path;  
import java.nio.file.Paths;

import org.json.JSONObject;
import org.json.JSONArray;

import org.ga4gh.starterkit.drs.testutils.ResourceLoader;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DemoServerCheckExpandTest
{
    // Define variables and constants
    private static final String DEFAULT_PUBLIC_URL = "http://localhost:4500/ga4gh/drs/v1/objects/";
    private static final String CUSTOM_PUBLIC_URL = "http://localhost:7000/ga4gh/drs/v1/objects/";

    // DRS Object directory
    private static final String OBJ_DIR = "/responses/objects/getObjectById/";
    
    @DataProvider(name = "cases")
    public Object[][] getData() 
    {
        return new Object[][] 
        {
            {
                DEFAULT_PUBLIC_URL,
                "b8cd0667-2c33-4c9f-967b-161b905932c9",
                "04.json",
                false
            },  
            {
                DEFAULT_PUBLIC_URL,
                "b8cd0667-2c33-4c9f-967b-161b905932c9",
                "05.json",
                true
            },   
            {
                CUSTOM_PUBLIC_URL,
                "b8cd0667-2c33-4c9f-967b-161b905932c9",
                "04-custom.json",
                false
            },  
            {
                CUSTOM_PUBLIC_URL,
                "b8cd0667-2c33-4c9f-967b-161b905932c9",
                "05-custom.json",
                true
            }         
        };
    }

    @Test(dataProvider = "cases")
    public void testDemoServerCheckexpand(String requestURL, String drsObjectId, String expFileName, Boolean expand) throws Exception 
    {
        HttpClient client = HttpClient.newHttpClient();

        Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(requestURL + drsObjectId + "?expand=" + String.valueOf(expand))); //Ask for the object

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        String requestedObj = response.body();

        // Load local file containing expected response and assert
        String drsObjectExpFile = OBJ_DIR + expFileName;
        String expResponseBody = ResourceLoader.load(drsObjectExpFile);
        JSONObject exp_JSON = new JSONObject(expResponseBody);
        
        // If access_methods exists, set access_id to 0
        JSONObject req_JSON = new JSONObject(requestedObj);

        if(req_JSON.has("access_methods"))
        {
            JSONArray JSONarray = req_JSON.getJSONArray("access_methods");
            JSONObject JSONobj_access_methods = JSONarray.getJSONObject(0);     
            JSONobj_access_methods.put("access_id", "00000000-0000-0000-0000-000000000000");
        }
        
        requestedObj = req_JSON.toString();
        expResponseBody = exp_JSON.toString();

        Assert.assertEquals(requestedObj, expResponseBody);
    }
}
