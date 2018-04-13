package com.detectorvision.graphTheory;

import java.util.ArrayList;

public class AbstractNets {
	public ArrayList<AbstractNet> abstractNets = new ArrayList<AbstractNet>();
	public int size=0;

	public AbstractNets(ArrayList<AbstractLink> links) {
		this.abstractNets.clear();
		for(int i=0;i<links.size();i++){
			this.addLink(links.get(i));
		}
	}

	public void addLink(AbstractLink abstractLink){
		//System.out.println("Adding Link:"+abstractLink.start+"\t"+abstractLink.end);
		ArrayList<Integer> foundInNets= new ArrayList<Integer>();
		foundInNets.clear();//superfluous

		for(int i=0;i<this.abstractNets.size();i++){
			boolean found=false;
			for(int j=0;j<this.abstractNets.get(i).abstractNet.size();j++){
				if(     this.abstractNets.get(i).abstractNet.get(j).end  ==abstractLink.end   ||
						this.abstractNets.get(i).abstractNet.get(j).start==abstractLink.end   ||
						this.abstractNets.get(i).abstractNet.get(j).end  ==abstractLink.start ||
						this.abstractNets.get(i).abstractNet.get(j).start==abstractLink.start 
				){
					if(!found){
						foundInNets.add(i);
						found=true;
					}
				}
			}
		}

		if(foundInNets.size()==0){
			//new links not found in any net / add a new abstractNet to abstractNets.
			AbstractNet an = new AbstractNet();
			an.abstractNet.add(abstractLink);
			an.number=this.size;
			this.size++;
			this.abstractNets.add(an);
		}
		else{
			//add the new link to the first net where it was found
			AbstractNet aN = this.abstractNets.get(foundInNets.get(0));
			aN.abstractNet.add(abstractLink);
			this.abstractNets.set(foundInNets.get(0), aN);
			//System.out.println("merged to "+foundInNets.get(0));

			AbstractNet collectNet = new AbstractNet();
			for(int i=0;i<foundInNets.size();i++){
				for(int j=0;j<this.abstractNets.get(foundInNets.get(i)).abstractNet.size();j++)
					collectNet.abstractNet.add(this.abstractNets.get(foundInNets.get(i)).abstractNet.get(j));
			}
			//collectNet.print();
			//clear all which is in collectNet
			for(int i=0;i<foundInNets.size();i++){
				this.abstractNets.get(foundInNets.get(i)).abstractNet.clear();//gone with the wind
			}
			this.abstractNets.add(collectNet);

			//remove the empty nets			
			int upperBorder=this.abstractNets.size();
			int kill=0;
			for(int i=0;i<upperBorder;i++){
				if(this.abstractNets.get(i).abstractNet.size()==0){
					this.abstractNets.remove(i);
					upperBorder--;i--;
					kill++;
				}
			}
			//System.out.println("killed nets:"+kill);

		}
	}

	public void printNets(){
		for(int i=0;i<this.abstractNets.size();i++){
			System.out.println("---------------------\nNetsize:"+this.abstractNets.get(i).abstractNet.size());
			for(int j=0;j<this.abstractNets.get(i).abstractNet.size();j++){
				System.out.println(this.abstractNets.get(i).abstractNet.get(j).start + "\t"+  this.abstractNets.get(i).abstractNet.get(j).end );
			}

		}

	}
}

