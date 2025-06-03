package com.ptithcm.portal.service;

import com.ptithcm.portal.entity.DotDangKy;
import com.ptithcm.portal.entity.HocKy;
import com.ptithcm.portal.repository.DangKyRepository;
import com.ptithcm.portal.repository.DotDangKyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationUpdateService {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationUpdateService.class);
    private static final String TRANG_THAI_CHUA_XAC_NHAN = "Chua xac nhan";
    private static final String TRANG_THAI_DA_XAC_NHAN = "Da xac nhan";

    @Autowired
    private DotDangKyRepository dotDangKyRepository;

    @Autowired
    private DangKyRepository dangKyRepository;

    /**
     * Scheduled task to update registration statuses.
     * The cron expression is sourced from application.properties (jobs.registrationUpdate.cronExpression).
     * Finds registration periods (DotDangKy) that have ended and not yet processed.
     * For each such period, it updates the status of associated student registrations (DangKy)
     * from "Chua xac nhan" to "Da xac nhan".
     */
    @Scheduled(cron = "${jobs.cron}") // Cron expression from properties
    @Transactional
    public void updateRegistrationStatuses() {
        logger.info("Scheduled task: Starting updateRegistrationStatuses at {}", LocalDateTime.now());
        LocalDateTime currentTime = LocalDateTime.now();

        List<DotDangKy> unprocessedPeriods = dotDangKyRepository.findEndedAndUnprocessed(currentTime);

        if (unprocessedPeriods.isEmpty()) {
            logger.info("Scheduled task: No unprocessed registration periods found that have ended.");
            return;
        }

        logger.info("Scheduled task: Found {} unprocessed registration periods to process.", unprocessedPeriods.size());

        for (DotDangKy period : unprocessedPeriods) {
            HocKy hocKy = period.getHocKy();
            if (hocKy == null) {
                logger.warn("Scheduled task: DotDangKy with ID {} has no associated HocKy. Skipping.", period.getId());
                continue;
            }

            logger.info("Scheduled task: Processing DotDangKy ID: {}, HocKy ID: {}", period.getId(), hocKy.getId());

            try {
                int updatedCount = dangKyRepository.updateStatusByHocKyAndOldStatus(
                        hocKy,
                        TRANG_THAI_CHUA_XAC_NHAN,
                        TRANG_THAI_DA_XAC_NHAN
                );
                logger.info("Scheduled task: Updated {} DangKy records from '{}' to '{}' for HocKy ID: {}",
                        updatedCount, TRANG_THAI_CHUA_XAC_NHAN, TRANG_THAI_DA_XAC_NHAN, hocKy.getId());

                period.setProcessed(true);
                dotDangKyRepository.save(period);
                logger.info("Scheduled task: Marked DotDangKy ID: {} as processed.", period.getId());

            } catch (Exception e) {
                logger.error("Scheduled task: Error processing DotDangKy ID: {}. Error: {}", period.getId(), e.getMessage(), e);
                // Depending on requirements, you might want to rethrow, or handle specific exceptions
                // to prevent the period from being marked as processed if the update failed.
            }
        }
        logger.info("Scheduled task: Finished updateRegistrationStatuses at {}", LocalDateTime.now());
    }
}
