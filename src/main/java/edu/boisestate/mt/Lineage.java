package edu.boisestate.mt;
import java.io.* ;
import java.util.Date;
import java.util.Vector;
import java.util.Arrays;
import java.math.BigInteger;

public class Lineage implements Runnable
{

   // "new" Variables
   
   private static MTlogger MTout = new MTlogger("Lineage.java", 3); // call file "Lineage.java", indent output 3 spaces
   
   private static int lineageCount = 0;
   private int lineageNumber; // Index number for the current lineage.
   
   private static int mpc ; // Number-of-Mothers: Mothers to spawn from each lineage
   private static int ndpm ; // Number-of-Daughters: Daughters to spawn from each Mother
   
   private static int cpl; // Cycles-Per-Lineage: Number of consecutive Mutate-Evolve-Select cycles are performed per lineage.
   private static int gpc; // Generations-Per-Cylce: Generations for each Mutate-Evolve-Select cycle.
   private static int mpm; // Mutations-Per-Mother: Number of times a Lineage's Mother0 is mutated to form more mothers.
   private static int mpd; // Mutations-Per-Daughter: Number of times a Mother is mutated to form it's daughters.
   
   private int cg = 1; // Current-Generation: counter storing what generation the overall lineage is currently on.
   private Vector GST; // Generation-Score-Trajectory: The score of the Lineage-Mother as a function of generation number.
   private Vector logCST; // Logarithmic cycle score trajectory
   private Vector linearCST; // Linear cycle score trajectory
   private Vector TDSTrajectory; //Vector for storing how many total designs have been scored every time a generation trajectory was recorded
   private Vector GenerationNumberTrajectory; //Vector for recording how many generations have been executed including cycle mothers and succesive daughters.
   private static String tr;  // trajectory-reporting: Wither trajectories file contains scores for each generation or cycle.
   
   private static boolean GenerationReporting;
   private static boolean CycleReporting;
   private static boolean LogCycleReporting;
   private static int ReportingFrequency;
   
   private Generation LM; // Lineage-Mother: Overall mother for the entire lineage.
   private Generation CMs[]; // Cycle-Mothers :
   private Generation CDs[]; // Cycle-Daughters :  

   private Generation Mother;
   
   // **********************************************************
   // Constructor for creating a lineage from a given generation
   // **********************************************************
      
   public Lineage ( Generation IG ) // Incoming-Generation
   {
      lineageNumber = lineageCount;
      lineageCount = lineageCount + 1;
      LM = IG; // Lineage-Mother = Incoming-Generation
   }
   
