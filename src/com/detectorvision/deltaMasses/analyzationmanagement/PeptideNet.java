/* $Id: PeptideNet.java 403 2010-11-06 15:55:38Z frank $ */

package com.detectorvision.deltaMasses.analyzationmanagement;

import com.detectorvision.deltaMasses.postSQL;
import com.detectorvision.utility.DeltaMassBase;
import com.detectorvision.utility.GaussianFit;

import java.awt.Color;
import java.awt.Graphics2D;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;

import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;

public class PeptideNet {
	public ArrayList<DB_deltamass> pairList = new ArrayList<DB_deltamass>();
	public ArrayList<Integer> specIdList = new ArrayList<Integer>();
	
	public ArrayList<Double> commonMasses = new ArrayList<Double>();
	public int specnet_id;
	public int numspecs;
	public int numpairs;
	public Double minmass;
	public Double maxmass;
	public int numphospho;
	public int maxSpecId;

	// Logging with log4j
	static Logger logger = Logger.getLogger(PeptideNet.class.getName());
	public static ArrayList<Spectrum_to_Experiment> spec2Experiment = new ArrayList<Spectrum_to_Experiment>();

	public void clearNet(){
		this.commonMasses.clear();
		this.pairList.clear();
		this.specIdList.clear();
	}
	
	public void loadSpec2Experiment(){
		logger.info("setting spec2Experiment");
		Connection conn = DeltaMassBase.getConnection();
		PreparedStatement getSpecs;
		try {
			getSpecs = conn.prepareStatement("SELECT spectrum_id,experiment_id " +
					"                         from spectrum,record,experiment " +
					"                         where spectrum.fk_record_id=record.record_id and fk_experiment_id=experiment.experiment_id ");
			ResultSet result = getSpecs.executeQuery();
			int count=0;
			while (result.next()) {
				Spectrum_to_Experiment s2e = new Spectrum_to_Experiment();
				s2e.spectrum_id=result.getInt(1);
				s2e.experiment_id=result.getInt(2);
				count++;
				this.spec2Experiment.add(s2e);
			}
			conn.close();
			logger.info("spec2Experiment: loaded specs:"+count);
		} catch (SQLException e) {
			logger.error("SQLException:"+e.toString());
		}
	}

	public boolean loadNet(int net_id, boolean isDiscEdition){
		logger.debug("loadNet:"+net_id);
		boolean RC=true;
		Connection conn = DeltaMassBase.getConnection();
		PreparedStatement getNets;
		try {
			getNets = conn.prepareStatement("SELECT deltamass_id, dm, sim, p, fk_l_spectrum_id,fk_h_spectrum_id,mass_light,lfk_specnet_id,weakdelta FROM deltamass where lfk_specnet_id = ? ORDER BY fk_l_spectrum_id");
			getNets.setInt(1,net_id);


			ResultSet result = getNets.executeQuery();
			int count=0;
			while (result.next()) {
				DB_deltamass tmp_deltamass = new DB_deltamass();
				tmp_deltamass.deltamass_id=result.getInt(1);
				tmp_deltamass.dm=result.getDouble(2);
				tmp_deltamass.sim=result.getDouble(3);
				tmp_deltamass.p=result.getDouble(4);
				tmp_deltamass.fk_l_spectrum_id=result.getInt(5);
				tmp_deltamass.fk_h_spectrum_id=result.getInt(6);
				tmp_deltamass.mass_light=result.getDouble(7);
				tmp_deltamass.lfk_specnet_id=result.getInt(8);
				tmp_deltamass.weakloss=result.getBoolean(9);
				count++;
				this.pairList.add(tmp_deltamass);
			}
			conn.close();//20080110
		} catch (SQLException e) {
			logger.error("SQLException:"+e.toString());
			RC=false;
			return RC;
		}
		preparenet(net_id);
		return(RC);
	}

