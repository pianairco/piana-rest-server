package ir.piana.dev.webtool2.server;

import ir.piana.dev.webtool2.server.annotation.PianaServer;

/**
 * Created by SYSTEM on 7/31/2017.
 */
@PianaServer()
public class PianaRestServerTest {
    public static void main(String[] args)
            throws Exception {
        PianaAnnotationAppMain.start(PianaRestServerTest.class);
    }
}
