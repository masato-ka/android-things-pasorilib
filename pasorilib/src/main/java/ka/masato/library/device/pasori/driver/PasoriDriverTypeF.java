package ka.masato.library.device.pasori.driver;

import android.os.Handler;
import ka.masato.library.device.pasori.callback.PasoriReadCallback;
import ka.masato.library.device.pasori.exception.IlligalParameterTypeException;
import ka.masato.library.device.pasori.exception.PasoriNotInitializedException;
import ka.masato.library.device.pasori.service.CardDataDecoder;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class PasoriDriverTypeF extends AbstractPasoriDriver {

    private boolean isStartRead;
    private final CardDataDecoder cardDataDecoder;

    public PasoriDriverTypeF(UsbPasoriDriver usbPasoriDriver, CardDataDecoder cardDataDecoder) {
        super(usbPasoriDriver);
        this.cardDataDecoder = cardDataDecoder;
    }

    @Override
    void insertRF() {
        ByteBuffer cmd = ByteBuffer.allocate(5);
        cmd.put(new byte[]{(byte) 0x00, 0x01, 0x01, 0x0f, 0x01});
        this.usbPasoriDriver.transferCommand(cmd.array(), cmd.array().length);
    }

    @Override
    void packetInsertProtocolTwo() {
        ByteBuffer cmd = null;
        cmd = ByteBuffer.wrap(new byte[]{(byte) 0x02, 0x08, 0x18});
        this.usbPasoriDriver.transferCommand(cmd.array(), cmd.array().length);

    }


    public void startPolling(Handler pasoriHandler, PasoriReadCallback mCallback) {
        startPolling(pasoriHandler, mCallback, 1000L);
    }

    public void startPolling(Handler pasoriHandler, PasoriReadCallback mCallback, Long periodicalMiliTime) {

        if (!isInitialized) {
            throw new PasoriNotInitializedException("Should be initialized pasori object before startRead");
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!isStartRead) return;

                byte[] result = pollingNFC(110);

                if (result.length <= 0) {
                } else {
                    //Get result.
                    cardDataDecoder.loadPacket(result).decodeIDm().decodePMm();
                    mCallback.pollingRecieve(cardDataDecoder.getIDm(), cardDataDecoder.getPMm());
                }
                pasoriHandler.postDelayed(this, periodicalMiliTime);

            }

        };
        isStartRead = true;
        pasoriHandler.post(runnable);
    }

    public void stopPolling() {
        isStartRead = false;
    }


    public byte[] pollingNFC(int timeout) {
        ByteBuffer cmd = null;
        cmd = buildRfCommand(new byte[]{0x00, (byte) 0xFF, (byte) 0xFF, 0x01, 0x00}, timeout);
        //if (rfType == CardType.B) cmd = buildRfCommand(new byte[]{(byte) 0x00, 0x10}, timeout);
        //if(rfType == CardType.A) cmd = buildRfCommand(new byte[] {(byte)0x04,0x6e,0x00,0x26}, timeout);
        if (cmd == null) {
            throw new IlligalParameterTypeException("Card type accept A or B, F");
        }
        byte[] getPayload = this.usbPasoriDriver.transferCommand(cmd.array(), cmd.array().length);
        byte[] result = extractRfCommand(getPayload);
        return result;
    }

    public byte[] requestService(byte[] idm, byte[] nodeList, int timeout) {
        //TODO nodeList and result nodeKeyList should be match order.
        if ((nodeList.length % 2) != 0) {
            throw new IlligalParameterTypeException("Must be node code list length is even number.");
        }
        if (2 > nodeList.length || nodeList.length > 64) {
            throw new IlligalParameterTypeException("Must be node code list length is 2 to 64 and even number.");
        }
        if (idm.length != 8) {
            throw new IlligalParameterTypeException("Must be idm length is just 8 byte but idm length is " + idm.length);
        }

        ByteBuffer rfcmd = ByteBuffer.allocate(1 + 8 + 1 + nodeList.length);
        rfcmd.put((byte) 0x02);
        rfcmd.put(idm);
        rfcmd.put((byte) ((byte) nodeList.length / 2));
        rfcmd.put(nodeList);
        ByteBuffer cmd = buildRfCommand(rfcmd.array(), timeout);

        if (cmd == null) {
            throw new IlligalParameterTypeException("Failed create cmd payload on send RequestService.");
        }

        byte[] resultPayload = this.usbPasoriDriver.transferCommand(cmd.array(), cmd.array().length);
        byte[] result = extractRfCommand(resultPayload);
        return result;
    }

    public byte[] requestResponse(byte[] idm, int timeout) {

        if (idm.length != 8) {
            throw new IlligalParameterTypeException("Must be idm length is just 8 byte but idm length is " + idm.length);
        }

        ByteBuffer rfcmd = ByteBuffer.allocate(1 + 8);
        rfcmd.put((byte) 0x04);
        rfcmd.put(idm);
        ByteBuffer cmd = buildRfCommand(rfcmd.array(), timeout);
        if (cmd == null) {
            throw new IlligalParameterTypeException("Failed create cmd payload on send RequestResponse.");
        }

        byte[] resultPayload = this.usbPasoriDriver.transferCommand(cmd.array(), cmd.array().length);
        byte[] result = extractRfCommand(resultPayload);
        return result;
    }

    public byte[] readWithoutEncryption(byte[] idm, byte[] serviceCodeList, byte[] blockList, int blockNumber, int timeout) {

        if (2 > serviceCodeList.length || serviceCodeList.length > 32) {
            throw new IlligalParameterTypeException("Must be service code list length is 2 to 32 but length is "
                    + serviceCodeList.length);
        }

        if ((serviceCodeList.length % 2) != 0) {
            throw new IlligalParameterTypeException("Must be service code list length is Even but length is "
                    + serviceCodeList.length);
        }

        if (blockNumber != calcNumberofBlock(blockList)) {
            throw new IlligalParameterTypeException("Wrong block number. you specific : " + blockNumber);
        }


        if ((2 * blockNumber) > blockList.length && blockList.length < (3 * blockNumber)) {
            throw new IlligalParameterTypeException(" Illigal block list length. check block number or block list. ");
        }

        if (idm.length != 8) {
            throw new IlligalParameterTypeException("Must be idm Length is just 8 byte but idm length is " + idm.length);
        }

        ByteBuffer rfcmd = ByteBuffer.allocate(1 + 8 + 1 + serviceCodeList.length + 1 + blockList.length);
        rfcmd.put((byte) 0x06);
        rfcmd.put(idm);
        rfcmd.put((byte) ((byte) serviceCodeList.length / 2));
        rfcmd.put(serviceCodeList);
        rfcmd.put((byte) blockNumber);
        rfcmd.put(blockList);
        ByteBuffer cmd = buildRfCommand(rfcmd.array(), timeout);

        if (cmd == null) {
            throw new IlligalParameterTypeException("Failed create cmd payload on send readWithoutEncryption.");
        }

        byte[] resultPayload = this.usbPasoriDriver.transferCommand(cmd.array(), cmd.array().length);
        byte[] result = extractRfCommand(resultPayload);
        return result;

    }

    public byte[] writeWithoutEncryption(byte[] idm, byte[] serviceCodeList,
                                         byte[] blockList, int blockNumber, byte[] blockData, int timeout) {

        if (2 > serviceCodeList.length || serviceCodeList.length > 32) {
            throw new IlligalParameterTypeException("Must be service code list length is 2 to 32 but length is "
                    + serviceCodeList.length);
        }

        if ((serviceCodeList.length % 2) != 0) {
            throw new IlligalParameterTypeException("Must be service code list length is Even but length is "
                    + serviceCodeList.length);
        }

        if (blockNumber != calcNumberofBlock(blockList)) {
            throw new IlligalParameterTypeException("Wrong block number. you specific : " + blockNumber);
        }


        if ((2 * blockNumber) > blockList.length && blockList.length < (3 * blockNumber)) {
            throw new IlligalParameterTypeException(" Illigal block list length. check block number or block list. ");
        }

        int blockDataSize = blockNumber * 16;
        if (blockData.length != blockDataSize) {
            throw new IlligalParameterTypeException("Illigal block data size, expect size is " + blockDataSize);
        }

        if (idm.length != 8) {
            throw new IlligalParameterTypeException("Must be idm Length is just 8 byte but idm length is " + idm.length);
        }

        ByteBuffer rfcmd = ByteBuffer.allocate(1 + 8 + 1 + serviceCodeList.length
                + 1 + blockList.length + (blockNumber * 16));
        rfcmd.put((byte) 0x08);
        rfcmd.put(idm);
        rfcmd.put((byte) ((byte) serviceCodeList.length / 2));
        rfcmd.put(serviceCodeList);
        rfcmd.put((byte) blockNumber);
        rfcmd.put(blockList);
        rfcmd.put(blockData);
        ByteBuffer cmd = buildRfCommand(rfcmd.array(), timeout);

        if (cmd == null) {
            throw new IlligalParameterTypeException("Failed create cmd payload on send writeWithoutEncryption.");
        }

        byte[] resultPayload = this.usbPasoriDriver.transferCommand(cmd.array(), cmd.array().length);
        byte[] result = extractRfCommand(resultPayload);
        return result;
    }

    private int calcNumberofBlock(byte[] blockList) {

        ByteBuffer bf = ByteBuffer.wrap(blockList);
        int length = bf.limit();
        int position = 0;
        int count = 0;
        while (position < length) {
            bf.position(position);
            byte d = bf.get();
            if ((d & 0x80) == 0x80) {
                position += 2;

            } else {
                position += 3;
            }
            count++;
        }
        if (position != length) {
            throw new IlligalParameterTypeException("Illigal block list byte length.");
        }
        return count;
    }

    private byte[] extractRfCommand(byte[] resultPayload) {
        byte[] expect = {(byte) 0xD7, (byte) 0x04 + 1};
        ByteBuffer byteBuffer = ByteBuffer.wrap(resultPayload, 0, 5);
        byte[] header1 = new byte[2];
        byteBuffer.get(header1, 0, 2);
        if (!Arrays.equals(header1, expect)) {
            throw new FailedRfCommunication("RF command Response header is illigal.");
        }
        byteBuffer.position(2);
        byte[] header2 = new byte[3];
        byteBuffer.get(header2, 0, 3);
        if (!Arrays.equals(header2, new byte[]{0x00, 0x00, 0x00})) {
            return new byte[0];
        }
        byteBuffer.clear();
        byteBuffer.position(5);
        byte[] result = new byte[resultPayload.length - 5];
        byteBuffer.get(result, 0, resultPayload.length - 5);

        return result;
    }


    private ByteBuffer buildRfCommand(byte[] nfcCommand, int timeout) {
        ByteBuffer buildResult = null;

        //devicecomm , timeout(2), (data_length+1), nfc_command(5)
        byte[] header = new byte[4];
        timeout = timeout >= 65535 ? 65535 : timeout;
        timeout = timeout <= 0 ? 0 : timeout;
        header[0] = (byte) 0x04;
        header[1] = (byte) ((byte) 0xFF & (timeout));
        header[2] = (byte) (((timeout) >> 8) & (byte) 0xFF);

        int length_size = 1;
        header[3] = (byte) ((byte) 0xFF & (nfcCommand.length + length_size));
        buildResult = ByteBuffer.allocate(nfcCommand.length + 4);
        buildResult.put(header);
        buildResult.put(nfcCommand);

        return buildResult;
    }

}
