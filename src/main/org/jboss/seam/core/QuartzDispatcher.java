package org.jboss.seam.core;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.Date;
import java.rmi.server.UID;
import java.io.Serializable;

import javax.interceptor.Interceptors;

import org.quartz.SchedulerFactory;
import org.quartz.Scheduler;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobDataMap;
import org.quartz.Trigger;
import org.quartz.SimpleTrigger;
import org.quartz.CronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.ejb.SeamInterceptor;
import org.jboss.seam.intercept.InvocationContext;

/**
 * Dispatcher implementation that uses the Quartz library.
 * 
 * @author Michael Yuan
 *
 */
@Scope(ScopeType.APPLICATION)
@Name("org.jboss.seam.core.dispatcher")
@Interceptors(SeamInterceptor.class)
@Install(value=false, precedence=BUILT_IN)
public class QuartzDispatcher extends AbstractDispatcher<QuartzDispatcher.QuartzTriggerHandle, TimerSchedule>
{
   
   private static final LogProvider log = Logging.getLogProvider(QuartzDispatcher.class);
   private static Scheduler scheduler;

   @Create
   public void initScheduler () 
   {
     SchedulerFactory schedulerFactory = new org.quartz.impl.StdSchedulerFactory();
     try {
       scheduler = schedulerFactory.getScheduler();
       scheduler.start();
       log.info("The QuartzDispatcher has started");
     } catch (SchedulerException se) {
       log.error("Cannot get or start a Quartz Scheduler");
       se.printStackTrace ();
     }
   }

   public QuartzTriggerHandle scheduleAsynchronousEvent(String type, Object... parameters)
   {  
      String jobName = nextUniqueName();
      String triggerName = nextUniqueName();
      
      JobDetail jobDetail = new JobDetail(jobName, null, QuartzJob.class);
      jobDetail.getJobDataMap().put("async", new AsynchronousEvent(type, parameters));
       
      SimpleTrigger trigger = new SimpleTrigger (triggerName, null);
      
      log.info("In the scheduleAsynchronousEvent()");

      try {
        scheduler.scheduleJob( jobDetail, trigger );
        return new QuartzTriggerHandle(triggerName);
      } catch (SchedulerException se) {
        log.error("Cannot Schedule a Quartz Job");
        se.printStackTrace ();
        return null;
      }
   }
    
   public QuartzTriggerHandle scheduleTimedEvent(String type, TimerSchedule schedule, Object... parameters)
   {
      log.info("In the scheduleTimedEvent()");
      try {
        return scheduleWithQuartzService( schedule, new AsynchronousEvent(type, parameters) );
      } catch (SchedulerException se) {
        log.error("Cannot Schedule a Quartz Job");
        se.printStackTrace ();
        return null;
      }
   }
   
   public QuartzTriggerHandle scheduleInvocation(InvocationContext invocation, Component component)
   {
      log.info("In the scheduleInvocation()");
      try {
        return scheduleWithQuartzService( 
               createSchedule(invocation), 
               new AsynchronousInvocation(invocation, component)
            );
      } catch (SchedulerException se) {
        log.error("Cannot Schedule a Quartz Job");
        se.printStackTrace ();
        return null;
      }
   }
      
   private static Date calculateDelayedDate (long delay)
   {
     Date now = new Date ();
     now.setTime(now.getTime() + delay);
     return now;
   }

