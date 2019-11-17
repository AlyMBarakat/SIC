/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;



/**
 *
 * @author AlyBarakat
 */
public class Assembler {
    
    //Files declarations
    static File code = new File("srcCode.txt");
    static File intermdiate = new File("intermdiate.txt");
   
    
    //SrcLine variables
    static ArrayList label = new ArrayList();
    static ArrayList opCode = new ArrayList();
    static ArrayList operand = new ArrayList();
    static ArrayList address = new ArrayList();
    
    //SymbolTable and OpCodeTable 
    static Hashtable<String,String> symTab = new Hashtable<>();
    static Hashtable<String, String> opTab = new Hashtable<>();
    
    //record parsers
    static ArrayList record = new ArrayList();
    static ArrayList recordAddress = new ArrayList();
    
    //counters
    static int startingAdd;
    static int EndingAdd;
    static int locCtr;
    static int progLength;
    
    //flags
    static int duplicateFlag = 0;
    static int InvalidOpCodeError = 0;
    
    


    Assembler(){
        opTab.put("ADD","18");
        opTab.put("AND","40");
        opTab.put("COMP","28");
        opTab.put("DIV","24");
        opTab.put("J","3C");
        opTab.put("JEQ","30");
        opTab.put("JGT","34");
        opTab.put("JLT","38");
        opTab.put("JSUB","48");
        opTab.put("LDA","00");
        opTab.put("LDCH","50");
        opTab.put("LDL","08");
        opTab.put("LDX","04");
        opTab.put("MUL","20");
        opTab.put("OR","44");
        opTab.put("RD","D8");
        opTab.put("RSUB","4C");
        opTab.put("STA","0C");
        opTab.put("STCH","54");
        opTab.put("STL","14");
        opTab.put("STSW","E8");
        opTab.put("STX","10");
        opTab.put("SUB","1C");
        opTab.put("TD","E0");
        opTab.put("TIX","2C");
        opTab.put("WD","dC");       
    }
      
