package org.noear.solon.extend.quartz;


import org.noear.solon.Utils;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.util.TextUtil;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class JobManager {
    static Scheduler _server = null;
    static Map<String, JobEntity> jobMap = new HashMap<>();

    protected static void init() throws Exception {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();

        _server = schedulerFactory.getScheduler();
    }

    protected static void start() throws Exception {
        _server.start();
    }

    protected static void stop() throws Exception {

        if (_server != null) {
            _server.shutdown();

            _server = null;
        }
    }

    protected static void doAddBean(String name, String cronx, boolean enable, BeanWrap bw) throws Exception {
        if (enable == false) {
            return;
        }

        if (Runnable.class.isAssignableFrom(bw.clz()) || Job.class.isAssignableFrom(bw.clz())) {
            JobManager.addJob(new JobEntity(name, cronx, enable, bw));
        }
    }

    public static JobEntity getJob(String jobID) {
        if (TextUtil.isEmpty(jobID)) {
            return null;
        } else {
            return jobMap.get(jobID);
        }
    }

    /**
     *
     */
    public static void addJob(JobEntity jobEntity) throws Exception {
        jobMap.putIfAbsent(jobEntity.jobID, jobEntity);

        if (jobEntity.cronx.indexOf(" ") < 0) {
            if (jobEntity.cronx.endsWith("ms")) {
                long period = Long.parseLong(jobEntity.cronx.substring(0, jobEntity.cronx.length() - 2));
                addFuture(jobEntity, period, TimeUnit.MILLISECONDS);
            } else if (jobEntity.cronx.endsWith("s")) {
                long period = Long.parseLong(jobEntity.cronx.substring(0, jobEntity.cronx.length() - 1));
                addFuture(jobEntity, period, TimeUnit.SECONDS);
            } else if (jobEntity.cronx.endsWith("m")) {
                long period = Long.parseLong(jobEntity.cronx.substring(0, jobEntity.cronx.length() - 1));
                addFuture(jobEntity, period, TimeUnit.MINUTES);
            } else if (jobEntity.cronx.endsWith("h")) {
                long period = Long.parseLong(jobEntity.cronx.substring(0, jobEntity.cronx.length() - 1));
                addFuture(jobEntity, period, TimeUnit.HOURS);
            } else if (jobEntity.cronx.endsWith("d")) {
                long period = Long.parseLong(jobEntity.cronx.substring(0, jobEntity.cronx.length() - 1));
                addFuture(jobEntity, period, TimeUnit.DAYS);
            }
        } else {
            addSchedule(jobEntity, jobEntity.cronx);
        }
    }

    private static void addSchedule(JobEntity jobEntity, String cronx) throws Exception {
        JobDetail jobDetail = JobBuilder.newJob(QuartzProxy.class)
                .withIdentity(jobEntity.jobID, "solon")
                .usingJobData("__jobID", jobEntity.jobID)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobEntity.jobID, "solon")
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(cronx))
                .build();

        _server.scheduleJob(jobDetail, trigger);
    }

    private static void addFuture(JobEntity jobEntity, long period, TimeUnit unit) throws Exception {
        SimpleScheduleBuilder ssb = SimpleScheduleBuilder.simpleSchedule();
        switch (unit) {
            case MILLISECONDS:
                ssb.withIntervalInMilliseconds(period);
                break;
            case SECONDS:
                ssb.withIntervalInSeconds((int) period);
                break;
            case MINUTES:
                ssb.withIntervalInMinutes((int) period);
                break;
            case HOURS:
                ssb.withIntervalInHours((int) period);
                break;
            case DAYS:
                ssb.withIntervalInSeconds((int) period);
                break;
            default:
                return;
        }

        JobDetail jobDetail = JobBuilder.newJob(QuartzProxy.class)
                .withIdentity(jobEntity.jobID, "solon")
                .usingJobData("__jobID", jobEntity.jobID)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobEntity.jobID, "solon")
                .startNow()
                .withSchedule(ssb.repeatForever())
                .build();

        _server.scheduleJob(jobDetail, trigger);
    }
}
