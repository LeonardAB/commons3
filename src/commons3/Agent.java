package commons3;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import static java.lang.Math.*;

/**
 *
 * @author Dipta Mahardhika
 */
public class Agent {

    static double COMMF = 0.75; //communication factor. 1 means no noise, all communication are succesfil
    static String LNK_STRAT = "AVG";
    static String L2_AGG_STRAT = "AVG";
    static String DEC_STRAT = "MAJ3";

    public static void setLNK_STRAT(String LNK_STRAT) {
        Agent.LNK_STRAT = LNK_STRAT;
    }

    public static void setL2_AGG_STRAT(String L2_AGG_STRAT) {
        Agent.L2_AGG_STRAT = L2_AGG_STRAT;
    }

    public static void setDEC_STRAT(String DEC_STRAT) {
        Agent.DEC_STRAT = DEC_STRAT;
    }

    public static void setCOMMF(double COMMF) {
        Agent.COMMF = COMMF;
    }
    
    
    
 //   Random randSeeder;
    static Random randomizer = new Random();
    String name, //name of agent
            type; //opportunist, balance, conformer
    int countNormTreshold; // treshold value if number of agents that do this action is above this treshold, it will be a CADIDATE for norm
    List<Agent> partners;
    int decision; //amount of land agent decides to occupy in the next iteration
    int currentArea, //amount of land that is occupied in the current iteration by the agent
            prevArea, //amount of land that was occupied in the previous iteration
            addArea, //amount of land that want to be added for the next iteration
            addAreaMax; //opportunist 3, balance 2, conformer 1;
    double maxProfit;
    int layer1, //amount of land agent wanst to occupy (rationaly)
            layer2; //amount of land agent belief the society wants to occupy
    Norm layer2Norm, //norm candidate distribution in layer 2
            layer3Norm; //norm candidate distribution in layer 3
    int lastKnownNorm;  // the amount of land that the agent believe is the normal amount to be occupied by the society
    boolean communicative; //determine whether this agent will tell status of his layers to his partner or not
    double[] lastProfits = new double[20]; //to store max profit in last twenty iteration.
                                           //set to be 20 to represent adaptation. (not the whole lifetime)
    double rand;
    String normOrSelf;

    public Agent(String name, String type, boolean communicative, int countNormTreshold, int initArea) {
        this.name = name;
        this.type = type;
        this.countNormTreshold = countNormTreshold;
        this.currentArea = initArea;
        this.prevArea = 0;
        this.communicative = communicative;
        switch (type) {
            case "opportunist":
                this.addAreaMax = 3;
                break;
            case "balanced":
                this.addAreaMax = 2;
                break;
            case "conformist":
                this.addAreaMax = 1;
                break;
            default:
                throw new AssertionError();
        }

        for (int i = 0; i < lastProfits.length; i++) {
            lastProfits[i] = 0;
        }
        this.partners = new ArrayList();
        this.layer2Norm = new Norm();
        this.layer3Norm = new Norm();
        this.maxProfit = 0;
  //      this.randSeeder = new Random();
    }

    public void setNRandomPartners(List<Agent> lst, int n) { //given the popList, choose partners for this agent
        List<Agent> copy = new LinkedList<>(lst);
        copy.remove(this);
        Collections.shuffle(copy);
        this.partners = copy.subList(0, n);
    }

    public int beliefAboutL1(Agent partner, double rand) {
        int beliefAboutPartner;
        if (rand <= COMMF && this.partners.contains(partner)) {
            beliefAboutPartner = partner.layer1;
        } else {
            beliefAboutPartner = this.lastKnownNorm;
        }
        return beliefAboutPartner;
    }

    public int beliefAboutL2(Agent partner, double rand) {
        int beliefAboutPartner;
        if (rand <= COMMF && this.partners.contains(partner)) {
            beliefAboutPartner = partner.layer2;
        } else {
            beliefAboutPartner = this.lastKnownNorm;
        }
        return beliefAboutPartner;
    }

