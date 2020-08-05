package edu.boisestate.mt;
import java.io.* ;
import java.util.Date;
import java.util.Vector;
import java.util.Arrays;
import java.math.BigInteger;

public class SeqEvo
{
   static MTlogger MTout = new MTlogger("SequenceEvolver.java");
   
   // ************************************
   // Global constants used by the program
   // ************************************
   
   private static final Date runDate = new Date();
   private static final String version = "1.8";// 2020-08-04;
   
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

   private static boolean eilm; // Report-Initial-Mothers : wither or not to print the generation 1 lineage mothers to the screen and report file.
   private static boolean eflm; // Report-Final-Mothers : wither or not to print the final generation lineage mothers to the screen and report file.
  
   private static String ILMFilePath = "ILM/";
   private static String FLMFilePath = "FLM/";

      
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
      
      // *******************************
      // Import settings for each module
      // *******************************
      
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
      
      if(Domains.getNVD() == 0) throw new RuntimeException("No Variable domains!");
      
      // **********************
      // Create Generation Zero
      // **********************
      
      MTout.log("Initiating Gen0");
      Generation Gen0 = new Generation( Domains, Strands ); // Create Generation Zero from the imported domains and strands.

      // **************************************
      // Print the 0th Generation's information
      // **************************************
      
      System.out.println("********************************");
      System.out.println("Information from generation zero");
      System.out.println("********************************");
      System.out.println();
      
      Outputs.printIGI( Gen0 );
      Outputs.printScores( Gen0 );
      
      // ***********************************
      // Create the Array of Lineage Mothers
      // ***********************************
      
      MTout.log("Initiating Lineage Mothers");
      Generation LMs[] = new Generation[nl];  // Lineage-Mothers: Declare a new generation array to store the victors of each lineage.
      
      for (int i = 0 ; i < nl ; i ++) // for each Lineage-Mother
      {
         LMs[i] = new Generation( Gen0 ); // initialize it as a copy of generation 0
      }
      
      MTout.log("Randomizing Designs");
      if(!Generation.checkValidity(Gen0)) 
      {
         System.out.println("Initial Sequences invalid (too many consecutive identical bases), looking for valid alternatives"); 
         System.out.println();
      }
      if (sflm || !Generation.checkValidity(Gen0)) // if shuffle first lineage is active.
      {
         ShuffleCalls++;
         Generation.randomizeDeviceItteratively(LMs[0]);
      }
      
      if (solm ) // if shuffle other lineage mothers is active.
      {
         for (int i = 1; i < nl; i ++) // for all Lineage-Mothers, except mother zero.   
         {
            Generation.randomizeDeviceItteratively(LMs[i]); // shuffle any variable domains. 
         }
      }
      
      // ***************************************
      // Print the information from Generation 1
      // ***************************************
      
      if (eilm) //if the option has been chosen to export initial mothers.
      {
         File ILMtemp = new File(ILMFilePath); // uhh, stuff to make directory if it doesnt exist yet?
         ILMtemp.mkdirs();
		 
         //Remove any files from previous run.
		 
         for(File f: ILMtemp.listFiles())
            if(f.getName().startsWith("ILM-"))
               f.delete();
	
         FileWriter ILMfilewriter = new FileWriter( ILMFilePath + "ILM-1.network.txt" ); //initiate the export for initial lineage mothers
         BufferedWriter ILMbw = new BufferedWriter (ILMfilewriter);
         PrintWriter ILMPW = new PrintWriter (ILMbw);
         
         System.out.println("****************************************");
         System.out.println("Initial Lineage Mother reporting active.");
         System.out.println("Lineage mothers (Generation #1) will be");  
         System.out.println("output to directory \"" + ILMFilePath+ "\"");         
         System.out.println("****************************************");
         System.out.println();
         
         MTout.log("Exporting initial mothers");
         for ( int i = 0; i < nl; i++) // for all Lineage-Mothers
         {
            LMs[i].decodeDomainSequences();
            LMs[i].decodeStrandSequences();
            
            // ***************************************
            // Export the information from this mother
            // ***************************************
            
            ILMfilewriter = new FileWriter (ILMFilePath + "ILM-" + (i+1) + ".network.txt");
            ILMbw = new BufferedWriter (ILMfilewriter);
            ILMPW = new PrintWriter (ILMbw);
            
            Outputs.exportNI(ILMPW , LMs[i]);
            ILMPW.close();
            
            ILMfilewriter = new FileWriter (ILMFilePath + "ILM-" + (i+1) + ".strands.txt");
            ILMbw = new BufferedWriter (ILMfilewriter);
            ILMPW = new PrintWriter (ILMbw);
            
            Outputs.exportISR(ILMPW , LMs[i]);
            ILMPW.close();
            
            ILMfilewriter = new FileWriter (ILMFilePath + "ILM-" + (i+1) + ".domains.txt");
            ILMbw = new BufferedWriter (ILMfilewriter);
            ILMPW = new PrintWriter (ILMbw);
            
            Outputs.exportIDI(ILMPW , LMs[i]);
            ILMPW.close();
            
         }
      }

