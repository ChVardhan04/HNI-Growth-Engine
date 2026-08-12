package com.hnigrowth.provider;

import com.hnigrowth.campaign.Campaign;
import com.hnigrowth.lead.Lead;

import java.util.List;

/** Abstraction over "wherever raw leads come from" (LinkedIn today, Apollo/Clay tomorrow). */
public interface LeadProvider {
    List<Lead> fetchLeads(Campaign campaign);
}
