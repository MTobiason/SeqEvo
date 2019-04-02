package BoiseState;
import java.io.* ;
import java.util.Date;
import java.util.Vector;

public class ProgressReporter
{
   static private int TotalThreadCycles;
   static private int CurrentThreadCycle;
   static private int NumberOfUpdates;
   static private int NumberIncrements;
   static private int UpdateCalls;
   static private int PreviousUpdate;
   
   static private int TotalGenerations; //total number of generations to accumulate
   static private int CurrentGeneration; //current cumulative generation
   static private int nl; //Number-Lineages
   static private int nt;//Number-Threads
   static private double StartTime;
   
   static private int CompletionPercentage;
   
   public ProgressReporter()
   {
      NumberOfUpdates=0;
      CompletionPercentage=0;
      CurrentGeneration = 0;
      CurrentThreadCycle = 0;
      UpdateCalls = 0;
      PreviousUpdate = 0;
   }
   
   static public void endReporting()
   {
      System.out.println();
      System.out.println();
   }
   
   static public void startTiming()
   {
      StartTime = System.currentTimeMillis(); // start timer for predicting completion time.
   }
   
   static public void incrementGenerations( int Generations )
   {
         CurrentGeneration = CurrentGeneration + Generations;
   }   
   
   static public void incrementThreadCycle()
   {
      CurrentThreadCycle = CurrentThreadCycle + 1;
   }
   
   static public boolean isReportingLineage( int LineageNumber)
   {
      if( LineageNumber % nt == 0)
      {
         return true;
      }
      else return false;
   }
   
   static public void reportProgress()
   {
      System.out.println("Current Progress:");
      System.out.print("0% completed");
   }
   
   static public void setNumberThreads(int NumberThreads)
   {
      nt = NumberThreads;
   }
      
   static public void setTotalGenerations( int Generations)
   {
         TotalGenerations = Generations;
   }
   
   static public void setTotalThreadCycles( int NumberCycles)
   {
      TotalThreadCycles = NumberCycles;
   }
   
   static public void setCurrentThreadCycle (int CycleNumber)
   {
      CurrentThreadCycle = CycleNumber;
   }
   
   static public void updateProgress()
   {
         UpdateCalls++;
         int CG = CurrentGeneration;   // Finished-Generations
         int TG = TotalGenerations * TotalThreadCycles;   // Total-Generations to calculate
         double ET = (System.currentTimeMillis() - StartTime) ; // Elapsed-Time;
         double FC = ( ((double)CG) / TG ) ; // Fraction-Completed
         int CP = (int)(FC * 100); // Completion-Percent
         double PRT = ( ( ET / FC )* (1 - FC) );   // Projected-Remaining-Time
         int RH = (int)((PRT/1000) / (60 *60)); // Remaining-Hours
         int RM = (int)(((PRT/1000) / 60) % 60 ); // Remaining-Minutes
         int RS = (int)((PRT/1000) % 60 );   // Remaining-Seconds
         
         if ( CP != CompletionPercentage || UpdateCalls >= 2*PreviousUpdate )
         {
            System.out.print("\r" + (int)(FC * 100) + "% completed; " + "Estimated time remaining: " + RH + " h " + RM + " m " + RS + " s                ");
            CompletionPercentage = CP;
            PreviousUpdate = UpdateCalls;
         };
         
   }
}
