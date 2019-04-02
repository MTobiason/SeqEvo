package BoiseState;
import java.io.* ;
import java.util.Vector;


public class StrandPool
{
   //*******************************************
   //Call the MTlogger, which will print outputs
   //only if they are asked for. (Debug/Verbose)
   //*******************************************
   
   static MTlogger MTout = new MTlogger("StrandPool.java",3);
   
   //**********************************
   //Constants for "StrandPool" objects
   //**********************************

   public int mns;   // Maximum-Number-Strands: Integer declaring the maximum number of Strands which can be input.
   public int msrl;  // Maximum-Strand-Recipe-Length: Integer declaring the maximum number of domains in a strand.
   
   //**********************************
   //Variables for "StrandPool" objects
   //**********************************
   
   public String sn[];         // Strand-Name: Array storing the strand names.
   public String sr[][]; // Strand-Recipe: Array storing the strand recipes.
   public int isr[][];      // Indexed-Strand-Recipe: Strand Recipe, containing domain indexes instead of domain names.
   public int srl[];              // Strand-Recipe-Length: Array storing the number of domains in each strand
   public int tns = 0;                          // Total-Number-Strands: Total number of strands imported.
   public char st[]; //Strand-Type
   // ***************************************
   // Global variables for strandpool objects
   // ***************************************
   
   private static String SIFilePath;
   
   //*********************************************
   //Constructors which create a StrandPool object
   //*********************************************
   
   public StrandPool() throws Exception
   {
      
      MTout.log("Calculating Max-Number-Strands from: " + SIFilePath);
      calculateMNS(SIFilePath);
      
      MTout.log("Calculating Max-Strand-Recipe-Length from: " + SIFilePath);
      calculateMSRL(SIFilePath);
      
      MTout.log("Setting array sizes using mns = " + mns +"; msrl = " + msrl);
      setArraySizes();
   
      MTout.log("Importing strand information from: " + SIFilePath);
      readStrands(SIFilePath);
   }
   
   // *************************************************
   // Method to set the Maximum-Number-Strands Variable
   // *************************************************
   //set the maximum possible number of domains to be the number of (non-comment) lines in the passed file
   
   public void calculateMNS( String InputFilePath) throws Exception
   {
      mns = 1;
      FileReader filereader = new FileReader(InputFilePath);   
      StreamTokenizer streamtokenizer = new StreamTokenizer(filereader);
      streamtokenizer.eolIsSignificant(true);

      while(streamtokenizer.ttype != streamtokenizer.TT_EOF) // for each line of input file, until end of file
      {
         if (streamtokenizer.ttype == streamtokenizer.TT_EOL)
         {
            mns = mns + 1; 
         }
         streamtokenizer.nextToken();
      }
   }
   
   // *******************************************************
   // Method for calculating the maximum strand-recipe-length
   // *******************************************************

   public void calculateMSRL(String IncomingFilePath) throws Exception
   {
      msrl = 1;
      FileReader filereader = new FileReader(IncomingFilePath); // Start FileReader
      StreamTokenizer streamtokenizer = new StreamTokenizer(filereader); // Start Tokenizer

      streamtokenizer.nextToken(); // Skip first token, which is start of file.
      while( streamtokenizer.ttype != StreamTokenizer.TT_EOF) // for each line of code...
      {
         int SRLcounter = 0; // initiate the current recipe length counter ...
         streamtokenizer.nextToken(); // Skip the strand number supplied by user, move to strand name
         streamtokenizer.nextToken() ; // Skip the strand name, move to strand recipe
		
         while(streamtokenizer.ttype == streamtokenizer.TT_WORD)  // count tokens until token type is no longer word
         {
            SRLcounter++; //increment the counter
            streamtokenizer.nextToken(); //move to the next token (hopefully recipe element
         }
         
         if (SRLcounter > msrl) // if the current number of recipe elements is longer than the previous max,
         {
            msrl = SRLcounter; // make this the new max.
         }
      }  
   }
   
