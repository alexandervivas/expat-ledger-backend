package com.expatledger.tenants.service

case class OnboardTenantRequest(
    name: String,
    reportingCurrency: String,
    initialTaxResidency: String
)
