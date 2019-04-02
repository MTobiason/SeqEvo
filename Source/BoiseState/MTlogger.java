package BoiseState;
import java.io.* ;

public class MTlogger
{
   //*********************************
   //Variables which are program wide:
   //*********************************
   
   public static boolean DebugFlag = false;  //Variable which will tell all MTlogger objects to print Debug messages
   public static boolean VerboseFlag = false;  //Variable which will tell all MTlogger objects to print Verbose Messages
   
   //**************************************
   //Variables which are local to each file
   //**************************************
   
   public String Filename;
   public int Indent = 0;
   
   //************
   //Constructors
   //************
   
   public MTlogger(String IncomingFilename)
   {
      Filename = IncomingFilename;
   }
   
   public MTlogger(String IncomingFilename, int IncomingIndent)
   {
      Filename = IncomingFilename;
      Indent = IncomingIndent;
   }
      
   // *****************************
   // Method for getting Debug Flag
   // *****************************
   
   public static boolean getDebug()
   {
      return DebugFlag;
   }
   
   // *******************************
   // Method for getting Verbose Flag
   // *******************************
   
   public static boolean getVerbose()
   {
      return VerboseFlag;
   }
   
   // ********************************
   // Method for logging information
   // This prints a passed message in 
   // debug or verbose format.
   // ********************************
   
   public void log(String IncomingMessage)
   {
      if (DebugFlag || VerboseFlag) //If program has request either debug or verbose.
      {
         for (int i = 0; i < Indent; i++) //Indent the output Indent number of times.
         {
            System.out.print(" ");
         }
      }
      
      if (DebugFlag) //If the program has requested debug mode ...
      {
         System.out.print(Filename + " says: " ); // Print the current file and " says "
      }
      
      if (DebugFlag || VerboseFlag) // If the program has requested either debug or verbose mode ...
      {
         System.out.print (IncomingMessage); //Print the message passed to the MTlogger
         System.out.println(); // end current line
         System.out.println(); // create a blank line 
      }
   }
   
   // *****************************
   // Method for setting Debug Flag
   // *****************************
   
   public static void setDebug( boolean IncomingFlag)
   {
      DebugFlag = IncomingFlag;
   }
   
   // *******************************
   // Method for setting Verbose Flag
   // *******************************
   
   public static void setVerbose( boolean IncomingFlag)
   {
      VerboseFlag = IncomingFlag;
   }
   
   // ********************************************
   // Method for setting indent amount (in spaces)
   // ********************************************
   
   public void setIndent( int IncomingIndent)
   {
      Indent = IncomingIndent;  // set indent to be IncomingIndent number of spaces
   }
}