   private QuartzTriggerHandle scheduleWithQuartzService(TimerSchedule schedule, Asynchronous async) throws SchedulerException
   {
      log.info("In the scheduleWithQuartzService()");
      
      String jobName = nextUniqueName();
      String triggerName = nextUniqueName();
      
      JobDetail jobDetail = new JobDetail(jobName, null, QuartzJob.class);
      jobDetail.getJobDataMap().put("async", async);

      if ( schedule.getCron()!=null )
      {
        try {
          CronTrigger trigger = new CronTrigger (triggerName, null);
          trigger.setCronExpression(schedule.getCron());
          if ( schedule.getExpiration()!=null )
          {
            trigger.setStartTime (schedule.getExpiration());
          }
          else if ( schedule.getDuration()!=null )
          {
            trigger.setStartTime (calculateDelayedDate(schedule.getDuration()));
          }

          scheduler.scheduleJob( jobDetail, trigger );

          return new QuartzTriggerHandle (triggerName);

        } catch (Exception e) {
          log.error ("Cannot submit cron job, fall back to fixed interval");
          e.printStackTrace ();

          // The method does not return, and execution flow follows
        }
      }

      if ( schedule.getIntervalDuration()!=null )
      {
         if ( schedule.getExpiration()!=null )
         {
            SimpleTrigger trigger = new SimpleTrigger (triggerName, null, schedule.getExpiration(), null, SimpleTrigger.REPEAT_INDEFINITELY, schedule.getIntervalDuration());
            scheduler.scheduleJob( jobDetail, trigger );

         }
         else if ( schedule.getDuration()!=null )
         {
             SimpleTrigger trigger = new SimpleTrigger (triggerName, null, calculateDelayedDate(schedule.getDuration()), null, SimpleTrigger.REPEAT_INDEFINITELY, schedule.getIntervalDuration());
             scheduler.scheduleJob( jobDetail, trigger );

         }
         else
         {
            SimpleTrigger trigger = new SimpleTrigger (triggerName, null, SimpleTrigger.REPEAT_INDEFINITELY, schedule.getIntervalDuration());
            scheduler.scheduleJob( jobDetail, trigger );

         }
      }
      else if ( schedule.getExpiration()!=null )
      {
          SimpleTrigger trigger = new SimpleTrigger (triggerName, null, schedule.getExpiration());
          scheduler.scheduleJob( jobDetail, trigger );

      }
      else if ( schedule.getDuration()!=null )
      { 
          SimpleTrigger trigger = new SimpleTrigger (triggerName, null, calculateDelayedDate(schedule.getDuration()));
          scheduler.scheduleJob( jobDetail, trigger );

      }
      else
      {
         SimpleTrigger trigger = new SimpleTrigger (triggerName, null);
         scheduler.scheduleJob( jobDetail, trigger );

      }
      return new QuartzTriggerHandle (triggerName);
   }
   
   private String nextUniqueName ()
   {
      return (new UID()).toString();
   }
   
   @Destroy
   public void destroy()
   {
      log.info("The QuartzDispatcher is shut down");
      try {
        scheduler.shutdown();
      } catch (SchedulerException se) {
        log.error("Cannot shutdown the Quartz Scheduler");
        se.printStackTrace ();
      }
      
   }
   
   public static class QuartzJob implements Job
   {
      private AbstractDispatcher.Asynchronous async;
      
      public QuartzJob() { }

      public void execute(JobExecutionContext context)
          throws JobExecutionException
      {
         log.info("Start executing Quartz job");
         JobDataMap dataMap = context.getJobDetail().getJobDataMap();
         async = (AbstractDispatcher.Asynchronous)dataMap.get("async");
         async.execute(null);
         log.info("End executing Quartz job");
      }
   }
   
   public static class QuartzTriggerHandle implements Serializable
   {
      private String triggerName;
        
      public QuartzTriggerHandle(String triggerName) 
      {
         this.triggerName = triggerName; 
      }

      public void cancel () throws SchedulerException
      {
         log.info("Cancel executing Quartz job");
         scheduler.unscheduleJob(triggerName, null);
      }
      
      public void pause () throws SchedulerException
      {
         log.info("Pause executing Quartz job");
         scheduler.pauseTrigger (triggerName, null);
         
      }
      
      public void resume () throws SchedulerException
      {
         log.info("Resume executing Quartz job");
         scheduler.resumeTrigger (triggerName, null);
      }
   }

}