	public void preparenet(int net_id){		
		this.minmass=Double.MAX_VALUE;
		this.maxmass=Double.MIN_VALUE;
		this.numphospho=0;
		this.numspecs=0;
		this.numpairs=0;
		this.specnet_id=net_id;
		
		specIdList.clear();//20080122
		
		for(int i=0;i<this.pairList.size();i++){
			if (pairList.get(i).dm<0)
				logger.error("preparenet:dm smaller zero:SYSTEM_FAILURE:" +
										 pairList.get(i).dm);
			if(pairList.get(i).mass_light+pairList.get(i).dm > maxmass){maxmass=pairList.get(i).mass_light+pairList.get(i).dm;}
			if(pairList.get(i).mass_light<minmass){minmass=pairList.get(i).mass_light;}
			numpairs++;
			if(  Math.abs(pairList.get(i).dm-79.96666)<0.01  ){numphospho++;}
			if(! specIdList.contains(pairList.get(i).fk_l_spectrum_id)){
				specIdList.add(pairList.get(i).fk_l_spectrum_id);
			}
			if(! specIdList.contains(pairList.get(i).fk_h_spectrum_id)){
				specIdList.add(pairList.get(i).fk_h_spectrum_id);
			}
		}
		
		//numspecs=specIdList.size()+1; corrected 20071230 frank		
		numspecs=specIdList.size();
		if(numspecs==0){
			logger.error("preparenet:numspecs is zero:SYSTEM_FAILURE");
			return;
		}

		//populate commonMasses
		//this is the list of commonly occuring masses in the specnet
		//as indicated by horizontal lines in the specnet plots.
		//not stored in database currently.
		double massWidth=this.maxmass-this.minmass;
		int numBin=(int)((massWidth)/0.02);
		int histogram[] = new int[numBin+1];
		for(int i=0;i<=numBin;i++){histogram[i]=0;}
		for(int i=0;i<this.pairList.size();i++){
			int bin=(int)((this.pairList.get(i).mass_light-this.minmass)/0.02);
			if (bin>numBin) {
				logger.fatal("numBin too big:" + bin);
				System.exit(1);
			}
			histogram[bin]++;

			bin=(int)((this.pairList.get(i).mass_light+this.pairList.get(i).dm - this.minmass)/0.02);
			if (bin>numBin) {
				logger.fatal("peptideNet error> numBin too big:"+bin);
				System.exit(1);
			}
			histogram[bin]++;
		}

		for(int i=0;i<10;i++){
			int max=0;int maxIndex=0;
			for(int j=0;j<=numBin;j++){
				if(histogram[j]>max){max=histogram[j];maxIndex=j;}
			}

			if(max>0){
				double avMass=0.0;int count=0;
				double tmpMass=this.minmass+0.02*maxIndex;
				for(int k=0;k<this.pairList.size();k++){
					if(Math.abs(this.pairList.get(k).mass_light-tmpMass)<0.02){avMass+=this.pairList.get(k).mass_light;count++;}
					if(Math.abs(this.pairList.get(k).mass_light+this.pairList.get(k).dm-tmpMass)<0.02){avMass+=this.pairList.get(k).mass_light+this.pairList.get(k).dm;count++;}
				}
				avMass/=count;
				commonMasses.add(avMass);
				//System.out.println("new common mass:"+avMass);
			}
			//clean neighbourhood
			histogram[maxIndex]=0;
			if(maxIndex>0){histogram[maxIndex-1]=0;}
			if(maxIndex<numBin){histogram[maxIndex+1]=0;}
			if(maxIndex>1){histogram[maxIndex-2]=0;}
			if(maxIndex<numBin-1){histogram[maxIndex+2]=0;}
			if(maxIndex>2){histogram[maxIndex-3]=0;}
			if(maxIndex<numBin-2){histogram[maxIndex+3]=0;}
			if(maxIndex>3){histogram[maxIndex-4]=0;}
			if(maxIndex<numBin-3){histogram[maxIndex+4]=0;}
			if(maxIndex>4){histogram[maxIndex-5]=0;}
			if(maxIndex<numBin-4){histogram[maxIndex+5]=0;}
			if(maxIndex>5){histogram[maxIndex-6]=0;}
			if(maxIndex<numBin-5){histogram[maxIndex+6]=0;}
		}
		this.maxSpecId=Integer.MIN_VALUE;
		for(int i=0;i<this.specIdList.size();i++){
			this.maxSpecId=Math.max(this.specIdList.get(i), this.maxSpecId);
		}
	}

