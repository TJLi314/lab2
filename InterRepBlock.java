public class InterRepBlock {
    private Integer SR;
    private Integer VR;
    private Integer PR;
    private Integer NU;

    public InterRepBlock() {
        this.SR = null;
        this.VR = null;
        this.PR = null;
        this.NU = null;
    }

    public InterRepBlock(int SR) {
        this.SR = SR;
        this.VR = null;
        this.PR = null;
        this.NU = null;
    }

    public InterRepBlock(int SR, int VR, int PR, int NU) {
        this.SR = SR;
        this.VR = VR;
        this.PR = PR;
        this.NU = NU;
    }

    public Integer getSR() {
        return this.SR;
    }

    public Integer getVR() {
        return this.VR;
    }

    public Integer getPR() {
        return this.PR;
    }

    public Integer getNU() {
        return this.NU;
    }

    public void setSR(int SR) {
        this.SR = SR;
    }

    public void setVR(int VR) {
        this.VR = VR;
    }

    public void setPR(int PR) {
        this.PR = PR;
    }

    public void setNU(int NU) {
        this.NU = NU;
    }

    @Override
    public String toString() {
        if (SR != null && VR == null) {
            // For constants (e.g. loadI)
            return String.valueOf(SR);
        } else if (PR != null) {
            // For registers
            return "r" + PR;
        }
        return "";
    }

    // @Override
    // public String toString() {
    //     return String.format(
    //         "InterRepBlock { SR=%s, VR=%s, PR=%s, NU=%s }",
    //         SR != null ? SR : "null",
    //         VR != null ? "r" + VR : "null",
    //         PR != null ? PR : "null",
    //         NU != null ? NU : "null"
    //     );
    // }

}   