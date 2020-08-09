package edu.boisestate.mt;
import java.io.* ;
import java.util.Vector;
import java.util.Set;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.LinkedList;
import java.util.HashSet;
import java.math.BigInteger;

public class referencePosition
{

	private static MTlogger MTout = new MTlogger("referencePosition.java",3);
   private static int IntraOligoSLC; //Scoring-length-Criteria. WCC shorter than SLC will be ingored.
	private static int InterOligoSLC;

	private int S1; // index of first strand involved in the alignment
	private int S2; // index of second strand involved in the alignment
   private int RPI; // reference position index. (index of S1 base where 3' most S2 base starts)
   
   public boolean isIntramolecular; 
   
   private ArrayList<int[]> contBases;   // consists of [0] -> S1B1 index, [1]-> S2B2 index, [2] -> length of stretch
   //private TreeMap<Integer,Integer> baselineProfile = new TreeMap<Integer,Integer>();
   private BigInteger baselineScore = null;
   
   private static HashSet<referencePosition> interReferencePositions = new HashSet<referencePosition>(); 
   private static HashSet<referencePosition> intraReferencePositions = new HashSet<referencePosition>(); 
   private static Generation initialGeneration;
   
   private static TreeMap<Integer,BigInteger> intraScoreMap;
   private static TreeMap<Integer,BigInteger> interScoreMap;
   

	public referencePosition() // Create a blank Alignment
	{ 
		S1 = 0; 
		S2 = 0;
      //isIntramolecular = false; 
      contBases = new ArrayList<int[]>();
	}
   
   public referencePosition(Generation IG, int IS1, int IS2, int IRPI) 
   {
      //generate contiguous bases for an intermolecular reference position
      // record S1, S2, S1B1?, record isIntramolecular?
      
      S1 = IS1; 
		S2 = IS2;
      RPI = IRPI;
      
      isIntramolecular = false; // Incoming-Is-Intramolecular   

      
      int[] sl = IG.getSL();
      char[] st = IG.getST();
      contBases = new ArrayList<int[]>();
      
      int[] tempBAL = new int[]{ 0,0,0};
      
      int S1B1 = IRPI;
      int S2B2 = sl[S2]-1;
      int tempLength = 0;
      
      int i = IRPI;
      do 
      {
         int j = sl[S2]-1;
         do
         {
            tempLength++;
            //add to the array list.
            
            //int[] tempInt = new int[] { (i%sl[S1]),j };
            //tempBAL.add( tempInt );
            // if end of s1, record and start a new stretch of contiguous bases
            if (i > 0 && i % (sl[S1]-1) == 0 && st[S1] == 'L') // if it is the end of s1 and s1 is not circular,
            {
               contBases.add(new int[]{S1B1, S2B2, tempLength});
               S1B1 = (i+1)%sl[S1];
               S2B2 = (j-1)%sl[S2];
               tempLength = 0;
               //tempBAL.trimToSize();
               //contBases.add(tempBAL);
               //tempBAL = new LinkedList<int[]>();
            }
            else if ( j == 0 && st[S2] == 'L') // if this is the end of s2 and s2 is not circular
            {
               contBases.add(new int[]{S1B1, S2B2, tempLength});
               S1B1 = (i+1)%sl[S1];
               S2B2 = (j-1)%sl[S2];
               tempLength = 0;
               //tempBAL.trimToSize();
               //contBases.add(tempBAL);
               //tempBAL = new LinkedList<int[]>();
            }
            i++; 
            j--;
         } while (j >= 0 ); //itterate through S2
      } while (i < sl[S1]); // itterate through S2 again.
      
      calculateBaselineScore(this);
   }
      
   public referencePosition(Generation IG, int IS1, int IRPI) // make a baseline base-alignment from the relevant information
   {
      //generate contiguous bases.
      // record S1, S2, S1B1?, record isIntramolecular?
      
      S1 = IS1; 
		S2 = IS1;
      RPI = IRPI;

      isIntramolecular = true; // Incoming-Is-Intramolecular     
      
      contBases = new ArrayList<int[]>();
      int[] sl = IG.getSL();
      char[] st = IG.getST();
      
      
      
      int S1B1 = (int)(RPI+1)/2;
      int S2B2;
      int lengthRP;
      
      if (sl[S1] % 2 == 0)
      {
         if (RPI %2 == 0)
         {
            lengthRP = sl[S1]/2;
         } else lengthRP = sl[S1]/2-1;
      } 
      else 
      {
         lengthRP = (sl[S1]-1)/2;
      }
      
      //LinkedList<int[]> tempBAL = new LinkedList<int[]>();   
      
      if (RPI % 2 == 0)
      {
         S2B2 = (sl[S1]-1)+RPI/2; 
      } else {S2B2 = (sl[S1]-1)+(RPI-1)/2;}
      
      int tempIB1 = S1B1;
      int tempIB2 = S2B2;
      
      int contBaseS1B1 = S1B1 % sl[S1];
      int contBaseS2B2 = S2B2 % sl[S1];
      int tempLength = 0;
      
      
      for ( int i = 0; i < lengthRP; i++)
      {
         tempIB1 = S1B1 + i;
         tempIB2 = (S2B2 - i) % (sl[S1]);
         tempLength++;
         
         //int[] tempInt = new int[] { tempIB1, tempIB2 }; // add top base and bottom base
         //tempBAL.add( tempInt );
         
         if (tempIB2 == 0 && st[S1] == 'L') // if the bottom base is the end of the strand
         {
            contBases.add(new int[] {contBaseS1B1,contBaseS2B2,tempLength});
            contBaseS1B1 = (tempIB1 + 1) % sl[S1];
            contBaseS2B2 = (tempIB2 - 1) % sl[S1];
            tempLength = 0;
            
            //tempBAL.trimToSize();
            //contBases.add(tempBAL);
            //tempBAL = new LinkedList<int[]>();
         }      
      }
      if(tempLength != 0)
      {
         contBases.add(new int[] {contBaseS1B1,contBaseS2B2,tempLength});
      }
      
      calculateBaselineScore(this);

   }
   
