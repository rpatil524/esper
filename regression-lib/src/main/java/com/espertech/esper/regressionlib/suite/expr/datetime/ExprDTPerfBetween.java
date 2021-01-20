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
package com.espertech.esper.regressionlib.suite.expr.datetime;

import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.framework.RegressionFlag;
import com.espertech.esper.regressionlib.framework.RegressionPath;
import com.espertech.esper.regressionlib.support.bean.SupportDateTime;
import com.espertech.esper.regressionlib.support.bean.SupportTimeStartEndA;

import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

public class ExprDTPerfBetween implements RegressionExecution {

    @Override
    public EnumSet<RegressionFlag> flags() {
        return EnumSet.of(RegressionFlag.EXCLUDEWHENINSTRUMENTED);
    }

    public void run(RegressionEnvironment env) {

        RegressionPath path = new RegressionPath();
        env.compileDeploy("@public create window AWindow#keepall as A", path);
        env.compileDeploy("insert into AWindow select * from A", path);

        // preload
        for (int i = 0; i < 10000; i++) {
            env.sendEventBean(SupportTimeStartEndA.make("A" + i, "2002-05-30T09:00:00.000", 100), "A");
        }
        env.sendEventBean(SupportTimeStartEndA.make("AEarlier", "2002-05-30T08:00:00.000", 100), "A");
        env.sendEventBean(SupportTimeStartEndA.make("ALater", "2002-05-30T10:00:00.000", 100), "A");

        String epl = "@name('s0') select a.key as c0 from SupportDateTime unidirectional, AWindow as a where longdate.between(longdateStart, longdateEnd, false, true)";
        env.compileDeploy(epl, path).addListener("s0");

        // query
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            env.sendEventBean(SupportDateTime.make("2002-05-30T08:00:00.050"));
            env.assertEqualsNew("s0", "c0", "AEarlier");
        }
        long endTime = System.currentTimeMillis();
        long delta = endTime - startTime;
        assertTrue("Delta=" + delta / 1000d, delta < 500);

        env.sendEventBean(SupportDateTime.make("2002-05-30T10:00:00.050"));
        env.assertEqualsNew("s0", "c0", "ALater");

        env.undeployAll();
    }
}