   public void run()
   {
      
      // ***************************
      // Create Cycle-Mother's Array
      // ***************************
      //MTout.log("Creating Cycle-Mothers Array");
      CMs = new Generation[mpc];  // Cycle-Mothers: create mpc mother generations.
      
      GST = new Vector<BigInteger>();
      linearCST = new Vector<BigInteger>();
      logCST = new Vector<BigInteger>();
      TDSTrajectory = new Vector<Integer>();
      GenerationNumberTrajectory = new Vector<Integer>();
      Integer TDS = 0; // No designs have been scored yet
      Integer GenerationNumber =0;
      
      //MolecularScore.scoreGeneration( LM );// Score the lineage mother
      MTout.log("Scoring Lineage Mother");
      LM.getTFS();
      
      GenerationNumber++; //first generation 
      TDS++; // One Design has been scored.
      GST.add( LM.getTFS() ); // Record the score of the lineage mother (generation #1)
      TDSTrajectory.add( TDS); // Record the number of generations scored so far.
      GenerationNumberTrajectory.add(GenerationNumber); //record that generation #1 was recorded
      linearCST.add( LM.getTFS() );
      logCST.add( LM.getTFS() );      
      
      MTout.log("Creating Cycle-Mothers Array");
      for( int i =0; i< mpc; i ++)
      {
         CMs[i] = new Generation( LM ); // Cycle-Mothers become exact clones of Lineage-Mother
      }
      
      // ****************************
      // Create Cycle-Daughters Array
      // ****************************
      
      MTout.log("Creating Cycle-Daughters Array");
      CDs = new Generation[ (mpc*ndpm) ];  // Cycle-Daughters: create ndpm daughters per mother.
      
      for(int i = 0; i < (mpc*ndpm) ; i++) // for each Daughter.
      {
         CDs[i] = new Generation ( LM ); // Cycle-Daughters initially become exact clones of Lineage-Mother
      }
      
      // *****************************************************
      // Initiate Array to track each cycle's score trajectory
      // *****************************************************
      
      MTout.log("Creating Score Trajectories");
      Vector cst[]  = new Vector[mpc]; // Cycle-Score-Trajectory (Follows each Mother's Score)
      
      for ( int i =0; i < mpc; i ++) // for all Cycle-Mothers
      {
         cst[i] = new Vector<BigInteger>();
      }
      
      
      MTout.log("Beginning Lineage Sub-Cycles");
      for(int cycle = 1; cycle <= cpl; cycle ++) // for each cycle requested.
      {

         // *************
         // Clone Mothers
         // *************
         
         MTout.log("Copying Lineage-Mother to Cycle-Mothers");
         for( int i =0; i < mpc; i++) // for all Cycle-Mothers
         {
            CMs[i].copy(LM); // copy the Lineage-Mother
         }
         
         // ******************
         // Mutate the Mothers
         // ******************
         
         MTout.log("Mutating Cycle-Mothers");
         for(int i = 1; i < mpc; i++) // for all mothers (except Mother 0 )
         { 
            for( int j =0; j < mpm ; j++) // for the requested number of transpositions.
            {
               Generation.validatedTransposition(CMs[i]); // Call the module for selecting a valid transposition/mutation
            }
            
            //Score this Mother
            
            //MolecularScore.scoreGeneration(CMs[i]);
            //CMs[i].getTFS();
            TDS = TDS + 1;
         }
         
         GenerationNumber++; // record that another generation was itterated
         
         if( GenerationReporting ) //if we are reporting generations
         {
            for(int i = 0; i <mpc; i++) // for each mother
            {
               cst[i].clear(); //clear Score-Trajectory
               cst[i].add( CMs[i].getTFS()); //Mother's score is stored in her cycle-trajectory
            }
            TDSTrajectory.add(TDS);
            GenerationNumberTrajectory.add(GenerationNumber);
         } 
         

         // Record the initial Cycle-Mother's Score
         
         BigInteger ICMs = CMs[0].getTFS(); // Initial-Cycle-Mothers-Score 
         
         // ******************
         // Evolve each Mother
         // ******************
         
         MTout.log("Entering Evolutionary Cycle for Each Cycle-Mother");
         for (int g = 0; g < gpc; g ++) // for gpc Generations
         {
            for (int i =0; i < mpc; i ++) // for each mother
            {
               int DaughterNumber = 0 ;
               for( int j = 0; j < ndpm; j++) // for each daughter
               {
                  MTout.log("Cloning Mothers to Daughters");
                  DaughterNumber = (( i * ndpm ) + j);
                  CDs[ DaughterNumber ].copy(CMs[i]); // Daughter becomes clone of mother.

                  
                  MTout.log("Mutating Daughters");
                  for ( int k =0 ; k < mpd ; k++) // for each requested mutation.
                  {
                     Generation.validatedMutation( CDs[DaughterNumber] );
                  }
               }
            }
            
            //MolecularScore.scoreGenerations( CDs ); // Score all Daughters
            //Arrays.stream(CDs).forEach(e -> e.getTFS());
            
            GenerationNumber++; // record that another generation was itterated
            TDS= TDS + (ndpm*mpc) ;
            
            MTout.log("Scoring and comparing daughters");
            
            for (int i =0; i < mpc; i ++) // for each mother
            {
               int DaughterNumber = 0;
               for( int j = 0; j < ndpm; j++) // for each daughter
               {
                  
                  DaughterNumber = (( i * ndpm ) + j);
                  if ( CDs[ DaughterNumber ].getTFS().compareTo( CMs[i].getTFS() ) <= 0 ) // If the daughter's score improves upon Mother's, or is equivalent
                  {
                     CMs[i].copy( CDs[ DaughterNumber ] ); // Daughter Becomes Mother

                  }
               }
            }
            
            if( GenerationReporting )
            {
               for(int i = 0; i <mpc; i++) // for each mother
               {
                  cst[i].add( CMs[i].getTFS() ); //Mother's score is stored in her cycle-trajectory
               }
               TDSTrajectory.add(TDS);
               GenerationNumberTrajectory.add(GenerationNumber);
            }
            
            if(ProgressReporter.isReportingLineage(lineageNumber) ) 
            {
               ProgressReporter.incrementGenerations( 1 );
               ProgressReporter.updateProgress();
            };
         }
         
         MTout.log("Selecting the fittest Cycle-Mother");
         int fittestCycleMotherIndex = 0;
         for( int i =1 ; i < mpc; i ++ ) // for all Cycle-Mothers
         {
            if ( CMs[i].getTFS().compareTo( CMs[fittestCycleMotherIndex].getTFS()) <=0 ) // if the Cycle-Mother's score improves upon the currently fittest Cycle-Mother's.
            {
               fittestCycleMotherIndex = i; //this mother becomes the fittest cycle mother
            }
         }
		 
         LM.copy( CMs[fittestCycleMotherIndex] );

         
         MTout.log("Adding Score Trajectory to Lineage's ST.");
         if(GenerationReporting)
         {
            GST.addAll( cst[fittestCycleMotherIndex] ); // Add all generation's scores to the score trajectory.
         }
         
         if (CycleReporting)
         {
            if(recordCycle( cycle , "Cycles-Linear" )) { linearCST.add( CMs[fittestCycleMotherIndex].getTFS() );} //Add the initial cycle mother's score to the score trajectory.
         }
         if (LogCycleReporting)
         {
            if(recordCycle( cycle , "Cycles-Log" )) { logCST.add( CMs[fittestCycleMotherIndex].getTFS());} //Add the initial cycle mother's score to the score trajectory.
         }
      }
   }
   
