package com.project.reward.simulator.cli;

import com.project.reward.simulator.controller.ContractHandler;
import com.project.reward.simulator.controller.PartnerHandler;
import com.project.reward.simulator.controller.Statistics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;

// Simple interface to be able to handle incoming commands easily
interface Command {
    Integer runCommand(String[] args);
}

public class CommandHandler {

    public final String ANSI_RESET = "\u001B[0m";
    public final String ANSI_RED = "\u001B[31m";
    public final String ANSI_YELLOW = "\u001B[33m";

    private final HashMap<String, Command> commands = new HashMap<>() {
        {
            put("REGISTER", args -> handleRegister(args));
            put("LOAD", args -> handleLoad(args));
            put("LEVEL", args -> handleLevel(args));
            put("REWARDS", args -> handleRewards(args));
            put("ALL_REWARDS", args -> handleAllRewards(args));
        }
    };

    private void logError(String msg) {
        System.out.println(ANSI_RED + msg + ANSI_RESET);
    }

    private void logInfo(String msg) {
        System.out.println(ANSI_YELLOW + msg + ANSI_RESET);
    }

    private Integer handleRegister(String[] args) {
        if (args.length < 1 || args.length > 2) {
            logError("command REGISTER takes only 1 or 2 arguments.");
            return 0;
        }
        try {
            long partnerId, parentPartnerId = -1;
            partnerId = Long.parseLong(args[0]);
            if (args.length == 2) {
                parentPartnerId = Long.parseLong(args[1]);
            }

            PartnerHandler.getInstance().registerPartner(partnerId, parentPartnerId);
            return 1;
        } catch (Exception ex) {
            logError("Registration was not successful: " + ex.getMessage());
            return 0;
        }
    }

    private Integer handleLoad(String[] args) {
        if (args.length != 1) {
            logError("command LOAD takes exactly 1 argument");
            return 0;
        }
        try {
            ContractHandler.getInstance().loadContracts(args[0]);
            return 1;
        } catch (Exception ex) {
            logError("Loading was not successful: " + ex.getMessage());
            return 0;
        }
    }

    private Integer handleLevel(String[] args) {
        if (args.length != 3) {
            logError("command LEVEL takes exactly 3 arguments.");
            return 0;
        }
        try {
            long partnerId;
            int year, quarter;
            partnerId = Long.parseLong(args[0]);
            year = Integer.parseInt(args[1]);
            if(year < 1000 || year > 5000) {
                logError("Invalid year.");
                return 0;
            }
            quarter = Integer.parseInt(args[2]);
            if(quarter < 1 || quarter > 4) {
                logError("Invalid quarter.");
                return 0;
            }

            String level = Statistics.getPartnerLevel(partnerId, year, quarter);
            logInfo(String.format("%d --> '%s'", partnerId, level));
            return 1;
        } catch (Exception ex) {
            logError("Fetching level was not successful: " + ex.getMessage());
            return 0;
        }
    }

    private Integer handleRewards(String[] args) {
        if (args.length != 3) {
            logError("command REWARDS takes exactly 3 arguments.");
            return 0;
        }
        try {
            long partnerId;
            int year, quarter;
            partnerId = Long.parseLong(args[0]);
            year = Integer.parseInt(args[1]);
            if(year < 1000 || year > 5000) {
                logError("Invalid year.");
                return 0;
            }
            quarter = Integer.parseInt(args[2]);
            if(quarter < 1 || quarter > 4) {
                logError("Invalid quarter.");
                return 0;
            }

            long reward = Statistics.getPartnerReward(partnerId, year, quarter);
            logInfo(String.format("%d --> %d €", partnerId, reward));

            return 1;
        } catch (Exception ex) {
            logError("Calculating reward was not successful: " + ex.getMessage());
            return 0;
        }
    }

    private Integer handleAllRewards(String[] args) {
        if (args.length != 1) {
            logError("command REWARDS takes exactly 1 arguments.");
            return 0;
        }
        try {
            long partnerId;
            partnerId = Long.parseLong(args[0]);

            String res = Statistics.getPartnerAllReward(partnerId);
            logInfo(res);

            return 1;
        } catch (Exception ex) {
            logError("Calculating all rewards was not successful: " + ex.getMessage());
            return 0;
        }
    }

    private Integer processInput(String input) {
        String[] args = input.split(" ");

        String cmd = args[0];
        if (!commands.containsKey(cmd)) {
            logError("Invalid command '" + cmd + "'");
            return 0;
        }

        return commands.get(cmd).runCommand(
                Arrays.copyOfRange(args, 1, args.length)
        );
    }

    public void run() throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        System.in));
        String input;
        while ((input = in.readLine()) != null) {
            processInput(input);
        }

    }

    public void runTest() {
        String basePath = CommandHandler.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        String[] testCommands = new String[] {
                "REGISTER 1",
                "REGISTER 2 1",
                "REGISTER 3 2",
                "LOAD " + basePath + "com/project/reward/simulator/resources/data.csv",
                "LEVEL 1 2011 1",
                "LEVEL 2 2011 1",
                "LEVEL 3 2011 1",
                "REWARDS 1 2006 3",
                "REWARDS 2 2010 1",
                "REWARDS 3 2009 1",
                "REWARDS 3 2011 1",
                "ALL_REWARDS 1",
                "ALL_REWARDS 2",
                "ALL_REWARDS 3",
        };

        for (int i = 0 ; i < testCommands.length ; i++) {
            System.out.println(testCommands[i]);
            Integer res = processInput(testCommands[i]);
            if (res == 0) {
                break;
            }
        }
    }
}
