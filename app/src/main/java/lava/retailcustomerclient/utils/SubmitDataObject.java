package lava.retailcustomerclient.utils;

import java.util.List;

import lava.retailcustomerclient.utils.DeviceInfoObject;
import lava.retailcustomerclient.utils.InstallRecordObject;


public class SubmitDataObject {

    /* installer data - who did */
    // will be added by promoter

    /* Install records - what he did */
    public List<AppInfoObject> installRecords;

    /* Device data - where he did */
    public DeviceInfoObject deviceDetails;

    /* Other details */
}
