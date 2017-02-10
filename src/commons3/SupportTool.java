package commons3;

import java.io.File;
import java.io.FileWriter;
import static java.lang.Math.round;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import java.util.ArrayList;
import static java.util.Arrays.sort;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Dipta Mahardhika
 */
class SupportTool {
    public static void writeToCsv(File file, String line) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file.getName(), true);
//            System.out.println("-----------------------------------------------------------line = " + line);
            fileWriter.append(line + "\n");
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            System.out.println("error: " + e);
        }
    }

    public static String threeDigitHex(int decimal) {
        String hex = Integer.toHexString(decimal);
        int length = hex.length();
        switch (length) {
            case 1:
                hex = "00" + hex;
                break;
            case 2:
                hex = "0" + hex;
                break;
            case 3:
                break;
            case 4:
                System.out.println("number is too big");
                System.exit(0);
                break;
            default:
                throw new AssertionError();
        }
        return "agent" + hex;
    }
    public static File notOverwriteFileName(String fName) {
        Pattern p = Pattern.compile("(.*?)(\\d+)?(\\..*)?");
        do {
            Matcher m = p.matcher(fName);
            if (m.matches()) {//group 1 is the prefix, group 2 is the number, group 3 is the suffix
                fName = m.group(1) + (m.group(2) == null ? 1 : (Integer.parseInt(m.group(2)) + 1)) + (m.group(3) == null ? "" : m.group(3));
            }
        } while (new File(fName).exists());//repeat until a new filename is generated
        return new File(fName);
    }
    
    public static boolean intToBool(int number) {
        boolean bool= number!=0 ;
       return bool;
    }
    
   public static int findClosestVal (int target, List<Integer> theList) {
      int closest = target;
      int prevDist= 999999;
       for (Integer number: theList) {
            int curDist = Math.abs(number - target);
            if (curDist < prevDist) {
                closest = number;
                prevDist = curDist;
            }
       }
    return closest;  

   }
    
  public static <T> T mostCommon(List<T> list) {
   
      Map<T, Integer> map = new HashMap<>();

    for (T t : list) {
        Integer val = map.get(t);
        map.put(t, val == null ? 1 : val + 1);
    }

    Entry<T, Integer> max = null;

    for (Entry<T, Integer> e : map.entrySet()) {
        if (max == null || e.getValue() > max.getValue())
            max = e;
    }

    return max.getKey();
}
  
  public static <T> List<T> mostCommon(List<T> list, int top) {
      List<T> multiMaj = new ArrayList<>();
   if (!list.isEmpty() ){
    int highestFreq;
      Map<T, Integer> map = new HashMap<>();

     
        
    for (T t : list) {
       
        
        Integer val = map.get(t);
        map.put(t, val == null ? 1 : val + 1);
    }
  for (int i = 0; i < top; i++) {
  //    System.out.println("iteration "+ (i+1)+" -----");
      Entry<T, Integer> max = null;

    for (Entry<T, Integer> e : map.entrySet()) {
        if (max == null || e.getValue() > max.getValue())
            max = e;
   //     System.out.println("e.getKey() = " + e.getKey() + "  e.getValue() = " + e.getValue());
    }

    highestFreq = max.getValue();
    
    for (Entry<T, Integer> e : map.entrySet()) {
        if (e.getValue()==highestFreq && e.getValue()!=0)
        { multiMaj.add(e.getKey());
           map.put(e.getKey(), 0);}
    }
   }
   }
    return multiMaj;
   
}
  
  public static List<Integer> threeQuart(List<Integer> input) {
      int qInd1 = (int) floor(0.25 * input.size()-1);
      int qInd2 = (int) round(0.5 * input.size()-1);
      int qInd3 = (int) ceil(0.75 * input.size()-1);
      int inputSize = input.size();
      Integer q1, q2, q3;
      Integer[] tempArray = input.toArray(new Integer[inputSize]);
      sort(tempArray);

      List<Integer> output = new ArrayList<>();
      if (inputSize <= 3) {
          output = input;
      } else {
          q1 = tempArray[qInd1];
          q2 = tempArray[qInd2];
          q3 = tempArray[qInd3];
          output.add(q1);
          output.add(q2);
          output.add(q3);
      }
      return output;
  }
  
  
   
  
  
}
