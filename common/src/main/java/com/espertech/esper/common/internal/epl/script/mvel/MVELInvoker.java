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
package com.espertech.esper.common.internal.epl.script.mvel;

import com.espertech.esper.common.client.EPException;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.settings.ClasspathImportService;
import com.espertech.esper.common.internal.util.ClassHelperGenericType;
import com.espertech.esper.common.internal.util.JavaClassHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class MVELInvoker {

    private static Class mvelClass;
    private static Class parserContextClass;
    private static Class execStatementClass;
    private static Method executeExpressionMethod;

    public static boolean isMVELInClasspath(ClasspathImportService classpathImportService) {
        if (mvelClass == null) {
            init(classpathImportService);
        }
        return assertClasses();
    }

    public static void analysisCompile(String expression, Object parserContext, ClasspathImportService classpathImportService) throws InvocationTargetException {
        assertClasspath(classpathImportService);
        Method method;
        try {
            method = mvelClass.getMethod("analysisCompile", new Class[]{String.class, parserContextClass});
        } catch (Exception e) {
            throw new EPException("Failed to find MVEL method: " + e.getMessage(), e);
        }

        try {
            method.invoke(null, new Object[]{expression, parserContext});
        } catch (IllegalAccessException e) {
            throw new EPException("Failed to access MVEL method: " + e.getMessage(), e);
        }
    }

    public static Object compileExpression(String expression, Object parserContext) throws InvocationTargetException {
        Method method;
        try {
            method = mvelClass.getMethod("compileExpression", new Class[]{String.class, parserContextClass});
        } catch (Exception e) {
            throw new EPException("Failed to find MVEL method: " + e.getMessage(), e);
        }

        try {
            return method.invoke(null, new Object[]{expression, parserContext});
        } catch (IllegalAccessException e) {
            throw new EPException("Failed to access MVEL method: " + e.getMessage(), e);
        }
    }

    public static Object newParserContext(ClasspathImportService classpathImportService) {
        assertClasspath(classpathImportService);

        try {
            return parserContextClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new EPException("Failed to instantiate MVEL ParserContext: " + e.getMessage(), e);
        }
    }

    public static Map<String, Class> getParserContextInputs(Object parserContext) {
        Method method;
        try {
            method = parserContextClass.getMethod("getInputs", new Class[0]);
            return (Map<String, Class>) method.invoke(parserContext, null);
        } catch (Exception e) {
            throw new EPException("Failed to find MVEL method: " + e.getMessage(), e);
        }
    }

    public static Object executeExpression(Object executable, Map<String, Object> parameters) throws InvocationTargetException {
        try {
            return executeExpressionMethod.invoke(null, new Object[]{executable, parameters});
        } catch (IllegalAccessException e) {
            throw new EPException("Failed to find MVEL method: " + e.getMessage(), e);
        }
    }

    public static void setParserContextStrongTyping(Object parserContext) {
        Method method;
        try {
            method = parserContextClass.getMethod("setStrongTyping", boolean.class);
            method.invoke(parserContext, true);
        } catch (Exception e) {
            throw new EPException("Failed to find MVEL method: " + e.getMessage(), e);
        }
    }

    public static void setParserContextInputs(Object parserContext, Map<String, Class> mvelInputParamTypes) {
        Method method;
        try {
            method = parserContextClass.getMethod("setInputs", Map.class);
            method.invoke(parserContext, mvelInputParamTypes);
        } catch (Exception e) {
            throw new EPException("Failed to find MVEL method: " + e.getMessage(), e);
        }
    }

    public static EPTypeClass getExecutableStatementKnownReturnType(Object compiled) {
        Method method;
        try {
            method = execStatementClass.getMethod("getKnownEgressType", null);
            Class clazz = (Class) method.invoke(compiled, null);
            return ClassHelperGenericType.getClassEPType(clazz);
        } catch (Exception e) {
            throw new EPException("Failed to find MVEL method: " + e.getMessage(), e);
        }
    }

    private static void init(ClasspathImportService classpathImportService) {
        mvelClass = JavaClassHelper.getClassInClasspath("org.mvel2.MVEL", classpathImportService.getClassForNameProvider());
        parserContextClass = JavaClassHelper.getClassInClasspath("org.mvel2.ParserContext", classpathImportService.getClassForNameProvider());
        execStatementClass = JavaClassHelper.getClassInClasspath("org.mvel2.compiler.ExecutableStatement", classpathImportService.getClassForNameProvider());
        if (mvelClass != null) {
            try {
                executeExpressionMethod = mvelClass.getMethod("executeExpression", new Class[]{Object.class, Map.class});
            } catch (NoSuchMethodException e) {
                throw new EPException("Failed to find MVEL method: " + e.getMessage(), e);
            }
        }
    }

    private static void assertClasspath(ClasspathImportService classpathImportService) {
        if (mvelClass == null) {
            init(classpathImportService);
        }
        if (!assertClasses()) {
            throw new IllegalStateException("Failed to find MVEL in classpath");
        }
    }

    private static boolean assertClasses() {
        return mvelClass != null && parserContextClass != null;
    }
}