	public boolean loadPairList(){
		//reads the complete (!) table deltamass into te array this.pairList
		logger.info("loadPairlist:entering");
		Connection conn = DeltaMassBase.getConnection();
		PreparedStatement getNets;
		this.pairList.clear();
		try {
			//TODO what if we get a memory overflow below ???
			getNets = conn.prepareStatement("SELECT deltamass_id, dm, sim, p, fk_l_spectrum_id,fk_h_spectrum_id,mass_light,lfk_specnet_id FROM deltamass ORDER BY fk_l_spectrum_id");
			ResultSet result = getNets.executeQuery();
			int count=0;
			while (result.next()) {
				DB_deltamass tmp_deltamass = new DB_deltamass();
				tmp_deltamass.deltamass_id=result.getInt(1);
				tmp_deltamass.dm=result.getDouble(2);
				tmp_deltamass.sim=result.getDouble(3);
				tmp_deltamass.p=result.getDouble(4);
				tmp_deltamass.fk_l_spectrum_id=result.getInt(5);
				tmp_deltamass.fk_h_spectrum_id=result.getInt(6);
				tmp_deltamass.mass_light=result.getDouble(7);
				tmp_deltamass.lfk_specnet_id=result.getInt(8);
				count++;
				this.pairList.add(tmp_deltamass);
			}
			conn.close();
			logger.info("PeptideNet:loadPairList:loaded pairs:"+this.pairList.size());
			return true;
		} catch (SQLException e) {
			logger.error("PeptideNet:loadPairlist:SQLException:"+e.toString());
			return false;
		}	
	}

	public boolean cleanSpecnetDB(){
		logger.info("cleanSpecnetDB:entering");
		Connection conn = DeltaMassBase.getConnection();
		PreparedStatement pst;
		try {
			pst = conn.prepareStatement("update deltamass set lfk_specnet_id = 0");
		    pst.executeUpdate();
		} catch (SQLException e) {
			logger.error("PeptideNet:lfk_specnet_id = 0:SQLException:"+e.toString());
			return false;
		}	
		
		
		try{
     		if(DeltaMassBase.isFingerPrintTableExists()){
     			logger.warn("table fingerprint does not exist");
		    	pst = conn.prepareStatement("truncate table fingerprint");
		    	pst.executeUpdate();
		    }
		} catch (SQLException e) {
			logger.error("PeptideNet:truncate table fingerprint:SQLException:"+e.toString());
			return false;
		}	
     	    
		try{	
			pst = conn.prepareStatement("truncate table specnet");
			pst.executeUpdate();
			
			pst.close();
			conn.close();
			return true;
			
		} catch (SQLException e) {
			logger.error("PeptideNet:truncate table specnet:SQLException:"+e.toString());
			return false;
		}	
	}
		
	public static void main(String[] args)
	{
		logger.debug("from PeptideNet::main");
		PeptideNet walter = new PeptideNet();
		walter.calcNet(true);
	}

	public  boolean calcAndStoreFingerprint(){
		final int lowMass = 0;
		final int  highMass = 2000;
		int[] countBin = new int[200000];//C-Style ....
		ArrayList<GaussianFit> GaussFits = new ArrayList<GaussianFit>();
		boolean[] blocked = new boolean[200000];
		for(int i=0;i<200000;i++){
			countBin[i]=0;blocked[i]=false;
		}
		try{	
			Connection conn = DeltaMassBase.getConnection();
			Statement s = conn.createStatement ();

			ResultSet result = s.executeQuery("SELECT dm,sim,p from deltaMass where dm > " +lowMass + " AND dm < "+highMass);
			double dm=0;
			int bin=0;
			int countPairs=0;
			while (result.next()) {
				dm  = Math.abs(result.getDouble(1));
				if(dm<2000){
					bin=(int)(dm*100);
					if(bin>199999 || bin <0){
						logger.fatal("System error:wrong bin:"+bin);
						System.exit(1);
					}
					countPairs++;
					countBin[bin]++;
				}
			}
			for(int j=0;j<25;j++){
				int max=0;
				int maxIndex=0;
				for(int i=0;i<200000;i++){
					if(countBin[i]>max && blocked[i]==false){max=countBin[i];maxIndex=i;}
				}
				for(int i=Math.max(maxIndex-6, 0);i<Math.min(maxIndex+6, 200000);i++)blocked[i]=true;
				double d=((double)maxIndex)/100.0;
				if(max>50){
					GaussFits.add(postSQL.DMBgetGaussianFit(d, 0.04, 20));
				}
				
			}
			if(DeltaMassBase.isFingerPrintTableExists())
				storeValuesInDB(GaussFits);
			conn.close();
		}catch(Exception ex){
			logger.error("PeptideNet:calcAndStoreFingerprint: " + ex);
			return false;
		}
		return true;
	}
	
