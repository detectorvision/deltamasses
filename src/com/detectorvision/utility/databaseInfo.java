package com.detectorvision.utility;

import org.apache.log4j.Logger;
import com.sun.corba.se.impl.util.Utility;


public class databaseInfo {
	public String  filename            ="not set";
	public String  DELTAMASSBASE_NAME  ="not set";
	public String  USER                ="not set";
	public String  PASSWORD            ="not set";
	public String  PORT                ="not set";
	public String  HOST                ="not set";
	public String  URL                 ="not set";
	public boolean exists=false;
	public boolean isReachable=false;
	public int     db_schema_version=-1;
	public String  connectionError     ="not set";
	public boolean couldBeCreated      =false; //true only if the DbEngine is is reachable, but the database is not there.
	
	
public void print(){
	Logger logger = Logger.getLogger(DeltaMassBase.class.getName());
	logger.info("--------------------------------------------------------------------:");
	logger.info("filename        :\t"+filename);
	logger.info("database name   :\t"+DELTAMASSBASE_NAME);
	logger.info("user            :\t"+USER);
	logger.info("password        :\txxxxxxxxxxx");
	logger.info("port            :\t"+PORT);
	logger.info("host            :\t"+HOST);
	logger.info("url             :\t"+URL);
	logger.info("exists          :\t"+exists);
	logger.info("reachable       :\t"+isReachable);
	logger.info("connectionError :\t"+connectionError);
	logger.info("schema version  :\t"+db_schema_version);
	logger.info("couldBeCreated  :\t"+couldBeCreated);
	logger.info("--------------------------------------------------------------------:");
}

}
