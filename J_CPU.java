/*
 * J_CPU.java
 *
 * Created on October 21, 2005, 10:15 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

/**
 *
 * @author jwilliam
 */



class Cache
{
 // Create cache array and variables to hold cache addresses of the program and buffers
private byte cache[] = new byte[4000];//using 1/2byte to store 4 bits& instructions
//are in 8 4bit segments
private int cacheSize = 0;


public byte read(int realaddress)  {
        return cache[realaddress];
    }  
    
    // Read a value from cache
// Write a value to cache
 public void write(int realaddress, byte bt)  
 {
        cache[realaddress] = bt;
        cacheSize += 1;
 }
    
   
 public int readWord(int virtualaddress)
  {
      int realaddress= 8*virtualaddress;
      String bit_string = new String();
      for (int i = realaddress;i<realaddress+8;i++)
           // bit_string += Integer.toBinaryString((int)cache[i]);
            bit_string=bit_string.concat(Integer.toBinaryString((int)cache[i]));
      return (Integer.parseInt(bit_string, 2));
  }
 
  public void Cache(){clear();}
  
  public void writeWord(int virtualaddress,int value)
  {
       
      int realaddress= 8*virtualaddress;
      String bit_string,sub_s = new String();
      
      String zero="0";
      bit_string = Long.toBinaryString((long)value);
      while (bit_string.length() <32)bit_string=zero.concat(bit_string);//add place holder zeros to front if needed    
      for (int x=0;x<=28;x+=4)//0,4,8,12....
      { 
        sub_s=bit_string.substring(x,x+4);//make substring represent 4bit increments
	write(x/4+realaddress,Byte.parseByte(sub_s,2));//1,2,3,...put byte in disk
      }
      cacheSize+=8;
  }
   // Clear all data in the cache
    public void clear()
    {
        for (int i=0; i<4000; i++)
            cache[i] = 0;
        cacheSize = 0;
    }
    
    public int size(){return cacheSize;}
    
}
class DMA
{
private Cache cache = new Cache();
public void DMA(Cache c)
{
    cache=c;
}
public Memory readProcessFromCache(PCB p, Memory m)
{return m;}//Do later, you know where to rewrite instructions but not result data
public Cache writeProcessToCache(PCB p,Memory m)
{
       
       cache.clear();
       int c=0;
       int mem_start=p.init_mem_loc;
       int mem_end=p.final_mem_loc;
       
       
       
        int k = m.getKey();
       for (int i=mem_start;i<mem_end;i++)//make sure PCB jobsize includes instructions
       //and data
       {cache.write(c++,m.get(i,k));}
       m.returnKey(k);
           
       return cache;
}
}// End Class DMA

    


public class J_CPU{
//Cache holds one program at a time
//runJob method fetches and decodes until cache is empty
//
    private long userReg[] = new long[16]; // 16 general purpose 32-bit registers
                                           // userReg[0] = accumulator
                                           // userReg[1] = zero register
    private int pc = 0;                    // program counter or cache virtual index
    
    // Instruction register variables
    private byte[]instReg = new byte[8];  
    private int itype = 0;                 // Instruction type
    private int opcode = 0;                // Op code
    private int reg1 = 0;                  // Register 1
    private int reg2 = 0;                  // Register 2
    private int reg3 = 0;                  // Register 3
    private int address16 = 0;             // 16-bit address
    private int address24 = 0;             // 24-bit address

    private Cache cache= new Cache();
    private DMA dma = new DMA();

