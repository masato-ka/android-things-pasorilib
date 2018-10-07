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

    //TODO please test emulate when No card.


    /**
     * Normal
     */
    @Test
    public void requestResponse01() {
        byte[] testIDm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        byte[] mockExpectValue = {0x04, 0x6E, 0x00, 0x0A,// header
                0x04,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};//IDM
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x05,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00}; //Mode.

        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x05,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00};
        byte[] result = target.requestResponse(testIDm, 110);
        assertTrue(Arrays.equals(result, expect));
    }

    /**
     * Over max size of timeout
     */
    @Test
    public void requestResponse02() {
        byte[] testIDm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        byte[] mockExpectValue = {0x04, (byte) 0xFF, (byte) 0xFF, 0x0A,// header
                0x04,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};//IDM
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x05,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00}; //Mode.

        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x05,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00};
        byte[] result = target.requestResponse(testIDm, 1100000000);
        assertTrue(Arrays.equals(result, expect));
    }

    /**
     * IDm length size is short
     */
    @Test
    public void AbnormalrequestResponse01() {
        byte[] testIDm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        String expect = "Must be idm length is just 8 byte but idm length is 7";
        try {
            byte[] result = target.requestResponse(testIDm, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }
    }

    /**
     * IDm length size is long
     */
    @Test
    public void AbnormalrequestResponse02() {
        byte[] testIDm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        String expect = "Must be idm length is just 8 byte but idm length is 9";
        try {
            byte[] result = target.requestResponse(testIDm, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }
    }

    /**
     * IDm length size is blank
     */
    @Test
    public void AbnormalrequestResponse03() {
        byte[] testIDm = {};
        String expect = "Must be idm length is just 8 byte but idm length is 0";
        try {
            byte[] result = target.requestResponse(testIDm, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }
    }


    /**
     * Normal
     */
    @Test
    public void readWithoutEncryption01() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;

        byte[] mockExpectValue = {0x04, 0x6E, 0x00, 0x15,// header
                0x06,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                (byte) ((byte) testServiceCodeList.length / 2),// service code number
                0x00, 0x00, 0x00, 0x00, // service code list
                (byte) testBlockSize,
                (byte) 0x80, 0x00, 0x01, 0x01, 0x01
        };
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x07,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
                0x00, //number of block.
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,// block data
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00// block data
        };

        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x07,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
                0x00, //number of block.
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,// block data
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};// block data

        byte[] result = target.readWithoutEncryption(testIdm,
                testServiceCodeList, testBlockList, testBlockSize, 110);

        assertTrue(Arrays.equals(result, expect));

    }

    /**
     * Over timeout value.
     */
    @Test
    public void readWithoutEncryption02() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;

        byte[] mockExpectValue = {0x04, (byte) 0xFF, (byte) 0xFF, 0x15,// header
                0x06,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                (byte) ((byte) testServiceCodeList.length / 2),// service code number
                0x00, 0x00, 0x00, 0x00, // service code list
                (byte) testBlockSize,
                (byte) 0x80, 0x00, 0x01, 0x01, 0x01
        };
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x07,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
                0x00, //number of block.
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,// block data
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00// block data
        };

        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x07,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
                0x00, //number of block.
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,// block data
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};// block data

        byte[] result = target.readWithoutEncryption(testIdm,
                testServiceCodeList, testBlockList, testBlockSize, 11000000);

        assertTrue(Arrays.equals(result, expect));

    }

    /**
     * Max service code list
     */
    @Test
    public void readWithoutEncryption03() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;

        byte[] mockExpectValue = {0x04, (byte) 0xFF, (byte) 0xFF, 0x21,// header
                0x06,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                (byte) ((byte) testServiceCodeList.length / 2),// service code number
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // service code list
                (byte) testBlockSize,
                (byte) 0x80, 0x00, 0x01, 0x01, 0x01
        };
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x07,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
                0x00, //number of block.
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,// block data
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00// block data
        };

        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x07,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
                0x00, //number of block.
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,// block data
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};// block data

        byte[] result = target.readWithoutEncryption(testIdm,
                testServiceCodeList, testBlockList, testBlockSize, 11000000);

        assertTrue(Arrays.equals(result, expect));

    }

    /**
     * min service code list
     */
    @Test
    public void readWithoutEncryption04() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;

        byte[] mockExpectValue = {0x04, (byte) 0xFF, (byte) 0xFF, 0x13,// header
                0x06,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                (byte) ((byte) testServiceCodeList.length / 2),// service code number
                0x00, 0x00,// service code list
                (byte) testBlockSize,
                (byte) 0x80, 0x00, 0x01, 0x01, 0x01
        };
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x07,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
                0x00, //number of block.
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,// block data
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00// block data
        };

        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x07,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
                0x00, //number of block.
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,// block data
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};// block data

        byte[] result = target.readWithoutEncryption(testIdm,
                testServiceCodeList, testBlockList, testBlockSize, 11000000);

        assertTrue(Arrays.equals(result, expect));

    }

    /**
     * Min testBlockSize (2 byte)
     */
    @Test
    public void readWithoutEncryption05() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00};//A block is 2byte or 3byte
        int testBlockSize = 1;

        byte[] mockExpectValue = {0x04, 0x6E, 0x00, 0x12,// header
                0x06,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                (byte) ((byte) testServiceCodeList.length / 2),// service code number
                0x00, 0x00, 0x00, 0x00, // service code list
                (byte) testBlockSize,
                (byte) 0x80, 0x00
        };
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x07,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
                0x00, //number of block.
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,// block data
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00// block data
        };

        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x07,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
                0x00, //number of block.
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,// block data
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};// block data

        byte[] result = target.readWithoutEncryption(testIdm,
                testServiceCodeList, testBlockList, testBlockSize, 110);

        assertTrue(Arrays.equals(result, expect));

    }

    /**
     * Min testBlockSize (3 byte)
     */
    @Test
    public void readWithoutEncryption06() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {0x00, 0x00, 0x00};//A block is 2byte or 3byte
        int testBlockSize = 1;

        byte[] mockExpectValue = {0x04, 0x6E, 0x00, 0x13,// header
                0x06,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                (byte) ((byte) testServiceCodeList.length / 2),// service code number
                0x00, 0x00, 0x00, 0x00, // service code list
                (byte) testBlockSize,
                0x00, 0x00, 0x00
        };
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x07,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
                0x00, //number of block.
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,// block data
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00// block data
        };

        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x07,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
                0x00, //number of block.
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,// block data
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};// block data

        byte[] result = target.readWithoutEncryption(testIdm,
                testServiceCodeList, testBlockList, testBlockSize, 110);

        assertTrue(Arrays.equals(result, expect));

    }

    /**
     * Illigal Idm value less than 8.
     */
    @Test
    public void AbnormalreadWithoutEncryption01() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;

        String expect = "Must be idm Length is just 8 byte but idm length is 7";

        try {
            byte[] result = target.readWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal Idm value more than 8.
     */
    @Test
    public void AbnormalreadWithoutEncryption02() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;

        String expect = "Must be idm Length is just 8 byte but idm length is 9";

        try {
            byte[] result = target.readWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }


    /**
     * Illigal testService code empty.
     */
    @Test
    public void AbnormalreadWithoutEncryption03() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;

        String expect = "Must be service code list length is 2 to 32 but length is 0";

        try {
            byte[] result = target.readWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal testService length is more than 16(actual is over 32).
     */
    @Test
    public void AbnormalreadWithoutEncryption04() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};// length / 2 =16
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;

        String expect = "Must be service code list length is 2 to 32 but length is 33";

        try {
            byte[] result = target.readWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal testService length is less than 2.
     */
    @Test
    public void AbnormalreadWithoutEncryption05() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;

        String expect = "Must be service code list length is 2 to 32 but length is 1";

        try {
            byte[] result = target.readWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal testService length is odd.
     */
    @Test
    public void AbnormalreadWithoutEncryption06() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;

        String expect = "Must be service code list length is Even but length is 3";

        try {
            byte[] result = target.readWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal block list length. block list is 2 block (2 byte and 3 byte) but size is 1
     */
    @Test
    public void AbnormalreadWithoutEncryption07() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 1;

        String expect = "Wrong block number. you specific : 1";

        try {
            byte[] result = target.readWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal block list length. block list is 2 block (2 byte and 2 byte) but size is 1
     */
    @Test
    public void AbnormalreadWithoutEncryption08() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, (byte) 0x81, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 1;

        String expect = "Wrong block number. you specific : 1";

        try {
            byte[] result = target.readWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal block list length. block list is 3 block (2 byte and 3 byte) but size is 1
     */
    @Test
    public void AbnormalreadWithoutEncryption09() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 3;

        String expect = "Wrong block number. you specific : 3";

        try {
            byte[] result = target.readWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal block list length. block list is 3 block (3 byte and 3 byte) but size is 1
     */
    @Test
    public void AbnormalreadWithoutEncryption10() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {0x00, 0x00, 0x00, 0x01, 0x01, 0x01};//2 block element(3byte )
        int testBlockSize = 3;

        String expect = "Wrong block number. you specific : 3";

        try {
            byte[] result = target.readWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal block list length. block list is 3 block (2 byte and 2 byte) but size is 1
     */
    @Test
    public void AbnormalreadWithoutEncryption11() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, (byte) 0x81, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 3;

        String expect = "Wrong block number. you specific : 3";

        try {
            byte[] result = target.readWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * BlockList length is illigal. too long.
     */
    @Test
    public void AbnormalreadWithoutEncryption12() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, (byte) 0x81, 0x01, 0x02};//A block is 2byte or 3byte
        int testBlockSize = 2;

        String expect = "Illigal block list byte length.";

        try {
            byte[] result = target.readWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal block list length. block list is 3 block (2 byte and 2 byte) but size is 1
     */
    @Test
    public void AbnormalreadWithoutEncryption13() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, (byte) 0x81};//A block is 2byte or 3byte
        int testBlockSize = 2;

        String expect = "Illigal block list byte length.";

        try {
            byte[] result = target.readWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }


    /**
     * Normal
     */
    @Test
    public void writeWithoutEncryption01() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        byte[] mockExpectValue = {0x04, 0x6E, 0x00, 0x35,// header
                0x08,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                (byte) ((byte) testServiceCodeList.length / 2),// service code number
                0x00, 0x00, 0x00, 0x00, // service code list
                (byte) testBlockSize,
                (byte) 0x80, 0x00, 0x01, 0x01, 0x01, //BlockList
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//block data
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        };
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x09,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
        };

        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x09,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00};// status flag 1 and 2

        byte[] result = target.writeWithoutEncryption(testIdm,
                testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);

        assertTrue(Arrays.equals(result, expect));

    }

    /**
     * Over timeout value.
     */
    @Test
    public void writeWithoutEncryption02() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        byte[] mockExpectValue = {0x04, (byte) 0xFF, (byte) 0xFF, 0x35,// header
                0x08,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                (byte) ((byte) testServiceCodeList.length / 2),// service code number
                0x00, 0x00, 0x00, 0x00, // service code list
                (byte) testBlockSize,
                (byte) 0x80, 0x00, 0x01, 0x01, 0x01, //BlockList
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//block data
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        };
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x09,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
        };

        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x09,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00};// status flag 1 and 2

        byte[] result = target.writeWithoutEncryption(testIdm,
                testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110000000);

        assertTrue(Arrays.equals(result, expect));

    }

    /**
     * Max service code list
     */
    @Test
    public void writeWithoutEncryption03() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        byte[] mockExpectValue = {0x04, (byte) 0xFF, (byte) 0xFF, 0x41,// header
                0x08,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                (byte) ((byte) testServiceCodeList.length / 2),// service code number
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // service code list
                (byte) testBlockSize,
                (byte) 0x80, 0x00, 0x01, 0x01, 0x01,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//block data
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00

        };
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x09,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
        };

        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x09,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00};// status flag 1 and 2;// block data

        byte[] result = target.writeWithoutEncryption(testIdm,
                testServiceCodeList, testBlockList, testBlockSize, testBlockData, 11000000);

        assertTrue(Arrays.equals(result, expect));

    }

    /**
     * min service code list
     */
    @Test
    public void writeWithoutEncryption04() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


        byte[] mockExpectValue = {0x04, (byte) 0xFF, (byte) 0xFF, 0x33,// header
                0x08,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                (byte) ((byte) testServiceCodeList.length / 2),// service code number
                0x00, 0x00,// service code list
                (byte) testBlockSize,
                (byte) 0x80, 0x00, 0x01, 0x01, 0x01,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//block data
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        };
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x09,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
        };

        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x09,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
        };

        byte[] result = target.writeWithoutEncryption(testIdm,
                testServiceCodeList, testBlockList, testBlockSize, testBlockData, 11000000);

        assertTrue(Arrays.equals(result, expect));

    }

    /**
     * Min testBlockSize (2 byte)
     */
    @Test
    public void writeWithoutEncryption05() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00};//A block is 2byte or 3byte
        int testBlockSize = 1;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


        byte[] mockExpectValue = {0x04, 0x6E, 0x00, 0x22,// header
                0x08,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                (byte) ((byte) testServiceCodeList.length / 2),// service code number
                0x00, 0x00, 0x00, 0x00, // service code list
                (byte) testBlockSize,
                (byte) 0x80, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00// block data
        };
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x09,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00// status flag 1 and 2
        };

        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x09,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
        };// block data

        byte[] result = target.writeWithoutEncryption(testIdm,
                testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);

        assertTrue(Arrays.equals(result, expect));

    }

    /**
     * Min testBlockSize (3 byte)
     */
    @Test
    public void writeWithoutEncryption06() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {0x00, 0x00, 0x00};//A block is 2byte or 3byte
        int testBlockSize = 1;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        byte[] mockExpectValue = {0x04, 0x6E, 0x00, 0x23,// header
                0x08,// command
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                (byte) ((byte) testServiceCodeList.length / 2),// service code number
                0x00, 0x00, 0x00, 0x00, // service code list
                (byte) testBlockSize,
                0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00// block Data
        };
        byte[] mockReturn = {(byte) 0xD7, (byte) 0x04 + 1, 0x00, 0x00, 0x00,//header
                0x09,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
        };

        when(usbPasoriDriver.transferCommand(mockExpectValue, mockExpectValue.length))
                .thenReturn(mockReturn);

        byte[] expect = {0x09,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,//IDM
                0x00, 0x00,// status flag 1 and 2
        };

        byte[] result = target.writeWithoutEncryption(testIdm,
                testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);

        assertTrue(Arrays.equals(result, expect));

    }

    /**
     * Illigal Idm value less than 8.
     */
    @Test
    public void AbnormalwriteWithoutEncryption01() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


        String expect = "Must be idm Length is just 8 byte but idm length is 7";

        try {
            byte[] result = target.writeWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal Idm value more than 8.
     */
    @Test
    public void AbnormalwriteWithoutEncryption02() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        String expect = "Must be idm Length is just 8 byte but idm length is 9";

        try {
            byte[] result = target.writeWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }


    /**
     * Illigal testService code empty.
     */
    @Test
    public void AbnormalwriteWithoutEncryption03() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


        String expect = "Must be service code list length is 2 to 32 but length is 0";

        try {
            byte[] result = target.writeWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal testService length is more than 16(actual is over 32).
     */
    @Test
    public void AbnormalwriteWithoutEncryption04() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};// length / 2 =16
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


        String expect = "Must be service code list length is 2 to 32 but length is 33";

        try {
            byte[] result = target.writeWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal testService length is less than 2.
     */
    @Test
    public void AbnormalwriteWithoutEncryption05() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        String expect = "Must be service code list length is 2 to 32 but length is 1";

        try {
            byte[] result = target.writeWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal testService length is odd.
     */
    @Test
    public void AbnormalwriteWithoutEncryption06() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 2;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


        String expect = "Must be service code list length is Even but length is 3";

        try {
            byte[] result = target.writeWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal block list length. block list is 2 block (2 byte and 3 byte) but size is 1
     */
    @Test
    public void AbnormalwriteWithoutEncryption07() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 1;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


        String expect = "Wrong block number. you specific : 1";

        try {
            byte[] result = target.writeWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal block list length. block list is 2 block (2 byte and 2 byte) but size is 1
     */
    @Test
    public void AbnormalwriteWithoutEncryption08() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, (byte) 0x81, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 1;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        String expect = "Wrong block number. you specific : 1";

        try {
            byte[] result = target.writeWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal block list length. block list is 3 block (2 byte and 3 byte) but size is 1
     */
    @Test
    public void AbnormalwriteWithoutEncryption09() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, 0x01, 0x01, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 3;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        String expect = "Wrong block number. you specific : 3";

        try {
            byte[] result = target.writeWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal block list length. block list is 3 block (3 byte and 3 byte) but size is 1
     */
    @Test
    public void AbnormalwriteWithoutEncryption10() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {0x00, 0x00, 0x00, 0x01, 0x01, 0x01};//2 block element(3byte )
        int testBlockSize = 3;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        String expect = "Wrong block number. you specific : 3";

        try {
            byte[] result = target.writeWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal block list length. block list is 3 block (2 byte and 2 byte) but size is 1
     */
    @Test
    public void AbnormalwriteWithoutEncryption11() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, (byte) 0x81, 0x01};//A block is 2byte or 3byte
        int testBlockSize = 3;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        String expect = "Wrong block number. you specific : 3";

        try {
            byte[] result = target.writeWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * BlockList length is illigal. too long.
     */
    @Test
    public void AbnormalwriteWithoutEncryption12() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, (byte) 0x81, 0x01, 0x02};//A block is 2byte or 3byte
        int testBlockSize = 2;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


        String expect = "Illigal block list byte length.";

        try {
            byte[] result = target.writeWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal block list length. block list is 3 block (2 byte and 2 byte) but size is 1
     */
    @Test
    public void AbnormalwriteWithoutEncryption13() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, (byte) 0x81};//A block is 2byte or 3byte
        int testBlockSize = 2;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


        String expect = "Illigal block list byte length.";

        try {
            byte[] result = target.writeWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }


    /**
     * Illigal block data length. less than block size * 16
     */
    @Test
    public void AbnormalwriteWithoutEncryption14() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, (byte) 0x81, 0x00};//A block is 2byte or 3byte
        int testBlockSize = 2;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


        String expect = "Illigal block data size, expect size is 32";

        try {
            byte[] result = target.writeWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

    /**
     * Illigal block data length. more than block size * 16
     */
    @Test
    public void AbnormalwriteWithoutEncryption15() {
        byte[] testIdm = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] testServiceCodeList = {0x00, 0x00, 0x00, 0x00};
        byte[] testBlockList = {(byte) 0x80, 0x00, (byte) 0x81, 0x00};//A block is 2byte or 3byte
        int testBlockSize = 2;
        byte[] testBlockData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


        String expect = "Illigal block data size, expect size is 32";

        try {
            byte[] result = target.writeWithoutEncryption(testIdm,
                    testServiceCodeList, testBlockList, testBlockSize, testBlockData, 110);
            fail();
        } catch (IlligalParameterTypeException e) {
            assertThat(e.getMessage(), is(expect));
        }

    }

}