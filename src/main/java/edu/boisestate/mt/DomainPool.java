package edu.boisestate.mt;
import java.io.* ;
import java.util.Vector;

public class DomainPool
{

   //*******************************************
   //Call the MTlogger, which will print outputs
   //only if they are asked for. (Debug/Verbose)
   //*******************************************
   
   static MTlogger MTout = new MTlogger("DomainPool.java",3);
   
   //**********************************
   //Constants for "DomainPool" objects
   //**********************************

   public int mnd;   // Maximum-Number-Domains: Integer declaring the maximum number of domains which can be input.
   public int nbt = 4;    //Number-Base-Types: (There are 4 traditional bases A,C,G,T)
   
   //**********************************
   //Variables for "DomainPool" objects
   //**********************************
   
   public int dbn[][];  // Domain-Base-Number: Array storing the number of each base to seed (variable arrays)
   public int dl[];        // Domain-Lengths: the length of the sequences in each domain.
   private int eds[][];     // Encoded-Domain-Sequences: Array storing sequences after conversion to integers.
   
   public String dn[];  // Domain-Names: String array for storing domain names
   public String ds[];  // Domain-Sequence: String Storing the Sequence of a Strand (convention will be index 0 = 5' end)
   public String dt[];  // Domain-Types: String array storing if an array is fixed or variable
   
   public int ldl = 0; // Longest-Domain-Length: Longest domain length encountered after import.
   public int tnd = 0; // Total-Number-Domains: The number of domains found upon importing the file.
   private static int nvd = 0;
   
   // ***************************************
   // Global Variables for DomainPool objects
   // ***************************************
   
   private static String IDSFilePath;
   
   //*********************************************
   //Constructors which create a DomainPool object
   //*********************************************
   
   public DomainPool() throws Exception
   {
   
      MTout.log("Calculating the number of domains in: " + IDSFilePath);
      calculateMND( IDSFilePath );
      
      MTout.log("Setting the size of arrays using mnd = " + mnd);
      setArraySizes();
   
      MTout.log("Importing domain information from: " + IDSFilePath);
      readDomains( IDSFilePath );
      
      MTout.log("Seeding variable domains");
      seedVariableDomains();
      
      MTout.log("Calculating domain lengths");
      calculateDomainLengths();
      
      MTout.log("Creating encoded domains");
      encodeDomains();
   }
  
  
   //*************************************
   //Method for calculating Domain Lengths
   //*************************************
   
   public void calculateDomainLengths()
   {
      for(int i=0; i < tnd; i++) //iterate through all domains
      {
         dl[i] = ds[i].length();   // set domain length to be the length of the string.
         if (dl[i] > ldl) 
         {
            ldl = dl[i];
         }
         
      }
   }
   
   // *************************************************
   // Method to set the Maximum-Number-Domains Variable
   // *************************************************
   //set the maximum possible number of domains to be the number of (non-comment) lines in the passed file
   
   public void calculateMND( String ParametersFilePath) throws Exception
   {
      mnd = 1;
      FileReader filereader = new FileReader(ParametersFilePath);   
      StreamTokenizer streamtokenizer = new StreamTokenizer(filereader);
      streamtokenizer.eolIsSignificant(true);

      while(streamtokenizer.ttype != streamtokenizer.TT_EOF) // for each line of input file, until end of file
      {
         if (streamtokenizer.ttype == streamtokenizer.TT_EOL)
         {
            mnd = mnd + 1; 
         }
         streamtokenizer.nextToken();
      }
   }
   
   // **********************************************************************
   // Method for setting size of the variable arrays once mnd is calculated.
   // **********************************************************************
   //set the maximum possible number of domains to be the number of (non-comment) lines in the passed file.
   
   public void setArraySizes() 
   {
      dbn = new int[mnd][nbt];  // Domain-Base-Number: Array storing the number of each base to seed (variable arrays)
      dl = new int[mnd];        // Domain-Lengths: the length of the sequences in each domain.
      dn = new String[mnd];  // Domain-Names: String array for storing domain names
      ds = new String[mnd];  // Domain-Sequence: String Storing the Sequence of a Strand (convention will be index 0 = 5' end)
      dt = new String[mnd];  // Domain-Types: String array storing if an array is fixed or variable
   }

   
   //*********************************************
   //Method for encoding domains from ACGT to 1234
   //*********************************************
   
