/*
 * PCB.java
 *
 * Created on October 21, 2005, 10:41 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

/**
 *
 * @author jwilliam
 */
public class PCB
{

  public int job_id;
  public int job_size;//number of intructions
  public int job_priority;
  public int input_buffer_size;
  public int output_buffer_size;
  public int temp_buffer_size;
  public int init_disk_loc;
  public int final_disk_loc;
  public int init_mem_loc;
  public int final_mem_loc;
  public int status;
  public void display()
  {    
      System.out.println("\nPCB Value");
      System.out.println("id: "+job_id+" size: "+job_size+" priority: "+job_priority);
      System.out.println("input buffer size: "+input_buffer_size+" output buffer size: "+output_buffer_size);
      System.out.println("temp buffer size: "+temp_buffer_size+" initial disk location: "+init_disk_loc
              +"final disk location: "+final_disk_loc);
  }
  public void PCB()
  {
  job_id=0;job_size=0;job_priority=0;input_buffer_size=0;
  output_buffer_size=0;temp_buffer_size=0;init_disk_loc=0;final_disk_loc=0;
  init_mem_loc=0;final_mem_loc=0;
  

  }
  
  }//close class PCB