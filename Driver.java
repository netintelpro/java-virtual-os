import java.io.*;
import java.util.*;

class Loader
{

 private static int mode=0;// mode determines wheter instructions or data are being stored
 private static int i=0;//i counts instructions or data or parameters
 private static int job_num=0;//job_num Counts jobs
 private static int param_state=0;
 private static int instruction_count;
 private static int disk_index=0;
 private static Disk disk= new Disk();
 private static LinkedList<PCB> job_queue = new LinkedList<PCB>();
 private static PCB pcb = new PCB();

public static Disk getDisk(){return disk;}
public static LinkedList<PCB> getJobQueue(){return job_queue;}
public static void loader() throws IOException
{
//FILE READER AND STREAM TOKENIZER
File in = new File("C:\\OS\\Program1.txt");
FileReader inputFile = new FileReader(in);
StreamTokenizer st = new StreamTokenizer(inputFile); 

//STREAM TOKENIZER SETTINGS
//IMPORTANT: These are settings for desired token format
//Must make numbers 'Chars' before making them 'wordChars' 
        
st.eolIsSignificant(false);
st.ordinaryChar('/');
st.ordinaryChars('0','9');
st.wordChars('0','9');
		  
int token =0;
while (token != StreamTokenizer.TT_EOF)
{ 
     if (token==StreamTokenizer.TT_WORD) evalWord(st.sval);
     token = st.nextToken();
}//close while
System.out.println("Loading....................................\n\n");
inputFile.close();
}//close loader()

static void evalWord(String word)
{
 String zero = new String("0");
 String s= new String();
 String sub_s= new String();
 if (word.equals("JOB"))
					 {
					      mode=0;
					      i=0;
					      param_state=0;
					      instruction_count=0;
					      pcb = new PCB();
					      pcb.init_disk_loc=disk_index;
					      											
					 } 
					 //Job Mode:Ready2read 3 consecutive values for id,size&priority
 else 
  if (word.equals("Data"))
					 {
                                              instruction_count=param_state -3;
					      if(instruction_count!=pcb.job_size)pcb.job_size=instruction_count;
   				              mode=1;
					      i=0;
					      param_state=0;
					     
                                          }
					 //Data Mode:Ready2read 3 consecutive values for buffers
					 
 else 
   if (word.equals("END"))
					 {    
					 
					 	
                                                pcb.final_disk_loc=disk_index-1;
                                                job_queue.add(pcb);
						
					 }
 else
		{        //Handle Parameters
					if (mode==0)
					{
						if(param_state==0)pcb.job_id=Integer.parseInt(word,16);
						if(param_state==1)pcb.job_size=Integer.parseInt(word,16);//dont take his word,check against real program
					                                                 //count at END
						if(param_state==2)pcb.job_priority=Integer.parseInt(word,16);
						
					

					}		 
			
					if (mode==1)
					{  //Can these values be trusted also?
					    if(param_state==0)pcb.input_buffer_size=Integer.parseInt(word,16);
					    if(param_state==1)pcb.output_buffer_size=Integer.parseInt(word,16);
					    if(param_state==2)pcb.temp_buffer_size=Integer.parseInt(word,16);
					}

					if (param_state>2)//Meaning... If phrase and parameters and have been read
					{//Write 1 32 bit instruction to Disk as 8 bytes 
					 //Will break 32 bit num into 8 bytes and load into disk
					 		
					     	s =Long.toBinaryString(Long.parseLong(word.substring(2),16));//String s represents 32bit bit string
                                                //This value is converted to a string of ASCII digits in binary (base 2) with no extra leading 0s
					  		while (s.length() !=32)s=zero.concat(s);//add place holder zeros to front if needed    
							for (int x=0;x<=28;x+=4)//0,4,8,12....
	 							{ //System.out.print("\r x: "+x);
								  sub_s=s.substring(x,x+4);//make substring represent 4bit increments
								  disk.put(x/4+disk_index,Byte.parseByte(sub_s,2));//1,2,3,...put byte in disk
	                                                        }
							disk_index +=8;
						
					 }//if close
                                        param_state++;//param_state increments to count expectations of parameters or instructions or data
					
		}//else close
					 

}//close evalWord()  

}//close class Loader()
class LongScheduler
{
private static LinkedList<PCB> readyQ= new LinkedList<PCB>();

public static void LongScheduler(){}
public static LinkedList<PCB> prioritize( LinkedList<PCB> job_queue)
{
	PCB temp = new PCB();
	int initial_size=job_queue.size();
	//BUBBLE SORT-greatest to smallest
	for (int i = 0;i<(initial_size - 1); i++)
  	{
    for (int j=0; j<initial_size-1-i; j++) 
    {
      if (job_queue.get(j+1).job_priority > job_queue.get(j).job_priority)
      {
        temp = job_queue.get(j+1);
        job_queue.set(j+1,    job_queue.get(j) );
        job_queue.set(j,temp);
      }//close if
    }//close inner loop
  	}//close outer loop
return job_queue;
}//close prioritize

public static Memory insertMemory(LinkedList<PCB> rq,Memory m,Disk d)
//finds free space in memory
//inserts programs until memory is full
//stores location
{   
    
        int i;
        int q_index=0;
        int m_start=0;
	boolean enoughSpace = true;
        try{
	int k=m.getKey();
	while(enoughSpace)
	//check 1st program on q with memory space,if too big than enoughSpace is false exit loop
	//else write program onto memory and prepare to check next program
	{
            PCB p= rq.get(q_index);//take element of ready q
            //get size of disk block
            int d_block_size=p.final_disk_loc-p.init_disk_loc;//size of block on disk
            int d_index=p.init_disk_loc;//Initial block location on disk for reading
            enoughSpace=(m.freeSpace(k))>=(d_block_size);//is there enough space on memory for disk block
            //find start index of free space on memory
            while (m.isFull(m_start))if (m_start==m.MEMORY_SIZE-1)m_start=0;else m_start++;
            //'WRAP AROUND ARRAY or 'CIRCULAR QUEUE'
            int m_incr=m_start;
            for(i=0;i<d_block_size;i++)
            {
                if( (m_incr+i)==m.MEMORY_SIZE-1)m_incr=-i;//allows 'wrap around'
                m.put(m_incr+i, d.get(d_index++),k);//make sure reading disk form right place
                
            
            }//start writing at empty memory space
            //stop writing after covering disk block size       
          
            //put memory location on pcb element
            p.final_mem_loc=m_incr+i;
            p.init_mem_loc=m_start;
            //put pcb element back onto ready q
            rq.set(q_index++,p);
	}
//hopefully while loop ends the moment condition is not met,if not u need to choose another type of loop
	readyQ=rq;
	m.returnKey(k);
        }catch(Exception e){};
	return m;
}	
public static LinkedList<PCB> updateReadyQ()
//inserts memory location stored in insertMemory into ReadyQ
//
{return readyQ;}
}// Close class Scheduler


