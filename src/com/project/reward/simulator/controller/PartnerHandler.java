package com.project.reward.simulator.controller;

import com.project.reward.simulator.data.Partner;

import java.util.HashMap;

public class PartnerHandler {

    private static final PartnerHandler instance = new PartnerHandler();

    private HashMap<Long, Partner> registeredPartners = new HashMap<>();

    private PartnerHandler() {
    }

    public static PartnerHandler getInstance() {
        return instance;
    }

    public HashMap<Long, Partner> getPartners() {
        return registeredPartners;
    }

    public void registerPartner(Long id, Long parentId) throws Exception {
        Partner p = new Partner();
        p.setId(id);
        if (parentId > 0) {
            if (!registeredPartners.containsKey(parentId)) {
                throw new Exception("Parent ID not found: " + parentId);
            }
            registeredPartners.get(parentId).addChild(p);
        }
        registeredPartners.put(id, p);
    }
}
