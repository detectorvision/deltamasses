package com.detectorvision.deltaCluster;

import java.util.ArrayList;


public class ClusterSpectrum {
public double precursorMass;
public int    charge;
public int    id;
public int	  record_id;
public ArrayList<Double> mz = new ArrayList<Double>();
public ArrayList<Double> signal = new ArrayList<Double>();
double minMz;
double maxMz;
double range;
}
