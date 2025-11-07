package com.ticketmate.backend.member.infrastructure.repository;

import com.ticketmate.backend.member.core.vo.Phone;
import com.ticketmate.backend.member.infrastructure.entity.PhoneBlock;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneBlockRepository extends JpaRepository<PhoneBlock, UUID> {

  Optional<PhoneBlock> findByPhone(Phone phone);
}