	private void storeValuesInDB(ArrayList<GaussianFit> aGaussFits)
	{
		double expMass;
		double height;
		double base;
		double sigma;
		double numUnderCurve;
		double error;
		double x[], y[];
		StringBuffer xy = null;
		Connection conn = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		String selectSt = " SELECT MAX(FINGERPRINT_ID) FROM FINGERPRINT "; // for autoincrement for the Primary Key
		String insertSt = " INSERT INTO FINGERPRINT (fingerprint_id,exp_mass,height,base,sigma,numunder,xy,error) VALUES (?,?,?,?,?,?,?,?)";
		int seq = 1;
		try{
			conn = DeltaMassBase.getConnection();
			pst = conn.prepareStatement(selectSt);
			rs = pst.executeQuery();
			
			if(rs != null) {
				if (rs.next())
				{
				 seq = rs.getInt(1) + 1;	
				}
				
			}
			for(int i = 0; i < aGaussFits.size(); i++)
			{
				xy = new StringBuffer();
				GaussianFit gFit = aGaussFits.get(i);
				expMass = gFit.getExp();
				height = gFit.getH();
				base = gFit.getBase();
				sigma = gFit.getSigma();
				numUnderCurve = gFit.getNumUnderCurve();
				
				error = gFit.getError();
				x = gFit.getX();
				y = gFit.getY();
				
				for(int j = 0 ; j < x.length ; j++){
					xy.append(String.valueOf(x[i]));
					if(j+1 !=  x.length) 
						xy.append(",");
				}
				xy.append("|");
				for(int j = 0 ; j < y.length ; j++){
					xy.append(String.valueOf(y[i]));
					if(j+1 <  y.length) 
						xy.append(",");	
				}

				pst = conn.prepareStatement(insertSt);
				pst.setInt(1, seq);
				pst.setDouble(2,expMass);
				pst.setDouble(3,height);
				pst.setDouble(4,base);
				pst.setDouble(5,sigma);
				pst.setDouble(6,numUnderCurve);
				pst.setString(7,xy.toString());
				pst.setDouble(8,error);
				pst.execute();
				seq = seq + 1;
			}
		}
		 catch (Exception ex) {
			 logger.error("PeptideNet:storeValuesInDB: " + ex);
		}
	}
	
	public void sortPairList(){
		//sorts the pairList such that the most common fk_l_spectrum_id is first
		logger.info("PeptideNet:sortPairList:entering");
		ArrayList<DB_deltamass> tmpPairlist = new ArrayList<DB_deltamass>();
		
		Connection conn = DeltaMassBase.getConnection();
		PreparedStatement getStatement;
		logger.info("PeptideNet:sortPairList:start");
		try {
			getStatement = conn.prepareStatement("select fk_l_spectrum_id,count(fk_l_spectrum_id) from deltamass group by fk_l_spectrum_id order by count(fk_l_spectrum_id) desc;");
			ResultSet result = getStatement.executeQuery();
			int count=0;
			while (result.next()) {
				int fk_light=result.getInt(1);
				int fk_light_count=result.getInt(2);
				for(int i=0;i<this.pairList.size();i++){
					if(this.pairList.get(i).fk_l_spectrum_id==fk_light){
						DB_deltamass dm = new DB_deltamass();
						dm=this.pairList.get(i);
						tmpPairlist.add(dm);
					}					
				}
			}
			conn.close();
			this.pairList.clear();
			this.pairList=tmpPairlist;
		} catch (SQLException e) {
			logger.error("PeptideNet:sortPairList:SQLException:"+e.toString());
		}	
		logger.info("PeptideNet:sortPairList:end:all ok.size:"+this.pairList.size());
	}
	
