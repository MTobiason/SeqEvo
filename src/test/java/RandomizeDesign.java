import java.io.* ;
import edu.boisestate.mt.* ;
import java.util.Date;
import java.util.Vector;


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
by an input file.

The list of strands to be optimized for unwanted
interactions must be specified in an input file.
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

It requires a user input of network design (strand list), initial domain identity 
(domain list), and program parameters.
*/

public class RandomizeDesign
{

   //********************************************
   // Initialize the MTlogger, which will provide 
   // verbose or debug messages when requested.
   //********************************************
   
   static MTlogger MTout = new MTlogger("RandomizeDesign.java");
   
   // ************************************
   // Global constants used by the program
   // ************************************
   
   private static final Date runDate = new Date();
   private static final String version = "1.0; Build 1; 2016-09-12";
   
   private static int nd; // Number-of-Designs :: Number of randomly seeded designs to produce.

   private static boolean rrd; // Report-Randomized-Designs : wither or not to export the Randomized Designs to the RD directory.

   private static String RDFilePath = "RD/";   
   
   private static String PFilePath; // Parameters-File-Path
   private static String TOFilePath; // Trajectory-Output-File-Path
   private static String ROFilePath; // Report-Output-File-Path
   private static String SOFilePath; // Strands-Output-File-Path
   private static String DOFilePath; // Domains-Output-File-Path
   private static String NOFilePath; // Network-Output-File-Path
   private static String OOFilePath; // Occurrences-Out File Path
   
   private static boolean sflm; // Shuffle-First-Lineage-Mother
   private static boolean solm; // Shuffle-Other-Lineage-Mothers
   
   private static int nt; // Number-of-Threads: Number of threads to split lineages among.
   private static int nl; // Number-of-Lineages: Total number of separate lineages to run.
   
   private static int MaxShuffleAttempts = 1000; // Number of attempts to produce a valid design via random shuffle
   private static int ShuffleCalls = 0; // Number of times the program asked for a valid shuffle.
   private static int TotalShuffleAttempts = 0; // Number of attempts to produce a valid design via shuffle
   
   
   private static boolean rflm; // Report-Final-Mothers : wither or not to print the final generation lineage mothers to the screen and report file.
   
   private static String FLMFilePath = "FLM/";
   
   private static Generation InitialMothers[];
      
   //**********************************
   //main method for the build8 program
   //**********************************
   
