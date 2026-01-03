package com.expatledger.tenants.application

case class OnboardTenantRequest(
    name: String,
    reportingCurrency: String,
    initialTaxResidency: String
)
