package ir.piana.dev.webtool2.server.websocket;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

public class PianaWebSocketHandler {
    private Logger logger = Logger
            .getLogger(PianaWebSocketHandler.class);
    private Socket socket;

    public PianaWebSocketHandler(Socket socket) {
        this.socket = socket;
    }

    public boolean sendText(String text) {
        try {
            OutputStream os = socket.getOutputStream();
            int textLength = text.length() > 125 ?
                    (text.length() > 65536 ? 8 : 2) : 0;
            byte[] bytes = new byte[text.length() + 2 + textLength];
            bytes[0] = (byte)0x81;
            if(textLength == 0)
                bytes[1] = (byte) (text.length() & 0x7f);
            else if(textLength == 2) {
                bytes[1] = 126;
                bytes[2] = (byte)(text.length() >> 8);
                bytes[3] = (byte)(text.length());
            }
            else if(textLength == 8) {
                bytes[1] = 127;
                bytes[2] = (byte)(text.length() >> 56);
                bytes[3] = (byte)(text.length() >> 48);
                bytes[4] = (byte)(text.length() >> 40);
                bytes[5] = (byte)(text.length() >> 32);
                bytes[6] = (byte)(text.length() >> 24);
                bytes[7] = (byte)(text.length() >> 16);
                bytes[8] = (byte)(text.length() >> 8);
                bytes[9] = (byte)(text.length());
            }
            System.arraycopy(text.getBytes(Charset.forName("UTF-8")),
                    0, bytes, 2 + textLength, text.length());
            os.write(bytes);
        } catch (IOException e) {
            logger.error(e);
            return false;
        }
        return true;
    }

    public boolean closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.error(e);
            return false;
        }
        return true;
    }
}
