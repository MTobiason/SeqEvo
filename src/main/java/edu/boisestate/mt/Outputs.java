package edu.boisestate.mt;
import java.io.* ;
import java.util.Date;
import java.util.Vector;

public class Outputs
{

      
   // ********************************************
   // Method for printing Decoded-Domain-Sequences
   // ********************************************

   
   public static void printIDDS( Generation IG ) // IDDS: Indexed-Decoded-Domain-Sequences, IG: Incoming-Generation
   {
      DomainPool DA = IG.getDA();
      System.out.println("------------------------------");
      System.out.println("Domain #" + "  :  " + "Name" + "  :  " + "Sequence");
      System.out.println("------------------------------");
      for(int i = 0; i < DA.tnd; i++) // iterate through all domains.
      {
         System.out.print("Domain #" + (i+1) + " : "+ DA.dn[i]+" : ");
         for(int j = 0; j < DA.dl[i]; j++) // iterate through the whole domains.
         {
            System.out.print(IG.dds[i][j]);
         }
         System.out.println(); 
      }
      System.out.println();
   }
   
      
   // ********************************************
   // Method for printing Decoded-Strand-Sequences
   // ********************************************

   public static void printIDSS( Generation IG ) // IDSS: Indexed-Decoded-Strand-Sequences, IG: Incoming-Generation
   {
      StrandPool SA = IG.getSA();
      System.out.println("------------------------------");
      System.out.println("Strand #" + "  :  " + "Name" + "  :  " + "Sequence");
      System.out.println("------------------------------");
      for(int i = 0; i < SA.tns; i++) // iterate through all strands.
      {
         System.out.print("Strand #"+ (i+1) + " : "+ SA.sn[i]+" : ");
         for(int j = 0; j < IG.getSL()[i]; j++) // iterate through bases in the strand.
         {
            System.out.print(IG.dss[i][j]);
         }
         System.out.println(); 
      }
   System.out.println();
   }
   
   // ********************************************
   // Method for printing Encoded-Domain-Sequences
   // for given generation
   // ********************************************
   
   public static void printEDS( Generation IG) // IG: Incoming-Generation
   {
      DomainPool DA = IG.getDA();
      System.out.println("Encoded domain sequences:");
      for(int i = 0; i < DA.tnd; i++) // iterate through all domains.
      {
         System.out.print("Domain #"+ (i+1) + " : "+ DA.dn[i]+" : ");
         for(int j = 0; j < DA.dl[i]; j++) // iterate through the whole domains.
         {
            System.out.print(IG.eds[i][j]);
         }
         System.out.println(); 
      }
      System.out.println();
   }
   
   // ********************************************
   // Method for printing Encoded-Strand-Sequences 
   // for given generation
   // ********************************************


   public static void printESS( Generation IG ) // IG: Incoming-Generation
   {
      DomainPool DA = IG.getDA();
      StrandPool SA = IG.getSA();
      System.out.println("Encoded strand sequences:");
      for(int i = 0; i < SA.tns; i++) // iterate through all strands.
      {
         System.out.print("Strand #"+ (i+1) + " : "+ SA.sn[i]+" : ");
         for(int j = 0; j < IG.sl[i]; j++) // iterate through bases in the strand.
         {
            System.out.print(IG.ess[i][j]);
         }
         System.out.println(); 
      }
   System.out.println();
   }
   
   // *****************************************************
   // Method for printing all scores for a given generation
   // *****************************************************
   
   public static void printScores( Generation IG ) // IG: Incoming-Generation
   {
      System.out.println("--------------------------------");
      System.out.println("Strand fitness score: " + IG.getSFS());
      System.out.println("Network fitness score: " + IG.getNFS());
      System.out.println("Total fitness score: " + IG.getTFS());
      System.out.println("--------------------------------");
      System.out.println();
   }
  
   // ********************************************
   // Method for printing all relevant information
   // for a given generation
   // ********************************************
   
   public static void printIGI ( Generation IG ) // IGI: Indexed-Generation-Information, IG: Incoming-Generation
   {
      IG.decodeDomainSequences();
      IG.decodeStrandSequences();
      Outputs.printIDDS( IG );
      Outputs.printIDSS( IG );
   }

   public static void exportIDI(PrintWriter PW, Generation IG) throws Exception // DI: Domain-Information, PW: Print-Writer, IG: Incoming-Generation 
   {
      DomainPool DA = IG.getDA();
      for(int i = 0; i < DA.tnd; i++) // iterate through all domains.
      {
         PW.print((i+1) + "\t"+ DA.dn[i] + "\t" + IG.dt[i] + "\t");
         for(int j = 0; j < DA.dl[i]; j++) // iterate through the whole domains.
         {
            PW.print(IG.dds[i][j]);
         }
         PW.println(); 
      }
      PW.println();
   }

   public static void exportIDS(PrintWriter PW, Generation IG) throws Exception // IDS: Indexed-Domain-Sequences, PW: Print-Writer, IG: Incoming-Generation 
   {
      DomainPool DA = IG.getDA();
      for(int i = 0; i < DA.tnd; i++) // iterate through all domains.
      {
         PW.print((i+1) + "\t"+ DA.dn[i] + "\t");
         for(int j = 0; j < DA.dl[i]; j++) // iterate through the whole domains.
         {
            PW.print(IG.dds[i][j]);
         }
         PW.println(); 
      }
      PW.println();
   }
   