   //************************************************
   //Notice: CalculateDomainLengths must be run prior
   //to EncodeDomains, since this function requires 
   //ldl to have been calculated!
   //************************************************
   
   public void encodeDomains()
   {
      eds = new int[tnd][ldl];
      for (int i = 0; i < tnd; i++ ) // loop for all domains
      {
         int j = 0;
         for (char base: ds[i].toCharArray()) // for each base (character) in the ds[i] string
         {
            switch (base)
            {
               case 'A': case 'a':
                  eds[i][j] = 1;
                  break;
               case 'C': case 'c':
                  eds[i][j] = 2;
                  break;	
               case 'G': case 'g':
                  eds[i][j] = 3;
                  break;	
               case 'T': case 't':
                  eds[i][j] = 4;
                  break;			
               default: //base not acceptable? bummer...
                  System.out.println("Error:: Domain " + dn[i] + " base \"" + base + "\" not an acceptable base.");
                  System.out.println("Error:: Forcing Stop ");
                  System.exit(0);
                  break;
            }
            j++;
         }
      }
   }
   
   //***********************************************
   //Method for importing domains from a passed file
   //***********************************************
   
   public void readDomains(String ParametersFilePath) throws Exception
   {
      FileReader filereader = new FileReader(ParametersFilePath);   
      StreamTokenizer streamtokenizer = new StreamTokenizer(filereader);

	  streamtokenizer.nextToken();
      for(int i =0; streamtokenizer.ttype != StreamTokenizer.TT_EOF && i < mnd; i++) // for each line of input file, until end of file
      {
         streamtokenizer.nextToken(); // Read the domain name
         dn[i] = streamtokenizer.sval;

         streamtokenizer.nextToken(); // Read the domain type
         dt[i] = streamtokenizer.sval;

         if(dt[i].equals("vs") || dt[i].equals("Vs") || dt[i].equals("vS") || dt[i].equals("VS"))  // if domain type is variable-seeded
         {
            int tempL = 0;
            for(int j = 0; j < nbt; j++)
            {
               streamtokenizer.nextToken(); // Read base numbers
               dbn[i][j] = (int)streamtokenizer.nval;
               tempL = tempL + dbn[i][j];
            }
            if (tempL == 1) throw new RuntimeException("Domain "+dn[i]+" cannot have a single base and be variable.");
            nvd++; // increase variable domain counter.
         }
         
         if(dt[i].equals("v") || dt[i].equals("V") )  // if domain type is variable
         {
            streamtokenizer.nextToken(); // Read base sequence
            ds[i] = streamtokenizer.sval; 
            if (ds[i].length() == 1) throw new RuntimeException("Domain "+dn[i]+" cannot have a single base and be variable.");
            nvd++; // increase variable domain counter.
         }

         if(dt[i].equals("f") || dt[i].equals("F") ) // if domain type is fixed
         {
            streamtokenizer.nextToken(); // Read base sequence
            ds[i] = streamtokenizer.sval;         
         }
		tnd = i + 1 ;  // Set the total number of domains to be equal to i + 1
	   streamtokenizer.nextToken();
      }  
   }
   
   //***********************************
   //Method for seeding variable domains
   //***********************************
   