   public static void importSettings( String ParametersFilePath) throws Exception
   {
   
      //MTout.log("Importing Settings for Lineage() module from "+ ParametersFilePath );
      
      // ******************
      // Set Default Values
      // ******************

      cpl = 1000; // Cycles-Per-Lineage 
      gpc = 1; // Generations-Per-Cycle
      mpd = 1; // Mutations-Per-Daughter
      mpm = 1; // Mutations-Per-Mother
      mpc = 3; // Number-of-Mothers (Mothers Per Cycle)
      ndpm = 1; // Mutated-Daughters-Per-Mother
      
      ReportingFrequency = 1;
      boolean ReportingFrequencyTrigger = false;
      GenerationReporting = false;
      CycleReporting = false;
      LogCycleReporting = true;
  
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
            if (streamtokenizer.sval.equalsIgnoreCase("cpl"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 0)
               {
                  cpl = (int)streamtokenizer.nval;
                  System.out.println("cpl value imported. Accepted value: " + cpl);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"cpl\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"cpl\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
            }            
            else if (streamtokenizer.sval.equalsIgnoreCase("CycleReporting")) //Trajectory-Reporting
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  switch ( streamtokenizer.sval.toLowerCase() )
                  {
                     case "true" : 
                        CycleReporting = true;
                        System.out.println("CycleReporting value imported. Accepted value: " + CycleReporting);
                        break;
                     case "false" :
                        CycleReporting = false;
                        System.out.println("CycleReporting value imported. Accepted value: " + CycleReporting);
                        break;
                     default :
                        CycleReporting = false;
                        System.out.println("CycleReporting value invalid. Invalid value: " + streamtokenizer.sval.toLowerCase() + ". Using value: " + CycleReporting);
                        break;
                  }
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("GenerationReporting")) //Trajectory-Reporting
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  switch ( streamtokenizer.sval.toLowerCase() )
                  {
                     case "true" : 
                        GenerationReporting = true;
                        System.out.println("GenerationReporting value imported. Accepted value: " + GenerationReporting);
                        break;
                     case "false" :
                        GenerationReporting = false;
                        System.out.println("GenerationReporting value imported. Accepted value: " + GenerationReporting);
                        break;
                     default :
                        GenerationReporting = false;
                        System.out.println("GenerationReporting value invalid. Invalid value: " + streamtokenizer.sval.toLowerCase() + ". Using value: " + GenerationReporting);
                        break;
                  }
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"tr\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"tr\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("gpc"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval > 0)
               {
                  gpc = (int)streamtokenizer.nval;
                  System.out.println("gpc value imported. Accepted value: " + gpc);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"gpc\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"gpc\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            } 
            else if (streamtokenizer.sval.equalsIgnoreCase("LogCycleReporting")) //Trajectory-Reporting
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  switch ( streamtokenizer.sval.toLowerCase() )
                  {
                     case "true" : 
                        LogCycleReporting = true;
                        System.out.println("LogCycleReporting value imported. Accepted value: " + LogCycleReporting);
                        break;
                     case "false" :
                        LogCycleReporting = false;
                        System.out.println("LogCycleReporting value imported. Accepted value: " + LogCycleReporting);
                        break;
                     default :
                        LogCycleReporting = false;
                        System.out.println("LogCycleReporting value invalid. Invalid value: " + streamtokenizer.sval.toLowerCase() + ". Using value: " + LogCycleReporting);
                        break;
                  }
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"tr\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"tr\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
            }            

