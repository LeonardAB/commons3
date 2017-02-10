package commons3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Dipta Mahardhika
 */
public class Commons3 {

    static final int ITERATION = 10;
    static final int POP_SIZE = 200;
    public static final String POPULATION_FILE = "population_auto.csv";
    public static List<Agent> population = new ArrayList<>();

    public static void main(String[] args) {
        Scanner task = new Scanner(System.in);
        String taskNum;
        System.out.println("Task list");
        System.out.println("1. experiment ground");
        System.out.println("2. build population file");
        System.out.println("3. build network file");
        System.out.println("4. simulate");
        System.out.print("which task do you want to do?");
        taskNum = task.next();

        if (taskNum.equals("1")) {
            System.out.println("EXPERIMENT GROUND");
            //put something here to try / test
       
        } else if (taskNum.equals("2")) {
            System.out.println("BUILDING POPULATION-FILE");
            generatePopulation();
        } else if (taskNum.equals("3")) {
            System.out.println("BUILDING NETWORK-FILE");
            Scanner networkGeneratorScan = new Scanner(System.in);
            System.out.print("what is the name of the population file that you want to use?");
            String popFileName = networkGeneratorScan.next();
            System.out.println("how many partners for one agent?");
            int numOfPartners = Integer.parseInt(networkGeneratorScan.next());
//         int lowLim = 35, upLim = 50; //for now the variation of number of partners are arbitrarily chosen (inspired by the 3rd layer of dunbar intimacy gradient)
//         int numOfPartners = ThreadLocalRandom.current().nextInt(lowLim, upLim); 
            generateNetworkFile(popFileName, numOfPartners);
            System.out.println("Network file DONE");
        } else {
            System.out.println("MAIN SIMULATION");
            System.out.println("Working Directory = " + System.getProperty("user.dir"));

            Scanner simScan = new Scanner(System.in);
            System.out.print("what is the name of the population file that you want to use?");
            String popFileName = simScan.next();
            System.out.print("what is the name of the network file that you want to use?");
            String networkFileName = simScan.next();
//        System.out.print("how many times do you want this simulation to be repeated?");
//        int simRepeat = Integer.parseInt(simScan.next());

            loadPopulationFromFile(popFileName);
            loadPartnerFromfile(networkFileName);
            File resultFile = SupportTool.notOverwriteFileName("result.csv");
            File worldState = SupportTool.notOverwriteFileName("world.csv");

            SupportTool.writeToCsv(resultFile, "iteration, "+ITERATION);
            SupportTool.writeToCsv(resultFile, "pop_size, "+POP_SIZE);
            SupportTool.writeToCsv(resultFile, "LNK_strat, "+Agent.LNK_STRAT);
            SupportTool.writeToCsv(resultFile, "L2agg_strat, "+Agent.L2_AGG_STRAT);
            SupportTool.writeToCsv(resultFile, "dec_strat, "+Agent.DEC_STRAT);
            SupportTool.writeToCsv(resultFile, "popname, "+popFileName);
            SupportTool.writeToCsv(resultFile, "netname, "+networkFileName);
            SupportTool.writeToCsv(resultFile, "COMMF, "+Agent.COMMF);
            SupportTool.writeToCsv(resultFile, "");
            SupportTool.writeToCsv(resultFile, "agent,iteration,layer1,layer2,decision, payoff, norm or self, change");
            SupportTool.writeToCsv(worldState, "iteration, occupied land ratio, profit factor");
            int decChg;
            Random randSeeder = new Random();
            double rand;
            for (int i = 0; i < ITERATION; i++) {

                for (Agent agent: population) {
                    agent.calculateLastKnownNorm();
                    agent.calculateProfit();
                    agent.calculateAddition();
                    agent.setLayer1();
                }
                rand = randSeeder.nextDouble();
                for (Agent agent: population) {
                    agent.setLayer2(rand);
                }
                rand = randSeeder.nextDouble();
                for (Agent agent: population) {
                    agent.setLayer3(rand);
                    agent.decide();
                }

                World.calculateOccupiedLandRatio(population);
                World.calculateProfitFactor();
                System.out.println("-------------------World.profitFactor = " + World.profitFactor);
                SupportTool.writeToCsv(worldState, i + "," + World.occupiedLandRatio + "," + World.profitFactor);

                for (Agent agent: population) {
                    decChg = abs(agent.decision-agent.prevArea);
                    SupportTool.writeToCsv(resultFile, agent.name + "," + i + "," + agent.layer1 + "," + agent.layer2 + "," + agent.decision + "," + agent.decision * World.profitFactor + "," + agent.normOrSelf+ "," + decChg);
                }
            }
        }
    }

