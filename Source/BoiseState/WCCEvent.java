package BoiseState;
import java.io.* ;
import java.util.Vector;

public class WCCEvent
{

	private static MTlogger MTout = new MTlogger("WCCEvent.java",3);

	public int S1; // index of first strand involved in the complement
	public int S2; // index of second strand involved in the complement

	public int S1B1; // Strand-1 Base 1 (base index of 5' most base of complement)
	public int S1B2; // Strand-1 Base 2 (base index of 3' most base of complement)
	public int S2B1; // Strand-2 Base 1 (base index of 5' most base of complement)
	public int S2B2; // Strand-2 Base 2 (base index of 3' most base of complement)

	public int Length;
	public String Sequence1; //5' to 3' sequence of the bases on strand 1
	public String Sequence2; //3' to 5' sequence of the bases on strand 2
	
	public boolean IsHairpin;
	public int HingeLength;


	// ***************************************
	// Method to create a new Complement Event
	// ***************************************

	public WCCEvent()
	{ 
		S1 = new Integer(0); 
		S2 = new Integer(0);

		S1B1 = new Integer(0);
		S1B2 = new Integer(0);
		S2B1 = new Integer(0);
		S2B2 = new Integer(0);

		Length = new Integer(0);
		Sequence1= new String("");
		Sequence2 = new String("");
		
		IsHairpin = new Boolean(false);
		HingeLength = new Integer(0);
	}
   
   // **********************************
   // Method to clone a complement event
   // **********************************
   
	public WCCEvent( WCCEvent IE)  // IE: Incoming-Event
	{
		S1 = new Integer( getS1(IE) ); 
		S2 = new Integer( getS2(IE) );

		S1B1 = new Integer( getS1B1(IE) );
		S1B2 = new Integer( getS1B2(IE) );
		S2B1 = new Integer( getS2B1(IE) );
		S2B2 = new Integer( getS2B2(IE) );

		Length = new Integer( getLength(IE) );
		Sequence1= new String( getSequence1(IE) );
		Sequence2 = new String( getSequence2(IE) );
		
		IsHairpin = new Boolean(getIsHairpin(IE));
		HingeLength = new Integer( getHingeLength(IE) );
	}

	public static int getS1( WCCEvent IE )
	{
		return IE.S1;
	}
	
	public static int getS2( WCCEvent IE )
	{
		return IE.S2;
	}

	public static int getS1B1( WCCEvent IE )
	{
		return IE.S1B1;
	}

	public static int getS1B2( WCCEvent IE )
	{
		return IE.S1B2;
	}

	public static int getS2B1( WCCEvent IE )
	{
		return IE.S2B1;
	}
	
	public static int getS2B2( WCCEvent IE )
	{
		return IE.S2B2;
	}
	
	public static int getLength( WCCEvent IE )
	{
		return IE.Length;
	}
	
	public static String getSequence1( WCCEvent IE )
	{
		return IE.Sequence1;
	}
	
	public static String getSequence2( WCCEvent IE )
	{
		return IE.Sequence2;
	}
	
	public static boolean getIsHairpin( WCCEvent IE )
	{
		return IE.IsHairpin;
	}
	
	public static int getHingeLength( WCCEvent IE )
	{
		return IE.HingeLength;
	}
	
	public static void printEvent( WCCEvent IE ) //IE: Incoming-Event
	{
		System.out.println("Event Details:");
		System.out.println("S1 = " + getS1(IE)); 
		System.out.println("S2 = " + getS2(IE)); 

		System.out.println("S1B1 = " + getS1B1(IE)); 
		System.out.println("S1B2 = " + getS1B2(IE)); 
		System.out.println("S2B1 = " + getS2B1(IE)); 
		System.out.println("S2B2 = " + getS2B2(IE)); 
		
		System.out.println("Length = " + getLength(IE)); 
		System.out.println("Sequence 1 = " + getSequence1(IE));
		System.out.println("Sequence 2 = " + getSequence2(IE));
		
		System.out.println("IsHarpin = " + getIsHairpin(IE) );
		System.out.println("HingeLength = " + getHingeLength(IE));
		
	}
	
	public static void setS1( WCCEvent IE, int IV ) //IE: Incoming-Event IV: Incoming-Value
	{
		IE.S1 = new Integer( IV );
	}
	
	public static void setS2( WCCEvent IE, int IV ) //IE: Incoming-Event IV: Incoming-Value
	{
		IE.S2 = new Integer( IV );
	}
	
	public static void setS1B1( WCCEvent IE, int IV ) //IE: Incoming-Event IV: Incoming-Value
	{
		IE.S1B1 = new Integer( IV );
	}
	
	public static void setS1B2( WCCEvent IE, int IV ) //IE: Incoming-Event IV: Incoming-Value
	{
		IE.S1B2 = new Integer( IV );
	}
	
	public static void setS2B1( WCCEvent IE, int IV ) //IE: Incoming-Event IV: Incoming-Value
	{
		IE.S2B1 = new Integer( IV );
	}
	
	public static void setS2B2( WCCEvent IE, int IV ) //IE: Incoming-Event IV: Incoming-Value
	{
		IE.S2B2 = new Integer( IV );
	}
	
	public static void setLength( WCCEvent IE, int IV ) //IE: Incoming-Event IV: Incoming-Value
	{
		IE.Length = new Integer( IV );
	}
	
	public static void setSequence1(WCCEvent IE, String IS) // IE: Incoming-Event, IS: Incoming-String
	{
		IE.Sequence1 = new String( IS );
	}
	
	public static void setSequence2(WCCEvent IE, String IS) // IE: Incoming-Event, IS: Incoming-String
	{
		IE.Sequence2 = new String( IS );
	}
	
	public static void setIsHairpin( WCCEvent IE, boolean IV) // IE: Incoming-Event, IS: Incoming-String
	{
		IE.IsHairpin = new Boolean(IV);
	}
	
	public static void setHingeLength( WCCEvent IE, int IV) // IE: Incoming-Event, IS: Incoming-String
	{
		IE.HingeLength = new Integer(IV);
	}
	
	public static void reset(WCCEvent IE)
	{
		IE.S1 = 0; 
		IE.S2 = 0;

		IE.S1B1 = 0;
		IE.S1B2 = 0;
		IE.S2B1 = 0;
		IE.S2B2 = 0;

		IE.Length = 0;
		IE.Sequence1= "";
		IE.Sequence2 = "";
		
		IE.IsHairpin = false;
		IE.HingeLength = 0;
	}
}