   // **********************************************************************
   // Method for setting size of the variable arrays once mns is calculated.
   // **********************************************************************
   //set the maximum possible number of domains to be the number of (non-comment) lines in the passed file.
   
   public void setArraySizes() 
   {
      sn = new String[mns];         // Strand-Name: Array storing the strand names.
      sr = new String[mns][msrl]; // Strand-Recipe: Array storing the strand recipes.
      isr = new int[mns][msrl];      // Indexed-Strand-Recipe: Strand Recipe, containing domain indexes instead of domain names.
      srl = new int[mns];              // Strand-Recipe-Length: Array storing the number of domains in each strand
      st = new char[mns];
   }

   // **************************************************************
   // Method for reading the strand information from the passed file
   // **************************************************************

   public void readStrands(String InputFileName) throws Exception
   {
      FileReader filereader = new FileReader(InputFileName);
      StreamTokenizer streamtokenizer = new StreamTokenizer(filereader);

	   int token = streamtokenizer.nextToken();
      for(int i =0; streamtokenizer.ttype != StreamTokenizer.TT_EOF && i < mns; i++)
      {
	  
         streamtokenizer.nextToken(); // Skip the strand number supplied by user, move to strand name
         sn[i] = streamtokenizer.sval; // Read the strand name provided by user

         streamtokenizer.nextToken() ; // move to strand recipe
		
         for(int j = 0; streamtokenizer.ttype == streamtokenizer.TT_WORD && j < msrl; j++)  // import tokens until token type is no longer word, or until j exceeds maximum.
         {
            sr[i][j] = streamtokenizer.sval;
            streamtokenizer.nextToken();
            srl[i] = j + 1;
         }
         st[i] = 'L';
		tns = i + 1;  //Total number of strands is equal to i + 1
      }  
   }
   
   //********************************
   //Method for Printing Strand Names 
   //********************************
   
   public void printNames()
   {
      System.out.println("Printing Strand Names (sn[i])");
      for(int i = 0; i < tns; i++)
      {
         System.out.println("sn[" + i + "] = " + sn[i] );
      }
      System.out.println();
   }

   //**********************************
   //Method for Printing Strand Recipes 
   //**********************************
   
   public void printRecipes()
   {
      System.out.println("Printing Strand Recipes (sr[i][j])");
      for(int i = 0; i < tns; i++)
      {
         for( int j = 0; j < srl[i]; j++)
         {
            System.out.println("sr[" + i + "][" + j + "] = " + sr[i][j] );
         }
      }
      System.out.println();
   }
   
   public static void setSIFilePath( String ISIFilePath )
   {
      SIFilePath = ISIFilePath;   
   }
   
   public static void importSettings( String ParametersFilePath ) throws Exception
   {
         
      MTout.log("Importing Settings for StrandPool() module from "+ ParametersFilePath );
      
      SIFilePath = "in.strands.txt"; // SI: Strands-In
      
      FileReader filereader = new FileReader(ParametersFilePath);   
      StreamTokenizer streamtokenizer = new StreamTokenizer(filereader);
      
      while( streamtokenizer.ttype != StreamTokenizer.TT_EOF ) // for the entire "parameters" file
      {
         streamtokenizer.nextToken(); 
         if (streamtokenizer.ttype == StreamTokenizer.TT_WORD)
         {
            if (streamtokenizer.sval.equalsIgnoreCase("sifilepath"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  SIFilePath = streamtokenizer.sval;
                  System.out.println("SIFilePath value imported. Accepted value: "+ SIFilePath);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"SIFilePath\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"SIFilePath\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
            }
         }
      }
   }
   
   public String getStrandName( int ISI) //incoming Strand Index
   {
      return sn[ISI];
   }
   
   public char[] getST()
   {
      return st;
   }
      
}
