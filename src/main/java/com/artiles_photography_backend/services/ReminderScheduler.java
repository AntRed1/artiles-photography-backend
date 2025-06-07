/*
 * The MIT License
 *
 * Copyright 2025 arojas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.artiles_photography_backend.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.artiles_photography_backend.models.Appointment;
import com.artiles_photography_backend.models.Configuration;
import com.artiles_photography_backend.repository.ConfigurationRepository;

import jakarta.mail.MessagingException;

/**
 * Schedules appointment reminders with a configurable cron expression.
 *
 * @author arojas
 */
@Component
public class ReminderScheduler implements SchedulingConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(ReminderScheduler.class);
    private static final String DEFAULT_CRON = "0 0 8 * * *"; // Default: 8:00 AM daily

    private final AppointmentService appointmentService;
    private final EmailService emailService;
    private final ConfigurationRepository configurationRepository;

    private volatile String currentCron = DEFAULT_CRON;

    @Autowired
    public ReminderScheduler(AppointmentService appointmentService, EmailService emailService,
            @Lazy ConfigurationRepository configurationRepository) {
        this.appointmentService = appointmentService;
        this.emailService = emailService;
        this.configurationRepository = configurationRepository;
        logger.info("ReminderScheduler initialized with default cron: {}", DEFAULT_CRON);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                this::sendReminders,
                triggerContext -> {
                    String cron = getCronExpression();
                    if (!currentCron.equals(cron)) {
                        logger.info("Cron expression updated to: {}", cron);
                        currentCron = cron;
                    }
                    return new CronTrigger(cron).nextExecution(triggerContext); // Eliminamos toInstant()
                });
    }

    @Transactional
    public void sendReminders() {
        logger.info("Executing appointment reminder task...");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = now.plusHours(24); // Reminders 24 hours before
        List<Appointment> appointments = appointmentService.getAppointmentsForReminders(reminderTime);

        if (appointments.isEmpty()) {
            logger.info("No upcoming appointments for reminders.");
            return;
        }

        for (Appointment appointment : appointments) {
            try {
                emailService.sendAppointmentReminder(appointment);
                appointment.setReminderSent(true);
                appointmentService.updateAppointment(appointment.getId(), appointment);
                logger.info("Reminder sent for appointment ID: {}", appointment.getId());
            } catch (MessagingException | IOException e) {
                logger.error("Error sending reminder for appointment ID: {}: {}", appointment.getId(), e.getMessage(),
                        e);
            }
        }
        logger.info("Reminder task completed. Total sent: {}", appointments.size());
    }

    private String getCronExpression() {
        return configurationRepository.findAll().stream()
                .findFirst()
                .map(config -> {
                    String cron = config.getReminderCron();
                    return (cron != null && !cron.trim().isEmpty()) ? cron : DEFAULT_CRON;
                })
                .orElse(DEFAULT_CRON);
    }

    public void updateCron(String newCron) {
        logger.info("Updating reminder cron to: {}", newCron);
        Configuration config = configurationRepository.findAll().stream()
                .findFirst()
                .orElse(new Configuration());
        config.setReminderCron(newCron);
        configurationRepository.save(config);
        currentCron = newCron != null && !newCron.trim().isEmpty() ? newCron : DEFAULT_CRON;
    }
}