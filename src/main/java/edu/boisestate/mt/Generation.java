package edu.boisestate.mt;
import java.io.* ;
import java.util.Arrays;
import java.util.Vector;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.function.Function;
import java.util.Set;
import java.util.Optional;
import java.math.BigInteger;
import java.util.Scanner;

public class Generation
{
   //*******************************************
   //Call the MTlogger, which will print outputs
   //only if they are asked for. (Debug/Verbose)
   //*******************************************
   
   static MTlogger MTout = new MTlogger("Generation.java",3); //Name of the current file is "Generation.java", indent output 3 spaces.
   
   //*******************************************
   //Variables which may change with generation.
   //*******************************************   

   public int eds[][]; // Encoded Domain Sequences: array containing the sequences which compose each domain (format 1234 coding ACGT)
   public int ess[][]; // Encoded Strand Sequences: array containing the sequences which compose each strand (format 1234 coding ACGT)
   private int mess[][]; // mother's encoded strand sequences
   public char dds[][]; // Decoded-Domain-Sequences: array containing the sequences which compose each domain (format ACGT)
   public char dss[][]; // Decoded-Strand-Sequences: array containing the sequences which compose each strand (format ACGT)
   
   private BigInteger sfs = BigInteger.valueOf(0); // Strand-Fitness-Score
   private boolean sfsUpToDate = false;
   private BigInteger nfs = BigInteger.valueOf(0); // Network-Fitness-Score
   private boolean nfsUpToDate = false;
   private BigInteger tfs = BigInteger.valueOf(0);; // Total-Fitness-Score
   private boolean tfsUpToDate = false;
   
   private BigInteger sfsBaseline = BigInteger.valueOf(0);
   private boolean sfsBaselineUpToDate = false;
   private BigInteger nfsBaseline = BigInteger.valueOf(0);
   private boolean nfsBaselineUpToDate = false;
   private BigInteger tfsBaseline = BigInteger.valueOf(0);
   private boolean tfsBaselineUpToDate = false;
   
   static public String dt[] ; // Domain-Type
   
   
   private ArrayList<baseAlignment> intraAlignments = new ArrayList<baseAlignment>(); //generation-specific alignments
   private ArrayList<baseAlignment> interAlignments = new ArrayList<baseAlignment>(); //generation-specific alignmnets
   private Map<Integer,ArrayList<baseAlignment>> domainToAlignmentMap = new TreeMap<Integer,ArrayList<baseAlignment>>();


   //*****************************************************
   //Variables which will be the same for all generations.
   //*****************************************************
   
   public static int lsl;  //Longest-Strand-Length:
   public static int sl[]; //Strand-Lengths: length of the original parent strands.
   public static int isr[][][]; //Indexed-Strand-Recipes: strand recipes with domain names replaced with domain-indexes. 
   public static boolean biv[][]; //base-is-variable ->
   private static ArrayList<Integer> dSB;
   public static int nvb;     //Number-Variable-Bases: Constant storing the number of variable bases in the domain sequences.
   public static int bpm = 2;    // Bases-Per-Mutation: Maximum number of bases to swap per mutation.
   private static int AAslc;
   private static int CCslc;
   private static int GGslc;
   private static int TTslc;
   private static int IntraOligoSLC;
   private static int InterOligoSLC;
   private static BigInteger IntraOligoW;
   private static BigInteger InterOligoW;
   private static int MaxTranspositionAttempts = 1000; // Transposition-Attemps: Number of times to attempt to find a valid transposition
   private static int MaxShuffleAttempts = 1000; // Number of attempts to produce a valid design via random shuffle
   private static int MaxMutationAttempts = 1000;
   //   public static int slc;  //Scoring-Length-Criteria: complementary sections between strands will be scored only if they are longer than slc.
  
   public static DomainPool DA; // Domain-Ancestor: Reference to the original domain pool
   public static StrandPool SA; // Strand-Ancestor: Reference to the original strand pool

   private static int bess[][]; //Baseline-encoded-strand-sequences.
   private static ArrayList<baseAlignment> intraBaselineAlignments = new ArrayList<baseAlignment>();
   private static ArrayList<baseAlignment> interBaselineAlignments = new ArrayList<baseAlignment>();
   
   //private static Map<Integer,Map<Integer,Integer>> subOccurrences = new TreeMap<Integer,Map<Integer,Integer>>();
   private static Map<Integer,Map<Integer,Integer>> intraSubOccurrences = new TreeMap<Integer,Map<Integer,Integer>>();
   private static Map<Integer,Map<Integer,Integer>> interSubOccurrences = new TreeMap<Integer,Map<Integer,Integer>>();
   
   private static Map<Integer,Long> intraLengthScores = new TreeMap<Integer,Long>();
   private static Map<Integer,Long> interLengthScores = new TreeMap<Integer,Long>();
   
   private static HashSet<referencePosition> intraRPs;
   private static HashSet<referencePosition> interRPs;

   private ConcurrentHashMap<referencePosition, Long> intraScores;
   private ConcurrentHashMap<referencePosition, Long> interScores;
   
   private static ConcurrentHashMap<Integer,Set<referencePosition>> domainIntraRPMap;
   private static ConcurrentHashMap<Integer,Set<referencePosition>> domainInterRPMap;
   
   private ConcurrentHashMap<Integer,BigInteger> intraDTRPScores;
   private boolean intraDTRPScoresUpToDate = false;
   private ConcurrentHashMap<Integer,BigInteger> interDTRPScores;
   private boolean interDTRPScoresUpToDate = false;
   
   private static HashSet<Integer> uVDSet;
   
   // **********************************************************************
   // Constructor for creating a Generation from a DomainPool and StrandPool
   // **********************************************************************

   public Generation(DomainPool IncomingDomainAncestor, StrandPool IncomingStrandAncestor)
   {
      DA = IncomingDomainAncestor; //All Generation objects will be able to reference information from the DA DomainPool
      SA = IncomingStrandAncestor; //All Generation objects will be able to reference information from the SA StrandPool
      
      MTout.log("Copying Domain Types from DA");
      dt = copyDomainTypes( DA.getDT() );
      
      MTout.log("Indexing strand recipes");
      indexStrandRecipes();      //Fill the isr (Indexed-Strand-Recipes) array.
      
      MTout.log("Calculating strand lengths");
      calculateStrandlengths();  //Calculate sl[] array and lsl constant. (strand-lengths, and longest-strand-lengths)
      
      MTout.log("Calculating number of variable bases");
      calculateNumberVariableBases();
      
      MTout.log("Creating domain selection bag");
      createDomainSelectionBag();
      
      MTout.log("Create used Variable domains set");
      createUsedVariableDomainsSet();
      
      MTout.log("create base-is-variable array");
      createBIV();

      MTout.log("Copying encoded domain sequences from Domain Ancestor");
      eds = new int[DA.tnd][DA.ldl];
      copyEncodedDomainSequences(DA.getEDS(), eds);  // This may need to be fixed so that the DA is not changed.
      
      MTout.log("Creating encoded strand sequences");
      ess = new int[SA.tns][lsl+1];
      createEncodedStrandSequences();
      
      bess = new int[SA.tns][lsl+1];
      initializeBESS();
      
      MTout.log("Creating Reference Positions");
      referencePosition.initializeReferencePositions(this);
      intraRPs = referencePosition.getIntraReferencePositions();
      interRPs = referencePosition.getInterReferencePositions();

      MTout.log("Scoring Generation");
      getTFS();
      
      MTout.log("Creating DTRP Map");
      calculateDomainToRPMap();
   }

   // **********************************************
   // Constructor for creating a daughter generation
   // **********************************************
 
   public Generation(Generation IncomingMother)
   {     
      MTout.log("Copying encoded domain sequences from Mother to Daughter");
      eds = new int[DA.tnd][DA.ldl];
      copyEncodedDomainSequences(IncomingMother.eds, eds);

      MTout.log("Creating encoded strand sequences of Daughter");
      ess = new int[SA.tns][lsl+1];
      mess = new int[SA.tns][lsl+1];
      copyEncodedStrandSequences(IncomingMother.getESS(),ess);
      copyEncodedStrandSequences(IncomingMother.getESS(),mess);
      //createEncodedStrandSequences();
      
      //MTout.log("Creating RPS hash of daughter");
      
      //intraScores = new ConcurrentHashMap<referencePosition, Long>(getIntraScores(IncomingMother));
      //interScores = new ConcurrentHashMap<referencePosition, Long>(getInterScores(IncomingMother));
      //intraDTRPScores= new ConcurrentHashMap<Integer,Long>(IncomingMother.getIntraDTRPScores());
      //interDTRPScores= new ConcurrentHashMap<Integer,Long>(IncomingMother.getInterDTRPScores());
   }
   
   // **********************************************
   // Method for cloning another generation
   // **********************************************

   public void copy(Generation IncomingMother)
   { 
      
      MTout.log("Copying encoded domain sequences from Mother to Daughter");
      copyEncodedDomainSequences(IncomingMother.eds, eds);

      MTout.log("Creating encoded strand sequences of Daughter");
      copyEncodedStrandSequences(IncomingMother.getESS(),ess);
      copyEncodedStrandSequences(IncomingMother.getESS(),mess);
      
      //Disable Shortcut scoring, force re-calculation of score each generation.
      
      setSFS( IncomingMother.getSFS());
      sfsUpToDate = true;
      setNFS( IncomingMother.getNFS());
      nfsUpToDate = true;
      setTFS( IncomingMother.getTFS());
      tfsUpToDate = true;
	  
	  sfsBaseline = IncomingMother.sfsBaseline;
      sfsBaselineUpToDate = IncomingMother.sfsBaselineUpToDate;
	  nfsBaseline = IncomingMother.nfsBaseline;
      nfsBaselineUpToDate = IncomingMother.nfsBaselineUpToDate;
	  tfsBaseline = IncomingMother.tfsBaseline;
      tfsBaselineUpToDate = IncomingMother.tfsBaselineUpToDate;
      
      MTout.log("Copying intra scores from mother to daughter");
      //intraScores.clear();
      //intraScores.putAll(getIntraScores(IncomingMother));
      //getIntraScores(IncomingMother).entrySet().parallelStream().forEach( e -> intraScores.put(e.getKey(),e.getValue()));
      //intraDTRPScores.putAll(IncomingMother.getIntraDTRPScores());
      MTout.log("Copying inter scores from mother to daughter");
      //interScores.clear();
      //interScores.putAll(getInterScores(IncomingMother));
      //getInterScores(IncomingMother).entrySet().parallelStream().forEach( e -> interScores.put(e.getKey(),e.getValue()));
      //interDTRPScores.putAll(IncomingMother.getInterDTRPScores());
   }
   
   // ***********************************************
   // Method for calculating number of variable bases
   // ***********************************************
   
   public static void calculateNumberVariableBases()
   {
      nvb = 0;
      for(int i = 0; i < DA.tnd; i++) //iterate through all domains
      {
         if (DA.dt[i].equalsIgnoreCase("v") || DA.dt[i].equalsIgnoreCase("vs") ) // if the domain is variable
         {
            nvb = nvb + DA.dl[i]; 
         }
      }
   }
   
   // ******************************************
   // Method to Calculate the lengths of strands
   // ******************************************

   public static void calculateStrandlengths()
   {
      lsl = 0; // set longest strand length to start as zero
      sl = new int[SA.tns];
      for (int i = 0; i < SA.tns ; i++) // for all strands in the strand ancestor
      {
         sl[i] = 0; // set the ith strand length to start as zero...
         for (int j=0; j < SA.srl[i] ; j++) // for each recipe in the strand recipe
         {
            sl[i] = sl[i] + DA.dl[(isr[i][j][0])]; // increase sl[i] by the length of the domain referenced in isr
         }
         if (sl[i] > lsl) // if sl[i] is larger than largest yet, 
            {
               lsl = sl[i]; // replace lsl with sl[i]
            }
      }  
   }
   
   public static String[] copyDomainTypes( String[] IDT ) // IDT: IncomingDomainTypes
   {
      String tempDT[] = new String[ IDT.length ];
      for (int i = 0; i < IDT.length; i++)
      {
         tempDT[i] = IDT[i];
      }
      return tempDT;
   }
   
   // ***************************************************
   // Method to duplicate an eds array into a unique unit
   // ***************************************************
   
