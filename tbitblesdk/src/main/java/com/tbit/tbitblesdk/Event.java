package com.tbit.tbitblesdk;

/**
 * Created by Salmon on 2016/4/29 0029.
 */
public class Event {

//    public static class WriteRXCharacteristic {
//        public byte[] sendData;
//
//        public WriteRXCharacteristic(byte[] sendData) {
//            this.sendData = sendData;
//        }
//    }
//
//    public static class UpdateStatus {
//        public boolean status;
//
//        public UpdateStatus(boolean status) {
//            this.status = status;
//        }
//    }
//
//    public static class BleConnectSucc {
//
//    }
//
//    public static class BleConnectFail {
//
//    }
//
//    public static class BleVoltageUpdate {
//        public byte voltage;
//
//        public BleVoltageUpdate(byte voltage) {
//            this.voltage = voltage;
//        }
//    }
//
//    public static class BleSpeedUpdate {
//        public byte speed;
//
//        public BleSpeedUpdate(byte speed) {
//            this.speed = speed;
//        }
//    }
//
//    public static class BleMileageUpdate {
//        public byte mileage;
//
//        public BleMileageUpdate(byte mileage) {
//            this.mileage = mileage;
//        }
//    }
//
    public static class SendSuccess {
        public int requestId;

        public SendSuccess(int requestId) {
            this.requestId = requestId;
        }
    }

}
