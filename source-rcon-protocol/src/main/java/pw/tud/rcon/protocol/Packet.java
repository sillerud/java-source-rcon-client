package pw.tud.rcon.protocol;

import java.io.*;
import java.nio.charset.Charset;

public class Packet {
    private static final byte[] END_BYTES = new byte[] {0x00, 0x00};

    private int packetLength;
    private int packetId;
    private Type packetType;
    private String body;

    public Packet(int packetId, Type packetType, String body) {
        this.body = body;
        packetLength = 10 + body.length();
        this.packetId = packetId;
        this.packetType = packetType;
    }

    public Packet() {

    }

    public int getPacketId() {
        return packetId;
    }

    public Type getPacketType() {
        return packetType;
    }

    public String getBody() {
        return body;
    }

    public void read(InputStream in) throws IOException {
        packetLength = readBigEndianInt(in);
        packetId = readBigEndianInt(in);
        packetType = Type.valueOfServer(readBigEndianInt(in));
        byte[] bodyBytes = new byte[packetLength - 10];
        if (bodyBytes.length != 0) {
            if (in.read(bodyBytes) == -1)
                throw new IOException("Stream corrupted.");
        }
        if (in.read() != 0 || in.read() != 0)
            throw new IOException("Stream corrupted.");
        body = new String(bodyBytes, Charset.forName("ASCII"));

    }

    public void write(OutputStream out) throws IOException {
        writeBigEndianInt(packetLength, out);
        writeBigEndianInt(packetId, out);
        writeBigEndianInt(packetType.getType(), out);
        out.write(body.getBytes(Charset.forName("ASCII")));
        out.write(END_BYTES);
    }

    public int readBigEndianInt(InputStream in) throws IOException {
        int result = 0;
        for (int i = 0; i < 4; i++) {
            result |= in.read() << i * 8;
        }
        return result;
    }

    public void writeBigEndianInt(int number, OutputStream out) throws IOException {
        out.write(number);
        out.write(number >> 8);
        out.write(number >> 16);
        out.write(number >> 24);
    }

    public enum Type
    {
        SERVERDATA_RESPONSE_VALUE(0, true),
        SERVERDATA_EXECCOMMAND(2, false),
        SERVERDATA_AUTH_RESPONSE(2, true),
        SERVERDATA_AUTH(3, false);

        Type(int type, boolean server) {
            this.type = type;
            this.server = server;
        }

        private int type;
        private boolean server;
        public int getType() {
            return type;
        }

        public static Type valueOfServer(int i) {
            for (Type type : values())
            {
                if (type.server && type.getType() == i)
                    return type;
            }
            return null;
        }
    }
}
