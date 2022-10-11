package ru.greus;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortTimeoutException;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static ru.greus.Screen.Command.*;

/**
 * SDK for R-Call QR-screen
 */
public class Screen implements Closeable {
    enum Command {
        V,
        ID,
        IDS,
        Q,
        QL,
        T1,
        T2,
        CQ,
        CQL,
        OFF;

        @Override
        public String toString() {
            return "[" + name() + "]";
        }
    }

    private static final int BAUD_RATE = 115200;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;
    private static final int PARITY = 0;
    private static final int DEFAULT_READ_TIMEOUT = 5 * 1000;
    private static final int DEFAULT_WRITE_TIMEOUT = 5 * 1000;
    private static final String POSITIVE_ANSWER_STRING = "OK";
    private final SerialPort port;

    public Screen(String portTitle) {
        port = SerialPort.getCommPort(portTitle);
        port.openPort();
        port.setComPortParameters(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT);
    }

    @Override
    public void close() {
        port.closePort();
    }

    /**
     * Sets read timeout for the port
     * @param timeout in milliseconds
     */
    public void setReadTimeout(int timeout) {
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, timeout, port.getWriteTimeout());
    }

    /**
     * Sets write timeout for the port
     * @param timeout in milliseconds
     */
    public void setWriteTimeout(int timeout) {
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, port.getReadTimeout(), timeout);
    }

    /**
     * Returns the device version
     * @return {@link String} contains the device version (example: QR-1.3.10.7789)
     * @throws IOException if port not available or other IO error occurs
     */
    public String getVersion() throws IOException {
        return sendString(V).substring(V.toString().length());
    }

    /**
     * Returns an unique ID
     * @return {@link String} contains an unique id (example: 4FFCB0033130363208473130)
     * @throws IOException if port not available or other IO error occurs
     */
    public String getId() throws IOException {
        return sendString(ID).substring(ID.toString().length());
    }

    /**
     * Shows a unique ID QR code on the screen
     * @return true if shown or false
     * @throws IOException if port not available or other IO error occurs
     */
    public boolean showId() throws IOException {
        return checkResponse(IDS, sendString(IDS));
    }

    /**
     * Clears the QR code section with the logo
     * @return true if cleared or false
     * @throws IOException if port not available or other IO error occurs
     */
    public boolean clear() throws IOException {
        return checkResponse(CQ, sendString(CQ));
    }

    /**
     * Clears the QR code section without the logo
     * @return true if cleared or false
     * @throws IOException if port not available or other IO error occurs
     */
    public boolean clearWithoutLogo() throws IOException {
        return checkResponse(CQL, sendString(CQL));
    }

    /**
     * Switches off the screen
     * @return true if switched off or false
     * @throws IOException if port not available or other IO error occurs
     */
    public boolean switchOff() throws IOException {
        return checkResponse(OFF, sendString(OFF));
    }

    /**
     * Shows the QR code without the logo
     * @param text {@link String} to display
     * @return true if shown or false
     * @throws IOException if port not available or other IO error occurs
     */
    public boolean showQr(String text) throws IOException {
        return checkResponse(Q, sendString(Q, text));
    }

    /**
     * Shows the QR code with the logo
     * @param text {@link String} to display
     * @return true if shown or false
     * @throws IOException if port not available or other IO error occurs
     */
    public boolean showQrWithLogo(String text) throws IOException {
        return checkResponse(QL, sendString(QL, text));
    }

    /**
     * Shows the header
     * @param text {@link String} to display
     * @return true if shown or false
     * @throws IOException if port not available or other IO error occurs
     */
    public boolean showHeader(String text) throws IOException {
        return checkResponse(T1, sendString(T1, text));
    }

    /**
     * Shows the footer
     * @param text {@link String} to display
     * @return true if shown or false
     * @throws IOException if port not available or other IO error occurs
     */
    public boolean showFooter(String text) throws IOException {
        return checkResponse(T2, sendString(T2, text));
    }

    /**
     * Sends a command to the screen
     * @param command {@link Command} for execution
     * @return the response string or null if timeout reached
     * @throws IOException if port not available or other IO error occurs
     */
    private String sendString(Command command) throws IOException {
        return sendString(command, null);
    }

    /**
     * Sends a command with a string to the screen
     * @param command {@link Command} for execution
     * @param text {@link String} in addition to the command
     * @return the response string or null if timeout reached
     * @throws IOException if port not available or other IO error occurs
     */
    private String sendString(Command command, String text) throws IOException {
        var sb = new StringBuilder(command.toString());

        if (text != null) {
            sb.append(text);
        }

        sb.append("\n");

        return sendBytes(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Sends bytes to the screen
     * @param bytes array to send
     * @return the response string or null if timeout reached
     * @throws IOException if port not available or other IO error occurs
     */
    private String sendBytes(byte[] bytes) throws IOException {
        clearPort();

        try (var os = port.getOutputStream()){
            os.write(bytes);
            return readString();
        } catch (SerialPortTimeoutException ignored) {
            return null;
        }
    }

    /**
     * Reads string from screen
     * @return the response string or null if timeout reached
     * @throws IOException if port not available or other IO error occurs
     */
    private String readString() throws IOException {
        try (var r = new BufferedReader(new InputStreamReader(port.getInputStream()))) {
            return r.readLine();
        }
    }

    /**
     * Checks response
     * @param command {@link Command} for check
     * @param response {@link String} for check
     * @return true if response is positive or false
     */
    private boolean checkResponse(Command command, String response) {
        var positiveResponse = command.toString() + POSITIVE_ANSWER_STRING;

        return positiveResponse.equals(response);
    }

    /**
     * Clears signals, states and buffers of the port
     */
    private void clearPort() {
        port.clearBreak();
        port.clearDTR();
        port.clearRTS();
        port.flushIOBuffers();
    }
}