    public void calculateAddition() {
        double profitDiffRatio = this.lastProfits[lastProfits.length - 1] / maxProfit;
        if (profitDiffRatio >= 1) { //if there is no decline in profit, occupy more land
            this.addArea = this.addAreaMax;
        } 
        else if (profitDiffRatio < 0) { //if lost (profit negative) , reduce land;
            this.addArea = (int) round(profitDiffRatio * this.addAreaMax);
        } 
        else {  //if profit is declined, addition is calculated based on ratio and personality
            this.addArea = (int) round(profitDiffRatio * (this.addAreaMax - 1));
        } 
    }

    public void setLayer1() {
        this.layer1 = currentArea + addArea;
        //System.out.println(name + ".Layer1 = " + this.currentArea + "+" + this.addArea + "=" + this.layer1);
    }

    public void setLayer2(double rand) {
        ArrayList<Integer> allPartnersL1 = new ArrayList<>();
        int temp = 0;
        for (Agent partner: partners) {
            temp = temp + this.beliefAboutL1(partner, rand); //this is necessary because other people will ask this value for their L3
            allPartnersL1.add(this.beliefAboutL1(partner, rand));
        }
        this.layer2Norm.objEst = Norm.calcObjEst(allPartnersL1);
        this.layer2Norm.subjectify(this.countNormTreshold);
     
        switch (L2_AGG_STRAT) {
            case "AVG":
                this.layer2 = round(temp / partners.size()); //average of partners L1
                break;
            case "MCOM":
                this.layer2 = SupportTool.mostCommon(allPartnersL1); //most common L1
                break;
            case "RND":
                this.layer2 = allPartnersL1.get(randomizer.nextInt(allPartnersL1.size())); //by random pick (wild guess)
                break;
            default:
                throw new AssertionError();
        }
        
    }

   public void setLayer3(double rand) {
        ArrayList<Integer> allPartnersL2 = new ArrayList<>();
        for (Agent partner: partners) {
            allPartnersL2.add(this.beliefAboutL1(partner, rand)); // this is if you want to use the L3 to be a set of candidate
        }
        this.layer3Norm.objEst = Norm.calcObjEst(allPartnersL2);
        this.layer3Norm.subjectify(this.countNormTreshold);
    }

    public void calculateLastKnownNorm() {
        double tempVal = 0;
        List<Integer> allObserved = new ArrayList<>();
        for (Agent partner: partners) {
            tempVal += partner.currentArea;
            if (!allObserved.contains(partner.currentArea)) {
                allObserved.add(partner.currentArea);
            }
        }
        
        switch (LNK_STRAT) {
            case "AVG":
                 double mean = tempVal / partners.size();
          lastKnownNorm = (int) round(mean);   //by averaging
                break;
            case "MCOM":
          lastKnownNorm = SupportTool.mostCommon(allObserved); //by using the most common value
            break;
            case "RND":
          lastKnownNorm =  allObserved.get(randomizer.nextInt(allObserved.size())); //by random pick (wild guess)
                break;
            default:
                throw new AssertionError();
        }
       
    }

    public void decide() {
        List<Integer> highestCandidate = Norm.compareL2L3(layer2Norm, layer3Norm);
        if (!highestCandidate.isEmpty()) {
            decision = SupportTool.findClosestVal(layer1, highestCandidate); //if there is a candidate that exist both in L2 and L3, choose the closest value to my calc
            if (decision == layer1) {
                this.normOrSelf = "norm eq. self";
            } else {
                this.normOrSelf = "norm";
            }
        } else {
            decision = layer1;
            this.normOrSelf = "self";
        }
        prevArea = currentArea;  //store the previous area for comparison in calculateProfit()       
        currentArea = decision;
    }

    public void calculateProfit() {
        double currentProfit = currentArea * World.profitFactor;
        if (currentProfit > maxProfit && currentArea >= prevArea) { //the area comparison will make sure that maxProfit is 
            //updated due to decreased land size, not because conforming to lower size
            maxProfit = currentProfit;
        }
//        System.out.println("this.name = " + this.name + ", profit= " + currentProfit);
        for (int i = 0; i < lastProfits.length - 1; i++) {
            lastProfits[i] = lastProfits[i + 1]; //e.g. profit[8] moves to profit[7], profit[0] is forgotten
        }
        lastProfits[lastProfits.length - 1] = currentProfit; //if the range of calculation is 10, then the current profit is stored at profit[9]
    }

}
