package ka.masato.library.device.pasori.service;

import ka.masato.library.device.pasori.exception.CardDataDecodeErrorException;
import ka.masato.library.device.pasori.exception.IlligalCardDataException;

import java.nio.ByteBuffer;

public class CardDataDecoder {
    private String IDmString = "";
    private String PMmString = "";
    private byte[] cmdPayload;


    public CardDataDecoder() {
    }


    public CardDataDecoder loadPacket(byte[] packet) {
        cmdPayload = extractCmdPayload(packet);
        return this;
    }

    public CardDataDecoder decodeIDm() {

        if (cmdPayload == null) {
            throw new CardDataDecodeErrorException("Should not decode before load packet function.");
        }
        if (cmdPayload[0] != (byte) 0x01) {
            //TODO Runtime exception is better.
            throw new CardDataDecodeErrorException("Illigal cmd payload, can not decode IDm");
        }

        if (cmdPayload.length != 17 && cmdPayload.length != 19) {
            throw new CardDataDecodeErrorException("Can not decode IDm because data length is too short.");
        }

        ByteBuffer bb = ByteBuffer.wrap(cmdPayload);
        byte[] idm = new byte[8];
        bb.position(1);
        bb.get(idm, 0, 8);
        IDmString = convertBytes2String(idm);
        return this;
    }

    public CardDataDecoder decodePMm() {

        if (cmdPayload == null) {
            throw new CardDataDecodeErrorException("Should not decode before load packet function.");
        }
        if (cmdPayload[0] != (byte) 0x01) {
            //TODO Runtime exception is better.
            throw new CardDataDecodeErrorException("Illigal cmd payload, can not decode PMm");
        }

        if (cmdPayload.length != 17 && cmdPayload.length != 19) {
            throw new CardDataDecodeErrorException("Can not decode PMm because data length is too short.");
        }

        ByteBuffer bb = ByteBuffer.wrap(cmdPayload);
        byte[] ppm = new byte[8];
        bb.position(9);
        bb.get(ppm, 0, 8);
        PMmString = convertBytes2String(ppm);
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
        int lengthSizeByte = 1;
        if (packet.length <= 2) {// TODO please change check logic.
            throw new IlligalCardDataException("Can not load packet, because packet length illigal.");
        }
        if ((byte) packet[1] != (byte) packet.length - lengthSizeByte) {
            throw new IlligalCardDataException("Can not load packet, because packet length illigal.");
        }
        ByteBuffer bb = ByteBuffer.wrap(packet);
        //2 is header length
        byte[] result = new byte[packet.length - lengthSizeByte - 1];//NegativeArraySizeException
        //bb.get(result, 2, packet.length-lengthSizeByte-1);//IndexOutOfBoundsException
        bb.position(2);
        bb.get(result, 0, packet.length - 2);//IndexOutOfBoundsException
        //offset はバッファへの挿入位置のオフセットのこと。GETする側の読み出しオフセットではない。
        return result;
    }

}