public class Driver
{

private static LinkedList<PCB> rq= new LinkedList<PCB>();
private static LinkedList<PCB> jq = new LinkedList<PCB>();

private static Loader loader = new Loader();
private static Memory memory= new Memory();
private static Disk disk = new Disk();
private static J_CPU cpu = new J_CPU();
private static LongScheduler l_scheduler = new LongScheduler();
//the dispatcher will extract parameter data from the PCB 
//set the CPU’s PC, and other registers, before the OS calls the CPU to execute it.
//private static Dispatcher dispatcher = new Dispatcher();//

public static void waitforinterrupt(){}
public static void Driver()
{       
    try {loader.loader();}catch(Exception e){System.out.println("Error:Program File Not Found");}
	disk = loader.getDisk();
        disk.display();
	jq= loader.getJobQueue();
        for(int x=0;x<jq.size();x++)jq.get(x).display();
	rq = l_scheduler.prioritize(jq);//give pcb fields for memory location, Scheduler fills fields in.
        jq=rq;//maybe jq will be our version of central 'pcb' and will keep status of all jobs in system
        for(int x=0;x<jq.size();x++)jq.get(x).display();
        dispatch();
}
/*private static Disk returnMemorytoDisk(PCB p, Memory m, Disk d)
{
    
}
 */
public static void dispatch()
{
        PCB p = new PCB();
	do
	{
                
                //memory is filled from disk
		memory= l_scheduler.insertMemory(rq,memory,disk);//finds space in memory,inserts jobs, stores addresses
		//ready q memory fields are updated
                memory.display();
                //rq=l_scheduler.updateReadyQ();//gives readyq memory addresses
                for(int x=0;x<rq.size();x++)rq.get(x).display();
                p=rq.poll();
               //cpu updates memory memory(dispatch,fetch,decode,execute)
                System.out.println(" CPU Executing....................................\n\n");
		memory=cpu.runJob(memory,p);//executes instructions in memory until memory is empty
                //disk=returnMemorytoDisk(p,memory,disk);
                //WTF?
		//waitforinterrupt();//off specs...dont know what to do yet
	}while (rq.size()>0);
}	
public static void main(String args[])
{
    Driver();
}

}//close class Driver


	