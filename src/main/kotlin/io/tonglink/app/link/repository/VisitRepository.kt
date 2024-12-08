package io.tonglink.app.link.repository

import io.tonglink.app.link.entity.Visit
import org.springframework.data.jpa.repository.JpaRepository

interface VisitRepository : JpaRepository<Visit, Long>