package com.project.reward.simulator.controller;

import com.project.reward.simulator.data.Contract;
import com.project.reward.simulator.data.Contract.ContractType;
import com.project.reward.simulator.data.Contract.ContractAction;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;

public class ContractHandler {

    private static final ContractHandler instance = new ContractHandler();

    private final HashMap<Long, Contract> contractsById = new HashMap<>();

    private ContractHandler() {
    }

    private Contract generateContractByRow(String[] row) throws IOException {
        // Check if we have correct number of elements for this row
        if (row.length != 5) {
            throw new IOException(
                    String.format("Invalid number of elements %d. Each row must contain %d elements",
                            row.length, 5)
            );
        }

        Contract contract = new Contract();

        try {
            // Validate partner id
            Long partnerId = Long.parseLong(row[0]);
            if (!PartnerHandler.getInstance().getPartners().containsKey(partnerId)) {
                throw new IOException("Partner not found with id=" + partnerId);
            }
            contract.setPartner(PartnerHandler.getInstance().getPartners().get(partnerId));

            // Validate contract id
            contract.setId(Long.parseLong(row[1]));

            // Validate contract type
            ContractType type = ContractType.valueOf(row[2]);
            contract.setType(type);

            // Validate contract date and action
            LocalDate date = LocalDate.parse(row[3], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            ContractAction action = ContractAction.valueOf(row[4]);
            if (action.equals(ContractAction.BEGIN)) {
                contract.setStartDate(date);
            } else {
                contract.setEndDate(date);
            }
        } catch (NumberFormatException | IOException ex) {
            throw new IOException(
                    String.format("Validation error for row %s: %s", String.join(",", row), ex.getMessage()),
                    ex
            );
        }

        return contract;
    }

    private void addOrUpdateContract(Contract newContract) throws IOException {
        // Update contract if it's already stored, otherwise add it to the list
        if (contractsById.containsKey(newContract.getId())) {
            // The only way we could have duplicate contractId is when we want to
            // declare the termination date, so end_date for the old contract should be null.
            // Otherwise it's not allowed.
            if (contractsById.get(newContract.getId()).equals(newContract)
                    && Objects.isNull(contractsById.get(newContract.getId()).getEndDate())
                    && Objects.nonNull(newContract.getEndDate())) {
                contractsById.get(newContract.getId()).setEndDate(newContract.getEndDate());
            } else {
                throw new IOException("Duplicate contract found with the same id: " + newContract.getId());
            }
        } else if (Objects.isNull(newContract.getStartDate())) {
            // We shouldn't create new contract without a start date
            throw new IOException("New contract id must have a start date: " + newContract.getId());
        } else {
            contractsById.put(newContract.getId(), newContract);
        }
    }

    public static ContractHandler getInstance() {
        return instance;
    }

    public HashMap<Long, Contract> getContractsById(){
        return contractsById;
    }

    public void loadContracts(String csvFilePath) throws IOException {
        try {
            File csv = new File(csvFilePath);
            if (!csv.exists()) {
                throw new IOException("File not found: " + csvFilePath);
            }

            BufferedReader br = new BufferedReader(new FileReader(csv));
            String line = br.readLine(); // Skipping csv header
            while ((line = br.readLine()) != null) {
                if (line.equals("")) { // Skip empty lines
                    continue;
                }
                String[] row = line.split(",");
                addOrUpdateContract(
                        generateContractByRow(row)
                );
            }
        } catch (IOException ex) {
            throw new IOException(ex.getMessage(), ex);
        }
    }
}