            else if (streamtokenizer.sval.equalsIgnoreCase("mpd"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 0)
               {
                  mpd = (int)streamtokenizer.nval;
                  System.out.println("mpd value imported. Accepted value: " + mpd);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"mpd\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"mpd\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("mpm"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 0)
               {
                  mpm = (int)streamtokenizer.nval;
                  System.out.println("mpm value imported. Accepted value: " + mpm);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"mpm\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"mpm\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
            }            
            else if (streamtokenizer.sval.equalsIgnoreCase("ndpm"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 0)
               {
                  ndpm = ((int)streamtokenizer.nval);
                  System.out.println("ndpm value imported. Accepted value: " + ndpm);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"ndpm\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"ndpm\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("nmpc"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 0)
               {
                  mpc = ((int)streamtokenizer.nval+1);
                  System.out.println("nmpc value imported. Accepted value: " + (mpc-1) );
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"nmpc\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"nmpc\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("ReportingFrequency"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval > 0)
               {
                  ReportingFrequency = (int)streamtokenizer.nval;
                  System.out.println("ReportingFrequency value imported. Accepted value: " + ReportingFrequency);
                  ReportingFrequencyTrigger = true;
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"ReportingFrequency\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"ReportingFrequency\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }   
            else if (streamtokenizer.sval.equalsIgnoreCase("tr")) //Trajectory-Reporting
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  switch ( streamtokenizer.sval.toLowerCase() )
                  {
                     case "generation" : 
                        tr = "generation";
                        System.out.println("tr value imported. Accepted value: " + tr);
                        break;
                     case "cycle" :
                        tr = "cycle";
                        System.out.println("tr value imported. Accepted value: " + tr);
                        break;
                     default :
                        tr = "cycle";
                        System.out.println("tr value invalid. Invalid value: " + streamtokenizer.sval.toLowerCase() + ". Using value: " + tr);
                        break;
                  }
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"tr\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"tr\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
            }
         }   
      }
      if (ReportingFrequencyTrigger == false) {ReportingFrequency = (int) Math.ceil((double) cpl/1000);} //set default reporting frequency
   }
   
   //CDs[ DaughterNumber ].mutateDomain( CDs[ DaughterNumber].selectDomainForMutation()); // Select a domain for mutation, and mutate it.
   
   public void setLM(Generation IM) // IM: Incoming-Mother
   {
      LM = IM;
   }
   
   public int getCG()
   {
      return cg;
   }
   
   public Generation getLM()
   {
      return LM;
   }
   
   static public int getMPC()
   {
      return mpc;
   }
   static public int getNMPC()
   {
      return mpc-1;
   }
      
   static public int getNDPM()
   {
      return ndpm;
   }
      
   static public int getCPL()
   {
      return cpl;
   }
      
   static public int getGPC()
   {
      return gpc;
   }
      
   static public int getMPM()
   {
      return mpm;
   }
  
   static public int getMPD()
   {
      return mpd;
   }
   
  

   static public boolean recordCycle( int CycleNumber, String TT) //TT : Trajectory-type
   {
      switch (TT)
      {
         case "Cycles-Linear":
            if ( CycleNumber % ReportingFrequency == 0 )
               {return true;}
            break;
         case "Cycles-Log":
         {
            int i = 1;
            while (i <= CycleNumber)
            {
               for (int k = 1; k < 10; k++) //for every 10 cycles
               {
                  if( (k*i) == CycleNumber) {return true;} //select cycles with 3 significant figures.
               }
               i= 10*i;
            }
            return false;
         }
      }
      return false;
   }
   
   static public boolean recordGeneration(int GenNumber)
   {
      
      if(GenerationReporting == false) {return false;}
      else if ( GenNumber % ReportingFrequency == 0)
      {
         return true;
      }
      return false;
   }
   
   public Vector getGST( )
   {
      return GST;
   }

   public Vector getCST( )
   {
      return linearCST;
   }

   public Vector getLCST()
   {
      return logCST;
   }
   
   static public boolean getGenerationReporting()
   {
      return GenerationReporting;
   }
   
   static public boolean getCycleReporting()
   {
      return CycleReporting;
   }
   
   static public boolean getLogCycleReporting()
   {
      return LogCycleReporting;
   }
   
   static public int getReportingFrequency()
   {
      return ReportingFrequency;
   }
   
   public Vector<Integer> getTDSTrajectory()
   {
      return TDSTrajectory;
   }
   public Vector<Integer> getGenerationNumberTrajectory()
   {
      return GenerationNumberTrajectory;
   }
}