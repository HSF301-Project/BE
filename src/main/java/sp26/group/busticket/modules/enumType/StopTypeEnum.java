package sp26.group.busticket.modules.enumType;

/**
 * Loại hoạt động tại một điểm dừng trên tuyến.
 */
public enum StopTypeEnum {

    /** Chỉ đón khách lên xe tại điểm này. */
    PICKUP,

    /** Chỉ trả khách xuống xe tại điểm này. */
    DROPOFF,

    /** Vừa đón khách lên vừa trả khách xuống tại điểm này. */
    BOTH
}
