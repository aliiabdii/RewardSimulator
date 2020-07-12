package com.project.reward.simulator.controller;

import com.project.reward.simulator.data.Contract;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.*;

public class Statistics {

    private static final Integer CONTRACT_REWARD_EXPIRY_TIME = 8; // Reward for contract will expire x years after start date

    private static final Integer RABBIT_BONUS = 50;

    private enum RewardLevel {
        None(0), Ant(5), Bee(7), Cat(9), Dog(12), Elephant(15);

        private final Integer rewardPerContract;

        RewardLevel(Integer reward) {
            this.rewardPerContract = reward;
        }

        public Integer getRewardPerContract() {
            return this.rewardPerContract;
        }
    }

    /**
     * Function to get partner level based on number of active contracts
     * @param numOfContracts
     * Number of active contracts
     * @return RewardLevel
     * Reward level
     */
    private static RewardLevel getLevel(long numOfContracts) {
        if (numOfContracts >= 1000) {
            return RewardLevel.Elephant;
        } else if (numOfContracts >= 200) {
            return RewardLevel.Dog;
        } else if (numOfContracts >= 50) {
            return RewardLevel.Cat;
        } else if (numOfContracts >= 10) {
            return RewardLevel.Bee;
        } else if (numOfContracts >= 1) {
            return RewardLevel.Ant;
        } else {
            return RewardLevel.None;
        }
    }

    /***
     * Recursive function to find all the eligible partner ids to be included in
     * calculation (meaning partner itself and all its sub-partners)
     * @param parentId
     * Id of the parent partner (root in the tree)
     * @return List<Long>
     */
    private static List<Long> getEligiblePartnerIds(Long parentId) {
        List<Long> eligiblePartnerIds = new ArrayList<>(Collections.singletonList(parentId));
        Arrays.stream(
                PartnerHandler.getInstance().getPartners().get(parentId).getChildren()
        ).forEach(c -> {
            eligiblePartnerIds.addAll(getEligiblePartnerIds(c.getId()));
        });
        return eligiblePartnerIds;
    }

    /**
     * Function to get all the active contracts for the given partnerIds.
     * Active means non-ended non-expired contracts.
     * @param partnerIds
     * @param year
     * @param quarter
     * @return Contract[]
     */
    private static Contract[] getActiveContracts(List<Long> partnerIds, Integer year, Integer quarter) {
        LocalDate validFrom = LocalDate.of(year - CONTRACT_REWARD_EXPIRY_TIME, 3 * quarter - 2, 1);
        LocalDate initial = LocalDate.of(year, 3 * quarter, 1);
        LocalDate validTo = initial.withDayOfMonth(initial.lengthOfMonth());

        return ContractHandler.getInstance().getContractsById().values()
                .stream()
                .filter(c -> partnerIds.contains(c.getPartner().getId())
                        && c.getStartDate().isAfter(validFrom)
                        && c.getStartDate().isBefore(validTo)
                        && (Objects.isNull(c.getEndDate()) || c.getEndDate().isAfter(validTo))
                ).toArray(Contract[]::new);
    }

    /**
     * Function to get the partner level for given partner in a specific year and quarter
     * @param partnerId
     * @param year
     * @param quarter
     * @return String
     * @throws Exception
     */
    public static String getPartnerLevel(Long partnerId, Integer year, Integer quarter) throws Exception {
        if (!PartnerHandler.getInstance().getPartners().containsKey(partnerId)) {
            throw new Exception("Partner id not found: " + partnerId);
        }
        List<Long> eligiblePartnerIds = getEligiblePartnerIds(partnerId);
        long count = getActiveContracts(eligiblePartnerIds, year, quarter).length;
        return getLevel(count).name();
    }

    /**
     * Function to get total reward for a given partner in a given year and quarter
     * @param partnerId
     * @param year
     * @param quarter
     * @return long
     * @throws Exception
     */
    public static long getPartnerReward(Long partnerId, Integer year, Integer quarter) throws Exception {
        if (!PartnerHandler.getInstance().getPartners().containsKey(partnerId)) {
            throw new Exception("Partner id not found: " + partnerId);
        }
        long reward = 0;

        // Find the level of the partner
        List<Long> eligiblePartnerIds = getEligiblePartnerIds(partnerId);
        Contract[] activeContracts = getActiveContracts(eligiblePartnerIds, year, quarter);
        RewardLevel partnerLevel = getLevel(activeContracts.length);

        // Sum-up reward for direct contracts (made by partner itself and in the same quarter)
        Contract[] directContracts = Arrays.stream(activeContracts)
                .filter(c -> c.getStartDate().get(IsoFields.QUARTER_OF_YEAR) == quarter
                        && c.getPartner().getId().equals(partnerId))
                .toArray(Contract[]::new);
        reward += directContracts.length * partnerLevel.getRewardPerContract();

        // Sum-up one-time bonus for new rabit contracts
        long numOfNewDirectRabbitContracts = Arrays.stream(directContracts)
                .filter(c -> c.getStartDate().getYear() == year
                        && c.getType().equals(Contract.ContractType.Rabbit))
                .count();
        reward += numOfNewDirectRabbitContracts * RABBIT_BONUS;

        // Sum-up reward for all the children
        reward += Arrays.stream(PartnerHandler.getInstance().getPartners().get(partnerId).getChildren())
                .mapToLong(p -> {
                    Contract[] activeContractsForChild = getActiveContracts(getEligiblePartnerIds(p.getId()), year, quarter);
                    RewardLevel childLevel = getLevel(activeContractsForChild.length);

                    Contract[] directChildContracts = Arrays.stream(activeContractsForChild)
                            .filter(c -> c.getStartDate().get(IsoFields.QUARTER_OF_YEAR) == quarter
                                    && c.getPartner().getId().equals(p.getId()))
                            .toArray(Contract[]::new);
                    long rewardDifference = partnerLevel.getRewardPerContract() - childLevel.getRewardPerContract();
                    return directChildContracts.length * rewardDifference;
                }).sum();

        return reward;
    }

    /**
     * Function to get the reward for a given partner id, for all possible years and quarters
     * @param partnerId
     * @return String
     * Contains all the possible rewards, line by line
     * @throws Exception
     */
    public static String getPartnerAllReward(Long partnerId) throws Exception {
        StringBuilder res = new StringBuilder();

        // We want to show the reward for all years and quarters, so we need to sort all the contracts
        // based on create date and pick the first and last one to have a more accurate range.
        Contract[] allContracts = ContractHandler.getInstance().getContractsById().values()
                .stream()
                .sorted(Comparator.comparing(Contract::getStartDate))
                .toArray(Contract[]::new);
        if (allContracts.length == 0) {
            return res.toString();
        }
        LocalDate start = allContracts[0].getStartDate();
        LocalDate end = allContracts[allContracts.length - 1].getStartDate();

        // Show reward for all the possible quarters in our year range
        for(int year = start.getYear() ; year <= end.getYear() ; year++) {
            for (int quarter = 1 ; quarter <= 4 ; quarter++) {
                long r = getPartnerReward(partnerId, year, quarter);
                res.append(String.format("%d %d %d\n", year, quarter, r));
            }
        }

        return res.toString();
    }

}