      // **************************************
      // Print constants for evolutionary cycle
      // **************************************
      
      System.out.println("******************************");
      System.out.println("Evolutionary Search Parameters");
      System.out.println("******************************");
      System.out.println();
      
      System.out.println("Algorithm Structure");
      System.out.println("-------------------");
      System.out.println("Number of lineages: " + nl );
      System.out.println("Number of cycles per lineage: " + Lineage.getCPL());
      System.out.println("Number of computing threads used: " + nt);
      System.out.println("Shuffle first lineage mother: " + sflm);
      System.out.println("Shuffle other lineage mothers: " + solm);
      System.out.println("Number of new mothers per cycle: " + Lineage.getNMPC() );
      System.out.println("Number of mutations per mother: " + Lineage.getMPM() );
      System.out.println("Number of new daughters per mother: " + Lineage.getNDPM() );
      System.out.println("Number of mutations per daughter: " + Lineage.getMPD() );
      System.out.println("Number of generations per cycle: " + Lineage.getGPC() );
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
      
      Lineage Lineages[] = new Lineage[nl];
      
      for ( int i= 0; i < nl; i++)
      {
         Lineages[i] = new Lineage( LMs[i] );
      }
      
      // **************
      // Create Threads
      // **************
      
      Thread Threads[] = new Thread[nt];
     
      // *******************
      // Execute the Threads
      // *******************
    
      System.out.println("*****************************");
      System.out.println("Beginning Evolutionary Cycles");
      System.out.println("*****************************");
      System.out.println();
      
      double startTime = System.currentTimeMillis(); // start timer for runtime.
      
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
      
      int IndexOfFittest = 0; // Index of the fittest generation 
      Arrays.stream(LMs).forEach(e -> e.getTFS());
      for( int i = 0; i < nl; i ++) // for number of lineages,
      {
         if ( LMs[i].getTFS().compareTo( LMs[IndexOfFittest].getTFS()) < 0 ) // if the lineage-mother's score is better than the current best lineage score,
         {
            IndexOfFittest = i ; //Record this index
         } 
      }
      FittestGeneration.copy(LMs[IndexOfFittest]); // replace victor with fittest lineage mother.
      
      // **********************************
      // Calculate runtime up to this point
      // **********************************
            
      double endTime   = System.currentTimeMillis(); // record evolutionary cycle endtime
      double elapsedTime = endTime-startTime;
      int H = (int)((elapsedTime/1000) / (60 *60)); // Hours
      int M = (int)(((elapsedTime/1000) / 60) % 60 ); // Minutes
      int S = (int)((elapsedTime/1000) % 60 );   // Seconds
      String totalTime = ( H + " h " + M + " m " + S + " s ");
      
      
      // ***************************************
      // Print information from final generation
      // ***************************************

