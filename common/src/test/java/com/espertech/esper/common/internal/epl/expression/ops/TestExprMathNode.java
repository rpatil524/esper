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
import com.espertech.esper.common.internal.supportunit.util.SupportExprNodeUtil;
import com.espertech.esper.common.internal.type.MathArithTypeEnum;
import junit.framework.TestCase;

public class TestExprMathNode extends TestCase {
    private ExprMathNode arithNode;

    public void setUp() {
        arithNode = new ExprMathNode(MathArithTypeEnum.ADD, false, false);
    }

    public void testGetType() throws Exception {
        arithNode.addChildNode(new SupportExprNode(Double.class));
        arithNode.addChildNode(new SupportExprNode(Integer.class));
        arithNode.validate(SupportExprValidationContextFactory.makeEmpty());
        assertEquals(EPTypePremade.DOUBLEBOXED.getEPType(), arithNode.getForge().getEvaluationType());
    }

    public void testToExpressionString() throws Exception {
        // Build (5*(4-2)), not the same as 5*4-2
        ExprMathNode arithNodeChild = new ExprMathNode(MathArithTypeEnum.SUBTRACT, false, false);
        arithNodeChild.addChildNode(new SupportExprNode(4));
        arithNodeChild.addChildNode(new SupportExprNode(2));

        arithNode = new ExprMathNode(MathArithTypeEnum.MULTIPLY, false, false);
        arithNode.addChildNode(new SupportExprNode(5));
        arithNode.addChildNode(arithNodeChild);

        assertEquals("5*(4-2)", ExprNodeUtilityPrint.toExpressionStringMinPrecedenceSafe(arithNode));
    }

    public void testValidate() {
        // Must have exactly 2 subnodes
        try {
            arithNode.validate(SupportExprValidationContextFactory.makeEmpty());
            fail();
        } catch (ExprValidationException ex) {
            // Expected
        }

        // Must have only number-type subnodes
        arithNode.addChildNode(new SupportExprNode(String.class));
        arithNode.addChildNode(new SupportExprNode(Integer.class));
        try {
            arithNode.validate(SupportExprValidationContextFactory.makeEmpty());
            fail();
        } catch (ExprValidationException ex) {
            // Expected
        }
    }

    public void testEvaluate() throws Exception {
        arithNode.addChildNode(new SupportExprNode(10));
        arithNode.addChildNode(new SupportExprNode(1.5));
        ExprNodeUtilityValidate.getValidatedSubtree(ExprNodeOrigin.SELECT, arithNode, SupportExprValidationContextFactory.makeEmpty());
        assertEquals(11.5d, arithNode.getForge().getExprEvaluator().evaluate(null, false, null));

        arithNode = makeNode(null, Integer.class, 5d, Double.class);
        assertNull(arithNode.getForge().getExprEvaluator().evaluate(null, false, null));

        arithNode = makeNode(5, Integer.class, null, Double.class);
        assertNull(arithNode.getForge().getExprEvaluator().evaluate(null, false, null));

        arithNode = makeNode(null, Integer.class, null, Double.class);
        assertNull(arithNode.getForge().getExprEvaluator().evaluate(null, false, null));
    }

    public void testEqualsNode() throws Exception {
        assertTrue(arithNode.equalsNode(arithNode, false));
        assertFalse(arithNode.equalsNode(new ExprMathNode(MathArithTypeEnum.DIVIDE, false, false), false));
    }

    private ExprMathNode makeNode(Object valueLeft, Class typeLeft, Object valueRight, Class typeRight) throws Exception {
        ExprMathNode mathNode = new ExprMathNode(MathArithTypeEnum.MULTIPLY, false, false);
        mathNode.addChildNode(new SupportExprNode(valueLeft, typeLeft));
        mathNode.addChildNode(new SupportExprNode(valueRight, typeRight));
        SupportExprNodeUtil.validate(mathNode);
        return mathNode;
    }
}
