package com.fiap.hackgov.bidding.internal.services;

import com.fiap.hackgov.bidding.internal.entities.ETP;
import com.fiap.hackgov.bidding.internal.repositories.ETPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ETPService {

    @Autowired
    private ETPRepository etpRepository;

    public ETP save(ETP etp) {
        return etpRepository.save(etp);
    }

}