      if ( eflm ) // Report-Final-Mothers if the option to print the final mother's information has been selected,
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
         }
      }    
 
      System.out.println("***********************************");
      System.out.println("Information from fittest generation");
      System.out.println("***********************************");
      System.out.println();
      
      Outputs.printIGI( FittestGeneration ); // print the fittest generation's information.
      Outputs.printScores( FittestGeneration );


      // ******************
      // Create Report File
      // ******************
       
      FileWriter filewriter = new FileWriter( ROFilePath );
      BufferedWriter bw = new BufferedWriter (filewriter);
      PrintWriter PW = new PrintWriter (bw);
      
      PW.println("SequenceEvolver Output");
      PW.println("Version: " + version );
      PW.println("Program Started: " + runDate);      
      PW.println("Time taken to complete search: " + totalTime );
      PW.println();
      
      PW.println("******************************");
      PW.println("Evolutionary Search Parameters");
      PW.println("******************************");
      PW.println();
      
      PW.println("Algorithm Duration");
      PW.println("------------------");
      PW.println("Number of cycles per lineage: " + Lineage.getCPL());
      PW.println("Number of lineages: " + nl );
      PW.println("Number of computing threads used: " + nt);
      PW.println();
      
      PW.println("Algorithm Structure");
      PW.println("-------------------");
      PW.println("Shuffle first lineage mother: " + sflm);
      PW.println("Shuffle other lineage mothers: " + solm);
      PW.println("Number of new mothers per cycle: " + Lineage.getNMPC() );
      PW.println("Number of mutations per mother: " + Lineage.getMPM() );
      PW.println("Number of new daughters per mother: " + Lineage.getNDPM() );
      PW.println("Number of mutations per daughter: " + Lineage.getMPD() );
      PW.println("Number of generations per cycle: " + Lineage.getGPC() );
      PW.println();
      
      PW.println("Scoring Parameters");
      PW.println("------------------");      
      PW.println("IntramolecularSLC: " + Generation.getIntraSLC());
      PW.println("IntermolecularSLC: " + Generation.getIntraSLC());
      PW.println("IntramolecularW: " + Generation.getIntraW());
      PW.println("IntermolecularW: " + Generation.getInterW());
      PW.println("AASLC: " + Generation.getAASLC());
      PW.println("TTSLC: " + Generation.getTTSLC());
      PW.println("CCSLC: " + Generation.getCCSLC());
      PW.println("GGSLC: " + Generation.getGGSLC());
      PW.println();
      
      PW.println("Time taken to complete evolutionary process: " + totalTime );
      PW.println();
      
      PW.println("**********************************");
      PW.println("Initial Design (Input) Information");
      PW.println("**********************************");
      PW.println();
      Outputs.exportIGI( PW, Gen0 );
      
      PW.println("*********************************");
      PW.println("Final Design (Output) Information");
      PW.println("*********************************");
      PW.println();
      Outputs.exportIGI( PW, FittestGeneration);
      
      PW.close();
      
      // **************************************
      // Print the Generation Trajectories file
      // **************************************
      
      if(Lineage.getGenerationReporting()) // if generaton trajectory reporting is active
      {
         filewriter = new FileWriter( GTOFilePath );
         bw = new BufferedWriter (filewriter);
         PW = new PrintWriter (bw);
 
         PW.print("Total Designs Scored, Generation #,");  // print the generation number in the first column 
 
         for (int i = 0; i < nl ; i++) // for each lineage
         {
            PW.print(" Lineage " + (i+1) + " Score,"); //print that column's header.
         }
         PW.println(); // move to the first row.
         
         PW.print("0, gen0"); //print gen 0's score
         for (int i = 0; i < nl ; i++) // for each lineage
         {
            PW.print(", " + Gen0.getTFS()); //print Gen 0's score
         }
         PW.println(); // move to the first row.

         for( int g = 0; g < Lineages[0].getGST().size() ; g++ ) // for all elements in the vector
         {
            PW.print( nl * Lineages[0].getTDSTrajectory().get(g) + ", " + Lineages[0].getGenerationNumberTrajectory().get(g) + ", ");  
            
            for (int i = 0; i < nl ; i++) // for each lineage
            {
               PW.print( Lineages[i].getGST().get(g) + ", "); // print the lineage's score in the next column
            }
            PW.println(); // move to the next line.
         }
         
         PW.close();
      
      }
      
      // **************************************
      // Print the Cycle Trajectories file
      // **************************************
      
      if(Lineage.getCycleReporting()) // if generaton trajectory reporting is active
      {
         filewriter = new FileWriter( CTOFilePath );
         bw = new BufferedWriter (filewriter);
         PW = new PrintWriter (bw);
 
         PW.print("Cycle #,");  // print the generation number in the first column 
                
         for (int i = 0; i < nl ; i++) // for each lineage
         {
            PW.print(" Lineage " + (i+1) + " Score,"); //print that column's header.
         }
         PW.println(); // move to the first row.

         for( int g = 0; g < Lineages[0].getCST().size() ; g++ ) // for all elements in the vectors
         {
                  
            PW.print( Lineages[0].getReportingFrequency()*(g) + ","); // print the generation (or cycle) number in the first column
            for (int i = 0; i < nl ; i++) // for each lineage
            {
               PW.print(" " + Lineages[i].getCST().get(g) + ","); // print the lineage's score in the next column
            }
            PW.println(); // move to the next line.
         }
         
         PW.close();
      }
      
      // **************************************
      // Print the Cycle Trajectories file
      // **************************************
      
      if(Lineage.getLogCycleReporting()) // if generaton trajectory reporting is active
      {
         filewriter = new FileWriter( LCTOFilePath );
         bw = new BufferedWriter (filewriter);
         PW = new PrintWriter (bw);
 
         PW.print("Cycle #,");  // print the generation number in the first column 
                
         for (int i = 0; i < nl ; i++) // for each lineage
         {
            PW.print(" Lineage " + (i+1) + " Score,"); //print that column's header.
         }
         PW.println(); // move to the first row.
         
         Vector CycleNumbers = new Vector<Integer>();
         
         CycleNumbers.add(0);
         // calculate the cycle number for at least the number of elements in LCST
         for (int i = 0;  (i*9) <= Lineages[0].getLCST().size(); i++)// for every 100 memebers of the trajectory
         {
            for (int j = 1; j < 10 ; j++)
            {
               CycleNumbers.add( j * Math.pow(10,i) );
            }
         }

         for( int g = 0; g < Lineages[0].getLCST().size() ; g++ ) // for all elements in the vectors
         {
            PW.print( CycleNumbers.get(g) + ","); // print the generation (or cycle) number in the first column
            for (int i = 0; i < nl ; i++) // for each lineage
            {
               PW.print(" " + Lineages[i].getLCST().get(g) + ","); // print the lineage's score in the next column
            }
            PW.println(); // move to the next line.
         }
         
         PW.close();
      }
      
      // **********************
      // Create DomainsOut File
      // **********************
      if( DOFilePath != "disabled")
      {
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
      }
      
      // **********************
      // Create StrandsOut File
      // **********************
      
      if (SOFilePath != "disabled")
      {
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
      }
      
      // **********************
      // Create NetworkOut File
      // **********************
      if ( NOFilePath != "disabled")
      {
         filewriter = new FileWriter( NOFilePath );
         bw = new BufferedWriter (filewriter);
         PW = new PrintWriter (bw);
         
         PW.println("Fittest Generation from StrandEvolver; Version: " + version + "; run time: " + runDate);
         PW.println();      
         Outputs.exportNI ( PW , FittestGeneration);

         PW.close(); 
      }
      
      // ********************************
      // Create Interference Output File
      // ********************************
      if( IOFilePath != "disabled" )
      {
         FileWriter interferenceFileWriter = new FileWriter( IOFilePath );
         BufferedWriter interferenceBW = new BufferedWriter (interferenceFileWriter);
         PrintWriter interferencePW = new PrintWriter (interferenceBW);
         
         interferencePW.println("// Profile of interference structures found in fittest generation. Produced by SequenceEvolver Version: " + version + "; run on  " + runDate);
         interferencePW.println();  

         interferencePW.println("**************************************");
         interferencePW.println("Intramolecular Interference Structures");
         interferencePW.println("**************************************");
         interferencePW.println();
         
         interferencePW.println("Complement Length (Base-Pairs), Number of Complements");
         interferencePW.println("-----------------------------------------------------");
         
         FittestGeneration.getIntraInterferenceProfile().entrySet().stream().forEach( e -> interferencePW.println(e.getKey()+", "+e.getValue()));
         interferencePW.println();
         interferencePW.println();

       
         interferencePW.println("**************************************");
         interferencePW.println("Intermolecular Interference Structures");
         interferencePW.println("**************************************");
         interferencePW.println();
         interferencePW.println("Complement Length (Base-Pairs), Number of Complements");
         interferencePW.println("-----------------------------------------------------");
         
         FittestGeneration.getInterInterferenceProfile().entrySet().stream().forEach( e -> interferencePW.println(e.getKey()+", "+e.getValue()));
         interferencePW.close();
         
      }
      
     
      System.out.println("***********");
      System.out.println("Program End");
      System.out.println("***********");
      System.out.println();
      
      // **************
      // Report Runtime
      // **************
      
      System.out.println("Runtime of Evolutionary Process: " + totalTime);
      System.out.println();
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
      // **************************************
      // Set Default Values for local variables
      // **************************************
      
      nl = 8; // Number-Lineages 
      nt = Runtime.getRuntime().availableProcessors();
      
      eilm = false; // Export-Initial-Lineage-Mothers
      eflm = false; // Export-Final-Lineage-Mothers
      
      sflm = false; // Shuffle-First-Lineage-Mothers
      solm = true; // Shuffle-Other-Lineage-Mothers
      
      GTOFilePath = "se.out.generation-trajectory.csv"; //GTO: Generation-Trajectory-Output 
      CTOFilePath = "se.out.cycle-tragectory"; //log-Cycle-Trajectory-Output
      LCTOFilePath = "se.out.log-cycle-trajectory.csv";
      ROFilePath = "se.out.report.txt"; // RO: Report-Out
      SOFilePath = "se.out.strands.txt"; // SO: Strands-Out
      DOFilePath = "se.out.domains.txt"; // DO: Domains-Out  
      NOFilePath = "disabled"; // NO: Network-Out 
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

