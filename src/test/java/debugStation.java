import edu.boisestate.mt.* ;
import java.io.* ;
import java.util.Date;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.TreeMap;



//******************************************************************************
// This will be the "main" section of code for the "IntelligentDesigner" project
//******************************************************************************


//Bernie's comments from the fill1.java file
/*********************************************
This application designs a set of DNA strands
with minimal unwanted interactions.


The program requires one to specify the number
of A, C, G, and T basis in each domain or to
provide the base sequence that the domain is 
to have. The specifications must be provided
by an parameter file.

The list of strands to be optimized for unwanted
interactions must be specified in an parameter file.
***********************************************/

/**********************************************
To compile this program, go into the MSDOS
terminal window and go to the directory in which
the program is located.  At the DOS prompt type
<javac build4.java>.  This creates the file
build2.class.  To run the program type 
<java build4.java>
 
To get a MSDOS terminal window go to Windows Help
and Support, Look under Command Prompt frequently
asked questions and click on "Click to open Command 
Prompt."  If I try Run -> All Progarms -> Command
Prompt I get a command window with U:\> that I
can't seem to do anything with. 

I am in C:\Users\BernardYurke\My Documents\Research\DNA-JAVA-programs\fill1-141007b

To set the path in the command prompt type:

set path=%path%;C:\Program Files\Java\jdk1.8.0_20\bin
***********************************************/

//MT Comments 2016-04-13
/*
This program is designed to minimize the presence of Watson-Crick based interference 
present at specific locations within a target design.

It requires a user parameter of network design (strand list), initial domain identity 
(domain list), and program parameters.
*/

public class debugStation
{

   //********************************************
   // Initialize the MTlogger, which will provide 
   // verbose or debug messages when requested.
   //********************************************
   
   static MTlogger MTout = new MTlogger("SequenceEvolver.java");
   
   // ************************************
   // Global constants used by the program
   // ************************************
   
   private static final Date runDate = new Date();
   private static final String version = "1.4; Build 1; 2018-01-10";
   
   private static String PFilePath; // Parameters-File-Path
   private static String CTOFilePath; // Cycle-Trajectory-Output-File-Path
   private static String LCTOFilePath; // Logarithmic-Cycle-Trajectory-File-Path
   private static String GTOFilePath; // Generation-Trajectory-Output
   private static String ROFilePath; // Report-Output-File-Path
   private static String SOFilePath; // Strands-Output-File-Path
   private static String DOFilePath; // Domains-Output-File-Path
   private static String NOFilePath; // Network-Output-File-Path
   private static String IOFilePath; // Interference-Out File Path
   
   private static boolean sflm; // Shuffle-First-Lineage-Mother
   private static boolean solm; // Shuffle-Other-Lineage-Mothers
   
   private static int nt; // Number-of-Threads: Number of threads to split lineages among.
   private static int nl; // Number-of-Lineages: Total number of separate lineages to run.
   
   private static int MaxShuffleAttempts = 1000; // Number of attempts to produce a valid design via random shuffle
   private static int ShuffleCalls = 0; // Number of times the program asked for a valid shuffle.
   private static int TotalShuffleAttempts = 0; // Number of attempts to produce a valid design via shuffle
   
   
   private static boolean eilm; // Report-Initial-Mothers : wither or not to print the generation 1 lineage mothers to the screen and report file.
   private static boolean eflm; // Report-Final-Mothers : wither or not to print the final generation lineage mothers to the screen and report file.
  
   private static String ILMFilePath = "ILM/";
   private static String FLMFilePath = "FLM/";
   
   private static Generation InitialMothers[];
      
   //********************************************
   //main method for the Sequence Evolver program
   //********************************************
   