    private int job_size = 0;
    private int baseAddress=0;
    //WTF??
    /*
    int baseAddress = 0;
    int inputBufferBaseAddress = 0;
    int outputBufferBaseAddress = 0;
     */
    
   
public void J_CPU()
{
    dma.DMA(cache);
}	


 
 
 
 private void fetch()
    {
        int realindex=pc*8;
        for(int i=0;i<8;i++)instReg[i]=0;
        for(int i=0;i<8;i++)
            instReg[i]=cache.read(realindex+i);
        pc++;               
		       
    }
 // Decode instruction. Every possible field of the 4 instruction formats are decoded, whether
 // they are valid or not. The Execute method will pick the appropriate fields using the opcode
 private void decode()
    {
      String b_strg= new String();
      String b_strg2= new String();
        //byte from array to integer to bitstring to smaller bitstring to integer
      b_strg = Integer.toBinaryString((int)instReg[0]);
      while (b_strg.length() <4)b_strg="0".concat(b_strg);
      itype     = Integer.parseInt(b_strg.substring(0, 2),2);
      //add second 2bits of first string with 4bits of next string
      b_strg2=Integer.toBinaryString((int)instReg[1]);
      while (b_strg2.length() <4)b_strg2="0".concat(b_strg2);
      opcode = 
      Integer.parseInt(b_strg.substring(2).concat(b_strg2), 2);
      
      reg1 = Integer.parseInt(Integer.toBinaryString((int)instReg[2]),2);
      reg2 = Integer.parseInt(Integer.toBinaryString((int)instReg[3]),2);
      reg3 = Integer.parseInt(Integer.toBinaryString((int)instReg[4]),2);
      
//Address 16
      b_strg= new String();
      b_strg2= new String();
      
      
      for(int i = 4;i<8;i++)
      {
          b_strg=Integer.toBinaryString((int)instReg[i]);
          while (b_strg.length() <4)b_strg="0".concat(b_strg);
          b_strg2= b_strg2.concat(b_strg);
      }
      address16 = (Integer.parseInt(b_strg2, 2));
      
      
 //Address 24     
      b_strg= new String();
      b_strg2= new String();
      
      
      for(int i = 2;i<8;i++)
      {
          b_strg=Integer.toBinaryString((int)instReg[i]);
          while (b_strg.length() <4)b_strg="0".concat(b_strg);
          b_strg2=b_strg2.concat(b_strg);
      }
      address24 = (Integer.parseInt(b_strg2, 2));
	
    }
 // Execute single decoded instruction. Returns 0 if HALT is executed and 1 otherwise
 private boolean execute()
 //Use same methods but BEWARE his cache stores 32bits & yours 4
 //and check out his addresses.
 
 //IMPORTANT
 //every cache read in cpu execution is a read word
 //with a translated index
 