    public void pass1() throws FileNotFoundException, IOException
    {
        //i: file line pointer
        int i = 0;
        Scanner s = new Scanner(code);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("intermdiate.txt"))) {
            String x = s.nextLine();
            String[] line = x.split("\t",3);
            label.add(line[0]);
            opCode.add(line[1]);
            operand.add(line[2]);
            
            //Start line check  
            if("START".equals((String)opCode.get(i)))
            {
                startingAdd = Integer.parseInt((String)operand.get(i),16);
                locCtr = startingAdd;
                writer.write((String)label.get(i) + "\t" +(String) opCode.get(i) + "\t" +(String) operand.get(i) + "\t" + Integer.toHexString(locCtr) + "\n");
                locCtr -= 3;
                //DEBUG
                System.out.println((String) label.get(i) + "\t" + (String) opCode.get(i) + "\t" + (String) operand.get(i) + "\t" + locCtr );
            }
            else
            {
                locCtr = 0;
            }
            i++;
            
            //while file not empty
            while(s.hasNextLine())
            {     
                x = s.nextLine();
                line = x.split("\t",3);
                label.add(line[0]);
                opCode.add(line[1]);
                operand.add(line[2]);
                //check for not last line of code
                if(!"END".equals((String)opCode.get(i)))
                {

                    //temp for alpahbet check
                    String temp = (String)label.get(i);
                    //check for a valid symbol in label field
                    if(temp.matches("^[a-zA-Z]*$"))
                    {
                        //check for symbol table reocc
                        if(symTab.containsKey((String) label.get(i)))
                        {
                            duplicateFlag = 1;
                            System.out.println("ERROR: Duplicate symbol");
                            break;
                        }
                        else 
                        {
                            //add value to symTable [label,LocationCounter]
                            symTab.put((String) label.get(i), Integer.toHexString(locCtr));
                        }
                    }
                    /*
                    else if(temp.contains(",X"))
                    { 
                        //temp = label
                        temp = temp.substring(0, temp.lastIndexOf(',')-1);
                        symTab.put(temp, Integer.toHexString(locCtr));
                    }
                    */
                    //search opTable for opCode,WORD,BYTE,RESW,RESB
                    if(opTab.containsKey((String) opCode.get(i)) || "WORD".equals((String)opCode.get(i) ) )
                    {
                        locCtr += 3;    
                    }
                    else if("RESW".equals((String)opCode.get(i)))
                    {   
                        int op = Integer.parseInt((String)operand.get(i));
                        locCtr += 3 * op;
                    }
                    else if("RESB".equals((String)opCode.get(i)))
                    {
                        int op = Integer.parseInt((String)operand.get(i));
                        locCtr += op;
                    }
                    else if("BYTE".equals((String)opCode.get(i)))
                    {
                        String op = (String)operand.get(i);
                        if(op.contains("C'"))
                        {
                            locCtr += op.length()-3;
                        }
                        else if(op.contains("X'"))
                        {
                            String hex = "";
                            hex = op.substring(2, op.length()-2);  
                            locCtr += Integer.parseInt(hex,16);
                        }
                        else
                        {
                            locCtr += Integer.parseInt((String)operand.get(i));
                        }
                    } 
                    else
                    {
                        InvalidOpCodeError =  1;            
                        System.out.println("ERROR: Invalid operation code");
                        break;
                    }//End if opcode search

                    //DEBUG
                    System.out.println((String) label.get(i) + "\t" + (String) opCode.get(i) + "\t" + (String) operand.get(i) + "\t" + locCtr );

                    //Write in file(intermediate)
                    writer.write(label.get(i) + "\t" + opCode.get(i) + "\t" + operand.get(i) + "\t" + Integer.toHexString(locCtr) + "\n");
                    i++;
                }   //End if not "END"
                else
                {
                    //Write in file(intermediate)
                    writer.write(label.get(i) + "\t" + opCode.get(i) + "\t" + operand.get(i) + "\t" + Integer.toHexString(locCtr) + "\n");
                    progLength = locCtr - startingAdd;
                    System.out.println("Program length opa7 = " + progLength);      
                }   //End if "END"
            }   //End While has next line 
        }   //End try Buffer writer 
    }//End pass1
    
    
    public void pass2() throws FileNotFoundException, IOException
    {   
        //i: file line pointer
        int i = 0;
        //t: record pointer
        int t = 0;
        //obCode holder
        String holder = new String();
        
        Scanner s = new Scanner(intermdiate);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("objectProgram.txt"))) 
        {
            String x = s.nextLine();
            String[] line = x.split("\t",4);
            label.add(line[0]);
            opCode.add(line[1]);
            operand.add(line[2]);
            address.add(line[3]);
            
            //Right START record to object program
            if("START".equals((String)opCode.get(i)))
            {
                writer.write("H" + "\t");
                writer.write((String)label.get(i) + "\t");
                writer.write(decimalToHex6(startingAdd)+ "\t");
                writer.write(decimalToHex6(progLength));
                i++;
            }
                     
            while(s.hasNextLine())
            {
                
                x = s.nextLine();
                line = x.split("\t",4);
                label.add(line[0]);
                opCode.add(line[1]);
                operand.add(line[2]);
                address.add(line[3]);
                
                if(!"END".equals((String)opCode.get(i)))
                {
                    String temp = (String)operand.get(i);
                    //check for valid opCode
                    if(opTab.containsKey((String) opCode.get(i)) || temp.contains(",X"))
                    {
                        //check for symbol in operand field
                        if(temp.matches("^[a-zA-Z]*$"))
                        {
                            //check for symbol reoccurance
                            if(symTab.containsKey((String) operand.get(i)))
                            {
                                holder = opTab.get((String)opCode.get(i));
                                holder = holder.concat(symTab.get(temp));
                                System.out.println(holder);  
                            }
                            else
                            {
                                holder = opTab.get((String) opCode.get(i));
                                holder = holder.concat("0000");
                                System.out.println("ERROR: undefined symbol");
                            }
                        }
                        else if(temp.contains(",X"))
                        { 
                            holder = opTab.get((String)opCode.get(i));
                            //temp = label
                            temp = temp.substring(0, temp.lastIndexOf(','));
                            System.out.println(temp);
                            //address + 8000HEX
                            int y = Integer.parseInt(symTab.get(temp)) + 32768;
                            System.out.println(decimalToHex4(y));
                            holder = holder.concat(decimalToHex4(y));
                        }
                        else
                        {
                            holder = opTab.get((String) opCode.get(i));
                            holder = holder.concat("0000");
                            System.out.println(holder);  

                        }
                        record.add(holder);
                        recordAddress.add(hexToHex6((String)address.get(i)));
                        t++;
                    }
                    else if("WORD".equals((String)opCode.get(i)))
                    {  
                        int n = Integer.parseInt((String)operand.get(i));
                        holder = decimalToHex6(n);
                        record.add(holder);
                        recordAddress.add(hexToHex6((String)address.get(i)));
                        t++;
                        
                        //DEBUG
                        //System.out.println("WORD: " + holder + "adress = " + hexToHex6((String)address.get(i)));           
                    }
                    else if("BYTE".equals((String)opCode.get(i)))
                    {
                        
                        //check for BYTE array
                        String op = (String)operand.get(i);
                        if(op.contains("C'"))
                        {
                            holder = decimalToHex6(op.length()-3);
                            
                        }
                        else if(op.contains("X'"))
                        {
                            String hex = "";
                            hex = op.substring(2, op.length()-2);  
                            holder = decimalToHex6(Integer.parseInt(hex,16));
                        }
                        else
                        {
                            holder = decimalToHex6(Integer.parseInt((String)operand.get(i)));
                        }
                        record.add(holder);
                        recordAddress.add(hexToHex6((String)address.get(i)));
                        t++;
                        //DEBUG
                        //System.out.println("BYTE: " + holder + "adress = " + hexToHex6((String)address.get(i)));
                    }
                    //Right text record to object program
                    if("RESW".equals((String)opCode.get(i)) || "RESB".equals((String)opCode.get(i)) || t > 9)
                    {
                        try{
                            int start = Integer.parseInt((String)recordAddress.get(0) , 16);
                            int end =   Integer.parseInt((String)recordAddress.get(t-1) , 16);
                            int length = end - start;
                            writer.write("\nT" + "\t");
                            writer.write((String)recordAddress.get(0) + "\t");
                            writer.write(Integer.toHexString(length)+ "\t");
                            for(int j = 0; j < t; j++)
                            {
                                writer.write((String)record.get(j) + "\t");
                            }                                  
                        }catch(Exception e)
                        {
                          System.out.println("RESW jumped !!");
                        }
                        
                        //DEBUG
                        //System.out.println("t =   " + t +"\ti = " + i );
                        //System.out.println("record size =   " + record.size() +"\trecordAddress size = " + recordAddress.size());
                        //System.out.println(record.toString());
                        //System.out.println(recordAddress.toString());
                               
                        t = 0;
                        record = new ArrayList();
                        recordAddress = new ArrayList();
                    }
                    i++;                    
                }
                //Right END record to object program
                else
                {
                    writer.write("\nE" + "\t");
                    writer.write(decimalToHex6(startingAdd));
                    System.out.println("Symbol table = " + symTab.toString());
                } 
            }   //End While has next line
        }   //End try Buffer writer
    }   //End pass 2
    
    //Convert decimal to HEX string 6 bits
    public String decimalToHex6(int x)
    {
        String hex = Integer.toHexString(x);
        String val = "";
        for(int i = 0; i < 6-hex.length();i++)
        {
            val = val.concat("0");
        }
        val = val.concat(hex);
        return val.toUpperCase() ;
    }
    
    //Convert decimal to HEX string 6 bits
    public String decimalToHex4(int x)
    {
        String hex = Integer.toHexString(x);
        String val = "";
        for(int i = 0; i < 4-hex.length();i++)
        {
            val = val.concat("0");
        }
        val = val.concat(hex);
        return val.toUpperCase() ;
    }
    
    
    
    
    public String hexToHex6(String hex)
    {
        String val = "";
        for(int i = 0; i < 6-hex.length(); i++)
        {
            val = val.concat("0");
        }
        val = val.concat(hex);
        return val.toUpperCase();
    }  
}   //End Assembler()