	public static void main(String[] args) throws Exception
	{
      
      System.out.println();
      System.out.println("********************************************************************");  
      System.out.println("                 SequenceEvolver: " + version );      
      System.out.println("Program for designing DNA Strands with minimal unwanted interactions");
      System.out.println("********************************************************************"); 
      System.out.println();
      
      readArguments( args );
      importSettings( PFilePath ); // read the arguments, and accept incoming settings.
            
      if (MTout.DebugFlag)
      {
         System.out.println("Debug reporting active");
         System.out.println();
      }
      
      if (MTout.VerboseFlag)
      {
         System.out.println("Verbose reporting active");
         System.out.println();
      }
      
      DomainPool.importSettings(PFilePath);
      StrandPool.importSettings(PFilePath);           
      Generation.importSettings(PFilePath);
      Lineage.importSettings(PFilePath);
      WCCSet.importSettings( PFilePath );
      baseAlignment.importSettings( PFilePath);
	  
      MolecularScore.importSettings(PFilePath);
      referencePosition.importSettings(PFilePath);
      
      // **************************
      // Import network information
      // **************************
      
      DomainPool Domains = new DomainPool(); // Import the domain information into the "Domains" object.
      StrandPool Strands = new StrandPool(); // Import the strand information into the "Strands" object.
      
      // **********************
      // Create Generation Zero
      // **********************
      
      Generation Gen0 = new Generation( Domains, Strands ); // Create Generation Zero from the imported domains and strands.
      //Generation BaselineGeneration = new Generation(Gen0); //Create a Gneration to calculate and store the baseline information.
      //BaselineGeneration.generateBaseline(); // generate WCI baseline in unique generation using generation 0.
      //MolecularScore.scoreGeneration(Gen0); // rescore Gen0 using new baseline
      //MolecularScore.scoreGeneration(BaselineGeneration); // score BaselineGeneration
      
      
      // **************************************
      // Print the 0th Generation's information
      // **************************************
      
      System.out.println();
      System.out.println("********************************");
      System.out.println("Information from generation zero");
      System.out.println("********************************");
      System.out.println();
      
      Outputs.printIGI( Gen0 );
      Outputs.printScores( Gen0 );
      
      referencePosition testRP = new referencePosition();
      //testRP.getContBases();
      referencePosition.printInformation( testRP);
      
      //referencePosition.getInterReferencePositions().stream()
      //   .forEach(e-> {referencePosition.printInformation(e); referencePosition.printReferencePosition(e);});

      //referencePosition.getIntraReferencePositions().stream()
      //  .forEach(e-> {referencePosition.printInformation(e); referencePosition.printReferencePosition(e);});
     
      long tempSFS = Gen0.getIntraReferencePositionScores().values().stream().mapToLong( i-> referencePositionScore.getScore(i)).sum();
      long tempNFS = Gen0.getInterReferencePositionScores().values().stream().mapToLong( i-> referencePositionScore.getScore(i)).sum();
      
      System.out.println("new SFS = " + tempSFS);
      System.out.println("new NFS = " + tempNFS);
      
      

	  


      ArrayList<WCStructure> intraBaselineStructures = Generation.getIntraBaselineStructures();
      System.out.println("Intramolecular baseline size: " + intraBaselineStructures.size());
      System.out.println("Intramolecular Baseline Alignments size:" + Generation.getIntraBaselineAlignments().size());
      
      ArrayList<WCStructure> interBaselineStructures = Generation.getInterBaselineStructures();
      System.out.println("Intermolecular baseline size: " + interBaselineStructures.size());
      System.out.println("Intermolecular Baseline Alignments size:" + Generation.getInterBaselineAlignments().size());

      
      System.out.println("Intramolecular Alignments for Generation Gen0:");
      ArrayList<baseAlignment> gen0IntraAlignments = Gen0.getIntraAlignments();
      System.out.println("Gen0 Intramolecular alignments size: " + gen0IntraAlignments.size());
      
      System.out.println("Intermolecular Alignments for Generation Gen0:");
      ArrayList<baseAlignment> gen0InterAlignments = Gen0.getInterAlignments();
      System.out.println("Gen0 Intermolecular alignments size: " + gen0InterAlignments.size());
      System.out.println();
      
      System.out.println("Intramolecular Structures for Generation Gen0:");
      ArrayList<WCStructure> gen0IntraStructures = Gen0.getIntraUniqueStructures();
      System.out.println("Gen0 Intramolecular Structures size: " + gen0IntraStructures.size());
      System.out.println();
      
      System.out.println("Intermolecular Structures for Generation Gen0:");
      ArrayList<WCStructure> gen0InterStructures = Gen0.getInterUniqueStructures();
      System.out.println("Gen0 Intermolecular Structures size: " + gen0InterStructures.size());
      System.out.println();
      
      System.out.println("Intramolecular Baseline Structures for Generation Gen0:");
      ArrayList<WCStructure> gen0IntraBaselineStructures = Gen0.getIntraBaselineStructures();
      System.out.println("Gen0 Intramolecular Baseline Structures size: " + gen0IntraBaselineStructures.size());
      System.out.println();

      System.out.println("Intermolecular Baseline Structures for Generation Gen0:");
      ArrayList<WCStructure> gen0InterBaselineStructures = Gen0.getInterBaselineStructures();
      System.out.println("Gen0 Intermolecular Baseline Structures size: " + gen0InterBaselineStructures.size());
      System.out.println();
      
      System.out.println("Intramolecular Interference Structures for Generation Gen0:");
      ArrayList<WCStructure> gen0IntraInterferenceStructures = Gen0.getIntraInterferenceStructures();
      System.out.println("Gen0 Intramolecular Interference Structures size: " + gen0IntraInterferenceStructures.size());
      System.out.println();
      
      System.out.println("Intermolecular Interference Structures for Generation Gen0:");
      ArrayList<WCStructure> gen0InterInterferenceStructures = Gen0.getInterInterferenceStructures();
      System.out.println("Gen0 Intermolecular Interference Structures size: " + gen0InterInterferenceStructures.size());
      System.out.println();
      
      System.out.println("Intramolecular Baseline UniqueOccurrences for Generation Gen0:");
      Map<Integer,Integer> gen0IntraBaselineUniqueOccurrences = Gen0.getIntraBaselineUniqueOccurrences();
      System.out.println("Gen0 Intramolecular Baseline Occurences size: " + gen0IntraBaselineUniqueOccurrences.size());
      System.out.println();
      
      if ( gen0IntraBaselineUniqueOccurrences.size() > 0) 
      {
         System.out.println("Intramolecular Baseline UniqueOccurrences: ");
         gen0IntraBaselineUniqueOccurrences.entrySet().stream().forEach( e -> System.out.println("Length: "+e.getKey()+", Count: " +e.getValue()) );
      }
      else System.out.println("No Baseline Intramolecular Structures");
      System.out.println();
      
      System.out.println("Intermolecular Baseline UniqueOccurrences for Generation Gen0:");
      Map<Integer,Integer> gen0InterBaselineUniqueOccurrences = Gen0.getInterBaselineUniqueOccurrences();
      System.out.println("Gen0 Intermolecular Baseline Occurences size: " + gen0InterBaselineUniqueOccurrences.size());
      System.out.println();
      
      if ( gen0InterBaselineUniqueOccurrences.size() > 0) 
      {
         System.out.println("Intermolecular Baseline UniqueOccurrences: ");
         gen0InterBaselineUniqueOccurrences.entrySet().stream().forEach( e -> System.out.println("Length: "+e.getKey()+", Count: " +e.getValue()) );
      }
      else System.out.println("No Baseline Intermolecular UniqueOccurrences");
      System.out.println();
      
      System.out.println("Intramolecular UniqueOccurrences for Generation Gen0:");
      Map<Integer,Integer> gen0IntraUniqueOccurrences = Gen0.getIntraUniqueOccurrences();
      System.out.println("Gen0 Intramolecular Occurences size: " + gen0IntraUniqueOccurrences.size());
      System.out.println();
      
      if ( gen0IntraUniqueOccurrences.size() > 0) 
      {
         System.out.println("Intramolecular UniqueOccurrences: ");
         gen0IntraUniqueOccurrences.entrySet().stream().forEach( e -> System.out.println("Length: "+e.getKey()+", Count: " +e.getValue()) );
      }
      else System.out.println("No Intramolecular UniqueOccurrences");
      System.out.println();
      
      System.out.println("Intermolecular UniqueOccurrences for Generation Gen0:");
      Map<Integer,Integer> gen0InterUniqueOccurrences = Gen0.getInterUniqueOccurrences();
      System.out.println("Gen0 Intermolecular Occurences size: " + gen0InterUniqueOccurrences.size());
      System.out.println();
      
      if ( gen0InterUniqueOccurrences.size() > 0) 
      {
         System.out.println("Intermolecular UniqueOccurrences: ");
         gen0InterUniqueOccurrences.entrySet().stream().forEach( e -> System.out.println("Length: "+e.getKey()+", Count: " +e.getValue()) );
      }
      else System.out.println("No Intermolecular UniqueOccurrences");
      System.out.println();
      
      System.out.println("Intramolecular Interference UniqueOccurrences for Generation Gen0:");
      Map<Integer,Integer> gen0IntraInterferenceUniqueOccurrences = Gen0.getIntraInterferenceUniqueOccurrences();
      System.out.println("Gen0 Intramolecular Interference Occurences size: " + gen0IntraInterferenceUniqueOccurrences.size());
      System.out.println();
      
      if ( gen0IntraInterferenceUniqueOccurrences.size() > 0) 
      {
         System.out.println("Intramolecular Interference UniqueOccurrences: ");
         gen0IntraInterferenceUniqueOccurrences.entrySet().stream().forEach( e -> System.out.println("Length: "+e.getKey()+", Count: " +e.getValue()) );
      }
      else System.out.println("No Intramolecular Interference UniqueOccurrences");
      System.out.println();
      
      System.out.println("Intermolecular Interference UniqueOccurrences for Generation Gen0:");
      Map<Integer,Integer> gen0InterInterferenceUniqueOccurrences = Gen0.getInterInterferenceUniqueOccurrences();
      System.out.println("Gen0 Intermolecular Interference Occurences size: " + gen0InterInterferenceUniqueOccurrences.size());
      System.out.println();
      
      if ( gen0InterInterferenceUniqueOccurrences.size() > 0) 
      {
         System.out.println("Intermolecular Interference UniqueOccurrences: ");
         gen0InterInterferenceUniqueOccurrences.entrySet().stream().forEach( e -> System.out.println("Length: "+e.getKey()+", Count: " +e.getValue()) );
      }
      else System.out.println("No Intermolecular Interference UniqueOccurrences");
      System.out.println();
      
      System.out.println("Intramolecular Complete Occurrences for Generation Gen0:");
      Map<Integer,Integer> gen0IntraCompleteOccurrences = Gen0.getIntraCompleteOccurrences();
      System.out.println("Gen0 Complete Intramolecular Occurences size: " + gen0IntraCompleteOccurrences.size());
      System.out.println();
      
      if ( gen0IntraCompleteOccurrences.size() > 0) 
      {
         System.out.println("Intramolecular Complete Occurrences: ");
         gen0IntraCompleteOccurrences.entrySet().stream().forEach( e -> System.out.println("Length: "+e.getKey()+", Count: " +e.getValue()) );
      }
      else System.out.println("No Intramolecular Complete Occurrences");
      System.out.println();
      
      System.out.println("Intermolecular Complete Occurrences for Generation Gen0:");
      Map<Integer,Integer> gen0InterCompleteOccurrences = Gen0.getInterCompleteOccurrences();
      System.out.println("Gen0 Complete Intermolecular Occurences size: " + gen0InterCompleteOccurrences.size());
      System.out.println();
      
      if ( gen0InterCompleteOccurrences.size() > 0) 
      {
         System.out.println("Intermolecular Complete Occurrences: ");
         gen0InterCompleteOccurrences.entrySet().stream().forEach( e -> System.out.println("Length: "+e.getKey()+", Count: " +e.getValue()) );
      }
      else System.out.println("No Intermolecular Complete Occurrences");
      System.out.println();
      
      System.out.println("Intramolecular Baseline Complete Occurrences for Generation Gen0:");
      Map<Integer,Integer> gen0IntraBaselineCompleteOccurrences = Gen0.getIntraBaselineCompleteOccurrences();
      System.out.println("Gen0 Complete Intramolecular Occurences size: " + gen0IntraBaselineCompleteOccurrences.size());
      System.out.println();
      
      if ( gen0IntraBaselineCompleteOccurrences.size() > 0) 
      {
         System.out.println("Intramolecular Baseline Complete Occurrences: ");
         gen0IntraBaselineCompleteOccurrences.entrySet().stream().forEach( e -> System.out.println("Length: "+e.getKey()+", Count: " +e.getValue()) );
      }
      else System.out.println("No Intramolecular Baseline Complete Occurrences");
      System.out.println();
      
      System.out.println("Intermolecular Baseline Complete Occurrences for Generation Gen0:");
      Map<Integer,Integer> gen0InterBaselineCompleteOccurrences = Gen0.getInterBaselineCompleteOccurrences();
      System.out.println("Gen0 Complete Intermolecular Occurences size: " + gen0InterBaselineCompleteOccurrences.size());
      System.out.println();
      
      if ( gen0InterBaselineCompleteOccurrences.size() > 0) 
      {
         System.out.println("Intermolecular Baseline Complete Occurrences: ");
         gen0InterBaselineCompleteOccurrences.entrySet().stream().forEach( e -> System.out.println("Length: "+e.getKey()+", Count: " +e.getValue()) );
      }
      else System.out.println("No Intermolecular Baseline Complete Occurrences");
      System.out.println();
      
      System.out.println("Intramolecular Interference Complete Occurrences for Generation Gen0:");
      Map<Integer,Integer> gen0IntraInterferenceCompleteOccurrences = Gen0.getIntraInterferenceCompleteOccurrences();
      System.out.println("Gen0 Complete Intramolecular Occurences size: " + gen0IntraInterferenceCompleteOccurrences.size());
      System.out.println();
      
      if ( gen0IntraInterferenceCompleteOccurrences.size() > 0) 
      {
         System.out.println("Intramolecular Interference Complete Occurrences: ");
         gen0IntraInterferenceCompleteOccurrences.entrySet().stream().forEach( e -> System.out.println("Length: "+e.getKey()+", Count: " +e.getValue()) );
      }
      else System.out.println("No Intramolecular Interference Complete Occurrences");
      System.out.println();
      
      System.out.println("Intermolecular Interference Complete Occurrences for Generation Gen0:");
      Map<Integer,Integer> gen0InterInterferenceCompleteOccurrences = Gen0.getInterInterferenceCompleteOccurrences();
      System.out.println("Gen0 Complete Intermolecular Occurences size: " + gen0InterInterferenceCompleteOccurrences.size());
      System.out.println();
      
      if ( gen0InterInterferenceCompleteOccurrences.size() > 0) 
      {
         System.out.println("Intermolecular Interference Complete Occurrences: ");
         gen0InterInterferenceCompleteOccurrences.entrySet().stream().forEach( e -> System.out.println("Length: "+e.getKey()+", Count: " +e.getValue()) );
      }
      else System.out.println("No Intermolecular Interference Complete Occurrences");
      System.out.println();
      
      
      long SFS = Gen0.getSFS();
      System.out.println("SFS score: "+ SFS);
      
      long NFS = Gen0.getNFS();
      System.out.println("NFS score: "+ NFS);
      
      long TFS = Gen0.getTFS();
      System.out.println("TFS score: "+ TFS);
      
	}
   
