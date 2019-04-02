import java.io.* ;
import BoiseState.* ;
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
   private static final String version = "Build 1.6; 2019-04-01";
   
   private static String PFilePath; // Parameters-File-Path

   private static String ReportFilePath;
   
   private static String pathACS;
   private static String pathECS;
   private static String pathAUS;
   private static String pathEUS;
   private static String pathAUSstruct;
   private static String pathEUSstruct;
   
   private static String pathACB;
   private static String pathECB;
   private static String pathAUB;
   private static String pathEUB;
   private static String pathAUBstruct;
   private static String pathEUBstruct;
   
   private static String pathACI;
   private static String pathECI;
   private static String pathAUI;
   private static String pathEUI;
   private static String pathAUIstruct;
   private static String pathEUIstruct;
   
   private static String CFilePath;
   
      
   //**********************************
   //main method for the build8 program
   //**********************************
   
	public static void main(String[] args) throws Exception
	{
      
      System.out.println();
      System.out.println("********************************************************");  
      System.out.println("Device Profiler (DevPro) " + version );      
      System.out.println("Program for identifying complements in a given a device");
      System.out.println("Written by Mike Tobiason, Boise State University, 2019");
      System.out.println("********************************************************"); 
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
    
      System.out.println("*******************");
      System.out.println("Network Information");
      System.out.println("*******************");
      System.out.println();

      Outputs.printIGI(Network);

      // ***********************************************
      // Print the Structural Profiles for the device
      // ***********************************************

      System.out.println();
      System.out.println("**********************************");
      System.out.println("Summary of Structures (Complete)");
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
      

      // Export dp.out.Structures.Profile.Intra
      File tempFile = new File("DevPro-Output/"); 
      tempFile.mkdirs();
      
      //Nomenculture for output files: A-> Intramolecular E-> Intermolecular, B-> Baseline, I-> Interference, S -> All Structures(Both int & baseline), .structures, .profile
      
      if(!pathACS.equals("disabled"))
      {
         FileWriter SPIntrafilewriter = new FileWriter( "DevPro-Output/"+pathACS);
         BufferedWriter SPIntrabw = new BufferedWriter (SPIntrafilewriter);
         PrintWriter SPIntraPW = new PrintWriter (SPIntrabw);
    
         SPIntraPW.println("//Intramolecular structural profile identified by DevPro.jar on " + runDate );
         SPIntraPW.println("Complement Length (Base-Pairs), Number of Complements");
         Network.getIntraCompleteOccurrences().entrySet().stream().forEach( e -> SPIntraPW.println(e.getKey()+", "+e.getValue()));
         SPIntraPW.close();
      }
      
      
      // Export dp.out.Structures.Profile.Inter
      
      if(!pathECS.equals("disabled"))
      {
         FileWriter SPInterfilewriter = new FileWriter( "DevPro-Output/" + pathECS );
         BufferedWriter SPInterbw = new BufferedWriter (SPInterfilewriter);
         PrintWriter SPInterPW = new PrintWriter (SPInterbw);
         
         SPInterPW.println("//Intermolecular structural profile identified by DevPro.jar on " + runDate );
         SPInterPW.println("Complement Length (Base-Pairs), Number of Complements");
         Network.getInterCompleteOccurrences().entrySet().stream().forEach( e -> SPInterPW.println(e.getKey()+", "+e.getValue()));
         SPInterPW.close();
      }

      // Export dp.out.Structures.Unique.Profile.Intra
      
      if(!pathAUS.equals("disabled"))
      {
         FileWriter SUPIntrafilewriter = new FileWriter( "DevPro-Output/" + pathAUS );
         BufferedWriter SUPIntrabw = new BufferedWriter (SUPIntrafilewriter);
         PrintWriter SUPIntraPW = new PrintWriter (SUPIntrabw);
    
         SUPIntraPW.println("//Intramolecular structural profile of unique complements identified by DevPro.jar on " + runDate );
         SUPIntraPW.println("Complement Length (Base-Pairs), Number of Complements");
         Network.getIntraUniqueOccurrences().entrySet().stream().forEach( e -> SUPIntraPW.println(e.getKey()+", "+e.getValue()));
         SUPIntraPW.close();
      }
      
      // Export dp.out.Structures.Unique.Profile.Inter
      
      if(!pathEUS.equals("disabled"))
      {
         FileWriter SUPInterfilewriter = new FileWriter( "DevPro-Output/" + pathEUS );
         BufferedWriter SUPInterbw = new BufferedWriter (SUPInterfilewriter);
         PrintWriter SUPInterPW = new PrintWriter (SUPInterbw);
    
         SUPInterPW.println("//Intermolecular structural profile of unique complements identified by DevPro.jar on " + runDate );
         SUPInterPW.println("Complement Length (Base-Pairs), Number of Complements");
         Network.getInterUniqueOccurrences().entrySet().stream().forEach( e -> SUPInterPW.println(e.getKey()+", "+e.getValue()));
         SUPInterPW.close();
      }
      
      // Export dp.out.Structures.Unique.Complements.Intra
      
      if(!pathAUSstruct.equals("disabled"))
      {
      FileWriter SUCIntrafilewriter = new FileWriter( "DevPro-Output/" + pathAUSstruct );
      BufferedWriter SUCIntrabw = new BufferedWriter (SUCIntrafilewriter);
      PrintWriter SUCIntraPW = new PrintWriter (SUCIntrabw);
 
      SUCIntraPW.println("//Unique intramolecular structures(complements) identified by DevPro.jar on " + runDate );
      SUCIntraPW.println("Length (bp), Strand #1, index of 1st base in Strand #1, Sequence #1, Strand #2, index of 1st base in Strand #2, Sequence #2");
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
      
      if(!pathEUSstruct.equals("disabled"))
      {
      FileWriter SUCInterfilewriter = new FileWriter( "DevPro-Output/"+pathEUSstruct );
      BufferedWriter SUCInterbw = new BufferedWriter (SUCInterfilewriter);
      PrintWriter SUCInterPW = new PrintWriter (SUCInterbw);
 
      SUCInterPW.println("//Unique intermolecular structures(complements) identified by DevPro.jar on " + runDate );
      SUCInterPW.println("Length (bp), Strand #1, index of 1st base in Strand #1, Sequence #1, Strand #2, index of 1st base in Strand #2, Sequence #2");
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

      if(!pathACB.equals("disabled"))
      {
         FileWriter BSPIntrafilewriter = new FileWriter( "DevPro-Output/"+pathACB);
         BufferedWriter BSPIntrabw = new BufferedWriter (BSPIntrafilewriter);
         PrintWriter BSPIntraPW = new PrintWriter (BSPIntrabw);
    
         BSPIntraPW.println("//Intramolecular baseline structural profile identified by DevPro.jar on " + runDate );
         BSPIntraPW.println("Complement Length (Base-Pairs), Number of Complements");
         Network.getIntraBaselineCompleteOccurrences().entrySet().stream().forEach( e -> BSPIntraPW.println(e.getKey()+", "+e.getValue()));
         BSPIntraPW.close();
      }
      
      // Export dp.out.BaselineStructures.Profile.Inter  

      if(!pathECB.equals("disabled"))
      {
         FileWriter BSPInterfilewriter = new FileWriter( "DevPro-Output/" + pathECB );
         BufferedWriter BSPInterbw = new BufferedWriter (BSPInterfilewriter);
         PrintWriter BSPInterPW = new PrintWriter (BSPInterbw);
    
         BSPInterPW.println("//Intermolecular baseline structural profile identified by DevPro.jar on " + runDate );
         BSPInterPW.println("Complement Length (Base-Pairs), Number of Complements");
         Network.getInterBaselineCompleteOccurrences().entrySet().stream().forEach( e -> BSPInterPW.println(e.getKey()+", "+e.getValue()));
         BSPInterPW.close();
      }   
     
      // Export dp.out.BaselineStructures.Unique.Profile.Intra
      
      if(!pathAUB.equals("disabled"))
      {
         FileWriter BSUPIntrafilewriter = new FileWriter( "DevPro-Output/"+pathAUB );
         BufferedWriter BSUPIntrabw = new BufferedWriter (BSUPIntrafilewriter);
         PrintWriter BSUPIntraPW = new PrintWriter (BSUPIntrabw);
    
         BSUPIntraPW.println("//Intramolecular baseline structural profile of unique complements identified by DevPro.jar on " + runDate );
         BSUPIntraPW.println("Complement Length (Base-Pairs), Number of Complements");
         Network.getIntraBaselineUniqueOccurrences().entrySet().stream().forEach( e -> BSUPIntraPW.println(e.getKey()+", "+e.getValue()));
         BSUPIntraPW.close();
      }
      
      // Export dp.out.BaselineStructures.Unique.Profile.Inter
      
      if(!pathEUB.equals("disabled"))
      {
         FileWriter BSUPInterfilewriter = new FileWriter( "DevPro-Output/" + pathEUB );
         BufferedWriter BSUPInterbw = new BufferedWriter (BSUPInterfilewriter);
         PrintWriter BSUPInterPW = new PrintWriter (BSUPInterbw);
    
         BSUPInterPW.println("//Intermolecular baseline structural profile of unique complements identified by DevPro.jar on " + runDate );
         BSUPInterPW.println("Complement Length (Base-Pairs), Number of Complements");
         Network.getInterBaselineUniqueOccurrences().entrySet().stream().forEach( e -> BSUPInterPW.println(e.getKey()+", "+e.getValue()));
         BSUPInterPW.close();
      }
      
      // Export dp.out.BaselineStructures.Unique.Complements.Intra
      
      if(!pathAUBstruct.equals("disabled"))
      {
      FileWriter BSUCIntrafilewriter = new FileWriter( "DevPro-Output/" + pathAUBstruct );
      BufferedWriter BSUCIntrabw = new BufferedWriter (BSUCIntrafilewriter);
      PrintWriter BSUCIntraPW = new PrintWriter (BSUCIntrabw);
 
      BSUCIntraPW.println("//Unique intramolecular baseline structures(complements) identified by DevPro.jar on " + runDate );
      BSUCIntraPW.println("Length (bp), Strand #1, index of 1st base in Strand #1, Sequence #1, Strand #2, index of 1st base in Strand #2, Sequence #2");
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
      
      if(!pathEUBstruct.equals("disabled"))
      {
      FileWriter BSUCInterfilewriter = new FileWriter( "DevPro-Output/" + pathEUBstruct );
      BufferedWriter BSUCInterbw = new BufferedWriter (BSUCInterfilewriter);
      PrintWriter BSUCInterPW = new PrintWriter (BSUCInterbw);
 
      BSUCInterPW.println("//Unique intramolecular baseline structures(complements) identified by DevPro.jar on " + runDate );
      BSUCInterPW.println("Length (bp), Strand #1, index of 1st base in Strand #1, Sequence #1, Strand #2, index of 1st base in Strand #2, Sequence #2");
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
      
      if(!pathACI.equals("disabled"))
      { 
      FileWriter ISPIntrafilewriter = new FileWriter( "DevPro-Output/" + pathACI );
      BufferedWriter ISPIntrabw = new BufferedWriter (ISPIntrafilewriter);
      PrintWriter ISPIntraPW = new PrintWriter (ISPIntrabw);
 
      ISPIntraPW.println("//Intramolecular interference structural profile identified by DevPro.jar on " + runDate );
      ISPIntraPW.println("Complement Length (Base-Pairs), Number of Complements");
      Network.getIntraInterferenceCompleteOccurrences().entrySet().stream().forEach( e -> ISPIntraPW.println(e.getKey()+", "+e.getValue()));
      ISPIntraPW.close();
      }
      
      // Export dp.out.InterferenceStructures.Profile.Inter
      
      if(!pathECI.equals("disabled"))
      { 
      FileWriter ISPInterfilewriter = new FileWriter( "DevPro-Output/"+ pathECI );
      BufferedWriter ISPInterbw = new BufferedWriter (ISPInterfilewriter);
      PrintWriter ISPInterPW = new PrintWriter (ISPInterbw);
 
      ISPInterPW.println("//Intermolecular interference structural profile identified by DevPro.jar on " + runDate );
      ISPInterPW.println("Complement Length (Base-Pairs), Number of Complements");
      Network.getInterInterferenceCompleteOccurrences().entrySet().stream().forEach( e -> ISPInterPW.println(e.getKey()+", "+e.getValue()));
      ISPInterPW.close();
      }
      
      // Export dp.out.InterferenceStructures.Unique.Profile.Intra
      
      if(!pathAUI.equals("disabled"))
      { 
      FileWriter ISUPIntrafilewriter = new FileWriter( "DevPro-Output/" + pathAUI );
      BufferedWriter ISUPIntrabw = new BufferedWriter (ISUPIntrafilewriter);
      PrintWriter ISUPIntraPW = new PrintWriter (ISUPIntrabw);
 
      ISUPIntraPW.println("//Profile-Summary of unique intramolecular interference structures identified by DevPro.jar on " + runDate );
      ISUPIntraPW.println("Complement Length (Base-Pairs), Number of Complements");
      Network.getIntraInterferenceUniqueOccurrences().entrySet().stream().forEach( e -> ISUPIntraPW.println(e.getKey()+", "+e.getValue()));
      ISUPIntraPW.close();
      }
      
      // Export dp.out.InterferenceStructures.Unique.Profile.Inter
      
      if(!pathEUI.equals("disabled"))
      { 
      FileWriter ISUPInterfilewriter = new FileWriter( "DevPro-Output/" +pathEUI );
      BufferedWriter ISUPInterbw = new BufferedWriter (ISUPInterfilewriter);
      PrintWriter ISUPInterPW = new PrintWriter (ISUPInterbw);
 
      ISUPInterPW.println("//Profile-Summary of unique intermolecular interference structures identified by DevPro.jar on " + runDate );
      ISUPInterPW.println("Complement Length (Base-Pairs), Number of Complements");
      Network.getInterInterferenceUniqueOccurrences().entrySet().stream().forEach( e -> ISUPInterPW.println(e.getKey()+", "+e.getValue()));
      ISUPInterPW.close();
      }
      
      // Export dp.out.InterferenceStructures.Unique.Complements.Intra
      
      if(!pathAUIstruct.equals("disabled"))
      { 
      FileWriter ISUCIntrafilewriter = new FileWriter( "DevPro-Output/" +pathAUIstruct );
      BufferedWriter ISUCIntrabw = new BufferedWriter (ISUCIntrafilewriter);
      PrintWriter ISUCIntraPW = new PrintWriter (ISUCIntrabw);
 
      ISUCIntraPW.println("//Identity and location of unique intramolecular interference structures identified by DevPro.jar on " + runDate );
      ISUCIntraPW.println("Length (bp), Strand #1, index of 1st base in Strand #1, Sequence #1, Strand #2, index of 1st base in Strand #2, Sequence #2");
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
      
      if(!pathEUIstruct.equals("disabled"))
      {
      FileWriter ISUCInterfilewriter = new FileWriter( "DevPro-Output/"+pathEUIstruct );
      BufferedWriter ISUCInterbw = new BufferedWriter (ISUCInterfilewriter);
      PrintWriter ISUCInterPW = new PrintWriter (ISUCInterbw);
 
      ISUCInterPW.println("//Identity and location of unique intermolecular interference structures identified by DevPro.jar on " + runDate );
      ISUCInterPW.println("Length (bp), Strand #1, index of 1st base in Strand #1, Sequence #1, Strand #2, index of 1st base in Strand #2, Sequence #2");
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
         FileWriter Cfilewriter = new FileWriter( "DevPro-Output/" + CFilePath );
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
      
      
      
      FileWriter Rfilewriter = new FileWriter( "DevPro-Output/" + ReportFilePath );
      BufferedWriter Rbw = new BufferedWriter (Rfilewriter);
      PrintWriter RPW = new PrintWriter (Rbw);
      
      RPW.println("// Report Generated by Device Profiler (DevPro) on " + runDate );
      RPW.println();
       
      RPW.println("*******************");     
      RPW.println("Network Information");
      RPW.println("*******************");
      RPW.println();
      Outputs.exportNI(RPW, Network);
      
      RPW.println("******************************************");     
      RPW.println("Profile of Unique Interference Structures:");
      RPW.println("******************************************"); 
      RPW.println();
      RPW.println("Intramolecular Interferences:");
      RPW.println("-----------------------------");
      RPW.println("Complement Length (Base-Pairs), Number of Complements");
      Network.getIntraInterferenceUniqueOccurrences().entrySet().stream().forEach( e -> RPW.println(e.getKey()+", "+e.getValue()));
      RPW.println();
      RPW.println("Intermolecular Interferences:");
      RPW.println("-----------------------------");
      RPW.println("Complement Length (Base-Pairs), Number of Complements");
      Network.getInterInterferenceUniqueOccurrences().entrySet().stream().forEach( e -> RPW.println(e.getKey()+", "+e.getValue()));
      RPW.println();

      RPW.println("*************");     
      RPW.println("SeqEvo Scores");
      RPW.println("*************");
      RPW.println();
      Outputs.exportScores(RPW, Network);
         
      RPW.close();
           
      System.out.println("***********");
      System.out.println("Program End");
      System.out.println("***********");
      System.out.println();
	}
   
   public static void readArguments ( String[] Iargs )  // Incoming-ARGumentS 
   {
      // ****************************************************
      // Set default values for settings local to this method
      // ****************************************************
      
      MTout.setDebug(false);      //Set Debug text to be active (true) or inactive (false)
      MTout.setVerbose(false);      //Set Verbose text to be active (true) or inactive (false)
      PFilePath = "dp.parameters.txt";      // I: Parameters-File-Path
      
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
      
      ReportFilePath = "dp.out.report.txt";
      
      pathACS= "disabled";
      pathECS = "disabled";
      pathAUS = "disabled";
      pathEUS = "disabled";
      pathAUSstruct = "disabled";
      pathEUSstruct = "disabled";
      
      pathACB = "disabled";
      pathECB = "disabled";
      pathAUB = "disabled";
      pathEUB = "disabled";
      pathAUBstruct = "disabled";
      pathEUBstruct = "disabled";
      
      pathACI = "ACI.profile.txt";
      pathECI = "ECI.profile.txt";
      pathAUI = "AUI.profile.txt";
      pathEUI = "EUI.profile.txt";
      pathAUIstruct = "AUI.structures.txt";
      pathEUIstruct = "EUI.structures.txt";
      
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
            else if (streamtokenizer.sval.equalsIgnoreCase("pathACS"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathACS = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathACS = " + pathACS);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathACS\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathACS\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("pathECS"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathECS = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathECS = " + pathECS);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathECS\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathECS\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("pathAUS"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathAUS = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathAUS = " + pathAUS);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathAUS\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathAUS\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("pathEUS"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathEUS = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathEUS = " + pathEUS);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathEUS\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathEUS\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("pathAUSstruct"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathAUSstruct = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathAUSstruct = " + pathAUSstruct);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathAUSstruct\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathAUSstruct\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("pathEUSstruct"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathEUSstruct = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathEUSstruct = " + pathEUSstruct);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathEUSstruct\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathEUSstruct\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("pathACB"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathACB = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathACB = " + pathACB);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathACB\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathACB\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("pathECB"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathECB = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathECB = " + pathECB);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathECB\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathECB\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("pathAUB"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathAUB = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathAUB = " + pathAUB);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathAUB\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathAUB\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("pathEUB"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathEUB = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathEUB = " + pathEUB);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathEUB\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathEUB\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("pathAUBstruct"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathAUBstruct = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathAUBstruct = " + pathAUBstruct);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathAUBstruct\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathAUBstruct\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("pathEUBstruct"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathEUBstruct = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathEUBstruct = " + pathEUBstruct);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathEUBstruct\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathEUBstruct\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("pathACI"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathACI = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathACI = " + pathACI);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathACI\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathACI\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("pathECI"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathECI = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathECI = " + pathECI);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathECI\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathECI\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("pathAUI"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathAUI = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathAUI = " + pathAUI);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathAUI\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathAUI\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("pathEUI"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathEUI = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathEUI = " + pathEUI);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathEUI\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathEUI\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("pathAUIstruct"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathAUIstruct = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathAUIstruct = " + pathAUIstruct);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathAUIstruct\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathAUIstruct\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
            else if (streamtokenizer.sval.equalsIgnoreCase("pathEUIstruct"))
            {   
               streamtokenizer.nextToken(); // move to separator ( usually "=" )
               streamtokenizer.nextToken(); // move to value
               if(streamtokenizer.ttype == StreamTokenizer.TT_WORD )
               {
                  pathEUIstruct = streamtokenizer.sval;
                  System.out.println("imported setting: Using pathEUIstruct = " + pathEUIstruct);
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_NUMBER)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.nval +"\" not acceptable for \"pathEUIstruct\" in " + PFilePath); 
                  System.exit(0);                    
               }
               else if( streamtokenizer.ttype == StreamTokenizer.TT_WORD)
               {
                  System.out.println("Error:: Value \"" + streamtokenizer.sval +"\" not acceptable for \"pathEUIstruct\" in " + PFilePath); 
                  System.exit(0);                    
               }
            }
         }
      }
      
   }
}

