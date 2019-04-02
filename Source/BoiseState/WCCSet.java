package BoiseState;
import java.io.* ;
import java.util.Vector;

public class WCCSet
{

	private static MTlogger MTout = new MTlogger("WCCSet.java",3);
	private static int IntramolecularSLC;
	private static int IntermolecularSLC;

	private Vector<WCCEvent> EventList;
	
	public WCCSet()
	{ 
		EventList = new Vector<WCCEvent>();
	}
	
	static public void importSettings ( String ParametersFilePath ) throws Exception
   {
      //MTout.log("Importing Settings for Scoring() module from "+ ParametersFilePath );
      
      // *******************
      // Load default values
      // *******************
        
      IntramolecularSLC = 2;
      IntermolecularSLC = 2;
      
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
            if (streamtokenizer.sval.equalsIgnoreCase("IntramolecularSLC"))
            {
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 1)
               {
                  IntramolecularSLC = (int)streamtokenizer.nval;
                  System.out.println("IntramolecularSLC value imported. Accepted value: " + IntramolecularSLC);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"IntramolecularSLC\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"IntramolecularSLC\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("IntermolecularSLC"))
            {
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 1)
               {
                  IntermolecularSLC = (int)streamtokenizer.nval;
                  System.out.println("IntermolecularSLC value imported. Accepted value: " + IntermolecularSLC);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"IntermolecularSLC\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"IntermolecularSLC\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            
         }
      }
   }
	
	public static void addEvent (WCCSet IS, WCCEvent IE ) //IE:Incoming-Event
	{
			IS.EventList.add(IE);
	}
	
	
	public Vector<WCCEvent> getEventList() // IS:Incoming-Set
	{
		return EventList;
	}
	
	
	public static void setEventList (WCCSet IS, Vector<WCCEvent> IEL) // IS: Incoming-Set IEL: Incoming Event List
	{
		IS.EventList = IEL; // This line does not achieve a deep copy, future changes to IEL will effect IS.EventList
	}
   
	public static WCCSet getIntramolecularWCCSet( Generation IG ) // IG: Incoming-Generation, IS: Incoming-Set
	{
		WCCSet TempWCCSet = new WCCSet();

		// ************************************
		// Count Unfavorable Stretches of Bases 
		// Generate Occurrence list for 
		// ************************************

		int tns = IG.getTNS(); //get total number of strands
		int ess[][] = IG.getESS(); //get encoded strand sequences
		int sl[] = IG.getSL();  // get strand lengths
		int nvb = IG.getNVB(); //get number of variable bases
		int IntramolecularCounter = 0; // counter for consecutive bases between strand 1,2 (including a strand and a copy of itself)
         
		String TempSequence1 =  new String("");
		String TempSequence2 = new String("");	

		// ***********************************
		// Find stretches of complements
		// ***********************************

		for( int i = 0; i < tns; i++ ) // for all strands in the system,
		{
         
         // *************************************
         // Search for Intramolecular complements
         // (Search for Hairpin-like complements)
         // *************************************
      		
			for (int RP = 1; RP < sl[i]; RP++) // for all reference positions, (move bottom strand for each reference position) 
			{
				 
				//search for all events in this alignment (No base in bulge)
				 
				for( int j = RP - Math.min( sl[i] - RP - 1, RP-1 ); j <= RP ; j++) // for each base in the overlap range 
				{
		   
				   if ( ess[ i ][j - 1 ] + ess[ i ][ 2*RP - j ] == 5 || ess[ i ][j - 1 ] + ess[ i ][ 2*RP - j +1 - 1 ] == (2*nvb +11) ) // if the bases in the anti-aligned areas are complementary ...
				   {
					  IntramolecularCounter++; //increase the current run by one.
					  TempSequence1 = TempSequence1 + Integer.toString( ess[ i ] [j-1] );
					  TempSequence2 = TempSequence2 + Integer.toString ( ess[ i ][ 2*RP - j ]);
					  
				   }
				   else // if the bases are not a complement
				   {
					  if (IntramolecularCounter >= IntramolecularSLC ) // if the stretch is long enough to count
					  {
						 WCCEvent TempEvent = new WCCEvent();
						 WCCEvent.setS1( TempEvent, i );
						 WCCEvent.setS2( TempEvent, i );
						 WCCEvent.setS1B1( TempEvent, j - 1 - IntramolecularCounter); //5' most base of complement 1
						 WCCEvent.setS1B2( TempEvent, j - 2 ); // 3' most base of complement 1
						 WCCEvent.setS2B1( TempEvent, 2*RP - j + 1 ); //5' most base of complement 2
						 WCCEvent.setS2B2( TempEvent, 2*RP - j + IntramolecularCounter ); // 3' most base of complement 2
						 WCCEvent.setSequence1( TempEvent, TempSequence1);
						 WCCEvent.setSequence2( TempEvent, TempSequence2);
						 WCCEvent.setLength( TempEvent, IntramolecularCounter); 
						 WCCEvent.setIsHairpin( TempEvent, true );
						 WCCEvent.setHingeLength( TempEvent, (2*RP - j - 1 ) - (j - 2) - 1 );
						 
						 WCCSet.addEvent( TempWCCSet, TempEvent );
						 
					  }
					  IntramolecularCounter = 0; // reset the current run.
					  TempSequence1 = "";
					  TempSequence2 = "";
					}
				}
				if (IntramolecularCounter>= IntramolecularSLC) // if the search ended on a long enough run, record it.
				{
					WCCEvent TempEvent = new WCCEvent();
					WCCEvent.setS1( TempEvent, i );
					WCCEvent.setS2( TempEvent, i );
					WCCEvent.setS1B1( TempEvent, RP - 0 - IntramolecularCounter); //5' most base of complement 1
					WCCEvent.setS1B2( TempEvent, RP - 1 ); // 3' most base of complement 1
					WCCEvent.setS2B1( TempEvent, RP - 0 ); //5' most base of complement 2
					WCCEvent.setS2B2( TempEvent, RP - 1 + IntramolecularCounter ); // 3' most base of complement 2
					WCCEvent.setSequence1( TempEvent, TempSequence1);
					WCCEvent.setSequence2( TempEvent, TempSequence2);
					WCCEvent.setLength( TempEvent, IntramolecularCounter); 
					WCCEvent.setIsHairpin( TempEvent, true );
					WCCEvent.setHingeLength( TempEvent, 0 );

					WCCSet.addEvent( TempWCCSet, TempEvent );
				}
				IntramolecularCounter = 0; // reset the current run.
				TempSequence1 = "";
				TempSequence2 = "";
				
				//Slide the top strand by one (leaving one base in the bulge) and search for events.
				
				if ( (RP + 1) < sl[i] )
				{
				   for( int j = RP - Math.min( sl[i] - RP - 2, RP-1 ); j <= RP ; j++) // for each base in the overlap range 
				   {
					  if ( ess[ i ][j-1] + ess[ i ][ 2*RP - j  +1 ] == 5 || ess[ i ][j-1] + ess[ i ][ 2*RP - j  +1 ] == (2*nvb + 11)) // if the bases in the anti-aligned areas are complementary ...
					  {
						IntramolecularCounter++; //increase the current run by one.
						TempSequence1 = TempSequence1 + Integer.toString( ess[ i ] [j-1] );
						TempSequence2 = TempSequence2 + Integer.toString( ess[ i ][ 2*RP - j + 1 ]);
					  }
					  else // if the bases are not a complement
					  {
						if (IntramolecularCounter >= IntramolecularSLC ) // if the stretch is long enough to count
						{
							WCCEvent TempEvent = new WCCEvent();
							WCCEvent.setS1( TempEvent, i );
							WCCEvent.setS2( TempEvent, i );
							WCCEvent.setS1B1( TempEvent, j - 1 - IntramolecularCounter); //5' most base of complement 1
							WCCEvent.setS1B2( TempEvent, j - 2 ); // 3' most base of complement 1
							WCCEvent.setS2B1( TempEvent, 2*RP - j  ); //5' most base of complement 2
							WCCEvent.setS2B2( TempEvent, 2*RP -1 - j  + IntramolecularCounter ); // 3' most base of complement 2
							WCCEvent.setSequence1( TempEvent, TempSequence1);
							WCCEvent.setSequence2( TempEvent, TempSequence2);
							WCCEvent.setLength( TempEvent, IntramolecularCounter); 
							WCCEvent.setIsHairpin( TempEvent, true );
							WCCEvent.setHingeLength( TempEvent, 2*RP - 2*j + 1 );
							 
							WCCSet.addEvent( TempWCCSet, TempEvent );
						}
						IntramolecularCounter = 0; // reset the current run.
						TempSequence1 = "";
						TempSequence2 = "";
					  }
				   }
				   if (IntramolecularCounter>= IntramolecularSLC) // if the search ended on a long enough run, record it.
				   {
						WCCEvent TempEvent = new WCCEvent();
						WCCEvent.setS1( TempEvent, i );
						WCCEvent.setS2( TempEvent, i );
						WCCEvent.setS1B1( TempEvent, RP - 1 - IntramolecularCounter); //5' most base of complement 1
						WCCEvent.setS1B2( TempEvent, RP - 1 ); // 3' most base of complement 1
						WCCEvent.setS2B1( TempEvent, RP + 1 ); //5' most base of complement 2
						WCCEvent.setS2B2( TempEvent, RP + IntramolecularCounter ); // 3' most base of complement 2
						WCCEvent.setSequence1( TempEvent, TempSequence1);
						WCCEvent.setSequence2( TempEvent, TempSequence2);
						WCCEvent.setLength( TempEvent, IntramolecularCounter); 
						WCCEvent.setIsHairpin( TempEvent, true );
						WCCEvent.setHingeLength( TempEvent, 1 );
						 
						WCCSet.addEvent( TempWCCSet, TempEvent );
					}
					IntramolecularCounter = 0; // reset the current run.
					TempSequence1 = "";
					TempSequence2 = "";
				}
			}
		}
	return TempWCCSet;
	}
	
	public static WCCSet getIntermolecularWCCSet( Generation IG ) // IG: Incoming-Generation, IS: Incoming-Set
	{
		WCCSet TempWCCSet = new WCCSet();

		// ************************************
		// Count Unfavorable Stretches of Bases 
		// Generate Occurrence list for 
		// ************************************

		int tns = IG.getTNS(); //get total number of strands
		int ess[][] = IG.getESS(); //get encoded strand sequences
		int sl[] = IG.getSL();  // get strand lengths
		int nvb = IG.getNVB(); //get number of variable bases
		int IntermolecularCounter = 0; // counter for consecutive bases between strand 1,2 (including a strand and a copy of itself)
         
		String TempSequence1 =  new String("");
		String TempSequence2 = new String("");	
		
		for( int i = 0; i < tns; i++ ) // for all strands in the system,
		{ 
		 for (int j = i; j < tns; j++) //for all sets of strands that have not yet been considered
		 {
			for (int rp = 0; rp < sl[i] + sl[j] - 1; rp++) // for all reference positions, 
			{
			   for( int k = Math.max( 0 , rp + 1  - sl[j] ); k < Math.min( rp + 1 , sl[i]); k++) // for the overlap range 
			   {
				  // *********************************************************
				  // Score strand1 vs strand1 in the anti-parallel orientation
				  // *********************************************************
				  
				  if ( ess[ i ][k] + ess[ j ][rp-k] == 5 ||  ess[ i ][k] + ess[ j ][rp-k] == (2*nvb +11)) // if the bases on anti-parallel aligned strand 1 and 2 are complementary ...
				  {
					IntermolecularCounter ++; //increase the current run by one.
					TempSequence1 = TempSequence1 + Integer.toString( ess[ i ] [k] );
					TempSequence2 = TempSequence2 + Integer.toString( ess[ j ][ rp-k]);
				  }
				  else
				  {
					 if (IntermolecularCounter >= IntermolecularSLC ) 
					 {
						WCCEvent TempEvent = new WCCEvent();
						WCCEvent.setS1( TempEvent, i );
						WCCEvent.setS2( TempEvent, j );
						WCCEvent.setS1B1( TempEvent, k - IntermolecularCounter); //5' most base of complement 1
						WCCEvent.setS1B2( TempEvent, k - 1 ); // 3' most base of complement 1
						WCCEvent.setS2B1( TempEvent, rp - k + 1  ); //5' most base of complement 2
						WCCEvent.setS2B2( TempEvent, rp - k + IntermolecularCounter ); // 3' most base of complement 2
						WCCEvent.setSequence1( TempEvent, TempSequence1);
						WCCEvent.setSequence2( TempEvent, TempSequence2);
						WCCEvent.setLength( TempEvent, IntermolecularCounter); 
						WCCEvent.setIsHairpin( TempEvent, false );
						 
						WCCSet.addEvent( TempWCCSet, TempEvent );
					 }
					 IntermolecularCounter = 0; // reset the current run.
					 TempSequence1 = "";
					 TempSequence2 = "";
				  }
			   }
			   
			   // ************************
			   // score any remaining runs
			   // ************************
			   
			   if ( IntermolecularCounter >= IntermolecularSLC ) 
			   {
					int k = Math.min( rp + 1 , sl[i]);
					WCCEvent TempEvent = new WCCEvent();
					WCCEvent.setS1( TempEvent, i );
					WCCEvent.setS2( TempEvent, j );
					WCCEvent.setS1B1( TempEvent, k +1 - IntermolecularCounter); //5' most base of complement 1
					WCCEvent.setS1B2( TempEvent, k  ); // 3' most base of complement 1
					WCCEvent.setS2B1( TempEvent, rp - k  ); //5' most base of complement 2
					WCCEvent.setS2B2( TempEvent, rp - k -1 + IntermolecularCounter ); // 3' most base of complement 2
					WCCEvent.setSequence1( TempEvent, TempSequence1);
					WCCEvent.setSequence2( TempEvent, TempSequence2);
					WCCEvent.setLength( TempEvent, IntermolecularCounter); 
					WCCEvent.setIsHairpin( TempEvent, false );
			   }
			   IntermolecularCounter=0; // reset run counter
			   TempSequence1 = "";
			   TempSequence2 = "";
			}
		 }
		}
		return TempWCCSet;
	}
}
/*
*******************
OLD CODE BELOW HERE
*******************
old code below here        
         // *************************************
         // Search for Intermolecular complements
         // *************************************
      
         int IntermolecularCounter = 0; // counter for consecutive bases between strand 1,2 (including a strand and a copy of itself)
         
         for (int j = i; j < tns; j++) //for all sets of strands that have not yet been considered
         {
            for (int rp = 0; rp < sl[i] + sl[j] - 1; rp++) // for all reference positions, 
            {
               for( int k = Math.max( 0 , rp + 1  - sl[j] ); k < Math.min( rp + 1 , sl[i]); k++) // for the overlap range 
               {
                  // *********************************************************
                  // Score strand1 vs strand1 in the anti-parallel orientation
                  // *********************************************************
                  
                  if ( ess[ i ][k] + ess[ j ][rp-k] == 5 ||  ess[ i ][k] + ess[ j ][rp-k] == (2*nvb +11)) // if the bases on anti-parallel aligned strand 1 and 2 are complementary ...
                  {
                     IntermolecularCounter ++; //increase the current run by one.
                  }
                  else
                  {
                     if (IntermolecularCounter >= IntermolecularSLC ) 
                     {
                        addStretch( IntermolecularProfile, IntermolecularCounter);
                     }
                     IntermolecularCounter = 0; // reset the current run.
                  }
               }
               
               // ************************
               // score any remaining runs
               // ************************
               
               if ( IntermolecularCounter >= IntermolecularSLC ) addStretch( IntermolecularProfile, IntermolecularCounter );
               IntermolecularCounter=0; // reset run counter
            }
         }
      }
      if (ProfileCompleteness.equals("Complete"))
      {
         AAStretches = makeComplete( AAStretches, AAslc);
         TTStretches = makeComplete( TTStretches, TTslc);
         CCStretches = makeComplete( CCStretches, CCslc);
         GGStretches = makeComplete( GGStretches, GGslc);
         IntramolecularProfile = makeComplete( IntramolecularProfile, IntramolecularSLC);
         IntermolecularProfile = makeComplete( IntermolecularProfile, IntermolecularSLC);
      }
   }
	

*******************
OLD CODE BELOW HERE
*******************
 
   
   public void generateDetailedOccurrences ( Generation IG ) //Generate Detailed occurence list, containing both intra/intermolecular complements and iso-base stretches. sub-complements and sub-stretches are not counted. 
   {
      
      // **********************************
      // Clear previously found occurrences
      // **********************************
      
      IntramolecularProfile.clear();
      IntermolecularProfile.clear();
      
      AAStretches.clear();
      CCStretches.clear();
      GGStretches.clear();
      TTStretches.clear();
      
      // ************************************
      // Count Unfavorable Stretches of Bases 
      // Generate Occurrence list for 
      // ************************************
      
      int tns = IG.getTNS(); //get total number of strands
      int ess[][] = IG.getESS(); //get encoded strand sequences
      int sl[] = IG.getSL();  // get strand lengths
      int nvb = IG.getNVB(); //get number of variable bases
      
      for(int i = 0; i < tns; i++) // iterate through all strands.
      {
         int Cc = 0; // number of c's in a row counter
         int Gc = 0; // number of g's in a row counter
         int Ac = 0; // number of a's in a row counter
         int Tc = 0; // number of t's in a row counter
         
         int b0 = ess[i][0]; //set the first case
         int b1 = 0;

         switch(b0) //start the first run
         {
            case 1: // if the coded sequence = 1 = A
               Ac = Ac + 1;
               break;
            case 2: // if the coded sequence = 2 = C
               Cc = Cc + 1;
               break;
            case 3: // if the coded sequence = 3 = G
               Gc = Gc + 1;
               break;
            case 4: // if the coded sequence = 4 = T
               Tc = Tc + 1;
               break;
         }

         for(int j = 1; j <= sl[i]; j++) // for all bases j in the strand i (starting at base 1, not zero)
         {
            b0 = ess[i][j-1]; // set b0 to be the previous base
            
            if( j < sl[i]) // if b1 will be within the strand
            {
               b1 = ess[i][j]; // set b1 to be the current base
            } 
            else if (j == sl[i]) // if b1 will be beyond the strand
            {
               b1 = 0; // let b1 = 0 so the previous base and runs can be scored.
            }
            
            if(b1 == b0) // if this base is the same as the previous base.
            {
               switch(b0) //increment the proper counter.
               {
                  case 1:
                     Ac = Ac + 1;
                     break;
                  case 2:
                     Cc = Cc + 1;
                     break;
                  case 3:
                     Gc = Gc + 1;
                     break;
                  case 4:
                     Tc = Tc + 1;
                     break;
               }
            }
            
            if(b1 != b0) // if the two bases are not equal
            {
               switch(b0) // record the score from the consecutive base counter, reset counter
               {
                  case 1:
                     addStretch( AAStretches , Ac );
                     Ac = 0;
                     break;
                  case 2:
                     addStretch( CCStretches , Cc );
                     Cc = 0;
                     break;
                  case 3:
                     addStretch( GGStretches , Gc );
                     Gc = 0;
                     break;
                  case 4:
                     addStretch( TTStretches , Tc );
                     Tc = 0;
                     break;
               }
               switch(b1) //start the next run
               {
                  case 1: // if the coded sequence = 1 = A
                     Ac = Ac + 1;
                     break;
                  case 2: // if the coded sequence = 2 = C
                     Cc = Cc + 1; 
                     break;
                  case 3: // if the coded sequence = 3 = G
                     Gc = Gc + 1;
                     break;
                  case 4: // if the coded sequence = 4 = T
                     Tc = Tc + 1;
                     break;
                  case 0:
                     break;
               }
            }
         }
         
      }
      
      // ***********************************
      // Find unfit stretches of complements
      // ***********************************
      
      for( int i = 0; i < tns; i++ ) // for all strands in the system,
      {
         
         // *************************************
         // Search for Intramolecular complements
         // (Search for Hairpin-like complements)
         // *************************************
      
         int IntramolecularCounter = 0; // counter for consecutive hairpin-style complements
      
         for (int RP = 1; RP < sl[i]; RP++) // for all reference positions, (move bottom strand for each reference position) 
         {
            //System.out.println("Checking Reference Position # " + RP); 
            
            //search for all events in this alignment (No base in bulge)
            
            for( int j = RP - Math.min( sl[i] - RP - 1, RP-1 ); j <= RP ; j++) // for each base in the overlap range 
            {
       
               if ( ess[ i ][j - 1 ] + ess[ i ][ 2*RP - j +1 - 1 ] == 5 || ess[ i ][j - 1 ] + ess[ i ][ 2*RP - j +1 - 1 ] == (2*nvb +11) ) // if the bases in the anti-aligned areas are complementary ...
               {
                  IntramolecularCounter++; //increase the current run by one.
               }
               else // if the bases are not a complement
               {
                  if (IntramolecularCounter >= IntramolecularSLC ) // if the stretch is long enough to count
                  {
                     addStretch( IntramolecularProfile, IntramolecularCounter); //record the complement
                  }
                  IntramolecularCounter = 0; // reset the current run.
               }
            }
            if (IntramolecularCounter>= IntramolecularSLC) // if the search ended on a long enough run, record it.
            {
                addStretch( IntramolecularProfile, IntramolecularCounter);
            }
            IntramolecularCounter = 0; // reset the current run.
            
            //Slide the top strand by one (leaving one base in the bulge) and search for events.
            
            if ( (RP + 1) < sl[i] )
            {
               for( int j = RP - Math.min( sl[i] - RP - 2, RP-1 ); j <= RP ; j++) // for each base in the overlap range 
               {
                  if ( ess[ i ][j-1] + ess[ i ][ 2*RP - j  +1 ] == 5 || ess[ i ][j-1] + ess[ i ][ 2*RP - j  +1 ] == (2*nvb + 11)) // if the bases in the anti-aligned areas are complementary ...
                  {
                     IntramolecularCounter++; //increase the current run by one.
                  }
                  else // if the bases are not a complement
                  {
                     if (IntramolecularCounter >= IntramolecularSLC ) // if the stretch is long enough to count
                     {
                        addStretch( IntramolecularProfile, IntramolecularCounter); //record the complement
                     }
                     IntramolecularCounter = 0; // reset the current run.
                  }
               }
               if (IntramolecularCounter>= IntramolecularSLC) // if the search ended on a long enough run, record it.
               {
                   addStretch( IntramolecularProfile, IntramolecularCounter);
               }
               IntramolecularCounter = 0; // reset the current run.
            }
         }
         
         
         // *************************************
         // Search for Intermolecular complements
         // *************************************
      
         int IntermolecularCounter = 0; // counter for consecutive bases between strand 1,2 (including a strand and a copy of itself)
         
         for (int j = i; j < tns; j++) //for all sets of strands that have not yet been considered
         {
            for (int rp = 0; rp < sl[i] + sl[j] - 1; rp++) // for all reference positions, 
            {
               for( int k = Math.max( 0 , rp + 1  - sl[j] ); k < Math.min( rp + 1 , sl[i]); k++) // for the overlap range 
               {
                  // *********************************************************
                  // Score strand1 vs strand1 in the anti-parallel orientation
                  // *********************************************************
                  
                  if ( ess[ i ][k] + ess[ j ][rp-k] == 5 ||  ess[ i ][k] + ess[ j ][rp-k] == (2*nvb +11)) // if the bases on anti-parallel aligned strand 1 and 2 are complementary ...
                  {
                     IntermolecularCounter ++; //increase the current run by one.
                  }
                  else
                  {
                     if (IntermolecularCounter >= IntermolecularSLC ) 
                     {
                        addStretch( IntermolecularProfile, IntermolecularCounter);
                     }
                     IntermolecularCounter = 0; // reset the current run.
                  }
               }
               
               // ************************
               // score any remaining runs
               // ************************
               
               if ( IntermolecularCounter >= IntermolecularSLC ) addStretch( IntermolecularProfile, IntermolecularCounter );
               IntermolecularCounter=0; // reset run counter
            }
         }
      }
      if (ProfileCompleteness.equals("Complete"))
      {
         AAStretches = makeComplete( AAStretches, AAslc);
         TTStretches = makeComplete( TTStretches, TTslc);
         CCStretches = makeComplete( CCStretches, CCslc);
         GGStretches = makeComplete( GGStretches, GGslc);
         IntramolecularProfile = makeComplete( IntramolecularProfile, IntramolecularSLC);
         IntermolecularProfile = makeComplete( IntermolecularProfile, IntermolecularSLC);
      }
   }
   
   static public void importSettings ( String ParametersFilePath ) throws Exception
   {
      //MTout.log("Importing Settings for Scoring() module from "+ ParametersFilePath );
      
      // *******************
      // Load default values
      // *******************
        
      IntramolecularSLC = 2;
      IntermolecularSLC = 2;
      
      AAslc = 2;
      CCslc = 2;
      GGslc = 2;
      TTslc = 2;
   
      IntramolecularW = 1;
      IntermolecularW = 1;
      
      ScoringFunction = "WCI";
      ScoreType = "SLC";
      ProfileType = "Interference";
      ProfileCompleteness = "Complete";
      
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
            if (streamtokenizer.sval.equalsIgnoreCase("IntramolecularW"))
            {
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 0)
               {
                  IntramolecularW = (int)streamtokenizer.nval;
                  System.out.println("IntramolecularW value imported. Accepted value: " + IntramolecularW);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"IntramolecularW\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"IntramolecularW\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("IntermolecularW"))
            {
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 0)
               {
                  IntermolecularW = (int)streamtokenizer.nval;
                  System.out.println("IntermolecularW value imported. Accepted value: " + IntermolecularW);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"IntermolecularW\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"IntermolecularW\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("IntramolecularSLC"))
            {
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 1)
               {
                  IntramolecularSLC = (int)streamtokenizer.nval;
                  System.out.println("IntramolecularSLC value imported. Accepted value: " + IntramolecularSLC);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"IntramolecularSLC\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"IntramolecularSLC\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("IntermolecularSLC"))
            {
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 1)
               {
                  IntermolecularSLC = (int)streamtokenizer.nval;
                  System.out.println("IntermolecularSLC value imported. Accepted value: " + IntermolecularSLC);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"IntermolecularSLC\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"IntermolecularSLC\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("AAslc"))
            {
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 1)
               {
                  AAslc = (int)streamtokenizer.nval;
                  System.out.println("AAslc value imported. Accepted value: " + AAslc);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"AAslc\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"AAslc\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("CCslc"))
            {
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 1)
               {
                  CCslc = (int)streamtokenizer.nval;
                  System.out.println("CCslc value imported. Accepted value: " + CCslc);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"CCslc\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"CCslc\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("GGslc"))
            {
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 1)
               {
                  GGslc = (int)streamtokenizer.nval;
                  System.out.println("GGslc value imported. Accepted value: " + GGslc);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"GGslc\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"GGslc\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("TTslc"))
            {
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_NUMBER && (int)streamtokenizer.nval >= 1)
               {
                  TTslc = (int)streamtokenizer.nval;
                  System.out.println("TTslc value imported. Accepted value: " + TTslc);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"TTslc\" in " + ParametersFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"TTslc\" in " + ParametersFilePath); 
                  System.exit(0);            
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("ScoringFunction")) //Trajectory-Reporting
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  switch ( streamtokenizer.sval.toLowerCase() )
                  {
                     case "wcp" : 
                        ScoringFunction = "WCP";
                        ProfileType = "Profile";
                        ScoreType = "SLC";
                        System.out.println("ScoringFunction value imported. Accepted value: " + ScoringFunction);
                        break;
                     case "wci" :
                        ScoringFunction = "WCI";
                        ProfileType = "Interference";
                        ScoreType = "SLC";
                        System.out.println("ScoringFunction value imported. Accepted value: " + ScoringFunction);
                        break;
                     case "longestwci" :
                        ScoringFunction = "LongestWCI";
                        ProfileType = "Interference";
                        ScoreType = "Longest";
                        System.out.println("ScoringFunction value imported. Accepted value: " + ScoringFunction);
                        break;
                     default :
                        ScoringFunction = "LongestWCI";
                        ProfileType = "Interference";
                        ScoreType = "Longest";
                        System.out.println("ScoringFunction value invalid. Invalid value: " + streamtokenizer.sval.toLowerCase() + ". Using value: " + ScoringFunction);
                        break;
                  }
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("ProfileCompleteness")) //Trajectory-Reporting
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  switch ( streamtokenizer.sval.toLowerCase() )
                  {
                     case "longest" : 
                        ProfileCompleteness = "Longest";
                        System.out.println("ProfileCompleteness value imported. Accepted value: " + ProfileCompleteness);
                        break;
                     case "complete" :
                        ProfileCompleteness = "Complete";
                        System.out.println("ProfileCompleteness value imported. Accepted value: " + ProfileCompleteness);
                        break;
                     default :
                        ProfileCompleteness = "Longest";
                        System.out.println("ProfileCompleteness value invalid. Invalid value: " + streamtokenizer.sval.toLowerCase() + ". Using value: " + ProfileCompleteness);
                        break;
                  }
               }
            }
         }
      }
   }
   
   static public void addStretch( Vector<Integer[]> IV, Integer IL) //IV: Incoming-Vector, IL: Incoming-Length-Criteria
   {
      boolean FoundLength = false;

      
      if (IV.size() == 0)
      {
            Integer[] SeedingValue = new Integer[2];
            SeedingValue[0] = IL;
            SeedingValue[1] = 1;
            IV.add(SeedingValue);
      }
      else 
      {
         for (int i = 0 ; i < IV.size(); i++) // for all the integer pairs in IV
         {
            if ( IV.get(i)[0].equals( IL) ) // if the current element is the length we are looking for (IL), 
            {
               Integer[] TempA = {0,0};
               TempA[0] = IV.get(i)[0];
               TempA[1] = (IV.get(i)[1] + 1);
               IV.set( i , TempA);
               FoundLength = true; // flag that the length was found
            }
            
            if ( FoundLength == false && (IV.get(i)[0] > IL) ) // if we have not found the stretch, but the next element of the list is larger than IL
            {
               Integer[] TempB = {0,0};
               TempB[0] = IL;
               TempB[1] = 1;
               IV.add( i, TempB );
               FoundLength = true;
            }
         }
         if ( FoundLength == false ) // if the length is not found.
         { 
            // add that length at the end of the array.
            Integer[] TempC = {0,0};
            TempC[0] = IL;
            TempC[1] = 1;
            IV.add( TempC );
         }
      } 
   }
   
   static public void addStretches( Vector<Integer[]> IV, Integer IL, Integer IC) //IV: Incoming-Vector, IL: Incoming-Length, IC: Incoming-Count
   {
      boolean FoundLength = false;

      
      if (IV.size() == 0)
      {
            Integer[] SeedingValue = new Integer[2];
            SeedingValue[0] = IL; // Set the length
            SeedingValue[1] = IC; // Set the count
            IV.add(SeedingValue);
      }
      else 
      {
         for (int i = 0 ; i < IV.size(); i++) // for all the integer pairs in IV
         {
            if ( IV.get(i)[0].equals( IL) ) // if the current element is the length we are looking for (IL), 
            {
               Integer[] TempA = {0,0};
               TempA[0] = IV.get(i)[0]; //set the length
               TempA[1] = (IV.get(i)[1] + IC); //set the count
               IV.set( i , TempA);
               FoundLength = true; // flag that the length was found
            }
            
            if ( FoundLength == false && (IV.get(i)[0] > IL) ) // if we have not found the stretch, but the next element of the list is larger than IL
            {
               Integer[] TempB = {0,0};
               TempB[0] = IL;
               TempB[1] = IC;
               IV.add( i, TempB );
               FoundLength = true;
            }
         }
         if ( FoundLength == false ) // if the length is not found.
         { 
            // add that length at the end of the array.
            Integer[] TempC = {0,0};
            TempC[0] = IL;
            TempC[1] = IC;
            IV.add( TempC );
         }
      } 
   }
   
   public void printDetailedOccurrenceLists ()
   {
      if(AAStretches.size() != 0 )
	  {
			System.out.println("Stretches of Consecutive A's:");
			printOccurrenceList( AAStretches);
      } 
	  else {System.out.println("No Stretches of Consecutive A's");System.out.println();};
	  
	  if(CCStretches.size() != 0 )
	  {
      System.out.println("Stretches of Consecutive C's:");
      printOccurrenceList( CCStretches);
	  } 
	  else {System.out.println("No Stretches of Consecutive C's");System.out.println();};

	  if(GGStretches.size() != 0 )
	  {	  
      System.out.println("Stretches of Consecutive G's:");
      printOccurrenceList( GGStretches);
	  } 
	  else {System.out.println("No Stretches of Consecutive G's");System.out.println();};
	  
	  if(TTStretches.size() != 0 )
	  {	
      System.out.println("Stretches of Consecutive T's:");
      printOccurrenceList( TTStretches);
	  } 
	  else {System.out.println("No Stretches of Consecutive T's");System.out.println();};
	  
	  if(IntramolecularProfile.size() != 0 )
	  {	
      System.out.println("Stretches of Intramolecular Complements:");
      printOccurrenceList( IntramolecularProfile);
	  }
	  else {System.out.println("No Stretches of Intramolecular Complements");System.out.println();};
	  
	  if(IntermolecularProfile.size() != 0 )
	  {	
      System.out.println("Stretches of Intermolecular Complements:");
      printOccurrenceList( IntermolecularProfile);
	  }
	  else {System.out.println("No Stretches of Intermolecular Complements");System.out.println();};
	  
}
   
   public void printProfileLists()
   {
      if( IntramolecularProfile.size() != 0)
	  {
		System.out.println("Stretches of Intramolecular Complements:");
		printOccurrenceList( IntramolecularProfile);
	  } else {System.out.println("No Stretches of Intramolecular Complements");System.out.println();};
	  
	  if( IntermolecularProfile.size() != 0)
	  {
		System.out.println("Stretches of Intermolecular Complements:");
		printOccurrenceList( IntermolecularProfile);
	  } else {System.out.println("No Stretches of Intermolecular Complements");System.out.println();};
   }
   
   public void printCompleteProfileLists()
   {
      if( IntramolecularProfile.size() != 0)
	  {
		System.out.println("Stretches of Intramolecular Complements:");
		printOccurrenceList( makeComplete(IntramolecularProfile,IntramolecularSLC));
	  } else {System.out.println("No Stretches of Intramolecular Complements");System.out.println();};
	  
	  if( IntermolecularProfile.size() != 0)
	  {
		System.out.println("Stretches of Intermolecular Complements:");
		printOccurrenceList( makeComplete(IntermolecularProfile,IntermolecularSLC));
	  } else {System.out.println("No Stretches of Intermolecular Complements");System.out.println();};
   }
   
   static public void printOccurrenceList( Vector<Integer[]> IV ) // IV: Incoming-Vector
   {
         System.out.println("------------------------------");
         System.out.println("Length(nt), Occurences(Counts)");
         System.out.println("------------------------------");
      for( int i = 0; i < IV.size(); i++)
      {
         System.out.println(IV.get(i)[0] +", " + IV.get(i)[1]);
      };
      System.out.println();
   }
   
   static public void exportScores(PrintWriter PW,  Generation IG ) // IG: Incoming-Generation PW: Print-Writer
   {
      PW.println("----------------------");
      PW.println("Strand  Fitness Score: "+ IG.getSFS());
      PW.println("Network Fitness Score: " + IG.getNFS());
      PW.println("Total   Fitness Score: " + IG.getTFS());
      PW.println("----------------------");
      PW.println();
   }
   
   static public void exportOccurrenceList(PrintWriter PW,  Vector<Integer[]> IV ) // IV: Incoming-Vector PW: Print-Writer
   {
      if (IV.size() >0 )
      {
         PW.println("------------------------------");
         PW.println("Length(nt), Occurences(Counts)");
         PW.println("------------------------------");
         for( int i = 0; i < IV.size(); i++)
         {
            PW.println(IV.get(i)[0] +", " + IV.get(i)[1]);
         };
	   } else {
         PW.println("-------------------");
         PW.println("None");
         }
	   PW.println();
   }

   public void exportDetailedOccurrenceLists ( PrintWriter PW) throws Exception // PW: Print-Writer, IG: Incoming-Generation
   {
      PW.println("Stretches of Consecutive A's:");
      exportOccurrenceList( PW, AAStretches);
      PW.println("Stretches of Consecutive C's:");
      exportOccurrenceList( PW, CCStretches);
      PW.println("Stretches of Consecutive G's:");
      exportOccurrenceList( PW, GGStretches);
      PW.println("Stretches of Consecutive T's:");
      exportOccurrenceList( PW, TTStretches);
      PW.println("Stretches of Intramolecular complements:");
      exportOccurrenceList( PW, IntramolecularProfile);
      PW.println("Stretches of Intermolecular complements:");
      exportOccurrenceList( PW, IntermolecularProfile);
   }
   
   public void exportOccurrenceLists ( PrintWriter PW) throws Exception // PW: Print-Writer, IG: Incoming-Generation
   {
      PW.println("Stretches of Intramolecular complements:");
      exportOccurrenceList( PW, IntramolecularProfile);
      PW.println("Stretches of Intermolecular complements:");
      exportOccurrenceList( PW, IntermolecularProfile);
   }
   
   public long calculateProfileSFS ()
   {
      long sfs = 0;
            
      // ***************************
      // Calc. Strand Fitness Scores
      // ***************************
     
      int currentLength = 0;
      int currentOccurrences = 0;
    
      // ********************
      // Score Intramolecular Stretches
      // ********************
      
      long IntramolecularScore = 0;
      for( int i = 0; i < IntramolecularProfile.size(); i++) // for all elements in the GG occurrences array
      {
         currentLength = IntramolecularProfile.get(i)[0];
         currentOccurrences = IntramolecularProfile.get(i)[1];
         IntramolecularScore = IntramolecularScore + (scoreStretch( currentLength ) * ( ScoreRatio + currentOccurrences));

      }
      sfs = IntramolecularScore;
      return sfs;
   }
   
   public static long calculateInterferenceSFS ( MolecularScore IMS) //Incoming-Molecular-Score
   {
      long sfs = 0;
            
      // ***************************
      // Calc. Strand Fitness Scores
      // ***************************
     
      int currentLength = 0;
      int currentOccurrences = 0;
      long IntramolecularScore = 0;
    
      // shortcut if there are no intramolecular interferences
      
      if(IMS.getIntramolecularInterference().size() == 0)
      { return 0 ;}
    
      // ********************
      // Score Intramolecular Stretches
      // ********************
      Vector<Integer[]> currentProfile = IMS.getIntramolecularInterference();
      
      switch (ScoreType)
      {
         case "SLC":
         {
            for( int i = 0; i < currentProfile.size(); i++) // for all elements in the GG occurrences array
            {
               IntramolecularScore = IntramolecularScore + ( scoreStretch( currentProfile.get(i)[0] ) * ( ScoreRatio + currentProfile.get(i)[1]));
            }
            break;
         }
         case "Longest":
         {
            IntramolecularScore = IntramolecularScore + (scoreStretch( currentProfile.get(currentProfile.size()-1)[0] ) * ( ScoreRatio + currentProfile.get(currentProfile.size()-1)[1]));
            break;
         }
      }
      sfs = IntramolecularScore;

      return sfs;
   }
   
   public long calculateProfileNFS()
   {
      long nfs =0;
      int currentLength=0;
      int currentOccurrences=0;
      
      // *******************************
      // Calculate Network Fitness Score
      // *******************************
      
      // ********************
      // Score Intermolecular Stretches
      // ********************
      
      long IntermolecularScore = 0;
      for( int i = 0; i < IntermolecularProfile.size(); i++) // for all elements in the Intermolecular profile array
      {
         currentLength = IntermolecularProfile.get(i)[0];
         currentOccurrences = IntermolecularProfile.get(i)[1];
         IntermolecularScore = IntermolecularScore + (scoreStretch( currentLength ) * ( ScoreRatio + currentOccurrences) );
         //System.out.println("Scored "+ currentOccurrences + " stretch(es) of length " + currentLength +":"); 
         //System.out.println("   IntramolecularScore = " + IntermolecularScore);
      }
      nfs = IntermolecularScore;

      return nfs;
   }
   
   public static long calculateInterferenceNFS( MolecularScore IMS) // Incoming-Molecular-Score
   {
      long nfs =0;
      int currentLength=0;
      int currentOccurrences=0;
      long IntermolecularScore = 0;
      
      // *******************************
      // Calculate Network Fitness Score
      // *******************************

      if(IMS.getIntermolecularInterference().size() == 0)
         { return 0 ;}
      
      // ******************************
      // Score Intermolecular Stretches
      // ******************************
      
      Vector<Integer[]> currentProfile =  IMS.getIntermolecularInterference();
      
      switch (ScoreType)
      {
         case "SLC":
         {
            for( int i = 0; i < currentProfile.size(); i++) // for all elements in the Intermolecular profile array
            {
               IntermolecularScore = IntermolecularScore + ( scoreStretch( currentProfile.get(i)[0] ) * ( ScoreRatio + currentProfile.get(i)[1]) );
            }
            break;
         }
         case "Longest":
         {
            IntermolecularScore = IntermolecularScore + (scoreStretch( currentProfile.get(currentProfile.size()-1)[0] ) * ( ScoreRatio + currentProfile.get(currentProfile.size()-1)[1]) );
            break;
         }
      }
      nfs = IntermolecularScore;

      return nfs;
   }
   
   public void clearStretches()
   {
      IntramolecularProfile.clear();
      IntermolecularProfile.clear();
      
      AAStretches.clear();
      CCStretches.clear(); 
      GGStretches.clear(); 
      TTStretches.clear();
   }
   
   static public void scoreGeneration (Generation IG ) // IG: Incoming-Generation
   {
      Generation[] tempArray = { IG };
      //tempArray[0] = IG;
      scoreGenerations ( tempArray );
   }
   
   static public void scoreGenerations( Generation[] IGs ) // IGs: Incoming-Generations 
   {
      // ******************************
      // Reset Scores for each IG
      // ******************************
      
      for( Generation IG : IGs )
      {
         IG.setSFS(0);
         IG.setNFS(0);
         IG.setTFS(0);
      }
      
      // ********************
      // Search for stretches
      // ********************
     
      for (Generation IG: IGs) // for each generation
      {
         IG.getMolecularScore().generateSLCProfiles( IG ); // Create  profiles containing complementssearch for all complements longer than SLC
      }
      
      switch (ProfileType)
      {
         case "Profile": // For scoring based on the full watson-crick-profile
            for (Generation IG: IGs) // for each generation
            {
               IG.setSFS( IntramolecularW * IG.getMolecularScore().calculateProfileSFS() ); // calculate and set SFS
               IG.setNFS( IntermolecularW * IG.getMolecularScore().calculateProfileNFS() ); // calculate and set NFS
               IG.setTFS( (IG.getSFS() + IG.getNFS() ) );  // calculate and set TFS
            } 
            break;
         case "Interference": // For scoring based on only the longest watson-crick-interference
            for (Generation IG: IGs) // for each generation
            {
               calculateInterferences( IG.getMolecularScore() );
               IG.setSFS( IntramolecularW * calculateInterferenceSFS( IG.getMolecularScore() ) ); // calculate and set SFS
               IG.setNFS( IntermolecularW * calculateInterferenceNFS( IG.getMolecularScore() ) ); // calculate and set NFS
               IG.setTFS( ( IG.getSFS() + IG.getNFS() ) );  // calculate and set TFS
            } 
            break;
      }
   }
   
   static private long scoreStretch( int IL) //IL: Incoming-length Calculate the Score for a stretch of length IL
   {
      //Scoring code prior to 2018-01-09
      // Calculate score for length IL
      // Score = Length Squared + Length Squared of all sub-stretches
      
      //long tempScore = 0;
      //for (int i=0; i < IL; i++) // for all lengths up to the target length
      //{
         //tempScore = tempScore + ( (IL-i) * (i+1)); // IL-i = Length of this stretch i+1 = # of stretches of this length
      //}
      //System.out.println("Length = " + IL + ". Score = " + tempScore);
      
      long tempScore = 0;
      tempScore = 1;
      

      for(int i=0; i < IL && i < 10; i++)
      {
         tempScore = tempScore * 10;
      }
      if (IL >=10)
      {
         tempScore= tempScore*IL*IL;
      }

      
      return tempScore;
   }
    
   public static void exportScoringParameters( PrintWriter PW) throws Exception // PW: Print-Writer
   {
      PW.println("Scoring Parameters: ");
      PW.println("--------------------------------");
      
      PW.println("Scoring Length Criteria (SLC's):");
      PW.println("IntramolecularSLC = " + IntramolecularSLC);
      PW.println("IntermolecularSLC = " + IntermolecularSLC);
      
      PW.println("AAslc = " + AAslc);
      PW.println("CCslc = " + CCslc);
      PW.println("GGslc = " + GGslc);
      PW.println("TTslc = " + TTslc);

      PW.println();
      PW.println("Scoring Weights:");       
      PW.println("IntramolecularW = " + IntramolecularW);
      PW.println("IntermolecularW = " + IntermolecularW);

      PW.println();
   }
   
   public static boolean checkValidity( Generation IG )// IG: Incoming Generation
   {
		boolean Validity = true;
      
      // ************************************
      // Count Unfavorable Stretches of Bases 
      // Generate Occurrence list for 
      // ************************************
     
     int tns = IG.getTNS(); //get total number of strands
     int ess[][] = IG.getESS(); //get encoded strand sequences
     int sl[] = IG.getSL();  // get strand lengths
     
     search:
     for(int i = 0; i < tns; i++) // iterate through all strands.
     {
       //System.out.println("Scoring Strand " + i);
       //System.out.print("Strand bases:");
        
       int Cc = 0; // number of c's in a row counter
       int Gc = 0; // number of g's in a row counter
       int Ac = 0; // number of a's in a row counter
       int Tc = 0; // number of t's in a row counter

       int b0 = ess[i][0]; //set the first case
       int b1 = 0;

       switch(b0) //start the first run
       {
         case 1: // if the coded sequence = 1 = A
            Ac = Ac + 1;
            break;
         case 2: // if the coded sequence = 2 = C
            Cc = Cc + 1;
            break;
         case 3: // if the coded sequence = 3 = G
            Gc = Gc + 1;
            break;
         case 4: // if the coded sequence = 4 = T
            Tc = Tc + 1;
            break;
       };

       for(int j = 1; j <= sl[i]; j++) // for all bases j in the strand i (starting at base 1, not zero)
       {
         //System.out.print(b0);
         b0 = ess[i][j-1]; // set b0 to be the previous base
         
         if( j < sl[i]) // if b1 will be within the strand
         {
            b1 = ess[i][j]; // set b1 to be the current base
         } 
         else if (j == sl[i] ) // if b1 will be beyond the strand
         {
            b1 = 0; // let b1 = 0 so the previous base and runs can be scored.
         };
         
         if(b1 == b0) // if this base is the same as the previous base.
         {
            switch(b0) //increment the proper counter.
            {
              case 1:
                Ac = Ac + 1;
                break;
              case 2:
                Cc = Cc + 1;
                break;
              case 3:
                Gc = Gc + 1;
                break;
              case 4:
                Tc = Tc + 1;
                break;
            };
         };
         
         
         if(b1 != b0) // if the two bases are not equal
         {
            switch(b0) // record the score from the consecutive base counter, reset counter
            {
              case 1:
                if( Ac > AAslc) {Validity = false; break search;};
                Ac = 0;
                break;
              case 2:
                if( Cc > CCslc) {Validity = false; break search;};
                Cc = 0;
                break;
              case 3:
                if( Gc > GGslc) {Validity = false; break search;};
                Gc = 0; 
                break;
              case 4:
                if( Tc > TTslc) {Validity = false; break search;};
                Tc = 0;
                break;
            };
            switch(b1) //start the next run
            {
              case 1: // if the coded sequence = 1 = A
                Ac = Ac + 1;
                break;
              case 2: // if the coded sequence = 2 = C
                Cc = Cc + 1; 
                break;
              case 3: // if the coded sequence = 3 = G
                Gc = Gc + 1;
                break;
              case 4: // if the coded sequence = 4 = T
                Tc = Tc + 1;
                break;
              case 0:
                break;
            };
         };
       }
       //System.out.println();
     }
     return Validity;
   }
   
   private static void generateBaselineProfile ( Generation IG )// generate vector of complements longer than a specified SLC for intra/intermolecular events.
   {
      
      // **********************************
      // Clear previously found occurrences
      // **********************************
      
      IntramolecularBaseline.clear();
      IntermolecularBaseline.clear();
      
      // ************************************
      // Count Unfavorable Stretches of Bases 
      // Generate Occurrence list for 
      // ************************************
      
      int tns = IG.getTNS(); //get total number of strands
      int ess[][] = IG.getESS(); //get encoded strand sequences
      int sl[] = IG.getSL();  // get strand lengths
      int nvb = IG.getNVB();
	  
      // ***********************************
      // Find unfit stretches of complements
      // ***********************************
      
      for( int i = 0; i < tns; i++ ) // for all strands in the system,
      {
         
         // *************************************
         // Search for Intramolecular complements
         // (Search for Hairpin-like complements)
         // *************************************
      
         int IntramolecularCounter = 0; // counter for consecutive hairpin-style complements
         
         for (int RP = 1; RP < sl[i]; RP++) // for all reference positions, (move bottom strand for each reference position) 
         {
            
            //search for all events in this alignment (No base in bulge)
            
            for( int j = RP - Math.min( sl[i] - RP - 1, RP-1 ); j <= RP ; j++) // for each base in the overlap range 
            {
               if ( ess[ i ][j - 1 ] + ess[ i ][ 2*RP - j +1 - 1 ] == 5  || ess[ i ][j - 1 ] + ess[ i ][ 2*RP - j +1 - 1 ] == (2*nvb + 11) ) // if the bases in the anti-aligned areas are complementary ...
               {
                  IntramolecularCounter++; //increase the current run by one.
               } 
               else // if the bases are not a complement
               {
                  if (IntramolecularCounter >= 1 ) // if the stretch is long enough to count
                  {
                     addStretch( IntramolecularBaseline, IntramolecularCounter); //record the complement
                  }
                  IntramolecularCounter = 0; // reset the current run.
               }
            }
            if (IntramolecularCounter>= 1) // if the search ended on a long enough run, record it.
            {
                addStretch( IntramolecularBaseline, IntramolecularCounter);
            }
            IntramolecularCounter = 0; // reset the current run.
            
            //Slide the top strand by one (leaving one base in the bulge) and search for events.
            
            if ( (RP + 1) < sl[i] )
            {
               for( int j = RP - Math.min( sl[i] - RP - 2, RP-1 ); j <= RP ; j++) // for each base in the overlap range 
               {
                  if ( ess[ i ][j-1] + ess[ i ][ 2*RP - j  +1 ] == 5 || ess[ i ][j-1] + ess[ i ][ 2*RP - j  +1 ] == (2*nvb + 11)) // if the bases in the anti-aligned areas are complementary ...
                  {
                     IntramolecularCounter++; //increase the current run by one.
                  }
                  else // if the bases are not a complement
                  {
                     if (IntramolecularCounter >= 1 ) // if the stretch is long enough to count
                     {
                        addStretch( IntramolecularBaseline, IntramolecularCounter); //record the complement
                     }
                     IntramolecularCounter = 0; // reset the current run.
                  }
               }
               if (IntramolecularCounter>= 1) // if the search ended on a long enough run, record it.
               {
                   addStretch( IntramolecularBaseline, IntramolecularCounter);
               }
               IntramolecularCounter = 0; // reset the current run.
            }
         }
         
         
         // *************************************
         // Search for Intermolecular complements
         // *************************************
      
         int IntermolecularCounter = 0; // counter for consecutive bases between strand 1,2 (including a strand and a copy of itself)
         
         for (int j = i; j < tns; j++) //for all sets of strands that have not yet been considered
         {
            for (int rp = 0; rp < sl[i] + sl[j] - 1; rp++) // for all reference positions, 
            {
               for( int k = Math.max( 0 , rp + 1  - sl[j] ); k < Math.min( rp + 1 , sl[i]); k++) // for the overlap range 
               {
                  // *********************************************************
                  // Score strand1 vs strand1 in the anti-parallel orientation
                  // *********************************************************
                  
                  if ( ess[ i ][k] + ess[ j ][rp-k] == 5 || ess[ i ][k] + ess[ j ][rp-k] == (2*nvb + 11) ) // if the bases on anti-parallel aligned strand 1 and 2 are complementary ...
                  {
                     IntermolecularCounter++; //increase the current run by one.
                  }
                  else
                  {
                     if (IntermolecularCounter >= 1 ) 
                     {
                        addStretch( IntermolecularBaseline, IntermolecularCounter);
                     }
                     IntermolecularCounter = 0; // reset the current run.
                  }
               }
               
               // ************************
               // score any remaining runs
               // ************************
               
               if ( IntermolecularCounter >= 1 ) addStretch( IntermolecularBaseline, IntermolecularCounter );
               IntermolecularCounter=0; // reset run counter
            }
         }
      } 

      if (ProfileCompleteness.equals("Complete"))
      {
         IntramolecularBaseline = makeComplete( IntramolecularBaseline, IntramolecularSLC);
         IntermolecularBaseline = makeComplete( IntermolecularBaseline, IntermolecularSLC);
      }
   }
   
   private void generateSLCProfiles ( Generation IG )// generate vector of complements longer than a specified SLC for intra/intermolecular events.
   {
      
      // **********************************
      // Clear previously found occurrences
      // **********************************
      
      IntramolecularProfile.clear();
      IntermolecularProfile.clear();
      
      // ************************************
      // Count Unfavorable Stretches of Bases 
      // Generate Occurrence list for 
      // ************************************
      
      int tns = IG.getTNS(); //get total number of strands
      int ess[][] = IG.getESS(); //get encoded strand sequences
      int sl[] = IG.getSL();  // get strand lengths
      int nvb = IG.getNVB();
	  
      // ***********************************
      // Find unfit stretches of complements
      // ***********************************
      
      for( int i = 0; i < tns; i++ ) // for all strands in the system,
      {
         
         // *************************************
         // Search for Intramolecular complements
         // (Search for Hairpin-like complements)
         // *************************************
      
         int IntramolecularCounter = 0; // counter for consecutive hairpin-style complements
         
         for (int RP = 1; RP < sl[i]; RP++) // for all reference positions, (move bottom strand for each reference position) 
         {
            
            //search for all events in this alignment (No base in bulge)
            
            for( int j = RP - Math.min( sl[i] - RP - 1, RP-1 ); j <= RP ; j++) // for each base in the overlap range 
            {
               if ( ess[ i ][j - 1 ] + ess[ i ][ 2*RP - j +1 - 1 ] == 5  || ess[ i ][j - 1 ] + ess[ i ][ 2*RP - j +1 - 1 ] == (2*nvb + 11) ) // if the bases in the anti-aligned areas are complementary ...
               {
                  IntramolecularCounter++; //increase the current run by one.
               } 
               else // if the bases are not a complement
               {
                  if (IntramolecularCounter >= IntramolecularSLC ) // if the stretch is long enough to count
                  {
                     addStretch( IntramolecularProfile, IntramolecularCounter); //record the complement
                  }
                  IntramolecularCounter = 0; // reset the current run.
               }
            }
            if (IntramolecularCounter>= IntramolecularSLC) // if the search ended on a long enough run, record it.
            {
                addStretch( IntramolecularProfile, IntramolecularCounter);
            }
            IntramolecularCounter = 0; // reset the current run.
            
            //Slide the top strand by one (leaving one base in the bulge) and search for events.
            
            if ( (RP + 1) < sl[i] )
            {
               for( int j = RP - Math.min( sl[i] - RP - 2, RP-1 ); j <= RP ; j++) // for each base in the overlap range 
               {
                  if ( ess[ i ][j-1] + ess[ i ][ 2*RP - j  +1 ] == 5 || ess[ i ][j-1] + ess[ i ][ 2*RP - j  +1 ] == (2*nvb + 11)) // if the bases in the anti-aligned areas are complementary ...
                  {
                     IntramolecularCounter++; //increase the current run by one.
                  }
                  else // if the bases are not a complement
                  {
                     if (IntramolecularCounter >= IntramolecularSLC ) // if the stretch is long enough to count
                     {
                        addStretch( IntramolecularProfile, IntramolecularCounter); //record the complement
                     }
                     IntramolecularCounter = 0; // reset the current run.
                  }
               }
               if (IntramolecularCounter>= IntramolecularSLC) // if the search ended on a long enough run, record it.
               {
                   addStretch( IntramolecularProfile, IntramolecularCounter);
               }
               IntramolecularCounter = 0; // reset the current run.
            }
         }
         
         
         // *************************************
         // Search for Intermolecular complements
         // *************************************
      
         int IntermolecularCounter = 0; // counter for consecutive bases between strand 1,2 (including a strand and a copy of itself)
         
         for (int j = i; j < tns; j++) //for all sets of strands that have not yet been considered
         {
            for (int rp = 0; rp < sl[i] + sl[j] - 1; rp++) // for all reference positions, 
            {
               for( int k = Math.max( 0 , rp + 1  - sl[j] ); k < Math.min( rp + 1 , sl[i]); k++) // for the overlap range 
               {
                  // *********************************************************
                  // Score strand1 vs strand1 in the anti-parallel orientation
                  // *********************************************************
                  
                  if ( ess[ i ][k] + ess[ j ][rp-k] == 5 || ess[ i ][k] + ess[ j ][rp-k] == (2*nvb + 11) ) // if the bases on anti-parallel aligned strand 1 and 2 are complementary ...
                  {
                     IntermolecularCounter++; //increase the current run by one.
                  }
                  else
                  {
                     if (IntermolecularCounter >= IntermolecularSLC ) 
                     {
                        addStretch( IntermolecularProfile, IntermolecularCounter);
                     }
                     IntermolecularCounter = 0; // reset the current run.
                  }
               }
               
               // ************************
               // score any remaining runs
               // ************************
               
               if ( IntermolecularCounter >= IntermolecularSLC ) 
               {
                  addStretch( IntermolecularProfile, IntermolecularCounter );
               }
               IntermolecularCounter=0; // reset run counter
            }
         }
      }

      if (ProfileCompleteness.equals("Complete"))
      {
         IntramolecularProfile = makeComplete( IntramolecularProfile, IntramolecularSLC);
         IntermolecularProfile = makeComplete( IntermolecularProfile, IntermolecularSLC);
      }      
   }
   
   public Vector<Integer[]> getIntramolecularProfile()
   {
      return IntramolecularProfile;
   }
   
   public Vector<Integer[]> getIntermolecularProfile()
   {
      return IntermolecularProfile;
   }
   
   public static void setBaselines( Generation IG ) // Incoming-Generation
   {     
      IG.getMolecularScore().generateBaselineProfile( IG ); // search for all complements
   }
   
   private static Vector<Integer[]> profileDifference ( Vector<Integer[]> IV1,  Vector<Integer[]> IV2)
   {
      Vector<Integer[]> differenceVector = new Vector<Integer[]>();
   
      boolean FoundLength = false;
   
      for (int i = 0 ; i < IV1.size(); i++) // for all the integer pairs in IV1
      {
         FoundLength = false; //restart found length counter.
         
         for (int j = 0 ; j < IV2.size(); j++) // for all integer pairs in vector 2
         {
            if( IV2.get(j)[0] .equals( IV1.get(i)[0] )) //if the length is the same
            {
               Integer[] TempA = new Integer[2];
               TempA[0] = IV1.get(i)[0];
               TempA[1] = (IV1.get(i)[1] - IV2.get(j)[1]);
               differenceVector.add( TempA);
               FoundLength= true;
            }
         }
         // if the length was not found in vector 2.
         if( FoundLength == false )
         {
            Integer[] TempB = new Integer[2];
            TempB[0] = IV1.get(i)[0];
            TempB[1] = IV1.get(i)[1];
            differenceVector.add( TempB ); // add the element of vector 1 without subtraction.
         }
      }
      
      //for all the elements in difference vector. 
         // check if the occurrences value is zero.
            //if so remove the element
            
      for (int i = differenceVector.size()-1 ; i>=0; i--) // for all the elements in the new vector
      {
         if(differenceVector.get(i)[1] == 0) // if the length occurs zero times
         {
            differenceVector.remove(i); // remove that element
         }
      }
      return differenceVector;
   }
   
   public static void updateIntramolecularInterference (MolecularScore IMS) // Incoming-Molecular-Score
   { 
      IMS.getIntramolecularInterference().clear();
   
      boolean FoundLength = false;
   
      for (int i = 0 ; i < IMS.getIntramolecularProfile().size(); i++) // for all the integer pairs in IV1
      {
         FoundLength = false; //restart found length counter.
         
         for (int j = 0 ; j < IntramolecularBaseline.size(); j++) // for all integer pairs in vector 2
         {
            if( IntramolecularBaseline.get(j)[0].equals( IMS.getIntramolecularProfile().get(i)[0] )) //if the length is the same
            {
               Integer[] TempA = new Integer[2];
               TempA[0] = new Integer(IMS.getIntramolecularProfile().get(i)[0]);
               TempA[1] = new Integer(IMS.getIntramolecularProfile().get(i)[1] - IntramolecularBaseline.get(j)[1]);
               IMS.getIntramolecularInterference().add( TempA);
               FoundLength= true;
            }
         }
         // if the length was not found in vector 2.
         if( FoundLength == false )
         {
            Integer[] TempA = new Integer[2];
            TempA[0] = new Integer(IMS.getIntramolecularProfile().get(i)[0]);
            TempA[1] = new Integer(IMS.getIntramolecularProfile().get(i)[1]);
            IMS.getIntramolecularInterference().add( TempA ); // add the element of vector 1 without subtraction.
         }
      }
      
      //For all elements in Baseline
         //if the element is not in Interference Vector
            //introduce negative baseline counts.
      
      for(int i = 0; i < IntramolecularBaseline.size(); i++)
      {
         FoundLength = false;
         for(int j = 0; j < IMS.getIntramolecularInterference().size(); j++ )
         {
            if( IntramolecularBaseline.get(i)[0].equals(IMS.getIntramolecularInterference().get(j)[0]) )
            {
               FoundLength = true;
            }
            if ( IntramolecularBaseline.get(i)[0] < IMS.getIntramolecularInterference().get(j)[0] && FoundLength == false)
            {
               Integer[] TempA = new Integer[2];
               TempA[0]= new Integer(IntramolecularBaseline.get(i)[0]);
               TempA[1]= new Integer(0 - IntramolecularBaseline.get(i)[1]);
               IMS.getIntramolecularInterference().add(j,TempA);
               FoundLength = true;
            }
         }
      }
      
      //for all the elements in difference vector. 
         // check if the occurrences value is zero.
            //if so remove the element
            
      for (int i = IMS.getIntramolecularInterference().size()-1 ; i>=0; i--) // for all the elements in the new vector
      {
         if(IMS.getIntramolecularInterference().get(i)[1] == 0) // if the length occurs zero times
         {
            IMS.getIntramolecularInterference().remove(i); // remove that element
         }
      }
      
      
      
   }
   
   public static void updateIntermolecularInterference (MolecularScore IMS) // Incoming-Molecular-Score
   {
      IMS.getIntermolecularInterference().clear();
   
      boolean FoundLength = false;
      
      //For all elements in the baseline
      
      for (int i = 0 ; i < IMS.getIntermolecularProfile().size(); i++) // for all the integer pairs in WCP IMS.getIntermolecularProfile()
      {
         FoundLength = false; //restart found length counter.
         
         for (int j = 0 ; j < IntermolecularBaseline.size(); j++) // for all integer pairs in WCB
         {
            if( IntermolecularBaseline.get(j)[0].equals( IMS.getIntermolecularProfile().get(i)[0] )) //if the length is the same
            {
               Integer[] TempA = new Integer[2];               
               TempA[0] = new Integer(IMS.getIntermolecularProfile().get(i)[0]);
               TempA[1] = new Integer((IMS.getIntermolecularProfile().get(i)[1] - IntermolecularBaseline.get(j)[1]));
               IMS.getIntermolecularInterference().add( TempA );
               FoundLength= true;
            }
         }
         // if the length was not found in vector 2.
         if( FoundLength == false )
         {
            Integer[] TempA = new Integer[2];
            TempA[0] = new Integer(IMS.getIntermolecularProfile().get(i)[0]);
            TempA[1] = new Integer(IMS.getIntermolecularProfile().get(i)[1]);
            IMS.getIntermolecularInterference().add( TempA ); // add the element of vector 1 without subtraction.
         }
      }
      
      //For all elements in Baseline
         //if the element is not in Interference Vector
            //introduce negative baseline counts.
      
      for(int i = 0; i < IntermolecularBaseline.size(); i++) // for all elements in the baseline
      {
         FoundLength = false;
         for(int j = 0; j < IMS.getIntermolecularInterference().size(); j++ )// for all elements in the interference vector
         {
            if( IntermolecularBaseline.get(i)[0].equals(IMS.getIntermolecularInterference().get(j)[0]) ) // is this the length of the baseline element?
            {
               FoundLength = true;
            }
            if ( IntermolecularBaseline.get(i)[0] < IMS.getIntermolecularInterference().get(j)[0] && (FoundLength == false))
            {
               Integer[] TempA = new Integer[2];
               TempA[0]= new Integer(IntermolecularBaseline.get(i)[0]);
               TempA[1]= new Integer(0 - IntermolecularBaseline.get(i)[1]);
               IMS.getIntermolecularInterference().add(j,TempA);
               FoundLength = true;
            }
         }
      }
      
      //for all the elements in difference vector. 
         // check if the occurrences value is zero.
            //if so remove the element
            
      for (int i = IMS.getIntermolecularInterference().size()-1 ; i>=0; i--) // for all the elements in the new vector
      {
         if(IMS.getIntermolecularInterference().get(i)[1] == 0) // if the length occurs zero times
         {
            IMS.getIntermolecularInterference().remove(i); // remove that element
         }
      }
   }
   
   public static void calculateInterferences( MolecularScore IMS) // Incoming-Molecular-Score
   {
      //for the intermolecular profile...
      //IMS.setIntermolecularInterference( profileDifference( IMS.getIntermolecularProfile(), IntermolecularBaseline) );
      //IMS.setIntramolecularInterference( profileDifference( IMS.getIntramolecularProfile(), IntramolecularBaseline) );   
      updateIntramolecularInterference( IMS );
      updateIntermolecularInterference( IMS );

   }
   
   public void printInterferenceLists()
   {
      if( IntramolecularInterference.size() != 0)
	  {
		System.out.println("Stretches of Intramolecular Complements:");
		printOccurrenceList( IntramolecularInterference);
	  } else {System.out.println("No Stretches of Intramolecular Complements");System.out.println();};
	  
	  if( IntermolecularInterference.size() != 0)
	  {
		System.out.println("Stretches of Intermolecular Complements:");
		printOccurrenceList( IntermolecularInterference);
	  } else {System.out.println("No Stretches of Intermolecular Complements");System.out.println();};
   }
   
   public void printBaselineLists()
   {
      if( IntramolecularBaseline.size() != 0)
	  {
		System.out.println("Stretches of Intramolecular Complements:");
		printOccurrenceList( IntramolecularBaseline);
	  } else {System.out.println("No Stretches of Intramolecular Complements");System.out.println();};
	  
	  if( IntermolecularBaseline.size() != 0)
	  {
		System.out.println("Stretches of Intermolecular Complements:");
		printOccurrenceList( IntermolecularBaseline);
	  } else {System.out.println("No Stretches of Intermolecular Complements");System.out.println();};
   }
   
   public void printCompleteBaselineLists()
   {
      if( IntramolecularBaseline.size() != 0)
	  {
		System.out.println("Stretches of Intramolecular Complements:");
		printOccurrenceList( makeComplete(IntramolecularBaseline,IntramolecularSLC ) );
	  } else {System.out.println("No Stretches of Intramolecular Complements");System.out.println();};
	  
	  if( IntermolecularBaseline.size() != 0)
	  {
		System.out.println("Stretches of Intermolecular Complements:");
		printOccurrenceList( makeComplete(IntermolecularBaseline,IntermolecularSLC));
	  } else {System.out.println("No Stretches of Intermolecular Complements");System.out.println();};
   }
   
   public void printConsecutiveBases()
   {
      
      if(AAStretches.size() != 0 )
	  {
			System.out.println("Stretches of Consecutive A's:");
			printOccurrenceList( AAStretches);
      } 
	  else {System.out.println("No Stretches of Consecutive A's");System.out.println();};
	  
	  if(CCStretches.size() != 0 )
	  {
      System.out.println("Stretches of Consecutive C's:");
      printOccurrenceList( CCStretches);
	  } 
	  else {System.out.println("No Stretches of Consecutive C's");System.out.println();};

	  if(GGStretches.size() != 0 )
	  {	  
      System.out.println("Stretches of Consecutive G's:");
      printOccurrenceList( GGStretches);
	  } 
	  else {System.out.println("No Stretches of Consecutive G's");System.out.println();};
	  
	  if(TTStretches.size() != 0 )
	  {	
      System.out.println("Stretches of Consecutive T's:");
      printOccurrenceList( TTStretches);
	  } 
	  else {System.out.println("No Stretches of Consecutive T's");System.out.println();};
   }
   
   public void exportBaselineLists ( PrintWriter PW) throws Exception // PW: Print-Writer, IG: Incoming-Generation
   {
      PW.println("Stretches of Intramolecular complements:");
      exportOccurrenceList( PW, IntramolecularBaseline);
      PW.println("Stretches of Intermolecular complements:");
      exportOccurrenceList( PW, IntermolecularBaseline);
   }
 
   public void exportProfileLists ( PrintWriter PW) throws Exception // PW: Print-Writer, IG: Incoming-Generation
   {
      PW.println("Stretches of Intramolecular complements:");
      exportOccurrenceList( PW, IntramolecularProfile);
      PW.println("Stretches of Intermolecular complements:");
      exportOccurrenceList( PW, IntermolecularProfile);
   } 
   
   public void exportInterferenceLists ( PrintWriter PW) throws Exception // PW: Print-Writer, IG: Incoming-Generation
   {
      PW.println("****************************************");
      PW.println("Stretches of Intramolecular complements:");
      PW.println("****************************************");
      PW.println();
      exportOccurrenceList( PW, IntramolecularInterference);
      PW.println("--Complements shorter than " + IntramolecularSLC + "nt omitted due to IntramolecularSLC setting.--");
      PW.println();
      
      PW.println("****************************************");
      PW.println("Stretches of Intermolecular complements:");
      PW.println("****************************************");
      PW.println();
      exportOccurrenceList( PW, IntermolecularInterference);
      
      PW.println("--Complements shorter than " + IntermolecularSLC + "nt omitted due to IntermolecularSLC setting.--");
      PW.println();
      
   }
   
      public void exportConsecutiveBases( PrintWriter PW ) throws Exception
   {
      
      PW.println("Stretches of Consecutive A's:");
      exportOccurrenceList(PW, AAStretches);

      PW.println("Stretches of Consecutive C's:");
      exportOccurrenceList( PW, CCStretches);

      PW.println("Stretches of Consecutive G's:");
      exportOccurrenceList( PW, GGStretches);

      PW.println("Stretches of Consecutive T's:");
      exportOccurrenceList( PW, TTStretches);
   }
   
   public static void setScoringFunction(String Incoming)
   {
      switch ( Incoming )
      {
         case "WCP" : 
            ScoringFunction = "WCP";
            ProfileType = "Profile";
            ScoreType = "SLC";
            break;
         case "WCI" :
            ScoringFunction = "WCI";
            ProfileType = "Interference";
            ScoreType = "SLC";
            break;
         case "LongestWCI" :
            ScoringFunction = "LongestWCI";
            ProfileType = "Interference";
            ScoreType = "Longest";
            break;
      }
   }
   
   public static String getScoringFunction()
   {
      return ScoringFunction;
   }
   
   public static int getIntraSLC()
   {
      return IntramolecularSLC;
   }
   
   public static int getInterSLC()
   {
      return IntermolecularSLC;
   }
   
   public static int getIntraW()
   {
      return IntramolecularW;
   }
   
   public static int getInterW()
   {
      return IntermolecularW;
   }
   
   public static int getAASLC()
   {
      return AAslc;
   }
   
   public static int getTTSLC()
   {
      return TTslc;
   }
   
   public static int getCCSLC()
   {
      return CCslc;
   }
   
   public static int getGGSLC()
   {
      return GGslc;
   }
   public void setIntramolecularInterference( Vector IV )
   {
      IntramolecularInterference = IV;
   }
   
   public Vector<Integer[]> getIntramolecularInterference ()
   {
      return IntramolecularInterference;
   }
   
   public Vector<Integer[]> getIntermolecularInterference ()
   {
      return IntermolecularInterference;
   }
   public void setIntermolecularInterference( Vector IV )
   {
      IntermolecularInterference = IV;
   }
   
   public static String getProfileCompleteness()
   {
      return ProfileCompleteness;
   }
   
   public static Vector<Integer[]> makeComplete( Vector<Integer[]> IV, int ISLC ) // Incoming-Vector, Incoming Scoring-Length-Criteria
   {
      Vector<Integer[]> TV = new Vector<Integer[]>(); //Temp-Vector
      
      if(IV.size() != 0)
      {         
         for (int i = IV.size() - 1 ; i >= 0 ; i--) // for all the integer pairs in IV, read through the array backward.
         {
            for (int j = IV.get(i)[0] ; j > 0 && j >= ISLC; j--) // for all lengths shorter than  or equal to this length, but longer than SLC
            {
               addStretches( TV, j, IV.get(i)[1]*(IV.get(i)[0] - j + 1) ); // There are (IV.get(i)[0] - j + 1) sub-complements per complement of length IV.get(i)[0].
            }
         }
      }
      
      return TV;
   }
}
*/