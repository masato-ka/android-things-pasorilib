package ka.masato.library.device.pasori.driver;

import ka.masato.library.device.pasori.service.CardDataDecoder;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PasoriDriverTypeFTest {


    UsbPasoriDriver usbPasoriDriver;
    PasoriDriverTypeF target;

    @Before()
    public void setup() {
        usbPasoriDriver = mock(UsbPasoriDriver.class);
        target = new PasoriDriverTypeF(usbPasoriDriver, new CardDataDecoder());
    }


    //TOOD change expect data aligne to specification.
    @Test
    public void pollingNFCTest01() {
        when(usbPasoriDriver.transferCommand(new byte[]{0x04, 0x6E, 0x00, 0x06, 0x00,
                (byte) 0xFF, (byte) 0xFF, 0x01, 0x00}, 9))
                .thenReturn(new byte[]{(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        byte[] result = target.pollingNFC(110);
        byte[] expect = {0x00, 0x00, 0x00, 0x00};
        assertTrue(Arrays.equals(result, expect));
    }

    @Test
    public void pollingNFCTest02() {
        when(usbPasoriDriver.transferCommand(new byte[]{0x04, 0x6E, 0x00, 0x06, 0x00,
                (byte) 0xFF, (byte) 0xFF, 0x01, 0x00}, 9))
                .thenReturn(new byte[]{(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00});
        byte[] result = target.pollingNFC(110);
        assertTrue(Arrays.equals(result, new byte[]{}));
    }

    @Test
    public void AbnormalpollingNFCTest01() {
        when(usbPasoriDriver.transferCommand(new byte[]{0x04, 0x6E, 0x00, 0x06, 0x00,
                (byte) 0xFF, (byte) 0xFF, 0x01, 0x00}, 9))
                .thenReturn(new byte[]{(byte) 0x00, (byte) 0x04 + 1, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        String expect = "RF command Response header is illigal :[B@62043840";
        try {
            byte[] result = target.pollingNFC(110);
            fail();
        } catch (FailedRfCommunication e) {
            assertThat(e.getMessage(), is(expect));
        }
    }


    @Test
    public void AbnormalpollingNFCTest02() {
        when(usbPasoriDriver.transferCommand(new byte[]{0x04, 0x6E, 0x00, 0x06, 0x00,
                (byte) 0xFF, (byte) 0xFF, 0x01, 0x00}, 9))
                .thenReturn(new byte[]{(byte) 0x00, (byte) 0x04 + 1, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        String expect = "RF command Response header is illigal :[B@4157f54e";
        try {
            byte[] result = target.pollingNFC(110);
            fail();
        } catch (FailedRfCommunication e) {
            assertThat(e.getMessage(), is(expect));
        }
    }


    @Test
    public void requestService() {
    }

    @Test
    public void requestResponse01() {
        byte[] loadPacket = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] expectSendData = {0x04, 0x6E, 0x00, 0x06,// header
                0x01,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x01, //node number
                0x00, 0x00};//node code list
        byte[] mockReturn = {(byte) 0x7D, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x03,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x01, //number of node
                0x00, 0x00};// node key version list.
        when(usbPasoriDriver.transferCommand(expectSendData, expectSendData.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x03,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x01, //number of node
                0x00, 0x00};// node key version list.}

        byte[] result = target.requestResponse(loadPacket, 110);
        assertTrue(Arrays.equals(result, expect));
    }

    @Test
    public void AbnormalrequestResponse01() {
        byte[] loadPacket = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] expectSendData = {0x04, 0x6E, 0x00, 0x06,// header
                0x01,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x01, //node number
                0x00, 0x00};//node code list
        byte[] mockReturn = {(byte) 0x7D, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x03,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x01, //number of node
                0x00, 0x00};// node key version list.
        when(usbPasoriDriver.transferCommand(expectSendData, expectSendData.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x03,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x01, //number of node
                0x00, 0x00};// node key version list.}

        byte[] result = target.requestResponse(loadPacket, 110);
        assertTrue(Arrays.equals(result, expect));
    }

    @Test
    public void AbnormalrequestResponse02() {
        byte[] loadPacket = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] expectSendData = {0x04, 0x6E, 0x00, 0x06,// header
                0x01,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x01, //node number
                0x00, 0x00};//node code list
        byte[] mockReturn = {(byte) 0x7D, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x03,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x01, //number of node
                0x00, 0x00};// node key version list.
        when(usbPasoriDriver.transferCommand(expectSendData, expectSendData.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x03,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM˚
                0x01, //number of node
                0x00, 0x00};// node key version list.}

        byte[] result = target.requestResponse(loadPacket, 110);
        assertTrue(Arrays.equals(result, expect));
    }

    @Test
    public void readWithoutEncription() {
    }
}