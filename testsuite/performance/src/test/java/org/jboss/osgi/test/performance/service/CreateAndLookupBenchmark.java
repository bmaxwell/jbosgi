/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.osgi.test.performance.service;

import java.util.Arrays;
import java.util.Date;

import org.jboss.osgi.test.performance.AbstractThreadedBenchmark;
import org.jboss.osgi.test.performance.ChartType;
import org.jboss.osgi.test.performance.ChartTypeImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class CreateAndLookupBenchmark extends AbstractThreadedBenchmark<Integer>
{
   private static final ChartType REGISTRATION = new ChartTypeImpl("REG", "Service Registration Time", "Number", "Time");
   private static final ChartType LOOKUP = new ChartTypeImpl("LKU", "Service Lookup Time", "Number", "Time");
   
   @SuppressWarnings("unchecked")
   private static final Class<TestClass>[] CLASSES = new Class[] { TestClass1.class, TestClass2.class, TestClass3.class, TestClass4.class, TestClass5.class,
         TestClass6.class, TestClass7.class, TestClass8.class, TestClass9.class, TestClass10.class, TestClass11.class, TestClass12.class, TestClass13.class,
         TestClass14.class, TestClass15.class, TestClass16.class, TestClass17.class, TestClass18.class, TestClass19.class, TestClass20.class };

   public CreateAndLookupBenchmark(BundleContext context)
   {
      super(context);
   }

   @Override
   protected ChartType[] getAllChartTypes()
   {
      return new ChartType[] { REGISTRATION, LOOKUP };
   }

   public void run(int numthreads, int numservices) throws Exception
   {
      runTest(numthreads, numservices);
   }

   @Override
   protected void runThread(String threadName, Integer numServicesPerThread) throws Exception
   {
      long[] serviceIDs = new long[numServicesPerThread];

      System.out.println("Starting at " + new Date());
      long regStart = System.currentTimeMillis();
      // Service Registrations
      for (int i = 0; i < numServicesPerThread; i++)
      {
         TestClass svc = TestClass.createInst(CLASSES[i % CLASSES.length], threadName + i);
         ServiceRegistration reg = bundleContext.registerService(svc.getClass().getName(), svc, null);
         serviceIDs[i] = (Long)reg.getReference().getProperty(Constants.SERVICE_ID);
      }
      long regEnd = System.currentTimeMillis();
      writeData(REGISTRATION, numServicesPerThread, regEnd - regStart);
      System.out.println("Registered Services " + new Date());

      long lkuStart = System.currentTimeMillis();
      // Lookup & Invoke Services
      for (int i = 0; i < numServicesPerThread; i++)
      {
         ServiceReference[] srs = bundleContext.getServiceReferences(CLASSES[i % CLASSES.length].getName(), "(" + Constants.SERVICE_ID + "=" + serviceIDs[i] + ")");
         if (srs.length != 1)
            throw new IllegalStateException("Should only have found 1 service: " + Arrays.toString(srs));

         TestClass ti = (TestClass)bundleContext.getService(srs[0]);
         if (!ti.toString().equals(threadName + i))
            throw new IllegalStateException("Wrong service used, expected: " + i + " but got " + ti);
      }
      long lkuEnd = System.currentTimeMillis();
      writeData(LOOKUP, numServicesPerThread, lkuEnd - lkuStart);
      System.out.println("Invoked Services " + new Date());
   }
}