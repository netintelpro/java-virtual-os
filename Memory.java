/*
 * Memory.java
 *
 * Created on October 21, 2005, 10:10 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

/**
 *
 * @author jwilliam
 */


class Disk
{
//Spec:contents of disk is 2048 words


	private static int DISK_SIZE = 16384;
        //using 1/2byte to store 4 bits& instructions
	 //are in 8 4bit segments

	public byte[]disk = new byte[DISK_SIZE];
        public boolean[] full = new boolean[DISK_SIZE];
	
	
	public void Disk()
	{for(int i=0;i<DISK_SIZE;i++){ full[i]=false;disk[i]=0;}}
	
	public byte get(int index)
	{return disk[index];}
	
	public void put(int index,byte bt)
	{disk[index]=bt;
        full[index]=true;}
        
        public void display()
        {
            System.out.println("\nDisk Content(bytes): ");
            for(int i=0;i<disk.length-8;i+=8)
            {   
                
                if (full[i])
                {
                    if ((i%32)==0)System.out.println(" ");
                    if((i%8)==0)System.out.print("   ");
                    for(int x=0;x<8;x++)
                    {
                        if (full[i])System.out.print(" "+disk[i+x]);
                    }
                    
                }
            
             }
        }

}//close class Disk

public class Memory {
    
 //Spec:Content of Memory is 1024 words(Each word is 4 bytes).
//The CPU executes the programs in Memory
//An 'address' must always be passed to Memory

public static int MEMORY_SIZE = 8192;//using 1/2byte to store 4 bits& instructions
//are in 8 4bit segments



private static int key;
private static boolean locked;


private byte[]mem = new byte[MEMORY_SIZE];
private boolean[]full = new boolean[MEMORY_SIZE];


public void Memory()
{
	key=(int)(100*Math.random())+1;
	locked=false;
	for(int i=0;i<MEMORY_SIZE;i++){full[i]= false; mem[i]=0;}
}
	
public byte get(int i,int k)
{
	if (k==key)
        {
            full[i]=false;
            return mem[i];
        }
        else return -1;
}
   
public byte peek(int i,int k)
{  
 return mem[i];
       
}
		
public void put(int i,byte bt,int k)
{
	if (k==key)
        {
            mem[i]=bt;
            full[i]=true;

        }
}	
	 
public boolean isFull(int i)
{
	 return full[i];
}
	 
public int freeSpace(int k)
{
	
   int count=0;
	for(int i=0;i<MEMORY_SIZE;i++)
		if (!(full[i]))count++;
	return count;
}  
	
public int getKey()
{
	if  (locked){ return 0;}
        else
        {
            locked=true;
            return key;
        }

}
	 
public void returnKey(int k)
{
	if (k==key)
        {
	key=(int)(100*Math.random())+1;
	locked=false;
        }
}
public void display()
        {
            System.out.println("\nMemory Content(bytes): ");
            for(int i=0;i<mem.length-8;i+=8)
            {   
                
                if (full[i])
                {
                    if ((i%32)==0)System.out.println(" ");
                    if((i%8)==0)System.out.print("   ");
                    for(int x=0;x<8;x++)
                    {
                        if (full[i])System.out.print(" "+mem[i+x]);
                    }
                    
                }
            
             }
        }

}//close class Memory
