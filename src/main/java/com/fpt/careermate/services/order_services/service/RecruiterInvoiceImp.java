package com.fpt.careermate.services.order_services.service;

import com.fpt.careermate.common.constant.StatusInvoice;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.common.util.CoachUtil;
import com.fpt.careermate.services.order_services.domain.RecruiterInvoice;
import com.fpt.careermate.services.order_services.domain.RecruiterPackage;
import com.fpt.careermate.services.order_services.repository.RecruiterInvoiceRepo;
import com.fpt.careermate.services.order_services.repository.RecruiterPackageRepo;
import com.fpt.careermate.services.order_services.service.dto.response.MyRecruiterInvoiceResponse;
import com.fpt.careermate.services.order_services.service.impl.RecruiterInvoiceService;
import com.fpt.careermate.services.order_services.service.mapper.RecruiterInvoiceMapper;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RecruiterInvoiceImp implements RecruiterInvoiceService {

    RecruiterInvoiceRepo recruiterInvoiceRepo;
    RecruiterPackageRepo recruiterPackageRepo;
    CoachUtil coachUtil;
    RecruiterInvoiceMapper recruiterInvoiceMapper;

//    @Transactional
    public void creatInvoice(String packageName, Recruiter currentRecruiter) {
        RecruiterPackage pkg = recruiterPackageRepo.findByName(packageName);

        RecruiterInvoice invoice = new RecruiterInvoice();
        invoice.setRecruiter(currentRecruiter);
        invoice.setRecruiterPackage(pkg);
        invoice.setAmount(pkg.getPrice());
        invoice.setStatus(StatusInvoice.PAID);
        invoice.setActive(true);
        invoice.setStartDate(LocalDate.now());
        invoice.setEndDate(LocalDate.now().plusDays(pkg.getDurationDays()));

        recruiterInvoiceRepo.save(invoice);
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public void cancelMyInvoice() {
        Recruiter currentRecruiter = coachUtil.getCurrentRecruiter();
        RecruiterInvoice invoice = recruiterInvoiceRepo.findByRecruiter_IdAndIsActiveTrue(currentRecruiter.getId())
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_INVOICE_NOT_FOUND));

        if (invoice.isActive()) {
            invoice.setStatus(StatusInvoice.CANCELLED);
            invoice.setCancelledAt(LocalDate.now());
            invoice.setActive(false);
            recruiterInvoiceRepo.save(invoice);
        }
        else {
            throw new AppException(ErrorCode.CANNOT_DELETE_MY_RECRUITER_INVOICE);
        }
    }

    public RecruiterPackage getPackageByName(String packageName){
        return recruiterPackageRepo.findByName(packageName);
    }

    public void updateRecruiterInvoice(RecruiterInvoice exstingInvoice, String packageName){
        RecruiterPackage pkg = recruiterPackageRepo.findByName(packageName);

        exstingInvoice.setRecruiterPackage(pkg);
        exstingInvoice.setAmount(pkg.getPrice());
        exstingInvoice.setStatus(StatusInvoice.PAID);
        exstingInvoice.setActive(true);
        exstingInvoice.setStartDate(LocalDate.now());
        exstingInvoice.setEndDate(LocalDate.now().plusDays(pkg.getDurationDays()));
        exstingInvoice.setCancelledAt(null);

        recruiterInvoiceRepo.save(exstingInvoice);
    }

    // Check if recruiter has an active order
    public boolean hasActivePackage() {
        Recruiter currentRecruiter = coachUtil.getCurrentRecruiter();
        Optional<RecruiterInvoice> activeOrder =
                recruiterInvoiceRepo.findByRecruiter_IdAndIsActiveTrue(currentRecruiter.getId());
        return activeOrder.isPresent();
    }

    // Get my active invoice by recruiter id
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public MyRecruiterInvoiceResponse getMyActiveInvoice() {
        Recruiter currentRecruiter = coachUtil.getCurrentRecruiter();
        Optional<RecruiterInvoice> exsting =
                recruiterInvoiceRepo.findByRecruiter_IdAndIsActiveTrue(currentRecruiter.getId());

        if(exsting.isEmpty()) throw new AppException(ErrorCode.RECRUITER_INVOICE_NOT_FOUND);

        return recruiterInvoiceMapper.toMyRecruiterInvoiceResponse(exsting.get());
    }
}
