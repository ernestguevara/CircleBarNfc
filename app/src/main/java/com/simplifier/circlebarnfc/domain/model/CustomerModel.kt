package com.simplifier.circlebarnfc.domain.model

data class CustomerModel(
    var customerName: String? = null,
    var customerTier: Int? = null,
    var customerAddress: String? = null,
    var customerVisitCount: Int? = null,
    var customerBalance: Double? = null,
    var customerFlag: Int? = null
)