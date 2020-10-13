package edu.boisestate.mt;
import java.io.* ;
import java.util.Date;
import java.util.Vector;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.math.BigInteger;

public class DevPro
{

   //********************************************
   // Initialize the MTlogger, which will provide 
   // verbose or debug messages when requested.
   //********************************************
   
   static MTlogger MTout = new MTlogger("DevPro.java");
   
   // ************************************
   // Global constants used by the program
   // ************************************
   
   private static final Date runDate = new Date();
   private static final String version = "1.8"; // MT 2020-08-07
   
   private static String PFilePath; // Parameters-File-Path

   private static String ReportFilePath;
   private static String ScoresFilePath;
   
   private static String PO;
   private static String PN;
   private static String POU;
   private static String PNU;
   private static String SOU;
   private static String SNU;
   
   private static String POB;
   private static String PNB;
   private static String POUB;
   private static String PNUB;
   private static String SOUB;
   private static String SNUB;
   
   private static String POI;
   private static String PNI;
   private static String POUI;
   private static String PNUI;
   private static String SOUI;
   private static String SNUI;
   
   private static String CFilePath;
   
      
   //**********************************
   //main method for the build8 program
   //**********************************
   
	public static void main(String[] args) throws Exception
	{
      
      System.out.println();
      System.out.println("******************************************************");  
      System.out.println("            Device Profiler (DevPro) " + version );      
      System.out.println("Program for identifying inadvertent structures which a");
      System.out.println("        given set of oloigonucleotides may form.");
      System.out.println("******************************************************"); 
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
      Generation.importSettings(PFilePath);
      Lineage.importSettings(PFilePath);
      StrandPool.importSettings(PFilePath);
      baseAlignment.importSettings( PFilePath);
      referencePosition.importSettings(PFilePath);
	  System.out.println("Parameters initialized.");
	  System.out.println();
      
      // **************************
      // Import network information
      // **************************
      
      DomainPool Domains = new DomainPool(); // Import the domain information into the "Domains" object.
      StrandPool Strands = new StrandPool(); // Import the strand information into the "Strands" object.
	  
      // **********************************
      // Create Generation object "Network"
      // **********************************
      
      Generation Network = new Generation( Domains, Strands ); // Create Generation Zero from the imported domains and strands.

      
      // **************************************
      // Print the Network's information
      // **************************************
    
      //System.out.println("*********************");
      //System.out.println("Oligo-Set Information");
      //System.out.println("*********************");
      //System.out.println();

      //Outputs.printIGI(Network);

      // ***********************************************
      // Print the Structural Profiles for the device
      // ***********************************************
/*
      System.out.println();
      System.out.println("**********************************");
      System.out.println("Profile of  of Structures (Complete)");
      System.out.println("**********************************");
      System.out.println();


      System.out.println("Intramolecular Structures:");
      System.out.println("-----------------------------------");
      System.out.println("Length (nt)\tCount");
      System.out.println("-----------------------------------");
      
      Map<Integer,Integer> intraCompleteOccurrences = Network.getIntraCompleteOccurrences();
      if ( intraCompleteOccurrences.size() > 0)
      {
         intraCompleteOccurrences.entrySet().stream().forEach( e-> System.out.println(e.getKey() + "\t\t" + e.getValue()));
      } else System.out.println("No structures");
      System.out.println();
     
      System.out.println("Intermolecular Structures:");
      System.out.println("---------------------------------------");
      System.out.println("Length (nt)\tCount");
      System.out.println("---------------------------------------");
      Map<Integer,Integer> interCompleteOccurrences = Network.getInterCompleteOccurrences();
      if ( interCompleteOccurrences.size() > 0)
      {
         interCompleteOccurrences.entrySet().stream().forEach( e-> System.out.println(e.getKey() + "\t\t" + e.getValue()));
      } else System.out.println("No structures");
      System.out.println();
      
      System.out.println();
      System.out.println("*****************************************");
      System.out.println("Summary of Baseline Structures (Complete)");
      System.out.println("*****************************************");
      System.out.println();

      
      System.out.println("Intramolecular Baseline Structures:");
      System.out.println("---------------------------------------");
      System.out.println("Length (nt)\tCount");
      System.out.println("---------------------------------------");
      Map<Integer,Integer> intraBaselineCompleteOccurrences = Network.getIntraBaselineCompleteOccurrences();
      if ( intraBaselineCompleteOccurrences.size() > 0)
      {
         intraBaselineCompleteOccurrences.entrySet().stream().forEach( e-> System.out.println(e.getKey() + "\t\t" + e.getValue()));
      } else System.out.println("No structures");
      System.out.println();
     
      System.out.println("Intermolecular Baseline Structures:");
      System.out.println("---------------------------------------");
      System.out.println("Length (nt)\tCount");
      System.out.println("---------------------------------------");
      Map<Integer,Integer> interBaselineCompleteOccurrences = Network.getInterBaselineCompleteOccurrences();
      if ( interBaselineCompleteOccurrences.size() > 0)
      {
         interBaselineCompleteOccurrences.entrySet().stream().forEach( e-> System.out.println(e.getKey() + "\t\t" + e.getValue()));
      } else System.out.println("No structures");
      System.out.println();
      
      System.out.println();
      System.out.println("*********************************************");
      System.out.println("Summary of Interference Structures (Complete)");
      System.out.println("*********************************************");
      System.out.println();

      System.out.println("Intramolecular Interference Structures:");
      System.out.println("---------------------------------------");
      System.out.println("Length (nt)\tCount");
      System.out.println("---------------------------------------");
      Map<Integer,Integer> intraInterferenceCompleteOccurrences = Network.getIntraInterferenceCompleteOccurrences();
      if ( intraInterferenceCompleteOccurrences.size() > 0)
      {
         intraInterferenceCompleteOccurrences.entrySet().stream().forEach( e-> System.out.println(e.getKey() + "\t\t" + e.getValue()));
      } else System.out.println("No structures");
      System.out.println();
     
      System.out.println("Intermolecular Interference Structures:");
      System.out.println("---------------------------------------");
      System.out.println("Length (nt)\tCount");
      System.out.println("---------------------------------------");
      Map<Integer,Integer> interInterferenceCompleteOccurrences = Network.getInterInterferenceCompleteOccurrences();
      if ( interInterferenceCompleteOccurrences.size() > 0)
      {
         interInterferenceCompleteOccurrences.entrySet().stream().forEach( e-> System.out.println(e.getKey() + "\t\t" + e.getValue()));
      } else System.out.println("No structures");
      System.out.println();
      
      
      System.out.println("*******************************************");
      System.out.println("Fitness Scores: ");
      System.out.println("*******************************************");
      System.out.println();
      
      BigInteger NetworkSFS = Network.getSFS();
      System.out.println("SFS score: "+ NetworkSFS);
      
      BigInteger NetworkNFS = Network.getNFS();
      System.out.println("NFS score: "+ NetworkNFS);
      
      BigInteger NetworkTFS = Network.getTFS();
      System.out.println("TFS score: "+ NetworkTFS);
      System.out.println();
*/      

      // Export dp.out.Structures.Profile.Intra
      //File tempFile = new File("DevPro-Output/"); 
      //tempFile.mkdirs();
      
      //Nomenculture for output files: O -> IntraOligo N-> InterOligo, B-> Baseline (Intentional & Implied), I-> Inadvertent, S -> Structure Sequences, P-> Structure Profile
      
      if(!PO.equals("disabled"))
      {
         FileWriter SPIntrafilewriter = new FileWriter( PO);
         BufferedWriter SPIntrabw = new BufferedWriter (SPIntrafilewriter);
         PrintWriter SPIntraPW = new PrintWriter (SPIntrabw);
    
        
		 SPIntraPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
		 SPIntraPW.println("//Summary of intra-oligo simple secondary structures."); 
		 SPIntraPW.println("//Format: \"Structure size (base-pairs)\", \"Count (number of structures)\"");  
         Network.getIntraCompleteOccurrences().entrySet().stream().forEach( e -> SPIntraPW.println(e.getKey()+", "+e.getValue()));
         SPIntraPW.close();
      }
      
      
      // Export dp.out.Structures.Profile.Inter
      
      if(!PN.equals("disabled"))
      {
         FileWriter SPInterfilewriter = new FileWriter( PN );
         BufferedWriter SPInterbw = new BufferedWriter (SPInterfilewriter);
         PrintWriter SPInterPW = new PrintWriter (SPInterbw);
         
         SPInterPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
		 SPInterPW.println("//Summary of inter-oligo simple secondary structures."); 
		 SPInterPW.println("//Format: \"Structure size (base-pairs)\", \"Count (number of structures)\"");  
         Network.getInterCompleteOccurrences().entrySet().stream().forEach( e -> SPInterPW.println(e.getKey()+", "+e.getValue()));
         SPInterPW.close();
      }

      // Export dp.out.Structures.Unique.Profile.Intra
      
      if(!POU.equals("disabled"))
      {
         FileWriter SUPIntrafilewriter = new FileWriter( POU );
         BufferedWriter SUPIntrabw = new BufferedWriter (SUPIntrafilewriter);
         PrintWriter SUPIntraPW = new PrintWriter (SUPIntrabw);
    
         SUPIntraPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
		 SUPIntraPW.println("//Summary of unique intra-oligo simple secondary structures."); 
		 SUPIntraPW.println("//Format: \"Structure size (base-pairs)\", \"Count (number of structures)\"");  
         Network.getIntraUniqueOccurrences().entrySet().stream().forEach( e -> SUPIntraPW.println(e.getKey()+", "+e.getValue()));
         SUPIntraPW.close();
      }
      
      // Export dp.out.Structures.Unique.Profile.Inter
      
      if(!PNU.equals("disabled"))
      {
         FileWriter SUPInterfilewriter = new FileWriter( PNU );
         BufferedWriter SUPInterbw = new BufferedWriter (SUPInterfilewriter);
         PrintWriter SUPInterPW = new PrintWriter (SUPInterbw);
    
         SUPInterPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
		 SUPInterPW.println("//Summary of unique inter-oligo simple secondary structures."); 
		 SUPInterPW.println("//Format: \"Structure size (base-pairs)\", \"Count (number of structures)\"");  
         Network.getInterUniqueOccurrences().entrySet().stream().forEach( e -> SUPInterPW.println(e.getKey()+", "+e.getValue()));
         SUPInterPW.close();
      }
      
      // Export dp.out.Structures.Unique.Complements.Intra
      
      if(!SOU.equals("disabled"))
      {
      FileWriter SUCIntrafilewriter = new FileWriter( SOU );
      BufferedWriter SUCIntrabw = new BufferedWriter (SUCIntrafilewriter);
      PrintWriter SUCIntraPW = new PrintWriter (SUCIntrabw);
 
      SUCIntraPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
	  SUCIntraPW.println("//List of unique intra-oligo simple secondary structures."); 
	  SUCIntraPW.println("//Format: \"Structure size (base-pairs)\", \"Oligo #1\", \"Index of 1st base on oligo #1 (5' to 3')\", \"Sequence on oligo #1\", \"Oligo #2\", \"Index of 1st base on oligo #2 (5' to 3'), \"Sequence on oligo #2\"");
	  Network.getIntraUniqueStructures().stream()
         .sorted( (a1,a2)-> Integer.compare(WCStructure.getLength(a2),WCStructure.getLength(a1)))
         //.forEach( e-> WCStructure.printStructureInfo(e));
         .forEach( e -> SUCIntraPW.println(
            WCStructure.getLength(e)
            + ", " + Network.getStrandName(WCStructure.getS1(e))
            + ", " + (WCStructure.getS1B1(e)+1)
            + ", " + WCStructure.getSequence1(e, Network.getESS())
            + ", " + Network.getStrandName(WCStructure.getS2(e))
            + ", " + (WCStructure.getS2B1(e)+1)
            + ", " + WCStructure.getSequence2(e, Network.getESS())
            ));
      SUCIntraPW.close();
      }
      
      // Export dp.out.Structures.Unique.Complements.Inter
      
      if(!SNU.equals("disabled"))
      {
      FileWriter SUCInterfilewriter = new FileWriter( SNU );
      BufferedWriter SUCInterbw = new BufferedWriter (SUCInterfilewriter);
      PrintWriter SUCInterPW = new PrintWriter (SUCInterbw);
 
      SUCInterPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
	  SUCInterPW.println("//List of unique inter-oligo simple secondary structures."); 
	  SUCInterPW.println("//Format: \"Structure size (base-pairs)\", \"Oligo #1\", \"Index of 1st base on oligo #1 (5' to 3')\", \"Sequence on oligo #1\", \"Oligo #2\", \"Index of 1st base on oligo #2 (5' to 3'), \"Sequence on oligo #2\"");
	  Network.getInterUniqueStructures().stream()
         .sorted( (a1,a2)-> Integer.compare(WCStructure.getLength(a2),WCStructure.getLength(a1)))
         //.forEach( e-> WCStructure.printStructureInfo(e));
         .forEach( e -> SUCInterPW.println(
            WCStructure.getLength(e)
            + ", " + Network.getStrandName(WCStructure.getS1(e))
            + ", " + (WCStructure.getS1B1(e)+1)
            + ", " + WCStructure.getSequence1(e, Network.getESS())
            + ", " + Network.getStrandName(WCStructure.getS2(e))
            + ", " + (WCStructure.getS2B1(e)+1)
            + ", " + WCStructure.getSequence2(e, Network.getESS())
            ));
      SUCInterPW.close();
      }
      
      // Export dp.out.BaselineStructures.Profile.Intra

      if(!POB.equals("disabled"))
      {
         FileWriter BSPIntrafilewriter = new FileWriter( POB);
         BufferedWriter BSPIntrabw = new BufferedWriter (BSPIntrafilewriter);
         PrintWriter BSPIntraPW = new PrintWriter (BSPIntrabw);
    
         BSPIntraPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
		 BSPIntraPW.println("//Summary of baseline (intentional & implied) intra-oligo simple secondary structures."); 
		 BSPIntraPW.println("//Format: \"Structure size (base-pairs)\", \"Count (number of structures)\"");  
         Network.getIntraBaselineCompleteOccurrences().entrySet().stream().forEach( e -> BSPIntraPW.println(e.getKey()+", "+e.getValue()));
         BSPIntraPW.close();
      }
      
      // Export dp.out.BaselineStructures.Profile.Inter  

      if(!PNB.equals("disabled"))
      {
         FileWriter BSPInterfilewriter = new FileWriter( PNB );
         BufferedWriter BSPInterbw = new BufferedWriter (BSPInterfilewriter);
         PrintWriter BSPInterPW = new PrintWriter (BSPInterbw);
    
         BSPInterPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
		 BSPInterPW.println("//Summary of baseline (intentional & implied) inter-oligo simple secondary structures."); 
		 BSPInterPW.println("//Format: \"Structure size (base-pairs)\", \"Count (number of structures)\"");  
         Network.getInterBaselineCompleteOccurrences().entrySet().stream().forEach( e -> BSPInterPW.println(e.getKey()+", "+e.getValue()));
         BSPInterPW.close();
      }   
     
      // Export dp.out.BaselineStructures.Unique.Profile.Intra
      
      if(!POUB.equals("disabled"))
      {
         FileWriter BSUPIntrafilewriter = new FileWriter( POUB );
         BufferedWriter BSUPIntrabw = new BufferedWriter (BSUPIntrafilewriter);
         PrintWriter BSUPIntraPW = new PrintWriter (BSUPIntrabw);
    
         BSUPIntraPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
		 BSUPIntraPW.println("//Summary of baseline (intentional & implied) unique intra-oligo simple secondary structures."); 
		 BSUPIntraPW.println("//Format: \"Structure size (base-pairs)\", \"Count (number of structures)\"");  
         Network.getIntraBaselineUniqueOccurrences().entrySet().stream().forEach( e -> BSUPIntraPW.println(e.getKey()+", "+e.getValue()));
         BSUPIntraPW.close();
      }
      
      // Export dp.out.BaselineStructures.Unique.Profile.Inter
      
      if(!PNUB.equals("disabled"))
      {
         FileWriter BSUPInterfilewriter = new FileWriter( PNUB );
         BufferedWriter BSUPInterbw = new BufferedWriter (BSUPInterfilewriter);
         PrintWriter BSUPInterPW = new PrintWriter (BSUPInterbw);
    
         BSUPInterPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
		 BSUPInterPW.println("//Summary of baseline (intentional & implied) unique intra-oligo simple secondary structures."); 
		 BSUPInterPW.println("//Format: \"Structure size (base-pairs)\", \"Count (number of structures)\"");  
         Network.getInterBaselineUniqueOccurrences().entrySet().stream().forEach( e -> BSUPInterPW.println(e.getKey()+", "+e.getValue()));
         BSUPInterPW.close();
      }
      
      // Export dp.out.BaselineStructures.Unique.Complements.Intra
      
      if(!SOUB.equals("disabled"))
      {
      FileWriter BSUCIntrafilewriter = new FileWriter( SOUB );
      BufferedWriter BSUCIntrabw = new BufferedWriter (BSUCIntrafilewriter);
      PrintWriter BSUCIntraPW = new PrintWriter (BSUCIntrabw);
 
      BSUCIntraPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
	  BSUCIntraPW.println("//List of baseline (intentional & implied) unique intra-oligo simple secondary structures."); 
	  BSUCIntraPW.println("//Format: \"Structure size (base-pairs)\", \"Oligo #1\", \"Index of 1st base on oligo #1 (5' to 3')\", \"Sequence on oligo #1\", \"Oligo #2\", \"Index of 1st base on oligo #2 (5' to 3'), \"Sequence on oligo #2\"");
	  Network.getIntraBaselineStructures().stream()
         .sorted( (a1,a2)-> Integer.compare(WCStructure.getLength(a2),WCStructure.getLength(a1)))
         //.forEach( e-> WCStructure.printStructureInfo(e));
         .forEach( e -> BSUCIntraPW.println(
            WCStructure.getLength(e)
            + ", " + Network.getStrandName(WCStructure.getS1(e))
            + ", " + (WCStructure.getS1B1(e)+1)
            + ", " + WCStructure.getSequence1(e, Network.getESS())
            + ", " + Network.getStrandName(WCStructure.getS2(e))
            + ", " + (WCStructure.getS2B1(e)+1)
            + ", " + WCStructure.getSequence2(e, Network.getESS())
            ));
      BSUCIntraPW.close();
      }
      
      // Export dp.out.BaselineStructures.Unique.Complements.Inter
      
      if(!SNUB.equals("disabled"))
      {
      FileWriter BSUCInterfilewriter = new FileWriter( SNUB );
      BufferedWriter BSUCInterbw = new BufferedWriter (BSUCInterfilewriter);
      PrintWriter BSUCInterPW = new PrintWriter (BSUCInterbw);
 
      BSUCInterPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
	  BSUCInterPW.println("//List of baseline (intentional & implied) unique inter-oligo simple secondary structures."); 
	  BSUCInterPW.println("//Format: \"Structure size (base-pairs)\", \"Oligo #1\", \"Index of 1st base on oligo #1 (5' to 3')\", \"Sequence on oligo #1\", \"Oligo #2\", \"Index of 1st base on oligo #2 (5' to 3'), \"Sequence on oligo #2\"");
	  Network.getInterBaselineStructures().stream()
         .sorted( (a1,a2)-> Integer.compare(WCStructure.getLength(a2),WCStructure.getLength(a1)))
         //.forEach( e-> WCStructure.printStructureInfo(e));
         .forEach( e -> BSUCInterPW.println(
            WCStructure.getLength(e)
            + ", " + Network.getStrandName(WCStructure.getS1(e))
            + ", " + (WCStructure.getS1B1(e)+1)
            + ", " + WCStructure.getSequence1(e, Network.getESS())
            + ", " + Network.getStrandName(WCStructure.getS2(e))
            + ", " + (WCStructure.getS2B1(e)+1)
            + ", " + WCStructure.getSequence2(e, Network.getESS())
            ));
      BSUCInterPW.close();
      }
      
      // Export dp.out.InterferenceStructures.Profile.Intra
      
      if(!POI.equals("disabled"))
      { 
      FileWriter ISPIntrafilewriter = new FileWriter(POI);
      BufferedWriter ISPIntrabw = new BufferedWriter (ISPIntrafilewriter);
      PrintWriter ISPIntraPW = new PrintWriter (ISPIntrabw);
      
	  ISPIntraPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
	  ISPIntraPW.println("//Summary of inadvertent intra-oligo simple secondary structures."); 
	  ISPIntraPW.println("//Format: \"Structure size (base-pairs)\", \"Count (number of structures)\"");  
	  Network.getIntraInterferenceCompleteOccurrences().entrySet().stream().forEach( e -> ISPIntraPW.println(e.getKey()+", "+e.getValue()));
      ISPIntraPW.close();
      }
      
      // Export dp.out.InterferenceStructures.Profile.Inter
      
      if(!PNI.equals("disabled"))
      { 
      FileWriter ISPInterfilewriter = new FileWriter( PNI );
      BufferedWriter ISPInterbw = new BufferedWriter (ISPInterfilewriter);
      PrintWriter ISPInterPW = new PrintWriter (ISPInterbw);
 
      ISPInterPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
	  ISPInterPW.println("//Summary of inadvertent inter-oligo simple secondary structures."); 
	  ISPInterPW.println("//Format: \"Structure size (base-pairs)\", \"Count (number of structures)\"");  
	  Network.getInterInterferenceCompleteOccurrences().entrySet().stream().forEach( e -> ISPInterPW.println(e.getKey()+", "+e.getValue()));
      ISPInterPW.close();
      }
      
      // Export dp.out.InterferenceStructures.Unique.Profile.Intra
      
      if(!POUI.equals("disabled"))
      { 
      FileWriter ISUPIntrafilewriter = new FileWriter( POUI );
      BufferedWriter ISUPIntrabw = new BufferedWriter (ISUPIntrafilewriter);
      PrintWriter ISUPIntraPW = new PrintWriter (ISUPIntrabw);
 
      ISUPIntraPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
	  ISUPIntraPW.println("//Summary of Unique inadvertent intra-oligo simple secondary structures."); 
	  ISUPIntraPW.println("//Format: \"Structure size (base-pairs)\", \"Count (number of structures)\"");  
	  Network.getIntraInterferenceUniqueOccurrences().entrySet().stream().forEach( e -> ISUPIntraPW.println(e.getKey()+", "+e.getValue()));
      ISUPIntraPW.close();
      }
      
      // Export dp.out.InterferenceStructures.Unique.Profile.Inter
      
      if(!PNUI.equals("disabled"))
      { 
      FileWriter ISUPInterfilewriter = new FileWriter(PNUI );
      BufferedWriter ISUPInterbw = new BufferedWriter (ISUPInterfilewriter);
      PrintWriter ISUPInterPW = new PrintWriter (ISUPInterbw);
 
      ISUPInterPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
	  ISUPInterPW.println("//Summary of inadvertent inter-oligo simple secondary structures."); 
	  ISUPInterPW.println("//Format: \"Structure size (base-pairs)\", \"Count (number of structures)\"");  
	  Network.getInterInterferenceUniqueOccurrences().entrySet().stream().forEach( e -> ISUPInterPW.println(e.getKey()+", "+e.getValue()));
      ISUPInterPW.close();
      }
      
      // Export dp.out.InterferenceStructures.Unique.Complements.Intra
      
      if(!SOUI.equals("disabled"))
      { 
      FileWriter ISUCIntrafilewriter = new FileWriter(SOUI );
      BufferedWriter ISUCIntrabw = new BufferedWriter (ISUCIntrafilewriter);
      PrintWriter ISUCIntraPW = new PrintWriter (ISUCIntrabw);
 
 
      ISUCIntraPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
	  ISUCIntraPW.println("//List of inadvertent intra-oligo simple secondary structures."); 
	  ISUCIntraPW.println("//Format: \"Structure size (base-pairs)\", \"Oligo #1\", \"Index of 1st base on oligo #1 (5' to 3')\", \"Sequence on oligo #1\", \"Oligo #2\", \"Index of 1st base on oligo #2 (5' to 3'), \"Sequence on oligo #2\"");
	  Network.getIntraInterferenceStructures().stream()
         .sorted( (a1,a2)-> Integer.compare(WCStructure.getLength(a2),WCStructure.getLength(a1)))
         //.forEach( e-> WCStructure.printStructureInfo(e));
         .forEach( e -> ISUCIntraPW.println(
            WCStructure.getLength(e)
            + ", " + Network.getStrandName(WCStructure.getS1(e))
            + ", " + (WCStructure.getS1B1(e)+1)
            + ", " + WCStructure.getSequence1(e, Network.getESS())
            + ", " + Network.getStrandName(WCStructure.getS2(e))
            + ", " + (WCStructure.getS2B1(e)+1)
            + ", " + WCStructure.getSequence2(e, Network.getESS())
            ));
      ISUCIntraPW.close();
      }
      
      // Export dp.out.InterferenceStructures.Unique.Complements.Inter
      
      if(!SNUI.equals("disabled"))
      {
      FileWriter ISUCInterfilewriter = new FileWriter(SNUI );
      BufferedWriter ISUCInterbw = new BufferedWriter (ISUCInterfilewriter);
      PrintWriter ISUCInterPW = new PrintWriter (ISUCInterbw);
 
      ISUCInterPW.println("//File Generated by DevPro. Version: " + version + "; run time: " + runDate);
	  ISUCInterPW.println("//List of inadvertent inter-oligo simple secondary structures."); 
	  ISUCInterPW.println("//Format: \"Structure size (base-pairs)\", \"Oligo #1\", \"Index of 1st base on oligo #1 (5' to 3')\", \"Sequence on oligo #1\", \"Oligo #2\", \"Index of 1st base on oligo #2 (5' to 3'), \"Sequence on oligo #2\"");
	  Network.getInterInterferenceStructures().stream()
         .sorted( (a1,a2)-> Integer.compare(WCStructure.getLength(a2),WCStructure.getLength(a1)))
         .forEach( e -> ISUCInterPW.println(
            WCStructure.getLength(e)
            + ", " + Network.getStrandName(WCStructure.getS1(e))
            + ", " + (WCStructure.getS1B1(e)+1)
            + ", " + WCStructure.getSequence1(e, Network.getESS())
            + ", " + Network.getStrandName(WCStructure.getS2(e))
            + ", " + (WCStructure.getS2B1(e)+1)
            + ", " + WCStructure.getSequence2(e, Network.getESS())
            ));
      ISUCInterPW.close();
      }
      
      if(!CFilePath.equals("disabled"))
      {
         FileWriter Cfilewriter = new FileWriter( CFilePath );
         BufferedWriter Cbw = new BufferedWriter (Cfilewriter);
         PrintWriter CPW = new PrintWriter (Cbw);
         
         CPW.println("L, C, CL, EL, EC, ECL, AL, AC, ACL"); 
         //Length (equvalent to EL)
         
         if(Network.getInterInterferenceUniqueOccurrences().size()>0)
         {
            CPW.print(Network.getInterInterferenceUniqueOccurrences().lastKey());
         } else CPW.print("0");
         CPW.print(", ");
         
         //Count
         if(Network.getInterInterferenceUniqueOccurrences().size() == 0)
         {
            if ( Network.getIntraInterferenceUniqueOccurrences().size() == 0)
            {
               CPW.print("0");
            }
            else
            {
               CPW.print(Network.getIntraInterferenceUniqueOccurrences().values().stream().mapToInt(Integer::intValue).sum());
            }
         }
         else 
         {
            if ( Network.getIntraInterferenceUniqueOccurrences().size() == 0)
            {
               CPW.print(Network.getInterInterferenceUniqueOccurrences().values().stream().mapToInt(Integer::intValue).sum()); 
            }
            else
            {
               CPW.print((Network.getInterInterferenceUniqueOccurrences().values().stream().mapToInt(Integer::intValue).sum() +Network.getIntraInterferenceUniqueOccurrences().values().stream().mapToInt(Integer::intValue).sum()));
            }
         }
         CPW.print(", ");
         
         // Count of longest intereference(s)
         if(Network.getInterInterferenceUniqueOccurrences().size() == 0)
         {
            if ( Network.getIntraInterferenceUniqueOccurrences().size() == 0)
            {
               CPW.print("0");
            }
            else
            {
               CPW.print(Network.getIntraInterferenceUniqueOccurrences().lastEntry().getValue());
            }
         }
         else 
         {
            if ( Network.getIntraInterferenceUniqueOccurrences().size() == 0)
            {
               CPW.print(Network.getIntraInterferenceUniqueOccurrences().lastEntry().getValue()); 
            }
            else
            {
               int temp1 = Network.getInterInterferenceUniqueOccurrences().lastEntry().getValue();
               int temp2 = Network.getIntraInterferenceUniqueOccurrences().lastEntry().getValue();
         
               if( Network.getInterInterferenceUniqueOccurrences().lastEntry().getKey() == Network.getIntraInterferenceUniqueOccurrences().lastEntry().getKey())
               {
                  CPW.print(temp1+temp2);
               }else if (Network.getInterInterferenceUniqueOccurrences().lastEntry().getKey() > Network.getIntraInterferenceUniqueOccurrences().lastEntry().getKey())
               { 
                  CPW.print(temp1);
               }else
               {
                  CPW.print(temp2);
               }
               
            }
         }
         CPW.print(", ");
         
         
         //Length of longest Intermolecular only (=L)
         if(Network.getInterInterferenceUniqueOccurrences().size()>0)
         {
            CPW.print(Network.getInterInterferenceUniqueOccurrences().lastKey());
         } else CPW.print("0");
         CPW.print(", ");
         
         //Count of Intermolecular only
         if(Network.getInterInterferenceUniqueOccurrences().size()>0)
         {
            CPW.print(Network.getInterInterferenceUniqueOccurrences().values().stream().mapToInt(Integer::intValue).sum());
         } else CPW.print("0");
         CPW.print(", ");
         
         //Count of longest intermolecular interference
         if(Network.getInterInterferenceUniqueOccurrences().size() >0)
         {
            CPW.print(Network.getInterInterferenceUniqueOccurrences().lastEntry().getValue());
         }else CPW.print("0");
         CPW.print(", ");
         
         // Length of longest intramolecular
         if( Network.getIntraInterferenceUniqueOccurrences().size() > 0 )
         {
            CPW.print(Network.getIntraInterferenceUniqueOccurrences().lastKey());
         } else {CPW.print("less than SLC");};
         CPW.print(", ");
         
         //Count of intramolecular unique occurrences
         if(Network.getIntraInterferenceUniqueOccurrences().size()>0)
         {
            CPW.print(Network.getIntraInterferenceUniqueOccurrences().values().stream().mapToInt(Integer::intValue).sum());
         }else CPW.print("0");
         CPW.print(", ");
         
         //Count of longest intramolecular unique occurrences
         
         if(Network.getIntraInterferenceUniqueOccurrences().size()>0)
         {
            CPW.print(Network.getIntraInterferenceUniqueOccurrences().lastEntry().getValue());
         }else CPW.print("0");

         CPW.close();
      }
      
      FileWriter Rfilewriter = new FileWriter( ReportFilePath );
      BufferedWriter Rbw = new BufferedWriter (Rfilewriter);
      PrintWriter RPW = new PrintWriter (Rbw);
      
      RPW.println("//Report file generated by DevPro. Version: " + version + "; Run time: " + runDate);
      RPW.println();
       
      RPW.println("***************");
      RPW.println("Input Oligo-Set");
      RPW.println("***************");
      RPW.println();
      Outputs.exportIGI( RPW, Network );
	  RPW.println("***************");
	  RPW.println();
	  
      RPW.close();
	  
	  if (ScoresFilePath != "disabled")
	  {
		FileWriter Sfilewriter = new FileWriter( ScoresFilePath );
		BufferedWriter Sbw = new BufferedWriter (Sfilewriter);
		PrintWriter SPW = new PrintWriter (Sbw);

		SPW.println("//File generated by DevPro. Version: " + version + "; Run time: " + runDate);
		Outputs.exportUnformatedScores(SPW, Network);
		SPW.close();
	  }
	}
   
