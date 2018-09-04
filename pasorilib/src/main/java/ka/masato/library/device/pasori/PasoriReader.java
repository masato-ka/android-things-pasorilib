package ka.masato.library.device.pasori;

import android.hardware.usb.*;
import android.os.Handler;
import ka.masato.library.device.pasori.enums.CardType;
import ka.masato.library.device.pasori.exception.PasoriNotInitializedException;
import ka.masato.library.device.pasori.model.CardRecord;
import ka.masato.library.device.pasori.model.TypeFCardRecord;
import ka.masato.library.device.pasori.exception.FailedTransferCommandException;
import ka.masato.library.device.pasori.exception.IlligalCardTypeException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

public class PasoriReader {


    private UsbDevice rcs380 =null;
    private UsbDeviceConnection mUsbdeviceConnection = null;
    private UsbEndpoint mInUsbEndpoint = null;
    private UsbEndpoint mOutUsbEndpoint = null;

    private CardType rfCardType;
    private boolean isInitialized = false;
    private boolean isStartRead = false;
    private PasoriReader(){}


    public void startRead(Handler pasoriHandler, PasoriReadCallback mCallback){
        startRead(pasoriHandler, mCallback, 1000L);
    }

    public void startRead(Handler pasoriHandler, PasoriReadCallback mCallback, Long periodicalMiliTime) {

        if(!isInitialized){
            throw new PasoriNotInitializedException("Should be initialized pasori object before startRead");
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(!isStartRead)return;
                byte[] reulst = readCardData(rfCardType);
                //41,5,-128,0,0
                CardRecord mCardRecord = null;
                if(rfCardType == CardType.F) mCardRecord = new TypeFCardRecord();
                mCallback.getResult(mCardRecord);
                pasoriHandler.postDelayed(this,periodicalMiliTime);
            }

        };
        isStartRead = true;
        pasoriHandler.post(runnable);
    }

    public void stop() {
        isStartRead = false;
    }


    private static class HasInstance{
        private static PasoriReader mPasoriReader = new PasoriReader();
    }

    public static PasoriReader getInstance(){
        return HasInstance.mPasoriReader;
    }

    public boolean initializeDevice(UsbManager mUsbManager, CardType cardType){
        if(isInitialized){
            return false;
        }
        rfCardType = cardType;
        initUsbDevice(mUsbManager);
        resetPasoriDevice();
        setCommandType();
        setSwitchRF((byte)0x00);
        insertRF(cardType);
        packetInsertProtocolOne();
        packetInsertProtocolTwo(cardType);

        isInitialized = true;

        return true;
    }

    private void initUsbDevice(UsbManager mUsbManager) {
        HashMap<String, UsbDevice> devices = mUsbManager.getDeviceList();
        devices.forEach((key, value)-> {
            if (value.getVendorId()==1356 && value.getProductId()==1731) {
                rcs380 = value;
            }
        });

        if (rcs380 == null) {
            return;
        }
        mUsbdeviceConnection = mUsbManager.openDevice(rcs380);
        if (mUsbdeviceConnection == null) {
            return;
        }
        if (!mUsbdeviceConnection.claimInterface(rcs380.getInterface(0), true)) {
            mUsbdeviceConnection.close();
            return;
        }

        for (int index =0; index < rcs380.getInterface(0).getEndpointCount(); index++){
            UsbEndpoint mEndPoint = rcs380.getInterface(0).getEndpoint(index);

            if (mEndPoint.getDirection() == UsbConstants.USB_DIR_IN && mEndPoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                mInUsbEndpoint = mEndPoint;
            }
            if (mEndPoint.getDirection() == UsbConstants.USB_DIR_OUT && mEndPoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                mOutUsbEndpoint = mEndPoint;
            }

        }

        if(mInUsbEndpoint == null){
            return;
        }
        if(mOutUsbEndpoint == null){
            return ;
        }
    }

    private boolean resetPasoriDevice() {
        byte[] initial = {(byte)0x00,(byte)0x00,(byte)0xff,(byte)0x00,(byte)0xff,(byte)0x00};
        if (mUsbdeviceConnection.bulkTransfer(mOutUsbEndpoint, initial, initial.length, 100) != initial.length) {
            return false;
        }
        return true;
    }


    public String getFirmwareVersion(){
        byte[] firmwareVersion = {(byte)0x20};
        byte[] result = transferCommand(firmwareVersion, firmwareVersion.length);
        String version = String.format("%x.%02x", result[3], result[2]);
        return  version;
    }

    public void setCommandType(){
        byte[] commandType = {(byte)0x2A,(byte)0x01};
        transferCommand(commandType, commandType.length);
    }

    public void setSwitchRF(byte mode){
        //TODO CHECK mode
        byte[] switchRF = {(byte)0x06, mode};
        transferCommand(switchRF, switchRF.length);
    }

    public void insertRF(CardType rfType){
        ByteBuffer cmd = ByteBuffer.allocate(5);
        if (rfType == CardType.F)cmd.put(new byte[] {(byte)0x00, 0x01, 0x01, 0x0f, 0x01});
        else if (rfType == CardType.A)cmd.put(new byte[] {(byte)0x00, 0x02, 0x03, 0x0F, 0x03});
        else if (rfType == CardType.B)cmd.put(new byte[] {(byte)0x00, 0x03, 0x07, 0x0F, 0x07});
        else throw new IlligalCardTypeException("Card type accept A or B, F");
        transferCommand(cmd.array(), cmd.array().length);
    }

    public void packetInsertProtocolOne(){
        byte[] cmd = {0x02,0x00,0x18,0x01,0x01,0x02,0x01,0x03,0x00,
                0x04,0x00,0x05,0x00,0x06,0x00,0x07,0x08,0x08,
                0x00,0x09,0x00,0x0a,0x00,0x0b,0x00,0x0c,0x00,
                0x0e,0x04,0x0f,0x00,0x10,0x00,0x11,0x00,0x12,
                0x00,0x13,0x06
        };
        transferCommand(cmd, cmd.length);
    }

    public void packetInsertProtocolTwo(CardType rfType){
        ByteBuffer cmd = null;
        if(rfType == CardType.F) cmd = ByteBuffer.wrap(new byte[] {(byte)0x02,0x08,0x18});
        if(rfType == CardType.A) cmd = ByteBuffer.wrap(new byte[] {(byte)0x02,0x00,0x06,0x01,0x00,0x02,0x00,0x05,0x01,0x07,0x07});
        if(rfType == CardType.B) cmd = ByteBuffer.wrap(new byte[] {(byte)0x02,0x00,0x14,0x09,0x01,0x0a,0x01,0x0b,0x01,0x0c,0x01});
        if (cmd==null) {
            throw new IlligalCardTypeException("Card type accept A or B, F");
        }
        transferCommand(cmd.array(), cmd.array().length);
    }

    public byte[] readCardData(CardType rfType){
        ByteBuffer cmd = null;
        if(rfType == CardType.F) cmd = ByteBuffer.wrap(new byte[] {(byte)0x04,0x6e,0x00,0x06,0x00, (byte) 0xFF, (byte) 0xFF,0x01,0x00});
        if(rfType == CardType.B) cmd = ByteBuffer.wrap(new byte[] {(byte)0x04,0x6e,0x00,0x05,0x00,0x10});
        if(rfType == CardType.A) cmd = ByteBuffer.wrap(new byte[] {(byte)0x04,0x6e,0x00,0x26});
        if (cmd==null) {
            throw new IlligalCardTypeException("Card type accept A or B, F");
        }
        byte[] result = transferCommand(cmd.array(), cmd.array().length);
        return result;
    }

    private byte[] transferCommand(byte[] cmd, int cmd_length){
        byte[] temp = new byte[64];
        byte[] buf = new byte[cmd_length+11];


        buf[0] = (byte)0x0 ; buf[1] = (byte)0x0; buf[2] = (byte) 0xFF;
        buf[3] = (byte)0xFF; buf[4] = (byte)0xFF;
        buf[5] = (byte) ((byte)0xFF & (cmd_length+1));
        buf[6] = (byte) (((cmd_length+1) >> 8) & (byte) 0xFF);
        byte[] cs1 = {buf[5],buf[6]};
        buf[7] = getCheckSum(cs1, cs1.length);//5 ,6
        buf[8] = (byte) 0xD6;
        byte[] cs2 = new byte[cmd_length+1];
        cs2[0] = buf[8];
        for(int i=0; i < cmd_length; i++){
            buf[9+i] = cmd[i];
            cs2[1+i] = cmd[i];
        }
        buf[cmd_length+9] = getCheckSum( cs2, cs2.length);
        buf[cmd_length+10] = (byte)0x00;

        if (mUsbdeviceConnection.bulkTransfer(mOutUsbEndpoint, buf, buf.length, 100) != buf.length) {
            throw new FailedTransferCommandException("Failed send command.");
        }

        int retCode = mUsbdeviceConnection.bulkTransfer(mInUsbEndpoint, temp, temp.length, 100);
        if(retCode != 6){
            throw new FailedTransferCommandException("Can not get the ack command");
        }

        if (temp[0] != (byte)0x0 && temp[1] != (byte)0x0 && temp[2] != (byte)0xFF
                && temp[3] != (byte)0x0 && temp[4] != (byte)0xFF && temp[5] != (byte)0x00) {
            throw new FailedTransferCommandException("Success recv command but that is not ack packet");
        }

        retCode = mUsbdeviceConnection.bulkTransfer(mInUsbEndpoint, temp, temp.length, 100);
        byte[] resultPayload = null;
        if(temp[0] == (byte)0x00 && temp[1] == (byte)0x00 && temp[2] == (byte)0xFF){
            if (temp[3] == (byte)0xff && temp[4] == (byte)0xff) {
                byte[] rawLength = {temp[5],temp[6]};
                int length = (int)ByteBuffer.wrap(rawLength).order(ByteOrder.LITTLE_ENDIAN).getShort();
                resultPayload = new byte[length];
                for(int i=0; i < length; i++){
                    resultPayload[i] = temp[8+i];
                }
            }
        }
        if(resultPayload == null){
            throw new FailedTransferCommandException("Command result payload is null");
        }
        return resultPayload;
    }

    private byte getCheckSum(byte[] data, int length) {
        int sum = 0;
        for(int i = 0; i < length; i++){
            sum += data[i];
        }
        return (byte) (((0x100) - (sum & (byte)0xFF)) & ((byte)0xFF));
    }

}