	public static void main(String[] args) throws Exception
	{
      
      System.out.println();
      System.out.println("**********************************************************************");  
      System.out.println("                 StrandRandomizer: " + version );      
      System.out.println("Program for generating a (valid) randomly seeded sequence level design");
      System.out.println("**********************************************************************"); 
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
      
      // *******************************
      // Import settings for each module
      // *******************************
      
      //MTout.log("Importing Settings for each module");
      
      DomainPool.importSettings(PFilePath);
      StrandPool.importSettings(PFilePath);           
      Generation.importSettings(PFilePath);
      Lineage.importSettings(PFilePath);
      baseAlignment.importSettings(PFilePath);
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
      //Gen0.addScore( Gen0.getTFS() );
      
      // **************************************
      // Print the 0th Generation's information
      // **************************************
      
      System.out.println();
      System.out.println("********************************");
      System.out.println("Information from generation zero");
      System.out.println("********************************");
      System.out.println();
      
      Outputs.printIGI(Gen0);
      Outputs.printScores( Gen0 );
      
      // *******************************************
      // Create the Array of Randomly Seeded Designs
      // *******************************************
      
      Generation RandomizedDesigns[] = new Generation[nd]; 
      
      for (int i = 0 ; i < nd ; i ++) // for each Design
      {
         RandomizedDesigns[i] = new Generation( Gen0 ); // initialize it as a copy of generation 0.
         Generation.randomizeDesign(RandomizedDesigns[i]); //shuffle all varable domains in the design.
      }
      
      // *********************************
      // Score the Randomly Seeded Designs
      // *********************************
      
      //MolecularScore.scoreGenerations( RandomizedDesigns );

      // ***************************************
      // Print the information from Generation 1
      // ***************************************
      
      if (rrd) //if the option has been chosen to export the randomized designs.
      {
         File RDtemp = new File(RDFilePath); // stuff to make directory if it doesnt exist yet.
         RDtemp.mkdirs();
         
         FileWriter RDfilewriter = new FileWriter( RDFilePath + "rd-1.network.txt" ); //initiate the export for initial lineage mothers
         BufferedWriter RDbw = new BufferedWriter (RDfilewriter);
         PrintWriter RDPW = new PrintWriter (RDbw);
         
         System.out.println("************************************");
         System.out.println("Randomized Design reporting active. ");
         System.out.println("Randomized Designs will be output to");  
         System.out.println("directory \"" + RDFilePath+ "\"");         
         System.out.println("************************************");
         System.out.println();
         
         for ( int i = 0; i < nd; i++) // for all randomized designs
         {
            RandomizedDesigns[i].decodeDomainSequences();
            RandomizedDesigns[i].decodeStrandSequences();
            
            // ***************************************
            // Export the information from this design
            // ***************************************
            
            RDfilewriter = new FileWriter (RDFilePath + "rd-" + (i+1) + ".network.txt");
            RDbw = new BufferedWriter (RDfilewriter);
            RDPW = new PrintWriter (RDbw);
            
            Outputs.exportNI(RDPW , RandomizedDesigns[i]);
            RDPW.close();
            
            RDfilewriter = new FileWriter (RDFilePath + "rd-" + (i+1) + ".strands.txt");
            RDbw = new BufferedWriter (RDfilewriter);
            RDPW = new PrintWriter (RDbw);
            
            Outputs.exportISR(RDPW , RandomizedDesigns[i]);
            RDPW.close();
            
            RDfilewriter = new FileWriter (RDFilePath + "rd-" + (i+1) + ".domains.txt");
            RDbw = new BufferedWriter (RDfilewriter);
            RDPW = new PrintWriter (RDbw);
            
            Outputs.exportIDI(RDPW , RandomizedDesigns[i]);
            RDPW.close();
            
            //MolecularScore MS = new MolecularScore ( RandomizedDesigns[i] );
            //RDfilewriter = new FileWriter (RDFilePath + "rd-" + (i+1) + ".occurrences.txt");
            //RDbw = new BufferedWriter (RDfilewriter);
            //RDPW = new PrintWriter (RDbw);
            
            //MS.exportOccurrenceLists(RDPW);
            //RDPW.close();
            
         }
      }
      
      int BaseCount[][][] = Generation.countBases(RandomizedDesigns);
      Generation.printBaseCount( BaseCount );
      Generation.printFractionCG( BaseCount);
     
/*
      // **********************************
      // Record the Initial Lineage-Mothers
      // **********************************
      
      InitialMothers = new Generation[nl];
      
      for (int i = 0; i < nl; i++) // for all lineages
      {
         InitialMothers[i] = new Generation( LMs[i] ); //create a copy of the mother for reference 
      }
      
      // **************************************
      // Print constants for evolutionary cycle
      // **************************************
      
      System.out.println("*****************************");
      System.out.println("Evolutionary cycle conditions");
      System.out.println("*****************************");
      System.out.println();
      
      System.out.println("Number of computing threads used: " + nt);
      System.out.println();
      System.out.println("Number of lineages: " + nl );
      System.out.println("Number of cycles per lineage: " + Lineage.getCPL() );
      System.out.println("Number of generations per cycle: " + Lineage.getGPC() );
      System.out.println("Number of mothers per cycle: " + Lineage.getMPC() );
      System.out.println("Number of mutations per mother: " + Lineage.getMPM() );
      System.out.println("Number of daughters per mother: " + Lineage.getDPM() );
      System.out.println("Number of mutations per daughter: " + Lineage.getMPD() );
      
      System.out.println();
      
      
      // *************************************************************
      // Create an exception handler to catch errors thrown by threads
      // *************************************************************
      
      Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() 
      {
          public void uncaughtException(Thread th, Throwable ex) 
          {
              System.out.println();
              System.out.println("Error:: Exiting Program");
              System.out.println("Uncaught exception: " + ex); // relay error message to main()
              System.exit(0);
          }
      };
      
      // ***************
      // Create Lineages
      // ***************
      
      //MTout.log("Creating Lineages");
      Lineage Lineages[] = new Lineage[nl];
      
      for ( int i= 0; i < nl; i++)
      {
         //MTout.log( "Constructing Lineage " + i );
         Lineages[i] = new Lineage( LMs[i] );
      }
      
      // **************
      // Create Threads
      // **************
      
      Thread Threads[] = new Thread[nt];
     
      // *******************
      // Execute the Threads
      // *******************

      //MTout.log("Starting evolutionary cycle");
      double startTime = System.currentTimeMillis(); // start timer for runtime.
      
      System.out.println("*****************************");
      System.out.println("Beginning Evolutionary Cycles");
      System.out.println("*****************************");
      System.out.println();
      
      // *********************
      // Call Reporting Module
      // *********************
      
      ProgressReporter progressUpdater = new ProgressReporter();
      ProgressReporter.setTotalGenerations( Lineage.getCPL() * Lineage.getGPC() );
      ProgressReporter.setNumberThreads(nt);
      int TotalThreadCycles = 0;
      for( int i =0 ; i < nl; i = i + nt )
      {
         TotalThreadCycles = TotalThreadCycles+1;
      }
      ProgressReporter.setTotalThreadCycles( TotalThreadCycles);
      ProgressReporter.startTiming();
      ProgressReporter.reportProgress();
      
      for( int i =0 ; i < nl; i = i + nt ) // for each group of nt lineages.
      {
         ProgressReporter.incrementThreadCycle();
         
         // ****************
         // Start NT threads
         // ****************
         
         for( int j = 0 ; j < nt && (i + j) < nl ; j++ ) //for each thread up to the number of lineages.
         {
            //MTout.log("Starting Lineage # " + ( i + j +1 ) + " in thread " + (j+1));
            Threads[j] = new Thread( Lineages[(i+j)] ); // Create a new thread to handle the i'th lineage
            Threads[j].setUncaughtExceptionHandler(h); // set the exception handler so errors from threads are reported.
            Threads[j].start(); // Start this thread
         }
            
         // **************************
         // Wait for threads to finish
         // **************************
         
         for (int j = 0; j < nt && (i + j) < nl; j++) // for each thread,
         {
            Threads[j].join(); // wait until the thread is finished. 
         }
      }  

      ProgressReporter.endReporting();
      
      
      System.out.println("*****************************");
      System.out.println("Evolutionary Cycles Completed");
      System.out.println("*****************************");
      System.out.println();
      
      // *************************
      // Determine Fittest Lineage
      // *************************
      
      Generation FittestGeneration = new Generation( Gen0 ); //declare variable to hold the victor
      
      for( int i = 0; i < nl; i ++) // for number of lineages,
      {
         if ( LMs[i].getTFS() < FittestGeneration.getTFS() ) // if the lineage-mother's score is better than the current victor,
         {
            FittestGeneration.copy(LMs[i]); // replace victor with current mother.
            FittestGeneration.setST(LMs[i].getST()); // copy the victor's score trajectory also.
         }
      }
      
      // **********************************
      // Calculate runtime up to this point
      // **********************************
            
      //MTout.log("Calculating Evolutionary Cycle Runtime");
      double endTime   = System.currentTimeMillis(); // record evolutionary cycle endtime
      double elapsedTime = endTime-startTime;
      int H = (int)((elapsedTime/1000) / (60 *60)); // Hours
      int M = (int)(((elapsedTime/1000) / 60) % 60 ); // Minutes
      int S = (int)((elapsedTime/1000) % 60 );   // Seconds
      String totalTime = ( H + " h " + M + " m " + S + " s ");
      
      
      // ***************************************
      // Print information from final generation
      // ***************************************

      //MTout.log("Printing final generation information");        
      if ( rflm ) // Report-Final-Mothers if the option to print the final mother's information has been selected,
      {
         File FLMtemp = new File(FLMFilePath); // uhh, stuff to make directory if it doesnt exist yet?
         FLMtemp.mkdirs();
         
         FileWriter FLMfilewriter = new FileWriter( FLMFilePath + "FLM-1.network.txt" ); //initiate the export for initial lineage mothers
         BufferedWriter FLMbw = new BufferedWriter (FLMfilewriter);
         PrintWriter FLMPW = new PrintWriter (FLMbw);
         
         System.out.println("***************************************");
         System.out.println("Final Lineage Mother reporting active.");
         System.out.println("Lineage mothers (Final Generation) will");  
         System.out.println("be output to directory \"" + FLMFilePath+ "\"");         
         System.out.println("***************************************");
         System.out.println();
         
         for ( int i = 0; i < nl; i++) // for each lineage
         {
            //System.out.println("***********************************");
            //System.out.println("Final mother, lineage " + (i+1) + " Information");
            //System.out.println("***********************************");
            //System.out.println();
            //Outputs.printIGI( LMs[i] );  // print the lineage's information.
            
            FLMfilewriter = new FileWriter (FLMFilePath + "FLM-" + (i+1) + ".network.txt");
            FLMbw = new BufferedWriter (FLMfilewriter);
            FLMPW = new PrintWriter (FLMbw);
            
            Outputs.exportNI(FLMPW , LMs[i]);
            FLMPW.close();
            
            FLMfilewriter = new FileWriter (FLMFilePath + "FLM-" + (i+1) + ".strands.txt");
            FLMbw = new BufferedWriter (FLMfilewriter);
            FLMPW = new PrintWriter (FLMbw);
            
            Outputs.exportISR(FLMPW , LMs[i]);
            FLMPW.close();
            
            FLMfilewriter = new FileWriter (FLMFilePath + "FLM-" + (i+1) + ".domains.txt");
            FLMbw = new BufferedWriter (FLMfilewriter);
            FLMPW = new PrintWriter (FLMbw);
            
            Outputs.exportIDI(FLMPW , LMs[i]);
            FLMPW.close();
            
            MolecularScore MS = new MolecularScore ( LMs[i] );
            FLMfilewriter = new FileWriter (FLMFilePath + "FLM-" + (i+1) + ".occurrences.txt");
            FLMbw = new BufferedWriter (FLMfilewriter);
            FLMPW = new PrintWriter (FLMbw);
            
            MS.exportOccurrenceLists(FLMPW);
            FLMPW.close();
         }
      }    
 
      System.out.println("***********************************");
      System.out.println("Information from fittest generation");
      System.out.println("***********************************");
      System.out.println();
      
      Outputs.printIGI( FittestGeneration ); // print the fittest generation's information.
      Outputs.printScores( FittestGeneration );

     // ***************************
      // Print the Trajectories file
      // ***************************
      
      FileWriter filewriter = new FileWriter( TOFilePath );
      BufferedWriter bw = new BufferedWriter (filewriter);
      PrintWriter PW = new PrintWriter (bw);
      
      PW.print("Generation,"); // print the first column header. 
      for (int i = 0; i < nl ; i++) // for each lineage
      {
         PW.print(" Lineage " + (i+1) + " Score,"); //print that column's header.
      }
      PW.println(); // move to the first row.
     
      Vector Temp[]= new Vector[nl]; // create array to store the score trajectories
      for ( int i = 0; i < nl; i ++ ) // for all the lineages
      {
         Temp[i] = new Vector();    // initialize the array element.
         Temp[i] = Lineages[i].getST(); // 
      }
      
      for( int g = 1; g < Temp[0].size() ; g++ ) // for all generations after gen0
      {
         PW.print( (g) + ","); // print the generation number in the first column
         for (int i = 0; i < nl ; i++) // for each lineage
         {
            PW.print(" " + Temp[i].get(g) + ","); // print the lineage's score in the next column
         }
         PW.println(); // move to the next line.
      }
      
      PW.close();

      // ******************
      // Create Report File
      // ******************
       
      filewriter = new FileWriter( ROFilePath );
      bw = new BufferedWriter (filewriter);
      PW = new PrintWriter (bw);
      
      PW.println("SequenceEvolver Output");
      PW.println("Version: " + version );
      PW.println("Program Started: " + runDate);
      PW.println();
      
      PW.println("******************************");
      PW.println("Evolutionary Cycle Information");
      PW.println("******************************");
      PW.println();
      
      PW.println("Number of lineages: " + nl );
      PW.println("Number of cycles per lineage: " + Lineage.getCPL() );
      PW.println("Number of generations per cycle: " + Lineage.getGPC() );
      PW.println("Number of mothers per cycle: " + Lineage.getMPC() );
      PW.println("Number of mutations per mother: " + Lineage.getMPM() );
      PW.println("Number of daughters per mother: " + Lineage.getDPM() );
      PW.println("Number of mutations per daughter: " + Lineage.getMPD() );
      PW.println();
      
      MolecularScore.exportScoringParameters( PW );
      
      PW.println("System Information:");
      PW.println("------------------------");
      PW.println("Number of computing threads used (nt): " + nt );
      PW.println("Time taken to complete evolutionary process: " + totalTime );
      PW.println();
      
      PW.println("***********************************");
      PW.println("Generation Zero (Input) Information");
      PW.println("***********************************");
      PW.println();
      Outputs.exportIGI( PW, Gen0 );
      
      if ( rrd )
      {
         PW.println("**********************************************");
         PW.println("Generation One (\"Initial Mothers\") Information");
         PW.println("**********************************************");
         PW.println();
         
         for( int i =0; i < nl ; i++)
         {
            PW.println("**************************************");
            PW.println("Initial Mother, Lineage " + ( i +1 ) + " Information");
            PW.println("**************************************");
            PW.println();
            Outputs.exportIGI( PW, InitialMothers[i]);
         }
      }
      
      if ( rflm )
      {         
         for( int i =0; i < nl; i++)
         {
            PW.println("************************************");
            PW.println("Final Mother, Lineage " + (i+1) + " Information");
            PW.println("************************************");
            PW.println();
            Outputs.exportIGI( PW, LMs[i]);
         }
      }
      
      PW.println("***************************************");
      PW.println("Fittest Generation (Output) Information");
      PW.println("***************************************");
      PW.println();
      Outputs.exportIGI( PW, FittestGeneration);
      
      PW.close();
      
      // **********************
      // Create DomainsOut File
      // **********************
      
      filewriter = new FileWriter( DOFilePath );
      bw = new BufferedWriter (filewriter);
      PW = new PrintWriter (bw);
      
      PW.println("// Fittest generation's domains from StrandEvolver; Version: " + version + "; run time: " + runDate);
      PW.println("// Domain Types: f = fixed, vs = Variable(Seeded), v = Variable(unseeded)");
      PW.println("//");
      PW.println("// Domain information:");
      PW.println("// ------------------------------------------------------");      
      PW.println("// number" + "\t" + "name" + "\t" + "type" + "\t" + "sequence(5'-3')");
      PW.println("// ------------------------------------------------------");
      Outputs.exportIDI( PW, FittestGeneration);
    
      PW.close();
      
      // **********************
      // Create StrandsOut File
      // **********************
      
      filewriter = new FileWriter( SOFilePath );
      bw = new BufferedWriter (filewriter);
      PW = new PrintWriter (bw);
      
      PW.println("// Strand Recipe's used by Sequence Evolver: " + version + "; run time: " + runDate);
      PW.println("//");
      PW.println("// File Format:");
      PW.println("// ------------------------------------------------------");      
      PW.println("// Number" + "\t" + "Strand Name" + "\t" + "Constituent Domains(5'-3')");
      PW.println("// ------------------------------------------------------");
      Outputs.exportISR( PW, FittestGeneration);
    
      PW.close();
      
      // **********************
      // Create NetworkOut File
      // **********************
      
      filewriter = new FileWriter( NOFilePath );
      bw = new BufferedWriter (filewriter);
      PW = new PrintWriter (bw);
      
      PW.println("Fittest Generation from StrandEvolver; Version: " + version + "; run time: " + runDate);
      PW.println();      
      Outputs.exportNI ( PW , FittestGeneration);

      PW.close();
      
      // **********************
      // Create OccurrencesOut File
      // **********************
      
      MolecularScore MS = new MolecularScore ( FittestGeneration );
      filewriter = new FileWriter( OOFilePath );
      bw = new BufferedWriter (filewriter);
      PW = new PrintWriter (bw);
      
      PW.println("List of WCI events found in fittest generation. Produced by SequenceEvolver Version: " + version + "; run time: " + runDate);
      PW.println();      
      MS.exportOccurrenceLists ( PW );

      PW.close();
     
*/
      System.out.println("***********");
      System.out.println("Program End");
      System.out.println("***********");
      System.out.println();
      
      // **************
      // Report Runtime
      // **************
      
      System.out.println("*********************************");
      System.out.println("Statistics for this run (approx.)");
      System.out.println("*********************************");
      System.out.println();
      
      printShuffleAttempts();
      //Lineage.printTranspositionAttempts();
      //Lineage.printMutationAttempts();
      
	}
   