   public static void readArguments ( String[] Iargs )  // Incoming-ARGumentS 
   {
      // ****************************************************
      // Set default values for settings local to this method
      // ****************************************************
      
      MTout.setDebug(false);      //Set Debug text to be active (true) or inactive (false)
      MTout.setVerbose(false);      //Set Verbose text to be active (true) or inactive (false)
      PFilePath = "DP-parameters.txt";      // I: Parameters-File-Path
      
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
            System.out.println("\t"+"::Set parameters file to [filepath]");
            System.out.println("\t"+"::Default parameters file '"+ PFilePath +"'");            
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
            PFilePath = Iargs[i+1]; // accept the next argument as the input file
            System.out.println("Using parameters file: " + PFilePath); 
         }
      }
   }
   
   public static void importSettings( String PFilePath ) throws Exception
   {
   
      //MTout.log("Importing Settings for main() module from "+ PFilePath );
      
      // **************************************
      // Set Default Values for local variables
      // **************************************
      
      ReportFilePath = "DP-out-report.txt";
	  ScoresFilePath = "DP-out-scores.txt";
      
      PO = "disabled";
      PN = "disabled";
      POU = "disabled";
      PNU = "disabled";
      SOU = "disabled";
      SNU = "disabled";
      
      POB = "disabled";
      PNB = "disabled";
      POUB = "disabled";
      PNUB = "disabled";
      SOUB = "disabled";
      SNUB = "disabled";
      
      POI = "disabled";
      PNI = "disabled";
      POUI = "disabled";
      PNUI = "disabled";
      SOUI = "disabled";
      SNUI = "disabled";
      
      CFilePath = "disabled";
      
      // ************************************************
      // Read the Parameters File, looking for local variables
      // ************************************************
      
      FileReader filereader = new FileReader(PFilePath);   
      StreamTokenizer streamtokenizer = new StreamTokenizer(filereader);
      while( streamtokenizer.ttype != StreamTokenizer.TT_EOF ) // for the entire "Parameters" file
      {
         streamtokenizer.nextToken();
         if (streamtokenizer.ttype == StreamTokenizer.TT_WORD )
         {
            if (streamtokenizer.sval.equalsIgnoreCase("ReportFilePath"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  ReportFilePath = streamtokenizer.sval;
                  System.out.println("imported setting: Using ReportFilePath = " + ReportFilePath);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"ReportFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"ReportFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("ScoresFilePath"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  ScoresFilePath = streamtokenizer.sval;
                  System.out.println("imported setting: Using ScoresFilePath = " + ScoresFilePath);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"ScoresFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"ScoresFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("CFilePath"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  CFilePath = streamtokenizer.sval;
                  System.out.println("imported setting: Using CFilePath = " + CFilePath);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"CFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"CFilePath\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("PO"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  PO = streamtokenizer.sval;
                  System.out.println("imported setting: Using PO = " + PO);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"PO\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"PO\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("PN"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  PN = streamtokenizer.sval;
                  System.out.println("imported setting: Using PN = " + PN);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"PN\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"PN\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("POU"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  POU = streamtokenizer.sval;
                  System.out.println("imported setting: Using POU = " + POU);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"POU\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"POU\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("PNU"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  PNU = streamtokenizer.sval;
                  System.out.println("imported setting: Using PNU = " + PNU);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"PNU\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"PNU\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("SOU"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  SOU = streamtokenizer.sval;
                  System.out.println("imported setting: Using SOU = " + SOU);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"SOU\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"SOU\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("SNU"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  SNU = streamtokenizer.sval;
                  System.out.println("imported setting: Using SNU = " + SNU);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"SNU\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"SNU\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("POB"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  POB = streamtokenizer.sval;
                  System.out.println("imported setting: Using POB = " + POB);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"POB\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"POB\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("PNB"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  PNB = streamtokenizer.sval;
                  System.out.println("imported setting: Using PNB = " + PNB);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"PNB\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"PNB\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("POUB"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  POUB = streamtokenizer.sval;
                  System.out.println("imported setting: Using POUB = " + POUB);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"POUB\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"POUB\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("PNUB"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  PNUB = streamtokenizer.sval;
                  System.out.println("imported setting: Using PNUB = " + PNUB);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"PNUB\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"PNUB\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("SOUB"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  SOUB = streamtokenizer.sval;
                  System.out.println("imported setting: Using SOUB = " + SOUB);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"SOUB\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"SOUB\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("SNUB"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  SNUB = streamtokenizer.sval;
                  System.out.println("imported setting: Using SNUB = " + SNUB);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"SNUB\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"SNUB\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("POI"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  POI = streamtokenizer.sval;
                  System.out.println("imported setting: Using POI = " + POI);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"POI\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"POI\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("PNI"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  PNI = streamtokenizer.sval;
                  System.out.println("imported setting: Using PNI = " + PNI);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"PNI\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"PNI\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("POUI"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  POUI = streamtokenizer.sval;
                  System.out.println("imported setting: Using POUI = " + POUI);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"POUI\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"POUI\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("PNUI"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  PNUI = streamtokenizer.sval;
                  System.out.println("imported setting: Using PNUI = " + PNUI);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"PNUI\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"PNUI\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("SOUI"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  SOUI = streamtokenizer.sval;
                  System.out.println("imported setting: Using SOUI = " + SOUI);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"SOUI\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"SOUI\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("SNUI"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  SNUI = streamtokenizer.sval;
                  System.out.println("imported setting: Using SNUI = " + SNUI);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"SNUI\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"SNUI\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
         }
      }
      
   }
}

