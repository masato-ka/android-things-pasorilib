package ka.masato.library.device.pasori.driver;

import ka.masato.library.device.pasori.exception.FailedTransferCommandException;
import ka.masato.library.device.pasori.exception.IlligalParameterTypeException;
import ka.masato.library.device.pasori.exception.PasoriDeviceNotFoundException;


public abstract class AbstractPasoriDriver {
    public final UsbPasoriDriver usbPasoriDriver;
    protected boolean isInitialized;

    public AbstractPasoriDriver(UsbPasoriDriver usbPasoriDriver1) {
        this.usbPasoriDriver = usbPasoriDriver1;
    }

    public boolean initializeDevice() throws PasoriDeviceNotFoundException {
        if (isInitialized) {
            return false;
        }
        resetPasoriDevice();
        setCommandType();
        setSwitchRF((byte) 0x00);
        insertRF();
        packetInsertProtocolOne();
        packetInsertProtocolTwo();

        isInitialized = true;

        return true;
    }

    private void resetPasoriDevice() {
        byte[] initial = {(byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00};
        if (!this.usbPasoriDriver.bulkTransferSend(initial, initial.length, 100)) {
            throw new FailedTransferCommandException("Failed transfer reset command.");
        }
    }

    public String getFirmwareVersion() {
        byte[] firmwareVersion = {(byte) 0x20};
        byte[] result = this.usbPasoriDriver.transferCommand(firmwareVersion, firmwareVersion.length);
        String version = String.format("%x.%02x", result[3], result[2]);
        return version;
    }

    private void setCommandType() {
        byte[] commandType = {(byte) 0x2A, (byte) 0x01};
        this.usbPasoriDriver.transferCommand(commandType, commandType.length);
    }

    private void setSwitchRF(byte mode) {
        //TODO CHECK mode
        if (mode != (byte) 0x00 && mode != (byte) 0x01) {
            throw new IlligalParameterTypeException("setSwitch parameter must be 0x00 or 0x01");
        }
        byte[] switchRF = {(byte) 0x06, mode};
        this.usbPasoriDriver.transferCommand(switchRF, switchRF.length);
    }

    abstract void insertRF();

    /*    public void insertRF(){
            ByteBuffer cmd = ByteBuffer.allocate(5);
            if (rfType == CardType.F)cmd.put(new byte[] {(byte)0x00, 0x01, 0x01, 0x0f, 0x01});
            else if (rfType == CardType.A)cmd.put(new byte[] {(byte)0x00, 0x02, 0x03, 0x0F, 0x03});
            else if (rfType == CardType.B)cmd.put(new byte[] {(byte)0x00, 0x03, 0x07, 0x0F, 0x07});
            else throw new IlligalParameterTypeException("Card type accept A or B, F");
            this.usbPasoriDriver.transferCommand(cmd.array(), cmd.array().length);
        }
    */
    public void packetInsertProtocolOne() {
        byte[] cmd = {0x02, 0x00, 0x18, 0x01, 0x01, 0x02, 0x01, 0x03, 0x00,
                0x04, 0x00, 0x05, 0x00, 0x06, 0x00, 0x07, 0x08, 0x08,
                0x00, 0x09, 0x00, 0x0a, 0x00, 0x0b, 0x00, 0x0c, 0x00,
                0x0e, 0x04, 0x0f, 0x00, 0x10, 0x00, 0x11, 0x00, 0x12,
                0x00, 0x13, 0x06
        };
        this.usbPasoriDriver.transferCommand(cmd, cmd.length);
    }

    abstract void packetInsertProtocolTwo();

/*    public void packetInsertProtocolTwo(){
        ByteBuffer cmd = null;
        if(rfType == CardType.F) cmd = ByteBuffer.wrap(new byte[] {(byte)0x02,0x08,0x18});
        if(rfType == CardType.A) cmd = ByteBuffer.wrap(new byte[] {(byte)0x02,0x00,0x06,0x01,0x00,0x02,0x00,0x05,0x01,0x07,0x07});
        if(rfType == CardType.B) cmd = ByteBuffer.wrap(new byte[] {(byte)0x02,0x00,0x14,0x09,0x01,0x0a,0x01,0x0b,0x01,0x0c,0x01});
        if (cmd==null) {
            throw new IlligalParameterTypeException("Card type accept A or B, F");
        }
        this.usbPasoriDriver.transferCommand(cmd.array(), cmd.array().length);
    }*/
}
