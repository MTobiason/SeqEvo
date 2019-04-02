package BoiseState;
import java.io.* ;
import java.util.Vector;

public class WCComplement
{

	private static MTlogger MTout = new MTlogger("WCComplement.java",3);

	private int S1; // index of first strand involved in the Complement
	private int S2; // index of second strand involved in the Complement

	private int S1B1; // Strand-1 Base 1 (base index of 5' most base of Complement)
	private int S2B1; // Strand-2 Base 1 (base index of 5' most base in Complement)

	private int L; // Length - Number of bases within Complement

	public WCComplement() // Create a blank Complement
	{ 
		S1 = new Integer(0); 
		S2 = new Integer(0);

		S1B1 = new Integer(0);
		S2B1 = new Integer(0);

		L = new Integer(0);
	}
   
   public WCComplement(int IS1, int IS1B1, int IS2, int IS2B1, int IL) // make a Complement from the relevant information
   {
      S1 = new Integer(IS1); 
		S2 = new Integer(IS2);

		S1B1 = new Integer(IS1B1);
		S2B1 = new Integer(IS2B1);

      L = new Integer(IL);
   }
   
	public static int getS1( WCComplement I )
	{
		return I.S1;
	}
	
	public static int getS2( WCComplement I )
	{
		return I.S2;
	}

	public static int getS1B1( WCComplement I )
	{
		return I.S1B1;
	}

	public static int getS2B1( WCComplement I )
	{
		return I.S2B1;
	}
	
	public static int getLength( WCComplement I )
	{
		return I.L;
	}
	
	public static void printComplementInformation( WCComplement I ) 
	{
		System.out.println("Complement Information:");
		System.out.println("S1 = " + getS1(I)); 
		System.out.println("S2 = " + getS2(I)); 

		System.out.println("S1B1 = " + getS1B1(I)); 
		System.out.println("S2B1 = " + getS2B1(I));  
		
		System.out.println("Length = " + getLength(I)); 		
	}
	
   
}