	public void sortByHeavySpectrum(){
		Collections.sort(this.pairList, new Comparator<DB_deltamass>() {
			   public int compare(DB_deltamass arg0, DB_deltamass arg1) {
				if       (arg0.fk_h_spectrum_id > arg1.fk_h_spectrum_id){return -1;}
				else if  (arg0.fk_h_spectrum_id < arg1.fk_h_spectrum_id){return  1;}
				return 0;
			}});
	}
	
	public void sortBySpecNetId(){
		Collections.sort(this.pairList, new Comparator<DB_deltamass>() {
			   public int compare(DB_deltamass arg0, DB_deltamass arg1) {
				if       (arg0.lfk_specnet_id > arg1.lfk_specnet_id){return -1;}
				else if  (arg0.lfk_specnet_id < arg1.lfk_specnet_id){return  1;}
				return 0;
			}});
	}

    public void mergeNets(int net1, int net2){//net2 becomes part of net1
	   for(int i=0;i<this.pairList.size();i++){
		   if(this.pairList.get(i).lfk_specnet_id==net2){
			   this.pairList.get(i).lfk_specnet_id=net1;
		   }
	   }
   }
   
    public int countNets(){
	   ArrayList<Integer> ids= new ArrayList<Integer>();
	   for(int i=0;i<this.pairList.size();i++){
		   if(! ids.contains(this.pairList.get(i).lfk_specnet_id)){
			   ids.add(this.pairList.get(i).lfk_specnet_id);
		   }
	   }
	   return ids.size();
   }
   
