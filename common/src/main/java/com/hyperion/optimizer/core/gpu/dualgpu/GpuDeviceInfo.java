package com.hyperion.optimizer.core.gpu.dualgpu;

public final class GpuDeviceInfo {
    private final int index;
    private final String name;
    private final String vendor;
    private final long vramMb;
    private final boolean integrated;
    private final boolean discrete;

    public GpuDeviceInfo(int index, String name, String vendor, long vramMb, boolean integrated) {
        this.index = index;
        this.name = (name != null) ? name : "Unknown GPU";
        this.vendor = (vendor != null) ? vendor : "Unknown Vendor";
        this.vramMb = vramMb;
        this.integrated = integrated;
        this.discrete = !integrated;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getVendor() {
        return vendor;
    }

    public long getVramMb() {
        return vramMb;
    }

    public boolean isIntegrated() {
        return integrated;
    }

    public boolean isDiscrete() {
        return discrete;
    }

    public boolean isAmd() {
        String n = name.toLowerCase();
        String v = vendor.toLowerCase();
        return n.contains("amd") || n.contains("radeon") || v.contains("amd") || v.contains("ati");
    }

    public boolean isNvidia() {
        String n = name.toLowerCase();
        String v = vendor.toLowerCase();
        return n.contains("nvidia") || n.contains("geforce") || v.contains("nvidia");
    }

    public boolean isIntel() {
        String n = name.toLowerCase();
        String v = vendor.toLowerCase();
        return n.contains("intel") || n.contains("iris") || n.contains("uhd") || n.contains("arc") || v.contains("intel");
    }

    public boolean isAppleSilicon() {
        String n = name.toLowerCase();
        String v = vendor.toLowerCase();
        return n.contains("apple") || n.contains("m1") || n.contains("m2") || n.contains("m3") || n.contains("m4") ||
               v.contains("apple") || n.contains("metal") || n.contains("moltenvk");
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s, %d MB VRAM, %s)",
            index, name, vendor, vramMb, integrated ? "iGPU (Встроенная)" : "dGPU (Дискретная)");
    }
}
