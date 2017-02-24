/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commons3;

import java.util.List;

/**
 *
 * @author Dipta Mahardhika
 */
public class World {
    static final int TOTAL_LAND = Commons3.POP_SIZE*200; //total area available in the world
    static double occupiedLandRatio = 0; //0% means all are available, 100% means none is available
    static final double DECLINE_TRESH = 0.8;
    static double profitFactor =1; //profit received by agent is the amount of land owned multiplied by this factor

    public static void setOccupiedLandRatio(double occupiedLandRatio) {
        World.occupiedLandRatio = occupiedLandRatio;
    }

    public static void setProfitFactor(double profitFactor) {
        World.profitFactor = profitFactor;
    }
     
    
    
static void calculateProfitFactor()    {
    profitFactor = (occupiedLandRatio<DECLINE_TRESH) ? 1 : (5-(5*occupiedLandRatio)); //the formula is made in such way at 100% it will be 0. any value above 100% will result in negative factor (loss)
    //if land occupancy is less than DECLINE_TRESH, profit factor will be 1. larger than that, it will get smaller or even minus
}

static void calculateOccupiedLandRatio(List<Agent> population) {
    int occupiedLand;
    occupiedLand= 0;
     for (Agent partner: population) {
            occupiedLand += partner.decision; 
           // System.out.println("partner.decision = " + occupiedLand);
        }
    occupiedLandRatio = (double) occupiedLand / TOTAL_LAND;
     //System.out.println("--(from the class)------World.occupiedLandRatio = " + occupiedLandRatio);
    
}


}
