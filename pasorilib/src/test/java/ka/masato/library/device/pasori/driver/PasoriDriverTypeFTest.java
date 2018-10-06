package ka.masato.library.device.pasori.driver;

import ka.masato.library.device.pasori.exception.IlligalParameterTypeException;
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
        String expect = "RF command Response header is illigal.";
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
        String expect = "RF command Response header is illigal.";
        try {
            byte[] result = target.pollingNFC(110);
            fail();
        } catch (FailedRfCommunication e) {
            assertThat(e.getMessage(), is(expect));
        }
    }

    @Test
    public void requestService01() {
        byte[] testIDm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] nodeList = {0x00, 0x00};

        byte[] mockExpectValue = {0x04, 0x6E, 0x00, 0x0D,// header
                0x02,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x01, //node number
                0x00, 0x00};//node code list
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x03,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x01, //number of node
                0x00, 0x00};// node key version list.
        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x03,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x01, //number of node
                0x00, 0x00};// node key version list.}

        byte[] result = target.requestService(testIDm, nodeList, 110);
        assertTrue(Arrays.equals(result, expect));
    }


    /**
     * Overflow timeout value.
     */
    @Test
    public void requestService02() {
        byte[] testIDm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] nodeList = {0x00, 0x00};

        byte[] mockExpectValue = {0x04, (byte) 0xFF, (byte) 0xFF, 0x0D,// header
                0x02,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x01, //node number
                0x00, 0x00};//node code list
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x03,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x01, //number of node
                0x00, 0x00};// node key version list.
        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x03,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x01, //number of node
                0x00, 0x00};// node key version list.}

        byte[] result = target.requestService(testIDm, nodeList, 100000);
        assertTrue(Arrays.equals(result, expect));
    }

    /**
     * Max length of node code list.
     */
    @Test
    public void requestService03() {
        byte[] testIDm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] nodeList = new byte[64];
        for (int i = 0; i < 64; i++) {
            nodeList[i] = 0x00;
        }

        byte[] mockExpectValue = {0x04, (byte) 0x6E, (byte) 0x00, 0x4B,// header
                0x02,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x20, //node number
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};//node code list
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x03,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x20, //number of node
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};// node key version list.
        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x03,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x20, //number of node
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};// node key version list.

        byte[] result = target.requestService(testIDm, nodeList, 110);
        assertTrue(Arrays.equals(result, expect));
    }

    /**
     * IDM length is short.
     **/
    @Test
    public void AbnormalrequestService01() {
        byte[] testIDm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] nodeList = {0x00, 0x00};


        byte[] mockExpectValue = {0x04, 0x6E, 0x00, 0x0B,// header
                0x02,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x01, //node number
                0x00, 0x00};//node code list
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x03,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //IDM
                0x01, //number of node
                0x00, 0x00};// node key version list.
        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        String expect = "Must be idm length is just 8 byte but idm length is 6";

        try {
            target.requestService(testIDm, nodeList, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }
    }

    /**
     * IDM length is long
     */
    @Test
    public void AbnormalrequestService02() {
        byte[] testIDm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] nodeList = {0x00, 0x00};

        byte[] mockExpectValue = {0x04, 0x6E, 0x00, 0x0E,// header
                0x02,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x01, //node number
                0x00, 0x00};//node code list
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x03,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x01, //number of node
                0x00, 0x00};// node key version list.
        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        String expect = "Must be idm length is just 8 byte but idm length is 9";

        try {
            target.requestService(testIDm, nodeList, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }
    }

    /**
     * nodeList is odd size
     */
    @Test
    public void AbnormalrequestService03() {
        byte[] testIDm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] nodeList = {0x00, 0x00, 0x00};

        String expect = "Must be node code list length is even number.";

        try {
            target.requestService(testIDm, nodeList, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }
    }

    /**
     * nodeList size is blank
     */
    @Test
    public void AbnormalrequestService04() {
        byte[] testIDm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] nodeList = {};

        String expect = "Must be node code list length is 2 to 64 and even number.";

        try {
            target.requestService(testIDm, nodeList, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }
    }

    /**
     * nodeList size is over 64
     */
    @Test
    public void AbnormalrequestService05() {
        byte[] testIDm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] nodeList = new byte[66];

        for (int i = 0; i < 66; i++) {
            nodeList[i] = 0x00;
        }

        String expect = "Must be node code list length is 2 to 64 and even number.";

        try {
            target.requestService(testIDm, nodeList, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }
    }

    @Test
    public void request() {
    }

    @Test
    public void readWithoutEncription() {
    }
}