   public static void exportDS(PrintWriter PW, Generation IG) throws Exception // DS: Domain-Sequences, PW: Print-Writer, IG: Incoming-Generation 
   {
      DomainPool DA = IG.getDA();
      for(int i = 0; i < DA.tnd; i++) // iterate through all domains.
      {
         PW.print(DA.dn[i] + "\t");
         for(int j = 0; j < DA.dl[i]; j++) // iterate through the whole domains.
         {
            PW.print(IG.dds[i][j]);
         }
         PW.println(); 
      }
      PW.println();
   }
   
   public static void exportISR( PrintWriter PW, Generation IG) throws Exception // ISR: Indexed-Strand-Recipe, PW: Print-Writer, IG: Incoming-Generation
   {     
      StrandPool SA = IG.getSA();
      for(int i = 0 ; i < SA.tns; i++)
      {
         PW.print( (i+1) + "\t" + SA.sn[i] + "\t" );
         for( int j = 0; j< SA.srl[i] ; j++)
         {
            PW.print(SA.sr[i][j] + " ");
         }
         PW.println();
      }
      PW.println();
   }
   
   public static void exportSR( PrintWriter PW, Generation IG) throws Exception // SR: Strand-Recipe, PW: Print-Writer, IG: Incoming-Generation
   {     
      StrandPool SA = IG.getSA();
      for(int i = 0 ; i < SA.tns; i++)
      {
         PW.print(SA.sn[i] + "\t" );
         for( int j = 0; j< SA.srl[i] ; j++)
         {
            PW.print(SA.sr[i][j] + " ");
         }
         PW.println();
      }
      PW.println();
   }
   
   // *********************************************
   // Method for exporting Indexed-Strand-Sequences
   // *********************************************

   public static void exportISS( PrintWriter PW, Generation IG ) throws Exception // ISS: Indexed-Strand-Sequences PW: Print-Writer, IG: Incoming-Generation
   {      
      StrandPool SA = IG.getSA();
      for(int i = 0; i < SA.tns; i++) // iterate through all strands.
      {
         PW.print( (i+1) + "\t" + SA.sn[i] + "\t");
         for(int j = 0; j < IG.sl[i]; j++) // iterate through bases in the strand.
         {
            PW.print(IG.dss[i][j]);
         }
         PW.println(); 
      }
   PW.println();
   }   
   
   // *************************************
   // Method for exporting Strand-Sequences
   // *************************************

   public static void exportSS( PrintWriter PW, Generation IG ) throws Exception // SS: Strand-Sequences PW: Print-Writer, IG: Incoming-Generation
   {      
      StrandPool SA = IG.getSA();
      for(int i = 0; i < SA.tns; i++) // iterate through all strands.
      {
         PW.print(SA.sn[i] + "\t");
         for(int j = 0; j < IG.sl[i]; j++) // iterate through bases in the strand.
         {
            PW.print(IG.dss[i][j]);
         }
         PW.println(); 
      }
   PW.println();
   }
   

   
   // ************************************************
   // Method for exporting a given generation's scores
   // ************************************************
   
   public static void exportScores( PrintWriter PW, Generation IG ) throws Exception // PW: Print-Writer, IG: Incoming-Generation
   {
      PW.println("--------------------------------");
      PW.println("Strand fitness score: " + IG.getSFS());
      PW.println("Network fitness score: " + IG.getNFS());
      PW.println("Total fitness score: " + IG.getTFS());
      PW.println("--------------------------------");
      PW.println(); 
   }
     
   // **************************************************************
   // Method for exporting a given generation's relevant information
   // **************************************************************
   
   public static void exportIGI (PrintWriter PW, Generation IG) throws Exception// GI: Generation-Information, PW: Print-Writer, IG: Incoming-Generation
   {      
      PW.println("Strand Domains:");
      PW.println("-----------------------");
      PW.println("#"+"\t"+"name"+"\t"+"domains");
      PW.println("-----------------------"); 
      exportISR( PW, IG);     
      
      PW.println("Domain Sequences: ");
      PW.println("---------------------------------");
      PW.println("#"+ "\t" +"name"+ "\t" + "bases (5' to 3')");
      PW.println("---------------------------------");
      exportIDS( PW, IG);
      
      PW.println("Strand Sequences: ");
      PW.println("---------------------------------");
      PW.println("#"+ "\t" +"name"+ "\t" + "bases (5' to 3')");
      PW.println("---------------------------------");
      exportISS( PW, IG);
      
      IG.decodeDomainSequences();
      IG.decodeStrandSequences();
      exportScores( PW, IG);
   }
   

   public static void exportNI( PrintWriter PW, Generation IG) throws Exception// NI: Netowork-Information, PW: Print-Writer, IG:Incoming-Generation
   {
      IG.decodeDomainSequences();
      IG.decodeStrandSequences();
      
      PW.println("Strand Domains:");
      PW.println("--------------------------");
      PW.println("name"+"\t"+"domains (5' to 3')");
      PW.println("--------------------------"); 
      exportSR( PW, IG);

      PW.println("Domain Sequences: ");
      PW.println("------------------------");
      PW.println("name"+ "\t" + "bases (5' to 3')");
      PW.println("------------------------");
      exportDS( PW, IG);
      
      PW.println("Strand Sequences: ");
      PW.println("------------------------");
      PW.println("name"+ "\t" + "bases (5' to 3')");
      PW.println("------------------------");
      exportSS( PW, IG);
   }
}
