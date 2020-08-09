package edu.boisestate.mt;
import java.io.* ;
import java.util.Vector;
import java.util.Set;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class baseAlignment
{

	private static MTlogger MTout = new MTlogger("baseAlignment.java",3);
   private static int IntraOligoSLC; //Scoring-length-Criteria. WCC shorter than SLC will be ingored.
	private static int InterOligoSLC;

	private int S1; // index of first strand involved in the alignment
	private int S2; // index of second strand involved in the alignment
   private boolean isIntramolecular; 
	private int S1B1; // Strand-1 Base 1 (base index of 5' most base of alignment)
	private int S2B1; // Strand-2 Base 1 (base index of 5' most base in alignment)
	private int L; // Length - Number of bases within alignment
   
   private int[][] ESS; //encoded strand sequence
   private baseAlignment BBA; // Baseline-base-Alignment;
   private boolean isBBA;
   
   private Map<Integer,Integer> UniqueOccurrences;   
   private boolean UniqueOccurrencesUpToDate = false;
   
   private Map<Integer,Integer> baselineUniqueOccurrences;   
   private boolean baselineUniqueOccurrencesUpToDate = false;
   
   private Map<Integer,Integer> interferenceUniqueOccurrences; 
   private boolean interferenceUniqueOccurrencesUpToDate = false;
   
   private boolean structuresUpToDate = false;
   ArrayList<WCStructure> structures = new ArrayList<WCStructure>();
   
   private boolean interferenceStructuresUpToDate = false;
   ArrayList<WCStructure> interferenceStructures = new ArrayList<WCStructure>();
   
   private boolean baselineStructuresUpToDate = false;
   ArrayList<WCStructure> baselineStructures = new ArrayList<WCStructure>();

	public baseAlignment() // Create a blank Alignment
	{ 
		S1 = 0; 
		S2 = 0;
      isIntramolecular = false; 
		S1B1 = 0;
		S2B1 = 0;

		L = 0;
      UniqueOccurrences = new TreeMap<Integer,Integer>();
      baselineUniqueOccurrences = new TreeMap<Integer,Integer>();   
      interferenceUniqueOccurrences = new TreeMap<Integer,Integer>();       
	}
   
   public baseAlignment(int IS1, int IS1B1, int IS2, int IS2B1, int IL, boolean III, int[][] IESS) // make a baseline base-alignment from the relevant information
   {
      S1 = IS1; 
		S2 = IS2;
		S1B1 = IS1B1;
		S2B1 = IS2B1;
      L = IL;
      isIntramolecular = III; // Incoming-Is-Intramolecular 
      baselineUniqueOccurrences = new TreeMap<Integer,Integer>();   
      isBBA = true;
      
      ESS = IESS;
      
   }
   
   public baseAlignment( baseAlignment IBE , int[][] IESS) // create a non-baseline BA from a BBA
	{ 
		S1 = getS1(IBE); 
		S2 = getS2(IBE);

		S1B1 = getS1B1(IBE);
		S2B1 = getS2B1(IBE) ;

		L = getLength(IBE);
      isIntramolecular = getIsIntramolecular(IBE);
      
      ESS = IESS;
      
      BBA = IBE;
      isBBA=false;
       
      UniqueOccurrences = new TreeMap<Integer,Integer>();   
      interferenceUniqueOccurrences = new TreeMap<Integer,Integer>();

      UniqueOccurrencesUpToDate = false;
      interferenceUniqueOccurrencesUpToDate = false;
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
   
	public static int getS1( baseAlignment I )
	{
		return I.S1;
	}
	
	public static int getS2( baseAlignment I )
	{
		return I.S2;
	}

	public static int getS1B1( baseAlignment I )
	{
		return I.S1B1;
	}

	public static int getS2B1( baseAlignment I )
	{
		return I.S2B1;
	}
	
	public static int getLength( baseAlignment I )
	{
		return I.L;
	}
   
   public static boolean getIsIntramolecular (baseAlignment I)
   {
      return I.isIntramolecular;
   }
   
   public static int[][] getESS (baseAlignment I)
   {
      return I.ESS;
   }
   

	public static void printAlignmentInformation( baseAlignment I ) 
	{
		System.out.println("Alignment Information:");
		System.out.println("S1 = " + getS1(I)); 
		System.out.println("S2 = " + getS2(I)); 

		System.out.println("S1B1 = " + getS1B1(I)); 
		System.out.println("S2B1 = " + getS2B1(I));  
		
		System.out.println("Length = " + getLength(I));
      System.out.println(); 		
	}
   
   public static void printAlignment( baseAlignment I)
	{
		System.out.println("Alignment Visualization:");

      int s1 = getS1( I );
      int s1b1 = getS1B1( I );
      int s2 = getS2( I );
      int s2b1 = getS2B1( I );
      int length = getLength( I );
      
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
	}
	 
  
   public void generateBaselineStructures() // incoming generation
   {
		int ess[][] = Generation.getBESS(); //get encoded strand sequences
		int nvb = Generation.getNVB(); //get number of variable bases
      
      baselineStructures.clear();

      int lengthCounter = 0;
      int complementS1B1 = 0;
      
      for( int i = 0; i < L; i++) //for the overlap in the alignment
      {
         if( ess[S1][(S1B1 + i)] + ess[S2][(S2B1+(L-1)-i)] == 5 || ess[S1][(S1B1 + i)] + ess[S2][(S2B1+(L-1)-i)]== (2*nvb +11) ) //if the bases are complements
         {
            if(lengthCounter == 0) {complementS1B1 = S1B1 + i;}
            lengthCounter++;
         }
         else  //if the bases are not complementary 
         {
            if(isIntramolecular && lengthCounter >= IntraOligoSLC)
            {
               //add the complement to the baseline, reset variables
               baselineStructures.add( new WCStructure(S1,complementS1B1,S2,(S2B1+(L-1)-i+1),lengthCounter, isIntramolecular));
            }
            else if(lengthCounter >= InterOligoSLC) // and there is a long intermolecular complement
            {
               //add the complement to the baseline, reset variables
               baselineStructures.add( new WCStructure(S1,complementS1B1,S2,(S2B1+(L-1)-i+1),lengthCounter, isIntramolecular));
            }
            lengthCounter = 0;
         }
         
      }
      //if the loop ends on a complement, count it.
      if( ess[S1][(S1B1 + (L-1))] + ess[S2][(S2B1)] == 5 || ess[S1][(S1B1 + (L-1))] + ess[S2][(S2B1)]== (2*nvb +11) )
      {
         if(isIntramolecular && lengthCounter >= IntraOligoSLC)
         {
            //add the complement to the baseline, reset variables
            baselineStructures.add( new WCStructure(S1,complementS1B1,S2,S2B1,lengthCounter, isIntramolecular));
         }
         else if(lengthCounter >= InterOligoSLC) // and there is a long intermolecular complement
         {
            //add the complement to the baseline, reset variables
            baselineStructures.add( new WCStructure(S1,complementS1B1,S2,S2B1,lengthCounter, isIntramolecular));
         }
      }
      baselineStructuresUpToDate = true;
      
      //System.out.println("Baseline Structures found: " + baselineStructures.size());
   }
   
   public ArrayList<WCStructure> getBaselineStructures() // incoming generation
   {
      if (isBBA)
      {
         if( baselineStructuresUpToDate == false ) //find/initialize baseline if not yet done.
         {
            generateBaselineStructures();
         }
         return baselineStructures;
      }
      else return BBA.getBaselineStructures();
      
   }
   
   public static String decodeBase (int IB)
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
   
   public ArrayList<WCStructure> getStructures() // incoming-encoded-strand-sequences
   {
      if (structuresUpToDate == false)
      {
         generateStructures();
      }
      return structures;
   }
   
   private void generateStructures()
   {
      structures.clear();
     
      int lengthCounter = 0;
      int complementS1B1 = 0;
      
      for( int i = 0; i < L; i++) //for the overlap in the alignment
      {
         if( ESS[S1][(S1B1 + i)] + ESS[S2][(S2B1+(L-1)-i)] == 5 ) //if the bases are complements
         {
            if(lengthCounter == 0) {complementS1B1 = S1B1 + i;}
            lengthCounter++;
         }
         
         else  //if the bases are not complementary 
         {
            if(isIntramolecular && lengthCounter >= IntraOligoSLC)
            {
               //add the complement to the baseline, reset variables
               structures.add( new WCStructure(S1,complementS1B1,S2,(S2B1+(L-1)-i+1),lengthCounter, isIntramolecular));
            }
            else if(lengthCounter >= InterOligoSLC) // and there is a long intermolecular complement
            {
               //add the complement to the baseline, reset variables
               structures.add( new WCStructure(S1,complementS1B1,S2,(S2B1+(L-1)-i+1),lengthCounter, isIntramolecular));
            }
            lengthCounter = 0;
         }
         
      }
      //if the loop ends on a complement, count it.
      if( ESS[S1][(S1B1 + (L-1))] + ESS[S2][(S2B1)] == 5 )
      {
         if(isIntramolecular && lengthCounter >= IntraOligoSLC)
         {
            //add the complement to the baseline, reset variables
            structures.add( new WCStructure(S1,complementS1B1,S2,S2B1,lengthCounter, isIntramolecular));
         }
         else if(!isIntramolecular && lengthCounter >= InterOligoSLC) // and there is a long intermolecular complement
         {
            //add the complement to the baseline, reset variables
            structures.add( new WCStructure(S1,complementS1B1,S2,S2B1,lengthCounter, isIntramolecular));
         }
      }
   }
   
   public ArrayList<WCStructure> getInterferenceStructures() // incoming-encoded-strand-sequences
   {
      if (!interferenceStructuresUpToDate)
      {
         generateInterferenceStructures();
      }
      return interferenceStructures;
   }
   
   private void generateInterferenceStructures()
   {
      interferenceStructures.clear();
      interferenceStructures.addAll( getStructures( ) );
      
      for (WCStructure temp : getBaselineStructures())
      {
         interferenceStructures.removeIf( p -> WCStructure.isImplementation( p, temp ));
      };
 
      
   }
   
   public void sequencesUpdated()
   {
      structuresUpToDate = false;
      interferenceStructuresUpToDate = false;
   }
   
   public boolean isUpToDate()
   {
      if (structuresUpToDate && interferenceStructuresUpToDate)
         return true;
      else return false;
   }  
   
   public Map<Integer,Integer> getBaselineUniqueOccurrences()
   {
      if( !baselineUniqueOccurrencesUpToDate) generateBaselineUniqueOccurrences();
      
      return baselineUniqueOccurrences;
   }
   
   private void generateBaselineUniqueOccurrences()
   {
      baselineUniqueOccurrences.clear();
      
      int ess[][] = Generation.getBESS(); //get encoded strand sequences
		int nvb = Generation.getNVB(); //get number of variable bases
      
      int tempSLC = 0;
      if(isIntramolecular) tempSLC = IntraOligoSLC;
      else tempSLC = InterOligoSLC;

      int lengthCounter = 0;
      
      for( int i = 0; i < L; i++) //for the overlap in the alignment
      {
         if( ess[S1][(S1B1 + i)] + ess[S2][(S2B1+(L-1)-i)] == 5 || ess[S1][(S1B1 + i)] + ess[S2][(S2B1+(L-1)-i)]== (2*nvb +11) ) //if the bases are complements
         {
            lengthCounter++;
         }
         
         else  // if the bases are not complements
         {
            if(lengthCounter >= tempSLC) // if the complement is long enough to count.
            {
               //count it
               if (baselineUniqueOccurrences.containsKey( lengthCounter)) // add the complement to the occurrence list.
               {
                  baselineUniqueOccurrences.replace( lengthCounter, (baselineUniqueOccurrences.get(lengthCounter) + 1) );
               } else baselineUniqueOccurrences.put(lengthCounter, 1);
            }
            lengthCounter = 0;
         }
      }
      
      //if the loop ends on a complement, count it.
      if( ess[S1][(S1B1 + (L-1))] + ess[S2][(S2B1)] == 5 || ess[S1][(S1B1 + (L-1))] + ess[S2][(S2B1)]== (2*nvb +11) )
      {
         if(lengthCounter >= tempSLC) // if the complement is long enough to count.
         {
            // Count it
            if (baselineUniqueOccurrences.containsKey( lengthCounter)) // add the complement to the occurrence list.
            {
               baselineUniqueOccurrences.replace( lengthCounter, (baselineUniqueOccurrences.get(lengthCounter) + 1) );
            } else baselineUniqueOccurrences.put(lengthCounter, 1);
         }
      }
      
      baselineUniqueOccurrencesUpToDate = true;
   }
   
   public Map<Integer,Integer> getUniqueOccurrences()
   {
      if( !UniqueOccurrencesUpToDate) generateUniqueOccurrences( );
      
      return UniqueOccurrences;
   }
   
   private void generateUniqueOccurrences( )
   {
      UniqueOccurrences.clear();
      
      int tempSLC = 0;
      if(isIntramolecular) tempSLC = IntraOligoSLC;
      else tempSLC = InterOligoSLC;

      int lengthCounter = 0;
      
      for( int i = 0; i < L; i++) //for the overlap in the alignment
      {
         if( ESS[S1][(S1B1 + i)] + ESS[S2][(S2B1+(L-1)-i)] == 5 ) //if the bases are complements
         {
            lengthCounter++;
         }
         
         else  // if the bases are not complements
         {
            if(lengthCounter >= tempSLC) // if the complement is long enough to count.
            {
               //count it
               if (UniqueOccurrences.containsKey( lengthCounter)) // add the complement to the occurrence list.
               {
                  UniqueOccurrences.replace( lengthCounter, (UniqueOccurrences.get(lengthCounter) + 1) );
               } else UniqueOccurrences.put(lengthCounter, 1);
            }
            lengthCounter = 0;
         }
      }
      
      //if the loop ends on a complement, count it.
      if( ESS[S1][(S1B1 + (L-1))] + ESS[S2][(S2B1)] == 5 )
      {
         if(lengthCounter >= tempSLC) // if the complement is long enough to count.
         {
            // Count it
            if (UniqueOccurrences.containsKey( lengthCounter)) // add the complement to the occurrence list.
            {
               UniqueOccurrences.replace( lengthCounter, (UniqueOccurrences.get(lengthCounter) + 1) );
            } else UniqueOccurrences.put(lengthCounter, 1);
         }
      }
      
      UniqueOccurrencesUpToDate = true;
   }
   
   public Map<Integer,Integer> getInterferenceUniqueOccurrences( )
   {
      if( !interferenceUniqueOccurrencesUpToDate) generateInterferenceUniqueOccurrences( );
      
      return interferenceUniqueOccurrences;
   }
   
   private void generateInterferenceUniqueOccurrences( )
   {
      interferenceUniqueOccurrences.clear();
      interferenceUniqueOccurrences.putAll(getUniqueOccurrences());      
      
      BBA.getBaselineUniqueOccurrences().entrySet().stream()
         .forEach( e -> 
         {
            if(interferenceUniqueOccurrences.containsKey(e.getKey())) interferenceUniqueOccurrences.replace( e.getKey(), (interferenceUniqueOccurrences.get(e.getKey())) - e.getValue());
               else interferenceUniqueOccurrences.put(e.getKey(), 0 - e.getValue());
         });
         
      while (interferenceUniqueOccurrences.containsValue( 0 ))
      {
         interferenceUniqueOccurrences.values().remove(0);
      }
      interferenceUniqueOccurrencesUpToDate = true;
   }

   public void outdateSequences()
   {
      UniqueOccurrencesUpToDate = false ;
      interferenceUniqueOccurrencesUpToDate = false ; 
      interferenceStructuresUpToDate = false ;
      interferenceUniqueOccurrencesUpToDate = false ;
   }
   
   public Set<Integer> getBADAssociations(int[][] IDA) // incoming Domain-Associations. get basealignment-domain associations
   {
      TreeSet<Integer> tempDL = new TreeSet<Integer>(); //domain length
      
      for( int i = 0; i < L; i++) //for the overlap in the alignment
      {
         int d1 = IDA[S1][S1B1+i]; //domain #1
         int d2 = IDA[S2][S2B1+(L-1)-i]; //domain #2
         
         if(!tempDL.contains(d1)) tempDL.add(d1);
         if(!tempDL.contains(d2)) tempDL.add(d2);
      }
      
      return tempDL;
      
   }
}