   public static void readArguments ( String[] Iargs )  // Incoming-ARGumentS 
   {
      // ****************************************************
      // Set default values for settings local to this method
      // ****************************************************
      
      MTout.setDebug(false);      //Set Debug text to be active (true) or inactive (false)
      MTout.setVerbose(false);      //Set Verbose text to be active (true) or inactive (false)
      PFilePath = "se.parameters.txt";      // P: parameter-File-Path
      
      // ***************************
      // Check arguments for options
      // ***************************
      
      for(int i = 0; i < Iargs.length; i++)
      {         
         if (Iargs[i].equals("-v"))
         {
            MTout.setVerbose(true); //set verbose flags
         }
         
         if (Iargs[i].equals("-d"))
         {
            MTout.setDebug(true); //set debug flags
         }
         
         if (Iargs[i].equals("-h") || Iargs[i].equals("-help")) // Print explanation of acceptable arguments.
         {
            System.out.println("Recognized arguments:");
            System.out.println();
            System.out.println("\t"+"-p [filepath]");
            System.out.println("\t"+"::Set paramters file to [filepath]"); 
            System.out.println("\t"+"::Default parameters file: '"+ PFilePath +"'");
            System.out.println();
            System.out.println("\t"+"-v");
            System.out.println("\t"+"::Set verbose output mode"); 
            System.out.println();           
            System.out.println("\t"+"-d");
            System.out.println("\t"+"::Set debug output mode");  
            System.out.println();
            System.exit(0);
         }
         
         if (Iargs[i].equals("-p"))
         {
            PFilePath = Iargs[i+1]; // accept the next argument as the parameter file
            System.out.println("Using Parameters file: " + PFilePath); 
         }
      }
   }
   
