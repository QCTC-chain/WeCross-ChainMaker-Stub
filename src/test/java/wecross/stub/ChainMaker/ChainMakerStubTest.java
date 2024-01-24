package wecross.stub.ChainMaker;

import org.junit.Assert;
import org.junit.Test;

import com.webank.wecross.stub.chainmaker.ChainMakerStubFactory;

public class ChainMakerStubTest {
    @Test
    public void ChainMakerStubFactoryTest() {
        try {
            ChainMakerStubFactory stubFactory = new ChainMakerStubFactory();
            Assert.assertNotNull("stubFactory object is null", stubFactory);
            stubFactory.newConnection("/Users/dbliu/work/java/WeCross-ChainMaker-Stub/src/main/resources");
        } catch (Exception ec) {
            System.out.println(ec);
        }
    }
}