	public boolean calcNet(boolean isDiscoveryEdition){
		int specNetId=0;	
		logger.info("PeptideNet:calcNet:entering:copenhagen edition:");
 
		this.cleanSpecnetDB();//truncates specnet and NULLs deltamass.lfk_specnet_id
		calcAndStoreFingerprint(); // call for calculating and storing the finger print
		this.loadPairList();
		this.sortPairList();
		
		logger.info("PeptideNet:calcNet:loaded pairlist:"+this.pairList.size());
		
        //merging based on light spectrum------------------------------------------------
		DB_deltamass tmpPair = new DB_deltamass();
		int tmpNetId=0;
		if(this.pairList.size()==0){
			logger.info("PeptideNet:this.pairlist has size zero");
			return true;
		}
		
		//tmpPair=this.pairList.get(0);
		//tmpPair.lfk_specnet_id=tmpNetId;
		//this.pairList.set(0, tmpPair);
		this.pairList.get(0).lfk_specnet_id=tmpNetId;
		
		for(int i =1;i<this.pairList.size();i++){
			if(this.pairList.get(i).fk_l_spectrum_id!=this.pairList.get(i-1).fk_l_spectrum_id){
				tmpNetId++;
			}
			this.pairList.get(i).lfk_specnet_id=tmpNetId;
			//tmpPair=this.pairList.get(i);
			//tmpPair.lfk_specnet_id=tmpNetId;
			//this.pairList.set(i, tmpPair);
		}
				
		logger.debug("nets:"+tmpNetId);
		
		//merging based on heavy spectrum===============================================
        this.sortByHeavySpectrum();
        
		for(int i =1;i<this.pairList.size();i++){
			if(this.pairList.get(i).fk_h_spectrum_id==this.pairList.get(i-1).fk_h_spectrum_id && this.pairList.get(i).lfk_specnet_id != this.pairList.get(i-1).lfk_specnet_id){
				mergeNets(this.pairList.get(i).lfk_specnet_id,this.pairList.get(i-1).lfk_specnet_id);
			}
		}
		logger.info("nets:"+countNets());
		
		//merging based on light-heavy
		for(int i=0;i<this.pairList.size();i++){
			for(int j=0;j<this.pairList.size();j++){
				if(this.pairList.get(i).fk_l_spectrum_id==this.pairList.get(j).fk_h_spectrum_id && this.pairList.get(i).lfk_specnet_id != this.pairList.get(j).lfk_specnet_id && i != j){
					mergeNets(this.pairList.get(i).lfk_specnet_id, this.pairList.get(j).lfk_specnet_id);
				}
			}
		}
		logger.info("nets after light heavy:"+countNets());

		sortBySpecNetId();
		logger.info("nets:"+countNets());
		
		specNetId=0;
		
		int specNetMemory=0;
		try{
			Connection conn = DeltaMassBase.getConnection();
			PreparedStatement pst;	
			//pst = conn.prepareStatement("truncate table deltamass");
			//pst.executeUpdate();
			
			do{//---------------------next specnet is produced and stored-------------------------------START
				specNetId=specNetId+1;//first value to be used is 1		
				ArrayList<DB_deltamass> tmpPairList = new ArrayList<DB_deltamass>();
				tmpPairList.clear();
				//copy next specnet from this.pairList to tmpPairList.
				specNetMemory=this.pairList.get(0).lfk_specnet_id;
				int loop=0;
				for(loop=0;loop < this.pairList.size() && this.pairList.get(loop).lfk_specnet_id==specNetMemory ;loop++){
					tmpPair=this.pairList.get(loop);
					tmpPair.lfk_specnet_id=specNetId;
					tmpPairList.add(tmpPair);
				}
				if(loop<this.pairList.size()){
				specNetMemory=this.pairList.get(loop).lfk_specnet_id;
				}
				else{specNetMemory=-1;}//boil out in while
				
				//System.out.println("loop:"+loop);
		        for(int j=0;j<loop;j++){
		        	if(this.pairList.size()>0){
		        	this.pairList.remove(0);//list is moving "down"
		        	}
		        	else{
		        		logger.fatal("calcNet:small this.pairList. check logic:");
		        	}
		        }
		        //------------------------------calculate minMass maxMass numPhospho numpairs
		        double minMass=Double.MAX_VALUE;
		        double maxMass=Double.MIN_VALUE;
		        int numPhospho=0;
		        ArrayList<Integer> tmpList= new ArrayList<Integer>();
		        for(int i=0;i<tmpPairList.size();i++){
		        	if(!tmpList.contains(tmpPairList.get(i).fk_h_spectrum_id)){
		        		tmpList.add(tmpPairList.get(i).fk_h_spectrum_id);
		        	}
		        	if(!tmpList.contains(tmpPairList.get(i).fk_l_spectrum_id)){
		        		tmpList.add(tmpPairList.get(i).fk_l_spectrum_id);
		        	}
		        	minMass=Math.min(tmpPairList.get(i).mass_light,minMass);
		        	maxMass=Math.max(tmpPairList.get(i).mass_light+tmpPairList.get(i).deltamass_id, maxMass);
		        	if(Math.abs(tmpPairList.get(i).dm-79.966331)<0.01){numPhospho++;}
		        }
		        
				//put the specnet into the database---------------------------------------------------------------
				pst=conn.prepareStatement("INSERT INTO specnet (specnet_id,numspecs,numpairs,minmass,maxmass,numphospho,fk_experiment_id) VALUES(?,?,?,?,?,?,?)");
				pst.setInt(1, specNetId);
				pst.setInt(2, tmpList.size());
				pst.setInt(3,tmpPairList.size());
				pst.setDouble(4, minMass);
				pst.setDouble(5,maxMass);
				pst.setInt(6, numPhospho);
				pst.setInt(7,0);
				pst.execute();
				
				//------------------------------update lfk_specnet_id in deltamass ----------------------------------
				for(int i=0;i<tmpPairList.size();i++){
						pst=conn.prepareStatement("UPDATE deltamass set lfk_specnet_id=? where deltamass_id=?");
					    pst.setInt(1, tmpPairList.get(i).lfk_specnet_id);
					    pst.setInt(2, tmpPairList.get(i).deltamass_id);
					    //pst = conn.prepareStatement("INSERT INTO deltamass (deltamass_id , dm , sim , p , fk_l_spectrum_id , fk_h_spectrum_id , mass_light , lfk_specnet_id) VALUES (?,?,?,?,?,?,?,?);");
					   // pst.setInt(1, tmpPairList.get(i).deltamass_id);
					   //pst.setDouble(2,tmpPairList.get(i).dm);
						//pst.setDouble(3, tmpPairList.get(i).sim);
						//pst.setDouble(4, tmpPairList.get(i).p);
						//pst.setInt(5, tmpPairList.get(i).fk_l_spectrum_id);
						//pst.setInt(6, tmpPairList.get(i).fk_h_spectrum_id);
						//pst.setDouble(7, tmpPairList.get(i).mass_light);
						//pst.setInt(8, tmpPairList.get(i).lfk_specnet_id);
						pst.executeUpdate();
						
				}
				logger.debug("pairList size:"+this.pairList.size());
			}while(this.pairList.size()>1 && specNetMemory>0);
			//---------------------next specnet is produced and stored-------------------------------END
			
			//------------update lastSpecnetDate in meta_db
			PreparedStatement pstmt = conn.prepareStatement("update meta_db set lastSpecnetDate = ?");
			java.sql.Timestamp  sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
			pstmt.setTimestamp(1,sqlDate);
			pstmt.executeUpdate();
			
			conn.close();
			logger.info("PeptideNet:calcNet:success. Number of specnets:"+specNetId);
			return true;
		}
		catch(SQLException e){
			logger.error("PeptideNet:calcNet:error:SQLException:"+e.getMessage());
			return false;
		}
	}
	