   public static void importSettings( String PFilePath ) throws Exception
   {
   
      //MTout.log("Importing Settings for main() module from "+ PFilePath );
      
      // **************************************
      // Set Default Values for local variables
      // **************************************
      
      nl = 1; // Number-Lineages 
      nt = Runtime.getRuntime().availableProcessors();;
      
      eilm = false; // Export-Initial-Lineage-Mothers
      eflm = false; // Export-Final-Lineage-Mothers
      
      sflm = false; // Shuffle-First-Lineage-Mothers
      solm = true; // Shuffle-Other-Lineage-Mothers
      
      GTOFilePath = "se.out.Generation-trajectory.csv"; //GTO: Generation-Trajectory-Output 
      CTOFilePath = "se.out.Cycle-trajectory.csv"; //log-Cycle-Trajectory-Output
      LCTOFilePath = "se.out.Log-Cycle-Trajectory.csv";
      ROFilePath = "se.out.report.txt"; // RO: Report-Out
      SOFilePath = "disabled"; // SO: Strands-Out
      DOFilePath = "se.out.domains.txt"; // DO: Domains-Out  
      NOFilePath = "se.out.network.txt"; // NO: Network-Out 
      IOFilePath = "disabled"; // IO: Interference-Out 
      
      // ************************************************
      // Read the parameter File, looking for local variables
      // ************************************************
      
      FileReader filereader = new FileReader(PFilePath);   
      StreamTokenizer streamtokenizer = new StreamTokenizer(filereader);
      while( streamtokenizer.ttype != StreamTokenizer.TT_EOF ) // for the entire "parameter" file
      {
         streamtokenizer.nextToken();
         if (streamtokenizer.ttype == StreamTokenizer.TT_WORD )
         {
            if (streamtokenizer.sval.equalsIgnoreCase("ctofilepath"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  CTOFilePath = streamtokenizer.sval;
                  System.out.println("CTOFilePath value imported. Accepted value: "+ CTOFilePath);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"CTOFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"CTOFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }else if (streamtokenizer.sval.equalsIgnoreCase("dofilepath"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  DOFilePath = streamtokenizer.sval;
                  System.out.println("DOFilePath value imported. Accepted value: "+ DOFilePath);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"DOFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"DOFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("gtofilepath"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  GTOFilePath = streamtokenizer.sval;
                  System.out.println("GTOFilePath value imported. Accepted value: "+ GTOFilePath);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"GTOFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"GTOFilePath\" in " + PFilePath); 
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
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"MaxShuffleAttempts\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"MaxShuffleAttempts\" in " + PFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("lctofilepath"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  LCTOFilePath = streamtokenizer.sval;
                  System.out.println("LCTOFilePath value imported. Accepted value: "+ LCTOFilePath);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"LCTOFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"LCTOFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("nl"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval > 0)
               {
                  nl = (int)streamtokenizer.nval;
                  System.out.println("nl value imported. Accepted value: "+ nl);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"nl\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"nl\" in " + PFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("nofilepath"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  NOFilePath = streamtokenizer.sval;
                  System.out.println("NOFilePath value imported. Accepted value: " + DOFilePath);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"NOFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"NOFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("IOFilePath"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  IOFilePath = streamtokenizer.sval;
                  System.out.println("IOFilePath value imported. Accepted value: "+ IOFilePath);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"IOFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"IOFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("nt"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval > 0)
               {
                  nt = (int)streamtokenizer.nval;
                  System.out.println("nt value imported. Accepted value: "+ nt);
               }
               else if( streamtokenizer.sval.equalsIgnoreCase("auto"))
               {
                  nt = Runtime.getRuntime().availableProcessors();
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"nt\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"nt\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("eflm"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.sval.equalsIgnoreCase("true"))
               {
                  eflm = true;
                  System.out.println("eflm value imported. Accepted value: "+ eflm);
               }
               else if(streamtokenizer.sval.equalsIgnoreCase("false"))
               {
                  eflm = false;
                  System.out.println("eflm value imported. Accepted value: "+ eflm);
               }
               else 
               { 
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"eflm\" in " + PFilePath); 
                  System.exit(0);
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("rofilepath"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  ROFilePath = streamtokenizer.sval;
                  System.out.println("ROFilePath value imported. Accepted value: "+ ROFilePath);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"ROFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"ROFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("sofilepath"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  SOFilePath = streamtokenizer.sval;
                  System.out.println("SOFilePath value imported. Accepted value: "+ SOFilePath);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"SOFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"SOFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("eilm"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.sval.equalsIgnoreCase("true"))
               {
                  eilm = true;
                  System.out.println("eilm value imported. Accepted value: "+ eilm);
               }
               else if(streamtokenizer.sval.equalsIgnoreCase("false"))
               {
                  eilm = false;
                  System.out.println("eilm value imported. Accepted value: "+ eilm);
               }
               else 
               { 
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"eilm\" in " + PFilePath); 
                  System.exit(0);
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("sflm"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.sval.equalsIgnoreCase("true"))
               {
                  sflm = true;
                  System.out.println("sflm value imported. Accepted value: "+ sflm);
               }
               else if(streamtokenizer.sval.equalsIgnoreCase("false"))
               {
                  sflm = false;
                  System.out.println("sflm value imported. Accepted value: "+ sflm);
               }
               else 
               { 
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"sflm\" in " + PFilePath); 
                  System.exit(0);
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("solm"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.sval.equalsIgnoreCase("true"))
               {
                  solm = true;
                  System.out.println("solm value imported. Accepted value: "+ solm);
               }
               else if(streamtokenizer.sval.equalsIgnoreCase("false"))
               {
                  solm = false;
                  System.out.println("solm value imported. Accepted value: "+ solm);
               }
               else 
               { 
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"solm\" in " + PFilePath); 
                  System.exit(0);
               }
            }
         }
      }
      
   }
}