   static public void importSettings ( String ParametersFilePath ) throws Exception
   {
      //MTout.log("Importing Settings for Scoring() module from "+ ParametersFilePath );
      
      // *******************
      // Load default values
      // *******************
        
      IntraOligoSLC = 1;
      InterOligoSLC = 1;
      
      // ****************************
      // Check parameters File for options
      // ****************************
   
      FileReader filereader = new FileReader(ParametersFilePath);   
      StreamTokenizer streamtokenizer = new StreamTokenizer(filereader);
      while( streamtokenizer.ttype != StreamTokenizer.TT_EOF ) // for the entire "parameters" file
      {
         streamtokenizer.nextToken();
         if (streamtokenizer.ttype == StreamTokenizer.TT_WORD)
         {
            if (streamtokenizer.sval.equalsIgnoreCase("IntraOligoSLC"))
            {
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 1)
               {
                  IntraOligoSLC = (int)streamtokenizer.nval;
                  System.out.println("IntraOligoSLC value imported. Accepted value: " + IntraOligoSLC);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"IntraOligoSLC\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"IntraOligoSLC\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("InterOligoSLC"))
            {
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 1)
               {
                  InterOligoSLC = (int)streamtokenizer.nval;
                  System.out.println("InterOligoSLC value imported. Accepted value: " + InterOligoSLC);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"InterOligoSLC\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"InterOligoSLC\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            
         }
      }
   }
   
	public static int getS1( referencePosition I )
	{
		return I.S1;
	}
	
	public static int getS2( referencePosition I )
	{
		return I.S2;
	}

	public static int getRPI( referencePosition I )
	{
		return I.RPI;
	}
	
   public static boolean getIsIntramolecular (referencePosition I)
   {
      return I.isIntramolecular;
   }
   
	public static void printInformation( referencePosition I ) 
	{
		System.out.println("Position Information:");
		System.out.println("S1 = " + getS1(I)); 
		System.out.println("S2 = " + getS2(I)); 

		System.out.println("RPI = " + getRPI(I));  

      System.out.println(); 		
	}
   
   public static void printReferencePosition( referencePosition I)
	{
		System.out.println("Position Visualization:");

      I.contBases.stream()
         .forEach(i->
         {
            int S1B1 = i[0];
            int S2B2 = i[1];
            int tempLength = i[2];
            for(int j = 0; j< tempLength; j++)
            {
               System.out.print((S1B1+j) % initialGeneration.getSL()[I.S1]);
            }
            System.out.print("|");
         });
      System.out.println();
      I.contBases.stream()
         .forEach(i->
         {
            int S1B1 = i[0];
            int S2B2 = i[1];
            int tempLength = i[2];
            for(int j = 0; j< tempLength; j++)
            {
               System.out.print(Math.floorMod((S2B2-j), initialGeneration.getSL()[I.S1]));
            }
            System.out.print("|");
         });
      System.out.println();
      System.out.println();
      /* Code to print/visualize the RP 
      System.out.print("S1: ");
      for( int i = 0; i < length ; i++) 
      {
         System.out.print( decodeBase(getESS(I)[s1][ (s1b1 + (i) ) ]));
      }
      System.out.println();
      System.out.print("S2: ");
      for( int i = length - 1; i >= 0 ; i--) // print 1 less space than strand 2 is long.
      {
         System.out.print( decodeBase(getESS(I)[s2][ (s2b1 + i ) ]));
      }      
      System.out.println();
      System.out.println();
      */
	}
   
   private static String decodeBase (int IB)
   {
      String OB = "";
      switch(IB){
         case 1:
         OB = "A";
         break;
         
         case 2:
         OB = "C";
         break;
         
         case 3:
         OB = "G";
         break;
         
         case 4:
         OB = "T";
         break;
         
         default: 
         OB = "0";
         break;
      }
      return OB;    
   }  
      
   public static HashSet<referencePosition> getIntraReferencePositions()
   {
      //make sure longer strand is always S1.
      return intraReferencePositions;
   }
   
   public static HashSet<referencePosition> getInterReferencePositions()
   {
      //make sure longer strand is always S1.
      return interReferencePositions;
   }
   
   
   public static void initializeReferencePositions(Generation IG)
   {
      int tempLSL = IG.getLSL();
      // hash/Treemap sub-occurrences
	  intraScoreMap = new TreeMap<Integer,BigInteger>();
      
      for(int i = 1; i <= tempLSL; i++) // for every length up to the longest strand length.
      {
         //add this length to the scores hash/Treemap
         Map<Integer,Integer> tempOL = getIntraSubOccurrences(i);
         BigInteger tempScore = tempOL.entrySet().stream().parallel().map(e-> scoringFunction(e.getKey()).multiply(BigInteger.valueOf(e.getValue()))).reduce(BigInteger.valueOf(0),(a,b)->a.add(b));
         intraScoreMap.put(i,tempScore);
      }
      

	  interScoreMap = new TreeMap<Integer,BigInteger>();
      for(int i = 1; i <= tempLSL; i++) // for every length up to the longest strand length.
      {
         //add this length to the scores hash/Treemap
         Map<Integer,Integer> tempOL = getInterSubOccurrences(i);
         BigInteger tempScore = tempOL.entrySet().stream().parallel().map(e-> scoringFunction(e.getKey()).multiply(BigInteger.valueOf(e.getValue()))).reduce(BigInteger.valueOf(0),(a,b)->a.add(b));
         interScoreMap.put(i,tempScore);
      }

      // hash/Treemap the values for scoring each complement length
      
      
      // initialize intermolecular reference positions
      interReferencePositions.clear();
      intraReferencePositions.clear();
      
      //For each strand combination
      int tns = IG.getTNS();
      int[] sl = IG.getSL();
      
      
      for( int i = 0; i < tns; i++ ) // for all strands in the system,
      { 
         for (int j = i; j < tns; j++) //for all sets of strands that have not yet been considered
         {
            if (sl[i] >= sl[j])
            {  
               for (int rp = 0; rp < sl[j]; rp++) // for each reference position
               {
                  interReferencePositions.add(new referencePosition( IG, i, j, rp));
               }
            }
            else for (int rp = 0; rp < sl[i]; rp++) // for each reference position
            {
               interReferencePositions.add(new referencePosition( IG, j, i, rp)); //create backwards rp
            }
         }
      }   

      //set intramolecular RP's
      for( int i = 0; i < tns; i++ ) // for all strands in the system,
      { 
         for (int rp = 0; rp < sl[i]; rp++) // for each reference position
         {
            intraReferencePositions.add(new referencePosition( IG, i, rp));
         }
      }      

   }
   
   /*public static void scoreRP( referencePosition IRP, referencePositionScore IRPS, int[][] IESS)
   {
      TreeMap<Integer,Integer> tempSP = new TreeMap<Integer,Integer>();
      calculateInterferenceProfile( IRP, tempSP, IESS);
      long tempScore = 0L;
      
      if(tempSP.size() != 0)
      {
         if (IRP.isIntramolecular)
            {tempScore = tempSP.entrySet().stream().mapToLong(i -> i.getValue()*intraScoreMap.get(i.getKey())).sum(); }
         else {tempScore = tempSP.entrySet().stream().mapToLong(i -> i.getValue()*interScoreMap.get(i.getKey())).sum(); }
      }
      referencePositionScore.setScore(IRPS, tempScore);
   }
   */
   
   public static BigInteger scoreRP( referencePosition IRP, int[][] IESS )
   {
      //TreeMap<Integer,Integer> tempSP = calculateInterferenceProfile( IRP, IESS);
      /*if(tempSP.size() != 0)
      {
         if (IRP.isIntramolecular)
            {
               tempScore = tempSP.entrySet().stream()
                  .map(i->BigInteger.valueOf(i.getValue().longValue()).multiply(BigInteger.valueOf(intraScoreMap.get(i.getKey()).longValue())))
                  .reduce(new BigInteger("0"), (a,b)-> a.add(b));
            }
         else 
            {
               tempScore = tempSP.entrySet().stream()
                  .map(i->BigInteger.valueOf(i.getValue().longValue()).multiply(BigInteger.valueOf(interScoreMap.get(i.getKey()).longValue())))
                  .reduce(new BigInteger("0"), (a,b)-> a.add(b));
            }
      }
      */
      
      BigInteger tempScore = BigInteger.valueOf(0);
      // for the first stretch of bases, if the first base is complementary (and S1 or S2 is circular), find out where it started.
      // for each other stretch of bases in contbases.
      int complementLength = 0;
      boolean tempIsIntramolecular = IRP.isIntramolecular;
      int tempS1 = IRP.S1;
      int tempS2 = IRP.S2;
      int[] sl = initialGeneration.getSL();
      int[][] ess = IESS;
      
      int baseS1 = Math.floorMod(IRP.contBases.get(0)[0],sl[tempS1]);
      int baseS2 = Math.floorMod(IRP.contBases.get(0)[1],sl[tempS2]);
      
      // identify complementLength for the first base-pair
      if( !tempIsIntramolecular && ess[tempS1][baseS1] + ess[tempS2][baseS2] == 5) //if the first two bases are complementary
      {
         complementLength = -1;
         //read backwards up to sl[S2] bases
         for( int i =0; i< sl[tempS2]; i++)
         {
            baseS1 = Math.floorMod(IRP.contBases.get(0)[0] - i,sl[tempS1]);
            baseS2 = Math.floorMod(IRP.contBases.get(0)[1] + i,sl[tempS2]);
            if(ess[tempS1][baseS1]+ess[tempS2][baseS2] == 5)
            {
               complementLength++;
            } else break;
            if(baseS1 == 0 && initialGeneration.getST()[tempS1] =='L') break;
            if(baseS2 == (sl[tempS2]-1) && initialGeneration.getST()[tempS2] =='L') break;
         }
      };
      
      //printInformation( IRP);
      //printReferencePosition( IRP);
      //System.out.println("Complength = 0 for debugging, previous value: "+complementLength);
      //complementLength = 0;
      
      for (int[] stretch :IRP.contBases)
      {
         for(int i = 0; i < stretch[2]; i++)
         {
            baseS1 = Math.floorMod(stretch[0] + i,sl[tempS1]);
            baseS2 = Math.floorMod(stretch[1] - i,sl[tempS2]);
            
            if (ess[tempS1][baseS1]+ess[tempS2][baseS2] == 5)  // are the bases complementary?
            {
               complementLength++;
            }
            else 
            {
               if(complementLength != 0)
               {
                  if (tempIsIntramolecular)
                  {
                     if (complementLength >= IntraOligoSLC)
                     {
                        //record the complement
                        tempScore = tempScore.add(intraScoreMap.get(complementLength));
                     }
                  }
                  else
                  {
                     if(complementLength >= InterOligoSLC)
                     {
                        //record the complement
                        tempScore = tempScore.add(interScoreMap.get(complementLength));
                     }
                  }
                  complementLength = 0;
               }
            }
         }
         if(complementLength != 0)
         {
            if (tempIsIntramolecular)
            {
               if (complementLength >= IntraOligoSLC)
               {
                  //record the complement
                  tempScore = tempScore.add(intraScoreMap.get(complementLength));
               }
            }
            else
            {
               if(complementLength >= InterOligoSLC)
               {
                  //record the complement
                  tempScore = tempScore.add(interScoreMap.get(complementLength));
               }
            }
            complementLength =0;
         }
      }
      // remove baseline structures
      tempScore = tempScore.subtract(IRP.baselineScore);
      
      return tempScore;
   }
   
   private static BigInteger getBaselineScore( referencePosition IRP)
   {
      return IRP.baselineScore;
   }
   
   private static void calculateBaselineScore(referencePosition IRP)
   {
      BigInteger tempScore = BigInteger.valueOf(0);
      int[][] ess = initialGeneration.getBESS();
      
      // for the first stretch of bases, if the first base is complementary (and S1 or S2 is circular), find out where it started.
      
      // for each other stretch of bases in contbases.
      int complementLength = 0;
      boolean tempIsIntramolecular = IRP.isIntramolecular;
      int tempS1 = IRP.S1;
      int tempS2 = IRP.S2;
      int nvb = initialGeneration.getNVB();
      int[] sl = initialGeneration.getSL();
      
      int baseS1 = Math.floorMod(IRP.contBases.get(0)[0],sl[tempS1]);
      int baseS2 = Math.floorMod(IRP.contBases.get(0)[1],sl[tempS2]);
      
      // identify complementLength for the first base-pair
      if( !tempIsIntramolecular && ess[tempS1][baseS1] + ess[tempS2][baseS2] == 5 || ess[tempS1][baseS1] + ess[tempS2][baseS2] == (2*nvb +11) ) //if the first two bases are complementary
      {
         complementLength = -1;
         //read backwards up to sl[S2] bases
         for( int i =0; i< sl[tempS2]; i++)
         {
            baseS1 = Math.floorMod(IRP.contBases.get(0)[0] - i,sl[tempS1]);
            baseS2 = Math.floorMod(IRP.contBases.get(0)[1] + i,sl[tempS2]);
            if(ess[tempS1][baseS1]+ess[tempS2][baseS2] == 5 || ess[tempS1][baseS1] + ess[tempS2][baseS2] == (2*nvb +11) )
            {
               complementLength++;
            } else break;
            if(baseS1 == 0 && initialGeneration.getST()[tempS1] =='L') break;
            if(baseS2 == (sl[tempS2]-1) && initialGeneration.getST()[tempS2] =='L') break;
         }
      };
      
      for (int[] stretch :IRP.contBases)
      {
         for(int i = 0; i < stretch[2]; i++)
         {
            baseS1 = Math.floorMod(stretch[0] + i,sl[tempS1]);
            baseS2 = Math.floorMod(stretch[1] - i,sl[tempS2]);
            
            if (ess[tempS1][baseS1]+ess[tempS2][baseS2] == 5 || ess[tempS1][baseS1] + ess[tempS2][baseS2] == (2*nvb +11) )  // are the bases complementary?
            {
               complementLength++;
            }
            else 
            {
               if(complementLength != 0)
               {
                  if (tempIsIntramolecular)
                  {
                     if (complementLength >= IntraOligoSLC)
                     {
                        //record the complement
                        tempScore = tempScore.add(intraScoreMap.get(complementLength));
                     };
                  }
                  else
                  {
                     if(complementLength >= InterOligoSLC)
                     {
                        //record the complement
                        tempScore = tempScore.add(interScoreMap.get(complementLength));
                     };
                  }
               complementLength = 0;
               }
            }
         }
         if(complementLength != 0)
         {
            if (tempIsIntramolecular)
            {
               if (complementLength >= IntraOligoSLC)
               {
                  //record the complement
                  tempScore = tempScore.add(intraScoreMap.get(complementLength));
               }
            }
            else
            {
               if(complementLength >= InterOligoSLC)
               {
                  //record the complement
                  tempScore = tempScore.add(interScoreMap.get(complementLength));
               }
            }
            complementLength =0;
         }
      }
      
      /*
      if (IRP.isIntramolecular)
         {
            tempScore = IRP.baselineProfile.entrySet().stream()
               .map(i->BigInteger.valueOf(i.getValue().longValue()).multiply(BigInteger.valueOf(intraScoreMap.get(i.getKey()).longValue())))
               .reduce(new BigInteger("0"), (a,b)-> a.add(b));
         }
      else 
         {
            tempScore = IRP.baselineProfile.entrySet().stream()
               .map(i->BigInteger.valueOf(i.getValue().longValue()).multiply(BigInteger.valueOf(interScoreMap.get(i.getKey()).longValue())))
               .reduce(new BigInteger("0"), (a,b)-> a.add(b));
         }
      */
      IRP.baselineScore = tempScore;      
   }
   
   public static Map<Integer,Integer> getIntraSubOccurrences ( int IL )
   {
      Map<Integer,Integer> tempOL = new TreeMap<Integer,Integer>();
         
      for (int j = IL ; j > 0 && j >= IntraOligoSLC; j--) // for all lengths shorter than  or equal to this length, but longer than SLC
      {
         tempOL.put( j , IL - j + 1);
      }
      return tempOL;
   }
   
   public static Map<Integer,Integer> getInterSubOccurrences ( int IL )
   {
      Map<Integer,Integer> tempOL = new TreeMap<Integer,Integer>();
         
      for (int j = IL ; j > 0 && j >= InterOligoSLC; j--) // for all lengths shorter than  or equal to this length, but longer than SLC
      {
         tempOL.put( j , IL - j + 1);
      }
      return tempOL;
   }
   
   private static BigInteger scoringFunction (int IL ) //incoming length
   {
      BigInteger tempScore = BigInteger.valueOf(0);
      
	  tempScore = BigInteger.valueOf(10).pow(IL);
      /*
	  if (IL <= 10)
      {
         tempScore = (long) Math.pow(10,IL);
         
      } else tempScore = (long)(Math.pow(10,10) * Math.pow(IL,2));
	  */
      return tempScore;
   }
   
   public static void calculateInterferenceProfile( referencePosition IRP, TreeMap<Integer,Integer> IIP, int[][] IESS)
   {
      TreeMap<Integer,Integer> tempInterferenceProfile = IIP;
      tempInterferenceProfile.clear();
      // for the first stretch of bases, if the first base is complementary (and S1 or S2 is circular), find out where it started.
      
      // for each other stretch of bases in contbases.
      int complementLength = 0;
      boolean tempIsIntramolecular = IRP.isIntramolecular;
      int tempS1 = IRP.S1;
      int tempS2 = IRP.S2;
      int[] sl = initialGeneration.getSL();
      int[][] ess = IESS;
      
      int baseS1 = Math.floorMod(IRP.contBases.get(0)[0],sl[tempS1]);
      int baseS2 = Math.floorMod(IRP.contBases.get(0)[1],sl[tempS2]);
      
      // identify complementLength for the first base-pair
      if( !tempIsIntramolecular && ess[tempS1][baseS1] + ess[tempS2][baseS2] == 5) //if the first two bases are complementary
      {
         complementLength = -1;
         //read backwards up to sl[S2] bases
         for( int i =0; i< sl[tempS2]; i++)
         {
            baseS1 = Math.floorMod(IRP.contBases.get(0)[0] - i,sl[tempS1]);
            baseS2 = Math.floorMod(IRP.contBases.get(0)[1] + i,sl[tempS2]);
            if(ess[tempS1][baseS1]+ess[tempS2][baseS2] == 5)
            {
               complementLength++;
            } else break;
            if(baseS1 == 0 && initialGeneration.getST()[tempS1] =='L') break;
            if(baseS2 == (sl[tempS2]-1) && initialGeneration.getST()[tempS2] =='L') break;
         }
      };
      
      //printInformation( IRP);
      //printReferencePosition( IRP);
      //System.out.println("Complength = 0 for debugging, previous value: "+complementLength);
      //complementLength = 0;
      
      for (int[] stretch :IRP.contBases)
      {
         for(int i = 0; i < stretch[2]; i++)
         {
            baseS1 = Math.floorMod(stretch[0] + i,sl[tempS1]);
            baseS2 = Math.floorMod(stretch[1] - i,sl[tempS2]);
            
            if (ess[tempS1][baseS1]+ess[tempS2][baseS2] == 5)  // are the bases complementary?
            {
               complementLength++;
            }
            else 
            {
               if(complementLength != 0)
               {
                  if (tempIsIntramolecular)
                  {
                     if (complementLength >= IntraOligoSLC)
                     {
                        //record the complement
                        if(tempInterferenceProfile.containsKey(complementLength))
                        {
                           tempInterferenceProfile.put(complementLength, tempInterferenceProfile.get(complementLength) + 1);
                        } else tempInterferenceProfile.put(complementLength, 1);
                     }
                  }
                  else
                  {
                     if(complementLength >= InterOligoSLC)
                     {
                        //record the complement
                        if(tempInterferenceProfile.containsKey(complementLength))
                        {
                           tempInterferenceProfile.put(complementLength, tempInterferenceProfile.get(complementLength) + 1);
                        } else tempInterferenceProfile.put(complementLength, 1);
                     }
                  }
                  complementLength = 0;
               }
            }
         }
         if(complementLength != 0)
         {
            if (tempIsIntramolecular)
            {
               if (complementLength >= IntraOligoSLC)
               {
                  //record the complement
                  if(tempInterferenceProfile.containsKey(complementLength))
                  {
                     tempInterferenceProfile.put(complementLength, tempInterferenceProfile.get(complementLength) + 1);
                  } else tempInterferenceProfile.put(complementLength, 1);
               }
            }
            else
            {
               if(complementLength >= InterOligoSLC)
               {
                  //record the complement
                  if(tempInterferenceProfile.containsKey(complementLength))
                  {
                     tempInterferenceProfile.put(complementLength, tempInterferenceProfile.get(complementLength) + 1);
                  } else tempInterferenceProfile.put(complementLength, 1);
               }
            }
            complementLength =0;
         }
      }
      // remove baseline structures
      
      TreeMap<Integer,Integer> tempBP = getBaselineProfile(IRP);
      if(tempBP.size()>0)
      {
         tempBP.entrySet().stream().forEach(i->
         {
            int BPL = i.getKey();
            int BPC = i.getValue();
            if( tempInterferenceProfile.containsKey( BPL ))
            {
               int IPC = tempInterferenceProfile.get(BPL);
               if(BPC == IPC)
               {
                  tempInterferenceProfile.remove(BPL);
               } 
               else tempInterferenceProfile.put( BPL, IPC - BPC);
            } else tempInterferenceProfile.put(BPL, 0-BPC);

         });
      }
      
   }
   
   public static TreeMap<Integer,Integer> calculateInterferenceProfile( referencePosition IRP, int[][] IESS)
   {
      TreeMap<Integer,Integer> tempInterferenceProfile = new TreeMap<Integer,Integer>();
      // for the first stretch of bases, if the first base is complementary (and S1 or S2 is circular), find out where it started.
      
      // for each other stretch of bases in contbases.
      int complementLength = 0;
      boolean tempIsIntramolecular = IRP.isIntramolecular;
      int tempS1 = IRP.S1;
      int tempS2 = IRP.S2;
      int[] sl = initialGeneration.getSL();
      int[][] ess = IESS;
      
      int baseS1 = Math.floorMod(IRP.contBases.get(0)[0],sl[tempS1]);
      int baseS2 = Math.floorMod(IRP.contBases.get(0)[1],sl[tempS2]);
      
      // identify complementLength for the first base-pair
      if( !tempIsIntramolecular && ess[tempS1][baseS1] + ess[tempS2][baseS2] == 5) //if the first two bases are complementary
      {
         complementLength = -1;
         //read backwards up to sl[S2] bases
         for( int i =0; i< sl[tempS2]; i++)
         {
            baseS1 = Math.floorMod(IRP.contBases.get(0)[0] - i,sl[tempS1]);
            baseS2 = Math.floorMod(IRP.contBases.get(0)[1] + i,sl[tempS2]);
            if(ess[tempS1][baseS1]+ess[tempS2][baseS2] == 5)
            {
               complementLength++;
            } else break;
            if(baseS1 == 0 && initialGeneration.getST()[tempS1] =='L') break;
            if(baseS2 == (sl[tempS2]-1) && initialGeneration.getST()[tempS2] =='L') break;
         }
      };
      
      //printInformation( IRP);
      //printReferencePosition( IRP);
      //System.out.println("Complength = 0 for debugging, previous value: "+complementLength);
      //complementLength = 0;
      
      for (int[] stretch :IRP.contBases)
      {
         for(int i = 0; i < stretch[2]; i++)
         {
            baseS1 = Math.floorMod(stretch[0] + i,sl[tempS1]);
            baseS2 = Math.floorMod(stretch[1] - i,sl[tempS2]);
            
            if (ess[tempS1][baseS1]+ess[tempS2][baseS2] == 5)  // are the bases complementary?
            {
               complementLength++;
            }
            else 
            {
               if(complementLength != 0)
               {
                  if (tempIsIntramolecular)
                  {
                     if (complementLength >= IntraOligoSLC)
                     {
                        //record the complement
                        if(tempInterferenceProfile.containsKey(complementLength))
                        {
                           tempInterferenceProfile.put(complementLength, tempInterferenceProfile.get(complementLength) + 1);
                        } else tempInterferenceProfile.put(complementLength, 1);
                     }
                  }
                  else
                  {
                     if(complementLength >= InterOligoSLC)
                     {
                        //record the complement
                        if(tempInterferenceProfile.containsKey(complementLength))
                        {
                           tempInterferenceProfile.put(complementLength, tempInterferenceProfile.get(complementLength) + 1);
                        } else tempInterferenceProfile.put(complementLength, 1);
                     }
                  }
                  complementLength = 0;
               }
            }
         }
         if(complementLength != 0)
         {
            if (tempIsIntramolecular)
            {
               if (complementLength >= IntraOligoSLC)
               {
                  //record the complement
                  if(tempInterferenceProfile.containsKey(complementLength))
                  {
                     tempInterferenceProfile.put(complementLength, tempInterferenceProfile.get(complementLength) + 1);
                  } else tempInterferenceProfile.put(complementLength, 1);
               }
            }
            else
            {
               if(complementLength >= InterOligoSLC)
               {
                  //record the complement
                  if(tempInterferenceProfile.containsKey(complementLength))
                  {
                     tempInterferenceProfile.put(complementLength, tempInterferenceProfile.get(complementLength) + 1);
                  } else tempInterferenceProfile.put(complementLength, 1);
               }
            }
            complementLength =0;
         }
      }
      // remove baseline structures
      TreeMap<Integer,Integer> tempBP = getBaselineProfile(IRP); 
      if(tempBP.size()>0)
      {
         tempBP.entrySet().stream().forEach(i->
         {
            int BPL = i.getKey();
            int BPC = i.getValue();
            if( tempInterferenceProfile.containsKey( BPL ))
            {
               int IPC = tempInterferenceProfile.get(BPL);
               if(BPC == IPC)
               {
                  tempInterferenceProfile.remove(BPL);
               } 
               else tempInterferenceProfile.put( BPL, IPC - BPC);
            } else tempInterferenceProfile.put(BPL, 0-BPC);

         });
      }
      return tempInterferenceProfile;
   }

   
   public static TreeMap<Integer,Integer> getBaselineProfile( referencePosition IRP )
   {
      TreeMap<Integer,Integer> tempBP =  new TreeMap<Integer,Integer>();
      int[][] ess = initialGeneration.getBESS();
      
      // for the first stretch of bases, if the first base is complementary (and S1 or S2 is circular), find out where it started.
      
      // for each other stretch of bases in contbases.
      int complementLength = 0;
      boolean tempIsIntramolecular = IRP.isIntramolecular;
      int tempS1 = IRP.S1;
      int tempS2 = IRP.S2;
      int nvb = initialGeneration.getNVB();
      int[] sl = initialGeneration.getSL();
      
      int baseS1 = Math.floorMod(IRP.contBases.get(0)[0],sl[tempS1]);
      int baseS2 = Math.floorMod(IRP.contBases.get(0)[1],sl[tempS2]);
      
      // identify complementLength for the first base-pair
      if( !tempIsIntramolecular && ess[tempS1][baseS1] + ess[tempS2][baseS2] == 5 || ess[tempS1][baseS1] + ess[tempS2][baseS2] == (2*nvb +11) ) //if the first two bases are complementary
      {
         complementLength = -1;
         //read backwards up to sl[S2] bases
         for( int i =0; i< sl[tempS2]; i++)
         {
            baseS1 = Math.floorMod(IRP.contBases.get(0)[0] - i,sl[tempS1]);
            baseS2 = Math.floorMod(IRP.contBases.get(0)[1] + i,sl[tempS2]);
            if(ess[tempS1][baseS1]+ess[tempS2][baseS2] == 5 || ess[tempS1][baseS1] + ess[tempS2][baseS2] == (2*nvb +11) )
            {
               complementLength++;
            } else break;
            if(baseS1 == 0 && initialGeneration.getST()[tempS1] =='L') break;
            if(baseS2 == (sl[tempS2]-1) && initialGeneration.getST()[tempS2] =='L') break;
         }
      };
      
      for (int[] stretch :IRP.contBases)
      {
         for(int i = 0; i < stretch[2]; i++)
         {
            baseS1 = Math.floorMod(stretch[0] + i,sl[tempS1]);
            baseS2 = Math.floorMod(stretch[1] - i,sl[tempS2]);
            
            if (ess[tempS1][baseS1]+ess[tempS2][baseS2] == 5 || ess[tempS1][baseS1] + ess[tempS2][baseS2] == (2*nvb +11) )  // are the bases complementary?
            {
               complementLength++;
            }
            else 
            {
               if(complementLength != 0)
               {
                  if (tempIsIntramolecular)
                  {
                     if (complementLength >= IntraOligoSLC)
                     {
                        //record the complement
                        if(tempBP.containsKey(complementLength))
                        {
                           tempBP.put(complementLength, tempBP.get(complementLength) + 1);
                        } else tempBP.put(complementLength, 1);
                     };
                  }
                  else
                  {
                     if(complementLength >= InterOligoSLC)
                     {
                        //record the complement
                        if(tempBP.containsKey(complementLength))
                        {
                           tempBP.put(complementLength, tempBP.get(complementLength) + 1);
                        } else tempBP.put(complementLength, 1);
                     };
                  }
               complementLength = 0;
               }
            }
         }
         if(complementLength != 0)
         {
            if (tempIsIntramolecular)
            {
               if (complementLength >= IntraOligoSLC)
               {
                  //record the complement
                  if(tempBP.containsKey(complementLength))
                  {
                     tempBP.put(complementLength, tempBP.get(complementLength) + 1);
                  } else tempBP.put(complementLength, 1);
               }
            }
            else
            {
               if(complementLength >= InterOligoSLC)
               {
                  //record the complement
                  if(tempBP.containsKey(complementLength))
                  {
                     tempBP.put(complementLength, tempBP.get(complementLength) + 1);
                  } else tempBP.put(complementLength, 1);
               }
            }
            complementLength =0;
         }
      }
      return tempBP;
   }
   /*
   private static void calculateBaselineProfile( referencePosition IRP )
   {
      TreeMap<Integer,Integer> tempBP =  IRP.baselineProfile;
      tempBP.clear();
      int[][] ess = initialGeneration.getBESS();
      
      // for the first stretch of bases, if the first base is complementary (and S1 or S2 is circular), find out where it started.
      
      // for each other stretch of bases in contbases.
      int complementLength = 0;
      boolean tempIsIntramolecular = IRP.isIntramolecular;
      int tempS1 = IRP.S1;
      int tempS2 = IRP.S2;
      int nvb = initialGeneration.getNVB();
      int[] sl = initialGeneration.getSL();
      
      int baseS1 = Math.floorMod(IRP.contBases.get(0)[0],sl[tempS1]);
      int baseS2 = Math.floorMod(IRP.contBases.get(0)[1],sl[tempS2]);
      
      // identify complementLength for the first base-pair
      if( !tempIsIntramolecular && ess[tempS1][baseS1] + ess[tempS2][baseS2] == 5 || ess[tempS1][baseS1] + ess[tempS2][baseS2] == (2*nvb +11) ) //if the first two bases are complementary
      {
         complementLength = -1;
         //read backwards up to sl[S2] bases
         for( int i =0; i< sl[tempS2]; i++)
         {
            baseS1 = Math.floorMod(IRP.contBases.get(0)[0] - i,sl[tempS1]);
            baseS2 = Math.floorMod(IRP.contBases.get(0)[1] + i,sl[tempS2]);
            if(ess[tempS1][baseS1]+ess[tempS2][baseS2] == 5 || ess[tempS1][baseS1] + ess[tempS2][baseS2] == (2*nvb +11) )
            {
               complementLength++;
            } else break;
            if(baseS1 == 0 && initialGeneration.getST()[tempS1] =='L') break;
            if(baseS2 == (sl[tempS2]-1) && initialGeneration.getST()[tempS2] =='L') break;
         }
      };
      
      for (int[] stretch :IRP.contBases)
      {
         for(int i = 0; i < stretch[2]; i++)
         {
            baseS1 = Math.floorMod(stretch[0] + i,sl[tempS1]);
            baseS2 = Math.floorMod(stretch[1] - i,sl[tempS2]);
            
            if (ess[tempS1][baseS1]+ess[tempS2][baseS2] == 5 || ess[tempS1][baseS1] + ess[tempS2][baseS2] == (2*nvb +11) )  // are the bases complementary?
            {
               complementLength++;
            }
            else 
            {
               if(complementLength != 0)
               {
                  if (tempIsIntramolecular)
                  {
                     if (complementLength >= IntraOligoSLC)
                     {
                        //record the complement
                        if(tempBP.containsKey(complementLength))
                        {
                           tempBP.put(complementLength, tempBP.get(complementLength) + 1);
                        } else tempBP.put(complementLength, 1);
                     };
                  }
                  else
                  {
                     if(complementLength >= InterOligoSLC)
                     {
                        //record the complement
                        if(tempBP.containsKey(complementLength))
                        {
                           tempBP.put(complementLength, tempBP.get(complementLength) + 1);
                        } else tempBP.put(complementLength, 1);
                     };
                  }
               complementLength = 0;
               }
            }
         }
         if(complementLength != 0)
         {
            if (tempIsIntramolecular)
            {
               if (complementLength >= IntraOligoSLC)
               {
                  //record the complement
                  if(tempBP.containsKey(complementLength))
                  {
                     tempBP.put(complementLength, tempBP.get(complementLength) + 1);
                  } else tempBP.put(complementLength, 1);
               }
            }
            else
            {
               if(complementLength >= InterOligoSLC)
               {
                  //record the complement
                  if(tempBP.containsKey(complementLength))
                  {
                     tempBP.put(complementLength, tempBP.get(complementLength) + 1);
                  } else tempBP.put(complementLength, 1);
               }
            }
            complementLength =0;
         }
      }
      
      calculateBaselineScore(IRP);
      
      //IRP.baselineProfile.entrySet().stream().forEach(e-> System.out.println( e.getKey() + ", " + e.getValue()));
      //System.out.println("BaselineScore: " + IRP.baselineScore);
   }*/
   
   public static void printBaselineProfile( referencePosition IRP)
   {
      System.out.println("Baseline Profile: (length, count)");
      System.out.println("-------------------------------------");
      
      getBaselineProfile(IRP)
      .entrySet().stream().forEach(i->
      {
         System.out.println(i.getKey() +", " + i.getValue());
      });
      
      System.out.println();
   }
   
   public static void getRPDAssociations( referencePosition IRP, HashSet<Integer> IRPDA, int[][] IDA)
   {
      HashSet<Integer> tempRPDA = IRPDA;
      tempRPDA.clear();

      int tempS1 = IRP.S1;
      int tempS2 = IRP.S2;
      int[] sl = initialGeneration.getSL();
      
      for (int[] stretch :IRP.contBases)
      {
         for(int i = 0; i < stretch[2]; i++)
         {
            int baseS1 = Math.floorMod(stretch[0] + i,sl[tempS1]);
            int baseS2 = Math.floorMod(stretch[1] - i,sl[tempS2]);
            tempRPDA.add(IDA[tempS1][baseS1]);
            tempRPDA.add(IDA[tempS2][baseS2]);
            //if (!tempRPDA.contains( IDA[tempS1][baseS1] ))
            //   tempRPDA.add(IDA[tempS1][baseS1]);
            //if (!tempRPDA.contains( IDA[tempS2][baseS2] ))
            //   tempRPDA.add(IDA[tempS2][baseS2]);
         }
      }

   }
   
   public static HashSet<Integer> getRPDAssociations( referencePosition IRP, int[][] IDA)
   {
      HashSet<Integer> tempRPDA = new HashSet<Integer>();

      int tempS1 = IRP.S1;
      int tempS2 = IRP.S2;
      int[] sl = initialGeneration.getSL();
      
      for (int[] stretch :IRP.contBases)
      {
         for(int i = 0; i < stretch[2]; i++)
         {
            int baseS1 = Math.floorMod(stretch[0] + i,sl[tempS1]);
            int baseS2 = Math.floorMod(stretch[1] - i,sl[tempS2]);
            tempRPDA.add(IDA[tempS1][baseS1]);
            tempRPDA.add(IDA[tempS2][baseS2]);
            //if (!tempRPDA.contains( IDA[tempS1][baseS1] ))
            //   tempRPDA.add(IDA[tempS1][baseS1]);
            //if (!tempRPDA.contains( IDA[tempS2][baseS2] ))
            //   tempRPDA.add(IDA[tempS2][baseS2]);
         }
      }
      
      return tempRPDA;

   }
   
	 

}