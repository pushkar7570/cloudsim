package org.cloudbus.cloudsim.examples;


import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import utils.Constants;
import utils.DatacenterCreator;
import utils.GenerateMatrices;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class FCFS_Scheduler {

    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmList;
    private static Datacenter[] datacenter;
   
    private static List<Vm> createVM(int userId, int vms) {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<Vm> list = new LinkedList<Vm>();

        //VM Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        int mips = 250;
        long bw = 10;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        //create VMs
        Vm[] vm = new Vm[vms];

        for (int i = 0; i < vms; i++) {
            vm[i] = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
        }

        return list;
    }

    private static List<Cloudlet> createCloudlet(int userId, int cloudlets){
		// Creates a container to store Cloudlets
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		//cloudlet parameters
		long length = 1000;
		long fileSize = 300;
		long outputSize = 300;
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		
		Cloudlet[] cloudlet = new Cloudlet[cloudlets];

		for(int i=0;i<cloudlets;i++){
//			Random random = new Random(12);
//			int x = (int) (random.nextInt(100) * ((2000 - 1) + 1)) + 1;
			cloudlet[i] = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}

		return list;
	}

    public static void main(String[] args) {
        Log.printLine("Starting FCFS Scheduler...");

//        new GenerateMatrices();
//        execMatrix = GenerateMatrices.getExecMatrix();
//        commMatrix = GenerateMatrices.getCommMatrix();

        try {
            int num_user = 1;   // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            datacenter = new Datacenter[Constants.NO_OF_DATA_CENTERS];
            for (int i = 0; i < Constants.NO_OF_DATA_CENTERS; i++) {
                datacenter[i] = DatacenterCreator.createDatacenter("Datacenter_" + i);
            }

            //Third step: Create Broker
            FCFSDatacenterBroker broker = createBroker("Broker_0");
            int brokerId = broker.getId();

            //Fourth step: Create VMs and Cloudlets and send them to broker
            int vms = 9;
            int cloudlets = 200;
            vmList = createVM(brokerId, vms);
            cloudletList = createCloudlet(brokerId, cloudlets);

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            // Fifth step: Starts the simulation
            CloudSim.startSimulation();

            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            //newList.addAll(globalBroker.getBroker().getCloudletReceivedList());

            CloudSim.stopSimulation();

            printCloudletList(newList, vms);

            Log.printLine(FCFS_Scheduler.class.getName() + " finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static FCFSDatacenterBroker createBroker(String name) throws Exception {
        return new FCFSDatacenterBroker(name);
    }

    /**
     * Prints the Cloudlet objects
     *
     * @param list list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> list, int vms) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" +
                indent + "Data center ID" +
                indent + "VM ID" +
                indent + indent + "Time" +
                indent + "Start Time" +
                indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        dft.setMinimumIntegerDigits(2);
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + dft.format(cloudlet.getCloudletId()) + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + dft.format(cloudlet.getResourceId()) +
                        indent + indent + indent + dft.format(cloudlet.getVmId()) +
                        indent + indent + dft.format(cloudlet.getActualCPUTime()) +
                        indent + indent + dft.format(cloudlet.getExecStartTime()) +
                        indent + indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }
        double makespan = calcMakespan(list, vms);
        Log.printLine("Makespan using FCFS: " + makespan);
    }

    private static double calcMakespan(List<Cloudlet> list, int vms) {
//      double makespan = 0;
//      double[] dcWorkingTime = new double[Constants.NO_OF_DATA_CENTERS];
//
//      for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
//          int dcId = list.get(i).getVmId() % Constants.NO_OF_DATA_CENTERS;
//          if (dcWorkingTime[dcId] != 0) --dcWorkingTime[dcId];
//          dcWorkingTime[dcId] += execMatrix[i][dcId] + commMatrix[i][dcId];
//          makespan = Math.max(makespan, dcWorkingTime[dcId]);
//      }
  	
  	
  	double[] vm_total = new double [vms]; 
  	for(int i=0; i<list.size(); i++) {
  		int cl_id = list.get(i).getVmId();
  		double finish_time = list.get(i).getFinishTime();
  		vm_total[cl_id] += finish_time;
  	}
  	double makespan = vm_total[0];
  	for(int i=1; i<vms; i++) {
  		if(vm_total[i] > makespan)
  			makespan = vm_total[i];
  	}
      return makespan;
  }
}