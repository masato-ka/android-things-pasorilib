package ka.masato.library.device.pasori;

import android.os.Handler;
import ka.masato.library.device.pasori.callback.PasoriReadCallback;
import ka.masato.library.device.pasori.exception.IlligalParameterTypeException;
import ka.masato.library.device.pasori.exception.PasoriNotInitializedException;
import ka.masato.library.device.pasori.service.CardDataDecoder;

import java.nio.ByteBuffer;

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

                if (result[0] == (byte) 0x29 && result[1] == 0x05 && result[2] == 0x80) {
                    //No IC card.
                    //41,5,-128,0,0
                } else {
                    //Get result.
                    cardDataDecoder.laodPacket(result).decodeIDm().decodePMm();
                    mCallback.pollingRecieve(cardDataDecoder.getIDm(), cardDataDecoder.getPMm());
                    pasoriHandler.postDelayed(this, periodicalMiliTime);
                }
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
        byte[] result = this.usbPasoriDriver.transferCommand(cmd.array(), cmd.array().length);
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