 //If you are reading or writing a word than you are also using a 
 //a virtual index that needs to be translated to a real index so
 //maybe do index translations with word reads/writes
    {
        switch(opcode)
        {
            case 0x00: // RD I/O	Reads content of I/P buffer into an accumulator
                if (reg2==0)  
                {
                  
                  userReg[reg1] = cache.readWord(address16);
                  System.out.println(" RD value: "+userReg[reg1]+" from address:"+address16+
                          " to register #"+reg1);
                }
                    
		else
                {
                    userReg[reg1] = cache.readWord((int)userReg[reg2]);
                    System.out.println(" RD value: "+userReg[reg1]+" from register #"+reg2+
                            " to register #"+reg1);
                }
		
                //stats.addIoToCount(process);
		break;
            case 0x01: // WR I/O	Writes the content of accumulator into O/P buffer
		if (reg2==0) 
                {
                    cache.writeWord(address16,(int)userReg[reg1]);
                    System.out.println(" WR value: "+userReg[reg1]+" from register #"+reg1+
                            " to address: "+address16);
                }
		else
                {
                    cache.writeWord((int)userReg[reg2],(int)userReg[reg1]);
                    System.out.println(" WR value: "+userReg[reg1]+" from register #"+reg1+
                            " to register #"+reg2);
                }
                //stats.addIoToCount(process);
		break;
            case 0x02: // ST I          Stores content of a reg.  into an address
            {
                cache.writeWord((int)userReg[reg2]+address16,(int)userReg[reg1]);
                System.out.println(" ST value: "+userReg[reg1]+" from register #"+reg1+
                        " to address: (base)"+userReg[reg2]+" + (offset)"+address16);
               
            }
                //stats.addIoToCount(process);
		break;
            case 0x03: // LW I          Loads the content of an address into a reg.
            {
		userReg[reg2] = cache.readWord((int)userReg[reg1] + address16);
                System.out.println
                (" LW value: "+userReg[reg2]+" from address: (base)"+userReg[reg1]+
                 " + (offset)"+address16+" to register #"+reg2);
            }
                //stats.addIoToCount(process);
		break; 
        // immediate operations
            case 0x0B: // MOVI I	Transfers address/data directly into a register
            {
                userReg[reg2] = address16;
                System.out.println(" MOVI address value: "+address16+" to register #"+ reg2);
            }
		break;
            case 0x0C: // ADDI I	Adds a data directly to the content of a register
            {
                userReg[reg2] += address16;
                System.out.println(" ADDI value: "+address16+" to register #"+ reg2);
            }
		break;
            case 0x0D: // MULI I	Multiplies a data directly to the content of a register
            {
		userReg[reg2] *= address16;
                System.out.println(" MULTI  value: "+address16+" by register #"+ reg2);
            }
		break;
            case 0x0E: // DIVI I	Divides a data directly to the content of a register
            {
		if (address16 != 0)userReg[reg2] /= address16;
                System.out.println(" DIVI value: "+address16+" by register #"+ reg2);
            }
		break;
            case 0x0F: // LDI I         Loads a data/address directly to the content of a register
            {
                userReg[reg2] = address16;
                System.out.println(" LDI value: "+address16+" to register #"+ reg2);
            }
		break;
            case 0x11: // SLTI I	Sets the D-reg to 1 if  first S-reg is less than a data
            {
		if (userReg[reg1] < address16)userReg[reg2] = 1;
                System.out.println( "SLTI register #"+reg2+ " to 1 if register #"+reg1+" < value: "
                        +address16);
            }
		break;
        // branch operations
            case 0x15: // BEQ I         Branches to an address when content of B-reg = D-reg
            {
                if (userReg[reg1] == userReg[reg2])pc = address16;
                System.out.println(" BEQ branches pc to  address #"+address16+"if value@register # "+
                        reg1+"("+userReg[reg1]+") = value@register #"+reg2+"("+userReg[reg2]+")");
            }	
                break;
            case 0x16: // BNE I         Branches to an address when content of B-reg <> D-reg
            {
                if (userReg[reg1] != userReg[reg2])pc = address16;
                System.out.println(" BNE branches pc to  address #"+address16+"if value@register # "+
                  reg1+"("+userReg[reg1]+") != value@register #"+reg2+"("+userReg[reg2]+")");
		
            }
            break;
            case 0x17: // BEZ I         Branches to an address when content of D-reg = 0
            {if (userReg[reg1] == 0)pc = address16;
             System.out.println(" BEZ branches pc to address #"+address16+"if value@register # "+
             reg1+"("+userReg[reg1]+") = 0");
            }
		break;
            case 0x18: // BNZ I         Branches to an address when content of B-reg <> 0
            {
                if (userReg[reg1] != 0)pc = address16;
                 System.out.println(" BNZ branches pc to address #"+address16+"if value@register # "+
                 reg1+"("+userReg[reg1]+") != 0");
            }
		break;
            case 0x19: // BGZ I         Branches to an address when content of B-reg > 0
            {
                if (userReg[reg1] > 0)pc = address16;
                System.out.println(" BGZ branches pc to address #"+address16+"if value@register # "+
                 reg1+"("+userReg[reg1]+") > 0");
            }
		break;
            case 0x1A: // BLZ I         Branches to an address when content of B-reg < 0
            {
                if (userReg[reg1] < 0) pc = address16;
                System.out.println(" BLZ branches pc to address #"+address16+"if value@register # "+
                 reg1+"("+userReg[reg1]+") < 0");
            }
		break;
        // arithmetic operations on registers
            case 0x04: // MOV R         Transfers the content of one register into another
            {
                userReg[reg1] = userReg[reg2];
                System.out.println(" MOV moves value@register #"+reg2+"("+userReg[reg2]+") to register #"+
                reg1);
            }
		break;
            case 0x05: // ADD R         Adds content of two S-regs into D-reg
            {
                userReg[reg3] = userReg[reg1] + userReg[reg2];
                System.out.println(" ADD adds value@register #"+reg2+"("+userReg[reg2]+") to value@register #"
                +reg1+"("+userReg[reg1]+") and stores value (" +userReg[reg3]+")  @register #"+reg3);
                        
            }
		break;
            case 0x06: // SUB R         Subtracts content of two S-regs into D-reg
            {
                userReg[reg3] = userReg[reg1] - userReg[reg2];
                System.out.println(" SUB subtracts value@register #"+reg2+"("+userReg[reg2]+") from value@register #"
                +reg1+"("+userReg[reg1]+") and stores value (" +userReg[reg3]+") @register #"+reg3);
            }
		break;
            case 0x07: // MUL R         Multiplies content of two S-regs into D-reg
            {
                userReg[reg3] = userReg[reg1] * userReg[reg2];
                 System.out.println(" MUL multiplies value@register #"+reg2+"("+userReg[reg2]+") with value@register #"
                +reg1+"("+userReg[reg1]+") and stores value("+userReg[reg3]+") @register #"+reg3);
            }
		break;
            case 0x08: // DIV R         Divides content of two S-regs into D-reg
            {
		if (userReg[reg2] != 0)userReg[reg3] = userReg[reg1] / userReg[reg2];
                System.out.println(" DIV divides value@register #"+reg1+"("+userReg[reg1]+") by value@register #"
                +reg2+"("+userReg[reg2]+") and stores value("+userReg[reg3]+") @register #"+reg3);
            }
            break;
            case 0x09: // AND R         Logical AND of two S-regs into D-reg
            {
                userReg[reg3] = userReg[reg1] & userReg[reg2];
                System.out.println(" AND ands value@register #"+reg1+"("+userReg[reg1]+") with value@register #"
                +reg2+"("+userReg[reg2]+") and stores value("+userReg[reg3]+") @register #"+reg3);
            }
		break;
            case 0x0A: // OR R          Logical OR of two S-regs into D-reg
            {
		userReg[reg3] = userReg[reg1] | userReg[reg2];
                System.out.println(" OR ors value@register #"+reg1+"("+userReg[reg1]+") with value@register #"
                +reg2+"("+userReg[reg2]+") and stores value("+userReg[reg3]+") @register #"+reg3);
            }
		break;
            case 0x10: // SLT R         Sets the D-reg to 1 if  first S-reg is less than second S-reg
            {
                if (userReg[reg1] < userReg[reg2])userReg[reg3] = 1;
                System.out.println(" SLT compares value@register #"+reg1+"("+userReg[reg1]+") with value@register #"
                +reg2+"("+userReg[reg2]+") and stores value("+userReg[reg3]+") @register #"+reg3);
            }
		break;
            case 0x12: // HLT J         Logical end of program
            {
                System.out.println(" HLT ends program!");
                return false;
            }
            case 0x14: // JMP J         Jumps to a specified location
            {
                
                pc = address24;
                System.out.println(" JMP jumps pc to address: "+address24);
            }
		break;
            case 0x13: // NOP           Does nothing and moves to next instruction
            {
                System.out.println(" NOP do nothing & move to next instruction");
                break;
            }
        }
        return true;
    }



public Memory runJob(Memory m,PCB p)
	{
        pc = 0;
        cache = dma.writeProcessToCache(p, m);//Reads process from RAM into cache

	job_size=p.job_size;
	// Set process status to "running"
        do {
            fetch();//get instruction from cache
            decode();//break instruction down
	} while (execute());//execute& terminate loop when cache provides 0000instr
		// Writes output back to RAM from cache
      // Set process status to "terminated"
        m=dma.readProcessFromCache(p, m);//Writes output back to RAM from cache
        cache.clear();
        return m;
	  }

}  

//end JKW_CPU class
