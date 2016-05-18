package lava.retailcustomerclient.utils;

import java.util.List;

import lava.retailcustomerclient.utils.DeviceInfoObject;
import lava.retailcustomerclient.utils.InstallRecordObject;


/*
 *   ************************************************************
 *   ******************       WARNING            ****************
 *   ************************************************************
 *
 *   ENSURE THIS CLASS IS EXACTLY SAME IN RETAIL JUNCTION PROJECT
 *
 */

public class SubmitDataObject {

    /* installer data - who did */
    // will be added by promoter
    public PromoterInfoObject promoterInfo;

    /* Install records - what he did */
    public List<AppInfoObject> installRecords;

    /* Device data - where he did */
    public DeviceInfoObject deviceDetails;

    /* Other details */
}
