package com.detectorvision.utility;

import java.sql.Date;

public class DeltaMassBase_table_record {
	public Integer record_id;
	public String filename;
	public String pepFilename;
	public String userName;
	public java.sql.Date deltaDate;
	public String url;
	public double msPrecision;
	public double msmsPrecision;
	public boolean hasRetention;
	public String originMethod;
	public int num_spectra;
	public int num_pairs;
	public int fk_experiment_id;
	public String experimentname;
}