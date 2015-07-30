package tw.edu.ncu.cc.location.data.place;

public enum PlaceType {

    WHEELCHAIR_RAMP              ("無障礙坡道"),
    DISABLED_CAR_PARKING         ("無障礙汽車位"),
    DISABLED_MOTOR_PARKING       ("無障礙機車位"),
    EMERGENCY                    ("緊急"),
    AED                          ("AED"),
    RESTAURANT                   ("餐廳"),
    SPORT_RECREATION             ("休閒生活"),
    ADMINISTRATION               ("行政服務"),
    RESEARCH                     ("教學研究"),
    DORMITORY                    ("宿舍"),
    OTHER                        ("其他單位"),
    TOILET                       ("廁所"),
    ATM                          ("提款機"),
    BUS_STATION                  ("公車站牌"),
    PARKING_LOT                  ("停車場");

    private String cName;

    PlaceType (String cName) {
        this.cName = cName;
    }

    public String value() {
        return name();
    }

    public String getCName() {
        return this.cName;
    }

}
