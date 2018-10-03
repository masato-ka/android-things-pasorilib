package ka.masato.library.device.pasori.service;

import ka.masato.library.device.pasori.exception.CardDataDecodeErrorException;
import ka.masato.library.device.pasori.exception.IlligalCardDataException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


public class CardDataDecoderTest {

    CardDataDecoder target;

    @Before
    public void setUp() throws Exception {
        target = new CardDataDecoder();
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void loadPacketTest01() {
        byte[] testData = {
                0x08, //unknown but a part of header.
                0x14, // data length + me(1)
                0x01, // data header 0x01 is response of polling.
                0x01,// start IDM
                0x01, 0x04, 0x10, 0x06, 0x0F, (byte) 0x9F, 0x23,
                0x10,//start PMm
                0x0B, 0x4B, 0x42, (byte) 0x84, (byte) 0x85, (byte) 0xD0, (byte) 0xFF,
                0x00,// start data
                0x03
        };
        byte[] expect = {
                0x01, // data header 0x01 is response of polling.
                0x01,// start IDM
                0x01, 0x04, 0x10, 0x06, 0x0F, (byte) 0x9F, 0x23,
                0x10,//start PMm
                0x0B, 0x4B, 0x42, (byte) 0x84, (byte) 0x85, (byte) 0xD0, (byte) 0xFF,
                0x00,// start data
                0x03
        };
        byte[] result = target.loadPacket(testData).getCmdPayload();
        assertThat(result.length, is(expect.length));
        assertTrue(Arrays.equals(result, expect));

    }

    @Test
    public void loadPacketTest02() {
        byte[] testData = new byte[129];

        testData[0] = 0x08;
        testData[1] = (byte) 0x80;

        for (int i = 2; i < 129; i++) {
            testData[i] = 0x01;
        }

        byte[] expect = new byte[127];

        for (int i = 0; i < 127; i++) {
            expect[i] = 0x01;
        }

        byte[] result = target.loadPacket(testData).getCmdPayload();
        assertThat(result.length, is(expect.length));
        assertTrue(Arrays.equals(result, expect));
    }

    @Test
    public void loadPacketTest03() {
        byte[] testData = new byte[3];

        testData[0] = 0x08;
        testData[1] = (byte) 0x02;
        testData[2] = 0x01;

        byte[] expect = new byte[1];
        expect[0] = 0x01;

        byte[] result = target.loadPacket(testData).getCmdPayload();
        assertThat(result.length, is(expect.length));
        assertTrue(Arrays.equals(result, expect));
    }

    /**
     * Set Wrong packet length
     */
    @Test
    public void AbnormalloadPacketTest01() {
        byte[] testData = {
                0x08, //unknown but a part of header.
                0x14, // data length + me(1)
                0x01, // data header 0x01 is response of polling.
                0x01,
                0x01
        };

        try {
            target.loadPacket(testData);
            fail();
        } catch (IlligalCardDataException e) {
            assertThat(e.getMessage(), equalTo("Can not load packet, because packet length illigal."));
        }

    }

    /**
     * empty data.
     */
    @Test
    public void AbnormalloadPacketTest02() {
        byte[] testData = {};
        try {
            target.loadPacket(testData);
        } catch (IlligalCardDataException e) {
            assertThat(e.getMessage(), equalTo("Can not load packet, because packet length illigal."));
        }
    }

    /**
     * size 2 array test. ( WhiteBox boundary test).
     */

    @Test
    public void AbnormalloadPacketTest03() {
        byte[] testData = {
                0x01, 0x01
        };

        try {
            target.loadPacket(testData);
        } catch (IlligalCardDataException e) {
            assertThat(e.getMessage(), equalTo("Can not load packet, because packet length illigal."));
        }

    }


    @Test
    public void decodeIDm01() {
        byte[] testData = {
                0x08, //unknown but a part of header.
                0x14, // data length + me(1)
                0x01, // data header 0x01 is response of polling.
                0x01,// start IDM
                0x01, 0x04, 0x10, 0x06, 0x0F, (byte) 0x9F, 0x23,
                0x10,//start PMm
                0x0B, 0x4B, 0x42, (byte) 0x84, (byte) 0x85, (byte) 0xD0, (byte) 0xFF,
                0x00,// start data
                0x03
        };
        String expect = "01010410060F9F23";
        String result = target.loadPacket(testData).decodeIDm().getIDm();
        assertThat(result, is(expect));
    }

    @Test
    public void decodeIDm02() {
        byte[] testData = {
                0x08, //unknown but a part of header.
                0x12, // data length + me(1)
                0x01, // data header 0x01 is response of polling.
                0x01,// start IDM
                0x01, 0x04, 0x10, 0x06, 0x0F, (byte) 0x9F, 0x23,
                0x10,//start PMm
                0x0B, 0x4B, 0x42, (byte) 0x84, (byte) 0x85, (byte) 0xD0, (byte) 0xFF
        };
        String expect = "01010410060F9F23";
        String result = target.loadPacket(testData).decodeIDm().getIDm();
        assertThat(result, is(expect));
    }

    @Test
    public void AbnormaldecodeIDm01() {
        byte[] testData = {
                0x08, //unknown but a part of header.
                0x14, // data length + me(1)
                0x00, // data header 0x01 is response of polling.
                0x01,// start IDM
                0x01, 0x04, 0x10, 0x06, 0x0F, (byte) 0x9F, 0x23,
                0x10,//start PMm
                0x0B, 0x4B, 0x42, (byte) 0x84, (byte) 0x85, (byte) 0xD0, (byte) 0xFF,
                0x00,// start data
                0x03
        };

        String expect = "Illigal cmd payload, can not decode IDm";

        try {
            String result = target.loadPacket(testData).decodeIDm().getIDm();
            fail();
        } catch (CardDataDecodeErrorException e) {
            assertThat(e.getMessage(), is(expect));
        }
    }

    @Test
    public void AbnormaldecodeIDm02() {

        String expect = "Should not decode before load packet function.";

        try {
            String result = target.decodeIDm().getIDm();
            fail();
        } catch (CardDataDecodeErrorException e) {
            assertThat(e.getMessage(), is(expect));
        }
    }

    @Test
    public void AbnormaldecodeIDm03() {
        byte[] testData = {
                0x08, //unknown but a part of header.
                0x11, // data length + me(1)
                0x01, // data header 0x01 is response of polling.
                0x02,// start IDM
                0x01, 0x04, (byte) 0x9F, 0x23,
                0x10,//start PMm
                0x0B, 0x4B, 0x42, (byte) 0x84, (byte) 0x85, (byte) 0xD0, (byte) 0xFF,
                0x00,// start data
                0x03
        };

        String expect = "Can not decode IDm because data length is too short.";

        try {
            String result = target.loadPacket(testData).decodeIDm().getIDm();
            fail();
        } catch (CardDataDecodeErrorException e) {
            assertThat(e.getMessage(), is(expect));
        }
    }

    @Test
    public void decodePMm01() {
        byte[] testData = {
                0x08, //unknown but a part of header.
                0x14, // data length + me(1)
                0x01, // data header 0x01 is response of polling.
                0x01,// start IDM
                0x01, 0x04, 0x10, 0x06, 0x0F, (byte) 0x9F, 0x23,
                0x10,//start PMm
                0x0B, 0x4B, 0x42, (byte) 0x84, (byte) 0x85, (byte) 0xD0, (byte) 0xFF,
                0x00,// start data
                0x03
        };
        String expect = "100B4B428485D0FF";
        String result = target.loadPacket(testData).decodePMm().getPMm();
        assertThat(result, is(expect));
    }

    @Test
    public void decodePMm02() {
        byte[] testData = {
                0x08, //unknown but a part of header.
                0x12, // data length + me(1)
                0x01, // data header 0x01 is response of polling.
                0x01,// start IDM
                0x01, 0x04, 0x10, 0x06, 0x0F, (byte) 0x9F, 0x23,
                0x10,//start PMm
                0x0B, 0x4B, 0x42, (byte) 0x84, (byte) 0x85, (byte) 0xD0, (byte) 0xFF
        };
        String expect = "100B4B428485D0FF";
        String result = target.loadPacket(testData).decodePMm().getPMm();
        assertThat(result, is(expect));
    }

    @Test
    public void AbnormaldecodePMm01() {
        byte[] testData = {
                0x08, //unknown but a part of header.
                0x14, // data length + me(1)
                0x00, // data header 0x01 is response of polling.
                0x01,// start IDM
                0x01, 0x04, 0x10, 0x06, 0x0F, (byte) 0x9F, 0x23,
                0x10,//start PMm
                0x0B, 0x4B, 0x42, (byte) 0x84, (byte) 0x85, (byte) 0xD0, (byte) 0xFF,
                0x00,// start data
                0x03
        };

        String expect = "Illigal cmd payload, can not decode PMm";

        try {
            String result = target.loadPacket(testData).decodePMm().getPMm();
            fail();
        } catch (CardDataDecodeErrorException e) {
            assertThat(e.getMessage(), is(expect));
        }
    }

    @Test
    public void AbnormaldecodePMm02() {

        String expect = "Should not decode before load packet function.";

        try {
            String result = target.decodePMm().getPMm();
            fail();
        } catch (CardDataDecodeErrorException e) {
            assertThat(e.getMessage(), is(expect));
        }
    }

    @Test
    public void AbnormaldecodePMm03() {
        byte[] testData = {
                0x08, //unknown but a part of header.
                0x11, // data length + me(1)
                0x01, // data header 0x01 is response of polling.
                0x02,// start IDM
                0x01, 0x04, (byte) 0x9F, 0x23,
                0x10,//start PMm
                0x0B, 0x4B, 0x42, (byte) 0x84, (byte) 0x85, (byte) 0xD0, (byte) 0xFF,
                0x00,// start data
                0x03
        };

        String expect = "Can not decode PMm because data length is too short.";

        try {
            String result = target.loadPacket(testData).decodePMm().getPMm();
            fail();
        } catch (CardDataDecodeErrorException e) {
            assertThat(e.getMessage(), is(expect));
        }
    }

    @Test
    public void getIDm01() {
        String result = target.getIDm();
        assertThat(result, is(""));
    }

    @Test
    public void getPMm01() {
        String result = target.getPMm();
        assertThat(result, is(""));
    }

}