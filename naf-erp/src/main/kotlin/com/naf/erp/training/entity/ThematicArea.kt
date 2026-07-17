package com.naf.erp.training.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name="thematic_areas")
class ThematicArea(

    @Id
    var code:String="",

    var description:String=""
)