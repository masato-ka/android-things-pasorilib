package ka.masato.library.device.pasori;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import ka.masato.library.device.pasori.callback.PasoriReadCallback;
import ka.masato.library.device.pasori.driver.PasoriDriverTypeF;
import ka.masato.library.device.pasori.driver.UsbPasoriDriver;
import ka.masato.library.device.pasori.exception.PasoriDeviceNotFoundException;
import ka.masato.library.device.pasori.service.CardDataDecoder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PasoriInstrumentedTest {

    boolean status = true;
    String stringIdm;

    private static UsbPasoriDriver usbPasoriDriver;
    private static PasoriDriverTypeF pasoriDriverTypeF;

    @BeforeClass
    public static void setupPasoriReadWriter() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        CardDataDecoder cardDataDecoder = new CardDataDecoder();
        UsbManager mUsbManager = (UsbManager) appContext.getSystemService(Context.USB_SERVICE);
        usbPasoriDriver = UsbPasoriDriver.getInstance();
        try {
            usbPasoriDriver.initUsbDevice(mUsbManager);
        } catch (PasoriDeviceNotFoundException e) {
            e.printStackTrace();
        }

        pasoriDriverTypeF = new PasoriDriverTypeF(usbPasoriDriver, cardDataDecoder);
        try {
            pasoriDriverTypeF.initializeDevice();
        } catch (PasoriDeviceNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUp() {
        status = true;
        stringIdm = "";
    }

    @Test
    public void pollingTest() {

        PasoriReadCallback pasoriReadCallback = new PasoriReadCallback() {
            @Override
            public void pollingRecieve(String idmString, String pmmString) {
                Log.i("InstrumentedTest", "IDM: " + idmString + " PMm: " + pmmString);
                status = false;
            }
        };

        HandlerThread handlerThread = new HandlerThread("polling");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        Log.i("InstrumentedTest", "Please touch your IC card on Pasori.");
        pasoriDriverTypeF.startPolling(handler, pasoriReadCallback);
        waitingPollingResult();
        pasoriDriverTypeF.stopPolling();
    }

    @Test
    public void RequestResponseTest() {
        PasoriReadCallback pasoriReadCallback = getPasoriReadCallback();

        HandlerThread handlerThread = new HandlerThread("polling");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        Log.i("InstrumentedTest", "Please touch your IC card on Pasori.");
        pasoriDriverTypeF.startPolling(handler, pasoriReadCallback);
        waitingPollingResult();
        pasoriDriverTypeF.stopPolling();
        byte[] idm = hex2bin(stringIdm);
        byte[] result = pasoriDriverTypeF.requestResponse(idm, 110);

        Log.i("InstrumentedTest", "Result:" + result.length);

    }

    @Test
    public void ReadWithoutEncriptionTest() {
        PasoriReadCallback pasoriReadCallback = getPasoriReadCallback();

        HandlerThread handlerThread = new HandlerThread("polling");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        Log.i("InstrumentedTest", "Please touch your IC card on Pasori.");

        pasoriDriverTypeF.startPolling(handler, pasoriReadCallback);

        waitingPollingResult();

        pasoriDriverTypeF.stopPolling();

        byte[] idm = hex2bin(stringIdm);
        byte[] serviceCodeList = {0x0F, 0x09}; //bigendian?
        byte[] blockList = {(byte) 0x80, (byte) 0x00}; //read block 0 (latest record of suica)
        int numberOfBlock = 1;
        //Read info that is suica ic card latest recoard.
        byte[] result = pasoriDriverTypeF.readWithoutEncryption(idm, serviceCodeList, blockList, numberOfBlock, 110);
        // If you decode suica reacoard please see in http://raspberry.mcoapps.com/archives/128.
        Log.i("InstrumentedTest", "Result:" + convertBytes2String(result));

    }

    private PasoriReadCallback getPasoriReadCallback() {
        return new PasoriReadCallback() {
            @Override
            public void pollingRecieve(String idmString, String pmmString) {
                Log.i("InstrumentedTest", "IDM: " + idmString + " PMm: " + pmmString);
                stringIdm = idmString;
                status = false;
            }
        };
    }


    private void waitingPollingResult() {
        while (status) {
            try {
                Log.i("InstrumentedTest", "Loop");
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private byte[] hex2bin(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int index = 0; index < bytes.length; index++) {
            bytes[index] = (byte) Integer.parseInt(hex.substring(index * 2, (index + 1) * 2), 16);
        }
        return bytes;
    }

    private String convertBytes2String(byte[] bytes) {
        //TODO please use data type converter.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02X", bytes[i]));
        }
        return sb.toString();
    }
}
