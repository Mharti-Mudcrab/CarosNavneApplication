package GenderAPI;

import com.google.gson.Gson;
import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HTTPClient 
{
    private CloseableHttpClient httpClient;
    private CloseableHttpResponse response;
    private HttpEntity entity;
    private final String address = "https://api.genderize.io?name=";
    Gson gson;

    public HTTPClient() 
    {
        httpClient = HttpClients.createDefault();
        gson = new Gson();
    }

    public String sendHTTP(String s) 
    {
        httpClient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(address + s);
        try 
        {
            response = httpClient.execute(httpget);
        } 
        catch (IOException exception) 
        {
            System.err.println(exception.getMessage() + "\n" + exception);
        }
        return this.readHTTP();
    }

    public String readHTTP() 
    {
        try 
        {
            if (response != null) 
            {
                entity = response.getEntity();
                if (entity != null) 
                {
                String s = EntityUtils.toString(entity);
                System.out.println(s);
                GenderGson g = gson.fromJson(s, GenderGson.class);
                System.out.println(g.getName() + ", " + g.getGender() + ", " + g.getProbability() + ", " + g.getError());

                    if (g.getError() == null) {
                        if (g.getGender() == null) {
                            return "Unknown";
                        }
                        switch (g.getGender()) {
                            case "male":
                                return "Man";
                            case "female":
                                return "Woman";
                            default:
                                return "Unknown";
                        }
                    }
                }
            }
        } 
        catch (IOException exception) 
        {
            System.err.println(exception.getMessage() + "\n" + exception);
        }
        return "error";
    }
}
