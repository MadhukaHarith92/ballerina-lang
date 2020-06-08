/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.annotations;

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.TypeDefinition;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.List;

/**
 * Class to test icon annotation.
 *
 * @since 2.0
 */
public class IconAnnotationTest {

    private CompileResult result;
    private CompileResult negative;

    @BeforeClass
    public void setup() {
        negative = BCompileUtil.compile("test-src/annotations/icon_annot_negative.bal");
        result = BCompileUtil.compile("test-src/annotations/icon_annot.bal");
    }

    @Test
    public void testIconOnFunction() {
        BLangFunction fooFunction = (BLangFunction) ((List) ((BLangPackage) result.getAST()).functions).get(0);
        BLangAnnotationAttachment annot = (BLangAnnotationAttachment) ((List) fooFunction.annAttachments).get(0);
        Assert.assertEquals(annot.expr.toString(), " {path: /fooIconPath.icon}");
    }

    @Test
    public void testIconOnObjectAndMemberFunction() {
        TypeDefinition objType = result.getAST().getTypeDefinitions().get(0);
        List<? extends AnnotationAttachmentNode> objAnnot = objType.getAnnotationAttachments();
        Assert.assertEquals(objAnnot.size(), 1);
        Assert.assertEquals(objAnnot.get(0).getExpression().toString(), " {path: /barIconPath.icon}");
    }

    @Test void testIconAnnotationNegative() {
        BAssertUtil.validateError(negative, 0,
                "cannot specify more than one annotation value for annotation 'icon'", 17, 1);
        BAssertUtil.validateError(negative, 1,
                "annotation 'ballerina/lang.annotations:1.0.0:icon' is not allowed on var", 23, 1);
        Assert.assertEquals(negative.getErrorCount(), 2);
    }
}
