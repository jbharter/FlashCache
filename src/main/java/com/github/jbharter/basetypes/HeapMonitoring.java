package com.github.jbharter.basetypes;

public class HeapMonitoring {

    private int pseudoCeiling;
    private long headSpace;

    public HeapMonitoring() { }
    public HeapMonitoring(int maxPct) {
        this.pseudoCeiling = (maxPct > 0 && maxPct < 100) ? maxPct : 70;
        this.headSpace = this.pseudoCeiling - getHeapSizeUsed();
    }

    public long getHeadSpace() {
        this.headSpace = this.pseudoCeiling - getHeapSizeUsed();
        return this.headSpace;
    }
    public boolean haveContention() {
        return this.getHeadSpace() < 5;
    }
    public void setPseudoCeiling(int ceiling) {
        this.pseudoCeiling = (ceiling > 0 && ceiling < 100) ? ceiling : 70;
    }
    public int getPseudoCeiling() {
        return pseudoCeiling;
    }

    public static long getHeapSize() {
        return Runtime.getRuntime().totalMemory();
    }
    public static long getHeapSizeMax() {
        return Runtime.getRuntime().maxMemory();
    }
    public static long getHeapSizeFree() {
        return Runtime.getRuntime().freeMemory();
    }
    public static long getHeapSizeUsed() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
    public static long getHeapPctFree() {
        return getHeapSizeFree()/getHeapSize();
    }

}
