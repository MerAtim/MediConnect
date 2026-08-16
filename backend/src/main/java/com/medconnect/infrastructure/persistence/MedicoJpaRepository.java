package com.medconnect.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicoJpaRepository extends JpaRepository<MedicoEntity, Long> {
}
