/* Mesquite.R source code.  Copyright 2010 W. Maddison, H. Lapp & D. Maddison. 

Mesquite.R is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This source code and its compiled class files are free and modifiable under the terms of 
GNU General Public License v. 2.  (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
*/

package mesquite.R.MesquiteCallsR.ape.aAPEIntro;
/*~~  */


import mesquite.R.common.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class aAPEIntro extends PackageIntro implements RPackageInterface {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
  	 }
	/*.................................................................................................................*/
	/** Returns the R packages needed*/
	public String[] packagesRequired(){
		return new String[]{"ape"};
	}
	 public Class getDutyClass(){
  	 	return aAPEIntro.class;
  	 }
	/*.................................................................................................................*/
    	 public String getExplanation() {
		return "Provides a link to functions of the R package \"ape\".";
   	 }
   
	/*.................................................................................................................*/
    public String getName() {
		return "ape Package Introduction";
   	 }
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
 	public String getPackageName(){
 		return "ape";
 	}
	/*.................................................................................................................*/
	/** Returns citation for a package of modules*/
 	public String getPackageCitation(){
 		return "";
 	}
	/*.................................................................................................................*/
  	 public String getPackageVersion() {
		return "0.6";
   	 }
	/*.................................................................................................................*/
  	 public String getPackageAuthors() {
		return "";
   	 }
	/*.................................................................................................................*/
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
 		return true; 
	}
}