	public void paintNet(Graphics2D g, int pix){
		//this is a FAST routine for printing a net.
		//a routine with more details is still to be implemented.
		//graphics has a size of pix*pix pixels
		
		ArrayList<Integer> SpecsWithId=DeltaUtils.DMBgetSpecnetIDsOfNetHavingProteinId(this.specnet_id);
		
		if(this.maxmass-this.minmass <=0){//security for division below
			logger.error("mass width error:specnet_id:"+this.specnet_id);
			return;
		}
		if(this.numspecs <=0){//security for division below
			logger.error("numspecs error:numspecs:"+this.numspecs);
			return;
		}
		if(pix<50){//security - this does not make sense
			logger.error("int pix smaller than 50:pix:"+pix);
			return;
		}

		int x0=10;
		int y0=10;
		int wx=pix-x0;
		int wy=pix-y0;
		int xl=0,yl=0,xh=0,yh=0; //x-light y-light x-heavy y-heavy
		int lSpecId,hSpecId;//light-spectrum-id heavy-spectrum-id

		if(wx<50){//security - this does not make sense
			logger.error("int wx smaller than 50:wx:"+wx);
			return;
		}
		if(wy<50){//security - this does not make sense
			logger.error("int wy smaller than 50:wy:"+wy);
			return;
		}
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.BLACK);
		g2.setBackground(Color.LIGHT_GRAY);

		double pixPerDalton=  (double)wy/(this.maxmass-this.minmass);
		double pixperSpec=    (double)wx/(this.numspecs);

		
		
		ArrayList<Integer> pointBuffer= new ArrayList<Integer>();//
		
