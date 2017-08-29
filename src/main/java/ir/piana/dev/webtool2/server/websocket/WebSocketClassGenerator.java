package ir.piana.dev.webtool2.server.websocket;

import ir.piana.dev.secure.hash.HashMaker;
import ir.piana.dev.secure.hash.HashType;
import ir.piana.dev.webtool2.server.annotation.AnnotationController;
import ir.piana.dev.webtool2.server.annotation.PianaServer;
import ir.piana.dev.webtool2.server.annotation.PianaWebSocket;
import ir.piana.dev.webtool2.server.session.Session;
import ir.piana.dev.webtool2.server.session.SessionManager;
import org.apache.log4j.Logger;

import javax.ws.rs.core.Cookie;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mohammad Rahmati, 5/7/2017 5:20 PM
 */
public class WebSocketClassGenerator {
    final static Logger logger =
            Logger.getLogger(WebSocketClassGenerator.class);

    public static Set<ServerSocket> generateWebsocketClasses(
            PianaServer pianaServer)
            throws Exception {
        Set<ServerSocket> serverSockets = new HashSet<>();
        List<Class> webSocketClasses = AnnotationController
                .getClassesAnnotatedWithWebSocket();

        for(Class c : webSocketClasses) {
            PianaWebSocket pianaWebSocket = AnnotationController
                    .getPianaWebSocket(c);
            pianaWebSocket.socketIp();
            InetAddress addr = InetAddress.getByName(pianaWebSocket.socketIp());
            ServerSocket serverSocket = new ServerSocket(
                    pianaWebSocket.socketPort(), 0, addr);
            serverSockets.add(serverSocket);
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            executorService.execute(() -> {
                try {
                    while (true) {
                        Socket client = serverSocket.accept();
                        executorService.execute(() -> {
                            System.out.println("A client connected.");

                            try {
                                InputStream in = client.getInputStream();

                                OutputStream out = client.getOutputStream();

                                String data = new Scanner(in, "UTF-8")
                                        .useDelimiter("\\r\\n\\r\\n").next();
//                                System.out.println(data);
                                Matcher get = Pattern.compile("^GET").matcher(data);
                                if (get.find()) {
                                    Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                                    match.find();
                                    byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                                            + "Connection: Upgrade\r\n"
                                            + "Upgrade: websocket\r\n"
                                            + "Sec-WebSocket-Accept: "
                                            + HashMaker.getBase64Hash((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"), HashType.SHA)
                                            + "\r\n\r\n").getBytes("UTF-8");
                                    out.write(response, 0, response.length);
//                                    byte[] inBytes = new byte[1024];
//                                    int read = in.read(inBytes);
//                                    if(read > 0) {
//                                        System.out.println(new String(inBytes));
//                                        String hex = DatatypeConverter.printHexBinary(inBytes);
//                                        System.out.println(hex);
//
//                                        int i1 = inBytes[0] & 0x0f;
//                                        if(i1 == 1)
//                                            System.out.println("text data");
//                                        int mask = inBytes[1] & 0x80 >> 7;
//                                        if(mask == 1)
//                                            System.out.println("exist mask");
//                                        int len = inBytes[1] & 0x7f;
//                                        System.out.println("len is : " + len);
//
//                                        byte[] maskBytes = null;
//                                        if(mask == 1)
//                                            maskBytes = new byte[]{inBytes[2], inBytes[3], inBytes[4], inBytes[5]};
//
//                                        byte[] maskData = null;
//                                        if(len >0 && len < 126) {
//                                            maskData = new byte[len];
//                                            System.arraycopy(inBytes, 6, maskData, 0, len);
//                                        }
//
//                                        byte[] dataBytes = new byte[len];
//                                        for (int i = 0; i < len; i++) {
//                                            dataBytes[i] = (byte)(maskData[i] ^ maskBytes[i % 4]);
//                                        }
//
//                                        System.out.println(new String(dataBytes));
//
//                                        byte[] outBytes = new byte[7];
//                                        outBytes[0] = (byte)0x81;
//                                        outBytes[1] = (byte)0x05;
//                                        System.arraycopy(dataBytes, 0, outBytes, 2, 5);
//                                        out.write(outBytes);
//                                    }
                                    Matcher cookie = Pattern.compile(".*Cookie.*").matcher(data);
                                    if(cookie.find()) {
                                        System.out.println("have cookie");
                                        Matcher pianaSession = Pattern.compile(".*" +
                                                pianaServer.serverSession().sessionName() +
                                                ".*").matcher(data);
                                        if(pianaSession.find()) {
                                            System.out.println("have session");
                                            int startIndex = data.indexOf(pianaServer.serverSession().sessionName());
                                            String substring = data.substring(startIndex);
                                            String sessionKey = substring.substring(substring.indexOf("=") + 1,
                                                    substring.indexOf("=") + 1 + 36);
                                            System.out.println(substring);
                                            Session session = SessionManager.getSessionManager(pianaServer.serverSession())
                                                    .retrieveSessionIfExist(sessionKey);
                                            if(session != null && session.getWebSocketHandler() != null) {
                                                session.getWebSocketHandler().closeSocket();
                                            }
                                            session.setWebSocketHandler(new PianaWebSocketHandler(client));
                                        }
                                    }
                                } else {

                                }
                            } catch (IOException e) {
                                logger.error(e);
                            } catch (Exception e) {
                                logger.error(e);
                            }
                        });
                    }
                } catch (IOException e) {
                    logger.error(e);
                }
            });
        }
        return serverSockets;
    }
}
