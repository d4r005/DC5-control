package com.naf.erp.training.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name="occupation_catalog")
class OccupationCatalog(

    @Id
    var code:String="",

    var description:String=""
)