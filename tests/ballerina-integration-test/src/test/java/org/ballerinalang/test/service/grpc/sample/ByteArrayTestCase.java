/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.service.grpc.sample;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A test case to test byte array.
 *
 * @since 0.983.0
 */
@Test(groups = "grpc-test")
public class ByteArrayTestCase extends GrpcBaseTest {

    @BeforeClass
    public void setup() throws Exception {
        TestUtils.prepareBalo(this);
    }

    @Test
    public void testbyteArray() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "clients", "grpc_byte_client.bal");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        final String serverMsg = "byte array works";

        BValue[] responses = BRunUtil.invoke(result, "testByteArray", new BValue[]{});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        BString responseValues = (BString) responses[0];
        Assert.assertEquals(responseValues.stringValue(), serverMsg);
    }

    @Test(description = "Test transmitting 30KB content in data frame.")
    public void testLargeByteArray() {
        Path balFilePath = Paths.get("src", "test", "resources", "grpc", "clients", "grpc_byte_client.bal");
        Path sampleDataFile = Paths.get("src", "test", "resources", "grpc", "clients", "sample_bytes.txt");
        CompileResult result = BCompileUtil.compile(balFilePath.toAbsolutePath().toString());
        final String serverMsg = "30KB file content transmitted successfully";

        BValue[] responses = BRunUtil.invoke(result, "testLargeByteArray",
                new BValue[]{new BString(sampleDataFile.toAbsolutePath().toString())});
        Assert.assertEquals(responses.length, 1);
        Assert.assertTrue(responses[0] instanceof BString);
        BString responseValues = (BString) responses[0];
        Assert.assertEquals(responseValues.stringValue(), serverMsg);
    }
}