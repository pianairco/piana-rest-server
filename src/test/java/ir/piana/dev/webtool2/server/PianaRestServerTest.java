package ir.piana.dev.webtool2.server;

import ir.piana.dev.webtool2.server.annotation.PianaServer;
import ir.piana.dev.webtool2.server.annotation.PianaSpaceProperties;
import ir.piana.dev.webtool2.server.annotation.PianaSpaceProperty;

/**
 * Created by SYSTEM on 7/31/2017.
 */
@PianaServer()
@PianaSpaceProperties(properties = {
    @PianaSpaceProperty(name = "msg", value = "hi president")
})
public class PianaRestServerTest {
    public static void main(String[] args)
            throws Exception {
        PianaAnnotationAppMain.start(PianaRestServerTest.class);
    }
}
