/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.common.internal.epl.expression.ops;

import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.epl.expression.core.ExprNodeOrigin;
import com.espertech.esper.common.internal.epl.expression.core.ExprNodeUtilityPrint;
import com.espertech.esper.common.internal.epl.expression.core.ExprNodeUtilityValidate;
import com.espertech.esper.common.internal.epl.expression.core.ExprValidationException;
import com.espertech.esper.common.internal.support.SupportExprValidationContextFactory;
import com.espertech.esper.common.internal.supportunit.util.SupportExprNode;
import com.espertech.esper.common.internal.type.BitWiseOpEnum;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestExprBitWiseNode extends TestCase {

    private ExprBitWiseNode _bitWiseNode;

    public void setUp() {
        _bitWiseNode = new ExprBitWiseNode(BitWiseOpEnum.BAND);
    }

    public void testValidate() {
        // Must have exactly 2 subnodes
        try {
            _bitWiseNode.validate(SupportExprValidationContextFactory.makeEmpty());
            fail();
        } catch (ExprValidationException ex) {
            // Expected
            log.debug("No nodes in the expression");
        }

        // Must have only number or boolean-type subnodes
        _bitWiseNode.addChildNode(new SupportExprNode(String.class));
        _bitWiseNode.addChildNode(new SupportExprNode(Integer.class));
        try {
            _bitWiseNode.validate(SupportExprValidationContextFactory.makeEmpty());
            fail();
        } catch (ExprValidationException ex) {
            // Expected
        }
    }

    public void testGetType() throws Exception {
        log.debug(".testGetType");
        _bitWiseNode = new ExprBitWiseNode(BitWiseOpEnum.BAND);
        _bitWiseNode.addChildNode(new SupportExprNode(Double.class));
        _bitWiseNode.addChildNode(new SupportExprNode(Integer.class));
        try {
            _bitWiseNode.validate(SupportExprValidationContextFactory.makeEmpty());
            fail();
        } catch (ExprValidationException ex) {
            // Expected
        }
        _bitWiseNode = new ExprBitWiseNode(BitWiseOpEnum.BAND);
        _bitWiseNode.addChildNode(new SupportExprNode(Long.class));
        _bitWiseNode.addChildNode(new SupportExprNode(Long.class));
        ExprNodeUtilityValidate.getValidatedSubtree(ExprNodeOrigin.SELECT, _bitWiseNode, SupportExprValidationContextFactory.makeEmpty());
        assertEquals(EPTypePremade.LONGBOXED.getEPType(), _bitWiseNode.getForge().getEvaluationType());
    }

    public void testEvaluate() throws Exception {
        log.debug(".testEvaluate");
        _bitWiseNode.addChildNode(new SupportExprNode(10));
        _bitWiseNode.addChildNode(new SupportExprNode(12));
        ExprNodeUtilityValidate.getValidatedSubtree(ExprNodeOrigin.SELECT, _bitWiseNode, SupportExprValidationContextFactory.makeEmpty());
        assertEquals(8, _bitWiseNode.getForge().getExprEvaluator().evaluate(null, false, null));
    }

    public void testEqualsNode() throws Exception {
        log.debug(".testEqualsNode");
        _bitWiseNode = new ExprBitWiseNode(BitWiseOpEnum.BAND);
        assertTrue(_bitWiseNode.equalsNode(_bitWiseNode, false));
        assertFalse(_bitWiseNode.equalsNode(new ExprBitWiseNode(BitWiseOpEnum.BXOR), false));
    }

    public void testToExpressionString() throws Exception {
        log.debug(".testToExpressionString");
        _bitWiseNode = new ExprBitWiseNode(BitWiseOpEnum.BAND);
        _bitWiseNode.addChildNode(new SupportExprNode(4));
        _bitWiseNode.addChildNode(new SupportExprNode(2));
        assertEquals("4&2", ExprNodeUtilityPrint.toExpressionStringMinPrecedenceSafe(_bitWiseNode));
    }

    static Logger log = LoggerFactory.getLogger(TestExprBitWiseNode.class);

}
