package com.project.reward.simulator.data;

import java.time.LocalDate;
import java.util.Objects;

public class Contract {

    public enum ContractAction {
        BEGIN, END;
    }

    public enum ContractType {
        Rabbit, Tortoise;
    }

    private long id;

    private Partner partner;

    private ContractType type;

    private LocalDate startDate;

    private LocalDate endDate;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Partner getPartner() {
        return this.partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public ContractType getType() {
        return this.type;
    }

    public void setType(ContractType type) {
        this.type = type;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj) || !Contract.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        final Contract other = (Contract) obj;
        return this.id == other.id
                && this.partner.getId().equals(other.partner.getId())
                && this.type.equals(other.type);
    }
}
