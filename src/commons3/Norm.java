package commons3;

import static java.lang.Math.round;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Dipta Mahardhika
 */
public class Norm {

    public List<NormEstimation> objEst; //objective estimation
    public List<NormEstimation> subEst; //subjective estimation

    public Norm() {
        this.objEst = new ArrayList<>();
        this.subEst = new ArrayList<>();
    }

    public static List<Integer> compareL2L3(Norm L2, Norm L3) {
        List<Integer> highestCandidate = new ArrayList<>();;
        List<NormEstimation> candComb = new ArrayList<>();
        List<Integer> tempCandidate = new ArrayList<>();
        int tempFreq;
        
        for (NormEstimation L2norm: L2.subEst) {
           
            for (NormEstimation L3norm: L3.subEst) {
                if (L2norm.value == L3norm.value) {
                    tempCandidate.add(L2norm.value); // only the land size
                    tempFreq = (int) round((L2norm.value+L3norm.value)/2);
                    candComb.add(new NormEstimation(L2norm.value,tempFreq)); //land size and freq
                    break;
                }
            }
        }
        
            switch (Agent.DEC_STRAT) {
                case "THRES":
                    highestCandidate = tempCandidate ;
                    break;
                case "MAJ3":
                    tempCandidate.clear();
                    for (NormEstimation tempNorm: candComb) {
                        for (int i = 0; i < tempNorm.freq; i++) {
                            tempCandidate.add(tempNorm.value);
                        }
                    }
                    highestCandidate = SupportTool.mostCommon(tempCandidate, 3);
                    break;
                case "QUART":
                    tempCandidate.clear();
                    for (NormEstimation tempNorm: candComb) {
                        for (int i = 0; i < tempNorm.freq; i++) {
                            tempCandidate.add(tempNorm.value);
                        }
                    }
                    highestCandidate = SupportTool.threeQuart(tempCandidate);
                    break;
                case "RAND3":
                    
                    break;
                default:
                    throw new AssertionError();
            }
       
        
        return highestCandidate;
    }

    public List<Integer> maj3(List<NormEstimation> normCol){
        List<Integer> majNorms = new ArrayList<>();
        List<Integer> normEnum = new ArrayList<>();
        for (NormEstimation normEst: normCol) {
            for (int i = 0; i < normEst.freq; i++) {
               normEnum.add(normEst.value);
            }
        }
        
        
        
        return majNorms;
    }
    
    public void subjectify(int treshold) {
        if ("THRES".equals(Agent.DEC_STRAT)) {
            for (NormEstimation normEstimation: objEst) {
                if (normEstimation.freq > treshold) {
                    subEst.add(new NormEstimation(normEstimation.value, normEstimation.freq - treshold));
                }
            }
        } else {
            subEst = objEst;
        }
    }

    public static List<NormEstimation> calcObjEst(List<Integer> allPartners) {
        List<NormEstimation> objEstTemp = new ArrayList<>();
        Set<Integer> uniqObjEst = new HashSet<>();
        uniqObjEst.addAll(allPartners);
        for (Integer uniqVal: uniqObjEst) {
            objEstTemp.add(new NormEstimation(uniqVal, Collections.frequency(allPartners, uniqVal)));
        }
        return objEstTemp;
    }

    public static class NormEstimation {

        int value;
        int freq;

        public NormEstimation(int value, int freq) {
            this.value = value;
            this.freq = freq;
        }

    }
}
