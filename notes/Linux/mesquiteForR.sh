#!/bin/sh
dir=`dirname "$0"`

R_HOME=/usr/lib64/R


JRI_LD_PATH=${R_HOME}/lib:${R_HOME}/bin:
if test -z "$LD_LIBRARY_PATH"; then
  LD_LIBRARY_PATH=$JRI_LD_PATH
else
  LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$JRI_LD_PATH
fi
JAVA=/usr/bin/java



export R_HOME
export LD_LIBRARY_PATH

   ${JAVA}  -Xmx400M -DstartedForR="r" -Djava.library.path="/usr/local/lib/R/site-library/rJava/jri" -Djri.ignore.ule="yes" -cp "$dir" mesquite.Mesquite $*

   #above, java.library.path could be "$dir/lib" if you've moved it there