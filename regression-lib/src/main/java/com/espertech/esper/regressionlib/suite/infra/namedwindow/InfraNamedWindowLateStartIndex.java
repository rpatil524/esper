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
package com.espertech.esper.regressionlib.suite.infra.namedwindow;

import com.espertech.esper.common.internal.support.SupportBean_S0;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.framework.RegressionFlag;
import com.espertech.esper.regressionlib.framework.RegressionPath;
import com.espertech.esper.regressionlib.support.bean.SupportCountAccessEvent;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

/**
 * NOTE: More namedwindow-related tests in "nwtable"
 */
public class InfraNamedWindowLateStartIndex implements RegressionExecution {
    @Override
    public EnumSet<RegressionFlag> flags() {
        return EnumSet.of(RegressionFlag.EXCLUDEWHENINSTRUMENTED);
    }

    public void run(RegressionEnvironment env) {
        // prepare
        RegressionPath path = preloadData(env, false);

        // test join
        String eplJoin = "@name('s0') select * from SupportBean_S0 as s0 unidirectional, AWindow(p00='x') as aw where aw.id = s0.id";
        env.compileDeploy(eplJoin, path).addListener("s0");
        assertEquals(2, SupportCountAccessEvent.getAndResetCountGetterCalled());

        env.sendEventBean(new SupportBean_S0(-1, "x"));
        env.assertListenerInvoked("s0");

        // test subquery no-index-share
        String eplSubqueryNoIndexShare = "@name('s1') select (select id from AWindow(p00='x') as aw where aw.id = s0.id) " +
            "from SupportBean_S0 as s0 unidirectional";
        env.compileDeploy(eplSubqueryNoIndexShare, path).addListener("s1");
        assertEquals(2, SupportCountAccessEvent.getAndResetCountGetterCalled());

        env.sendEventBean(new SupportBean_S0(-1, "x"));
        env.undeployAll();

        // test subquery with index share
        path = preloadData(env, true);

        String eplSubqueryWithIndexShare = "@name('s2') select (select id from AWindow(p00='x') as aw where aw.id = s0.id) " +
            "from SupportBean_S0 as s0 unidirectional";
        env.compileDeploy(eplSubqueryWithIndexShare, path).addListener("s2");
        assertEquals(2, SupportCountAccessEvent.getAndResetCountGetterCalled());

        env.sendEventBean(new SupportBean_S0(-1, "x"));
        env.assertListenerInvoked("s2");

        env.undeployAll();
    }

    private static RegressionPath preloadData(RegressionEnvironment env, boolean indexShare) {
        RegressionPath path = new RegressionPath();
        String createEpl = "@public create window AWindow#keepall as SupportCountAccessEvent";
        if (indexShare) {
            createEpl = "@Hint('enable_window_subquery_indexshare') " + createEpl;
        }

        env.compileDeploy(createEpl, path);
        env.compileDeploy("insert into AWindow select * from SupportCountAccessEvent", path);
        env.compileDeploy("create index I1 on AWindow(p00)", path);
        SupportCountAccessEvent.getAndResetCountGetterCalled();
        for (int i = 0; i < 100; i++) {
            env.sendEventBean(new SupportCountAccessEvent(i, "E" + i));
        }
        env.sendEventBean(new SupportCountAccessEvent(-1, "x"));
        assertEquals(101, SupportCountAccessEvent.getAndResetCountGetterCalled());
        return path;
    }
}
