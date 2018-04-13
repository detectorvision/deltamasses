package com.detectorvision.deltaCluster;


public class MSMS implements Comparable {
	double mz;
	double sig;

	public int compareTo(Object o1) {
		// TODO Auto-generated method stub
		if (this.mz == ((MSMS) o1).mz)
			return 0;
		else if ((this.mz) > ((MSMS) o1).mz)
			return 1;
		else
			return -1;
	}
}