   public static void copyEncodedDomainSequences( int[][] motherEDS, int[][] daughterEDS)
   {
       // Choose size of the new eds array.
      for(int i=0; i < DA.tnd; i++) //iterate through all domains.
      {
         for(int j=0; j<DA.dl[i]; j++) //iterate through the length of the domain.
         {
            daughterEDS[i][j] = motherEDS[i][j]; //new eds value = previous eds value.
         }
      }
   }
      
   
   // ***************************************************
   // Method to duplicate an ess array into a unique unit
   // ***************************************************
   
   private static void copyEncodedStrandSequences( int[][] motherESS, int[][] daughterESS ) // copy ESS values from IG1 to IG2
   {
      for(int i=0; i < SA.tns; i++) //iterate through all strands.
      {
         for(int j=0; j < sl[i]; j++) //iterate through the length of the strand.
         {
            daughterESS[i][j] = motherESS[i][j]; //new eds value = previous eds value.
         }
      }
   }
   
   // *******************
   // Create Baseline EDS
   // *******************
   
   public static int[][] createBaselineEDS( int[][] Incomingeds ) //create an EDS array which contains ATCG fixed bases, and a unique base and complement for each variable base.
   {
	   int[][] tempEDS = new int[DA.tnd][DA.ldl]; //initialize the domain array
	   
	   // start variable base counter
	   int vbn = 0;	 // variable base number
	   
	   // for each doman
		for (int i = 0; i < DA.tnd; i++ ) // loop for all domains
		{
			int dl = DA.dl[i]; // "domain length" = the length stored in dl[i]
   
			//if the domain is variable, fill it with unique variable bases and increase the counter.
			if(DA.dt[i].equals("v") || DA.dt[i].equals("V") ||  DA.dt[i].equals("vs") || DA.dt[i].equals("Vs") || DA.dt[i].equals("vS") ||  DA.dt[i].equals("VS")  )
			{
				for(int j = 0; j < dl; j++) //iterate through each base in the domain
				{
					vbn++; // Increment the variable base number.
					tempEDS[i][j] = ( 5 + vbn);
				}
			}
			// otherwise copy the existing sequence...
			else 
			{
				for(int j = 0; j < dl; j++) //iterate through each base in the domain
				{
				  tempEDS[i][j] = Incomingeds[i][j];
				}
			}
         }
	   return tempEDS;
   }
   // ***************************
   // Create domain selection bag
   // ***************************
   
   public static void createDomainSelectionBag()
   {
      // put one base for each base of each variable domain into the domain selection bag
      dSB = new ArrayList<Integer>();
      
      for(int i = 0; i < SA.tns; i++) //iterate through all strands.
      {
         for(int j = 0; j <  SA.srl[i]; j++)   //iterate through each strand recipe.
         {
            int di = isr[i][j][0]; // "domain index" = the index stored in isr
            int dl = DA.dl[di]; // "domain length" = the length stored in dl[i]
            String dt = DA.dt[di];
            if(dt.equalsIgnoreCase("v") || dt.equalsIgnoreCase("vs") )
            {
               if (!dSB.contains(di))
               {
                  for(int k = 0; k<dl; k++)
                  {
                     dSB.add(di);
                  }
               }
            }
         }
      }
   }
   
   public static void createBIV()
   {
      // put one base for each base of each variable domain into the domain selection bag
      biv = new boolean[SA.tns][lsl+1];
      
      for(int i = 0; i < SA.tns; i++) //iterate through all strands.
      {
         int baseIndex =0;
         for(int j = 0; j <  SA.srl[i]; j++)   //iterate through each strand recipe.
         {
            int di = isr[i][j][0]; // "domain index" = the index stored in isr
            int dl = DA.dl[di]; // "domain length" = the length stored in dl[i]
            String dt = DA.dt[di];
            for(int k = 0; k<dl;k++)
            {
               if(dt.equalsIgnoreCase("f") )
               {
                  biv[i][baseIndex]= false;
               }else biv[i][baseIndex] = true;
               baseIndex++;
            }
         }
      }
   }
   
   public static void createUsedVariableDomainsSet()
   {
      uVDSet = new HashSet<Integer>();
      
      for(int i = 0; i < SA.tns; i++) //iterate through all strands.
      {
         for(int j = 0; j <  SA.srl[i]; j++)   //iterate through each strand recipe.
         {
            int di = isr[i][j][0]; // "domain index" = the index stored in isr
            String dt = DA.dt[di];
            if(dt.equalsIgnoreCase("v") || dt.equalsIgnoreCase("vs") )
            {
               if (!uVDSet.contains(new Integer(di)))
               {
                  uVDSet.add(new Integer(di));
               }
            }
         }
      }
   }

   // *************************************
   // Method createEncodedStrandSequences()
   // *************************************
   private static int[][] getEncodedStrandSequences( int[][] IEDS)
   {
      int[][] tempESS = new int[SA.tns][lsl+1];
      
      for(int i = 0; i < SA.tns; i++) //iterate through all strands.
      {
         int k = 0; // base position along the stand;
         for(int j = 0; j <  SA.srl[i]; j++)   //iterate through each strand recipe.
         {
            int di = isr[i][j][0]; // "domain index" = the index stored in isr
            int dl = DA.dl[di]; // "domain length" = the length stored in dl[i]
            
            if( isr[i][j][1] == 0) // if the domain is not a complement,
            {
               for(int l = 0; l < dl; l++) //iterate through each base in the domain
               {
                  tempESS[i][k] = IEDS[di][l];
                  k = k+1;
               }
            }
            
            if(isr[i][j][1] == 1) //if the domain is a complement,
            {
               for (int l = 0; l < dl; l++) // for each base in the domain
               {
                  if (IEDS[di][dl-1-l] < 5) // if the base is a traditional base
				  {
					tempESS[i][k] = (5-IEDS[di][dl-1-l]); //The output ess base is the reverse complement of the domain sequence.
				  }
				  if (IEDS[di][dl-1-l] > 5) // if the base is an undeclared variable base
				  {
					tempESS[i][k] = ((nvb*2)+11-IEDS[di][dl-1-l]); // the output ess base is the reverse complement of the variable domain.
				  }
				  
                  k = k+1;
               }
            }
         }
      }
      return tempESS;
   }
   public void createEncodedStrandSequences()
   {
      for(int i = 0; i < SA.tns; i++) //iterate through all strands.
      {
         int k = 0; // base position along the stand;
         for(int j = 0; j <  SA.srl[i]; j++)   //iterate through each strand recipe.
         {
            int di = isr[i][j][0]; // "domain index" = the index stored in isr
            int dl = DA.dl[di]; // "domain length" = the length stored in dl[i]
            
            if( isr[i][j][1] == 0) // if the domain is not a complement,
            {
               for(int l = 0; l < dl; l++) //iterate through each base in the domain
               {
                  ess[i][k] = eds[di][l];
                  k = k+1;
               }
            }
            
            if(isr[i][j][1] == 1) //if the domain is a complement,
            {
               for (int l = 0; l < dl; l++) // for each base in the domain
               {
                  if (eds[di][dl-1-l] < 5) // if the base is a traditional base
				  {
					ess[i][k] = (5-eds[di][dl-1-l]); //The output ess base is the reverse complement of the domain sequence.
				  }
				  if (eds[di][dl-1-l] > 5) // if the base is a undeclared variable base
				  {
					ess[i][k] = ((nvb*2)+11-eds[di][dl-1-l]); // the output ess base is the reverse complement of the variable domain.
				  }
				  
                  k = k+1;
               }
            }
         }
      }
   }
   
   private static int[][] getDomainAssociations()
   {
      int[][] tempDA = new int[SA.tns][lsl+1];
      
      for(int i = 0; i < SA.tns; i++) //iterate through all strands.
      {
         int k = 0; // base position along the stand;
         for(int j = 0; j <  SA.srl[i]; j++)   //iterate through each strand recipe.
         {
            int di = isr[i][j][0]; // "domain index" = the index stored in isr
            int dl = DA.dl[di]; // "domain length" = the length stored in dl[i]
            
            if( isr[i][j][1] == 0) // if the domain is not a complement,
            {
               for(int l = 0; l < dl; l++) //iterate through each base in the domain
               {
                  tempDA[i][k] = di;
                  k = k+1;
               }
            }
            
            if(isr[i][j][1] == 1) //if the domain is a complement,
            {
               for (int l = 0; l < dl; l++) // for each base in the domain
               {
                  if (DA.getEDS()[di][dl-1-l] < 5) // if the base is a traditional base
				  {
                  tempDA[i][k] = di;
				  }
				  if (DA.getEDS()[di][dl-1-l] > 5) // if the base is a undeclared variable base
				  {
                  tempDA[i][k] = di;
				  }
				  
                  k = k+1; 
               }
            }
         }
      }
      return tempDA;
   }
   
   public static int[][] createBaselineEncodedStrandSequences( int[][] beds)
   {
      int[][] tempESS = new int[SA.tns][lsl+1];
      
      for(int i = 0; i < SA.tns; i++) //iterate through all strands.
      {
         int k = 0; // base position along the stand;
         for(int j = 0; j <  SA.srl[i]; j++)   //iterate through each strand recipe.
         {
            int di = isr[i][j][0]; // "domain index" = the index stored in isr
            int dl = DA.dl[di]; // "domain length" = the length stored in dl[i]
            
            if( isr[i][j][1] == 0) // if the domain is not a complement,
            {
               for(int l = 0; l < dl; l++) //iterate through each base in the domain
               {
                  tempESS[i][k] = beds[di][l];
                  k = k+1;
               }
            }
            
            if(isr[i][j][1] == 1) //if the domain is a complement,
            {
               for (int l = 0; l < dl; l++) // for each base in the domain
               {
                  if (beds[di][dl-1-l] < 5) // if the base is a traditional base
				  {
					tempESS[i][k] = (5-beds[di][dl-1-l]); //The output ess base is the reverse complement of the domain sequence.
				  }
				  if (beds[di][dl-1-l] > 5) // if the base is a undeclared variable base
				  {
					tempESS[i][k] = ((nvb*2)+ 11 - beds[di][dl-1-l]); // the output ess base is the reverse complement of the variable domain.
				  }
				  
                  k = k+1;
               }
            }
         }
      }
      return tempESS;
   }

   // ******************************************************
   // Method for creating the indexed strand recipe array
   // Rather than containing the strand domain names, this 
   // array will contain the domain index's
   // ******************************************************

   public static void indexStrandRecipes()
   {
      isr = new int[SA.tns][SA.msrl][2]; // declare the size of isr.
      for(int i = 0; i < SA.tns; i++) // itterate i through the total number of strands
      {
         for(int j = 0; j < SA.srl[i]; j++) // itterate j through the strand recipe length of strand i
         {
            int k = 0; //begin counter for tracking domain index
            int ck = 0; // Check if domain name has been found in the domain pool.
            
            while(ck == 0 && k!= DA.mnd) //search for strand recipe value in the domain names.
            {
               if(SA.sr[i][j].equals(DA.dn[k])) // found it?
               {
                  isr[i][j][0] = k;    //set domain's index number (k) to the current location in irs
                  isr[i][j][1] = 0; // set the second element = 0, indicating the value is for a direct domain
                  ck = 1;           //set checking flag. We found it!
               }
               k = k + 1; //increment k
            }
            
            k = 0; // reset domain index counter.
            while(ck == 0 && k!= DA.mnd) //search for strand recipe value in the domain names.
            {
               if(SA.sr[i][j].equals("c." + DA.dn[k]) || SA.sr[i][j].equals("C." + DA.dn[k])) // if the recipe element is the complement of the domain name,
               {
                  isr[i][j][0] = k;    //set domain's index number (k) to the current location in irs
                  isr[i][j][1] = 1;    //set the second element = 1, indicating the [0] value is for a complement domain.
                  ck = 1;           //set checking flag. We found it!
               }
               k = k + 1; //increment k
            }
            
            if (ck == 0 ) //Didn't find it? bummer...
            {
               System.out.println("Error:: Could not find domain " + SA.sr[i][j]);
               System.out.println("Error:: Forcing Stop ");
               System.exit(0);
            }
         }
      }
   }

   // ****************************************
   // Method for printing domain selection bag
   // ****************************************
   
   public static void printDomainSelectionBag()
   {
      System.out.println("Printing domain selection bag (dsb[i])");
      System.out.print("dsb = ");
      dSB.stream().forEach(e-> System.out.print(e+"-"));
      System.out.println();
   }


   // **************************************
   // Method to print indexed strand recipes
   // **************************************

   public static void printIndexedRecipes()
   {
      System.out.println("Printing indexed strand recipes (isr[i][j][0] ; dn[i])");
      for(int i = 0; i < SA.tns; i++) // iterate through all strands.
      {
         for( int j = 0; j < SA.srl[i]; j++) // iterate through all domains in the strand recipe.
         {
            System.out.println("isr[" + i + "][" + j + "][0] = " + isr[i][j][0] + "; " + SA.sr[i][j] );
         }
      }
      System.out.println();
   }
   
