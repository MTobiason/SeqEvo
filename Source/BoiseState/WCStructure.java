package BoiseState;
import java.io.* ;
import java.util.Vector;

public class WCStructure
{

	private static MTlogger MTout = new MTlogger("WCStructure.java",3);

	private int S1; // index of first strand involved in the Complement
	private int S2; // index of second strand involved in the Complement
   private boolean isIntramolecular; 
	private int S1B1; // Strand-1 Base 1 (base index of 5' most base of Complement)
	private int S2B1; // Strand-2 Base 1 (base index of 5' most base in Complement)

	private int L; // Length - Number of bases within Complement

	public WCStructure() // Create a blank Complement
	{ 
		S1 = 0; 
		S2 = 0;
      isIntramolecular = false;
		S1B1 = 0;
		S2B1 = 0;

		L = 0;
	}
   
   public WCStructure(int IS1, int IS1B1, int IS2, int IS2B1, int IL, boolean III) // make a Complement from the relevant information
   {
      S1 = IS1; 
		S2 = IS2;
      isIntramolecular = III;
		S1B1 = IS1B1;
		S2B1 = IS2B1;

      L = IL;
   }
   
   public WCStructure ( WCStructure IS)
   {
      S1 = getS1(IS); 
		S2 = getS2(IS);
      isIntramolecular = new Boolean ( getIsIntramolecular(IS));
		S1B1 = getS1B1(IS);
		S2B1 = getS2B1(IS);

      L = getLength(IS);
   }
   
	public static int getS1( WCStructure I )
	{
		return I.S1;
	}
	
	public static int getS2( WCStructure I )
	{
		return I.S2;
	}

	public static int getS1B1( WCStructure I )
	{
		return I.S1B1;
	}

	public static int getS2B1( WCStructure I )
	{
		return I.S2B1;
	}
	
	public static int getLength( WCStructure I )
	{
		return I.L;
	}
   
   public static boolean getIsIntramolecular (WCStructure I)
   {
      return I.isIntramolecular;
   }
	
	public static void printStructureInfo( WCStructure I ) 
	{
		System.out.println("Structure Information:");
      System.out.println("Complement Length = " + getLength(I)); 	
		System.out.println("1st Strand = " + getS1(I)); 
		System.out.println("2nd Strand = " + getS2(I)); 

		System.out.println("1st Strand 1st (5' most) Base = " + getS1B1(I)); 
		System.out.println("2nd Strand 1st (5' most) Base = " + getS2B1(I));  
      System.out.println();
	}
	
   public static boolean isImplementation( WCStructure ISA, WCStructure ISB)
   {
      if ( getS1(ISB)==(getS1(ISA)) && getS2(ISB)==(getS2(ISA)) && getS1B1(ISB)==(getS1B1(ISA)) && getS2B1(ISB)==(getS2B1(ISA)) && getLength(ISB)==(getLength(ISA)) && getIsIntramolecular(ISB)==( getIsIntramolecular(ISA)))
      {
         return true;
      }
      else return false;
   }
   
   public static String getSequence1( WCStructure IS, int[][] IESS) //incoming-structure
   {
      String tempString = "";
      for(int i = 0; i < getLength(IS); i++)
      {
         tempString = tempString + decodeBase( IESS[getS1(IS)][getS1B1(IS)+i]);
      };
      return tempString;
   };
   
   public static String getSequence2( WCStructure IS, int[][] IESS) //incoming-structure
   {
      String tempString = "";
      for(int i = 0; i < getLength(IS); i++)
      {
         tempString = tempString + decodeBase( IESS[getS2(IS)][getS2B1(IS)+i]);
      };
      return tempString;
   }
   
   private static char decodeBase( int IB)
   {
      char tempChar = 'X';
      switch(IB)
      {
         case 1:
            tempChar = 'A';
            break;
         case 2:
            tempChar = 'C';
            break;
         case 3:
            tempChar = 'G';
            break;
         case 4:
            tempChar = 'T';
            break;
      }
      
      return tempChar;
   }
   
}