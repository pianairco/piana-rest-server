package ir.piana.dev.webtool2.server.test;

import ir.piana.dev.webtool2.server.model.UserModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

/**
 * Created by SYSTEM on 7/31/2017.
 */
public class PostTest {
    private Client client;
    static final String BASE_URI =
            "http://localhost:8000/";

    @Before
    public void before() {
        client = ClientBuilder.newClient();
    }

    @Test
    public void testPostRequest() {
        WebTarget target = client.target(BASE_URI);
        UserModel userModel = new UserModel("ali", "rahmati");
        Entity<UserModel> userModelEntity = Entity.json(userModel);

        javax.ws.rs.core.Response response =
                target.path("send-form")
                        .request().post(userModelEntity);
        String hello = response
                .readEntity(String.class);
        Assert.assertEquals("Hello World", hello);
        response.close();
    }
}