   // *****************************************
   // Method to print calculated strand lengths
   // *****************************************

   public static void printStrandLengths()
   {
      System.out.println("Printing strand lengths (sl[i])");
      for(int i = 0; i < SA.tns; i++) // iterate through all strands.
      {
            System.out.println("sl[" + i + "] = " + sl[i]);
      }
      System.out.println();
      System.out.println("Printing longest strand length (lsl)");
      System.out.println("lsl = " + lsl);
      System.out.println();
   }

   // ****************************************
   // Method to select the domain for mutation
   // ****************************************

   public static int selectDomainForMutation()
   {
      Random tempRand = new Random();
      int bi = tempRand.nextInt(dSB.size());
      int di = dSB.get(bi);
      return di;
   }

   // ********************************
   // Method for permuting an array
   // ********************************
   
   public void newRandPermute( int IDI) //Incoming-Domain-Index randomly shuffle the domain
   {
      Random rnd = ThreadLocalRandom.current();
      for (int i = DA.dl[IDI] - 1; i > 0; i--)
      {
      int index = rnd.nextInt(i + 1);
      // Simple swap
      int a = eds[IDI][index];
      eds[IDI][index] = eds[IDI][i];
      eds[IDI][i] = a;
      }
      updateScores(IDI);
   }
   
   public void newPermute( int IDI) // incoming domain-index
   {
      Random rnd = ThreadLocalRandom.current();
      int i1 = rnd.nextInt( DA.dl[IDI]);
      int i2 = rnd.nextInt( DA.dl[IDI]);
      while (i2 == i1) i2 = rnd.nextInt( DA.dl[IDI]);
      int a = eds[IDI][i2];
      eds[IDI][i2] = eds[IDI][i1];
      eds[IDI][i1] = a;
   }
   
   public static void randpermute(int n, int a[], int p[]) // n= domain length, a[] = original sequences, p[] = sequence to be changed
   {
      double[] ra = new double[n]; // array that will be filled with random numbers.
      int[] pa = new int[n]; // permutation array.
   
      for (int i = 0; i < n; i++ ) // filling an array with random numbers.
      {
         ra[i] = Math.random(); // fill ra with random numbers
         pa[i] = i; // fill pa with indices
      }
      
      // Generating the permutation array //
      int j = 1;
      while( j < n) //for all indexes j
      {
         if( ra[j] > ra[j-1]) // if the j'th member of ra is larger than the previous value,
         {
             double r1 = ra[j-1]; //copy j-1'th to r1
             double r2 = ra[j];  //copy j'th to r2
             ra[j-1] = r2; // replace j-1'th with r2
             ra[j] = r1;   // replace j'th with r1
             int i1 = pa[j-1]; // do the same thing with the permutation array.
             int i2 = pa[j];
             pa[j-1] = i2;
             pa[j] = i1;
             j = 0;
         }
         j = j + 1;
      }

      // Performing the permutation //
      for (int i = 0; i < n; i ++ )
      {
         int k = pa[i];
         p[i] = a[k];
      }
   }

   // *********************************************************************
   // Method of permuting m randomly choosen elements in an array of size n
   // *********************************************************************
  

   public static void permute(int m, int n, int a[], int p[]) // m = size of domain to permute, n = number of bases to shuffle, a[] = the original domain, p[] = mutated copy of a[] to return.
   {

      int[] ai = new int[m]; // The array of array indexes to be permuted. 

      int[] bag = new int[n]; // The bag from which we draw array indices
      
      for (int i = 0; i < n; i++) // for all indexes in the array,
      {
         bag[i] = i; // add the index to the bag.
      }

      for (int i = 0; i < m; i++) // for each base to be permuted,
      {
         int j = (int)((n - i)*Math.random()); // pick a bag index
         ai[i] = bag[j]; // store index in index array
         for (int k = j; k < n - i -1 ; k++)
         {
            bag[k] = bag[k+1]; // remove picked element from bag
         }
      }

      int[] ap = new int[m]; // The permutation of the ai array
      randpermute(m, ai, ap);

      // Generating the permuted array p

      for (int i = 0; i < n; i++)
      {
         p[i] = a[i];
      }

      for (int i = 0; i < m; i++)
      {
         p[ap[i]] = a[ai[i]]; // Performing the permutation
      }
   }
   
   // ****************************
   // Method for mutating a domain
   // ****************************

   public void mutateDomain( int di ) // domain index
   {
      int[][] tempeds = new int[DA.tnd][DA.ldl];
      copyEncodedDomainSequences(eds,tempeds);
      int AC = 0; // AC: Attempt-Counter
      while (Arrays.deepEquals(tempeds, eds) && AC < 40) // until the new eds != the previous eds, or until max number of attempts is reached
      {
         //permute( bpm , DA.dl[di] , tempeds[di] , eds[di] ); // Permute the di'th domain with bp changes.
         newPermute( di );
         createEncodedStrandSequences(); // Update the encoded strand sequences.
         updateScores(di);
         AC++;
      }
     
   }
   
   public void printAntiParallelStrandAlignment(int si1, int si2, int rp) // si1 = strand index 1, si2= strand index 2, rp = relative position
   {
      System.out.println("Printing anti-parallel strand alignment of strands " + si1 + " and " + si2 + " at reference position rp = "+ rp );
      for( int k =0 ; k < sl[si2]-1; k++) // print 1 less space than strand 2 is long.
      {
         System.out.print(' ');
      }
      
      for (int k=0; k < sl[si1] ; k ++) // print strand 1.
      {
         System.out.print(ess[si1][k]);
      }
      System.out.println(); // end the line
      
      for( int k =0 ; k < rp; k++) // print rp number of spaces
      {
         System.out.print(' ');
      }
      
      for (int k = 0; k < sl[si2] ; k++) //iterate through strand 2's bases
      {
         System.out.print(ess[si2][sl[si2]-1-k]); //print strand 2's bases in reverse order.
      }
      System.out.println(); // end line
      System.out.println(); // Create a space
   }
      
   
   // *****************************************************************
   // Method for printing the overlap between two anti-parallel strands
   // *****************************************************************
   
   public void printAntiParallelStrandOverlap( int si1, int si2, int rp) // si1 = strand index 1, si2= strand index 2, rp = relative position
   {
      System.out.println("Printing the overlap between Anti-Parallel aligned strand numbers " + si1 + " and " + si2 + " at reference position " +rp);
      for( int i = Math.max( 0 , rp +1  - sl[si2] ); i < Math.min( rp + 1 , sl[si1]); i++) // for the overlap range 
      {
         System.out.print(ess[si1][i]); //print the elements of strand 1 on a line.
      }
      System.out.println(); // end the line
      
      for( int i = Math.max( 0 , rp +1  - sl[si2] ); i < Math.min( rp + 1 , sl[si1]); i++) // for the overlap range 
      {
//         System.out.print(ess[si2][Math.min( sl[si2],rp-i)]);  //print the elements of strand 2 on a line.
         System.out.print(ess[si2][rp-i]);   
      }
      System.out.println(); // end the line
      System.out.println(); // Create a space
   }
   
   // *****************************************************
   // Method for filling the decoded strand sequences array
   // *****************************************************
   
   public void decodeDomainSequences()
   {
      dds = new char[DA.tnd][DA.ldl];
      for( int i = 0; i < DA.tnd; i ++ ) // for all domains
      {
         for( int j = 0; j < DA.dl[i]; j ++)
         {
            switch( eds[i][j])
            {
               case 1:
                  dds[i][j] = 'A';
                  break;
               case 2:
                  dds[i][j] = 'C';
                  break;
               case 3:
                  dds[i][j] = 'G';
                  break;
               case 4:
                  dds[i][j] = 'T';
                  break;
            }
         }
      }
   }
   
   // *****************************************************
   // Method for filling the decoded strand sequences array
   // *****************************************************
   
   public void decodeStrandSequences()
   {
      dss = new char[SA.tns][lsl];
      for( int i = 0; i < SA.tns; i ++ ) // for all strands
      {
         for( int j = 0; j < sl[i]; j ++)
         {
            switch( ess[i][j])
            {
               case 1:
                  dss[i][j] = 'A';
                  break;
               case 2:
                  dss[i][j] = 'C';
                  break;
               case 3:
                  dss[i][j] = 'G';
                  break;
               case 4:
                  dss[i][j] = 'T';
                  break;
            }
         }
      }
   }
   
   // *****************************
   // Method for shuffling a domain
   // *****************************

   public void shuffleSeededDomains()
   {
      
      int[][] tempeds = new int[DA.tnd][DA.ldl];
      copyEncodedDomainSequences(eds, tempeds);
      for( int i = 0; i < DA.tnd; i++ )
      {
         if (dt[i].equalsIgnoreCase("vs") )
            {
               randpermute( DA.dl[i], tempeds[i], eds[i] ); 
               dt[i] = "v";
            }
      }
      createEncodedStrandSequences();
   }
   
   private static int[][] getShuffledEDS(int[][] IEDS)
   {
      int[][] tempEDS = new int[DA.tnd][DA.ldl];
      copyEncodedDomainSequences(IEDS,tempEDS);
      int AC = 0; // AC: Attempt-Counter
      while (Arrays.deepEquals(tempEDS, IEDS) && AC < 100) // until the new eds != the previous eds, or until max number of attempts is reached
      {
         for( Integer IDI : uVDSet) // for all variable domains used in the design.
         {
            Random rnd = ThreadLocalRandom.current();
            for (int i = DA.dl[IDI] - 1; i > 0; i--) // for each base in the domain
            {
               int index = rnd.nextInt(i + 1);
               // Simple swap
               int a = tempEDS[IDI][index];
               tempEDS[IDI][index] = tempEDS[IDI][i];
               tempEDS[IDI][i] = a;
            }
            dt[IDI] ="v";
         }
         AC++;
      }
      return tempEDS;
   }
   
   public static void randomizeDesign( Generation IG ) throws Exception // Incoming-Generation
   {
      
      boolean validity = false;
      int[][] IEDS = IG.getEDS();
      int[][] tempEDS= new int[1][1];
      int[][] tempESS= new int[1][1];
      
      for(int i = 0; i < MaxShuffleAttempts; i++) // for up to the maximum number of shuffle attempts
      {
         tempEDS = getShuffledEDS(IEDS);
         tempESS = getEncodedStrandSequences( tempEDS );
         //IG.transposeDomain( di ); // Select then mutate a domain.
         validity =  checkValidity(tempESS);
         if (validity) {break;};
      }

      if(!validity )
      {
         System.out.println("Failed to identify a valid design given " + MaxShuffleAttempts + " randomly seeded attempts. Program aborted, increase MaxShuffleAttempts or refine network design.");
         throw new RuntimeException("Failed to identify valid design");
         
      }else 
      {
         //update the eds
         //update the ess
         IG.setEDS( tempEDS);
         IG.setESS( tempESS);
         IG.updateScores();
      }
   }
   
   public static void randomizeDeviceItteratively( Generation IG ) throws Exception // Incoming-Generation
   {
      
      boolean deviceValidity = false;
      int[][] IEDS = IG.getEDS();
      int[][] shuffledEDS = getShuffledEDS(IEDS);
      int[][] tempEDS= getBEDS();
      int[][] tempESS= createBaselineEncodedStrandSequences(tempEDS);
      
      for(int totalShuffles = 1; deviceValidity == false && totalShuffles < MaxShuffleAttempts; totalShuffles++)
      {
         shuffledEDS = getShuffledEDS(IEDS);
         tempEDS = getBEDS();
         tempESS = createBaselineEncodedStrandSequences(tempEDS);
         
         currentCompleteShuffle:
         for(int i =0; i < DA.tnd; i++) //for each domain
         {
            //System.out.println("Working on domain "+ i);
            deviceValidity = false;
            
            for (int j = 0; j < DA.dl[i] ; j++) // for each base in the domain
            {
               tempEDS[i][j] = shuffledEDS[i][j];
               //System.out.print(tempEDS[i][j]);
            }
            //System.out.println();
            tempESS = createBaselineEncodedStrandSequences(tempEDS);
            
            //System.out.println("Validity is :" + checkValidity(tempESS));
            
            if (checkValidity(tempESS))
            {
               deviceValidity = true;
            }
            else
            {
               for (int reshuffles = 1; reshuffles < 100 && deviceValidity == false && totalShuffles < MaxShuffleAttempts; reshuffles++)
               {
                  Random rnd = ThreadLocalRandom.current();
                  for (int k = DA.dl[i] - 1; k > 0; k--) // for each base in the domain
                  {
                     int index = rnd.nextInt(k + 1);
                     // Simple swap
                     int a = tempEDS[i][index];
                     tempEDS[i][index] = tempEDS[i][k];
                     tempEDS[i][k] = a;
                  }
                  totalShuffles++;
                  tempESS=createBaselineEncodedStrandSequences(tempEDS);
                  deviceValidity= checkValidity(tempESS);
               }
            }
            if (deviceValidity == false)
            {
               break currentCompleteShuffle;
            }
         }
      }
      if(!deviceValidity )
      {
         System.out.println("Failed to identify a valid randomly seeded device given " + MaxShuffleAttempts + " attempts. Program aborted, increase MaxShuffleAttempts or refine network design.");
         throw new RuntimeException("Failed to identify valid device during random seeding");
         
      }else 
      {
         //update the eds
         //update the ess
         IG.setEDS( tempEDS);
         IG.setESS( tempESS);
         IG.updateScores();
      }

   }
   
