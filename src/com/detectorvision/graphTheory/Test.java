package com.detectorvision.graphTheory;

import java.util.ArrayList;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<AbstractLink> aLinks = new ArrayList<AbstractLink>();
		AbstractLink al1=  new AbstractLink();
		AbstractLink al2=  new AbstractLink();
		AbstractLink al3=  new AbstractLink();
		AbstractLink al4=  new AbstractLink();
		AbstractLink al5=  new AbstractLink();
		AbstractLink al6=  new AbstractLink();
		AbstractLink al7=  new AbstractLink();
		AbstractLink al8=  new AbstractLink();
		AbstractLink al9=  new AbstractLink();
		AbstractLink al10= new AbstractLink();
		
		al1.id=1;al1.start=1;al1.end=2;
		aLinks.add(al1);
		al2.id=2;al2.start=3;al2.end=4;
		aLinks.add(al2);
		al3.id=3;al3.start=4;al3.end=5;
		aLinks.add(al3);
		al4.id=4;al4.start=4;al4.end=6;
		aLinks.add(al4);		
		al5.id=5;al5.start=6;al5.end=5;
		aLinks.add(al5);
		al6.id=6;al6.start=2;al6.end=7;
		aLinks.add(al6);
		al7.id=7;al7.start=1;al7.end=3;
		al8.id=8;al8.start=9;al8.end=8;
		aLinks.add(al8);
		al9.id=9;al9.start=10;al9.end=9;
		aLinks.add(al9);
		al10.id=10;al10.start=10;al10.end=8;
		aLinks.add(al10);
		
		AbstractNets abstractnets = new AbstractNets(aLinks);
		abstractnets.printNets();
	}

}