		for(int h=0;h<this.specIdList.size();h++){
			for(int i=0;i<this.pairList.size();i++){
				if(this.specIdList.get(h)==this.pairList.get(i).fk_l_spectrum_id){
					xl=(int)(x0+pixperSpec*h);
					yl=(int)(y0+pixPerDalton*(this.pairList.get(i).mass_light-this.minmass));
					for(int k=0;k<this.specIdList.size();k++){
						if(this.specIdList.get(k)==this.pairList.get(i).fk_h_spectrum_id){
							xh=(int)(x0+pixperSpec*k);
							yh=(int)(y0+pixPerDalton*(this.pairList.get(i).mass_light+this.pairList.get(i).dm-this.minmass));
							//System.out.println(xl+" : "+ yl+" : "+ xh+" : "+ yh);	
							
							
							if     (  Math.abs(this.pairList.get(i).dm-79.966331) < 0.01 ) {g2.setColor(Color.YELLOW);}//HO3P Phosphorylation
							else if(  Math.abs(this.pairList.get(i).dm-15.994915) < 0.01 ) {g2.setColor(Color.BLUE);}//O Oxidation
							else if(  Math.abs(this.pairList.get(i).dm-14.01565)  < 0.01 ) {g2.setColor(Color.GREEN);}//Methylation	H2C			
							else if(  Math.abs(this.pairList.get(i).dm-57.021464) < 0.01 ) {g2.setColor(Color.RED);}//Carbamidomethylation  H3C2NO
							else if(  Math.abs(this.pairList.get(i).dm-43.005814) < 0.01 ) {g2.setColor(Color.CYAN);}//HCNO Carbamylation
							else if(  Math.abs(this.pairList.get(i).dm-28.031300)< 0.01 )  {g2.setColor(Color.ORANGE);}//Dimethylation
							else                                                           {g2.setColor(Color.LIGHT_GRAY);}		
							
							if(this.pairList.get(i).weakloss){	g2.setColor(Color.BLACK);}
						
							g2.drawLine(xl, yl, xh, yh);
							
							
							int myspec=this.pairList.get(i).fk_h_spectrum_id;
							for(int r=0;r<spec2Experiment.size();r++){
								if(spec2Experiment.get(r).spectrum_id==myspec){
									if(spec2Experiment.get(r).experiment_id==1){
										g2.setColor(Color.GREEN);
										g2.fillRect(xh-6, yh-6, 12, 12);
									}
									else if(spec2Experiment.get(r).experiment_id==2){
										g2.setColor(Color.RED);
										g2.fillRect(xh-6, yh-6, 12, 12);
									}
									else if(spec2Experiment.get(r).experiment_id==3){
										g2.setColor(Color.BLUE);
										g2.fillRect(xh-6, yh-6, 12, 12);
									}
									else{
										g2.setColor(Color.GRAY);
										g2.fillRect(xh-6, yh-6, 12, 12);
									}
									break;
								}
							}
							
							myspec=this.pairList.get(i).fk_l_spectrum_id;
							for(int r=0;r<spec2Experiment.size();r++){
								if(spec2Experiment.get(r).spectrum_id==myspec){
									if(spec2Experiment.get(r).experiment_id==1){
										g2.setColor(Color.GREEN);
										g2.fillRect(xl-6, yl-6, 12, 12);
									}
									else if(spec2Experiment.get(r).experiment_id==2){
										g2.setColor(Color.RED);
										g2.fillRect(xl-6, yl-6, 12, 12);
									}
									else if(spec2Experiment.get(r).experiment_id==3){
										g2.setColor(Color.BLUE);
										g2.fillRect(xl-6, yl-6, 12, 12);
									}
									else if(spec2Experiment.get(r).experiment_id==0){
										g2.setColor(Color.GRAY);
										g2.fillRect(xl-6, yl-6, 12, 12);
									}
						            else {
										g2.setColor(Color.YELLOW);
										g2.fillRect(xl-6, yl-6, 12, 12);
									}
									break;
								}
							}
							

							//mark spectra with id with a green circle (buffer and point later to bring into foreground.
							if(SpecsWithId.contains(this.pairList.get(i).fk_l_spectrum_id)){
								pointBuffer.add(xl);pointBuffer.add(yl);
							}
							if(SpecsWithId.contains(this.pairList.get(i).fk_h_spectrum_id)){
								pointBuffer.add(xh);pointBuffer.add(yh);
							}						
						}
					}		
				}
			}
		}

		

		for(int i=0;i<commonMasses.size();i++){
			int yTmp=(int)(y0+pixPerDalton*(commonMasses.get(i)-this.minmass));
			g2.setColor(Color.LIGHT_GRAY);
			g2.drawLine(x0, yTmp, pix, yTmp);
		}
		for(int i=0;i<pointBuffer.size();i=i+2){
			g2.setColor(Color.BLUE);
			g2.fillOval(pointBuffer.get(i)-6, pointBuffer.get(i+1)-6, 12, 12);
			g2.setColor(Color.WHITE);
			g2.fillOval(pointBuffer.get(i)-4, pointBuffer.get(i+1)-4, 8, 8);
			g2.setColor(Color.RED);
			g2.fillOval(pointBuffer.get(i)-1, pointBuffer.get(i+1)-1, 2, 2);
		}
		pointBuffer.clear();
		for(int i=0;i<commonMasses.size();i++){
			int yTmp=(int)(y0+pixPerDalton*(commonMasses.get(i)-this.minmass));
			g2.setColor(Color.BLACK);
			Formatter form1 = new Formatter();
			String tmpString = form1.format("%.4f", commonMasses.get(i)).toString(); 	
			g2.drawString(tmpString+" [Da]" , x0, yTmp);
		}
	}
}