   public static void validatedMutation( Generation IG) //Incoming-Generation
   {
      int di = 0;
      boolean validity = false;
      int[][] IEDS = IG.getEDS();
      int[][] tempEDS= new int[1][1];
      int[][] tempESS= new int[1][1];
      for(int i = 0; i < MaxMutationAttempts; i++) // for up to the maximum number of transposition attempts
      {
         di = IG.selectDomainForMutation();
         tempEDS = getMutatedEDS(IEDS,di);
         tempESS = getEncodedStrandSequences( tempEDS );
         //IG.transposeDomain( di ); // Select then mutate a domain.
         validity =  checkValidity(tempESS);
         if (validity) {break;};
      }
     
      if(!validity )
      {
         System.out.println("Failed to identify a valid transposition given " + MaxTranspositionAttempts + " randomly seeded attempts. Increase 'MaxTranspositionAttempts' or refine network design.");
      }else 
      {
         //update the eds
         //update the ess
         IG.setEDS( tempEDS);
         IG.setESS( tempESS);
         IG.updateScores(di);
      }
   }
   
   private static int[][] getMutatedEDS(int[][] IEDS, int IDI)
   {
      int[][] tempEDS = new int[DA.tnd][DA.ldl];
      copyEncodedDomainSequences(IEDS,tempEDS);
      int tempDL = DA.dl[IDI];
      int AC = 0; // AC: Attempt-Counter
      while (Arrays.deepEquals(tempEDS, IEDS) && AC < 100) // until the new eds != the previous eds, or until max number of attempts is reached
      {
         // permute the IDI'th domain.
         Random tempRND = new Random(); // Permute the di'th domain with bp changes.
         Integer[] tempArray = Arrays.stream( IEDS[IDI] ).boxed().toArray( Integer[]::new );
         
         ArrayList<Integer> targetDomain = new ArrayList<Integer>(Arrays.asList(tempArray));
         int b1 = tempRND.nextInt(tempDL);
         int b2 = tempRND.nextInt(tempDL);
         
         Integer tempInt = targetDomain.get(b1);
         
         targetDomain.set(b1, targetDomain.get(b2));
         targetDomain.set(b2, tempInt);
         
         for(int i = 0; i < tempDL; i++)
         {
            tempEDS[IDI][i] = targetDomain.get(i).intValue();
         }
         
         //tempEDS[IDI] = targetDomain.toArray(new int[]);
         //createEncodedStrandSequences(); // Update the encoded strand sequences.
         AC++;
      }
      return tempEDS;
   }
   
   // *******************************
   // Method for transposing a domain
   // *******************************
   
   public void transposeDomain( int di )
   {
      int[][] tempeds = new int[DA.tnd][DA.ldl];
      //copyEncodedDomainSequences(eds,tempeds);
      int AC = 0; // AC: Attempt-Counter
      while (Arrays.deepEquals(tempeds, eds) && AC < 100) // until the new eds != the previous eds, or until max number of attempts is reached
      {
         
         transpose( DA.dl[di] , eds[di], tempeds[di] ); // Permute the di'th domain with bp changes.
         createEncodedStrandSequences(); // Update the encoded strand sequences.
         AC++;
      }
   }
   
   private static int[][] getTransposedEDS(int[][] IEDS, int IDI)
   {
      int[][] tempEDS = new int[DA.tnd][DA.ldl];
      copyEncodedDomainSequences(IEDS,tempEDS);
      int tempDL = DA.dl[IDI];
      int AC = 0; // AC: Attempt-Counter
      while (Arrays.deepEquals(tempEDS, IEDS) && AC < 100) // until the new eds != the previous eds, or until max number of attempts is reached
      {
         // permute the IDI'th domain.
         Random tempRND = new Random(); // Permute the di'th domain with bp changes.
         Integer[] tempArray = Arrays.stream( IEDS[IDI] ).boxed().toArray( Integer[]::new );
         
         ArrayList<Integer> targetDomain = new ArrayList<Integer>(Arrays.asList(tempArray));
         int b1 = tempRND.nextInt(tempDL);
         int b2 = tempRND.nextInt(tempDL);
         
         List<Integer> removedStretch = new ArrayList<Integer> (targetDomain.subList(Math.min(b1,b2), Math.max(b1,b2)));
         targetDomain.subList(Math.min(b1,b2), Math.max(b1,b2)).clear();
         
         if (b1 < b2) {Collections.reverse(removedStretch);}
         
         int b3 = tempRND.nextInt(tempDL - (Math.max(b1,b2)-Math.min(b1,b2)));
         
         targetDomain.addAll(b3, removedStretch);
         
         for(int i = 0; i < tempDL; i++)
         {
            tempEDS[IDI][i] = targetDomain.get(i).intValue();
         }
         
         //tempEDS[IDI] = targetDomain.toArray(new int[]);
         //createEncodedStrandSequences(); // Update the encoded strand sequences.
         AC++;
      }
      return tempEDS;
   }
   
   
   public static void validatedTransposition( Generation IG) //Incoming-Generation
   {
      int di = 0;
      boolean validity = false;
      int[][] IEDS = IG.getEDS();
      int[][] tempEDS= new int[1][1];
      int[][] tempESS= new int[1][1];
      for(int i = 0; i < MaxTranspositionAttempts; i++) // for up to the maximum number of transposition attempts
      {
         di = IG.selectDomainForMutation();
         tempEDS = getTransposedEDS(IEDS,di);
         tempESS = getEncodedStrandSequences( tempEDS );
         //IG.transposeDomain( di ); // Select then mutate a domain.
         validity =  checkValidity(tempESS);
         if (validity) {break;};
      }
     
      if(!validity )
      {
         System.out.println("Failed to identify a valid transposition given " + MaxTranspositionAttempts + " randomly seeded attempts. Increase 'MaxTranspositionAttempts' or refine network design.");
      }else 
      {
         IG.setEDS( tempEDS);
         IG.setESS( tempESS);
         IG.updateScores(di);

      }
      
   }
   
   private void setEDS(int[][] IEDS)
   {
      copyEncodedDomainSequences( IEDS, eds);
   }
   
   private void setESS(int[][] IESS)
   {
      copyEncodedStrandSequences( IESS, ess);
   }
   
   public static void transpose( int m, int[] ID, int[] TD) // m = length of the domain , ID = incoming domain to be unchanged, TD = transposed copy of domain.
   {
      int isb[] = new int[m]; //isb = Index-Selection-Bag
      int bagSize = m; //size of the bag which will be variable later.
      
      for( int i = 0; i < m; i ++) // for each base
      {
         isb[i] = i; // add an elment to the domain selection bag.
      }
      
      int a = (int) ( bagSize*Math.random() ); // select an element from the isb
      int indexA = isb[a]; //
      
      for ( int i = a; i < bagSize - 1 ; i ++ ) // remove the element from the isb
      { 
         isb[i] = isb[i+1];
      }
      bagSize--;      

      int b = (int) ( bagSize*Math.random() ); //select an element from the isb
      int indexB = isb[b];
      
      for ( int i = b; i < bagSize - 1 ; i ++ ) //remove the element from the isb
      { 
         isb[i] = isb[i+1];
      }
      bagSize--;      

      int fragmentLength = (Math.abs(indexA-indexB) + 1); 
      int fragmentIndexes[] = new int[ fragmentLength ];
      
      if( indexA < indexB ) 
      {
         fragmentIndexes[0]= indexA; // set the first index in the array to be the selected A index
         fragmentIndexes[ fragmentLength - 1 ] = indexB; // set the final index in the array to be the selected B index.
         
         for (int i = 1; i < fragmentLength - 1 ; i++) //for each base in the interior of the fragment
         {
            fragmentIndexes[i] = isb[ indexA + (i - 1) ]; // copy the index to the fragmentIndexes
         }
         
         bagSize= bagSize - ( fragmentLength - 2 );
         for (int i = indexA; i < bagSize ; i++) // for each base in the interior of the fragment
         {
            isb[ i ] = isb[ i + (fragmentLength-2) ]; //remove the base from the isb
         }
         
      }
      
      if( indexA > indexB ) 
      {
         fragmentIndexes[0]= indexA; // set the first index in the array to be the selected A index
         fragmentIndexes[ fragmentLength - 1 ] = indexB; // set the final index in the array to be the selected B index.
         
         for (int i = 1; i < fragmentLength - 1 ; i++) //for each base in the interior of the fragment
         {
            fragmentIndexes[i] = isb[ indexA - (i + 1) ]; // copy the index to the fragmentIndexes
         }
         
         bagSize = bagSize - ( fragmentLength - 2 );
         for (int i = indexB; i < bagSize ; i++) // for each base in the interior of the fragment
         {
            isb[ i ] = isb[ i + (fragmentLength-2) ]; //remove the base from the isb
         }
         
      }
           
      if (bagSize != 0)
      {
      
         int c = (int) ( bagSize*Math.random() );
         int attempts = 0;
         /*while ( c == a-1 && attempts < 5)
         {
            c = (int) ( bagSize*Math.random() ); // select an index from the isb
         }
         */
         int indexC = isb[c];

         for ( int i = 0; i < fragmentLength ; i ++ ) //for each element in the fragment
         {
            for ( int j = bagSize ; j > c + i  ; j-- ) // for all indexes after index c 
            {
               //System.out.println("i = "+i+" ; j = "+j);
               isb[ j ] = isb[ j - 1]; // move all indexes down 1 base
            }
            bagSize++; 
            
            isb[ c + i + 1 ] = fragmentIndexes[ i ]; // insert the element of the fragment
         }
          
      }
      
      for (int i = 0; i < bagSize; i ++)
      {
         int index = isb[i];
         TD[i] = ID[ index ];
      }
   }