    private static void generatePopulation() {
        Random randSeeder = new Random();
        double rand;
        File outFile = SupportTool.notOverwriteFileName(POPULATION_FILE);
        String agentData;
        final Double OPP_AGENT_RATIO = 0.3; //opportunist
        final Double CONF_AGENT_RATIO = 0.4; //balanced agent ratio: 1-opp-conf
        String agentType;
        int countNormTreshold, agentCom, initArea;

        SupportTool.writeToCsv(outFile, "Name,Type,Communicative,Count Norm treshold, Init Area");
        for (int i = 0; i < POP_SIZE; i++) {
            rand = randSeeder.nextDouble();
            if (rand <= CONF_AGENT_RATIO) {
                agentType = "conformist";
            } else if (rand > 1 - OPP_AGENT_RATIO) {
                agentType = "opportunist";
            } else {
                agentType = "balanced";
            }

            agentCom = 1;
            initArea = ThreadLocalRandom.current().nextInt(1, 40 + 1); //in the beginning, agents will have various size of land between 1-40
            countNormTreshold = ThreadLocalRandom.current().nextInt(15, 20 + 1); //this is the most possible value for range of 20
            agentData = SupportTool.threeDigitHex(i) + "," + agentType + "," + agentCom + "," + countNormTreshold + "," + initArea;
            SupportTool.writeToCsv(outFile, agentData);
        }
    }

    public static void loadPopulationFromFile(String popFileName) {
        BufferedReader fileReader = null;

        try {
            String line;
            fileReader = new BufferedReader(new FileReader(popFileName));

            //Read the CSV file header to skip it
            fileReader.readLine();

            //Read the file line by line starting from the second line
            while ((line = fileReader.readLine()) != null) {

                String[] tokens = line.split(",");
                if (tokens.length > 0) {

                    Agent agent = new Agent(
                            tokens[0], //name
                            tokens[1], //type
                            SupportTool.intToBool(Integer.parseInt(tokens[2])), //communicative or not

                            Integer.parseInt(tokens[3]), //count norm treshold
                            Integer.parseInt(tokens[4]) //initial area size
                    );
                    population.add(agent);
                }
            }

            //Print the new agent list
            for (Agent agent: population) {
                System.out.println(agent.toString());
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileReader !!!");
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                System.out.println("Error while closing fileReader !!!");
                e.printStackTrace();
            }
        }
        System.out.println("load population from file succedeed");
    }

    public static void generateNetworkFile(String popFileName, int numOfPartners) {

        loadPopulationFromFile(popFileName);
        String networkFileHeader = "agent";
        for (int i = 1; i <= numOfPartners; i++) {
            networkFileHeader += ",partner" + i;

        }
        File networkFile = SupportTool.notOverwriteFileName("network_for_" + popFileName.substring(0, popFileName.length() - 4) + "_.csv");
        SupportTool.writeToCsv(networkFile, networkFileHeader);

        for (Agent agent: population) {
            agent.setNRandomPartners(population, numOfPartners);
            String partnerList = agent.name;
            for (Agent partner: agent.partners) {
                partnerList += "," + partner.name;
            }
            SupportTool.writeToCsv(networkFile, partnerList);
        }

    }

    public static void generateNetworkFile(String popFileName, int lowLim, int upLim) {
        int numOfPartners;
        loadPopulationFromFile(popFileName);
        String networkFileHeader = "agent";
        for (int i = 1; i <= upLim; i++) {
            networkFileHeader += ",partner" + i;

        }
        File networkFile = SupportTool.notOverwriteFileName("network_for_" + popFileName.substring(0, popFileName.length() - 4) + "_.csv");
        SupportTool.writeToCsv(networkFile, networkFileHeader);

        for (Agent agent: population) {
            numOfPartners = ThreadLocalRandom.current().nextInt(lowLim, upLim);
            agent.setNRandomPartners(population, numOfPartners);
            String partnerList = agent.name;
            for (Agent partner: agent.partners) {
                partnerList += "," + partner.name;
            }
            SupportTool.writeToCsv(networkFile, partnerList);
        }

    }

    public static void loadPartnerFromfile(String networkFileName) {
        BufferedReader fileReader = null;

        try {

            String line;
            fileReader = new BufferedReader(new FileReader(networkFileName));

            //Read the CSV file header to skip it
            fileReader.readLine();

            //Read the file line by line starting from the second line
            while ((line = fileReader.readLine()) != null) {

                String[] tokens = line.split(",");
//                System.out.println("tokens size = " + tokens.length);
                if (tokens.length > 0) {

                    for (Agent agent: population) {
//                        System.out.println("agent.name = " + agent.name);
                        if (agent.name.equals(tokens[0])) {
                            for (int i = 1; i <= tokens.length - 1; i++) {  //token length is the number of partners + the agent itself
                                boolean match = false;
                                for (Agent partner: population) {
                                    match = partner.name.equals(tokens[i]);
//                               System.out.println("partner.name = " + partner.name);
//                               System.out.println("tokens["+i+"] = " + tokens[i]);
                                    if (match) {
//                                   System.out.println("partner.name for " + agent.name+"="+partner.name);
                                        agent.partners.add(partner);
                                        break;
                                    }
                                }

                            }

                            break;
                        }
                    }
                }
            }

            //Print the partner list
            for (Agent agent: population) {
                System.out.println(agent.toString());
            }
        } catch (Exception e) {
            System.out.println("Error in partner CsvFileReader !!!");
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                System.out.println("Error while closing fileReader !!!");
                e.printStackTrace();
            }

        }
        System.out.println("load partner from file succedeed");
    }

}
