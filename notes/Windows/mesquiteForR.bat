set PATH=%PATH%;C:\Program Files\R\R-2.13-1\bin
java -Xmx400M -DstartedForR="r" -Djava.library.path="C:\Program Files\R\R-2.13-1\library\rJava\jri" -Djri.ignore.ule="yes" -cp . mesquite.Mesquite