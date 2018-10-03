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
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        UsbManager mUsbManager = (UsbManager) appContext.getSystemService(Context.USB_SERVICE);
        UsbPasoriDriver usbPasoriDriver = UsbPasoriDriver.getInstance();
        try {
            usbPasoriDriver.initUsbDevice(mUsbManager);
        } catch (PasoriDeviceNotFoundException e) {
            e.printStackTrace();
        }

        PasoriDriverTypeF pasoriDriverTypeF = new PasoriDriverTypeF(usbPasoriDriver, new CardDataDecoder());
        try {
            pasoriDriverTypeF.initializeDevice();
        } catch (PasoriDeviceNotFoundException e) {
            e.printStackTrace();
        }


        PasoriReadCallback pasoriReadCallback = new PasoriReadCallback() {
            @Override
            public void pollingRecieve(String idmString, String pmmString) {

                Log.i("InstrumentedTest", "IDM: " + idmString + " PMm: " + pmmString);

            }
        };

        HandlerThread handlerThread = new HandlerThread("polling");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());

        pasoriDriverTypeF.startPolling(handler, pasoriReadCallback);
        while (true) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //assertEquals("ka.masato.library.device.pasori", appContext.getPackageName());
    }
}