   static public void importSettings( String ParametersFilePath ) throws Exception
   {
     //MTout.log("Importing Settings for Scoring() module from "+ ParametersFilePath );
      
      // *******************
      // Load default values
      // *******************
      
      MaxShuffleAttempts = 1000;
      MaxTranspositionAttempts = 1000;
      MaxMutationAttempts = 1000;
      
      IntraOligoSLC = 1;
      InterOligoSLC = 1;
      
      IntraOligoW = BigInteger.valueOf(10000);
      InterOligoW = BigInteger.valueOf(1);
      
      AAslc = 6;
      CCslc = 3;
      GGslc = 3;
      TTslc = 6;
      
      // ****************************
      // Check parameters File for options
      // ****************************
		
		try
		{
			File file = new File(ParametersFilePath);
			Scanner scanner1 = new Scanner(file);
			while( scanner1.hasNextLine()) // for each line of input file, until end of file
			{
				String lineText = scanner1.nextLine();
				Scanner scanner2 = new Scanner(lineText);
				if( !lineText.startsWith("//") && scanner2.hasNext())
				{
					switch (scanner2.next())
					{
						case "IntraOligoW":
						scanner2.next(); // Skip = sign
						try{
							IntraOligoW = new BigInteger((String)scanner2.next());
							System.out.println("IntraOligoW value imported. Accepted value: " + IntraOligoW);
						}
						catch (Exception a)
						{
							throw new Exception("Invalid value for IntraOligoW");
						}
						if (IntraOligoW.compareTo(BigInteger.valueOf(0)) == -1)
						{
							throw new Exception((IntraOligoW + " is not a valid value for IntraOligoW"));
						}
						break;
						
						case "InterOligoW":
						scanner2.next(); // Skip = sign
						try{
							InterOligoW = new BigInteger((String)scanner2.next());
							System.out.println("InterOligoW value imported. Accepted value: " + InterOligoW);
						}
						catch (Exception a)
						{
							throw new Exception("Invalid value for InterOligoW");
						}
						if (InterOligoW.compareTo(BigInteger.valueOf(0)) == -1)
						{
							throw new Exception((InterOligoW + " is not a valid value for InterOligoW"));
						}
						break;
						
						default:
						break;
					}
				}
				scanner2.close();
			}  
			scanner1.close();
		}
		catch (Exception e)
		{
			System.out.println("Error while importing parameters from "+ ParametersFilePath + " :: ");
			System.out.println(e.getMessage());
			System.exit(0);
		}
		
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
            else if (streamtokenizer.sval.equalsIgnoreCase("MaxShuffleAttempts"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval > 0)
               {
                  MaxShuffleAttempts = (int)streamtokenizer.nval;
                  System.out.println("MaxShuffleAttempts value imported. Accepted value: "+ MaxShuffleAttempts);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"MaxShuffleAttempts\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"MaxShuffleAttempts\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("MaxMutationAttempts"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval > 0)
               {
                  MaxMutationAttempts = (int)streamtokenizer.nval;
                  System.out.println("MaxMutationAttempts value imported. Accepted value: " + MaxMutationAttempts);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"MaxMutationAttempts\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"MaxMutationAttempts\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("MaxTranspositionAttempts"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval > 0)
               {
                  MaxTranspositionAttempts = (int)streamtokenizer.nval;
                  System.out.println("MaxTranspositionAttempts value imported. Accepted value: " + MaxTranspositionAttempts);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"MaxTranspositionAttempts\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"MaxTranspositionAttempts\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("AAslc"))
            {
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 1)
               {
                  AAslc = (int)streamtokenizer.nval;
                  System.out.println("AAslc value imported. Accepted value: " + AAslc);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"AAslc\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"AAslc\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("CCslc"))
            {
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 1)
               {
                  CCslc = (int)streamtokenizer.nval;
                  System.out.println("CCslc value imported. Accepted value: " + CCslc);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"CCslc\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"CCslc\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("GGslc"))
            {
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 1)
               {
                  GGslc = (int)streamtokenizer.nval;
                  System.out.println("GGslc value imported. Accepted value: " + GGslc);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"GGslc\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"GGslc\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("TTslc"))
            {
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 1)
               {
                  TTslc = (int)streamtokenizer.nval;
                  System.out.println("TTslc value imported. Accepted value: " + TTslc);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"TTslc\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"TTslc\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
         }
      }
   }
   
   static public int[][][] countBases ( Generation IGs[])
   {
      // For all Generations (i) in IGs
      
      // For all strands (j) in IG
      
      // For all bases (k) in Strand
      
      // Record the identity of of base k at 
      
      int tempArray[][][] = new int[SA.tns][lsl][5]; // set the array to have dimensions equal to the number of strands, and the longest strand length
      
      for ( Generation IG : IGs) // for all the generations recieved.
      {
         for(int j = 0; j < SA.tns; j++) // For all strands in the design
         {
            for(int k = 0; k < sl[j]; k++) // for all bases in the strand.
            {
               switch (IG.ess[j][k])
               {
                  case 1: tempArray[j][k][1]++;
                     break;
                  case 2: tempArray[j][k][2]++;
                     break;
                  case 3: tempArray[j][k][3]++;
                     break;
                  case 4: tempArray[j][k][4]++;
                     break;
               }
            }
         }
      }
      
      return tempArray;
   }
   
   static public void printBaseCount ( int BaseCount[][][])
   {
      //For each strand
      System.out.println("Printing total number of counted bases.");
      
      for(int j = 0; j < SA.tns; j++) // For all strands in the design
      {
         
         System.out.print("Strand #" + (j+1) + " A's Counted: ");
         for(int k = 0; k < sl[j]; k++) // for all bases in the strand.
         {
               System.out.print((BaseCount[j][k][1]));
               if( k+1 < sl[j]) System.out.print(",");
         }
         System.out.println();
         System.out.print("Strand #" + (j+1) + " C's Counted: ");
         for(int k = 0; k < sl[j]; k++) // for all bases in the strand.
         {
               System.out.print((BaseCount[j][k][2]));
               if( k+1 < sl[j]) System.out.print(",");
         }
         System.out.println();
         System.out.print("Strand #" + (j+1) + " G's Counted: ");
         for(int k = 0; k < sl[j]; k++) // for all bases in the strand.
         {
               System.out.print((BaseCount[j][k][3]));
               if( k+1 < sl[j]) System.out.print(",");
         }
         System.out.println();
         System.out.print("Strand #" + (j+1) + " T's Counted: ");
         for(int k = 0; k < sl[j]; k++) // for all bases in the strand.
         {
               System.out.print((BaseCount[j][k][4]));
               if( k+1 < sl[j]) System.out.print(",");
         }
         System.out.println();
      }
   }
   
   static public void printFractionCG ( int BaseCount[][][])
   {
      //For each strand
      System.out.println("Printing information from counted bases.");
      
      for(int j = 0; j < SA.tns; j++) // For all strands in the design
      {
         System.out.print("Strand #" + (j+1) + " Fraction C/G: ");
         for(int k = 0; k < sl[j]; k++) // for all bases in the strand.
         {
               System.out.print( ((BaseCount[j][k][2]+BaseCount[j][k][3])*100/(BaseCount[j][k][1]+BaseCount[j][k][2]+BaseCount[j][k][3]+BaseCount[j][k][4]) /(double)100 ));
               if( k+1 < sl[j]) System.out.print(",");
         }
         System.out.println();
      }
   }
   
   public DomainPool getDA()
   {
      return DA;
   }
   
   public StrandPool getSA()
   {
      return SA;
   }
   
   public static int[] getSL()
   {
      return sl;
   }
   
   public static char[] getST()
   {
      return SA.getST();
   }
   
   public int[][] getESS()
   {
      return ess;
   }
   
   public static int getTNS()
   {
      return SA.tns;
   }
   
   public BigInteger getSFS()
   {
      if( !sfsUpToDate) calculateSFS();
      return sfs;
   }
   
   public void setSFS( BigInteger ISFS ) // Incoming-Strand-Fitness-Score
   {
      sfs = ISFS;
   }
   
   public void setSFS( long ISFS ) // Incoming-Strand-Fitness-Score
   {
      sfs = BigInteger.valueOf(ISFS);
   }
   
   public BigInteger getNFS()
   {
      if( !nfsUpToDate) calculateNFS();
      return nfs;
   }
  
   public void setNFS( BigInteger INFS ) // Incoming-Network-Fitness-Score
   {
      nfs = INFS;
   }
   
   public void setNFS( long IFS ) // Incoming-Strand-Fitness-Score
   {
      tfs = BigInteger.valueOf(IFS);
   }
   
   public BigInteger getTFS()
   {
      if(!tfsUpToDate) calculateTFS();
      return tfs;
   }
   
   public void setTFS( BigInteger ITFS ) // Incoming-Total-Fitness-Score
   {
      tfs = ITFS;
   }
   
   public void setTFS( long IFS ) // Incoming-Strand-Fitness-Score
   {
      tfs = BigInteger.valueOf(IFS);
   }
   
   public String[] getDT()
   {
      return dt;
   }
   
   
   public static int getNVB ()
   {
	   return nvb;
   }
   
   
   public static ArrayList<baseAlignment> getAllAlignments()
   {
      ArrayList<baseAlignment> tempBAL = new ArrayList<baseAlignment>();
      tempBAL.addAll(getIntraBaselineAlignments());
      tempBAL.addAll(getInterBaselineAlignments());
      return tempBAL;
   }
   
   public static ArrayList<baseAlignment> getIntraBaselineAlignments()
   {
      if (intraBaselineAlignments.size() == 0) // if Alignments has not been initialized
      {
         intraBaselineAlignments = generateIntraBaselineAlignments();
      }
      
      return intraBaselineAlignments;
   }
  
   public static ArrayList<baseAlignment> generateIntraBaselineAlignments()
   {
      ArrayList<baseAlignment> tempBAL = new ArrayList<baseAlignment>();

      int tns = getTNS(); //get total number of strands
      int nvb = getNVB(); //get number of variable bases
      int[][] BESS = getBESS();

      for( int i = 0; i < tns; i++ ) // for all strands in the system,
      {
         
         for (int RP = 1; RP < sl[i]; RP++) // for all reference positions
         {
             
            //(1st alignment: No base in bulge)
            
            int tempS1= i;
            int tempS1B1 = Math.max(0,sl[i]-(2*RP));
            int tempS2 = i;
            int tempS2B1 = sl[i] - RP;
            int tempLength =  Math.min( sl[i] - RP , RP );
            
            baseAlignment tempBA = new baseAlignment(tempS1, tempS1B1, tempS2, tempS2B1, tempLength, true, BESS);
            tempBAL.add( tempBA );
            
            
            //baseAlignment.printAlignmentInformation(tempBA);
            //baseAlignment.printAlignment(tempBA);
            
            if ( (RP + 1) < sl[i] ) // 2nd alignment: 1 base in bulge
            {

               tempS1 = i;
               tempS1B1 = Math.max(0,sl[i] - (2*RP) - 1 );
               tempS2 = i;
               tempS2B1 = sl[i] - RP ;
               tempLength = Math.min( sl[i] - RP - 1, RP );
               
               tempBA = new baseAlignment(tempS1, tempS1B1, tempS2, tempS2B1, tempLength, true, BESS);
               tempBAL.add( tempBA );
               
               //baseAlignment.printAlignmentInformation(tempBA);
               //baseAlignment.printAlignment(tempBA);
            }
         }
      }
      return tempBAL;
   }
   
   public static ArrayList<baseAlignment> getInterBaselineAlignments()
   {
      if (interBaselineAlignments.size() == 0)
      {

         int tns = getTNS(); //get total number of strands
         int[] sl = getSL();  // get strand lengths

         for( int i = 0; i < tns; i++ ) // for all strands in the system,
         { 
            for (int j = i; j < tns; j++) //for all sets of strands that have not yet been considered
            {
               for (int rp = 0; rp < sl[i] + sl[j] - 1; rp++) // for all reference positions, 
               {

                  int tempS1= i;
                  int tempS1B1 = Math.max( 0 , ( sl[i]-1 ) - rp );
                  int tempS1B2 = Math.min( sl[i]-1 , (sl[i]+sl[j] -2 )-rp);
                  int tempS2 = j;
                  int tempS2B1 = Math.max( 0 , ( sl[j]-1 ) - rp ) ;
                  int tempLength = tempS1B2-tempS1B1 + 1;
                  //int tempLength =  Math.min( rp + 1 , sl[i]) - Math.max( 0 , rp + 1  - sl[j] ) ;
                  
                  baseAlignment tempBA = new baseAlignment(tempS1, tempS1B1, tempS2, tempS2B1, tempLength, false, getBESS());
                  interBaselineAlignments.add( tempBA );
                  //baseAlignment.printAlignmentInformation( tempBA);
               }
            }
         }
      }
      return interBaselineAlignments;
   }
   private static int[][] initializeBESS()
   {
      int[][] tempBEDS = new int[DA.tnd][DA.ldl]; //baseline-Encoded-Domain-Sequences
         
      // start variable base counter
      int vbn = 0;	 // variable base number
      
      // for each doman
      for (int i = 0; i < DA.tnd; i++ ) // loop for all domains
      {
         int dl = DA.dl[i]; // "domain length" = the length stored in dl[i]
   
         //if the domain is variable, fill it with unique variable bases and increase the counter.
         if(DA.dt[i].equals("v") || DA.dt[i].equals("V") ||  DA.dt[i].equals("vs") || DA.dt[i].equals("Vs") || DA.dt[i].equals("vS") ||  DA.dt[i].equals("VS")  )
         {
            for(int j = 0; j < dl; j++) //iterate through each base in the domain
            {
               vbn++; // Increment the variable base number.
               tempBEDS[i][j] = ( 5 + vbn);
            }
         }
         // otherwise copy the existing sequence...
         else 
         {
            for(int j = 0; j < dl; j++) //iterate through each base in the domain
            {
              tempBEDS[i][j] = DA.getEDS()[i][j];
            }
         }
      }
      
      // assemble strand sequences
      bess = createBaselineEncodedStrandSequences( tempBEDS);
      return bess;
   }
   
   private static int[][] getBEDS()
   {
      int[][] tempBEDS = new int[DA.tnd][DA.ldl]; //baseline-Encoded-Domain-Sequences
         
      // start variable base counter
      int vbn = 0;	 // variable base number
      
      // for each doman
      for (int i = 0; i < DA.tnd; i++ ) // loop for all domains
      {
         int dl = DA.dl[i]; // "domain length" = the length stored in dl[i]
   
         //if the domain is variable, fill it with unique variable bases and increase the counter.
         if(DA.dt[i].equals("v") || DA.dt[i].equals("V") ||  DA.dt[i].equals("vs") || DA.dt[i].equals("Vs") || DA.dt[i].equals("vS") ||  DA.dt[i].equals("VS")  )
         {
            for(int j = 0; j < dl; j++) //iterate through each base in the domain
            {
               vbn++; // Increment the variable base number.
               tempBEDS[i][j] = ( 5 + vbn);
            }
         }
         // otherwise copy the existing sequence...
         else 
         {
            for(int j = 0; j < dl; j++) //iterate through each base in the domain
            {
              tempBEDS[i][j] = DA.getEDS()[i][j];
            }
         }
      }
      return tempBEDS;
   }
   
   public static int[][] getBESS()
   {
      return bess;
   }
   
   public static ArrayList<WCStructure> getIntraBaselineStructures()
   {
      ArrayList<WCStructure> tempCL =  
         getIntraBaselineAlignments()
         .stream()
         .map( (e) -> e.getBaselineStructures() )
         .collect(ArrayList<WCStructure>::new,
	    		(response, element) -> response.addAll(element),
	    		(response1, response2) -> Stream.concat( response1.stream(),response2.stream()) );
            
         //.reduce( new ArrayList<WCStructure>(), (a, b) -> {a.addAll(b);return a;} );
         
      return tempCL;
   }
   
   public static ArrayList<WCStructure> getInterBaselineStructures()
   {
      ArrayList<WCStructure> tempCL =  
         getInterBaselineAlignments()
         .stream()
         .map( (e) -> e.getBaselineStructures() )
         .collect(ArrayList<WCStructure>::new,
	    		(response, element) -> response.addAll(element),
	    		(response1, response2) -> Stream.concat( response1.stream(),response2.stream()) );
         
      return tempCL;
   }
   
   public ArrayList<baseAlignment> getIntraAlignments()
   {
      if (intraAlignments.size() == 0)
      {
         intraAlignments = generateIntraAlignments();
      }
      return intraAlignments;
   }
   
   private ArrayList<baseAlignment> generateIntraAlignments()
   {
      ArrayList<baseAlignment> tempIA = new ArrayList<baseAlignment>();
      getIntraBaselineAlignments().stream().forEach( e -> tempIA.add( new baseAlignment( e , ess ) ) );
      return tempIA;
   }
   
   public ArrayList<baseAlignment> getInterAlignments()
   {
      if (interAlignments.size() == 0)
      {
         interAlignments = generateInterAlignments();
      }
      return interAlignments;
   }
   
   private ArrayList<baseAlignment> generateInterAlignments()
   {
      ArrayList<baseAlignment> tempIA = new ArrayList<baseAlignment>();
      
      getInterBaselineAlignments().stream().forEach( e -> tempIA.add( new baseAlignment( e , ess ) ) );
      return tempIA;
   }
   
   public ArrayList<WCStructure> getIntraUniqueStructures()
   {
      ArrayList<WCStructure> tempSL = 
         getIntraAlignments().stream()
         .map(e -> e.getStructures())
         .collect(ArrayList<WCStructure>::new,
            (response, element) -> response.addAll(element),
            (r1,r2) -> r1.addAll(r2) );
      return tempSL;
   }
   
   public ArrayList<WCStructure> getInterUniqueStructures()
   {
      ArrayList<WCStructure> tempSL = 
         getInterAlignments().stream()
         .map(e-> e.getStructures())
         .collect(ArrayList<WCStructure>::new,
            (response, element) -> response.addAll(element),
            (r1,r2) -> r1.addAll(r2) );
      return tempSL;
   }
   
   public ArrayList<WCStructure> getIntraInterferenceStructures()
   {
      ArrayList<WCStructure> tempSL = new ArrayList<WCStructure>();
      getIntraAlignments().stream().forEach(e -> tempSL.addAll( e.getInterferenceStructures() ) );
      return tempSL;
   }
   
   public ArrayList<WCStructure> getInterInterferenceStructures()
   {
      ArrayList<WCStructure> tempSL = new ArrayList<WCStructure>();
      getInterAlignments().stream().forEach(e -> tempSL.addAll( e.getInterferenceStructures() ) );
      return tempSL;
   }
   
   public Map<Integer,Integer> getIntraBaselineUniqueOccurrences()
   {
      Map<Integer,Integer> tempOL = 
         getIntraBaselineAlignments().parallelStream()
         .map( (e) -> e.getBaselineUniqueOccurrences() )
         .collect( 
            TreeMap<Integer,Integer>::new,
            //combine two Length-Count Tree maps
            (response, element) -> 
            {
               element.keySet().stream().forEach( eKey ->
               {  if ( response.containsKey( eKey)) response.replace( eKey, (response.get(eKey) + element.get(eKey) ) );
                  else response.put(eKey, element.get(eKey)); });
            },
            (r1,r2) -> r2.keySet().stream().forEach( eKey ->
               {  if ( r1.containsKey( eKey)) r1.replace( eKey, (r1.get(eKey) + r2.get(eKey) ) );
                  else r1.put(eKey, r2.get(eKey)); }));   

      return tempOL;
   }
   
   public Map<Integer,Integer> getInterBaselineUniqueOccurrences()
   {
      Map<Integer,Integer> tempOL = 
         getInterBaselineAlignments().parallelStream()
         .map( (e) -> e.getBaselineUniqueOccurrences() )
         .collect( 
            TreeMap<Integer,Integer>::new,
            //combine two Length-Count Tree maps
            (response, element) -> 
            {
               element.keySet().stream().forEach( eKey ->
               {  if ( response.containsKey( eKey)) response.replace( eKey, (response.get(eKey) + element.get(eKey) ) );
                  else response.put(eKey, element.get(eKey)); });
            },
            (r1,r2) -> r2.keySet().stream().forEach( eKey ->
               {  if ( r1.containsKey( eKey)) r1.replace( eKey, (r1.get(eKey) + r2.get(eKey) ) );
                  else r1.put(eKey, r2.get(eKey)); }));   

      return tempOL;
   }
   
   public Map<Integer,Integer> getIntraUniqueOccurrences()
   {
      Map<Integer,Integer> tempOL = 
         getIntraAlignments().parallelStream()
         .map( (e) -> e.getUniqueOccurrences() )
         .collect( 
            TreeMap<Integer,Integer>::new,
            //combine two Length-Count Tree maps
            (response, element) -> 
            {
               element.keySet().stream().forEach( eKey ->
               {  if ( response.containsKey( eKey)) response.replace( eKey, (response.get(eKey) + element.get(eKey) ) );
                  else response.put(eKey, element.get(eKey)); });
            },
            (r1,r2) -> r2.keySet().stream().forEach( eKey ->
               {  if ( r1.containsKey( eKey)) r1.replace( eKey, (r1.get(eKey) + r2.get(eKey) ) );
                  else r1.put(eKey, r2.get(eKey)); }));   

      return tempOL;
   }
   
   public Integer getSSValue()
   {
	   return getInterInterferenceUniqueOccurrences().lastKey();
   }
   
   public SortedMap<Integer,Integer> getInterUniqueOccurrences()
   {
      SortedMap<Integer,Integer> tempOL = 
         getInterAlignments().parallelStream()
         .map( (e) -> e.getUniqueOccurrences() )
         .collect( 
            TreeMap<Integer,Integer>::new,
            //combine two Length-Count Tree maps
            (response, element) -> 
            {
               element.keySet().stream().forEach( eKey ->
               {  if ( response.containsKey( eKey)) response.replace( eKey, (response.get(eKey) + element.get(eKey) ) );
                  else response.put(eKey, element.get(eKey)); });
            },
            (r1,r2) -> r2.keySet().stream().forEach( eKey ->
               {  if ( r1.containsKey( eKey)) r1.replace( eKey, (r1.get(eKey) + r2.get(eKey) ) );
                  else r1.put(eKey, r2.get(eKey)); }));   

      return tempOL;
   }
   
   public TreeMap<Integer,Integer> getIntraInterferenceUniqueOccurrences()
   {
      TreeMap<Integer,Integer> tempOL = 
         getIntraAlignments().parallelStream()
         .map( (e) -> e.getInterferenceUniqueOccurrences() )
         .collect( 
            TreeMap<Integer,Integer>::new,
            //combine two Length-Count Tree maps
            (response, element) -> 
            {
               element.keySet().stream().forEach( eKey ->
               {  if ( response.containsKey( eKey)) response.replace( eKey, (response.get(eKey) + element.get(eKey) ) );
                  else response.put(eKey, element.get(eKey)); });
            },
            (r1,r2) -> r2.keySet().stream().forEach( eKey ->
               {  if ( r1.containsKey( eKey)) r1.replace( eKey, (r1.get(eKey) + r2.get(eKey) ) );
                  else r1.put(eKey, r2.get(eKey)); }));   
      return tempOL;
   }
   
   public TreeMap<Integer,Integer> getInterInterferenceUniqueOccurrences()
   {
      TreeMap<Integer,Integer> tempOL = 
         getInterAlignments().parallelStream()
         .map( (e) -> e.getInterferenceUniqueOccurrences() )
         .collect( 
            TreeMap<Integer,Integer>::new,
            //combine two Length-Count Tree maps
            (response, element) -> 
            {
               element.keySet().stream().forEach( eKey ->
               {  if ( response.containsKey( eKey)) response.replace( eKey, (response.get(eKey) + element.get(eKey) ) );
                  else response.put(eKey, element.get(eKey)); });
            },
            (r1,r2) -> r2.keySet().stream().forEach( eKey ->
               {  if ( r1.containsKey( eKey)) r1.replace( eKey, (r1.get(eKey) + r2.get(eKey) ) );
                  else r1.put(eKey, r2.get(eKey)); }));   
      return tempOL;
   }
  
   
   public TreeMap<Integer,Integer> makeIntraUniqueOccurrencesComplete (Map<Integer,Integer> IOL) //incoming Occurrence-List
   {
      // allocate a new treemap
      TreeMap<Integer,Integer> tempOL = new TreeMap<Integer,Integer>();
      // for each key in the IOL
      IOL.entrySet().stream()
      // calculate or retrieve the occurrence-profile of this length complement.
         .forEach( IOLentry -> 
         {
            // for each key in the occurrence-profile, add them (times number of IOL occurrences) to the new treemap.
            getIntraSubOccurrences( IOLentry.getKey() ).entrySet().stream()
            .forEach( SOentry ->
            {
               if (tempOL.containsKey( SOentry.getKey())) tempOL.put( SOentry.getKey(), (IOLentry.getValue()*SOentry.getValue() +  tempOL.get( SOentry.getKey())));
               else tempOL.put(SOentry.getKey(), IOLentry.getValue()*SOentry.getValue());
            });
         }); 
      return tempOL;
   }
      
   public TreeMap<Integer,Integer> makeInterUniqueOccurrencesComplete (Map<Integer,Integer> IOL) //incoming Occurrence-List
   {
      // allocate a new treemap
      TreeMap<Integer,Integer> tempOL = new TreeMap<Integer,Integer>();
      // for each key in the IOL
      IOL.entrySet().stream()
      // calculate or retrieve the occurrence-profile of this length complement.
         .forEach( IOLentry -> 
         {
            // for each key in the occurrence-profile, add them (times number of IOL occurrences) to the new treemap.
            getInterSubOccurrences( IOLentry.getKey() ).entrySet().stream()
            .forEach( SOentry ->
            {
               if (tempOL.containsKey( SOentry.getKey())) tempOL.put( SOentry.getKey(), (IOLentry.getValue()*SOentry.getValue() +  tempOL.get( SOentry.getKey())));
               else tempOL.put(SOentry.getKey(), IOLentry.getValue()*SOentry.getValue());
            });
         }); 
      return tempOL;
   }
   
   public  TreeMap<Integer,Integer> getIntraCompleteOccurrences()
   {
      return makeIntraUniqueOccurrencesComplete( getIntraUniqueOccurrences() );
   }
   
   public  TreeMap<Integer,Integer> getInterCompleteOccurrences()
   {
      return makeInterUniqueOccurrencesComplete( getInterUniqueOccurrences() );
   }
   
   public  TreeMap<Integer,Integer> getIntraBaselineCompleteOccurrences()
   {
      return makeIntraUniqueOccurrencesComplete( getIntraBaselineUniqueOccurrences() );
   }
   
   public  TreeMap<Integer,Integer> getInterBaselineCompleteOccurrences()
   {
      return makeInterUniqueOccurrencesComplete( getInterBaselineUniqueOccurrences() );
   }
   
   public  TreeMap<Integer,Integer> getIntraInterferenceCompleteOccurrences()
   {
      return makeIntraUniqueOccurrencesComplete( getIntraInterferenceUniqueOccurrences() );
   }
   
   public  TreeMap<Integer,Integer> getInterInterferenceCompleteOccurrences()
   {
      return makeInterUniqueOccurrencesComplete( getInterInterferenceUniqueOccurrences() );
   }
   
   public static Map<Integer,Integer> getIntraSubOccurrences ( int IL)
   {
      if(!intraSubOccurrences.containsKey( IL ))
      {
         Map<Integer,Integer> tempOL = new TreeMap<Integer,Integer>();
         
         for (int j = IL ; j > 0 && j >= IntraOligoSLC; j--) // for all lengths shorter than  or equal to this length, but longer than SLC
            {
               tempOL.put( j , IL - j + 1);
            }
         intraSubOccurrences.put(IL, tempOL);
      }
      return intraSubOccurrences.get(IL);
   }
   
   public static Map<Integer,Integer> getInterSubOccurrences ( int IL )
   {
      if(!interSubOccurrences.containsKey( IL ))
      {
         Map<Integer,Integer> tempOL = new TreeMap<Integer,Integer>();
         
         for (int j = IL ; j > 0 && j >= InterOligoSLC; j--) // for all lengths shorter than  or equal to this length, but longer than SLC
            {
               tempOL.put( j , IL - j + 1);
            }
         interSubOccurrences.put(IL, tempOL);
      }
      return interSubOccurrences.get(IL);
   }
   
   public void calculateSFS()
   {
      //sfs = intraScores.values().parallelStream().mapToLong(i->i).sum();
      MTout.log("Calculating SFS from intra RPs");
      sfs = intraRPs.parallelStream()
         .map(i->referencePosition.scoreRP(i,ess))
         .reduce( (a,b)-> a.add(b) )
         .get();
      sfsUpToDate = true;
   }
   
   public BigInteger getSFSBaseline()
   {
      //sfs = intraScores.values().parallelStream().mapToLong(i->i).sum();
		if(!sfsBaselineUpToDate)
		{
			MTout.log("Calculating Baseline SFS from intra RPs");
			sfsBaseline = intraRPs.parallelStream()
				.map(i->referencePosition.getBaselineScore(i))
				.reduce( (a,b)-> a.add(b) )
				.get();
			sfsBaselineUpToDate = true;
		}
		return sfsBaseline;
   }
   
   public BigInteger getNFSBaseline()
   {
		if(!nfsBaselineUpToDate)
		{
			MTout.log("Calculating Baseline NFS from intra RPs");
			nfsBaseline = interRPs.parallelStream()
				.map(i->referencePosition.getBaselineScore(i))
				.reduce( (a,b)-> a.add(b) )
				.get();
			nfsBaselineUpToDate = true;
		}
		return nfsBaseline;
   }
   
   public BigInteger getTFSBaseline()
   {
      //sfs = intraScores.values().parallelStream().mapToLong(i->i).sum();
		if (!tfsBaselineUpToDate)
		{
			tfsBaseline = (getSFSBaseline().multiply(IntraOligoW)).add(getNFSBaseline().multiply(InterOligoW));
			tfsBaselineUpToDate = true;
		}
		return tfsBaseline;
   }
   
   private long scoreIntraComplementLength ( int IL )
   {
      //either read or calculate the score for a given length
      if (!intraLengthScores.containsKey(IL))
      {
         long tempScore = 
            getIntraSubOccurrences( IL ).entrySet().stream()
            .mapToLong( OLentry-> (OLentry.getValue() * scoringFunction( OLentry.getKey() )))
            .sum();
         intraLengthScores.put( IL, tempScore);
            
            
         // get sub-complement-occurrences
         // for each length in SCO, calculate the score.
         // sum scores, store for next time.
      }

      return intraLengthScores.get( IL );
   }
   
   public void calculateNFS()
   {

      MTout.log("Calculating NFS from inter RPs");
      //nfs = interRPs.parallelStream().mapToLong(i->referencePosition.scoreRP(i,ess)).sum();
      nfs = interRPs.parallelStream()
         .map(i->referencePosition.scoreRP(i,ess))
         .reduce( (a,b)-> a.add(b) )
         .get();
      nfsUpToDate = true;
   }
   
   private long scoreInterComplementLength ( int IL )
   {
      //either read or calculate the score for a given length
      if (!interLengthScores.containsKey(IL))
      {
         long tempScore = 
            getInterSubOccurrences( IL).entrySet().stream()
            .mapToLong( OLentry-> (OLentry.getValue() * scoringFunction( OLentry.getKey() )))
            .sum();
         interLengthScores.put( IL, tempScore);
            
            
         // get sub-complement-occurrences
         // for each length in SCO, calculate the score.
         // sum scores, store for next time.
      }

      return interLengthScores.get( IL );
   }
   
   private long scoringFunction (int IL ) //incoming length
   {
      long tempScore = 1;
      
      if (IL <= 10)
      {
         tempScore = (long) Math.pow(10,IL);
         
      } else tempScore = (long)(Math.pow(10,10) * Math.pow(IL,2)) ;
      return tempScore;
   }
   
   private void calculateTFS()
   {
      tfs = (getSFS().multiply(IntraOligoW)).add(getNFS().multiply(InterOligoW));
      tfsUpToDate =true;
   }
   
   public static boolean checkValidity( Generation IG)
   {
      return checkValidity(IG.ess);
   }
   
   private static boolean checkValidity(int[][] IESS)
   {
		boolean Validity = true;
      
      // ************************************
      // Count Unfavorable Stretches of Bases 
      // Generate Occurrence list for 
      // ************************************
     
     int tns = getTNS(); //get total number of strands
     int ess[][] = IESS; //get encoded strand sequences
     int sl[] = getSL();  // get strand lengths
     
     search:
     for(int i = 0; i < tns; i++) // iterate through all strands.
     {
       //System.out.println("Scoring Strand " + i);
       //System.out.print("Strand bases:");
        
       int Cc = 0; // number of c's in a row counter
       int Gc = 0; // number of g's in a row counter
       int Ac = 0; // number of a's in a row counter
       int Tc = 0; // number of t's in a row counter

       int b0 = ess[i][0]; //set the first case
       int b1 = 0;
       boolean containsVariableBase = false;
       

       switch(b0) //start the first run
       {
         case 1: // if the coded sequence = 1 = A
            Ac = Ac + 1;
            break;
         case 2: // if the coded sequence = 2 = C
            Cc = Cc + 1;
            break;
         case 3: // if the coded sequence = 3 = G
            Gc = Gc + 1;
            break;
         case 4: // if the coded sequence = 4 = T
            Tc = Tc + 1;
            break;
            
       };

       for(int j = 1; j <= sl[i]; j++) // for all bases j in the strand i (starting at base 1, not zero)
       {
         //System.out.print(b0);
         b0 = ess[i][j-1]; // set b0 to be the previous base

         if( j < sl[i]) // if b1 will be within the strand
         {
            b1 = ess[i][j]; // set b1 to be the current base
         } 
         else if (j == sl[i] ) // if b1 will be beyond the strand
         {
            b1 = 0; // let b1 = 0 so the previous base and runs can be scored.
         };
         
         if(b1 == b0) // if this base is the same as the previous base.
         {
            if(biv[i][j]) {containsVariableBase = true;}
            switch(b0) //increment the proper counter.
            {
              case 1:
                Ac = Ac + 1;
                break;
              case 2:
                Cc = Cc + 1;
                break;
              case 3:
                Gc = Gc + 1;
                break;
              case 4:
                Tc = Tc + 1;
                break;
            };
         };
         
         
         if(b1 != b0) // if the two bases are not equal
         {
            switch(b0) // record the score from the consecutive base counter, reset counter
            {
              case 1:
                if( Ac > AAslc && containsVariableBase)
                {
                   Validity = false; 
                   break search;
                }
                Ac = 0;
                break;
              case 2:
                if( Cc > CCslc && containsVariableBase) 
                {
                   Validity = false; 
                   break search;
                }
                Cc = 0;
                break;
              case 3:
                if( Gc > GGslc && containsVariableBase) 
                {
                   Validity = false; 
                   break search;
                }
                Gc = 0; 
                break;
              case 4:
                if( Tc > TTslc && containsVariableBase) 
                {
                   Validity = false; 
                   break search;
                }
                Tc = 0;
                break;
            };
            containsVariableBase = biv[i][j];
            
            switch(b1) //start the next run
            {
              case 1: // if the coded sequence = 1 = A
                Ac = Ac + 1;
                break;
              case 2: // if the coded sequence = 2 = C
                Cc = Cc + 1; 
                break;
              case 3: // if the coded sequence = 3 = G
                Gc = Gc + 1;
                break;
              case 4: // if the coded sequence = 4 = T
                Tc = Tc + 1;
                break;
              case 0:
                break;
            };
         };
       }
       //System.out.println();
     }
     return Validity;
   }
   
   private static boolean checkDomainValidity(int[][] IEDS, int IDI)
   {
		boolean Validity = true;
      
      // ************************************
      // Count Unfavorable Stretches of Bases 
      // Generate Occurrence list for 
      // ************************************
     
      //If the domain is not variable, just call it valid.
      if(!(DA.dt[IDI].equals("v") || DA.dt[IDI].equals("V") ||  DA.dt[IDI].equals("vs") || DA.dt[IDI].equals("Vs") || DA.dt[IDI].equals("vS") ||  DA.dt[IDI].equals("VS")))
      {
         return true;
      }

      int dl = DA.dl[IDI];
      int Cc = 0; // number of c's in a row counter
      int Gc = 0; // number of g's in a row counter
      int Ac = 0; // number of a's in a row counter
      int Tc = 0; // number of t's in a row counter

      int b0 = IEDS[IDI][0]; //set the first case
      int b1 = 0;

      switch(b0) //start the first run
      {
      case 1: // if the coded sequence = 1 = A
         Ac = Ac + 1;
         break;
      case 2: // if the coded sequence = 2 = C
         Cc = Cc + 1;
         break;
      case 3: // if the coded sequence = 3 = G
         Gc = Gc + 1;
         break;
      case 4: // if the coded sequence = 4 = T
         Tc = Tc + 1;
         break;
      };
          
      search:
      for(int i=1; i <= dl; i++) // for all bases in the domain starting at base 1, not 0.
      {
         b0 = IEDS[IDI][i-1]; // set b0 to be the previous base

         if( i < dl) // if b1 will be within the strand
         {
            b1 = IEDS[IDI][i]; // set b1 to be the current base
         } 
         
         else if (i == dl ) // if b1 will be beyond the strand
         {
            b1 = 0; // let b1 = 0 so the previous base and runs can be scored.
         };
         
         if(b1 == b0) // if this base is the same as the previous base.
         {
            switch(b0) //increment the proper counter.
            {
              case 1:
                Ac = Ac + 1;
                if( Ac > AAslc) {Validity = false; break search;};
                break;
              case 2:
                Cc = Cc + 1;
                if( Cc > CCslc) {Validity = false; break search;};
                break;
              case 3:
                Gc = Gc + 1;
                if( Gc > GGslc) {Validity = false; break search;};
                break;
              case 4:
                Tc = Tc + 1;
                if( Tc > TTslc) {Validity = false; break search;};
                break;
            };
         };
         
         
         if(b1 != b0) // if the two bases are not equal
         {
            switch(b0) // record the score from the consecutive base counter, reset counter
            {
              case 1:
                if( Ac > AAslc) {Validity = false; break search;};
                Ac = 0;
                break;
              case 2:
                if( Cc > CCslc) {Validity = false; break search;};
                Cc = 0;
                break;
              case 3:
                if( Gc > GGslc) {Validity = false; break search;};
                Gc = 0; 
                break;
              case 4:
                if( Tc > TTslc) {Validity = false; break search;};
                Tc = 0;
                break;
            };
            switch(b1) //start the next run
            {
              case 1: // if the coded sequence = 1 = A
                Ac = Ac + 1;
                break;
              case 2: // if the coded sequence = 2 = C
                Cc = Cc + 1; 
                break;
              case 3: // if the coded sequence = 3 = G
                Gc = Gc + 1;
                break;
              case 4: // if the coded sequence = 4 = T
                Tc = Tc + 1;
                break;
              case 0:
                break;
            };
         };
       }     
     return Validity;
   }
   
   private void calculateIntraDTRPScores()
   {
      intraDTRPScores = new ConcurrentHashMap<Integer,BigInteger>();
      
      getDomainIntraRPMap().entrySet().parallelStream().forEach(
         e -> 
         {
            Optional<BigInteger> tempScore = e.getValue().parallelStream()
               .map(i->referencePosition.scoreRP(i,ess))
               .reduce( (a,b)-> a.add(b) );
            
            if(tempScore.isPresent())
            {
               intraDTRPScores.put(e.getKey(),tempScore.get());
            }
            else intraDTRPScores.put(e.getKey(), BigInteger.valueOf(0) );
         }
      );
      
      intraDTRPScoresUpToDate = true;
   }
   
   private ConcurrentHashMap<Integer,BigInteger> getIntraDTRPScores()
   {
      return intraDTRPScores;
   }
   
   private ConcurrentHashMap<Integer,BigInteger> getInterDTRPScores()
   {
      return interDTRPScores;
   }
   
   private void calculateInterDTRPScores()
   {
      interDTRPScores = new ConcurrentHashMap<Integer,BigInteger>();
      
      getDomainInterRPMap().entrySet().parallelStream().forEach(
         e -> 
         {
            Optional<BigInteger> tempScore = e.getValue().parallelStream()
               .map(i->referencePosition.scoreRP(i,ess))
               .reduce( (a,b)-> a.add(b) );
            
            if(tempScore.isPresent())
            {
               interDTRPScores.put(e.getKey(),tempScore.get());
            }
            else interDTRPScores.put(e.getKey(), BigInteger.valueOf(0) );
         }
      );
      
      interDTRPScoresUpToDate = true;
   }
   
   private void updateScores() //incoming Domain-index
   {
      MTout.log("Calculating scores for all RPs");
      
      /*
      if(intraDTRPScoresUpToDate)
      {
         
      }
      else
      {
         calculateIntraDTRPScores();
      }
      */   
   
      
      //intraScores.clear();
      //intraRPs.parallelStream().forEach(e-> intraScores.put(e,referencePosition.scoreRP(e,ess)));
      //interRPs.parallelStream().forEach(e-> interScores.put(e,referencePosition.scoreRP(e,ess)));
      
      //Map<referencePosition, Long> tempIntraScore = intraRPs.parallelStream()
      //   .collect(Collectors.toMap( Function.identity(), e -> referencePosition.scoreRP(e,ess)));
      //intraScores.putAll(tempIntraScore);
      
      //interScores.clear();
      //Map<referencePosition,Long> tempInterScore = interRPs.parallelStream()   
      //   .collect(Collectors.toMap( e->e, e->referencePosition.scoreRP(e,ess)));
      //interScores.putAll(tempInterScore);
      
      //calculateIntraDTRPScores();
      //calculateInterDTRPScores();
      calculateSFS();
      calculateNFS();
      calculateTFS();
   }
   
   private void updateScores(int IDI)
   {
      MTout.log("Calculating scores for RPs including domain " + IDI);
      
      //domainIntraRPMap.get(IDI).parallelStream().forEach(e-> intraScores.put(e,referencePosition.scoreRP(e,ess)));
      //domainInterRPMap.get(IDI).parallelStream().forEach(e-> interScores.put(e,referencePosition.scoreRP(e,ess)));
      
      //Map<referencePosition, Long> tempIntraScore = domainIntraRPMap.get(IDI).parallelStream()   
      //   .collect(Collectors.toMap( Function.identity(), e -> referencePosition.scoreRP(e,ess)));
      //intraScores.putAll(tempIntraScore);

      //Map<referencePosition,Long> tempInterScore = domainInterRPMap.get(IDI).parallelStream()   
      //   .collect(Collectors.toMap( e->e, e->referencePosition.scoreRP(e,ess)));
      //interScores.putAll(tempInterScore);
      
      if(sfsUpToDate)
      {
         Optional<BigInteger> optionalOldScore = domainIntraRPMap.get(IDI).parallelStream().map(i->referencePosition.scoreRP(i,mess)).reduce( (a,b)-> a.add(b));
         
         BigInteger oldScore = BigInteger.valueOf(0);
         if (optionalOldScore.isPresent()) {oldScore = optionalOldScore.get();}
         
         Optional<BigInteger> optionalNewScore = domainIntraRPMap.get(IDI).parallelStream().map(i->referencePosition.scoreRP(i,ess)).reduce( (a,b)-> a.add(b));
         
         BigInteger newScore = BigInteger.valueOf(0);
         if (optionalNewScore.isPresent()) {newScore = optionalNewScore.get();}
         
         
         sfs = sfs.subtract(oldScore).add(newScore);
         //intraDTRPScores.put(IDI,newScore);
      }
      else 
      {
         //calculateIntraDTRPScores();
         calculateSFS();
      }
      
      if(nfsUpToDate)
      {
         Optional<BigInteger> optionalOldScore = domainInterRPMap.get(IDI).parallelStream().map(i->referencePosition.scoreRP(i,mess)).reduce( (a,b)-> a.add(b));
         
         BigInteger oldScore = BigInteger.valueOf(0);
         if (optionalOldScore.isPresent()) {oldScore = optionalOldScore.get();}
            
         Optional<BigInteger> optionalNewScore = domainInterRPMap.get(IDI).parallelStream().map(i->referencePosition.scoreRP(i,ess)).reduce( (a,b)-> a.add(b));
         
         BigInteger newScore = BigInteger.valueOf(0);
         if (optionalNewScore.isPresent()) {newScore = optionalNewScore.get();}
         
         nfs = nfs.subtract(oldScore).add(newScore);
         //interDTRPScores.put(IDI,newScore);
      }
      else 
      {
         //calculateInterDTRPScores();
         calculateNFS();
      }

      calculateTFS();
   }
   
   public String getStrandName(int ISI) // incoming Strand Index-Selection-Bag
   {
      return SA.getStrandName(ISI);
   }
   
   
   private static void calculateDomainToRPMap()
   {
      domainIntraRPMap = new ConcurrentHashMap<Integer,Set<referencePosition>>();
      domainInterRPMap = new ConcurrentHashMap<Integer,Set<referencePosition>>();
      
      int[][] tempDA = getDomainAssociations();
      
      //ntitialize both DTRP maps
      for (int i =0; i < DA.tnd; i++)
      {
         domainIntraRPMap.put( i ,  ConcurrentHashMap.newKeySet());
         domainInterRPMap.put( i ,  ConcurrentHashMap.newKeySet());
      }
      
      //fill intra DTRP maps
      intraRPs.parallelStream().forEach( j->
      {
         referencePosition
            .getRPDAssociations(j,tempDA)
            .stream().forEach( k ->
            {
               domainIntraRPMap.get(k).add(j);
            });
      });
      
      interRPs.parallelStream().forEach( j->
      {
         referencePosition
            .getRPDAssociations(j,tempDA)
            .stream().forEach( k ->
            {
               domainInterRPMap.get(k).add(j);
            });
      });
   }
   
   private static void printDTRPMap()
   {
      domainIntraRPMap.entrySet().stream().forEach(i->
      {
         System.out.println("Domain Index: "+ i.getKey());
         System.out.println("RP's: ");
         i.getValue().stream().forEach(j->
         {
            referencePosition.printInformation(j);
         });
         System.out.println();
      });
      System.out.println();
      
      domainInterRPMap.entrySet().stream().forEach(i->
      {
         System.out.println("Domain Index: "+ i.getKey());
         System.out.println("RP's: ");
         i.getValue().stream().forEach(j->
         {
            referencePosition.printInformation(j);
         });
         System.out.println();
      });
      System.out.println();
   }
   
   private ConcurrentHashMap<Integer, Set<referencePosition>> getDomainIntraRPMap()
   {
      if(domainIntraRPMap.size()==0)
         calculateDomainToRPMap();
      return domainIntraRPMap;
   }
   
   private ConcurrentHashMap<Integer, Set<referencePosition>> getDomainInterRPMap()
   {
      if(domainInterRPMap.size()==0)
         calculateDomainToRPMap();
      return domainInterRPMap;
   }
   
   public Map<Integer,ArrayList<baseAlignment>> getDomainToAlignmentMap()
   {
      if(domainToAlignmentMap.size() == 0)
      {
         int[][] tempDA = getDomainAssociations();
         getIntraAlignments().stream()
            .forEach(i->
            {
            
               i.getBADAssociations(tempDA).stream()
               .forEach( j -> 
               {
                  if(domainToAlignmentMap.containsKey(j)) domainToAlignmentMap.get(j).add(i);
                     else 
                     {
                           domainToAlignmentMap.put( j , new ArrayList<baseAlignment>() );
                           domainToAlignmentMap.get(j).add(i);
                     }
               });  
            });
         getInterAlignments().stream()
            .forEach(i->
            {
            
               i.getBADAssociations(tempDA).stream()
               .forEach( j -> 
               {
                  if(domainToAlignmentMap.containsKey(j)) domainToAlignmentMap.get(j).add(i);
                     else 
                     {
                           domainToAlignmentMap.put( j , new ArrayList<baseAlignment>() );
                           domainToAlignmentMap.get(j).add(i);
                     }
               });  
            });
      }         
      return domainToAlignmentMap;
   }
   
   public int getLSL()
   {
      return lsl;
   }
   
   public HashSet<referencePosition> getIntraReferencePositions()
   {
      return intraRPs;
   }
   
   public HashSet<referencePosition> getInterReferencePositions()
   {
      return interRPs;
   }
   
   private int[][] getEDS()
   {
      return eds;
   }
   
   public TreeMap<Integer,Integer> getIntraInterferenceProfile()
   {
      TreeMap<Integer,Integer> tempOL =
         intraRPs.parallelStream()
         .map( e -> referencePosition.calculateInterferenceProfile( e, ess))
         .collect( 
            TreeMap<Integer,Integer>::new,
            //combine two Length-Count Tree maps
            (response, element) -> 
            {
               element.keySet().stream().forEach( eKey ->
               {  if ( response.containsKey( eKey)) response.replace( eKey, (response.get(eKey) + element.get(eKey) ) );
                  else response.put(eKey, element.get(eKey)); });
            },
            (r1,r2) -> r2.keySet().stream().forEach( eKey ->
               {  if ( r1.containsKey( eKey)) r1.replace( eKey, (r1.get(eKey) + r2.get(eKey) ) );
                  else r1.put(eKey, r2.get(eKey)); }));
                  
  
      tempOL = makeIntraUniqueOccurrencesComplete(tempOL);
      return tempOL;
   }
   
   public TreeMap<Integer,Integer> getInterInterferenceProfile()
   {
      TreeMap<Integer,Integer> tempOL =
         interRPs.parallelStream()
         .map( e -> referencePosition.calculateInterferenceProfile( e, ess))
         .collect( 
            TreeMap<Integer,Integer>::new,
            //combine two Length-Count Tree maps
            (response, element) -> 
            {
               element.keySet().stream().forEach( eKey ->
               {  if ( response.containsKey( eKey)) response.replace( eKey, (response.get(eKey) + element.get(eKey) ) );
                  else response.put(eKey, element.get(eKey)); });
            },
            (r1,r2) -> r2.keySet().stream().forEach( eKey ->
               {  if ( r1.containsKey( eKey)) r1.replace( eKey, (r1.get(eKey) + r2.get(eKey) ) );
                  else r1.put(eKey, r2.get(eKey)); }));
                  
      tempOL = makeInterUniqueOccurrencesComplete(tempOL);
      return tempOL;
   }

   private static ConcurrentHashMap<referencePosition,Long> getIntraScores( Generation IG)
   {
      return IG.intraScores;
   }
    
   private static ConcurrentHashMap<referencePosition,Long> getInterScores( Generation IG)
   {
      return IG.interScores;
   }
   
   public static int getIntraSLC()
   {
      return IntraOligoSLC;
   }
   
   public static int getInterSLC()
   {
      return InterOligoSLC;
   }
   
   public static BigInteger getIntraW()
   {
      return IntraOligoW;
   }
   public static BigInteger getInterW()
   {
      return InterOligoW;
   }
   
   public static int getAASLC()
   {
      return AAslc;
   }
   
   public static int getTTSLC()
   {
      return TTslc;
   }
   
   public static int getCCSLC()
   {
      return CCslc;
   }
   
   public static int getGGSLC()
   {
      return GGslc;
   }
}



