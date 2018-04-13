/* $Id: GaussianFit.java 233 2008-12-28 18:13:38Z frank $ */

package com.detectorvision.utility;

public class GaussianFit {
	double exp;
	double h;
	double base;
	double sigma;
	double numUnderCurve;
	double error;
	double[] x;
	double[] y;
	public boolean computationOK;
	

	public GaussianFit(double exp, double h, double base, double sigma,
			   double[]x, double[]y,double numUnderCurve,
			   double error) {
		this.exp=exp;
		this.h=h;
		this.base=base;
		this.sigma=sigma;
		this.x=x;
		this.y=y;
		this.numUnderCurve=numUnderCurve;
	}

	public double getBase() { return base; }

	public double getError() { return error; }

	public double getExp() { return exp; }

	public double getH() { return h; }

	public double getNumUnderCurve() { return numUnderCurve; }

	public double getSigma() { return sigma; }

	public double[] getX() { return x; }

	public double[] getY() { return y; }
	
	public void print(){
		System.out.println("exp           :"+exp);
		System.out.println("h             :"+h);
		System.out.println("base          :"+base);
		System.out.println("sigma         :"+sigma);
		System.out.println("numUnderCurve :"+numUnderCurve);
	}

}
