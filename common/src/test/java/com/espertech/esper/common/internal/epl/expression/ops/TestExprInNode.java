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

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPType;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.epl.expression.core.ExprNodeUtilityPrint;
import com.espertech.esper.common.internal.epl.expression.core.ExprValidationException;
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.common.internal.support.SupportExprValidationContextFactory;
import com.espertech.esper.common.internal.supportunit.event.SupportEventBeanFactory;
import com.espertech.esper.common.internal.supportunit.util.SupportExprNode;
import com.espertech.esper.common.internal.supportunit.util.SupportExprNodeFactory;
import junit.framework.TestCase;

public class TestExprInNode extends TestCase {
    private ExprInNode inNodeNormal;
    private ExprInNode inNodeNotIn;

    public void setUp() throws Exception {
        inNodeNormal = SupportExprNodeFactory.makeInSetNode(false);
        inNodeNotIn = SupportExprNodeFactory.makeInSetNode(true);
    }

    public void testGetType() throws Exception {
        EPType type = EPTypePremade.BOOLEANBOXED.getEPType();
        assertEquals(type, inNodeNormal.getForge().getEvaluationType());
        assertEquals(type, inNodeNotIn.getForge().getEvaluationType());
    }

    public void testValidate() throws Exception {
        inNodeNormal = SupportExprNodeFactory.makeInSetNode(true);
        inNodeNormal.validate(SupportExprValidationContextFactory.makeEmpty());

        // No subnodes: Exception is thrown.
        tryInvalidValidate(new ExprInNodeImpl(true));

        // singe child node not possible, must be 2 at least
        inNodeNormal = new ExprInNodeImpl(true);
        inNodeNormal.addChildNode(new SupportExprNode(4));
        tryInvalidValidate(inNodeNormal);

        // test a type mismatch
        inNodeNormal = new ExprInNodeImpl(true);
        inNodeNormal.addChildNode(new SupportExprNode("sx"));
        inNodeNormal.addChildNode(new SupportExprNode(4));
        tryInvalidValidate(inNodeNormal);
    }

    public void testEvaluate() throws Exception {
        assertFalse((Boolean) inNodeNormal.getForge().getExprEvaluator().evaluate(makeEvent(0), false, null));
        assertTrue((Boolean) inNodeNormal.getForge().getExprEvaluator().evaluate(makeEvent(1), false, null));
        assertTrue((Boolean) inNodeNormal.getForge().getExprEvaluator().evaluate(makeEvent(2), false, null));
        assertFalse((Boolean) inNodeNormal.getForge().getExprEvaluator().evaluate(makeEvent(3), false, null));

        assertTrue((Boolean) inNodeNotIn.getForge().getExprEvaluator().evaluate(makeEvent(0), false, null));
        assertFalse((Boolean) inNodeNotIn.getForge().getExprEvaluator().evaluate(makeEvent(1), false, null));
        assertFalse((Boolean) inNodeNotIn.getForge().getExprEvaluator().evaluate(makeEvent(2), false, null));
        assertTrue((Boolean) inNodeNotIn.getForge().getExprEvaluator().evaluate(makeEvent(3), false, null));
    }

    public void testEquals() throws Exception {
        ExprInNode otherInNodeNormal = SupportExprNodeFactory.makeInSetNode(false);
        ExprInNode otherInNodeNotIn = SupportExprNodeFactory.makeInSetNode(true);

        assertTrue(inNodeNormal.equalsNode(otherInNodeNormal, false));
        assertTrue(inNodeNotIn.equalsNode(otherInNodeNotIn, false));

        assertFalse(inNodeNormal.equalsNode(otherInNodeNotIn, false));
        assertFalse(inNodeNotIn.equalsNode(otherInNodeNormal, false));
        assertFalse(inNodeNotIn.equalsNode(SupportExprNodeFactory.makeCaseSyntax1Node(), false));
        assertFalse(inNodeNormal.equalsNode(SupportExprNodeFactory.makeCaseSyntax1Node(), false));
    }

    public void testToExpressionString() throws Exception {
        assertEquals("s0.intPrimitive in (1,2)", ExprNodeUtilityPrint.toExpressionStringMinPrecedenceSafe(inNodeNormal));
        assertEquals("s0.intPrimitive not in (1,2)", ExprNodeUtilityPrint.toExpressionStringMinPrecedenceSafe(inNodeNotIn));
    }

    private EventBean[] makeEvent(int intPrimitive) {
        SupportBean theEvent = new SupportBean();
        theEvent.setIntPrimitive(intPrimitive);
        return new EventBean[]{SupportEventBeanFactory.createObject(theEvent)};
    }

    private void tryInvalidValidate(ExprInNode exprInNode) throws Exception {
        try {
            exprInNode.validate(SupportExprValidationContextFactory.makeEmpty());
            fail();
        } catch (ExprValidationException ex) {
            // expected
        }
    }
}
