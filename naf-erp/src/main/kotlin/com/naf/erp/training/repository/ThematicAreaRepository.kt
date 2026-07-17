package com.naf.erp.training.repository

import com.naf.erp.training.entity.ThematicArea
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ThematicAreaRepository : JpaRepository<ThematicArea, String>
