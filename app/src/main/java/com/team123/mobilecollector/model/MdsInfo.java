package com.team123.mobilecollector.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MdsInfo {

    @SerializedName("Content")
    Content content;

    public Content getContent() {
        return content;
    }

    public class Content {

        private String manufacturerName;

        private String brandName;

        private String productName;

        private String variant;

        private String design;

        private String hwCompatiblity;

        private String serial;

        private String pcbSerial;

        private String sw;

        private String hw;

        private String additionalVersionInfo;

        private String apiLevel;

        @SerializedName("addressInfo")
        private List<MdsAddressModel> addressInfoNew;

        public String getManufacturerName() {
            return manufacturerName;
        }

        public String getBrandName() {
            return brandName;
        }

        public String getProductName() {
            return productName;
        }

        public String getVariant() {
            return variant;
        }

        public String getDesign() {
            return design;
        }

        public String getHwCompatiblity() {
            return hwCompatiblity;
        }

        public String getSerial() {
            return serial;
        }

        public String getPcbSerial() {
            return pcbSerial;
        }

        public String getSw() {
            return sw;
        }

        public String getHw() {
            return hw;
        }

        public String getAdditionalVersionInfo() {
            return additionalVersionInfo;
        }

        public String getApiLevel() {
            return apiLevel;
        }

        public List<MdsAddressModel> getAddressInfoNew() {
            return addressInfoNew;
        }

        @Override
        public String toString() {
            return "Content{" +
                    "manufacturerName='" + manufacturerName + '\'' +
                    ", brandName='" + brandName + '\'' +
                    ", productName='" + productName + '\'' +
                    ", variant='" + variant + '\'' +
                    ", design='" + design + '\'' +
                    ", hwCompatiblity='" + hwCompatiblity + '\'' +
                    ", serial='" + serial + '\'' +
                    ", pcbSerial='" + pcbSerial + '\'' +
                    ", sw='" + sw + '\'' +
                    ", hw='" + hw + '\'' +
                    ", additionalVersionInfo='" + additionalVersionInfo + '\'' +
                    ", apiLevel='" + apiLevel + '\'' +
                    ", addressInfoNew=" + addressInfoNew +
                    '}';
        }
    }
}


