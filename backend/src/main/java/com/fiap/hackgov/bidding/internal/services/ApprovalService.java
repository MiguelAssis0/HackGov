package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.entities.Approval;
import com.fiap.hackgov.bidding.internal.repositories.ApprovalRepository;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ApprovalService {

    @Autowired
    private ApprovalRepository approvalRepository;


    public Approval findById(UUID id) {
        return approvalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Approval not found:" + id));
    }

    public Approval save(Approval approval) {
        return approvalRepository.save(approval);
    }

}
