package ka.masato.library.device.pasori.service;

import ka.masato.library.device.pasori.exception.CardDataDecodeErrorException;
import ka.masato.library.device.pasori.exception.IlligalCardDataException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CardDataDecoder {
    private String IDmString = "";
    private String PMmString = "";
    private byte[] cmdPayload;


    public CardDataDecoder() {
    }


    public CardDataDecoder laodPacket(byte[] packet) {
        cmdPayload = extractCmdPayload(packet);
        return this;
    }

    public CardDataDecoder decodeIDm() {

        if (cmdPayload == null) {
            throw new CardDataDecodeErrorException("Should not decode before load packet function.");
        }

        if (cmdPayload[6] == (byte) 0x14 && cmdPayload[7] == (byte) 0x01) {
            ByteBuffer bb = ByteBuffer.wrap(cmdPayload);
            byte[] idm = new byte[8];
            bb.get(idm, 8, 8);
            IDmString = convertBytes2String(idm);
        } else {
            //TODO Runtime exception is better.
            throw new CardDataDecodeErrorException("Illigal cmd payload, can not decode IDm");
        }

        return this;
    }

    public CardDataDecoder decodePMm() {

        if (cmdPayload == null) {
            throw new CardDataDecodeErrorException("Shoud not decode begore load packet function.");
        }

        if (cmdPayload[6] == (byte) 0x14 && cmdPayload[7] == (byte) 0x01) {
            ByteBuffer bb = ByteBuffer.wrap(cmdPayload);
            byte[] ppm = new byte[8];
            bb.get(ppm, 16, 8);
            PMmString = convertBytes2String(ppm);
        } else {
            throw new CardDataDecodeErrorException("Illigal cmd payload can not decode IDm");
        }

        return this;
    }

    public String getIDm() {
        return IDmString;
    }

    public String getPMm() {
        return PMmString;
    }

    public byte[] getCmdPayload() {
        return cmdPayload;
    }

    private String convertBytes2String(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02X", bytes[i]));
        }
        return sb.toString();
    }

    private byte[] extractCmdPayload(byte[] packet) {
        if (packet.length <= 10) {
            throw new IlligalCardDataException("Can not load packet, because packet length illigal.");

            //TODO Throw Exception.
        }
        short length = ByteBuffer.wrap(new byte[]{packet[5], packet[6]})
                .order(ByteOrder.LITTLE_ENDIAN).getShort();
        ByteBuffer bb = ByteBuffer.wrap(packet);
        //9 is header length , and 10 is footer length
        byte[] result = new byte[length];//NegativeArraySizeException
        bb.get(result, 9, length - 10);//IndexOutOfBoundsException
        return result;
    }

}
