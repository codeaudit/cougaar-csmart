#include "org_cougaar_tools_csmart_runtime_jni_CpuClock.h"
#ifdef _WIN32
#include <windows.h>
#else
#include <sys/times.h>
#endif /* _WIN32 */


JNIEXPORT jint JNICALL Java_org_cougaar_tools_csmart_runtime_jni_CpuClock_clock (JNIEnv *env, jobject obj) {
#ifdef _WIN32
	LARGE_INTEGER bigIntKernel, bigIntUser;
	__int64 bigIntTotal, returnVal;
	static __int64 lastTotal = 0;
	FILETIME creationTime, exitTime, kernelTime, userTime;
	HANDLE proc;
	/* DebugBreak(); */
	proc = GetCurrentProcess();
	GetProcessTimes(proc, &creationTime, &exitTime, &kernelTime, &userTime);
	/* Add kernel and user times */
	memcpy(&bigIntKernel, &kernelTime, sizeof(bigIntKernel));
	memcpy(&bigIntUser, &userTime, sizeof(bigIntUser));
	bigIntTotal = bigIntKernel.QuadPart + bigIntUser.QuadPart;
	bigIntTotal /= 10000; /* convert 100 nsec units to msecs */
	returnVal = bigIntTotal - lastTotal;
	lastTotal = bigIntTotal;
	return returnVal;

#else /* _WIN32 */

/*
	int clk = clock();
	return clk/1000; /* returns CPU millisecs since last call */
	
 	static long int last_total = 0;
	long int total, return_val;
	struct tms tms;
	int clks = times(&tms);
	total = tms.tms_utime + tms.tms_stime;
	return_val = total - last_total;
	last_total = total;
/*
printf("total = %d\n", total);
printf("utime = %d\n", tms.tms_utime);
printf("stime = %d\n", tms.tms_stime);
printf("cutime = %d\n", tms.tms_cutime);
printf("cstime = %d\n", tms.tms_cstime);
*/
	return return_val*10; /* Don't understand the *10 (?) */

#endif /* _WIN32 */
}

