package ka.masato.library.device.pasori.service;

import ka.masato.library.device.pasori.exception.IlligalCardDataException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
    public void AbnormallaodPacketTest01() {
        byte[] testData = {
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };

        try {
            target.laodPacket(testData);
            fail();
        } catch (IlligalCardDataException e) {
            assertThat(e.getMessage(), equalTo("Can not load packet, because packet length illigal."));
        }

    }

    @Test
    public void laodPacketTest01() {
        byte[] testData = {
                (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,//header
                (byte) 0x02, (byte) 0x00, //length
                (byte) 0xcf, //checksum for length data + 0xd6
                (byte) 0xd6, //start data byte
                (byte) 0x05, //data
                (byte) 0x00,//
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0xcf, //checksum for data(include d6)
                (byte) 0x00 // end
        };

        byte[] result = target.laodPacket(testData).getCmdPayload();
        //assertThat(result, )

    }

    @Test
    public void decodeIDm() {
    }

    @Test
    public void decodePMm() {
    }

    @Test
    public void getIDm() {
    }

    @Test
    public void getPMm() {
    }
}