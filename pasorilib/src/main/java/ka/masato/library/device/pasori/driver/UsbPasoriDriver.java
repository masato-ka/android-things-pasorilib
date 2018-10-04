package ka.masato.library.device.pasori.driver;

import android.hardware.usb.*;
import ka.masato.library.device.pasori.exception.FailedTransferCommandException;
import ka.masato.library.device.pasori.exception.PasoriDeviceNotFoundException;
import ka.masato.library.device.pasori.exception.PasoriFailedInitializedException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

public class UsbPasoriDriver {


    private UsbDevice rcs380 =null;
    private UsbDeviceConnection mUsbdeviceConnection = null;
    private UsbEndpoint mInUsbEndpoint = null;
    private UsbEndpoint mOutUsbEndpoint = null;

    private boolean isInitialized = false;

    private UsbPasoriDriver() {
    }


    private static class HasInstance{
        private static UsbPasoriDriver mUsbPasoriDriver = new UsbPasoriDriver();
    }

    public static UsbPasoriDriver getInstance() {
        return HasInstance.mUsbPasoriDriver;
    }

    public void initUsbDevice(UsbManager mUsbManager) throws PasoriDeviceNotFoundException {
        if (isInitialized) return;
        HashMap<String, UsbDevice> devices = mUsbManager.getDeviceList();
        devices.forEach((key, value)-> {
            if (value.getVendorId()==1356 && value.getProductId()==1731) {
                rcs380 = value;
            }
        });

        if (rcs380 == null) {
            throw new PasoriDeviceNotFoundException("Not found pasori card reader on your device USB port.");
        }
        mUsbdeviceConnection = mUsbManager.openDevice(rcs380);
        if (mUsbdeviceConnection == null) {
            throw new PasoriFailedInitializedException("Failed open your pasori card reader.");
        }
        if (!mUsbdeviceConnection.claimInterface(rcs380.getInterface(0), true)) {
            mUsbdeviceConnection.close();
            throw new PasoriFailedInitializedException("Can not get pasori interface.");
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

        if (mInUsbEndpoint == null || mOutUsbEndpoint == null) {
            throw new PasoriDeviceNotFoundException("Can not get usb endpoint.");
        }
        isInitialized = true;
    }

    public boolean bulkTransferSend(byte[] data, int length, int timeout) {
        if (!isInitialized) {
            throw new FailedTransferCommandException("Can not bulkTransfer before initialized");
        }
        if (mUsbdeviceConnection.bulkTransfer(mOutUsbEndpoint, data, length, timeout) != length) {
            return false;
        }
        return true;
    }

    public int bulkTransferRecv(byte[] buffer, int length, int timeout) {
        if (!isInitialized) {
            throw new FailedTransferCommandException("Can not bulkTransfer before initialized.");
        }
        int retCode = mUsbdeviceConnection.bulkTransfer(mInUsbEndpoint, buffer, length, timeout);
        return retCode;
    }

    public byte[] transferCommand(byte[] cmd, int cmd_length) {
        byte[] temp = new byte[64];
        byte[] buf = buildUsbTransferMessage(cmd, cmd_length);

        //send command
        if (!bulkTransferSend(buf, buf.length, 100)) {
            throw new FailedTransferCommandException("Failed transfer command.");
        }

        //ack
        int retCode = bulkTransferRecv(temp, temp.length, 100);
        if(retCode != 6){
            throw new FailedTransferCommandException("Can not get the ack command");
        }
        if (temp[0] != (byte)0x0 && temp[1] != (byte)0x0 && temp[2] != (byte)0xFF
                && temp[3] != (byte)0x0 && temp[4] != (byte)0xFF && temp[5] != (byte)0x00) {
            throw new FailedTransferCommandException("Success recv command but that is not ack packet");
        }

        //get response
        bulkTransferRecv(temp, temp.length, 100);

        byte[] resultPayload = extractPasoriCommand(temp);

        return resultPayload;
    }

    private byte[] extractPasoriCommand(byte[] temp) {
        byte[] resultPayload = null;
        if (temp[0] == (byte) 0x00 && temp[1] == (byte) 0x00 && temp[2] == (byte) 0xFF) {
            if (temp[3] == (byte) 0xff && temp[4] == (byte) 0xff) {

                byte[] rawLength = {temp[5], temp[6]};
                int length = (int) ByteBuffer.wrap(rawLength).order(ByteOrder.LITTLE_ENDIAN).getShort();

                resultPayload = new byte[length];
                for (int i = 0; i < length; i++) {
                    resultPayload[i] = temp[8 + i];
                }
            }
        }
        if (resultPayload == null) {
            throw new FailedTransferCommandException("Command result payload is null");
        }
        return resultPayload;
    }

    private byte[] buildUsbTransferMessage(byte[] pasoriCmd, int length) {
        int headerSize = 11;
        byte[] buf = new byte[length + headerSize];
        buf[0] = (byte) 0x0;
        buf[1] = (byte) 0x0;
        buf[2] = (byte) 0xFF;
        buf[3] = (byte) 0xFF;
        buf[4] = (byte) 0xFF;
        buf[5] = (byte) ((byte) 0xFF & (length + 1));
        buf[6] = (byte) (((length + 1) >> 8) & (byte) 0xFF);
        byte[] cs1 = {buf[5], buf[6]};
        buf[7] = getCheckSum(cs1, cs1.length);//5 ,6
        buf[8] = (byte) 0xD6;
        byte[] cs2 = new byte[length + 1];
        cs2[0] = buf[8];
        for (int i = 0; i < length; i++) {
            buf[9 + i] = pasoriCmd[i];
            cs2[1 + i] = pasoriCmd[i];
        }
        buf[length + 9] = getCheckSum(cs2, cs2.length);
        buf[length + 10] = (byte) 0x00;
        return buf;
    }

    private byte getCheckSum(byte[] data, int length) {
        int sum = 0;
        for(int i = 0; i < length; i++){
            sum += data[i];
        }
        return (byte) (((0x100) - (sum & (byte)0xFF)) & ((byte)0xFF));
    }

}
