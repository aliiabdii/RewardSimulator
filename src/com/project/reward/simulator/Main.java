package com.project.reward.simulator;

import com.project.reward.simulator.cli.CommandHandler;

public class Main {

    public static void main(String[] args){
        CommandHandler handler = new CommandHandler();
        try {
            // Replace runTest() with run() to get the core command handler
            handler.runTest();
        } catch (Exception ex) {
            System.out.println("Error while running command handler: " + ex.getMessage());
        }
    }
}
