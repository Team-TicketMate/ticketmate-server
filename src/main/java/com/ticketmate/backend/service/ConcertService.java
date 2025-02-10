package com.ticketmate.backend.service;

import com.ticketmate.backend.repository.postgres.ConcertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;
}