   private static void printShuffleAttempts()
   {
      System.out.println("Lineage Mother Shuffle Calls: " + ShuffleCalls);
      System.out.println("Lineage Mother Shuffle Attempts: " + TotalShuffleAttempts);
      System.out.println("Mean Attempts per Call: " +  ((double)( TotalShuffleAttempts*100 / ShuffleCalls))/100);
      System.out.println();
   }
   

   
   public static void readArguments ( String[] Iargs )  // Incoming-ARGumentS 
   {
      // ****************************************************
      // Set default values for settings local to this method
      // ****************************************************
      
      MTout.setDebug(false);      //Set Debug text to be active (true) or inactive (false)
      MTout.setVerbose(false);      //Set Verbose text to be active (true) or inactive (false)
      PFilePath = "rd.parameters.txt";      // I: Input-File-Path
      
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
            System.out.println("\t"+"-i [filepath]");
            System.out.println("\t"+"::Set parameters file to [filepath]"); 
            System.out.println("\t"+"::Default parameters file '"+ PFilePath +"'");
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
            PFilePath = Iargs[i+1]; // accept the next argument as the input file
            System.out.println("Using Input file: " + PFilePath); 
         }
      }
   }
   
   public static void importSettings( String PFilePath ) throws Exception
   {
   
      //MTout.log("Importing Settings for main() module from "+ PFilePath );
      
      // **************************************
      // Set Default Values for local variables
      // **************************************
      
      nl = 3; // Number-Lineages 
      nt = Runtime.getRuntime().availableProcessors();;
      
      rrd = false; // Report-Initial-Lineage-Mothers
      rflm = false; // Report-Final-Lineage-Mothers
      
      sflm = false; // Shuffle-First-Lineage-Mothers
      solm = true; // Shuffle-Other-Lineage-Mothers
      
      TOFilePath = "out.trajectories.csv"; // TO: Trajectories-Out
      ROFilePath = "out.report.txt"; // RO: Report-Out
      SOFilePath = "out.strands.txt"; // SO: Strands-Out
      DOFilePath = "out.domains.txt"; // DO: Domains-Out  
      NOFilePath = "out.network.txt"; // NO: Network-Out 
      OOFilePath = "out.occurrences.txt"; // OO: Occurrences-Out 
      
      // ************************************************
      // Read the Input File, looking for local variables
      // ************************************************
      
      FileReader filereader = new FileReader(PFilePath);   
      StreamTokenizer streamtokenizer = new StreamTokenizer(filereader);
      while( streamtokenizer.ttype != StreamTokenizer.TT_EOF ) // for the entire "input" file
      {
         streamtokenizer.nextToken();
         if (streamtokenizer.ttype == StreamTokenizer.TT_WORD )
         {
            if (streamtokenizer.sval.equalsIgnoreCase("dofilepath"))
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
            else if (streamtokenizer.sval.equalsIgnoreCase("nd"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval > 0)
               {
                  nd = (int)streamtokenizer.nval;
                  System.out.println("nd value imported. Accepted value: "+ nd);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"nd\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"nd\" in " + PFilePath); 
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
            else if (streamtokenizer.sval.equalsIgnoreCase("oofilepath"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  OOFilePath = streamtokenizer.sval;
                  System.out.println("OOFilePath value imported. Accepted value: "+ OOFilePath);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"OOFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"OOFilePath\" in " + PFilePath); 
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
            else if (streamtokenizer.sval.equalsIgnoreCase("rflm"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.sval.equalsIgnoreCase("true"))
               {
                  rflm = true;
                  System.out.println("rflm value imported. Accepted value: "+ rflm);
               }
               else if(streamtokenizer.sval.equalsIgnoreCase("false"))
               {
                  rflm = false;
                  System.out.println("rflm value imported. Accepted value: "+ rflm);
               }
               else 
               { 
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"rflm\" in " + PFilePath); 
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
            else if (streamtokenizer.sval.equalsIgnoreCase("rrd"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.sval.equalsIgnoreCase("true"))
               {
                  rrd = true;
                  System.out.println("rrd value imported. Accepted value: "+ rrd);
               }
               else if(streamtokenizer.sval.equalsIgnoreCase("false"))
               {
                  rrd = false;
                  System.out.println("rrd value imported. Accepted value: "+ rrd);
               }
               else 
               { 
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"rrd\" in " + PFilePath); 
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
            else if (streamtokenizer.sval.equalsIgnoreCase("tofilepath"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  TOFilePath = streamtokenizer.sval;
                  System.out.println("TOFilePath value imported. Accepted value: "+ TOFilePath);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"TOFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"TOFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
         }
      }
      
   }
}

