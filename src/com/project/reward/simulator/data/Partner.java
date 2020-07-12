package com.project.reward.simulator.data;

import java.util.ArrayList;
import java.util.List;

public class Partner {
    private Long Id;

    private List<Partner> children;

    public Partner() {
        children = new ArrayList<>();
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Partner[] getChildren() {
        return this.children.toArray(new Partner[0]);
    }

    public void addChild(Partner child) {
        this.children.add(child);
    }
}
