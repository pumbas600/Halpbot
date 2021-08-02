package nz.pumbas.halpbot.commands;

import java.util.List;

import nz.pumbas.commands.annotations.Children;
import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.validation.Implicit;
import nz.pumbas.halpbot.HalpBot;
import nz.pumbas.halpbot.customparameters.electrical.CircuitManager;
import nz.pumbas.halpbot.customparameters.electrical.ElectricalComponent;
import nz.pumbas.halpbot.customparameters.electrical.PowerSupply;
import nz.pumbas.halpbot.customparameters.electrical.Resistor;

public class ElectricalCommands
{
    private final CircuitManager manager = new CircuitManager();

    @Command(alias = "solve", description = "Solves a series circuit with the specified electrical components")
    public String solve(
        @Children({PowerSupply.class, Resistor.class}) @Implicit List<ElectricalComponent> components)
    {
        this.manager.solve(components);
        return this.manager.buildCircuitOutput(components);
    }

    @Command(alias = "A2Q3", description = "Solves Assignment 2 Question 3", restrictedTo = HalpBot.CREATOR_ID)
    public String a2q3(double supplyCurrent, double r1, double r2, double r3, double r4) {
        // Voltage between A and B (Part1)
        double resistanceA = r1 + r3;
        double resistanceB = r2 + r4;
        double currentThroughA = supplyCurrent * (1/resistanceA) / ((1/resistanceA) + (1/resistanceB));
        double currentThroughB = supplyCurrent - currentThroughA;

        double voltageAcrossAB = currentThroughB * r2 - currentThroughA * r1;

        // Current through A-B (Part 2)
        currentThroughA = supplyCurrent * (1/r1) / ((1/r1) + (1/r2));
        double currentThroughAG = supplyCurrent * (1/r3) / ((1/r3) + (1/r4));

        double currentThroughAB = currentThroughA - currentThroughAG;

        return String.format("```V_ab: %.10f\nI_ab: %.10f```", voltageAcrossAB, currentThroughAB);
    }

    @Command(alias = "A2Q5", description = "Solves Assignment 2 Question 5", restrictedTo = HalpBot.CREATOR_ID)
    public String a2q5(double v1, double v2, double v3, double r1, double r2, double r3) {
        double numerator = (v1/r1) + (v2/r2) + (v3/r3);
        double denominator = (1/r1) + (1/r2) + (1/r3);
        double voltageAtA = numerator / denominator;

        double currentThrough1 = (voltageAtA - v1) / r1;
        double currentThrough2 = (voltageAtA - v2) / r2;
        double currentThrough3 = (voltageAtA - v3) / r3;

        return String.format(
                "```V_a = %.10f\n" +
                   "I_1 = %.10f\n" +
                   "I_2 = %.10f\n" +
                   "I_3 = %.10f```",
                voltageAtA, currentThrough1, currentThrough2, currentThrough3);
    }

    @Command(alias = "A2Q6", description = "Solves Assignment 2 Question 6", restrictedTo = HalpBot.CREATOR_ID)
    public String a2q6(double v1, double currentSource, double r1, double r2, double r3) {
        double numerator = currentSource + (v1 / r1);
        double denominator = (1/r1) + (1/r3);
        double voltageAtA = numerator / denominator;

        double currentThrough1 = (v1 - voltageAtA) / r1;

        return String.format("```V_a = %.10f\nI_1 = %.10f```", voltageAtA, currentThrough1);
    }
}
