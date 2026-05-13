package com.fiap.hackgov.bidding.internal.services;


import com.fiap.hackgov.bidding.internal.entities.ProcessHistory;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.enums.HistoryEventType;
import com.fiap.hackgov.bidding.internal.entities.enums.ProcessStage;
import com.fiap.hackgov.bidding.internal.repositories.ProcessHistoryRepository;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProcessHistoryService {

    private final ProcessHistoryRepository processHistoryRepository;

    public void createProcessHistory(Requisition requisition, Employee employee, String observation, ProcessStage stage, HistoryEventType eventType) {

        ProcessHistory history = new ProcessHistory();

        history.setRequisition(requisition);
        history.setStage(stage);
        history.setEventType(eventType);
        history.setChangedBy(employee);
        history.setObservation(observation);
        history.setChangedAt(LocalDateTime.now());

        processHistoryRepository.save(history);
    }

}