   public void seedVariableDomains()
   {
      for (int i = 0 ; i < tnd ; i++ )  // loop through domain indexes, ending at tnd (total number domains)
      {
         if(dt[i].equals("vs") || dt[i].equals("Vs") || dt[i].equals("vS") || dt[i].equals("VS")) // if domain type is variable seeded...
         {
            for(int j = 0; j < nbt; j++) // for each of the base types (nbt = number base types)
            {
               for(int k = 0; k < dbn[i][j]; k++) // seed bases . 
               {
                  if(j == 0)
                  {
                     if(ds[i] == null) ds[i] = "A";
                     else ds[i] = ds[i] + "A";
                  }

                  if(j == 1)
                  {
                     if(ds[i] == null ) ds[i] = "C";
                     else ds[i] = ds[i] + "C";
                  }

                  if(j == 2)
                  {
                     if(ds[i] == null) ds[i] = "G";
                     else ds[i] = ds[i] + "G";
                  }

                  if(j == 3)
                  {
                     if(ds[i] == null) ds[i] = "T";
                     else ds[i] = ds[i] + "T";
                  }
               }
            }
         }
      }
   }
   
   //***************************************
   //Method for printing Domain Base Numbers
   //***************************************
   
   public void printBaseNumbers()
   {
      System.out.println("Printing domain base numbers (dbn[i][j])");
      for(int i=0; i < tnd; i++)
      {
         for (int j=0; j < nbt ; j++) // iterate through number of base types (4; A,C,G,T)
         {
            System.out.println("dbn[" + i + "][" + j +"] = " + dbn[i][j] );
         }
      }
      System.out.println();
   }
   
   //********************************
   //Method for printing Domain Names
   //********************************
   
   public void printNames()
   {
      System.out.println("Printing domain names (dn[i])");
      for(int i=0; i < tnd; i++) // iterate through all domains.
      {
         System.out.println("dn[" + i + "] = " + dn[i] ); 
      }
      System.out.println();
   }

   //********************************
   //Method for printing Domain Types
   //********************************
   
   public void printTypes()
   {
      System.out.println("Printing domain types (dt[i])");
      for(int i=0; i < tnd; i++) // iterate through all domains.
      {
         System.out.println("dt[" + i + "] = " + dt[i] );
      }
      System.out.println();
   }
   
   //************************************
   //Method for printing domain sequences
   //************************************
   
   public void printSequences()
   {
      System.out.println("Printing domain sequences (ds[i])");
      for(int i=0; i < tnd; i++) // iterate through all domains.
      {
         System.out.println("ds[" + i + "] = " + ds[i] );
      }
      System.out.println();
   }
   
   
   //********************************************
   //Method for printing encoded domain sequences
   //********************************************
   
   public void printEncodedSequences()
   {
      System.out.println("Printing encoded domain sequences (eds[i])");
      for (int i = 0;  i < mnd && dn[i] != null; i ++ ) // iterate through all domains.
      {
         System.out.print("eds[" + i + "] = ");
         for(int j  = 0; j < dl[i] ; j++ ) // iterate through all bases in the domain.
         {
            System.out.print( eds[i][j] );
         }
         System.out.println();
      }
      System.out.println();
   }
   
   public static void setIDSFilePath( String IIDSFilePath )
   {
      IDSFilePath = IIDSFilePath;   
   }
   
   public static void importSettings (String IFilePath) throws Exception
   {
   
      MTout.log("Importing Settings for DomainPool() module from "+ IFilePath );
      
      // ************************************
      // Set default value for local settings
      // ************************************
      
      IDSFilePath = "DomainSequences.txt"; // DI: Domains-In
      
      FileReader filereader = new FileReader(IFilePath);   
      StreamTokenizer streamtokenizer = new StreamTokenizer(filereader);
      
      while( streamtokenizer.ttype != StreamTokenizer.TT_EOF ) // for the entire "input" file
      {
         streamtokenizer.nextToken(); 
         if (streamtokenizer.ttype == StreamTokenizer.TT_WORD)
         {
            if (streamtokenizer.sval.equalsIgnoreCase("idsfilepath"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  IDSFilePath = streamtokenizer.sval;
                  System.out.println("IDSFilePath value imported. Accepted value: "+ IDSFilePath);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"IDSFilePath\" in " + IFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"IDSFilePath\" in " + IFilePath); 
                  System.exit(0);                    
               }
            }
         }
      }
   }
   
   public String[] getDT()
   {
      return dt;
   }
   
   public int[][] getEDS()
   {
      return eds;
   }
   
   public int getNVD()
   {
      return nvd;
   }
}
